@echo off
rem This is the UI Sniff test batch file.  

set CLASSPATH=startup.jar

set LOCATION=ui_sniff_folder
del /Q /S %LOCATION%
echo Running UiTestSuite
.\jre\bin\java -cp %CLASSPATH% org.eclipse.core.launcher.UIMain -application org.eclipse.ui.junit.runner -dev bin -data %LOCATION% -testPluginName org.eclipse.ui.tests -className org.eclipse.ui.tests.UiTestSuite -console

set LOCATION=ui_session_sniff_folder
del /Q /S %LOCATION%
echo Running SessionCreateTest
.\jre\bin\java -cp %CLASSPATH% org.eclipse.core.launcher.UIMain -application org.eclipse.ui.junit.runner -dev bin -data %LOCATION% -testPluginName org.eclipse.ui.tests -className org.eclipse.ui.tests.api.SessionCreateTest -console
echo Running SessionRestoreTest
.\jre\bin\java -cp %CLASSPATH% org.eclipse.core.launcher.UIMain -application org.eclipse.ui.junit.runner -dev bin -data %LOCATION% -testPluginName org.eclipse.ui.tests -className org.eclipse.ui.tests.api.SessionRestoreTest -console

