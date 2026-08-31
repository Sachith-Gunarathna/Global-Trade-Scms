#!/usr/bin/env bash
set -euo pipefail
: "${PAYARA_HOME:?PAYARA_HOME is required}"
: "${POSTGRES_DRIVER_JAR:?POSTGRES_DRIVER_JAR is required}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-globaltrade_scms}"
DB_USER="${DB_USER:-postgres}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"
SCMS_BOOTSTRAP_PASSWORD="${SCMS_BOOTSTRAP_PASSWORD:-Scms@1234}"
export SCMS_BOOTSTRAP_PASSWORD
ASADMIN="$PAYARA_HOME/bin/asadmin"
"$ASADMIN" start-domain domain1 || true
"$ASADMIN" add-library "$POSTGRES_DRIVER_JAR" || true
"$ASADMIN" delete-jdbc-resource jdbc/GlobalTradeDS || true
"$ASADMIN" delete-jdbc-connection-pool GlobalTradePool || true
"$ASADMIN" create-jdbc-connection-pool --restype javax.sql.DataSource --datasourceclassname org.postgresql.ds.PGSimpleDataSource --property "user=$DB_USER:password=$DB_PASSWORD:serverName=$DB_HOST:portNumber=$DB_PORT:databaseName=$DB_NAME" GlobalTradePool
"$ASADMIN" create-jdbc-resource --connectionpoolid GlobalTradePool jdbc/GlobalTradeDS
"$ASADMIN" ping-connection-pool GlobalTradePool
"$ASADMIN" set server.security-service.activate-default-principal-to-role-mapping=true
