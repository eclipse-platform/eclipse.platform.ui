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

/**
 * Content describers know how to retrieve basic information on specific file
 * contents.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note</b>: This interface is part of early access API that may well 
 * change in incompatible ways until it reaches its finished form. 
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
	 * to determinate whether the contents were valid for 
	 * the intented content type.
	 * 
	 * @see #describe
	 */	
	public final static int INDETERMINATE = -1;	
	/**
	 * Tries to fill a description for the given contents. Returns 
	 * a boolean indicating whether the given stream of 
	 * bytes represents a valid sample for its corresponding content type.
	 * If no description options are specified, this method only performs 
	 * content type detection. In this case, a description object might not 
	 * be provided.  
	 * <p>
	 * The input stream must be kept open, and any IOExceptions while 
	 * reading the stream should flow to the caller.
	 * </p>
	 * 
	 * @param input the contents to be examined
	 * @param description a description to be filled in, or <code>null</code> if 
	 * no options are provided  
	 * @param optionsMask a bit-wise OR of all options that should be described
	 * @return whether this describer recognized the contents
	 * @throws IOException if an I/O error occurs
	 * @see IContentDescription#ALL
	 * @see IContentDescription#CHARSET
	 * @see IContentDescription#BYTE_ORDER_MARK
	 * @see IContentDescription#CUSTOM_PROPERTIES
	 */
	public int describe(InputStream contents, IContentDescription description, int optionsMask) throws IOException;
	/**
	 * Returns the options supported by this describer as a bit mask. 
	 *   
	 * @return the supported options
	 * @see #describe
	 */
	public int getSupportedOptions();
}