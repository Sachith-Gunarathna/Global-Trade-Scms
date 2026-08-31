@echo off
if "%PAYARA_HOME%"=="" exit /b 1
call "%PAYARA_HOME%\bin\asadmin.bat" deploy --force=true scms-ear\target\scms-ear-1.0.ear
