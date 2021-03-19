This directory contains the source code for Unix-based platforms,
including Linux and Mac OS X. These platforms
all share the same C source, but have different makefiles.

The macOS library in the macosx fragment is a Mach-O universal binary with 
2 architectures: [x86_64:Mach-O 64-bit bundle x86_64] [arm64:Mach-O 64-bit bundle arm64]
XCode 12.2 or newer is required to build this.

Command to run:
make -f Makefile clean install