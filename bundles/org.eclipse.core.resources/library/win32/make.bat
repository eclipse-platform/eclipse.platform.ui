set win_include=k:\dev\products\msvc60\vc98\include
set jdk_include=d:\jdk1.2.2\include
set dll_name=core203.dll
cl -I%win_include% -I%jdk_include% -I%jdk_include%\win32 -LD core.c -Fe%dll_name%
