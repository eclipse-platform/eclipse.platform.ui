# makefile for libcore.so

CORE.C = core.c
CORE.O = core.o
LIB_NAME = libcore.so
LIB_NAME_FULL = libcore_2_1_1.so

core :
	gcc -fPIC -g -c -I$(JDK_INCLUDE) $(CORE.C) -o $(CORE.O)
	gcc -g -shared -Wl,-soname,$(LIB_NAME) -o $(LIB_NAME_FULL) $(CORE.O) -lc

clean :
	rm *.o
