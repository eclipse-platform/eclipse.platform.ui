
APPVER=4.0
!include <win32.mak>

DLLNAME=core104.dll

LIBNAME=core

LIBPATH=

.c.obj:
	$(cc) $(cflags) $(cvarsmt) -DWIN32 /I. /IC:\jdk1.2.2\include /IC:\jdk1.2.2\include\win32 $*.c

BUILDFILES1=core.obj

MDLLIBFILES1 = g:\apps\tools\msvc40\lib\winmm.lib 

all: \
	 $(LIBNAME).lib $(DLLNAME)

BUILDLIB: $(LIBPATH)$(LIBNAME).lib

$(LIBPATH)$(LIBNAME).lib:\
	$(BUILDFILES1)
	echo -machine:$(CPU) >templrf
	echo -out:$(LIBPATH)$(LIBNAME).lib >>templrf
	echo -def:$(LIBNAME).def >>templrf
	echo $(BUILDFILES1) >>templrf
	$(implib) @templrf
	del templrf

$(DLLNAME): $(LIBPATH)$(LIBNAME).lib \
	$(BUILDFILES1) $(MDLLIBFILES1)
	echo $(dlllflags) >templrf
	echo -machine:$(CPU) >>templrf
	echo -subsystem:windows >>templrf
	echo -out:$(DLLNAME) >>templrf
	echo $(BUILDFILES1) >>templrf
	echo $(MDLLIBFILES1) >>templrf
	echo $(guilibsmt) $(LIBPATH)$(LIBNAME).exp >>templrf
	$(link) @templrf
	del templrf

clean:
        del *.obj
        del *.exp
