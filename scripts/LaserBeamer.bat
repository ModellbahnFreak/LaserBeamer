@echo off

set JAVA_BIN=java

%JAVA_BIN% -version >nul 2>nul
IF %ERRORLEVEL% EQU 0 GOTO begin

for %%f in (C:\Program Files\Java) do (
    set JAVA_BIN=%%f\bin\java.exe
    %JAVA_BIN% -version >nul 2>nul
    IF %ERRORLEVEL% EQU 0 GOTO begin
)

for %%f in (%userprofile%\.jdks) do (
    set JAVA_BIN=%%f\bin\java.exe
    %JAVA_BIN% -version >nul 2>nul
    IF %ERRORLEVEL% EQU 0 GOTO begin
)

echo java not found!
echo Make sure Java 17 or higher is installed to 'C:\Program Files\Java' or to '%userprofile%\.jdks' or added to PATH (i.e. 'java -version' works in the console).
exit /B 17

:begin
for %%f in (*.jar) do (
    %JAVA_BIN% -jar %%f %*
    goto end
)

:end