/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.outline;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

public class LocationProvider {
	private IFile fFile;
	
	public LocationProvider(IFile file) {
		fFile= file;
	}

	public IPath getLocation() {
		return fFile != null ? fFile.getLocation() : null;
	}
	
	public IFile getFile() {
		return fFile;
	}
}
