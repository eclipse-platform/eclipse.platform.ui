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

package org.eclipse.ui.editors.text;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ISynchronizable;

/**
 * An <code>InputStream</code> that reads from an <code>IDocument</code>.
 * The input stream ensures that its content is the same as the document content
 * when the stream was created.
 * <p>
 * Note that {@link InputStream#close()} must be called to release any acquired
 * resources.
 * </p>
 * 
 * @since 3.1
 */
class DocumentInputStream extends InputStream {
	
	/**
	 * Document based character sequence.
	 */
	private static class DocumentCharSequence implements CharSequence {

		/** Document */
		private IDocument fDocument;
		
		/**
		 * Initialize with the sequence of characters in the given
		 * document.
		 * 
		 * @param document the document
		 */
		public DocumentCharSequence(IDocument document) {
			fDocument= document;
		}
		
		/*
		 * @see java.lang.CharSequence#length()
		 */
		public int length() {
			return fDocument.getLength();
		}

		/*
		 * @see java.lang.CharSequence#charAt(int)
		 */
		public char charAt(int index) {
			try {
				return fDocument.getChar(index);
			} catch (BadLocationException x) {
				throw new IndexOutOfBoundsException(x.getLocalizedMessage());
			}
		}

		/*
		 * @see java.lang.CharSequence#subSequence(int, int)
		 */
		public CharSequence subSequence(int start, int end) {
			try {
				return fDocument.get(start, end - start);
			} catch (BadLocationException x) {
				throw new IndexOutOfBoundsException(x.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * Internal document listener.
	 */
	private class InternalDocumentListener implements IDocumentListener {

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			handleDocumentAboutToBeChanged();
		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
		}
	}

	/** Character sequence */
	private volatile CharSequence fCharSequence;
	
	/** Document length */
	private int fLength;
	
	/** Current offset */
	private int fOffset= 0;
	
	/** Document */
	private IDocument fDocument;
	
	/** Document listener */
	private IDocumentListener fDocumentListener= new InternalDocumentListener();
	
	/**
	 * Initialize the stream to read from the given document. If the
	 * document implements {@link ISynchronizable}, its lock object will be
	 * locked during initialization.
	 * 
	 * @param document the document
	 */
	public DocumentInputStream(IDocument document) {
		Object lock= null;
		if (document instanceof ISynchronizable)
			lock= ((ISynchronizable) document).getLockObject();
		
		if (lock != null)
			synchronized (lock) {
				acquireDocument(document);
			}
		else
			acquireDocument(document);
	}

	/**
	 * Initialize the stream to read from the given document.
	 * 
	 * @param document the document
	 */
	private void acquireDocument(IDocument document) {
		fDocument= document;
		fDocument.addDocumentListener(fDocumentListener);
		fCharSequence= new DocumentCharSequence(fDocument);
		fLength= fCharSequence.length();
	}

	/*
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		try {
			return fOffset < fLength ? fCharSequence.charAt(fOffset++) : -1;
		} catch (IndexOutOfBoundsException x) {
			throw new IOException(TextEditorMessages.getString("DocumentInputStream.error.read") + x.getLocalizedMessage()); //$NON-NLS-1$
		}
	}
	
	/*
	 * @see java.io.InputStream#close()
	 */
	public synchronized void close() throws IOException {
		fCharSequence= null;
		releaseDocument();
	}

	/**
	 * Copies the document prior to modification and removes the document listener.
	 */
	private synchronized void handleDocumentAboutToBeChanged() {
		fCharSequence= fDocument.get();
		releaseDocument();
	}

	/**
	 * Removes the document listener.
	 */
	private void releaseDocument() {
		if (fDocument != null && fDocumentListener != null)
			fDocument.removeDocumentListener(fDocumentListener);
		fDocument= null;
		fDocumentListener= null;
	}
}
