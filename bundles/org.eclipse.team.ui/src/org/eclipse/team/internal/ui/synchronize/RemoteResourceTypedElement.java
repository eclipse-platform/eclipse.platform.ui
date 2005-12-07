/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.io.InputStream;

import org.eclipse.compare.*;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;

/**
 * RemoteResourceTypedElement
 */
public class RemoteResourceTypedElement extends BufferedContent implements ITypedElement, IEditableContent, IEncodedStreamContentAccessor {

	private IResourceVariant remote;
	private IStorage bufferedContents;

	/**
	 * Creates a new content buffer for the given team node.
	 */
	public RemoteResourceTypedElement(IResourceVariant remote) {
		Assert.isNotNull(remote);
		this.remote = remote;
	}

	public Image getImage() {
		return CompareUI.getImage(getType());
	}

	public String getName() {
		return remote.getName();
	}

	public String getContentIdentifier() {
		return remote.getContentIdentifier();
	}
	
	public String getType() {
		if (remote.isContainer()) {
			return ITypedElement.FOLDER_TYPE;
		}
		String name = getName();
		if (name != null) {
			int index = name.lastIndexOf('.');
			if (index == -1)
				return ""; //$NON-NLS-1$
			if (index == (name.length() - 1))
				return ""; //$NON-NLS-1$
			return name.substring(index + 1);
		}
		return ITypedElement.FOLDER_TYPE;		
	}

	/**
	 * Returns true if this object can be modified.
	 * If it returns <code>false</code> the other methods must not be called.
	 * 
	 * @return <code>true</code> if this object can be modified.
	 */
	public boolean isEditable() {
		return false;
	}

	/**
	 * This is not the definitive API!
	 * This method is called on a parent to
	 * - add a child,
	 * - remove a child,
	 * - copy the contents of a child
	 * 
	 * What to do is encoded in the two arguments as follows:
	 * add:	child == null		other != null
	 * remove:	child != null		other == null
	 * copy:	child != null		other != null
	 */
	public ITypedElement replace(ITypedElement child, ITypedElement other) {
		return null;
	}

	/* (non-Javadoc)
	 * @see BufferedContent#createStream()
	 */
	protected InputStream createStream() throws CoreException {
		if(bufferedContents == null) {
			cacheContents(new NullProgressMonitor());
		}
		if (bufferedContents != null) {
			return bufferedContents.getContents();
		}
		return null;
	}
	
	public IResourceVariant getRemote() {
		return remote;
	}

	/**
	 * Cache the contents for the remote resource in a local buffer
	 * @param monitor
	 */
	public void cacheContents(IProgressMonitor monitor) throws TeamException {
		bufferedContents = remote.getStorage(monitor);		
	}

	/**
	 * Update the remote handle in this typed element.
	 * @param variant the new remote handle
	 */
	public void update(IResourceVariant variant) {
		Assert.isNotNull(variant);
		discardBuffer();
		remote = variant;
		fireContentChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IEncodedStreamContentAccessor#getCharset()
	 */
	public String getCharset() throws CoreException {
		if(bufferedContents == null) {
			cacheContents(new NullProgressMonitor());
		}
		if (bufferedContents instanceof IEncodedStorage) {
			return ((IEncodedStorage)bufferedContents).getCharset();
		}
		return null;
	}
}
