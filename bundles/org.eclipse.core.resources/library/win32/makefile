
APPVER=4.0
!include <win32.mak>

DLLNAME=core102.dll# declaration

LIBNAME=core# declaration

LIBPATH=# declaration

.c.obj:
	$(cc) $(cflags) $(cvarsmt) -DWIN32 /I. /IC:\jdk1.2.2\include /IC:\jdk1.2.2\include\win32 $(VMDEBUG) $*.c

.asm.obj:
	ml /c /Cp /W3 /nologo /coff /Zm $(VMASMDEBUG)  $<

BUILDFILES1 = core.obj

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
