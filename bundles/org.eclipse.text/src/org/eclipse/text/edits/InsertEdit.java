/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.text.edits;

import org.eclipse.core.runtime.Assert;

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

	private final String fText;

	/**
	 * Constructs a new insert edit.
	 *
	 * @param offset the insertion offset
	 * @param text the text to insert
	 */
	public InsertEdit(int offset, String text) {
		super(offset, 0);
		Assert.isNotNull(text);
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

	@Override
	protected TextEdit doCopy() {
		return new InsertEdit(this);
	}

	@Override
	protected void accept0(TextEditVisitor visitor) {
		boolean visitChildren= visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor);
		}
	}

	@Override
	int performDocumentUpdating(IDocument document) throws BadLocationException {
		document.replace(getOffset(), getLength(), fText);
		fDelta= fText.length() - getLength();
		return fDelta;
	}

	@Override
	boolean deleteChildren() {
		return false;
	}

	@Override
	void internalToString(StringBuilder buffer, int indent) {
		super.internalToString(buffer, indent);
		buffer.append(" <<").append(fText); //$NON-NLS-1$
	}
}
