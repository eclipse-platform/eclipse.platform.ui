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


import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * A text file buffer is a file buffer for text files. The contents of a text
 * file buffer is given in the form of a document and an associated annotation
 * model. Also, the text file buffer provides methods to manage the character
 * encoding used to read and write the buffer's underlying text file.
 * 
 * @since 3.0
 */
public interface ITextFileBuffer extends IFileBuffer {
	
	/**
	 * Returns the document of this text file buffer.
	 * 
	 * @return the document of this text file buffer
	 */
	IDocument getDocument();
	
	/**
	 * Returns the character encoding to be used for reading and writing the
	 * buffer's underlying file.
	 * 
	 * @return the character encoding
	 */
	String getEncoding();
	
	/**
	 * Sets the character encoding to be used for reading and writing the buffer's
	 * underlying file.
	 * 
	 * @param encoding the encoding
	 */
	void setEncoding(String encoding);
	
	/**
	 * Returns the annotation model of this text file buffer.
	 * 
	 * @return the annotation model of this text file buffer
	 */
	IAnnotationModel getAnnotationModel();
}
