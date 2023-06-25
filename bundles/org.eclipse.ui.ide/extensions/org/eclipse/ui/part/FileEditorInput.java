/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 461762
 *******************************************************************************/
package org.eclipse.ui.part;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Adapter for making a file resource a suitable input for an editor.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FileEditorInput extends PlatformObject implements IFileEditorInput, IPathEditorInput, IURIEditorInput,
		IPersistableElement {
	private IFile file;

	/**
	 * Return whether or not file is local. Only {@link IFile}s with a local value
	 * should call {@link IPathEditorInput#getPath()}
	 *
	 * @param file the file to check; not <code>null</code>
	 * @return boolean <code>true</code> if the file has a local implementation.
	 * @since 3.4
	 */
	public static boolean isLocalFile(IFile file){

		IPath location = file.getLocation();
		if (location != null)
			return true;
		//this is not a local file, so try to obtain a local file
		try {
			final URI locationURI = file.getLocationURI();
			if (locationURI == null)
				return false;
			IFileStore store = EFS.getStore(locationURI);
			//first try to obtain a local file directly fo1r this store
			java.io.File localFile = store.toLocalFile(EFS.NONE, null);
			//if no local file is available, obtain a cached file
			if (localFile == null)
				localFile = store.toLocalFile(EFS.CACHE, null);
			if (localFile == null)
				return false;
			return true;
		} catch (CoreException e) {
			//this can only happen if the file system is not available for this scheme
			IDEWorkbenchPlugin.log(
					"Failed to obtain file store for resource", e); //$NON-NLS-1$
			return false;
		}

	}

	/**
	 * Creates an editor input based of the given file resource.
	 *
	 * @param file the file resource
	 */
	public FileEditorInput(IFile file) {
		if (file == null)
			throw new IllegalArgumentException();
		this.file = file;

	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

	/*
	 * The <code>FileEditorInput</code> implementation of this
	 * <code>Object</code> method bases the equality of two
	 * <code>FileEditorInput</code> objects on the equality of their underlying
	 * <code>IFile</code> resources.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IFileEditorInput)) {
			return false;
		}
		IFileEditorInput other = (IFileEditorInput) obj;
		return file.equals(other.getFile());
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public String getFactoryId() {
		return FileEditorInputFactory.getFactoryId();
	}

	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		IContentType contentType = IDE.getContentType(file);
		return PlatformUI.getWorkbench().getEditorRegistry()
				.getImageDescriptor(file.getName(), contentType);
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return this;
	}

	@Override
	public IStorage getStorage() {
		return file;
	}

	@Override
	public String getToolTipText() {
		return file.getFullPath().makeRelative().toString();
	}

	@Override
	public void saveState(IMemento memento) {
		FileEditorInputFactory.saveState(memento, this);
	}



	@Override
	public URI getURI() {
		return file.getLocationURI();
	}


	@Override
	public IPath getPath() {
		IPath location = file.getLocation();
		if (location != null)
			return location;
		//this is not a local file, so try to obtain a local file
		try {
			final URI locationURI = file.getLocationURI();
			if (locationURI == null)
				throw new IllegalArgumentException();
			IFileStore store = EFS.getStore(locationURI);
			//first try to obtain a local file directly fo1r this store
			java.io.File localFile = store.toLocalFile(EFS.NONE, null);
			//if no local file is available, obtain a cached file
			if (localFile == null)
				localFile = store.toLocalFile(EFS.CACHE, null);
			if (localFile == null)
				throw new IllegalArgumentException();
			return IPath.fromOSString(localFile.getAbsolutePath());
		} catch (CoreException e) {
			//this can only happen if the file system is not available for this scheme
			IDEWorkbenchPlugin.log(
					"Failed to obtain file store for resource", e); //$NON-NLS-1$
			throw new RuntimeException(e);
		}
	}


	@Override
	public String toString() {
		return getClass().getName() + "(" + getFile().getFullPath() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * Allows for the return of an {@link IWorkbenchAdapter} adapter.
	 *
	 * @since 3.5
	 *
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	public <T> T getAdapter(Class<T> adapterType) {
		if (IWorkbenchAdapter.class.equals(adapterType)) {
			return adapterType.cast(new IWorkbenchAdapter() {

				@Override
				public Object[] getChildren(Object o) {
					return new Object[0];
				}

				@Override
				public ImageDescriptor getImageDescriptor(Object object) {
					return FileEditorInput.this.getImageDescriptor();
				}

				@Override
				public String getLabel(Object o) {
					return FileEditorInput.this.getName();
				}

				@Override
				public Object getParent(Object o) {
					return FileEditorInput.this.getFile().getParent();
				}
			});
		}

		return super.getAdapter(adapterType);
	}
}
