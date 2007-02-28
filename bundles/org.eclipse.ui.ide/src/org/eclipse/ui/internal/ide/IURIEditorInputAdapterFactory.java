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
package org.eclipse.ui.internal.ide;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;

import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;


/**
 * Adapter factory for <code>IURIEditorInput</code>.
 * 
 * @since 3.3
 */
public class IURIEditorInputAdapterFactory implements IAdapterFactory {

	private static class PathEditorInputAdapter extends FileStoreEditorInput implements IPathEditorInput {

		/**
		 * Creates a new adapter for the given file store.
		 * 
		 * @param fileStore the file store;
		 */
		public PathEditorInputAdapter(IFileStore fileStore) {
			super(fileStore);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPathEditorInput#getPath()
		 */
		public IPath getPath() {
			return URIUtil.toPath(getURI());
		}
	}

	
	/** The list of provided adapters. */
	private static final Class[] ADAPTER_LIST= new Class[] { IPathEditorInput.class };

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IPathEditorInput.class.equals(adapterType)) {
			if (adaptableObject instanceof IURIEditorInput) {
				IFileStore fileStore;
				try {
					fileStore= EFS.getStore(((IURIEditorInput) adaptableObject).getURI());
					if (fileStore.getFileSystem() == EFS.getLocalFileSystem()) {
						return new PathEditorInputAdapter(fileStore);
					}
				} catch (CoreException e) {
					return null;
				}
			}
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return ADAPTER_LIST;
	}
}
