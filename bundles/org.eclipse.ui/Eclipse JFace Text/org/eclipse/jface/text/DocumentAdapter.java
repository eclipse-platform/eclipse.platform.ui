package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;

import org.eclipse.jface.util.Assert;


/**
 * Adapts an <code>IDocument</code> to the <code>StyledTextContent</code> interface.
 */
class DocumentAdapter implements IDocumentAdapter, IDocumentListener {
	
	
	/**
	 * Remembers a document event and related information such as the number of lines
	 * as well as the text replaced by the changed described by the document change
	 * event.
	 */
	class EventInfo {
		/** The number of replaced lines. */
		int fReplacedLines;
		/** The replaced text. */
		String fReplacedText;
		/** Inserted lines */
		int fInsertedLines;
		/** The document event */
		DocumentEvent fEvent;
		
		/**
		 * The the given event as the event to be remembered and for which
		 * to remember the associated information.
		 *
		 * @param event the document event to be remembered
		 */
		void setEvent(DocumentEvent event) {
			
			fEvent= event;
			if (fEvent != null) {
				IDocument document= fEvent.getDocument();
				if (document != null) {
					
					// check whether replace or set
					if (fEvent.fOffset != 0 || fEvent.fLength != document.getLength()) {
						try {
							fReplacedLines= document.getNumberOfLines(fEvent.fOffset, fEvent.fLength) - 1;
							fReplacedText= (fEvent.fLength > 0 ? document.get(fEvent.fOffset, fEvent.fLength) : "");
							fInsertedLines= (fEvent.fText == null ? 0 : document.computeNumberOfLines(fEvent.fText));
							return;
						} catch (BadLocationException x) {
							fEvent= null;
						}
					}
				} else
					fEvent= null;
			}
			
			fReplacedLines= -1;
			fReplacedText= null;
			fInsertedLines= -1;
		}
		
		/**
		 * Checks whether the given event is the one which this info is about.
		 *
		 * @param event the event to be checked
		 * @return <code>true</code> if this info is about the given event
		 */
		boolean refersTo(DocumentEvent event) {	
			if (fEvent == null)
				return false;
			return (event == fEvent);
		}
		
		/**
		 * Checks whether the remembered event describes a set text or replace operation.
		 *
		 * @return <code>true</code> if the remembered event describes a set text operation
		 */
		boolean isTextSet() {
			return (fEvent != null && fReplacedLines == -1 && fReplacedText == null);
		}	
	};
	
	
	/** The adapted document. */
	private IDocument fDocument;
	/** The registered text change listeners */
	private List fTextChangeListeners= new ArrayList(1);
	/** The remembered event information */
	private EventInfo fEventInfo= new EventInfo();
	/** The line delimiter */
	private String fLineDelimiter= null;
	
	/**
	 * Creates a new document adapter which is initiallly not connected to
	 * any document.
	 */
	public DocumentAdapter() {
	}
	/*
	 * @see StyledTextContent#addTextChangeListener
	 */
	public void addTextChangeListener(TextChangeListener listener) {
		Assert.isNotNull(listener);
		if (! fTextChangeListeners.contains(listener))
			fTextChangeListeners.add(listener);
	}
	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		fEventInfo.setEvent(event);
		fireTextChanging();
	}
	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		if (fEventInfo.refersTo(event)) {
			if (fEventInfo.isTextSet())
				fireTextSet();
			else
				fireTextChanged();
		}
	}
	/**
	 * Sends a text changed event to all registered listeners.
	 */
	private void fireTextChanged() {
		TextChangedEvent event= new TextChangedEvent(this);
				
		if (fTextChangeListeners != null && fTextChangeListeners.size() > 0) {
			Iterator e= new ArrayList(fTextChangeListeners).iterator();
			while (e.hasNext())
				((TextChangeListener) e.next()).textChanged(event);
		}
	}
	/**
	 * Sends the text changing event to all registered listeners.
	 */
	private void fireTextChanging() {
		TextChangingEvent event= new TextChangingEvent(this);

		event.start= fEventInfo.fEvent.fOffset;
		event.replaceCharCount= fEventInfo.fEvent.fLength;
		event.newCharCount= (fEventInfo.fEvent.fText == null ? 0 : fEventInfo.fEvent.fText.length());
		event.replaceLineCount= fEventInfo.fReplacedLines;
		event.newText= fEventInfo.fEvent.fText;
		event.newLineCount= fEventInfo.fInsertedLines;
		
		if (fTextChangeListeners != null && fTextChangeListeners.size() > 0) {
			Iterator e= new ArrayList(fTextChangeListeners).iterator();
			while (e.hasNext())
				 ((TextChangeListener) e.next()).textChanging(event);
		}
	}
	/**
	 * Sends a text set event to all registered listeners.
	 */
	private void fireTextSet() {
		TextChangedEvent event = new TextChangedEvent(this);
		
		if (fTextChangeListeners != null && fTextChangeListeners.size() > 0) {
			Iterator e= new ArrayList(fTextChangeListeners).iterator();
			while (e.hasNext())
				((TextChangeListener) e.next()).textSet(event);
		}
	}
	/*
	 * @see StyledTextContent#getCharCount()
	 */
	public int getCharCount() {
		return fDocument.getLength();
	}
	/*
	 * @see StyledTextContent#getLine(int)
	 */
	public String getLine(int line) {
		try {
			IRegion r= fDocument.getLineInformation(line);
			return fDocument.get(r.getOffset(), r.getLength());
		} catch (BadLocationException x) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			return null;
		}
	}
	/*
	 * @see StyledTextContent#getLineAtOffset(int)
	 */
	public int getLineAtOffset(int offset) {
		try {
			return fDocument.getLineOfOffset(offset);
		} catch (BadLocationException x) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			return -1;
		}
	}
	/*
	 * @see StyledTextContent#getLineCount()
	 */
	public int getLineCount() {
		return fDocument.getNumberOfLines();
	}
	/*
	 * @see StyledTextContent#getLineDelimiter
	 */
	public String getLineDelimiter() {
		
		if (fLineDelimiter == null) {
			
			try {
				fLineDelimiter= fDocument.getLineDelimiter(0);
			} catch (BadLocationException x) {
			}
			
			if (fLineDelimiter == null)
				fLineDelimiter= System.getProperty("line.separator");
		}
		
		return fLineDelimiter;
	}
	/*
	 * @see StyledTextContent#getOffsetAtLine(int)
	 */
	public int getOffsetAtLine(int line) {
		try {
			return fDocument.getLineOffset(line);
		} catch (BadLocationException x) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			return -1;
		}
	}
	/*
	 * @see StyledTextContent#getTextRange(int, int)
	 */
	public String getTextRange(int offset, int length) {
		try {
			return fDocument.get(offset, length);
		} catch (BadLocationException x) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			return null;
		}
	}
	/*
	 * @see StyledTextContent#removeTextChangeListener
	 */
	public void removeTextChangeListener(TextChangeListener listener) {
		Assert.isNotNull(listener);
		fTextChangeListeners.remove(listener);
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
		
		if (fDocument != null)
			fDocument.addPrenotifiedDocumentListener(this);
	}
	/*
	 * @see StyledTextContent#setText
	 */
	public void setText(String text) {
		fDocument.set(text);
	}
}
