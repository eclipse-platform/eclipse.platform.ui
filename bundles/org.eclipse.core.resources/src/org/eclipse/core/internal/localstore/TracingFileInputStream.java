/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.localstore;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

/**
 * For testing purposes only.  Tracks opens and closes of file input streams.
 * When requested, will dump a report of all open files, along with the stack
 * trace of the caller that opened it.
 * 
 * @see TestingSupport.startTraceReads
 * @since 3.0
 */
class TracingFileInputStream extends FileInputStream {
	/*
	 * Set of TracingFileInputStream instances that are open
	 */
	private static final HashSet openFiles = new HashSet();
	private java.io.File file;
	private RuntimeException openTrace;
	static void dumpOpenFiles() {
		for (Iterator it = openFiles.iterator(); it.hasNext();) {
			TracingFileInputStream stream = (TracingFileInputStream) it.next();
			System.out.println("------------"); //$NON-NLS-1$
			System.out.println("Open stream detected on file: " + stream.file); //$NON-NLS-1$
			System.out.println("Stack trace of open call:"); //$NON-NLS-1$
			stream.openTrace.printStackTrace();
		}
	}
	TracingFileInputStream(File file) throws FileNotFoundException {
		super(file);
		this.file = file;
		this.openTrace = new RuntimeException();
		openFiles.add(this);
	}
	public void close() throws IOException {
		this.file = null;
		this.openTrace = null;
		openFiles.remove(this);
		super.close();
	}
	public String toString() {
		return "TracingFileInputStream(" + file + ")";//$NON-NLS-1$ //$NON-NLS-2$
	}
}