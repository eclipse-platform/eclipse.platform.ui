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

package org.eclipse.jface.text;


/**
 * Specification of changes applied to documents. All changes are represented as
 * replace commands, i.e. specifying a document range whose text gets replaced
 * with different text. In addition to this information, the event also contains
 * the changed document.
 * 
 * @see org.eclipse.jface.text.IDocument
 */
public class DocumentEvent {
	
	/** The changed document */
	public IDocument fDocument;
	/** The document offset */
	public int fOffset;
	/** Length of the replaced document text */
	public int fLength;
	/** Text inserted into the document */
	public String fText;
	
	/**
	 * Creates a new document event.
	 *
	 * @param doc the changed document
	 * @param offset the offset of the replaced text
	 * @param length the length of the replaced text
	 * @param text the substitution text
	 */
	public DocumentEvent(IDocument doc, int offset, int length, String text) {
		
		Assert.isNotNull(doc);
		Assert.isTrue(offset >= 0);
		Assert.isTrue(length >= 0);
		
		fDocument= doc;
		fOffset= offset;
		fLength= length;
		fText= text;
	}
	
	/**
	 * Creates a new, not initialized document event.
	 */
	public DocumentEvent() {
	}
	
	/**
	 * Returns the changed document.
	 *
	 * @return the changed document
	 */
	public IDocument getDocument() {
		return fDocument;
	}
	
	/**
	 * Returns the offset of the change.
	 * 
	 * @return the offset of the change
	 */
	public int getOffset() {
		return fOffset;
	}

	/**
	 * Returns the length of the replaced text.
	 *
	 * @return the length of the replaced text
	 */
	public int getLength() {
		return fLength;
	}
			
	/**
	 * Returns the text that has been inserted.
	 *
	 * @return the text that has been inserted
	 */
	public String getText() {
		return fText;
	}
}
