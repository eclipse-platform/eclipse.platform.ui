/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.content;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IScopeContext;

/**
 * Gives access to the user settings for a content type.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see IContentType
 * @see IContentType#getSettings(IScopeContext)
 * @since 3.1
 */
public interface IContentTypeSettings {
	/**
	 * File spec type constant, indicating a file extension specification.
	 */
	public static final int FILE_EXTENSION_SPEC = 0x08;
	/**
	 * File spec type constant, indicating a file name specification.
	 */
	public static final int FILE_NAME_SPEC = 0x04;

	/**
	 * Adds a user-defined file specification to the corresponding content type. Has no 
	 * effect if the given file specification is already defined.
	 * 
	 * @param fileSpec the file specification
	 * @param type the type of the file specification. One of 
	 * <code>FILE_NAME_SPEC</code>, 
	 * <code>FILE_EXTENSION_SPEC</code>.
	 * @throws IllegalArgumentException if the type bit mask is  
	 * incorrect
	 * @throws CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> An error occurred persisting this setting.</li>
	 * </ul>
	 * @see #FILE_NAME_SPEC
	 * @see #FILE_EXTENSION_SPEC	 
	 */
	public void addFileSpec(String fileSpec, int type) throws CoreException;

	/**
	 * Returns the default charset for the corresponding content type if 
	 * it has been set, or  
	 * <code>null</code> otherwise.
	 * 
	 * @return the default charset, or <code>null</code>
	 */
	public String getDefaultCharset();

	/**
	 * Returns the file specifications for the corresponding content type. The type mask 
	 * is a bit-wise or of file specification type constants indicating the 
	 * file specification types of interest.
	 * 
	 * @param type a bit-wise or of file specification type constants. Valid
	 * flags are one of <code>FILE_EXTENSION_SPEC</code> or 
	 *<code>FILE_NAME_SPEC</code>
	 * @return the file specification
	 * @see #FILE_NAME_SPEC
	 * @see #FILE_EXTENSION_SPEC
	 */
	public String[] getFileSpecs(int type);

	/**
	 * Returns the corresponding content type's unique identifier. Each content 
	 * type has an identifier by which they can be retrieved from the content 
	 * type catalog.
	 * 
	 * @return the content type unique identifier
	 */
	public String getId();

	/**
	 * Removes a user-defined file specification from the corresponding content type. Has no 
	 * effect if the given file specification was not defined by the user.
	 * 
	 * @param fileSpec the file specification
	 * @param type the type of the file specification. One of 
	 * <code>FILE_NAME_SPEC</code>, 
	 * <code>FILE_EXTENSION_SPEC</code>.
	 * @throws IllegalArgumentException if the type bit mask is  
	 * incorrect
	 * @throws CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> An error occurred persisting this setting.</li>
	 * </ul>
	 * @see #FILE_NAME_SPEC
	 * @see #FILE_EXTENSION_SPEC
	 */
	public void removeFileSpec(String fileSpec, int type) throws CoreException;

	/**
	 * Sets the default charset for the corresponding content type. If 
	 * <code>null</code> is provided, restores the pre-defined default charset. 
	 * 
	 * @param userCharset the new charset for the content type, or
	 * <code>null</code>  
	 * @throws CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> An error occurred persisting this setting.</li>
	 * </ul>
	 */
	public void setDefaultCharset(String userCharset) throws CoreException;
}
