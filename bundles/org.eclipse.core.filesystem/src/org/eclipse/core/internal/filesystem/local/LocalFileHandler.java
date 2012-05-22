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
package org.eclipse.core.internal.filesystem.local;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.provider.FileInfo;

/**
 * Native handler that delegates to the LocalFileNatives.
 */
public class LocalFileHandler extends NativeHandler {

	public int getSupportedAttributes() {
		return LocalFileNatives.attributes();
	}

	public FileInfo fetchFileInfo(String fileName) {
		return LocalFileNatives.fetchFileInfo(fileName);
	}

	public boolean putFileInfo(String fileName, IFileInfo info, int options) {
		return LocalFileNatives.putFileInfo(fileName, info, options);
	}
}
