/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;

import org.eclipse.core.filebuffers.IFileBuffer;

/**
 * @since 3.0
 */
public abstract class AbstractFileBuffer implements IFileBuffer {
	
	
	public abstract void create(IPath location, IProgressMonitor monitor) throws CoreException;
	
	public abstract void connect();
	
	public abstract void disconnect() throws CoreException;
	
	public abstract boolean isDisposed();

	public abstract void requestSynchronizationContext();
	
	public abstract void releaseSynchronizationContext();

	
	/**
	 * Helper method which computes the encoding out of the given description.
	 * <p>
	 * XXX:
	 * This method should be provided by Platform Core
	 * see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=64342
	 * </p>
	 * 
	 * @param description the content description
	 * @return the encoding
	 * @see org.eclipse.core.resources.IFile#getCharset()
	 */
	static String getCharset(IContentDescription description) {
		if (description == null)
			return null;
		byte[] bom= (byte[]) description.getProperty(IContentDescription.BYTE_ORDER_MARK);
		if (bom != null)
			if (bom == IContentDescription.BOM_UTF_8)
				return "UTF-8"; //$NON-NLS-1$
			else if (bom == IContentDescription.BOM_UTF_16BE || bom == IContentDescription.BOM_UTF_16LE)
				// UTF-16 will properly detect the BOM
				return "UTF-16"; //$NON-NLS-1$
			else {
				// unknown BOM... ignore it				
			}
		return (String)description.getProperty(IContentDescription.CHARSET);
	}
}
