/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris McGee (IBM) - Bug 380325 - Release filesystem fragment providing Java 7 NIO2 support
 *******************************************************************************/
package org.eclipse.core.internal.filesystem.java7;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.filesystem.local.NativeHandler;

/**
 * Handler for LocalFile using only Java 1.7 API's.
 */
public class Java7Handler extends NativeHandler {
	private static int ATTRIBUTES = 0;
	private static boolean POSIX = false;
	private static boolean DOS = false;

	static {
		// Through posix view we can retrieve extra information about the unix permissions
		if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) { //$NON-NLS-1$
			ATTRIBUTES = EFS.ATTRIBUTE_READ_ONLY | EFS.ATTRIBUTE_EXECUTABLE /* These properties are mapped to owner read and owner execute via FileInfo implementation */
					| EFS.ATTRIBUTE_OWNER_READ | EFS.ATTRIBUTE_OWNER_WRITE | EFS.ATTRIBUTE_OWNER_EXECUTE | EFS.ATTRIBUTE_GROUP_READ | EFS.ATTRIBUTE_GROUP_WRITE | EFS.ATTRIBUTE_GROUP_EXECUTE | EFS.ATTRIBUTE_OTHER_READ | EFS.ATTRIBUTE_OTHER_WRITE | EFS.ATTRIBUTE_OTHER_EXECUTE | EFS.ATTRIBUTE_SYMLINK | EFS.ATTRIBUTE_LINK_TARGET;
			POSIX = true;
			// Through dos view we can retrieve and set archive, read-only, hidden
		} else if (FileSystems.getDefault().supportedFileAttributeViews().contains("dos")) { //$NON-NLS-1$
			ATTRIBUTES = EFS.ATTRIBUTE_READ_ONLY | EFS.ATTRIBUTE_ARCHIVE | EFS.ATTRIBUTE_HIDDEN | EFS.ATTRIBUTE_SYMLINK | EFS.ATTRIBUTE_LINK_TARGET;
			DOS = true;
		} else {
			ATTRIBUTES = EFS.ATTRIBUTE_READ_ONLY | EFS.ATTRIBUTE_EXECUTABLE | EFS.ATTRIBUTE_SYMLINK | EFS.ATTRIBUTE_LINK_TARGET;
		}
	}

	// Important, leave the default constructor. The LocalFileNativesManager is not expecting to pass
	//  any parameters to construct the object.
	public Java7Handler() {
		super();
	}

	public int getSupportedAttributes() {
		return ATTRIBUTES;
	}

	public FileInfo fetchFileInfo(String fileName) {
		Path p = Paths.get(fileName);

		FileInfo info = new FileInfo();
		// Caller will set the file name

		boolean exists = Files.exists(p);
		info.setExists(exists);

		// Even if it doesn't exist then check for symbolic link information
		boolean isSymbolicLink = Files.isSymbolicLink(p);
		if (isSymbolicLink) {
			info.setAttribute(EFS.ATTRIBUTE_SYMLINK, true);
			try {
				info.setStringAttribute(EFS.ATTRIBUTE_LINK_TARGET, Files.readSymbolicLink(p).toString());
			} catch (IOException e) {
				// Leave the target alone
				info.setError(IFileInfo.IO_ERROR);
			}
		}

		if (POSIX) {
			try {
				// If the destination of the link doesn't exist then we do not provide any more information
				//  about the link itself. This is consistent with the existing native implementation.
				if (!exists) {
					return info;
				}

				PosixFileAttributes attrs = Files.readAttributes(p, PosixFileAttributes.class);

				info.setLastModified(attrs.lastModifiedTime().toMillis());
				info.setLength(attrs.size());

				info.setDirectory(attrs.isDirectory());

				Set<PosixFilePermission> perms = attrs.permissions();
				info.setAttribute(EFS.ATTRIBUTE_OWNER_READ, perms.contains(PosixFilePermission.OWNER_READ));
				info.setAttribute(EFS.ATTRIBUTE_OWNER_WRITE, perms.contains(PosixFilePermission.OWNER_WRITE));
				info.setAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE, perms.contains(PosixFilePermission.OWNER_EXECUTE));
				info.setAttribute(EFS.ATTRIBUTE_GROUP_READ, perms.contains(PosixFilePermission.GROUP_READ));
				info.setAttribute(EFS.ATTRIBUTE_GROUP_WRITE, perms.contains(PosixFilePermission.GROUP_WRITE));
				info.setAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE, perms.contains(PosixFilePermission.GROUP_EXECUTE));
				info.setAttribute(EFS.ATTRIBUTE_OTHER_READ, perms.contains(PosixFilePermission.OTHERS_READ));
				info.setAttribute(EFS.ATTRIBUTE_OTHER_WRITE, perms.contains(PosixFilePermission.OTHERS_WRITE));
				info.setAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE, perms.contains(PosixFilePermission.OTHERS_EXECUTE));
			} catch (IOException e) {
				// Leave alone and continue
				info.setError(IFileInfo.IO_ERROR);
			}
		}

		else if (DOS) {
			try {
				if (!exists) {
					return info;
				}

				// use canonical file to get the correct case of filename
				String canonicalName = new File(fileName).getCanonicalFile().getName();
				info.setName(canonicalName);

				// To be consistent with the native implementation we do not follow a symbolic link
				//  and return back the information about the target. Instead, we provide the information
				//  about the symbolic link itself whether it exists or not.
				DosFileAttributes attrs = Files.readAttributes(p, DosFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

				info.setLastModified(attrs.lastModifiedTime().toMillis());
				info.setLength(attrs.size());

				info.setDirectory(attrs.isDirectory());
				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, attrs.isReadOnly());
				info.setAttribute(EFS.ATTRIBUTE_HIDDEN, attrs.isHidden());
				info.setAttribute(EFS.ATTRIBUTE_ARCHIVE, attrs.isArchive());
			} catch (IOException e) {
				// Leave alone and continue
				info.setError(IFileInfo.IO_ERROR);
			}
		}

		// If this is not POSIX nor DOS then try to use the general Files API capabilities from Java
		else {
			// Since we will be using a mixture of pre Java 7 API's which do not support the
			//  retrieval of information for the symbolic link itself instead of the target
			//  we will only support the following details if the symbolic link target exists.
			if (!exists) {
				return info;
			}

			info.setDirectory(Files.isDirectory(p));

			try {
				info.setLastModified(Files.getLastModifiedTime(p).toMillis());
				info.setLength(Files.size(p));

				info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !Files.isWritable(p) && Files.isReadable(p));
				info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, Files.isExecutable(p));
			} catch (IOException e) {
				// Leave alone and continue
				info.setError(IFileInfo.IO_ERROR);
			}
		}

		return info;
	}

	public boolean putFileInfo(String fileName, IFileInfo info, int options) {
		Path p = Paths.get(fileName);

		if (DOS) {
			// To be consistent with fetchInfo above we do not following symbolic links
			//  to set archive, read only and hidden attributes.
			DosFileAttributeView view = Files.getFileAttributeView(p, DosFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
			try {
				view.setArchive(info.getAttribute(EFS.ATTRIBUTE_ARCHIVE));
				view.setReadOnly(info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
				view.setHidden(info.getAttribute(EFS.ATTRIBUTE_HIDDEN));
			} catch (IOException e) {
				return false;
			}
		}

		else if (POSIX) {
			PosixFileAttributeView view = Files.getFileAttributeView(p, PosixFileAttributeView.class);
			Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();

			if (info.getAttribute(EFS.ATTRIBUTE_OWNER_READ))
				perms.add(PosixFilePermission.OWNER_READ);
			if (info.getAttribute(EFS.ATTRIBUTE_OWNER_WRITE))
				perms.add(PosixFilePermission.OWNER_WRITE);
			if (info.getAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE))
				perms.add(PosixFilePermission.OWNER_EXECUTE);
			if (info.getAttribute(EFS.ATTRIBUTE_GROUP_READ))
				perms.add(PosixFilePermission.GROUP_READ);
			if (info.getAttribute(EFS.ATTRIBUTE_GROUP_WRITE))
				perms.add(PosixFilePermission.GROUP_WRITE);
			if (info.getAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE))
				perms.add(PosixFilePermission.GROUP_EXECUTE);
			if (info.getAttribute(EFS.ATTRIBUTE_OTHER_READ))
				perms.add(PosixFilePermission.OTHERS_READ);
			if (info.getAttribute(EFS.ATTRIBUTE_OTHER_WRITE))
				perms.add(PosixFilePermission.OTHERS_WRITE);
			if (info.getAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE))
				perms.add(PosixFilePermission.OTHERS_EXECUTE);

			try {
				view.setPermissions(perms);
			} catch (IOException e) {
				return false;
			}
		}

		else {
			// In all cases we will set the information on the symbolic link target
			//  and not the symbolic link itself. This is consistent with the approach
			//  taken above in fetchInfo.
			File f = new File(fileName);
			boolean success = true;

			if (info.getAttribute(EFS.ATTRIBUTE_READ_ONLY)) {
				success = success && f.setReadOnly();
			} else {
				success = success && f.setWritable(true);
			}

			if (!success) {
				return false;
			}

			success = success && f.setExecutable(info.getAttribute(EFS.ATTRIBUTE_EXECUTABLE));

			if (!success) {
				return false;
			}
		}

		return true;
	}
}
