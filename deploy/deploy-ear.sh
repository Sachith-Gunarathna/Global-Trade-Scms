#!/usr/bin/env sh
set -e
[ -n "$PAYARA_HOME" ] || exit 1
ASADMIN="$PAYARA_HOME/bin/asadmin"

# Remove the previous deployment first so deleted/renamed classes cannot remain in the exploded app.
"$ASADMIN" undeploy scms-ear-1.0 >/dev/null 2>&1 || true
"$ASADMIN" deploy --name scms-ear-1.0 scms-ear/target/scms-ear-1.0.ear
