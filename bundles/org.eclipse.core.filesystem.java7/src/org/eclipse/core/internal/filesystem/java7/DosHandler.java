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
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.filesystem.local.NativeHandler;

/**
 * NativeHandler for file system that supports legacy "DOS" attributes using
 * only Java 7 API's. It can be used for DOS/Windows file systems.
 */
public class DosHandler extends NativeHandler {
	private static final int ATTRIBUTES = EFS.ATTRIBUTE_SYMLINK | EFS.ATTRIBUTE_LINK_TARGET // symbolic link support
			| EFS.ATTRIBUTE_ARCHIVE | EFS.ATTRIBUTE_READ_ONLY | EFS.ATTRIBUTE_HIDDEN; // standard DOS attributes

	@Override
	public FileInfo fetchFileInfo(String fileName) {
		Path path = Paths.get(fileName);
		boolean exists = Files.exists(path, LinkOption.NOFOLLOW_LINKS);

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

		// See bug 431983.
		if (!exists)
			return info;

		try {
			// Use canonical file to get the correct case of filename. See bug 431983.
			String canonicalName = new File(fileName).getCanonicalFile().getName();
			info.setName(canonicalName);

			// To be consistent with the native implementation we do not follow a symbolic link
			// and return back the information about the target. Instead, we provide the information
			// about the symbolic link itself whether it exists or not.
			DosFileAttributes attrs = Files.readAttributes(path, DosFileAttributes.class, LinkOption.NOFOLLOW_LINKS);

			info.setLastModified(attrs.lastModifiedTime().toMillis());
			info.setLength(attrs.size());
			// Follow symbolic links because symbolic link targeting a directory is considered a directory.
			info.setDirectory(Files.isDirectory(path));

			info.setAttribute(EFS.ATTRIBUTE_ARCHIVE, attrs.isArchive());
			info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, attrs.isReadOnly());
			info.setAttribute(EFS.ATTRIBUTE_HIDDEN, attrs.isHidden());
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
		// To be consistent with fetchInfo do not following symbolic links to set archive, read only and hidden attributes.
		DosFileAttributeView view = Files.getFileAttributeView(path, DosFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
		try {
			view.setArchive(info.getAttribute(EFS.ATTRIBUTE_ARCHIVE));
			view.setReadOnly(info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
			view.setHidden(info.getAttribute(EFS.ATTRIBUTE_HIDDEN));
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
