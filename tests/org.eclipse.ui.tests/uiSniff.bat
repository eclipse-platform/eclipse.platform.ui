@echo off
rem This is the UI Sniff test batch file.  

set CLASSPATH=plugins\org.eclipse.ui.test.harness.launcher\bin;startup.jar

set LOCATION=ui_sniff_folder
del /q /s %LOCATION%
echo Running ui.testSuite
.\jre\bin\java -cp %CLASSPATH% org.eclipse.ui.test.harness.launcher.TestUIMain -dev bin -data %LOCATION% -uiTest ui.TestSuite

set LOCATION=ui_session_sniff_folder
del /q /s %LOCATION%
echo Running ui.api.SessionCreateTest
.\jre\bin\java -cp %CLASSPATH% org.eclipse.ui.test.harness.launcher.TestUIMain -dev bin -data %LOCATION% -uiTest ui.api.SessionCreateTest
echo Running ui.api.SessionRestoreTest
.\jre\bin\java -cp %CLASSPATH% org.eclipse.ui.test.harness.launcher.TestUIMain -dev bin -data %LOCATION% -uiTest ui.api.SessionRestoreTest

