#**********************************************************************
# Copyright (c) 2000, 2007 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# 
# Contributors:
#     IBM Corporation - initial API and implementation
# Martin Oberhuber (Wind River) - [183137] adapted from Linux for Solaris
#********************************************************************** 
#
# makefile for liblocalfile.so

CORE.C = ../localfile.c
CORE.O = localfile.o
LIB_NAME = liblocalfile.so
LIB_NAME_FULL = liblocalfile_1_0_0.so

#Set this to be your OS type
OS_TYPE = solaris

#Set this to be the location of your JRE
JAVA_HOME = ~/vm/sun142

JDK_INCLUDE = -I ${JAVA_HOME}/include -I ${JAVA_HOME}/include/${OS_TYPE}
COMMON_INCLUDE = -I include
#Solaris native cc uses -K PIC, gcc uses -fPIC
#To build native, use:     make
#To build with debug, use: make OPT_FLAGS=-g
#To build with gcc, use:   make CC=gcc PICFLAG=-fPIC
PICFLAG=-K PIC
#PICFLAG=-fPIC
OPT_FLAGS=-O -s
#OPT_FLAGS=-g

$(LIB_NAME_FULL) : $(CORE.C)
	$(CC) $(OPT_FLAGS) $(PICFLAG) -c $(JDK_INCLUDE) $(COMMON_INCLUDE) -o $(CORE.O) $(CORE.C)
	$(CC) $(OPT_FLAGS) -G -o $(LIB_NAME_FULL) $(CORE.O) -lc

core : $(LIB_NAME_FULL)

clean :
	rm *.o

install : $(LIB_NAME_FULL)
	cp -f $(LIB_NAME_FULL) ../../../../org.eclipse.core.filesystem.solaris.sparc/os/solaris/sparc/
