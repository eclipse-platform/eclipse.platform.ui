@rem ***************************************************************************
@rem Copyright (c) 2014 IBM Corporation and others.
@rem All rights reserved. This program and the accompanying materials
@rem are made available under the terms of the Eclipse Public License v1.0
@rem which accompanies this distribution, and is available at
@rem http://www.eclipse.org/legal/epl-v10.html
@rem
@rem Contributors:
@rem     IBM Corporation - initial API and implementation
@rem ***************************************************************************
del localfile.obj
del localfile_1_0_0*

call "%MSSDK%\bin\SetEnv.Cmd" /Release /ia64 /xp

set win_include=%MSSDK%\include
set lib_includes=UUID.LIB LIBCMT.LIB OLDNAMES.LIB KERNEL32.LIB
set jdk_include=%JAVA_HOME%\include
set dll_name=localfile_1_0_0

cl localfile.c -I%win_include% -I%jdk_include% -I%jdk_include%\win32 -LD -Fe%dll_name% /link %lib_includes%
