/***********************************************************************
* Copyright (c) 2009 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials 
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors:
*     IBM Corporation - initial API and implementation
***********************************************************************/

/* Use this directive when introducing platform-specific code in localfile.c */
#ifndef AIX
#define AIX
#endif

/* Linux supports reading symbolic links */
#ifndef EFS_SYMLINK_SUPPORT
#define EFS_SYMLINK_SUPPORT
#endif
#include <limits.h>
#include <unistd.h>
