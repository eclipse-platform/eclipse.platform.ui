/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.filesystem;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStoreConstants;

/**
 * 
 */
public class FileInfo implements IFileInfo, IFileStoreConstants {
	/**
	 * Internal attribute indicating if the file exists.
	 */
	private static final int ATTRIBUTE_EXISTS = 1 << 16;
	
	private int attributes = 0;

	private long lastModified = IFileStoreConstants.INVALID;
	private long length = IFileStoreConstants.INVALID;
	private String name = ""; //$NON-NLS-1$

	/**
	 * Creates a new file information object with default values.
	 */
	public FileInfo() {
		super();
	}

	private void clear(int mask) {
		attributes &= ~mask;
	}

	public int compareTo(Object o) {
		return name.compareTo(((FileInfo) o).name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#exists()
	 */
	public boolean exists() {
		return getAttribute(ATTRIBUTE_EXISTS);
	}

	public boolean getAttribute(int attribute) {
		return isSet(attribute);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#lastModified()
	 */
	public long getLastModified() {
		return lastModified;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#length()
	 */
	public long getLength() {
		return length;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#isDirectory()
	 */
	public boolean isDirectory() {
		return isSet(ATTRIBUTE_DIRECTORY);
	}

	public boolean isReadOnly() {
		return isSet(ATTRIBUTE_READ_ONLY);
	}

	private boolean isSet(long mask) {
		return (attributes & mask) != 0;
	}

	private void set(int mask) {
		attributes |= mask;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#setAttribute(int, boolean)
	 */
	public void setAttribute(int attribute, boolean value) {
		if (value)
			set(attribute);
		else
			clear(attribute);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#setLastModified(long)
	 */
	public void setLastModified(long value) {
		lastModified = value;
	}

	public void setLength(long value) {
		this.length = value;
	}

	public void setName(String name) {
		if (name == null)
			throw new IllegalArgumentException();
		this.name = name;
	}

	public void setExists(boolean value) {
		setAttribute(ATTRIBUTE_EXISTS, value);
	}
	
	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		return name;
	}
}