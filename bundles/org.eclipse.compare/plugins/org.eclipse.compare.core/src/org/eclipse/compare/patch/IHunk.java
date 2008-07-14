/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.patch;

import java.io.InputStream;

import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Interface that represents a hunk. A hunk is a portion of a patch. It
 * identifies where the hunk is to be located in the target file. One use of
 * this interface is a means to communicate to content merge viewers that one of
 * the sides of a compare input is a patch hunk. Clients can determine which
 * side it is by adapting the side to this interface (see {@link IAdaptable}.
 * <p>
 * This interface is not intended to be implemented by clients but can be
 * obtained from an {@link IFilePatchResult}
 * </p>
 * 
 * @since 3.3
 * 
 */
public interface IHunk {

	/**
	 * Return a label that can be used to describe the hunk.
	 * @return a label that can be used to describe the hunk
	 */
	public String getLabel();
	
	/**
	 * Return the start position of the hunk in the target file.
	 * 
	 * @return the start position of the hunk in the target file.
	 */
	public int getStartPosition();
	
	/**
	 * Return the original contents from which the hunk was generated.
	 * The returned contents usually only represent a portion of the
	 * file from which the hunk was generated.
	 * @return the original contents from which the hunk was generated
	 */
	public InputStream getOriginalContents();
	
	/**
	 * Return the contents that contain the modifications for this hunk.
	 * The returned contents usually only represent a portion of the
	 * file that was modified.
	 * @return the contents that contain the modifications for this hunk
	 */
	public InputStream getPatchedContents();
	
	/**
	 * Returns the name of a charset encoding to be used when decoding the contents 
	 * of this hunk into characters. Returns <code>null</code> if a proper 
	 * encoding cannot be determined.
	 * <p>
	 * Note that this method does not check whether the result is a supported
	 * charset name. Callers should be prepared to handle 
	 * <code>UnsupportedEncodingException</code> where this charset is used. 
	 * </p>
	 *
	 * @return the name of a charset, or <code>null</code>
	 * @exception CoreException if an error happens while determining 
	 * the charset. See any refinements for more information.
	 * @see IEncodedStorage
	 */
	public String getCharset() throws CoreException;
	
	
}
