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
import org.eclipse.core.runtime.QualifiedName;

/**
 * The content type manager provides facilities file name and content-based
 * type lookup, and content description.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see org.eclipse.core.runtime.Platform#getContentTypeManager()
 * @since 3.0
 */
public interface IContentTypeManager {
	/**
	 * Content type identifier constant for platform's primary 
	 * text-based content type: <code>org.eclipse.core.runtime.text</code>. 
	 * <p>
	 * All text-based content types ought to be sub types of the content type 
	 * identified by this string. This provides a simple way for detecting 
	 * whether a content type is text-based:
	 * <pre>
	 * IContentType text = Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);
	 * IContentType someType = ...;
	 * boolean isTextBased = someType.isKindOf(text);
	 * </pre> 
	 * </p>
	 */
	public final static String CT_TEXT = "org.eclipse.core.runtime.text"; //$NON-NLS-1$	

	/**
	 * Returns the preferred content type for the given contents and file name.
	 * <p>
	 * Returns <code>null</code> if no associated content types are 
	 * found.
	 * </p>
	 * <p>
	 * If a file name is not provided, the entire content type registry will be 
	 * queried. For performance reasons, it is highly recomended 
	 * to provide a file name if available.
	 * </p> 
	 * <p>
	 * Any IOExceptions that may occur while reading the given input stream 
	 * will flow to the caller. The input stream will not be closed by this 
	 * operation.
	 * </p> 
	 * 
	 * @param contents an input stream
	 * @param fileName the file name associated to the contents, or <code>null</code> 
	 * @return the preferred content type associated to the given file name, or <code>null</code>
	 * @throws IOException if an error occurs while reading the contents 
	 */
	public IContentType findContentTypeFor(InputStream contents, String fileName) throws IOException;

	/**
	 * Returns the preferred content type for the given file name. If multiple content types 
	 * are associated with the given file name, the one considered the most appropriated will
	 * be returned. If there are no content types associated, <code>null</code> is returned.
	 * 
	 * @param fileName the name of the file
	 * @return the preferred content type associated to the given file name, or <code>null</code>
	 */
	public IContentType findContentTypeFor(String fileName);

	/**
	 * Returns the content types associated to the given contents and file name.
	 * <p>
	 * Returns an empty array if no associated content types are found.
	 * </p>
	 * <p>
	 * If a file name is not provided, the entire content type registry will be 
	 * queried. For performance reasons, it is highly recomended 
	 * to provide a file name if available.
	 * </p>
	 * <p>
	 * Any IOExceptions that may occur while reading the given input stream 
	 * will flow to the caller.  The input stream will not be closed by this 
	 * operation.
	 * </p> 
	 * 
	 * @param contents an input stream
	 * @param fileName the file name associated to the contents, or <code>null</code> 
	 * @return all content types associated to the given contents and file name
	 * @throws IOException if an error occurs while reading the contents
	 */
	public IContentType[] findContentTypesFor(InputStream contents, String fileName) throws IOException;

	/**
	 * Returns all content types known by the platform that are associated to the given file name.
	 * <p> 
	 * Returns an empty array if there are no content types associated.
	 * </p>
	 * 
	 * @param fileName the name of the file
	 * @return all content types associated to the given file spec
	 */
	public IContentType[] findContentTypesFor(String fileName);

	/**
	 * Returns all content types known by the platform. 
	 * <p>
	 * Returns an empty array if there are no content types available.
	 * </p>
	 * 
	 * @return all content types known by the platform.
	 */
	public IContentType[] getAllContentTypes();

	/**
	 * Returns the content type with the given identifier, or <code>null</code>
	 * if no such content type is known by the platform.
	 * 
	 * @param contentTypeIdentifier the identifier for the content type
	 * @return the content type, or <code>null</code>
	 */
	public IContentType getContentType(String contentTypeIdentifier);

	/**
	 * Tries to obtain a description for the given contents and file name. 
	 * <p>
	 * Any IOExceptions that may occur while reading the given input stream 
	 * will flow to the caller.  The input stream will not be closed by this 
	 * operation.
	 * </p>
	 * <p>
	 * If a file name is not provided, the entire content type registry will be 
	 * queried. For performance reasons, it is highly recomended 
	 * to provide a file name if available.
	 * </p> 
	 *  
	 * @param contents the contents to be interpreted
	 * @param fileName the file name associated to the contents, or <code>null</code>
	 * @param options an array of keys for all properties that should be 
	 * described, or <code>IContentDescription.ALL</code>,  for all of them 
	 * @return a content description if one could be obtained, or <code>null</code>
	 * @throws IOException if an error occurs while reading the contents
	 * @see IContentDescription 
	 */
	public IContentDescription getDescriptionFor(InputStream contents, String fileName, QualifiedName[] options) throws IOException;

	/**
	 * Tries to obtain a description for the given contents and file name. 
	 * <p>
	 * Any IOExceptions that may occur while reading the given input stream 
	 * will flow to the caller.  The reader will not be closed by this 
	 * operation.
	 * </p>
	 * <p>
	 * If a file name is not provided, the entire content type registry will be 
	 * queried. For performance reasons, it is highly recomended 
	 * to provide a file name if available.
	 * </p> 
	 *  
	 * @param contents the contents to be interpreted
	 * @param fileName the file name associated to the contents, or <code>null</code>
	 * @param options an array of keys for all properties that should be 
	 * described, or <code>IContentDescription.ALL</code>,  for all of them 
	 * @return a content description if one could be obtained, or <code>null</code>
	 * @throws IOException if an error occurs while reading the contents
	 * @see IContentDescription 
	 */
	public IContentDescription getDescriptionFor(Reader contents, String fileName, QualifiedName[] options) throws IOException;
}