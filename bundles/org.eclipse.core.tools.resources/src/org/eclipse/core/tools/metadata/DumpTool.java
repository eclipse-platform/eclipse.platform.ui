/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.metadata;

import java.io.File;

/**
 * A command-line interface for running dumpers on metadata files.
 * This application requires the name of the file to be dumped as its unique
 * argument.  
 */
public class DumpTool {

	/**
	 * Dumps a given file using the associated dumper, sending its contents to the 
	 * standard output. 
	 * 
	 * @param args the command-line arguments
	 * @see DumperFactory#getDumper(String)
	 */
	public void run(String[] args) {

		if (args.length < 1) {
			System.err.println("Usage:\tDumpTool <filename>"); //$NON-NLS-1$
			System.exit(0);
		}

		String fileName = args[0];

		try {
			IDumper dumper = DumperFactory.getInstance().getDumper(fileName);
			IDump dump = dumper.dump(new File(fileName));
			System.out.print("Dump for file: "); //$NON-NLS-1$
			System.out.println(dump.getFile().getAbsolutePath());
			System.out.print("Contents: "); //$NON-NLS-1$
			System.out.println(dump.getContents());
			if (dump.isFailed()) {
				System.out.print("*** Dump failed. Reason: "); //$NON-NLS-1$
				System.out.print(dump.getFailureReason());
			} else {
				System.out.print(">>> File is ok"); //$NON-NLS-1$
			}
			System.out.print(". Bytes read: "); //$NON-NLS-1$
			System.out.print(dump.getOffset());
			System.out.print(" / Total: "); //$NON-NLS-1$
			System.out.println(dump.getFile().length());

		} catch (DumpException de) {
			System.err.println("Error: \n" + de); //$NON-NLS-1$
			System.exit(1);
		}
	}

	/**
	 * Command-line entry point. Just instantiates DumpTool and invokes its 
	 * <code>run()</code> method passing arguments provided in the command-line.
	 * 
	 * @param args the command-line arguments
	 */
	public static void main(String[] args) {
		new DumpTool().run(args);
	}
}