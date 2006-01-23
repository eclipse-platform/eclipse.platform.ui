/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.edits;

import org.eclipse.jface.text.BadLocationException;
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
	 * Returns the text to be inserted.
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

	/*
	 * @see TextEdit#accept0
	 */
	protected void accept0(TextEditVisitor visitor) {
		boolean visitChildren= visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor);
		}
	}

	/*
	 * @see TextEdit#performDocumentUpdating
	 */
	/* package */ int performDocumentUpdating(IDocument document) throws BadLocationException {
		document.replace(getOffset(), getLength(), fText);
		fDelta= fText.length() - getLength();
		return fDelta;
	}

	/* non Java-doc
	 * @see TextEdit#deleteChildren
	 */
	/* package */ boolean deleteChildren() {
		return false;
	}

	/* non Java-doc
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString() + " <<" + fText; //$NON-NLS-1$
	}
}
