#**********************************************************************
# Copyright (c) 2009, 2010 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#********************************************************************** 
#
# makefile for libunixfile.so

CORE.C = ../unixfile.c
CORE.O = unixfile.o
LIB_NAME_FULL = libunixfile_1_0_0.a

OS_TYPE=aix
CC=xlC_r
LD=ld
CFLAGS=-O -s -q32 -qwarn64
LDFLAGS=-b32 -G -s -bnoentry -bexpall -lc
JAVA_HOME=/usr/java5/
JDK_INCLUDE=-I${JAVA_HOME}/include -I${JAVA_HOME}/include/${OS_TYPE}

core :
	$(CC) $(CFLAGS) $(JDK_INCLUDE) -c $(CORE.C) -o $(CORE.O)
	$(LD) $(LDFLAGS) -o $(LIB_NAME_FULL) $(CORE.O)
clean :
	rm -f *.o *.a
