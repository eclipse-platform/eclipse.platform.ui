/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare;

import org.eclipse.core.runtime.CoreException;

/**
 * Extension for <code>IStreamContentAccessor</code>. Extends the original
 * concept of a <code>IStreamContentAccessor</code> to answer the Charset (encoding) used for the stream.
 * <p>
 * <b>Note</b>: This interface is part of early access API that may well 
 * change in incompatible ways until reach their finished form. 
 * </p>
 * @since 3.0
 */
public interface IEncodedStreamContentAccessor extends IStreamContentAccessor {
	
	/**
	 * Returns the name of a charset encoding to be used when decoding this 
	 * stream accessor's contents into characters. Returns <code>null</code> if a proper 
	 * encoding cannot be determined.
	 *
	 * @return the name of a charset, or <code>null</code>
	 * @exception CoreException if an error happens while determining 
	 * the charset. See any refinements for more information.
	 * @see IStreamContentAccessor#getContents
	 * @since 3.0
	 */
	String getCharset() throws CoreException;
}
