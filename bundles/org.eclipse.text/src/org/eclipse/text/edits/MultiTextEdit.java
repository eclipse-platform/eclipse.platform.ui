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

import java.util.List;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * A multi-text edit can be used to aggregate several edits into
 * one edit. The edit itself doesn't modify a document.
 * <p>
 * Clients are allowed to implement subclasses of a multi-text
 * edit.Subclasses must implement <code>doCopy()</code> to ensure
 * the a copy of the right type is created. Not implementing
 * <code>doCopy()</code> in subclasses will result in an assertion
 * failure during copying.
 *
 * @since 3.0
 */
public class MultiTextEdit extends TextEdit {

	private boolean fDefined;

	/**
	 * Creates a new <code>MultiTextEdit</code>. The range
	 * of the edit is determined by the range of its children.
	 *
	 * Adding this edit to a parent edit sets its range to the
	 * range covered by its children. If the edit doesn't have
	 * any children its offset is set to the parent's offset
	 * and its length is set to 0.
	 */
	public MultiTextEdit() {
		super(0, Integer.MAX_VALUE);
		fDefined= false;
	}

	/**
	 * Creates a new <code>MultiTextEdit</code> for the given
	 * range. Adding a child to this edit which isn't covered
	 * by the given range will result in an exception.
	 *
	 * @param offset the edit's offset
	 * @param length the edit's length.
	 * @see TextEdit#addChild(TextEdit)
	 * @see TextEdit#addChildren(TextEdit[])
	 */
	public MultiTextEdit(int offset, int length) {
		super(offset, length);
		fDefined= true;
	}

	/*
	 * Copy constructor.
	 */
	protected MultiTextEdit(MultiTextEdit other) {
		super(other);
	}

	/**
	 * Checks the edit's integrity.
	 * <p>
	 * Note that this method <b>should only be called</b> by the edit
	 * framework and not by normal clients.</p>
	 *<p>
	 * This default implementation does nothing. Subclasses may override
	 * if needed.</p>
	 *
	 * @exception MalformedTreeException if the edit isn't in a valid state
	 *  and can therefore not be executed
	 */
	protected void checkIntegrity() throws MalformedTreeException {
		// does nothing
	}

	@Override
	final boolean isDefined() {
		if (fDefined) {
			return true;
		}
		return hasChildren();
	}

	@Override
	public final int getOffset() {
		if (fDefined) {
			return super.getOffset();
		}

		List<TextEdit> children= internalGetChildren();
		if (children == null || children.isEmpty()) {
			return 0;
		}
		// the children are already sorted
		return children.get(0).getOffset();
	}

	@Override
	public final int getLength() {
		if (fDefined) {
			return super.getLength();
		}

		List<TextEdit> children= internalGetChildren();
		if (children == null || children.isEmpty()) {
			return 0;
		}
		// the children are already sorted
		TextEdit first= children.get(0);
		TextEdit last= children.get(children.size() - 1);
		return last.getOffset() - first.getOffset() + last.getLength();
	}

	@Override
	public final boolean covers(TextEdit other) {
		if (fDefined) {
			return super.covers(other);
		}
		// an undefined multiple text edit covers everything
		return true;
	}

	@Override
	protected boolean canZeroLengthCover() {
		return true;
	}

	@Override
	protected TextEdit doCopy() {
		Assert.isTrue(MultiTextEdit.class == getClass(), "Subclasses must reimplement copy0"); //$NON-NLS-1$
		return new MultiTextEdit(this);
	}

	@Override
	protected void accept0(TextEditVisitor visitor) {
		boolean visitChildren= visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor);
		}
	}

	@Override
	void adjustOffset(int delta) {
		if (fDefined) {
			super.adjustOffset(delta);
		}
	}

	@Override
	void adjustLength(int delta) {
		if (fDefined) {
			super.adjustLength(delta);
		}
	}

	@Override
	void performConsistencyCheck(TextEditProcessor processor, IDocument document) throws MalformedTreeException {
		checkIntegrity();
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

	@Override
	void aboutToBeAdded(TextEdit parent) {
		defineRegion(parent.getOffset());
	}

	void defineRegion(int parentOffset) {
		if (fDefined) {
			return;
		}
		if (hasChildren()) {
			IRegion region= getCoverage(getChildren());
			internalSetOffset(region.getOffset());
			internalSetLength(region.getLength());
		} else {
			internalSetOffset(parentOffset);
			internalSetLength(0);
		}
		fDefined= true;
	}

	@Override
	void internalToString(StringBuilder buffer, int indent) {
		super.internalToString(buffer, indent);
		if (! fDefined) {
			buffer.append(" [undefined]"); //$NON-NLS-1$
		}
	}
}
