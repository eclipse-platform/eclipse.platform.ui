del localfile.obj
del localfile_1_0_0*

set MSSDK=c:\Program Files\Microsoft SDKs\Windows\v6.0
set JAVA_HOME=c:\Program Files\Java\j2sdk1.4.2_18

call "%MSSDK%\bin\SetEnv.Cmd" /Release /x64 /xp

set win_include=%MSSDK%\include
set lib_includes="%MSSDK%\Lib\x64\UUID.LIB" "%MSSDK%\VC\LIB\x64\LIBCMT.LIB" "%MSSDK%\VC\LIB\x64\OLDNAMES.LIB" "%MSSDK%\Lib\x64\KERNEL32.LIB"
set jdk_include=%JAVA_HOME%\include
set dll_name=localfile_1_0_0

call "%MSSDK%\VC\Bin\x86_x64\cl" ..\localfile.c -I"%win_include%" -I"%jdk_include%" -I"%jdk_include%\win32" -LD -Fe%dll_name% /link %lib_includes% /Subsystem:CONSOLE
