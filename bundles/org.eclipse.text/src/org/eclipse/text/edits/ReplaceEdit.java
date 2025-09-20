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
 * Text edit to replace a range in a document with a different
 * string.
 *
 * @since 3.0
 */
public final class ReplaceEdit extends TextEdit {

	private final String fText;

	/**
	 * Constructs a new replace edit.
	 *
	 * @param offset the offset of the range to replace
	 * @param length the length of the range to replace
	 * @param text the new text
	 */
	public ReplaceEdit(int offset, int length, String text) {
		super(offset, length);
		Assert.isNotNull(text);
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

	@Override
	protected TextEdit doCopy() {
		return new ReplaceEdit(this);
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
		return true;
	}

	@Override
	void internalToString(StringBuilder buffer, int indent) {
		super.internalToString(buffer, indent);
		buffer.append(" <<").append(fText); //$NON-NLS-1$
	}
}
