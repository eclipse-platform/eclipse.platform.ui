set win_include=k:\dev\products\msvc60\vc98\include
set jdk_include="d:\sun jdk1.4.1_01\include"
set dll_name=core_2_1_0a.dll
cl -I%win_include% -I%jdk_include% -I%jdk_include%\win32 -LD core.c -Fe%dll_name%
