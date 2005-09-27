# makefile for ia64 libcore.so

CORE.C = ../localfile.c
CORE.O = localfile.o
LIB_NAME = liblocalfile.so
LIB_NAME_FULL = liblocalfile_1_0_0.so

core :
	cc +z -c +O3 +DD32 +DSblended -I$(JDK_INCLUDE)/hp-ux -I$(JDK_INCLUDE) $(CORE.C) -o $(CORE.O)
	ld -b -o $(LIB_NAME_FULL) $(CORE.O) -lc

clean :
	rm *.o
