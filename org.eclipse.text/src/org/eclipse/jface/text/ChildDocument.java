/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;



/**
 * A child document represent a range of its parent document. 
 * The child document is always in sync with its parent document
 * by utilizing the parent document as its <code>ITextStore</code>.
 * This class is for internal use only.
 *
 * @see ITextStore
 */
public final class ChildDocument extends AbstractDocument {
	
	
	/**
	 * Implements ITextStore based on IDocument.
	 */
	class TextStore implements ITextStore {
		
		/*
		 * @see ITextStore#set
		 */
		public void set(String txt) {
			try {
				fParentDocument.replace(fRange.getOffset(), fRange.getLength(), txt);
			} catch (BadLocationException x) {
				// cannot happen
			}
		}
		
		/*
		 * @see ITextStore#replace
		 */
		public void replace(int offset, int length, String txt) {
			try {
				fParentDocument.replace(fRange.getOffset() + offset, length, txt);
			} catch (BadLocationException x) {
				// ignored as surrounding document should have handled this
			}
		}
		
		/*
		 * @see ITextStore#getLength
		 */
		public int getLength() {
			return fRange.getLength();
		}
		
		/*
		 * @see ITextStore#get
		 */
		public String get(int offset, int length) {
			try {
				return fParentDocument.get(fRange.getOffset() + offset, length);
			} catch (BadLocationException x) {
			}
			
			return null;
		}
		
		/*
		 * @see ITextStore#get
		 */
		public char get(int offset) {
			try {
				return fParentDocument.getChar(fRange.getOffset() + offset);
			} catch (BadLocationException x) {
			}
			
			return (char) 0;
		}
	};
	
	
	
	/** The parent document */
	private IDocument fParentDocument;
	/** 
	 * The parent document as document extension
	 * @since 2.0
	 */
	private IDocumentExtension fExtension;
	/**
	 * The parent document as document extension 2
	 * @since 2.1
	 */
	private IDocumentExtension2 fExtension2;
	
	/** The section inside the parent document */
	private Position fRange;
	/** The document event issued by the parent document */
	private DocumentEvent fParentEvent;
	/** The document event issued and to be issued by the child document */
	private DocumentEvent fEvent;
	/** Indicates whether the child document initiated a parent document update or not */
	private boolean fIsUpdating= false;
	/** The expected document content after the parent document changed. */
	private String fExpectedContent;
	/** The length of this child document prior to the change of the parent document */
	private int fRememberedLength;
	/** Indicates whether this document is in autpo expand mode */
	private boolean fIsAutoExpanding= false;
	
	/**
	 * Creates a child document for the given range of the given parent document.
	 *
	 * @param parentDocument the parent Document
	 * @param range the parent document range covered by the child document
	 */
	public ChildDocument(IDocument parentDocument, Position range) {
		super();
		
		fParentDocument= parentDocument;
		if (fParentDocument instanceof IDocumentExtension) 
			fExtension= (IDocumentExtension) fParentDocument;
		if (fParentDocument instanceof IDocumentExtension2)
			fExtension2= (IDocumentExtension2) fParentDocument;
			
		fRange= range;
		
		ITextStore s= new TextStore();
		ILineTracker tracker= new DefaultLineTracker();
		tracker.set(s.get(0, fRange.getLength()));
		
		setTextStore(s);
		setLineTracker(tracker);
		
		completeInitialization();
	}
	
	/**
	 * Sets the child document's parent document range.
	 *
	 * @param offset the offset of the parent document range
	 * @param length the length of the parent document range
	 */
	public void setParentDocumentRange(int offset, int length) throws BadLocationException {
		
		if (offset < 0 || length < 0 || offset + length > fParentDocument.getLength())
			throw new BadLocationException();
								
		fRange.setOffset(offset);
		fRange.setLength(length);
		
		getTracker().set(fParentDocument.get(offset, length));
	}
	
	/**
	 * Returns parent document
	 *
	 * @return the parent document
	 */
	public IDocument getParentDocument() {
		return fParentDocument;
	}
	
