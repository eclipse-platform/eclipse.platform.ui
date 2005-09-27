#**********************************************************************
# Copyright (c) 2000, 2005 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#********************************************************************** 
#
# makefile for libcore on MacOS X


LIB_NAME = libcore_3_1_0.jnilib

core:
	cc -I /System/Library/Frameworks/JavaVM.framework/Headers core.c -o $(LIB_NAME) -bundle -framework JavaVM -framework CoreServices
	mv $(LIB_NAME) ../os/macosx/ppc

clean:
	rm *.o
	