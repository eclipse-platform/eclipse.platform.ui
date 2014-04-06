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
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.filesystem.local.NativeHandler;

/**
 * Default implementation of NativeHandler using only Java 7 API's. It can be
 * used in cases where neither a DOS/Windows nor POSIX file system could be
 * detected.
 */
public class DefaultHandler extends NativeHandler {
	private static final int ATTRIBUTES = EFS.ATTRIBUTE_SYMLINK | EFS.ATTRIBUTE_LINK_TARGET // symbolic link support
			| EFS.ATTRIBUTE_READ_ONLY | EFS.ATTRIBUTE_EXECUTABLE; // based on Java API

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

		// Since we will be using a mixture of pre Java 7 API's which do not support the
		// retrieval of information for the symbolic link itself instead of the target
		// we will only support the following details if the symbolic link target exists.
		if (!exists)
			return info;

		try {
			info.setLastModified(Files.getLastModifiedTime(path).toMillis());
			info.setLength(Files.size(path));
			info.setDirectory(Files.isDirectory(path));

			info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !Files.isWritable(path) && Files.isReadable(path));
			info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, Files.isExecutable(path));
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
		// In all cases we will set the information on the symbolic link target
		// and not the symbolic link itself. This is consistent with the approach
		// taken above in fetchInfo.
		File file = new File(fileName);
		if (info.getAttribute(EFS.ATTRIBUTE_READ_ONLY)) {
			if (!file.setReadOnly())
				return false;
		} else {
			if (!file.setWritable(true))
				return false;
		}
		// If setExecutable succeeds, then the whole method succeeds.
		return file.setExecutable(info.getAttribute(EFS.ATTRIBUTE_EXECUTABLE));
	}
}
