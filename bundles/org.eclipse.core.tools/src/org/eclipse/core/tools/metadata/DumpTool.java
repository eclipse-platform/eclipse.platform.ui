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
import org.eclipse.core.runtime.IPlatformRunnable;

/**
 * A command-line interface for running dumpers on metadata files.
 * This application requires the name of the file to be dumped as its unique
 * argument.  
 */
public class DumpTool implements IPlatformRunnable {

	/**
	 * Dumps a given file using the associated dumper, sending its contents to the 
	 * standard output. 
	 * 
	 * @param args the command-line arguments
	 * @see DumperFactory#getDumper(String)
	 */	
	public Object run(Object args) throws Exception {		
		String fileName = System.getProperty("dump.file");
		if (fileName == null) {
			System.err.println("Use \"dump.file\" system property to point to the metadata file to be dumped"); //$NON-NLS-1$			
			return new Integer(0);
		}

		File toDump = new File(fileName);
		if (!toDump.isFile()) {
			System.err.println("File \"" + toDump.getAbsolutePath() + "\" does not exist or is not a file"); //$NON-NLS-1$			
			return new Integer(1);
		}

		IDumper dumper = null;
		try {
			dumper = DumperFactory.getInstance().getDumper(fileName);
		} catch (DumpException de) {
			System.err.println("Error: \n" + de); //$NON-NLS-1$
			return new Integer(1);
		}
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
		return new Integer(0);
	}
}