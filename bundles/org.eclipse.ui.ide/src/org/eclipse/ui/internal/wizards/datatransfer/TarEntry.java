/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	String name, linkdest, username, groupname;
	long mode, time, size;
	int type, uid, gid, major, minor;
	int filepos;

	/**
	 * Entry type for normal files.
	 */
	public static final int FILE = '0';

	/**
	 * Entry type for hard links.
	 */
	public static final int LINK = '1';
	
	/**
	 * Entry type for symbolic links.
	 */
	public static final int SYMLINK = '2';
	
	/**
	 * Entry type for character device nodes.
	 */
	public static final int CHAR_DEVICE = '3';
	
	/**
	 * Entry type for block device nodes.
	 */
	public static final int BLOCK_DEVICE = '4';
	
	/**
	 * Entry type for directories.
	 */
	public static final int DIRECTORY = '5';
	
	/**
	 * Entry type for FIFO files.
	 */
	public static final int FIFO = '6';

	/**
	 * Create a new TarEntry for a file of the given name at the
	 * given position in the file.
	 * 
	 * @param name filename
	 * @param pos position in the file in bytes
	 */
	TarEntry(String name, int pos) {
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
	 * Create a copy of the given TarEntry.
	 * 
	 * @param e entry to copy from
	 */
	public TarEntry(TarEntry e) {
		name = e.name;
		mode = e.mode;
		linkdest = e.linkdest;
		username = e.username;
		groupname = e.groupname;
		time = e.time;
		size = e.size;
		type = e.type;
		uid = e.uid;
		gid = e.gid;
		major = e.major;
		minor = e.minor;
		filepos = e.filepos;
	}

	/**
	 * If this file is a device inode, returns its major number.
	 * 
	 * @return major number of the device
	 */
	public int getDeviceMajor() {
		return major;
	}

	/**
	 * If this file is a device inode, returns its minor number.
	 * 
	 * @return minor number of the device
	 */
	public int getDeviceMinor() {
		return minor;
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
	 * Returns the group ID of the file.
	 * 
	 * @return group ID
	 */
	public int getGroupID() {
		return gid;
	}

	/**
	 * Returns the name of the group.  If not null, this should be used
	 * in favour of the group ID should an equivalent group be found.
	 * 
	 * @return group name
	 */
	public String getGroupName() {
		return groupname;
	}

	/**
	 * If this file represents a symbolic link or hard link, this is
	 * its destination path.
	 * 
	 * @return link destination
	 */
	public String getLinkDestination() {
		return linkdest;
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
	 * Returns the user ID of the owner of this file.
	 * 
	 * @return userid
	 */
	public int getUserID() {
		return uid;
	}
	
	/**
	 * Returns the name of the user that owns this file.  If not null,
	 * this should be used in favour of the user ID should an
	 * equivalent user be found.
	 * 
	 * @return user name
	 */
	public String getUserName() {
		return username;
	}

	/**
	 * Sets the major number of the device if this entry represents a device
	 * inode.
	 * 
	 * @param major
	 */
	public void setDeviceMajor(int major) {
		this.major = major;
	}

	/**
	 * Sets the minor number of the device if this entry represents a device
	 * inode.
	 * 
	 * @param minor
	 */
	public void setDeviceMinor(int minor) {
		this.minor = minor;
	}

	/**
	 * Sets the type of the file, one of FILE, LINK, SYMLINK, CHAR_DEVICE,
	 * BLOCK_DEVICE, or DIRECTORY.
	 * 
	 * @param type
	 */
	public void setFileType(int type) {
		this.type = type;
	}

	/**
	 * Sets the group ID of this file.
	 * 
	 * @param gid
	 */
	public void setGroupID(int gid) {
		this.gid = gid;
	}

	/**
	 * Sets the name of the group of this file.
	 * 
	 * @param groupname
	 */
	public void setGroupName(String groupname) {
		this.groupname = groupname;
	}

	/**
	 * If this file represents a symbolic or hard link, sets the path name
	 * of the destination.
	 * 
	 * @param linkdest
	 */
	public void setLinkDestination(String linkdest) {
		this.linkdest = linkdest;
	}

	/**
	 * Sets the mode of the file in UNIX permissions format.
	 * 
	 * @param mode
	 */
	public void setMode(long mode) {
		this.mode = mode;
	}

	/**
	 * Sets the size of the file in bytes.
	 * 
	 * @param size
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Sets the modification time of the file in seconds since January
	 * 1st 1970.
	 * 
	 * @param time
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * Sets the user ID of the owner of this file.
	 * 
	 * @param uid
	 */
	public void setUserID(int uid) {
		this.uid = uid;
	}

	/**
	 * Sets the name of the user who owns this file.
	 * 
	 * @param username
	 */
	public void setUserName(String username) {
		this.username = username;
	}
}
