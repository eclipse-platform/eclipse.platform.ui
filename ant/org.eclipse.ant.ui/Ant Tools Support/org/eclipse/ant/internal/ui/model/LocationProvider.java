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
package org.eclipse.ant.internal.ui.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;

public class LocationProvider {
	private IEditorInput fEditorInput;
	
	public LocationProvider(IEditorInput input) {
		fEditorInput= input;
	}

	public IPath getLocation() {
		if(fEditorInput instanceof IFileEditorInput) {
			return ((IFileEditorInput)fEditorInput).getFile().getLocation();
		} else if (fEditorInput instanceof ILocationProvider) {
			return ((ILocationProvider)fEditorInput).getPath(fEditorInput);
		}
		return null;
	}
	
	public IFile getFile() {
		if(fEditorInput instanceof IFileEditorInput) {
			return ((IFileEditorInput)fEditorInput).getFile();
		}
		return null;
	}
}
