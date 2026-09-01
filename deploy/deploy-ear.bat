@echo off
if "%PAYARA_HOME%"=="" exit /b 1
set ASADMIN=%PAYARA_HOME%\bin\asadmin.bat

rem Remove the previous deployment first so deleted/renamed classes cannot remain in the exploded app.
call "%ASADMIN%" undeploy scms-ear-1.0 >nul 2>&1
call "%ASADMIN%" deploy --name scms-ear-1.0 scms-ear\target\scms-ear-1.0.ear
