#**********************************************************************
# Copyright (c) 2008, 2010 Oakland Software Incorporated and others.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#	Oakland Software Incorporated - initial submission
#	IBM Corporation - refactoring, bug 245849
#
#********************************************************************** 
#
# makefile for libgnomeproxy-1.0.0.so

GNOMEPROXY.C = ../gnomeproxy.c
GNOMEPROXY.O = gnomeproxy.o
LIB_NAME = gnomeproxy.so
LIB_NAME_FULL = libgnomeproxy-1.0.0.so

OS_TYPE = linux
JDK_INCLUDE = -I${JAVA_HOME}/include -I${JAVA_HOME}/include/${OS_TYPE}
INCLUDE = -I/usr/include/gconf/2/ -I/usr/include/orbit-2.0/ -I/usr/include/glib-2.0/ -I/usr/lib/glib-2.0/include/
COMPILER_FLAGS = -O0 -fPIC -g3 -Wall -c -fmessage-length=0 -m32

LIBS := -lgconf-2 -lORBit-2 -lgthread-2.0 -lrt -lgobject-2.0 -lglib-2.0
LINKER_FLAGS= -m32

all: link

compile:
	@echo "Building file: $(GNOMEPROXY.O)"
	@echo "Invoking: GCC C Compiler"
	gcc $(INCLUDE) $(JDK_INCLUDE) $(COMPILER_FLAGS) -o $(GNOMEPROXY.O) $(GNOMEPROXY.C)
	@echo "Finished building: $(GNOMEPROXY.O)"
	@echo " "

link: compile
	@echo "Building target: $(LIB_NAME_FULL)"
	@echo "Invoking: GCC C Linker"
	gcc $(LINKER_FLAGS) -shared -Wl,-soname,$(LIB_NAME) -o $(LIB_NAME_FULL) $(LIBS) $(GNOMEPROXY.O) -lc
	@echo "Finished building target: $(LIB_NAME_FULL)"
	@echo " "

clean:
	-$(RM) $(GNOMEPROXY.O) $(LIB_NAME_FULL)
	-@echo " "
