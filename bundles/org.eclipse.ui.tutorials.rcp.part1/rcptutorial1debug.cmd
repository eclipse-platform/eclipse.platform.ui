echo on
setlocal
cd %~dp0
java -cp startup.jar org.eclipse.core.launcher.Main -application org.eclipse.ui.tutorials.rcp.part1.RcpApplication -consoleLog %* || pause
endlocal
