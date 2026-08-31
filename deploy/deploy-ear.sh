#!/usr/bin/env bash
set -euo pipefail
: "${PAYARA_HOME:?PAYARA_HOME is required}"
"$PAYARA_HOME/bin/asadmin" deploy --force=true scms-ear/target/scms-ear-1.0.ear
