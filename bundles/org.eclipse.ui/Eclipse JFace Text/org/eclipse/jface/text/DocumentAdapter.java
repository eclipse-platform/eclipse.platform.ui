package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;import java.util.Iterator;import java.util.List;import org.eclipse.swt.SWT;import org.eclipse.swt.custom.TextChangeListener;import org.eclipse.swt.custom.TextChangedEvent;import org.eclipse.swt.custom.TextChangingEvent;import org.eclipse.jface.util.Assert;


/**
 * Adapts an <code>IDocument</code> to the <code>StyledTextContent</code> interface.
 */
class DocumentAdapter implements IDocumentAdapter, IDocumentListener, IDocumentAdapterExtension {

	/** The adapted document. */
	private IDocument fDocument;
	/** The registered text change listeners */
	private List fTextChangeListeners= new ArrayList(1);
	/** The remembered document event */
	private DocumentEvent fEvent;
	/** The line delimiter */
	private String fLineDelimiter= null;
	/** Indicates whether this adapter is forwarding document changes */
	private boolean fIsForwarding= true;
	
	private boolean fInvalidateLineDelimiter;
	
	/**
	 * Creates a new document adapter which is initiallly not connected to
	 * any document.
	 */
	public DocumentAdapter() {
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
	 * @see StyledTextContent#addTextChangeListener
	 */
	public void addTextChangeListener(TextChangeListener listener) {
		Assert.isNotNull(listener);
		if (! fTextChangeListeners.contains(listener))
			fTextChangeListeners.add(listener);
	}
		
	/*
	 * @see StyledTextContent#removeTextChangeListener
	 */
	public void removeTextChangeListener(TextChangeListener listener) {
		Assert.isNotNull(listener);
		fTextChangeListeners.remove(listener);
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
	 * @see StyledTextContent#setText
	 */
	public void setText(String text) {
		fDocument.set(text);
	}
	
	/*
	 * @see StyledTextContent#getCharCount()
	 */
	public int getCharCount() {
		return fDocument.getLength();
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
			
			if (fLineDelimiter == null) {
				/*
				 * Follow up fix for: 1GF5UU0: ITPJUI:WIN2000 - "Organize Imports" in java editor inserts lines in wrong format
				 * The line delimiter must always be a legal document line delimiter.
				 */
				String sysLineDelimiter= System.getProperty("line.separator"); //$NON-NLS-1$
				String[] delimiters= fDocument.getLegalLineDelimiters();
				Assert.isTrue(delimiters.length > 0);
				for (int i= 0; i < delimiters.length; i++) {
					if (delimiters[i].equals(sysLineDelimiter)) {
						fLineDelimiter= sysLineDelimiter;
						break;
					}
				}
				
				if (fLineDelimiter == null) {
					// system line delimiter is not a legal document line delimiter
					fLineDelimiter= delimiters[0];
				}
			}
		}
		
		return fLineDelimiter;
	}
	
	/*
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		// check whether the given event is the one which was remembered
		if (fEvent != null && event == fEvent)
			fireTextChanged();

		if (fInvalidateLineDelimiter) {
			fLineDelimiter= null;
			fInvalidateLineDelimiter= false;
		}			
	}
	
	/*
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		try {
			// invalidate cached line delimiter if first line of document was changed
			if (event.getOffset() < fDocument.getLineLength(0))
				fInvalidateLineDelimiter= true;
		} catch (BadLocationException e) {
		}

		// Remember the given event
	    fEvent= event;
		fireTextChanging();
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
	 */
	public void resumeForwardingDocumentChanges() {
		fIsForwarding= true;
		fireTextSet();
	}
	
	/*
	 * @see IDocumentAdapterExtension#stopForwardingDocumentChanges()
	 */
	public void stopForwardingDocumentChanges() {
		fIsForwarding= false;
	}
}