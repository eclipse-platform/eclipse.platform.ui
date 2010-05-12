rem *******************************************************************************
rem  Copyright (c) 2005, 2008 IBM Corporation and others.
rem  All rights reserved. This program and the accompanying materials
rem  are made available under the terms of the Eclipse Public License v1.0
rem  which accompanies this distribution, and is available at
rem  http://www.eclipse.org/legal/epl-v10.html
rem 
rem  Contributors:
rem      IBM Corporation - initial API and implementation
rem *******************************************************************************
del update.obj
del update.dll
set win_include=%MSSDK%\include
set lib1=%MSSDK%\Lib\ia64
set lib2=%MSSDK%\VC\Lib\ia64
set lib_includes=%lib1%\UUID.LIB %lib2%\LIBCMT.LIB %lib2%\OLDNAMES.LIB %lib1%\KERNEL32.LIB
set jdk_include=%JAVA_HOME%\include
set dll_name=update

call "%MSSDK%\bin\SetEnv.Cmd" /IA64 /RELEASE /XP

cl -I%win_include% -I%jdk_include% -I%jdk_include%\win32 -LD update.cpp -Fe%dll_name% /link %lib_includes%  Mpr.lib
