/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.text.edits.CopyTargetEdit;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MoveTargetEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditProcessor;
import org.eclipse.text.edits.TextEditVisitor;
import org.eclipse.text.edits.UndoEdit;

/**
 * A text edit processor that brackets the application of edits into a document rewrite session.
 *
 * @since 3.3
 */
public final class RewriteSessionEditProcessor extends TextEditProcessor {
	/** The threshold for <em>large</em> text edits. */
	private static final int THRESHOLD= 1000;

	/**
	 * Text edit visitor that estimates the compound size of an edit tree in characters.
	 */
	private static final class SizeVisitor extends TextEditVisitor {
		int fSize= 0;

		public boolean visit(CopyTargetEdit edit) {
			fSize += edit.getLength();
			return super.visit(edit);
		}

		public boolean visit(DeleteEdit edit) {
			fSize += edit.getLength();
			return super.visit(edit);
		}

		public boolean visit(InsertEdit edit) {
			fSize += edit.getText().length();
			return super.visit(edit);
		}

		public boolean visit(MoveTargetEdit edit) {
			fSize += edit.getLength();
			return super.visit(edit);
		}

		public boolean visit(ReplaceEdit edit) {
			fSize += Math.max(edit.getLength(), edit.getText().length());
			return super.visit(edit);
		}
	}

	/**
	 * Constructs a new edit processor for the given document.
	 *
	 * @param document the document to manipulate
	 * @param root the root of the text edit tree describing the modifications. By passing a text
	 *        edit a a text edit processor the ownership of the edit is transfered to the text edit
	 *        processors. Clients must not modify the edit (e.g adding new children) any longer.
	 * @param style {@link TextEdit#NONE}, {@link TextEdit#CREATE_UNDO} or
	 *        {@link TextEdit#UPDATE_REGIONS})
	 */
	public RewriteSessionEditProcessor(IDocument document, TextEdit root, int style) {
		super(document, root, style);
	}

	/*
	 * @see org.eclipse.text.edits.TextEditProcessor#performEdits()
	 */
	public UndoEdit performEdits() throws MalformedTreeException, BadLocationException {
		IDocument document= getDocument();
		if (!(document instanceof IDocumentExtension4))
			return super.performEdits();

		IDocumentExtension4 extension= (IDocumentExtension4) document;
		boolean isLargeEdit= isLargeEdit(getRoot());
		DocumentRewriteSessionType type= isLargeEdit ? DocumentRewriteSessionType.UNRESTRICTED : DocumentRewriteSessionType.UNRESTRICTED_SMALL;

		DocumentRewriteSession session= extension.startRewriteSession(type);
		try {
			return super.performEdits();
		} finally {
			extension.stopRewriteSession(session);
		}
	}

	/**
	 * Returns <code>true</code> if the passed edit is considered <em>large</em>,
	 * <code>false</code> otherwise.
	 *
	 * @param edit the edit to check
	 * @return <code>true</code> if <code>edit</code> is considered <em>large</em>,
	 *         <code>false</code> otherwise
	 * @since 3.3
	 */
	public static boolean isLargeEdit(TextEdit edit) {
		SizeVisitor sizeVisitor= new SizeVisitor();
		edit.accept(sizeVisitor);
		return sizeVisitor.fSize > THRESHOLD;
	}

}
