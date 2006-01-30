/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

import java.io.InputStream;

import org.eclipse.compare.*;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;

public abstract class StorageTypedElement extends BufferedContent implements ITypedElement, IEditableContent, IEncodedStreamContentAccessor {

	private IStorage bufferedContents;

	/* (non-Javadoc)
	 * @see BufferedContent#createStream()
	 */
	protected InputStream createStream() throws CoreException {
		if (bufferedContents == null) {
			cacheContents(new NullProgressMonitor());
		}
		if (bufferedContents != null) {
			return bufferedContents.getContents();
		}
		return null;
	}

	/**
	 * Cache the contents for the remote resource in a local buffer
	 * @param monitor
	 */
	public void cacheContents(IProgressMonitor monitor) throws CoreException {
		bufferedContents = getElementStorage(monitor);
	}

	/**
	 * Returns an IStorage for the element.
	 * @param monitor
	 * @return
	 * @throws TeamException
	 */
	abstract protected IStorage getElementStorage(IProgressMonitor monitor) throws CoreException;

	public Image getImage() {
		return CompareUI.getImage(getType());
	}

	public String getType() {
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

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IEncodedStreamContentAccessor#getCharset()
	 */
	public String getCharset() throws CoreException {
		if (bufferedContents == null) {
			cacheContents(new NullProgressMonitor());
		}
		if (bufferedContents instanceof IEncodedStorage) {
			return ((IEncodedStorage) bufferedContents).getCharset();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getName()
	 */
	abstract public String getName();

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IEditableContent#isEditable()
	 */
	abstract public boolean isEditable();

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IEditableContent#replace(org.eclipse.compare.ITypedElement, org.eclipse.compare.ITypedElement)
	 */
	abstract public ITypedElement replace(ITypedElement dest, ITypedElement src);
	
	/**
	 * Returns the unique content identifier for this element
	 * @return String	the string contains a unique content id
	 */
	abstract public String getContentIdentifier();
}
