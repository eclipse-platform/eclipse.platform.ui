/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filesystem.local.unix;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.provider.FileInfo;

/**
 * This class mirrors relevant fields of native struct stat
 * and is used by JNI calls wrapping OS file related functions.
 */
public class StructStat {

	public int st_mode;
	public long st_size;
	public long st_mtime;
	public long st_flags; // Filled only on Mac OS X

	public FileInfo toFileInfo() {
		FileInfo info = new FileInfo();
		info.setExists(true);
		info.setLength(st_size);
		info.setLastModified(st_mtime * 1000);
		if ((st_mode & UnixFileFlags.S_IFMT) == UnixFileFlags.S_IFDIR)
			info.setDirectory(true);
		if ((st_flags & (UnixFileFlags.UF_IMMUTABLE | UnixFileFlags.SF_IMMUTABLE)) != 0)
			info.setAttribute(EFS.ATTRIBUTE_IMMUTABLE, true);
		if ((st_mode & UnixFileFlags.S_IRUSR) == 0) // Set to true in FileInfo constructor
			info.setAttribute(EFS.ATTRIBUTE_OWNER_READ, false);
		if ((st_mode & UnixFileFlags.S_IWUSR) == 0) // Set to true in FileInfo constructor
			info.setAttribute(EFS.ATTRIBUTE_OWNER_WRITE, false);
		if ((st_mode & UnixFileFlags.S_IXUSR) != 0)
			info.setAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE, true);
		if ((st_mode & UnixFileFlags.S_IRGRP) != 0)
			info.setAttribute(EFS.ATTRIBUTE_GROUP_READ, true);
		if ((st_mode & UnixFileFlags.S_IWGRP) != 0)
			info.setAttribute(EFS.ATTRIBUTE_GROUP_WRITE, true);
		if ((st_mode & UnixFileFlags.S_IXGRP) != 0)
			info.setAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE, true);
		if ((st_mode & UnixFileFlags.S_IROTH) != 0)
			info.setAttribute(EFS.ATTRIBUTE_OTHER_READ, true);
		if ((st_mode & UnixFileFlags.S_IWOTH) != 0)
			info.setAttribute(EFS.ATTRIBUTE_OTHER_WRITE, true);
		if ((st_mode & UnixFileFlags.S_IXOTH) != 0)
			info.setAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE, true);
		return info;
	}

}
