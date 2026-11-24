@echo off
setlocal

:: 1. Check if mvn is available in PATH
where mvn >nul 2>&1
if %errorlevel% EQU 0 (
    endlocal
    mvn %*
    goto :eof
)

:: 2. Check user-specific .m2 maven distribution
if exist "C:\Users\HP BENCH\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd" (
    endlocal
    call "C:\Users\HP BENCH\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd" %*
    goto :eof
)

:: 3. Check current user's profile .m2 wrapper
if exist "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd" (
    endlocal
    call "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd" %*
    goto :eof
)

echo [ERROR] Apache Maven was not found!
echo Please make sure Maven is installed and on your PATH, or check your .m2 wrapper installation.
endlocal
exit /b 1
