/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.filesystem.provider;

import java.net.URI;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 * The common superclass for all file system implementations.  Instances
 * of this class are provided using the <tt>org.eclipse.core.filesystem.filesystems</tt>
 * extension point.
 * <p>
 * On creation, the <code>setInitializationData</code> method is called with
 * any parameter data specified in the declaring plug-in's manifest.
 * </p>
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 1.0
 */
public abstract class FileSystem extends PlatformObject implements IFileSystem {
	private String scheme;

	/**
	 * Creates a new file system instance.
	 */
	public FileSystem() {
		super();
	}

	/**
	 * This is the default implementation of {@link IFileSystem#attributes()}.  
	 * This implementation always returns <code>0</code>.
	 * Subclasses may override this method.
	 * 
	 * @return The attributes supported by this file system
	 * @see IFileSystem#attributes()
	 */
	public int attributes() {
		return 0;
	}

	/**
	 * This is the default implementation of {@link IFileSystem#canDelete()}.  
	 * This implementation always returns <code>false</code>.
	 * Subclasses may override this method.
	 * 
	 * @return <code>true</code> if this file system supports deletion, and
	 * <code>false</code> otherwise.
	 * @see IFileSystem#canDelete()
	 */
	public boolean canDelete() {
		return false;
	}

	/**
	 * This is the default implementation of {@link IFileSystem#canWrite()}.  
	 * This implementation always returns <code>false</code>.
	 * Subclasses may override this method.
	 * 
	 * @return <code>true</code> if this file system allows modification, and
	 * <code>false</code> otherwise.
	 * @see IFileSystem#canWrite()
	 */
	public boolean canWrite() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileSystem#getScheme()
	 */
	public final String getScheme() {
		return scheme;
	}

	/**
	 * This is the default implementation of {@link IFileSystem#getStore(IPath)}.  
	 * This implementation forwards to {@link IFileSystem#getStore(URI)}, 
	 * assuming that the provided path corresponds to the path component of the 
	 * URI for the file store.
	 * <p>
	 * Subclasses may override this method.
	 * </p>
	 * 
	 * @param path A path to a file store within the scheme of this file system.
	 * @return A handle to a file store in this file system
	 * @see IFileSystem#getStore(IPath)
	 */
	public IFileStore getStore(IPath path) {
		return getStore(URI.create(scheme + ':' + path.toString()));
	}

	/* (non-Javadoc)
	 * @see IFileSystem#getStore()
	 */
	public abstract IFileStore getStore(URI uri);

	/**
	 * Initializes this file system instance with the provided scheme.
	 * <p>
	 * This method is called by the platform immediately after the
	 * file system instance is created.  This method must not be
	 * called by clients.
	 * 
	 * @param aScheme The scheme of the file system.
	 */
	public final void initialize(String aScheme) {
		if (aScheme == null)
			throw new NullPointerException();
		//scheme cannot be changed after creation
		if (this.scheme != null)
			throw new IllegalStateException("File system already initialized"); //$NON-NLS-1$
		this.scheme = aScheme;
	}

	/**
	 * This is the default implementation of {@link IFileSystem#isCaseSensitive()}.  
	 * This implementation always returns <code>true</code>. Subclasses may override this method.
	 * 
	 * @return <code>true</code> if this file system is case sensitive, and
	 * <code>false</code> otherwise.
	 * @see IFileSystem#isCaseSensitive()
	 */
	public boolean isCaseSensitive() {
		return true;
	}
}