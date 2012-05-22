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
 * This delegate provides the interface for native file attribute support.
 */
public abstract class NativeHandler {
	public abstract int getSupportedAttributes();

	public abstract FileInfo fetchFileInfo(String fileName);

	public abstract boolean putFileInfo(String fileName, IFileInfo info, int options);
}
