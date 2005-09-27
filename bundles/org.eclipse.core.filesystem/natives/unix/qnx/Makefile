# makefile for libcore.so

CORE.C = ../localfile.c
CORE.O = localfile.o
LIB_NAME = liblocalfile.so
LIB_NAME_FULL = liblocalfile_1_0_0.so

core :
	qcc -Wall -O3 -shared -c -I$(IVE_HOME)/include $(CORE.C) -o $(CORE.O)
	qcc -Wall -O3 -shared -Wl,-soname,$(LIB_NAME) -o $(LIB_NAME_FULL) $(CORE.O)

clean :
	rm -f *.o *.so
