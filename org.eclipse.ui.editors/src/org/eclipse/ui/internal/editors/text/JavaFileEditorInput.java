/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IStorage;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.jface.text.Assert;

import org.eclipse.ui.editors.text.ILocationProvider;

import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @since 3.0
 */
public class JavaFileEditorInput implements IPathEditorInput, IStorageEditorInput, ILocationProvider {

	/**
	 * The workbench adapter which simply provides the label.
	 *
	 * @since 3.1
	 */
	private class WorkbenchAdapter implements IWorkbenchAdapter {
		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object o) {
			return null;
		}

		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
		 */
		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
		 */
		public String getLabel(Object o) {
			return ((JavaFileEditorInput)o).getName();
		}

		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
		 */
		public Object getParent(Object o) {
			return null;
		}
	}

	private IFileStore fFileStore;
	private WorkbenchAdapter fWorkbenchAdapter= new WorkbenchAdapter();

	public JavaFileEditorInput(IFileStore fileStore) {
		Assert.isNotNull(fileStore);
		Assert.isTrue(EFS.SCHEME_FILE.equals(fileStore.getFileSystem().getScheme()));
		fFileStore= fileStore;
		fWorkbenchAdapter= new WorkbenchAdapter();
	}
	/*
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return fFileStore.fetchInfo().exists();
	}

	/*
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/*
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return fFileStore.getName();
	}

	/*
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/*
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return fFileStore.toString();
	}

	/*
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (ILocationProvider.class.equals(adapter))
			return this;
		if (IWorkbenchAdapter.class.equals(adapter))
			return fWorkbenchAdapter;
		return Platform.getAdapterManager().getAdapter(this, adapter);
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
    	return new Path(fFileStore.toURI().getPath());
    }

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (o instanceof JavaFileEditorInput) {
			JavaFileEditorInput input= (JavaFileEditorInput) o;
			return fFileStore.equals(input.fFileStore);
		}

        if (o instanceof IPathEditorInput) {
            IPathEditorInput input= (IPathEditorInput)o;
            return getPath().equals(input.getPath());
        }

		return false;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fFileStore.hashCode();
	}

	/*
	 * @see org.eclipse.ui.IStorageEditorInput#getStorage()
	 * @since 3.2
	 */
	public IStorage getStorage() throws CoreException {
		return new JavaFileStorage(fFileStore);
	}

}
