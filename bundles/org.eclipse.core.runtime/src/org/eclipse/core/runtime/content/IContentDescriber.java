/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.content;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Content describers know how to retrieve metadata from 
 * contents.
 * <p>
 * Describers for text-based content types should implement 
 * <code>ITextContentDescriber</code> instead.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>

 * @see IContentDescription
 * @since 3.0
 */
public interface IContentDescriber {
	/**
	 * Description result constant, indicating the contents are valid for 
	 * the intended content type.
	 * 
	 * @see #describe
	 */
	public final static int VALID = 0;
	/**
	 * Description result constant, indicating the contents are invalid for 
	 * the intended content type.
	 * 
	 * @see #describe
	 */
	public final static int INVALID = 1;
	/**
	 * Description result constant, indicating that it was not possible 
	 * to determine whether the contents were valid for 
	 * the intended content type.
	 * 
	 * @see #describe
	 */
	public final static int INDETERMINATE = -1;

	/**
	 * Tries to fill a description for the given contents. Returns 
	 * an <code>int</code> indicating whether the given stream of 
	 * bytes represents a valid sample for its corresponding content type.
	 * If no content description is provided, this method should perform 
	 * content type validation.
	 * <p>
	 * The input stream must be kept open, and any IOExceptions while 
	 * reading the stream should flow to the caller.
	 * </p>
	 * 
	 * @param contents the contents to be examined
	 * @param description a description to be filled in, or <code>null</code> if 
	 * only content type validation is to be performed  
	 * @return one of the following:<ul>
	 * <li><code>VALID</code></li>,
	 * <li><code>INVALID</code></li>,
	 * <li><code>INDETERMINATE</code></li>
	 * </ul>
	 * @throws IOException if an I/O error occurs
	 * @see IContentDescription
	 * @see #VALID
	 * @see #INVALID
	 * @see #INDETERMINATE
	 */
	public int describe(InputStream contents, IContentDescription description) throws IOException;

	/**
	 * Returns the properties supported by this describer. 
	 *   
	 * @return the supported properties
	 * @see #describe
	 */
	public QualifiedName[] getSupportedOptions();
}