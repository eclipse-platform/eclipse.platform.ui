/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.filesystem.URIUtil;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;

import org.eclipse.ui.editors.text.ILocationProvider;

import org.eclipse.ui.IURIEditorInput;


/**
 * Adapter factory for <code>IURIEditorInput</code>.
 * 
 * @since 3.3
 */
public class IURIEditorInputAdapterFactory implements IAdapterFactory {

	private static class LocationProvider implements ILocationProvider {
		/*
		 * @see org.eclipse.ui.editors.text.ILocationProvider#getLocation(java.lang.Object)
		 */
		public IPath getPath(Object element) {
			if (element instanceof IURIEditorInput) {
				IURIEditorInput input= (IURIEditorInput)element;
				return URIUtil.toPath(input.getURI());
			}
			return null;
		}
	}

	/** The list of provided adapters. */
	private static final Class[] ADAPTER_LIST= new Class[] { ILocationProvider.class };

	/** The provided location provider */
	private ILocationProvider fLocationProvider= new LocationProvider();

	/*
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (ILocationProvider.class.equals(adapterType)) {
			if (adaptableObject instanceof IURIEditorInput)
				return fLocationProvider;
		}
		return null;
	}

	/*
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return ADAPTER_LIST;
	}
}
