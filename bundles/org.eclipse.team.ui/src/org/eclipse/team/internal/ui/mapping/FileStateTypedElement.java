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
package org.eclipse.team.internal.ui.mapping;

import java.io.InputStream;

import org.eclipse.compare.*;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.history.IFileState;

/**
 * A typed element for file revisions
 */
public class FileStateTypedElement extends BufferedContent implements ITypedElement, IEditableContent, IEncodedStreamContentAccessor {

	IFileState state;
	private IStorage bufferedContents;
	
	public FileStateTypedElement(IFileState state) {
		this.state = state;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.BufferedContent#createStream()
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

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getName()
	 */
	public String getName() {
		return state.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getImage()
	 */
	public Image getImage() {
		return CompareUI.getImage(getType());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getType()
	 */
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
	 * @see org.eclipse.compare.IEditableContent#isEditable()
	 */
	public boolean isEditable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IEditableContent#replace(org.eclipse.compare.ITypedElement, org.eclipse.compare.ITypedElement)
	 */
	public ITypedElement replace(ITypedElement dest, ITypedElement src) {
		return null;
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
	
	/**
	 * Cache the contents for the remote resource in a local buffer
	 * @param monitor
	 */
	public void cacheContents(IProgressMonitor monitor) throws CoreException {
		bufferedContents = state.getStorage(monitor);		
	}

}
