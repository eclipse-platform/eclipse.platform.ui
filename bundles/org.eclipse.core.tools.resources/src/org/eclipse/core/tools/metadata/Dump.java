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
 * Default implementation for <code>IDump</code>. 
 * 
 * @see IDump
 */
public class Dump implements IDump {
	/**
	 * The file processed in order to produce this dump
	 */
	private File file;
	/**
	 * The failure flag for this dump object.
	 */
	private boolean failed;
	/**
	 * The exception that caused the failure (may be null) 
	 */
	private Exception failureReason;
	/**
	 * An object that represents the dump contents.
	 */
	private Object contents;
	/**
	 * The offset where the dumper stopped when reading the dumped file.
	 */
	private long offset;

	/**
	 * Constructs a Dump object to be initialized using the setter methods.
	 */
	public Dump() {
		super();
	}

	/**
	 * @see org.eclipse.core.tools.metadata.IDump#getFile()
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @see org.eclipse.core.tools.metadata.IDump#isFailed()
	 */
	public boolean isFailed() {
		return failed || this.failureReason != null;
	}

	/**
	 * @see org.eclipse.core.tools.metadata.IDump#getFailureReason()
	 */
	public Exception getFailureReason() {
		return failureReason;
	}

	/**
	 * Sets this dump object failed flag.
	 * 
	 * @param failed The value of the failed flag for this dump object
	 */
	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	/**
	 * Sets the failure reason.
	 * 
	 * @param failureReason The failure reason for this dump object
	 */
	public void setFailureReason(Exception failureReason) {
		this.failureReason = failureReason;
	}

	/**
	 * Sets the file.
	 * 
	 * @param file The file for this dump object
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @see IDump#getContents()
	 */
	public Object getContents() {
		return contents;
	}

	/**
	 * Sets the contents.
	 * 
	 * @param contents The contents for this dump object
	 */
	public void setContents(Object contents) {
		this.contents = contents;
	}

	/**
	 * Sets the offset.
	 * 
	 * @param offset The offset for this dump object
	 */
	public void setOffset(long offset) {
		this.offset = offset;
	}

	/**
	 * @see IDump#getOffset()
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * Returns a human-readable representation for this dump object (for 
	 * debugging purposes).
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "File: " + getFile() + "\n" + "Contents: \n******\n" + getContents() + "\n******\n" + "Failed: " + failed + "\n" + "Reason: " + failureReason + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	}

}