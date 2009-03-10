del localfile.obj
del localfile_1_0_0*

call "%MSSDK%\bin\SetEnv.Cmd" /Release /ia64 /xp

set win_include=%MSSDK%\include
set lib_includes=UUID.LIB LIBCMT.LIB OLDNAMES.LIB KERNEL32.LIB
set jdk_include=%JAVA_HOME%\include
set dll_name=localfile_1_0_0

cl localfile.c -I%win_include% -I%jdk_include% -I%jdk_include%\win32 -LD -Fe%dll_name% /link %lib_includes%
