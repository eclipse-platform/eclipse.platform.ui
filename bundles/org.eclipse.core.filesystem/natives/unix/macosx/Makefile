#******************************************************************************
# Copyright (c) 2010 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     IBM Corporation - initial API and implementation
#******************************************************************************/
#
# makefile for libunixfile_1_0_0.jnilib

CORE.C=../unixfile.c
LIB_NAME_FULL=libunixfile_1_0_0.jnilib

JDK_INCLUDE=-I /System/Library/Frameworks/JavaVM.framework/Headers
FRAMEWORKS=-framework JavaVM -framework CoreServices
# define MACOSX to include Mac OS X specific code
CC_FLAGS=-arch i386 -arch ppc -arch x86_64 -mmacosx-version-min=10.4 -DMACOSX

core:
	cc $(JDK_INCLUDE) $(CORE.C) -o $(LIB_NAME_FULL) -bundle $(FRAMEWORKS) $(CC_FLAGS)

clean:
	rm -f $(LIB_NAME_FULL)

install:
	rm -f ../../../../org.eclipse.core.filesystem.macosx/os/macosx/$(LIB_NAME_FULL)
	mv $(LIB_NAME_FULL) ../../../../org.eclipse.core.filesystem.macosx/os/macosx/