	/**
	 * Returns the range of the parent document covered by this child document.
	 *
	 * @return the child document's parent document range
	 */
	public Position getParentDocumentRange() {
		return fRange;
	}
		
	/**
	 * <p>Transforms a document event of the parent document into a child document
	 * based document event. </p>
	 * This method is public for test purposes only.
	 *
	 * @param e the parent document event
	 * @return the child document event
	 */
	public DocumentEvent normalize(DocumentEvent event) {
				
		int delta= event.getOffset() - fRange.getOffset();
		
		if (isAutoExpandEvent(event)) {
						
			if (delta < 0) {
				int eventEndOffset= event.getOffset() + event.getLength();
				
				if (eventEndOffset <= fRange.getOffset()) {
					
					// case 1
					StringBuffer buffer= new StringBuffer();
					if (event.getText() != null)
						buffer.append(event.getText());
					try {
						buffer.append(fParentDocument.get(eventEndOffset, -delta - event.getLength()));
					} catch (BadLocationException e) {
						// should not happen as the event is a valid parent document event
					}
					
					return new SlaveDocumentEvent(this, 0, 0, buffer.toString(), event);
				
				} else {
					
					// cases 2 and 3
					int length= Math.min(eventEndOffset - fRange.getOffset(), fRange.getLength());
					return new SlaveDocumentEvent(this, 0, length, event.getText(), event);
				}
				
			} else {
				int rangeEndOffset= fRange.getOffset() + fRange.getLength();
				
				if (event.getOffset() >= rangeEndOffset) {
					
					// case 5
					StringBuffer buffer= new StringBuffer();
					try {
						buffer.append(fParentDocument.get(rangeEndOffset, event.getOffset() - rangeEndOffset));
					} catch (BadLocationException e) {
						// should not happen as this event is a valid parent document event
					}
					if (event.getText() != null)
						buffer.append(event.getText());
						
					return new SlaveDocumentEvent(this, fRange.getLength(), 0, buffer.toString(), event);
				
				} else {
					
					// case 4 and 6
					int offset= event.getOffset() - fRange.getOffset();
					int length= Math.min(rangeEndOffset - event.getOffset(), event.getLength());
					return new SlaveDocumentEvent(this, offset, length, event.getText(), event);
				}
			}
		
		} else if (fRange.overlapsWith(event.fOffset, event.fLength)) {			
			
			int offset= delta < 0 ? 0 : delta;
			int length= delta < 0 ? event.fLength + delta : event.fLength;
			if (offset + length > fRange.getLength())
				length= fRange.getLength() - offset;
				
			return new SlaveDocumentEvent(this, offset, length, event.fText, event);
		}
		
		return null;
	}
	
	/**
	 * When called this child document is informed about a forthcoming change
	 * of its parent document. This child document checks whether the parent
	 * document changed affects it and if so informs all document listeners.
	 *
	 * @param event the parent document event
	 */
	public void parentDocumentAboutToBeChanged(DocumentEvent event) {
		
		fParentEvent= event;
		
		fEvent= normalize(event);
		if (fEvent != null) {			
			
			StringBuffer buffer= new StringBuffer(get());
			fRememberedLength= buffer.length();
			buffer.replace(fEvent.fOffset, fEvent.fOffset+ fEvent.fLength, fEvent.fText == null ? "" : fEvent.fText);  //$NON-NLS-1$
			fExpectedContent= buffer.toString();
			
			delayedFireDocumentAboutToBeChanged();
		}
	}
		
	/**
	 * When called this child document is informed about a change of its parent document.
	 * If this child document is affected it informs all of its document listeners.
	 *
	 * @param event the parent document event
	 */
	public void parentDocumentChanged(DocumentEvent event) {
		if ( !fIsUpdating && event == fParentEvent && fEvent != null) {
			try {
				
				if (!fExpectedContent.equals(get())) {
					// patch the event
					fEvent.fOffset= 0;
					fEvent.fLength= fRememberedLength;
					fEvent.fText= get();
				}
				
				fRememberedLength= 0;
				fExpectedContent= null;
				
				getTracker().replace(fEvent.fOffset, fEvent.fLength, fEvent.fText);
				fireDocumentChanged(fEvent);
				
			} catch (BadLocationException x) {
				Assert.isLegal(false);
			}
		}
	}
	
