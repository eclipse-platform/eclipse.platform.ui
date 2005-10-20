#**********************************************************************
# Copyright (c) 2000, 2004 Hewlett-Packard Development Company and others.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#********************************************************************** 
# Contributors:
#      Hewlett-Packard Development Company - added "-Iinclude" and "*.so" 
#**********************************************************************

# makefile for ia64_32 liblocalfile.so

CORE.C = ../localfile.c
CORE.O = localfile.o
LIB_NAME = liblocalfile.so
LIB_NAME_FULL = liblocalfile_1_0_0.so

core :
	cc +z -c +O3 +DD32 +DSblended -Iinclude -I$(JDK_INCLUDE)/hp-ux -I$(JDK_INCLUDE) $(CORE.C) -o $(CORE.O)
	ld -b -o $(LIB_NAME_FULL) $(CORE.O) -lc

clean :
	rm *.o *.so
