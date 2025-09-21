/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;


/**
 * A <code>Reader</code> that reads from an <code>IDocument</code>.
 * The reader ensures that its content is the same as the document
 * content when the stream was created.
 * <p>
 * Note that {@link #close()} must be called to release any acquired
 * resources.
 * </p>
 *
 * @since 3.1
 */
class DocumentReader extends Reader {

	/**
	 * Document based character sequence.
	 */
	private static class DocumentCharSequence implements CharSequence {

		/** Document */
		private final IDocument fDocument;

		/**
		 * Initialize with the sequence of characters in the given
		 * document.
		 *
		 * @param document the document
		 */
		public DocumentCharSequence(IDocument document) {
			fDocument= document;
		}

		@Override
		public int length() {
			return fDocument.getLength();
		}

		@Override
		public char charAt(int index) {
			try {
				return fDocument.getChar(index);
			} catch (BadLocationException x) {
				throw new IndexOutOfBoundsException(x.getLocalizedMessage());
			}
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			try {
				return fDocument.get(start, end - start);
			} catch (BadLocationException x) {
				throw new IndexOutOfBoundsException(x.getLocalizedMessage());
			}
		}

		/** @see CharSequence#toString **/
		@Override
		public String toString() {
			return fDocument.get();
		}
	}

	/**
	 * Internal document listener.
	 */
	private class InternalDocumentListener implements IDocumentListener {

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			handleDocumentAboutToBeChanged();
		}

		@Override
		public void documentChanged(DocumentEvent event) {
		}
	}

	/** The character sequence. */
	private volatile CharSequence fCharSequence;

	/** Document length. */
	private final int fLength;

	/** The current offset. */
	private int fOffset= 0;

	/** The document. */
	private IDocument fDocument;

	/** The document listener. */
	private IDocumentListener fDocumentListener= new InternalDocumentListener();

	/**
	 * Creates a new document input stream and initializes
	 * the stream to read from the given document.
	 *
	 * @param document the document
	 */
	public DocumentReader(IDocument document) {
		Assert.isNotNull(document);
		fDocument= document;
		fCharSequence= new DocumentCharSequence(fDocument);
		fDocument.addDocumentListener(fDocumentListener);
		fLength= fCharSequence.length();
	}

	@Override
	public void close() throws IOException {
		synchronized (this) {
			fCharSequence= null;
		}
		releaseDocument();
	}

	/**
	 * Copies the document prior to modification and removes the document listener.
	 */
	private void handleDocumentAboutToBeChanged() {
		IDocument document= fDocument;
		if (fCharSequence == null || document == null) {
			return;
		}
		String content= document.get();
		synchronized (this) {
			if (fCharSequence == null) {
				return;
			}
			fCharSequence= content;
		}
		releaseDocument();
	}

	/**
	 * Removes the document listener.
	 */
	private synchronized void releaseDocument() {
		if (fDocument != null) {
			fDocument.removeDocumentListener(fDocumentListener);
		}
		fDocument= null;
		fDocumentListener= null;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int i= 0;
		try {
			for (; i < len && fOffset < fLength; i++) {
				cbuf[off + i]=  fCharSequence.charAt(fOffset++);
			}
			if (i > 0) {
				return i;
			}

			return -1;
		} catch (NullPointerException x) {
			throw new IOException(TextEditorMessages.DocumentInputStream_error_streamClosed);
		} catch (IndexOutOfBoundsException x) {
			return i-1;
		}
	}
}
