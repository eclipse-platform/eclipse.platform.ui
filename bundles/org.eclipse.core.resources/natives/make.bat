@rem ***************************************************************************
@rem Copyright (c) 2007, 2014 IBM Corporation and others.
@rem All rights reserved. This program and the accompanying materials
@rem are made available under the terms of the Eclipse Public License v1.0
@rem which accompanies this distribution, and is available at
@rem http://www.eclipse.org/legal/epl-v10.html
@rem
@rem Contributors:
@rem     IBM Corporation - initial API and implementation
@rem ***************************************************************************
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
