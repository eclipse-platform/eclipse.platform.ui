/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.ui.IFileEditorInput;

/**
 * FileInputAdapterFactory is the adapter factory for the
 * IFileEditorInput.
 * @since 3.2
 *
 */

public class FileInputAdapterFactory implements IAdapterFactory {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
	 *      java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IFile.class.equals(adapterType))
			return ((IFileEditorInput) adaptableObject).getFile();
		if (IResource.class.equals(adapterType))
			return ((IFileEditorInput) adaptableObject).getFile();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IFile.class, IResource.class };
	}
}
