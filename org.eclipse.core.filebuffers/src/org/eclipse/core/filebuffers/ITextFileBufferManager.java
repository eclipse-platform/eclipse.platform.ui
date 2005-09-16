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
package org.eclipse.core.filebuffers;


import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;


/**
 * A text file buffer manager manages text file buffers for files whose contents
 * is considered text.
 * <p>
 * Clients are not supposed to implement that interface.
 * </p>
 *
 * @since 3.0
 */
public interface ITextFileBufferManager extends IFileBufferManager {

	/**
	 * Returns the text file buffer managed for the file at the given location
	 * or <code>null</code> if either there is no such text file buffer.
	 * <p>
	 * The provided location is either a full path of a workspace resource or
	 * an absolute path in the local file system. The file buffer manager does
	 * not resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 *
	 * @param location the location
	 * @return the text file buffer managed for that location or <code>null</code>
	 */
	ITextFileBuffer getTextFileBuffer(IPath location);

	/**
	 * Returns the default encoding that is used to read the contents of text files
	 * if no other encoding is specified.
	 *
	 * @return the default text file encoding
	 */
	String getDefaultEncoding();

	/**
	 * Creates a new empty document. The document is set up in the same way as
	 * it would be used in a text file buffer for a file at the given location.
	 * <p>
	 * The provided location is either a full path of a workspace resource or
	 * an absolute path in the local file system. The file buffer manager does
	 * not resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 *
	 * @param location the location used to set up the newly created document
	 * @return a new empty document
	 */
	IDocument createEmptyDocument(IPath location);

	/**
	 * Creates a new annotation for the given location.
	 * <p>
	 * The provided location is either a full path of a workspace resource or an
	 * absolute path in the local file system. The file buffer manager does not
	 * resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 *
	 * @param location the location used to create the new annotation model
	 * @return the newly created annotation model
	 */
	IAnnotationModel createAnnotationModel(IPath location);

	/**
	 * Returns whether a file at the given location is or can be considered a
	 * text file. If the file exists, the concrete content type of the file is
	 * checked. If the concrete content type for the existing file can not be
	 * determined, this method returns <code>true</code>. If the file does
	 * not exist, it is checked whether a text content type is associated with
	 * the given location. If no content type is associated with the location,
	 * this method returns <code>true</code>.
	 * <p>
	 * The provided location is either a full path of a workspace resource or an
	 * absolute path in the local file system. The file buffer manager does not
	 * resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 *
	 * @param location the location to check
	 * @return <code>true</code> if the location is a text file location
	 * @since 3.1
	 * @deprecated As of 3.2, replaced by {@link #isTextFileLocation(IPath, boolean)}
	 */
	boolean isTextFileLocation(IPath location);
	/**
	 * Returns whether a file at the given location is or can be considered a
	 * text file. If the file exists, the concrete content type of the file is
	 * checked. If the concrete content type for the existing file can not be
	 * determined, this method returns <code>!strict</code>. If the file does
	 * not exist, it is checked whether a text content type is associated with
	 * the given location. If no content type is associated with the location,
	 * this method returns <code>!strict</code>.
	 * <p>
	 * The provided location is either a full path of a workspace resource or an
	 * absolute path in the local file system. The file buffer manager does not
	 * resolve the location of workspace resources in the case of linked
	 * resources.
	 * </p>
	 *
	 * @param location	the location to check
	 * @param strict	<code>true</code> if a file with unknown content type
	 * 					is not treated as text file, <code>false</code> otherwise
	 * @return <code>true</code> if the location is a text file location
	 * @since 3.2
	 */
	boolean isTextFileLocation(IPath location, boolean strict);
}
