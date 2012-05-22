/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filesystem.local.unix;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.internal.filesystem.local.NativeHandler;

/**
 * Native handler that delegates to UnixFileNatives
 */
public class UnixFileHandler extends NativeHandler {

	public int getSupportedAttributes() {
		return UnixFileNatives.getSupportedAttributes();
	}

	public FileInfo fetchFileInfo(String fileName) {
		return UnixFileNatives.fetchFileInfo(fileName);
	}

	public boolean putFileInfo(String fileName, IFileInfo info, int options) {
		return UnixFileNatives.putFileInfo(fileName, info, options);
	}
}
