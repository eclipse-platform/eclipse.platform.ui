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
package org.eclipse.text.edits;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;

/**
 * Text edit to replace a range in a document with a different
 * string.
 * 
 * @since 3.0
 */
public final class ReplaceEdit extends TextEdit {
	
	private String fText;
	
	/**
	 * Constructs a new replace edit.
	 * 
	 * @param offset the offset of the range to replace
	 * @param length the length of the range to replace
	 * @param text the new text
	 */
	public ReplaceEdit(int offset, int length, String text) {
		super(offset, length);
		fText= text;
	}
	
	/*
	 * Copy constructor
	 * 
	 * @param other the edit to copy from
	 */
	private ReplaceEdit(ReplaceEdit other) {
		super(other);
		fText= other.fText;
	}
	
	/**
	 * Returns the new text replacing the text denoted
	 * by the edit.
	 * 
	 * @return the edit's text.
	 */
	public String getText() {
		return fText;
	}
	
	/* non Java-doc
	 * @see TextEdit#doCopy
	 */	
	protected TextEdit doCopy() {
		return new ReplaceEdit(this);
	}
	
	/* non Java-doc
	 * @see TextEdit#perform
	 */	
	/* package */ void perform(IDocument document) throws BadLocationException {
		document.replace(getOffset(), getLength(), fText);
	}
	
	/* non Java-doc
	 * @see TextEdit#update
	 */	
	/* package */ void update(DocumentEvent event, TreeIterationInfo info) {
		markChildrenAsDeleted();
		super.update(event, info);
	}
			
	/* non Java-doc
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString() + " <<" + fText; //$NON-NLS-1$
	}			
}
