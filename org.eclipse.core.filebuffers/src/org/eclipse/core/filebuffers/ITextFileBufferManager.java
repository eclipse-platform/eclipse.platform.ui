/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.core.filebuffers;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.IDocument;


/**
 * A text file buffer manager manages text file buffers for files whose contents
 * could be considered text.
 * 
 * @since 3.0
 */
public interface ITextFileBufferManager extends IFileBufferManager {
	
	/**
	 * Returns the text file buffer managed for the given file or <code>null</code> if either
	 * the file is not connected or the file is not a text file.
	 * 
	 * @param file the file
	 * @return the text file buffer managed for that file or <code>null</code>
	 */
	ITextFileBuffer getTextFileBuffer(IFile file);
	
	/**
	 * Returns the default encoding that is used to read the contents of text files
	 * if no other encoding is specified.
	 * 
	 * @return the default text file encoding
	 */
	String getDefaultEncoding();
	
	/**
	 * Creates a new empty document . The document is setup in the same way as it would
	 * be used in a text file buffer for the given file.
	 * 
	 * @param file the file used to setup the newly created document
	 * @return a new empty document
	 */
	IDocument createEmptyDocument(IFile file);
}
