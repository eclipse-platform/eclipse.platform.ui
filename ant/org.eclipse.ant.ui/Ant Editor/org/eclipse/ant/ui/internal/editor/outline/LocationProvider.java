/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.ui.internal.editor.outline;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * LocationProvider.java
 */
public class LocationProvider implements ILocationProvider {
	private IPath fPath;
	
	public LocationProvider() {
	}
	
	public LocationProvider(IFile file) {
		setLocationFromFile(file);
	}

	public void setLocationFromFile(IFile file) {
		fPath= file != null ? file.getLocation() : null;
	}

	public IPath getLocation() {
		return fPath;
	}
}
