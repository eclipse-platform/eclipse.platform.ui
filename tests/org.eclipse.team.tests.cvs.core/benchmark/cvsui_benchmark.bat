@echo off
rem Team UI benchmark script
rem Expects the following plugins to be installed:
rem   org.eclipse.core.tests.harness
rem   org.eclipse.team.core
rem   org.eclipse.team.cvs.core
rem   org.eclipse.team.cvs.ui
rem   org.eclipse.team.tests.cvs
rem   org.eclipse.team.tests.cvs.core
rem   org.eclipse.team.ui
rem   org.junit

set ROOT=D:\PerformanceTesting

set ECLIPSE=%ROOT%\eclipse
set REPOSITORY_PROPERTIES=%ROOT%\repository.properties

rem set TEST=cvsui.benchmark.all
rem set REPEAT=25
rem set IGNOREFIRST=-ignorefirst

set TEST=cvsui.benchmark.command
set REPEAT=25
set IGNOREFIRST=-ignorefirst

set LOG=%ROOT%\%TEST%.xml
set WORKSPACE=%ECLIPSE%\workspace
set JRE=%ROOT%\jre
set JAVA=%JRE%\bin\java.exe
set HARNESS=org.eclipse.team.tests.cvs.core.harness

set VMARGS=-Declipse.cvs.properties=%REPOSITORY_PROPERTIES%
set PROGARGS=-dev bin -noupdate -application %HARNESS% -test %TEST% -log %LOG% -purge -repeat %REPEAT% %IGNOREFIRST%

pushd %ECLIPSE%
echo Purging the workspace: %WORKSPACE%
del /S /F /Q %WORKSPACE% >NUL:
@echo on
@echo Running Team UI benchmark test
%JAVA% -cp startup.jar %VMARGS% org.eclipse.core.launcher.Main %PROGARGS%
@echo off
popd