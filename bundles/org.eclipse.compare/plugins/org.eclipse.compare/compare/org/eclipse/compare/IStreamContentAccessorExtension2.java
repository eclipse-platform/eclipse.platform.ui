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
 * Extension interface for <code>IStreamContentAccessor</code>. Extends the original
 * concept of a <code>IStreamContentAccessor</code> to answer the Charset (encoding) used for the stream.
 * 
 * @deprecated Use <code>IEncodedStreamContentAccessor</code> instead
 */
public interface IStreamContentAccessorExtension2 extends IStreamContentAccessor {
	/**
	 * @return The character encoding of the stream returned by <code>getContents()</code>.
	 * @exception CoreException if the contents of this object could not be accessed
	 * @since 3.0
	 */
	String getCharset() throws CoreException;
}
