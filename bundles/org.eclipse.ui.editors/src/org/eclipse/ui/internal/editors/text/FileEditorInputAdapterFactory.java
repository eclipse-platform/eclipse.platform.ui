/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;

import org.eclipse.ui.IFileEditorInput;

import org.eclipse.ui.editors.text.ILocationProvider;

/**
 * @since 3.0
 */
public class FileEditorInputAdapterFactory implements IAdapterFactory {

	private static class LocationProvider implements ILocationProvider {
		/*
		 * @see org.eclipse.ui.editors.text.ILocationProvider#getLocation(java.lang.Object)
		 */
		@Override
		public IPath getPath(Object element) {
			if (element instanceof IFileEditorInput) {
				IFileEditorInput input= (IFileEditorInput) element;
				return input.getFile().getFullPath();
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
			if (adaptableObject instanceof IFile)
				return (T) fLocationProvider;
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTER_LIST;
	}
}
