/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;

import org.eclipse.core.runtime.Assert;


/**
 * Default implementation of {@link org.eclipse.jface.text.IDocumentAdapter}.
 * <p>
 * <strong>Note:</strong> This adapter does not work if the widget auto-wraps the text.
 * </p>
 */
class DefaultDocumentAdapter implements IDocumentAdapter, IDocumentListener, IDocumentAdapterExtension {

	/** The adapted document. */
	private IDocument fDocument;
	/** The document clone for the non-forwarding case. */
	private IDocument fDocumentClone;
	/** The original content */
	private String fOriginalContent;
	/** The original line delimiters */
	private String[] fOriginalLineDelimiters;
	/** The registered text change listeners */
	private List fTextChangeListeners= new ArrayList(1);
	/**
	 * The remembered document event
	 * @since 2.0
	 */
	private DocumentEvent fEvent;
	/** The line delimiter */
	private String fLineDelimiter= null;
	/**
	 * Indicates whether this adapter is forwarding document changes
	 * @since 2.0
	 */
	private boolean fIsForwarding= true;
	/**
	 * Length of document at receipt of <code>documentAboutToBeChanged</code>
	 * @since 2.1
	 */
	private int fRememberedLengthOfDocument;
	/**
	 * Length of first document line at receipt of <code>documentAboutToBeChanged</code>
	 * @since 2.1
	 */
	private int fRememberedLengthOfFirstLine;
	/**
	 * The data of the event at receipt of <code>documentAboutToBeChanged</code>
	 * @since 2.1
	 */
	private  DocumentEvent fOriginalEvent= new DocumentEvent();


	/**
	 * Creates a new document adapter which is initially not connected to
	 * any document.
	 */
	public DefaultDocumentAdapter() {
	}

	/**
	 * Sets the given document as the document to be adapted.
	 *
	 * @param document the document to be adapted or <code>null</code> if there is no document
	 */
	public void setDocument(IDocument document) {

		if (fDocument != null)
			fDocument.removePrenotifiedDocumentListener(this);

		fDocument= document;
		fLineDelimiter= null;

		if (!fIsForwarding) {
			fDocumentClone= null;
			if (fDocument != null) {
				fOriginalContent= fDocument.get();
				fOriginalLineDelimiters= fDocument.getLegalLineDelimiters();
			} else {
				fOriginalContent= null;
				fOriginalLineDelimiters= null;
			}
		}

		if (fDocument != null)
			fDocument.addPrenotifiedDocumentListener(this);
	}

	/*
	 * @see StyledTextContent#addTextChangeListener(TextChangeListener)
	 */
	public void addTextChangeListener(TextChangeListener listener) {
		Assert.isNotNull(listener);
		if (!fTextChangeListeners.contains(listener))
			fTextChangeListeners.add(listener);
	}

	/*
	 * @see StyledTextContent#removeTextChangeListener(TextChangeListener)
	 */
	public void removeTextChangeListener(TextChangeListener listener) {
		Assert.isNotNull(listener);
		fTextChangeListeners.remove(listener);
	}

	/**
	 * Tries to repair the line information.
	 *
	 * @param document the document
	 * @see IRepairableDocument#repairLineInformation()
	 * @since 3.0
	 */
	private void repairLineInformation(IDocument document) {
		if (document instanceof IRepairableDocument) {
			IRepairableDocument repairable= (IRepairableDocument) document;
			repairable.repairLineInformation();
		}
	}

	/**
	 * Returns the line for the given line number.
	 *
	 * @param document the document
	 * @param line the line number
	 * @return the content of the line of the given number in the given document
	 * @throws BadLocationException if the line number is invalid for the adapted document
	 * @since 3.0
	 */
	private String doGetLine(IDocument document, int line) throws BadLocationException {
		IRegion r= document.getLineInformation(line);
		return document.get(r.getOffset(), r.getLength());
	}

	private IDocument getDocumentForRead() {
		if (!fIsForwarding) {
			if (fDocumentClone == null) {
				String content= fOriginalContent == null ? "" : fOriginalContent; //$NON-NLS-1$
				String[] delims= fOriginalLineDelimiters == null ? DefaultLineTracker.DELIMITERS : fOriginalLineDelimiters;
				fDocumentClone= new DocumentClone(content, delims);
			}
			return fDocumentClone;
		}

		return fDocument;
	}

	/*
	 * @see StyledTextContent#getLine(int)
	 */
	public String getLine(int line) {

		IDocument document= getDocumentForRead();
		try {
			return doGetLine(document, line);
		} catch (BadLocationException x) {
			repairLineInformation(document);
			try {
				return doGetLine(document, line);
			} catch (BadLocationException x2) {
			}
		}

		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		return null;
	}

	/*
	 * @see StyledTextContent#getLineAtOffset(int)
	 */
	public int getLineAtOffset(int offset) {
		IDocument document= getDocumentForRead();
		try {
			return document.getLineOfOffset(offset);
		} catch (BadLocationException x) {
			repairLineInformation(document);
			try {
				return document.getLineOfOffset(offset);
			} catch (BadLocationException x2) {
			}
		}

		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		return -1;
	}

	/*
	 * @see StyledTextContent#getLineCount()
	 */
	public int getLineCount() {
		return getDocumentForRead().getNumberOfLines();
	}

	/*
	 * @see StyledTextContent#getOffsetAtLine(int)
	 */
	public int getOffsetAtLine(int line) {
		IDocument document= getDocumentForRead();
		try {
			return document.getLineOffset(line);
		} catch (BadLocationException x) {
			repairLineInformation(document);
			try {
				return document.getLineOffset(line);
			} catch (BadLocationException x2) {
			}
		}

		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		return -1;
	}