	/*
	 * @see AbstractDocument#fireDocumentAboutToBeChanged
	 */
	protected void fireDocumentAboutToBeChanged(DocumentEvent event) {
		// delay it until there is a notification from the parent document
		// otherwise there it is expensive to construct the parent document information
	}
	
	/**
	 * Fires the child document event as about-to-be-changed event to all
	 * registed listeners.
	 */
	private void delayedFireDocumentAboutToBeChanged() {
		super.fireDocumentAboutToBeChanged(fEvent);
	}
	
	/**
	 * Ignores the given event and sends the similar child document event instead.
	 *
	 * @param event the event to be ignored
	 */
	protected void fireDocumentChanged(DocumentEvent event) {
		super.fireDocumentChanged(fEvent);
	}
	
	/*
	 * @see IDocument#replace(int, int, String)
	 * @since 2.0
	 */
	public void replace(int offset, int length, String text) throws BadLocationException {
		try {
			fIsUpdating= true;
			if (fExtension != null)
				fExtension.stopPostNotificationProcessing();
				
			if ((0 > offset) || (0 > length) || (offset + length > getLength()))
				throw new BadLocationException();
			
			DocumentEvent event= new DocumentEvent(this, offset, length, text);
			fireDocumentAboutToBeChanged(event);
					
			try {
			
				if (fExtension2 != null)
					fExtension2.stopListenerNotification();
				
				getStore().replace(offset, length, text);
				getTracker().replace(offset, length, text);
				updateDocumentStructures(fEvent);
			
			} finally {
				if (fExtension2 != null)
					fExtension2.resumeListenerNotification();
			}
		 			
			doFireDocumentChanged(fEvent);
						
		} finally {
			fIsUpdating= false;
			if (fExtension != null)
				fExtension.resumePostNotificationProcessing();
		}
	}
	
	/*
	 * @see IDocument#set(String)
	 * @since 2.0
	 */
	public void set(String text) {
		try {
			fIsUpdating= true;
			if (fExtension != null)
				fExtension.stopPostNotificationProcessing();
			
			int length= getStore().getLength();
			DocumentEvent event= new DocumentEvent(this, 0, length, text);
			fireDocumentAboutToBeChanged(event);
		
			try {
				
				if (fExtension2 != null)
					fExtension2.stopListenerNotification();
					
				getStore().set(text);
				getTracker().set(text);
				updateDocumentStructures(fEvent);
				
			} finally {
				if (fExtension2 != null)
					fExtension2.resumeListenerNotification();
			}
			
			doFireDocumentChanged(fEvent);

		} finally {
			fIsUpdating= false;
			if (fExtension != null)
				fExtension.resumePostNotificationProcessing();
		}
	}
	
	/*
	 * @see IDocumentExtension#registerPostNotificationReplace(IDocumentListener, IDocumentExtension.IReplace)
	 * @since 2.0
	 */
	public void registerPostNotificationReplace(IDocumentListener owner, IDocumentExtension.IReplace replace) {
		if (!fIsUpdating)
			throw new UnsupportedOperationException();
		super.registerPostNotificationReplace(owner, replace);
	}
	
	/**
	 * Sets the auto expand mode of this document.
	 * @param autoExpand <code>true</code> if auto expanding, <code>false</code> otherwise
	 */
	public void setAutoExpandMode(boolean autoExpand) {
		fIsAutoExpanding= autoExpand;
	}
	
	/**
	 * Returns this document's auto expand mode.
	 * @return this document's auto expand mode
	 */	
	public boolean isAutoExpandEvent(DocumentEvent event) {
		return fIsAutoExpanding;
	}
}
