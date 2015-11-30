/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.edits;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


/**
 * This class encapsulates the reverse changes of an executed text
 * edit tree. To apply an undo memento to a document use method
 * <code>apply(IDocument)</code>.
 * <p>
 * Clients can't add additional children to an undo edit nor can they
 * add an undo edit as a child to another edit. Doing so results in
 * both cases in a <code>MalformedTreeException<code>.
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class UndoEdit extends TextEdit {

	UndoEdit() {
		super(0, Integer.MAX_VALUE);
	}

	private UndoEdit(UndoEdit other) {
		super(other);
	}

	@Override
	void internalAdd(TextEdit child) throws MalformedTreeException {
		throw new MalformedTreeException(null, this, TextEditMessages.getString("UndoEdit.no_children")); //$NON-NLS-1$
	}

	@Override
	void aboutToBeAdded(TextEdit parent) {
		throw new MalformedTreeException(parent, this, TextEditMessages.getString("UndoEdit.can_not_be_added")); //$NON-NLS-1$
	}

	@Override
	UndoEdit dispatchPerformEdits(TextEditProcessor processor) throws BadLocationException {
		return processor.executeUndo();
	}

	@Override
	void dispatchCheckIntegrity(TextEditProcessor processor) throws MalformedTreeException {
		processor.checkIntegrityUndo();
	}

	@Override
	protected TextEdit doCopy() {
		return new UndoEdit(this);
	}

	/*
	 * @see TextEdit#accept0
	 */
	@Override
	protected void accept0(TextEditVisitor visitor) {
		boolean visitChildren= visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor);
		}
	}

	/*
	 * @see TextEdit#performDocumentUpdating
	 */
	@Override
	int performDocumentUpdating(IDocument document) throws BadLocationException {
		fDelta= 0;
		return fDelta;
	}

	void add(ReplaceEdit edit) {
		List<TextEdit> children= internalGetChildren();
		if (children == null) {
			children= new ArrayList<>(2);
			internalSetChildren(children);
		}
		children.add(edit);
	}

	void defineRegion(int offset, int length) {
		internalSetOffset(offset);
		internalSetLength(length);
	}

	@Override
	boolean deleteChildren() {
		return false;
	}
}

