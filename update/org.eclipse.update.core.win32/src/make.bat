rem *******************************************************************************
rem  Copyright (c) 2005, 2006 IBM Corporation and others.
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
set win_include=k:\msvc60\vc98\include
set lib_includes=K:\Msvc60\VC98\Lib\UUID.LIB K:\Msvc60\VC98\Lib\LIBCMT.LIB K:\Msvc60\VC98\Lib\OLDNAMES.LIB K:\Msvc60\VC98\Lib\KERNEL32.LIB
set jdk_include=d:\vm\jdk1.4.2\include
set dll_name=update
cl -I%win_include% -I%jdk_include% -I%jdk_include%\win32 -LD update.cpp -Fe%dll_name% /link %lib_includes%
