#**********************************************************************
# Copyright (c) 2000, 2012 Hewlett-Packard Development Company and others.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#********************************************************************** 
# Contributors:
#      Hewlett-Packard Development Company - added "-Iinclude" and "*.so" 
#		IBM - Ongoing development
#**********************************************************************

# makefile for ia64 libunixfile.so

CORE.C = ../unixfile.c
CORE.O = unixfile.o
LIB_NAME_FULL = libunixfile_1_0_0.so

core :
	cc +DD64 -mt +z -c -D_LARGEFILE64_SOURCE -I$(JAVA_HOME)/include/hp-ux -I$(JAVA_HOME)/include $(CORE.C) -o $(CORE.O)
	ld -b -o $(LIB_NAME_FULL) $(CORE.O)

clean :
	rm *.o *.so
