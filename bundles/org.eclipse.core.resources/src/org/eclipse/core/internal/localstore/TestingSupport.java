/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Provides special internal access to the workspace resource implementation.
 * This class is to be used for testing purposes only.
 * 
 * @since 2.1
 */
public class TestingSupport {

	/* 
	 * Class cannot be instantiated.
	 */
	private TestingSupport() {
	}

	/**
	 * Call HistoryStore.accept().
	 */
	public static void accept(HistoryStore store, IPath path, IHistoryStoreVisitor visitor, boolean partialMatch) {
		store.accept(path, visitor, partialMatch);
	}
	/**
	 * Turn on tracing of calls to IFile.getContents.  Until tracing is turned off, it will
	 * track which files have been read but have not yet closed their file streams.
	 * This support can be used to track down cases where clients are failing to
	 * close their streams when reading files.
	 * <p>
	 * <b>Note:</b> turning read tracing on will greatly reduce the speed of file reads.
	 * This methods should only be used in test cases and as a debugging tool.
	 *
	 * @see stopTraceReads
	 * @since 3.0
	 */
	public static void startTraceReads() {
		((Workspace)ResourcesPlugin.getWorkspace()).getFileSystemManager().getStore().setTraceReads(true);
	}
	/**
	 * Turns off tracing of calls to IFile.getContents.  Produces a report to System.out
	 * of any open files that have not been closed, along with the stack trace when
	 * the file was opened.  For testing purposes only.
	 *
	 * @see startTraceReads
	 * @since 3.0
	 */
	public static void stopTraceReads() {
		((Workspace)ResourcesPlugin.getWorkspace()).getFileSystemManager().getStore().setTraceReads(false);
		TracingFileInputStream.dumpOpenFiles();
	}

}
