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

import java.io.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Content types represent and provide information on file types, such as 
 * associated file names/extensions, default charset, etc.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p> 
 * 
 * @since 3.0
 */
public interface IContentType {
	/**
	 * File spec type flag constant, indicating that pre-defined file 
	 * specifications should not be taken into account.
	 */
	public static final int IGNORE_PRE_DEFINED = 0x01;
	/**
	 * File spec type flag constant, indicating that user-defined file 
	 * specifications should not be taken into account.
	 */
	public static final int IGNORE_USER_DEFINED = 0x02;
	/**
	 * File spec type constant, indicating a file name specification.
	 */
	public static final int FILE_NAME_SPEC = 0x04;
	/**
	 * File spec type constant, indicating a file extension specification.
	 */
	public static final int FILE_EXTENSION_SPEC = 0x08;

	/**
	 * Adds a user-defined file specification to this content type. Has no 
	 * effect if the given file specification has already been added by either
	 * user or provider.
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
	 * Removes a user-defined file specification from this content type. Has no 
	 * effect if the given file specification does not exist, or was not defined
	 * by the user.
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
	 * Returns a reference to this content type's base type. If this content type
	 * does not have a base type (it is a root type), returns <code>null</code>.
	 * 
	 * @return this content type's base type, or <code>null</code>
	 */
	public IContentType getBaseType();

	/**
	 * Tries to obtain a description for the given contents. 
	 * <p>
	 * Any IOExceptions that may occur while reading the given input stream 
	 * will flow to the caller.  The input stream will not be closed by this 
	 * operation.
	 * </p>
	 *  
	 * @param contents the contents to be interpreted
	 * @param options an array of keys for all properties that should be described
	 * @return a content description if one could be obtained, or 
	 * <code>null</code>
	 * @throws IOException if an error occurs while reading the contents
	 * @see IContentDescription  
	 */
	public IContentDescription getDescriptionFor(InputStream contents, QualifiedName[] options) throws IOException;

	/**
	 * Tries to obtain a description for the given contents. 
	 * <p>
	 * Any IOExceptions that may occur while reading the given reader 
	 * will flow to the caller.  The reader will not be closed by this 
	 * operation.
	 * </p>
	 *  
	 * @param contents the contents to be interpreted
	 * @param options an array of keys for all properties that should be described
	 * @return a content description if one could be obtained, or 
	 * <code>null</code>
	 * @throws UnsupportedOperationException if this content type
	 * has a describer that does not implement 
	 * <code>ITextContentDescriber</code>
	 * @throws IOException if an error occurs while reading the contents 
	 * @see IContentDescription
	 */
	public IContentDescription getDescriptionFor(Reader contents, QualifiedName[] options) throws IOException;

	/**
	 * Returns the default charset for this content type if one has been defined, 
	 * <code>null</code> otherwise.
	 * 
	 * @return the default charset, or <code>null</code>
	 */
	public String getDefaultCharset();

	/**
	 * Returns file specifications from this content type. The type mask 
	 * is a bit-wise or of file specification type constants indicating the 
	 * file specification types of interest.
	 * 
	 * @param type a bit-wise or of file specification type constants. Valid
	 * flags are:
	 *<ul>
	 *<li>one of <code>FILE_EXTENSION_SPEC</code> or 
	 *<code>FILE_NAME_SPEC</code></li>
	 *<li>and optionally, one of <code>IGNORE_PRE_DEFINED</code>
	 *or <code>IGNORE_USER_DEFINED</code></li>
	 *</ul>
	 * @return the file specification
	 * @see #FILE_NAME_SPEC
	 * @see #FILE_EXTENSION_SPEC
	 * @see #IGNORE_PRE_DEFINED
	 * @see #IGNORE_USER_DEFINED
	 */
	public String[] getFileSpecs(int type);

	/**
	 * Returns this content type's unique identifier. Each content type has an 
	 * identifier by which they can be retrieved from the content type catalog.
	 * 
	 * @return this content type's unique identifier
	 */
	public String getId();

	/**
	 * Returns a user-friendly name for this content type.
	 * 
	 * @return this content type's name  
	 */
	public String getName();

	/**
	 * Returns whether this content type is associated with the 
	 * given file name.
	 * 
	 * @param fileName the file name
	 * @return <code>true</code> if this content type is associated with
	 * the given file name, <code>false</code> otherwise 
	 */
	public boolean isAssociatedWith(String fileName);

	/**
	 * Returns whether this content type is a kind of the given content 
	 * type. A content type A is a kind of a content type B if:
	 * <ol>
	 * <li>A and B are the same content type, or</li> 
	 * <li>A's base type is B, or</li>
	 * <li>A's base type is a kind of B.</li>
	 * </ol>
	 * 
	 * @param another a content type 
	 * @return <code>true</code> if this content type is a kind of the
	 * given content type, <code>false</code> otherwise
	 */
	public boolean isKindOf(IContentType another);

	/**
	 * Sets the default charset for this content type. If 
	 * <code>null</code> is provided, restores the pre-defined default charset. 
	 * 
	 * @param userCharset the new charset for this content type, or
	 * <code>null</code>  
	 * @throws CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> An error occurred persisting this setting.</li>
	 * </ul>
	 */
	public void setDefaultCharset(String userCharset) throws CoreException;
}