/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.filebuffers;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * An <code>InputStream</code> that reads from an <code>IDocument</code>.
 * If no references to this stream are retained it does not have to be closed in
 * order to release backing resources.
 * 
 * @since 3.1
 */
public class DocumentInputStream extends InputStream {
	
	/** the document */
	private IDocument fDocument;
	
	/** the document length */
	private int fLength;
	
	/** the current offset */
	private int fOffset= 0;
	
	/**
	 * Initializes the stream to read from the given document.
	 * 
	 * @param document the document
	 */
	public DocumentInputStream(IDocument document) {
		fDocument= document;
		fLength= fDocument.getLength();
	}
	
	/*
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		try {
			return fOffset < fLength ? fDocument.getChar(fOffset++) : -1;
		} catch (BadLocationException x) {
			throw new IOException(FileBuffersMessages.getString("DocumentInputStream.error.read")); //$NON-NLS-1$
		}
	}
	
	/*
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		fDocument= null;
	}
}
