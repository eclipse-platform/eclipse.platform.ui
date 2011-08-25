/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo;

import java.net.URI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdapterFactory;

public class AdapterFactory implements IAdapterFactory {

	private IWorkspace workspace;

	public AdapterFactory() {
		// TODO the following is bad - adapter factories should be able to have
		// dependencies injected!
		this.workspace = ResourcesPlugin.getWorkspace();
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof Exif) {
			if (IFile.class.equals(adapterType)) {
				URI uri = ((Exif) adaptableObject).getUri();
				if (uri != null) {
					IFile[] files = workspace.getRoot()
							.findFilesForLocationURI(uri);
					if (files.length == 1) {
						IFile file = files[0];
						if (file.exists()) {
							return file;
						}
					}
				}
			}
		}
		return null;
	}

	public Class[] getAdapterList() {
		return new Class[] { Exif.class };
	}

}
