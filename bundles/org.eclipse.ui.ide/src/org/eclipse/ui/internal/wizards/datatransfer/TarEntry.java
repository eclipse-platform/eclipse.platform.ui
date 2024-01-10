/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

/**
 * Representation of a file in a tar archive.
 *
 * @since 3.1
 */
public class TarEntry implements Cloneable
{
	private String name;
	private long mode, time, size;
	private int type;
	long filepos;

	/**
	 * Entry type for normal files.
	 */
	public static final int FILE = '0';

	/**
	 * Entry type for directories.
	 */
	public static final int DIRECTORY = '5';

	/**
	 * Create a new TarEntry for a file of the given name at the
	 * given position in the file.
	 *
	 * @param name filename
	 * @param pos position in the file in bytes
	 */
	TarEntry(String name, long pos) {
		this.name = name;
		mode = 0644;
		type = FILE;
		filepos = pos;
		time = System.currentTimeMillis() / 1000;
	}

	/**
	 * Create a new TarEntry for a file of the given name.
	 *
	 * @param name filename
	 */
	public TarEntry(String name) {
		this(name, -1);
	}

	/**
	 * Returns the type of this file, one of FILE, LINK, SYM_LINK,
	 * CHAR_DEVICE, BLOCK_DEVICE, DIRECTORY or FIFO.
	 *
	 * @return file type
	 */
	public int getFileType() {
		return type;
	}

	/**
	 * Returns the mode of the file in UNIX permissions format.
	 *
	 * @return file mode
	 */
	public long getMode() {
		return mode;
	}

	/**
	 * Returns the name of the file.
	 *
	 * @return filename
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the size of the file in bytes.
	 *
	 * @return filesize
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Returns the modification time of the file in seconds since January
	 * 1st 1970.
	 *
	 * @return time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets the type of the file, one of FILE, LINK, SYMLINK, CHAR_DEVICE,
	 * BLOCK_DEVICE, or DIRECTORY.
	 */
	public void setFileType(int type) {
		this.type = type;
	}

	/**
	 * Sets the mode of the file in UNIX permissions format.
	 */
	public void setMode(long mode) {
		this.mode = mode;
	}

	/**
	 * Sets the size of the file in bytes.
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Sets the modification time of the file in seconds since January
	 * 1st 1970.
	 */
	public void setTime(long time) {
		this.time = time;
	}
}
