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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * A range marker can be used to track positions when executing
 * text edits.
 *
 * @since 3.0
 */
public final class RangeMarker extends TextEdit {

	/**
	 * Creates a new range marker for the given offset and length.
	 *
	 * @param offset the marker's offset
	 * @param length the marker's length
	 */
	public RangeMarker(int offset, int length) {
		super(offset, length);
	}

	/*
	 * Copy constructor
	 */
	private RangeMarker(RangeMarker other) {
		super(other);
	}

	@Override
	protected TextEdit doCopy() {
		return new RangeMarker(this);
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
		fDelta= 0;
		return fDelta;
	}

	@Override
	boolean deleteChildren() {
		return false;
	}
}
