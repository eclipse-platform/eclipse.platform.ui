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
 * Text edit to insert a text at a given position in a 
 * document.
 * <p>
 * An insert edit is equivalent to <code>ReplaceEdit(offset, 0, text)
 * </code>
 * 
 * @since 3.0
 */
public final class InsertEdit extends TextEdit {
	
	private String fText;
	
	/**
	 * Constructs a new insert edit.
	 * 
	 * @param offset the insertion offset
	 * @param text the text to insert
	 */
	public InsertEdit(int offset, String text) {
		super(offset, 0);
		fText= text;
	}
	
	/*
	 * Copy constructor
	 */
	private InsertEdit(InsertEdit other) {
		super(other);
		fText= other.fText;
	}
	
	/**
	 * Returns the new text inserted at the offset denoted
	 * by this edit. 
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
		return new InsertEdit(this);
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
