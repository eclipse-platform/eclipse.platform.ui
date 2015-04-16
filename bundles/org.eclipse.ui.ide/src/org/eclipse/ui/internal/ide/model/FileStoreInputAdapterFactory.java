/*******************************************************************************
 * Copyright (C) 2015 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.ide.FileStoreEditorInput;

/**
 * The adapter factory for {@link FileStoreEditorInput}.
 * @since 3.11
 */
public class FileStoreInputAdapterFactory implements IAdapterFactory {
	private static final Class<?>[] ADAPTERS = new Class[] { IFileStore.class };

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof FileStoreEditorInput && adapterType.isAssignableFrom(IFileStore.class)) {
			FileStoreEditorInput editorInput = (FileStoreEditorInput) adaptableObject;
			try {
				return adapterType.cast(EFS.getStore(editorInput.getURI()));
			} catch (CoreException e) {
				// Ignore to return null.
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return ADAPTERS;
	}
}
