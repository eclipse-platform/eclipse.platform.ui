/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris McGee (IBM) - Bug 380325 - Release filesystem fragment providing Java 7 NIO2 support
 *     Sergey Prigogin (Google) - Bug 458006 - Fix tests that fail on Mac when filesystem.java7 is used
 *******************************************************************************/
package org.eclipse.core.internal.filesystem.java7;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.filesystem.local.NativeHandler;

/**
 * NativeHandler for POSIX using only Java 7 API's. It can be used for any
 * file system supporting POSIX family of standards.
 */
public class PosixHandler extends NativeHandler {
	private static final int ATTRIBUTES = EFS.ATTRIBUTE_SYMLINK | EFS.ATTRIBUTE_LINK_TARGET // symbolic link support
			| EFS.ATTRIBUTE_READ_ONLY | EFS.ATTRIBUTE_EXECUTABLE // mapped to owner read and owner execute via FileInfo implementation
			| EFS.ATTRIBUTE_OWNER_READ | EFS.ATTRIBUTE_OWNER_WRITE | EFS.ATTRIBUTE_OWNER_EXECUTE // owner
			| EFS.ATTRIBUTE_GROUP_READ | EFS.ATTRIBUTE_GROUP_WRITE | EFS.ATTRIBUTE_GROUP_EXECUTE // group
			| EFS.ATTRIBUTE_OTHER_READ | EFS.ATTRIBUTE_OTHER_WRITE | EFS.ATTRIBUTE_OTHER_EXECUTE; // other

	@Override
	public FileInfo fetchFileInfo(String fileName) {
		Path path = Paths.get(fileName);
		boolean exists = Files.exists(path);

		FileInfo info = new FileInfo();
		info.setExists(exists);

		// Even if it doesn't exist then check for symbolic link information.
		boolean isSymbolicLink = Files.isSymbolicLink(path);
		if (isSymbolicLink) {
			info.setAttribute(EFS.ATTRIBUTE_SYMLINK, true);
			try {
				info.setStringAttribute(EFS.ATTRIBUTE_LINK_TARGET, Files.readSymbolicLink(path).toString());
			} catch (IOException e) {
				// Leave the target alone.
				info.setError(IFileInfo.IO_ERROR);
			}
		}

		// Fill in the real name of the file.
		File file = new File(fileName);
		final String lastName = file.getName();
		// If the file does not exist, or the file system is not case sensitive, or the name
		// of the file is not case sensitive, use the name we have. Otherwise obtain the real
		// name of the file from a parent directory listing.
		//
		// Unfortunately, java.nio.file.Path.toRealPath(LinkOption.NOFOLLOW_LINKS) cannot be used
		// due to a JDK bug on Mac. As of JDK 1.8.0_25 the following code produces "/tmp/test.txt"
		// on Mac instead of "/tmp/Test.TXT":
		//   File file = new File("/tmp/Test.TXT");
		//   file.createNewFile();
		//   file = new File(file.toString().toLowerCase());
		//   String realName = file.toPath().toRealPath(LinkOption.NOFOLLOW_LINKS).toString();
		//
		// TODO(sprigogin): Provide a JDK bug reference once the bug report goes through Oracle review.
		if (!info.exists() || EFS.getLocalFileSystem().isCaseSensitive() || lastName.toLowerCase().equals(lastName.toUpperCase())) {
			info.setName(lastName);
		} else {
			// Notice that file.getParentFile() is guaranteed to be not null since fileName == "/"
			// case is handled by the other branch of the 'if' statement.
			String[] names = file.getParentFile().list(new FilenameFilter() {
				public boolean accept(File dir, String n) {
					return n.equalsIgnoreCase(lastName);
				}
			});
			if (names.length == 1) {
				info.setName(names[0]);
			} else {
				info.setName(lastName);
			}
		}

		// If the destination of the link doesn't exist then we do not provide any more information
		// about the link itself. This is consistent with the existing native implementation.
		if (!exists)
			return info;

		try {
			PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class);

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
			// Leave alone and continue.
			info.setError(IFileInfo.IO_ERROR);
		}
		return info;
	}

	@Override
	public int getSupportedAttributes() {
		return ATTRIBUTES;
	}

	@Override
	public boolean putFileInfo(String fileName, IFileInfo info, int options) {
		Path path = Paths.get(fileName);
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

		PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
		try {
			view.setPermissions(perms);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
