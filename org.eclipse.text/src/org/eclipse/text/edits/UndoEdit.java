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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * This class encapsulates the reverse changes of an executed text 
 * edit tree. To apply an undo memento to a document use method
 * <code>apply(IDocument)</code>.
 * <p>
 * Clients can add additional children to an undo edit nor can they
 * add an undo edit as a child to another edit. Doing so results in 
 * both cases in a <code>MalformedTreeException<code>.
 * 
 * @since 3.0
 */
public final class UndoEdit extends TextEdit {
	
	/* package */ UndoEdit() {
		super(0, Integer.MAX_VALUE);
	}
	
	private UndoEdit(UndoEdit other) {
		super(other);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.text.edits.TextEdit#internalAdd(org.eclipse.text.edits.TextEdit)
	 */
	/* package */ void internalAdd(TextEdit child) throws MalformedTreeException {
		throw new MalformedTreeException(null, this, TextEditMessages.getString("UndoEdit.no_children")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.text.edits.MultiTextEdit#aboutToBeAdded(org.eclipse.text.edits.TextEdit)
	 */
	/* package */ void aboutToBeAdded(TextEdit parent) {
		throw new MalformedTreeException(parent, this, TextEditMessages.getString("UndoEdit.can_not_be_added")); //$NON-NLS-1$
	}

	/* package */ UndoEdit dispatchPerformEdits(TextEditProcessor processor) throws BadLocationException {
		return processor.executeUndo();
	}
	
	/* package */ void dispatchCheckIntegrity(TextEditProcessor processor) throws MalformedTreeException {
		processor.checkIntegrityUndo();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.text.edits.TextEdit#doCopy()
	 */
	protected TextEdit doCopy() {
		return new UndoEdit(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.text.edits.TextEdit#perform(org.eclipse.jface.text.IDocument)
	 */
	void perform(IDocument document) throws BadLocationException {
		// do nothing. Only the children modify the
		// document
	}
	
	/* package */ void add(ReplaceEdit edit) {
		List children= internalGetChildren();
		if (children == null) {
			children= new ArrayList(2);
			internalSetChildren(children);
		}
		children.add(edit);
	}
	
	/* package */ void defineRegion(int offset, int length) {
		internalSetOffset(offset);
		internalSetLength(length);
	}
}

