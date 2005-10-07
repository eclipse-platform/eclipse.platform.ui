/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.filesystem.provider;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;

/**
 * This class should be used by file system providers in their implementation
 * of API methods that return {@link IFileInfo} objects.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @since 1.0
 */
public class FileInfo implements IFileInfo {
	/**
	 * Internal attribute indicating if the file exists.
	 */
	private static final int ATTRIBUTE_EXISTS = 1 << 16;

	/**
	 * Bit field of file attributes
	 */
	private int attributes = 0;

	/**
	 * The last modified time.
	 */
	private long lastModified = EFS.NONE;
	
	/**
	 * The file length.
	 */
	private long length = EFS.NONE;
	
	/**
	 * The file name.
	 */
	private String name = ""; //$NON-NLS-1$

	/**
	 * Creates a new file information object with default values.
	 */
	public FileInfo() {
		super();
	}

	/**
	 * Creates a new file information object. All values except the file name
	 * will have default values.
	 * 
	 * @param name The name of this file
	 */
	public FileInfo(String name) {
		super();
	}

	/**
	 * Convenience method to clear a masked region of the attributes bit field.
	 * 
	 * @param mask The mask to be cleared
	 */
	private void clear(int mask) {
		attributes &= ~mask;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
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
		return isSet(EFS.ATTRIBUTE_DIRECTORY);
	}

	public boolean isReadOnly() {
		return isSet(EFS.ATTRIBUTE_READ_ONLY);
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

	/**
	 * Sets whether this file or directory exists.
	 * 
	 * @param value <code>true</code> if this file exists, and <code>false</code>
	 * otherwise.
	 */
	public void setExists(boolean value) {
		setAttribute(ATTRIBUTE_EXISTS, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#setLastModified(long)
	 */
	public void setLastModified(long value) {
		lastModified = value;
	}

	/**
	 * Sets the length of this file. A value of {@link EFS#NONE}
	 * indicates the file does not exist, is a directory, or the length could not be computed.
	 * 
	 * @param value the length of this file, or {@link EFS#NONE}
	 */
	public void setLength(long value) {
		this.length = value;
	}

	/**
	 * Sets the name of this file.
	 * 
	 * @param name The file name
	 */
	public void setName(String name) {
		if (name == null)
			throw new IllegalArgumentException();
		this.name = name;
	}

	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		return name;
	}
}