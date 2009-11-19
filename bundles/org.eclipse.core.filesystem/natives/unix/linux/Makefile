#**********************************************************************
# Copyright (c) 2000, 2009 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#********************************************************************** 
#
# makefile for libcore.so

CORE.C = ../localfile.c
CORE.O = localfile.o
LIB_NAME = liblocalfile.so
LIB_NAME_FULL = liblocalfile_1_0_0.so

#Set this to be your OS type
OS_TYPE = linux

#Set this to be the location of your JRE
JAVA_HOME = ~/vm/sun142

JDK_INCLUDE = -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/${OS_TYPE}
COMMON_INCLUDE = -I include
#OPT_FLAGS=-g
OPT_FLAGS=-O -s -D_LARGEFILE_SOURCE -D_FILE_OFFSET_BITS=64

core :
	gcc $(OPT_FLAGS) -fPIC -c $(JDK_INCLUDE) $(COMMON_INCLUDE) -o $(CORE.O) $(CORE.C)
	gcc $(OPT_FLAGS) -shared -Wl,-soname,$(LIB_NAME) -o $(LIB_NAME_FULL) $(CORE.O) -lc

clean :
	rm -f $(CORE.O) $(LIB_NAME_FULL)
