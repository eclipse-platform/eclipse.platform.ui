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

package org.eclipse.ui.texteditor;


import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * An action to delete a whole line, the fraction of the line that is left from the cursor
 * or the fraction that is right from the cursor.
 *
 * @since 2.0
 */
public class DeleteLineAction extends TextEditorAction {

	/**
	 * Delete the whole line.
	 */
	public static final int WHOLE= 0;
	/**
	 * Delete to the beginning of line.
	 */
	public static final int TO_BEGINNING= 1;
	/**
	 * Delete to the end of line.
	 */
	public static final int TO_END= 2;

	/**
	 * The type of deletion.
	 */
	private final int fType;
	/**
	 * Should the deleted line be copied to the clipboard.
	 * @since 2.1
	 */
	private final boolean fCopyToClipboard;
	/** The deletion target.
	 * @since 2.1
	 */
	private DeleteLineTarget fTarget;

	/**
	 * Creates a line delimiter conversion action.
	 *
	 * @param bundle the resource bundle for UI strings
	 * @param prefix the prefix for the property keys into <code>bundle</code>
	 * @param editor the editor
	 * @param type the line deletion type, must be one of
	 * 	<code>WHOLE_LINE</code>, <code>TO_BEGINNING</code> or <code>TO_END</code>
	 */
	public DeleteLineAction(ResourceBundle bundle, String prefix, ITextEditor editor, int type) {
		this(bundle, prefix, editor, type, true);
	}

	/**
	 * Creates a line deletion action.
	 *
	 * @param bundle the resource bundle for UI strings
	 * @param prefix the prefix for the property keys into <code>bundle</code>
	 * @param editor the editor
	 * @param type the line deletion type, must be one of
	 * 	<code>WHOLE_LINE</code>, <code>TO_BEGINNING</code> or <code>TO_END</code>
	 * @param copyToClipboard if <code>true</code>, the contents of the deleted line are copied to the clipboard
	 * @since 2.1
	 */
	public DeleteLineAction(ResourceBundle bundle, String prefix, ITextEditor editor, int type, boolean copyToClipboard) {
		super(bundle, prefix, editor);
		fType= type;
		fCopyToClipboard= copyToClipboard;
		update();
	}

	/**
	 * Returns the editor's document.
	 *
	 * @param editor the editor
	 * @return the editor's document
	 */
	private static IDocument getDocument(ITextEditor editor) {

		IDocumentProvider documentProvider= editor.getDocumentProvider();
		if (documentProvider == null)
			return null;

		IDocument document= documentProvider.getDocument(editor.getEditorInput());
		if (document == null)
			return null;

		return document;
	}

	/**
	 * Returns the editor's selection.
	 *
	 * @param editor the editor
	 * @return the editor's selection
	 */
	private static ITextSelection getSelection(ITextEditor editor) {

		ISelectionProvider selectionProvider= editor.getSelectionProvider();
		if (selectionProvider == null)
			return null;

		ISelection selection= selectionProvider.getSelection();
		if (!(selection instanceof ITextSelection))
			return null;

		return (ITextSelection) selection;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {

		if (fTarget == null)
			return;

		ITextEditor editor= getTextEditor();
		if (editor == null)
			return;

		if (!validateEditorInputState())
			return;

		IDocument document= getDocument(editor);
		if (document == null)
			return;

		ITextSelection selection= getSelection(editor);
		if (selection == null)
			return;

		try {
			fTarget.deleteLine(document, selection.getOffset(), fType, fCopyToClipboard);
		} catch (BadLocationException e) {
			// should not happen
		}
	}

	/*
	 * @see IUpdate#update()
	 */
	public void update() {

		super.update();
		if (!isEnabled())
			return;

		if (!canModifyEditor()) {
			setEnabled(false);
			return;
		}

		ITextEditor editor= getTextEditor();
		if (editor != null)
			fTarget= (DeleteLineTarget) editor.getAdapter(DeleteLineTarget.class);
		else
			fTarget= null;

		setEnabled(fTarget != null);
	}
}
