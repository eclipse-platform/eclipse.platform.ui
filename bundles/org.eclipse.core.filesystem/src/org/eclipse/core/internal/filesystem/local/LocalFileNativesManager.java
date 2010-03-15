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
package org.eclipse.core.internal.filesystem.local;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.filesystem.local.unix.UnixFileNatives;

/**
 * Dispatches methods backed by native code to the appropriate platform specific 
 * implementation depending on a library provided by a fragment.
 */
public class LocalFileNativesManager {

	public static int getSupportedAttributes() {
		if (UnixFileNatives.isUsingNatives())
			return UnixFileNatives.getSupportedAttributes();
		return LocalFileNatives.attributes();
	}

	public static FileInfo fetchFileInfo(String fileName) {
		if (UnixFileNatives.isUsingNatives())
			return UnixFileNatives.fetchFileInfo(fileName);
		return LocalFileNatives.fetchFileInfo(fileName);
	}

	public static boolean putFileInfo(String fileName, IFileInfo info, int options) {
		if (UnixFileNatives.isUsingNatives())
			return UnixFileNatives.putFileInfo(fileName, info, options);
		return LocalFileNatives.putFileInfo(fileName, info, options);
	}

	public static boolean isUsingNatives() {
		return UnixFileNatives.isUsingNatives() || LocalFileNatives.isUsingNatives();
	}

}
