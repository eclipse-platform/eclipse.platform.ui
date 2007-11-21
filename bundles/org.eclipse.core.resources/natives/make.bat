REM build JNI header file
cd ..\bin
d:\vm\sun141\bin\javah org.eclipse.core.internal.resources.refresh.win32.ref
move org_eclipse_core_internal_resources_refresh_win32_ref.h ..\a\ref.h

REM compile and link
cd ..\a
set win_include=k:\dev\products\msvc60\vc98\include
set jdk_include="d:\vm\sun141\include"
set dll_name=win32refresh.dll
cl -I%win_include% -I%jdk_include% -I%jdk_include%\win32 -LD ref.c -Fe%dll_name%
