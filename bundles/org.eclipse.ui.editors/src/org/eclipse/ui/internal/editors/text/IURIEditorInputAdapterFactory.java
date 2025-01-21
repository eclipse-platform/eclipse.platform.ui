/*******************************************************************************
 * Copyright (c) 2007, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.net.URI;

import org.eclipse.core.filesystem.URIUtil;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;

import org.eclipse.ui.IURIEditorInput;

import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.editors.text.ILocationProviderExtension;


/**
 * Adapter factory for <code>IURIEditorInput</code>.
 *
 * @since 3.3
 */
public class IURIEditorInputAdapterFactory implements IAdapterFactory {

	private static class LocationProvider implements ILocationProvider, ILocationProviderExtension {
		/*
		 * @see org.eclipse.ui.editors.text.ILocationProvider#getLocation(java.lang.Object)
		 */
		@Override
		public IPath getPath(Object element) {
			URI uri= getURI(element);
			if (uri != null)
				return URIUtil.toPath(uri);
			return null;
		}

		@Override
		public URI getURI(Object element) {
			if (element instanceof IURIEditorInput) {
				IURIEditorInput input= (IURIEditorInput)element;
				return input.getURI();
			}
			return null;
		}
	}


	/** The list of provided adapters. */
	private static final Class<?>[] ADAPTER_LIST = { ILocationProvider.class };

	/** The provided location provider */
	private ILocationProvider fLocationProvider= new LocationProvider();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (ILocationProvider.class.equals(adapterType)) {
			if (adaptableObject instanceof IURIEditorInput)
				return (T) fLocationProvider;
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTER_LIST;
	}
}
