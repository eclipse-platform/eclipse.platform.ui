/**********************************************************************
Copyright (c) 2000, 2004 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.filebuffers;


import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;


/**
 * A text file buffer manager manages text file buffers for files whose contents
 * is considered text.
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
	 * Creates a new empty document . The document is set up in the same way as
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
	 * 
	 * @param location the location
	 * @return the newly created annotation model
	 */
	IAnnotationModel createAnnotationModel(IPath location);
}