	/*
	 * @see StyledTextContent#getTextRange(int, int)
	 */
	public String getTextRange(int offset, int length) {
		try {
			return getDocumentForRead().get(offset, length);
		} catch (BadLocationException x) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			return null;
		}
	}

	/*
	 * @see StyledTextContent#replaceTextRange(int, int, String)
	 */
	public void replaceTextRange(int pos, int length, String text) {
		try {
			fDocument.replace(pos, length, text);
		} catch (BadLocationException x) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
	}

	/*
	 * @see StyledTextContent#setText(String)
	 */
	public void setText(String text) {
		fDocument.set(text);
	}

	/*
	 * @see StyledTextContent#getCharCount()
	 */
	public int getCharCount() {
		return getDocumentForRead().getLength();
	}

	/*
	 * @see StyledTextContent#getLineDelimiter()
	 */
	public String getLineDelimiter() {
		if (fLineDelimiter == null)
			fLineDelimiter= TextUtilities.getDefaultLineDelimiter(fDocument);
		return fLineDelimiter;
	}

	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		// check whether the given event is the one which was remembered
		if (fEvent == null || event != fEvent)
			return;

		if (isPatchedEvent(event) || (event.getOffset() == 0 && event.getLength() == fRememberedLengthOfDocument)) {
			fLineDelimiter= null;
			fireTextSet();
		} else {
			if (event.getOffset() < fRememberedLengthOfFirstLine)
				fLineDelimiter= null;
			fireTextChanged();
		}
	}

	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {

		fRememberedLengthOfDocument= fDocument.getLength();
		try {
			fRememberedLengthOfFirstLine= fDocument.getLineLength(0);
		} catch (BadLocationException e) {
			fRememberedLengthOfFirstLine= -1;
		}

		fEvent= event;
		rememberEventData(fEvent);
		fireTextChanging();
	}

	/**
	 * Checks whether this event has been changed between <code>documentAboutToBeChanged</code> and
	 * <code>documentChanged</code>.
	 *
	 * @param event the event to be checked
	 * @return <code>true</code> if the event has been changed, <code>false</code> otherwise
	 */
	private boolean isPatchedEvent(DocumentEvent event) {
		return fOriginalEvent.fOffset != event.fOffset || fOriginalEvent.fLength != event.fLength || fOriginalEvent.fText != event.fText;
	}

	/**
	 * Makes a copy of the given event and remembers it.
	 *
	 * @param event the event to be copied
	 */
	private void rememberEventData(DocumentEvent event) {
		fOriginalEvent.fOffset= event.fOffset;
		fOriginalEvent.fLength= event.fLength;
		fOriginalEvent.fText= event.fText;
	}

	/**
	 * Sends a text changed event to all registered listeners.
	 */
	private void fireTextChanged() {

		if (!fIsForwarding)
			return;

		TextChangedEvent event= new TextChangedEvent(this);

		if (fTextChangeListeners != null && fTextChangeListeners.size() > 0) {
			Iterator e= new ArrayList(fTextChangeListeners).iterator();
			while (e.hasNext())
				((TextChangeListener) e.next()).textChanged(event);
		}
	}

	/**
	 * Sends a text set event to all registered listeners.
	 */
	private void fireTextSet() {

		if (!fIsForwarding)
			return;

		TextChangedEvent event = new TextChangedEvent(this);

		if (fTextChangeListeners != null && fTextChangeListeners.size() > 0) {
			Iterator e= new ArrayList(fTextChangeListeners).iterator();
			while (e.hasNext())
				((TextChangeListener) e.next()).textSet(event);
		}
	}

	/**
	 * Sends the text changing event to all registered listeners.
	 */
	private void fireTextChanging() {

		if (!fIsForwarding)
			return;

		try {
		    IDocument document= fEvent.getDocument();
		    if (document == null)
		    	return;

			TextChangingEvent event= new TextChangingEvent(this);
			event.start= fEvent.fOffset;
			event.replaceCharCount= fEvent.fLength;
			event.replaceLineCount= document.getNumberOfLines(fEvent.fOffset, fEvent.fLength) - 1;
			event.newText= fEvent.fText;
			event.newCharCount= (fEvent.fText == null ? 0 : fEvent.fText.length());
			event.newLineCount= (fEvent.fText == null ? 0 : document.computeNumberOfLines(fEvent.fText));

			if (fTextChangeListeners != null && fTextChangeListeners.size() > 0) {
				Iterator e= new ArrayList(fTextChangeListeners).iterator();
				while (e.hasNext())
					 ((TextChangeListener) e.next()).textChanging(event);
			}

		} catch (BadLocationException e) {
		}
	}

	/*
	 * @see IDocumentAdapterExtension#resumeForwardingDocumentChanges()
	 * @since 2.0
	 */
	public void resumeForwardingDocumentChanges() {
		fIsForwarding= true;
		fDocumentClone= null;
		fOriginalContent= null;
		fOriginalLineDelimiters= null;
		fireTextSet();
	}

	/*
	 * @see IDocumentAdapterExtension#stopForwardingDocumentChanges()
	 * @since 2.0
	 */
	public void stopForwardingDocumentChanges() {
		fDocumentClone= null;
		fOriginalContent= fDocument.get();
		fOriginalLineDelimiters= fDocument.getLegalLineDelimiters();
		fIsForwarding= false;
	}
}
