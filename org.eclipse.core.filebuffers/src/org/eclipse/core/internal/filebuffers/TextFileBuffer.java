/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;

import org.eclipse.jface.text.IDocument;

/**
 * 
 */
public class TextFileBuffer extends FileBuffer implements ITextFileBuffer {

	public TextFileBuffer(IFile file, FileDocumentProvider2 documentProvider) {
		super(file, documentProvider);
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedTextFile#getDocument()
	 */
	public IDocument getDocument() {
		return getDocumentProvider().getDocument(getUnderlyingFile());
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedTextFile#getEncoding()
	 */
	public String getEncoding() {
		return getDocumentProvider().getEncoding(getUnderlyingFile());
	}

	/*
	 * @see org.eclipse.core.buffer.text.IBufferedTextFile#setEncoding(java.lang.String)
	 */
	public void setEncoding(String encoding) {
		getDocumentProvider().setEncoding(getUnderlyingFile(), encoding);
	}
}
