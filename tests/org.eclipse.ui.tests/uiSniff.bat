@echo off
rem This is the UI Sniff test batch file.  

set CLASSPATH=plugins\org.eclipse.ui.test.harness.launcher\bin;startup.jar
set LOCATION=ui_sniff_folder

del /q /s %LOCATION%

.\jre\bin\java -cp %CLASSPATH% org.eclipse.ui.test.harness.launcher.TestUIMain -dev bin -data %LOCATION% -uiTest ui.TestSuite
