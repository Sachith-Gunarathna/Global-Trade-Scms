@echo off
if "%PAYARA_HOME%"=="" exit /b 1
if "%POSTGRES_DRIVER_JAR%"=="" exit /b 1
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=5432
if "%DB_NAME%"=="" set DB_NAME=globaltrade_scms
if "%DB_USER%"=="" set DB_USER=postgres
if "%DB_PASSWORD%"=="" exit /b 1
if "%SCMS_BOOTSTRAP_PASSWORD%"=="" set SCMS_BOOTSTRAP_PASSWORD=Scms@1234
set ASADMIN=%PAYARA_HOME%\bin\asadmin.bat
call "%ASADMIN%" start-domain domain1
call "%ASADMIN%" add-library "%POSTGRES_DRIVER_JAR%"
call "%ASADMIN%" delete-jdbc-resource jdbc/GlobalTradeDS
call "%ASADMIN%" delete-jdbc-connection-pool GlobalTradePool
call "%ASADMIN%" create-jdbc-connection-pool --restype javax.sql.DataSource --datasourceclassname org.postgresql.ds.PGSimpleDataSource --property user=%DB_USER%:password=%DB_PASSWORD%:serverName=%DB_HOST%:portNumber=%DB_PORT%:databaseName=%DB_NAME% GlobalTradePool
call "%ASADMIN%" create-jdbc-resource --connectionpoolid GlobalTradePool jdbc/GlobalTradeDS
call "%ASADMIN%" ping-connection-pool GlobalTradePool
call "%ASADMIN%" set server.security-service.activate-default-principal-to-role-mapping=true
