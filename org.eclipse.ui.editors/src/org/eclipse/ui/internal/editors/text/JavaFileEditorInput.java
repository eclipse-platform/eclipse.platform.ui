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
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;

import org.eclipse.core.runtime.IPath;

import org.eclipse.ui.editors.text.ILocationProvider;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ide.FileStoreEditorInput;

/**
 * @since 3.0
 * @deprecated will be deleted soon.
 */
public class JavaFileEditorInput extends FileStoreEditorInput implements ILocationProvider, IPersistableElement {
	
	private IPath fPath;
	
	public JavaFileEditorInput(IFileStore fileStore) {
		super(fileStore);
		fPath= URIUtil.toPath(fileStore.toURI());
	}

	/*
	 * @see org.eclipse.ui.editors.text.ILocationProvider#getPath(java.lang.Object)
	 */
	public IPath getPath(Object element) {
		if (element instanceof JavaFileEditorInput)
			return ((JavaFileEditorInput)element).getPath();
		return null;
	}

    /*
     * @see org.eclipse.ui.IPathEditorInput#getPath()
     * @since 3.1
     */
    public IPath getPath() {
	 return fPath;
    }

    /*
     * @see org.eclipse.ui.ide.FileStoreEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
    	return this;
    }

	/*
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	public String getFactoryId() {
		return JavaFileEditorInputFactory.ID;
	}

	/*
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 * @since 3.3
	 */
	public void saveState(IMemento memento) {
		JavaFileEditorInputFactory.saveState(memento, this);
	}
}
