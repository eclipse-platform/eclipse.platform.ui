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
 * @since 2.0
 */
public class DeleteLineAction extends TextEditorAction {

	/** Delete the whole line. */
	public static final int WHOLE= 0;
	/** Delete to the beginning of line. */
	public static final int TO_BEGINNING= 1;
	/** Delete to the end of line. */
	public static final int TO_END= 2;

	/** The type of deletion */
	private final int fType;
	/** Should the deleted line be copied to the clipboard */
	private final boolean fCopyToClipboard;
	/** The deletion target */
	private DeleteLineTarget fTarget;

	/**
	 * Creates a line delimiter conversion action.
	 * 
	 * @param editor the editor
	 * @param type the line deletion type, must be one of
	 * 	<code>WHOLE_LINE</code>, <code>TO_BEGINNING</code> or <code>TO_END</code>
	 */
	public DeleteLineAction(ResourceBundle bundle, String prefix, ITextEditor editor, int type) {
		this(bundle, prefix, editor, type, true);
	}
	
	/**
	 * Creates a line delimiter conversion action.
	 * 
	 * @param editor the editor
	 * @param type the line deletion type, must be one of
	 * 	<code>WHOLE_LINE</code>, <code>TO_BEGINNING</code> or <code>TO_END</code>
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

		ITextEditor editor= getTextEditor();
		if (editor instanceof ITextEditorExtension) {
			ITextEditorExtension extension= (ITextEditorExtension) editor;
			if (extension.isEditorInputReadOnly()) {
				setEnabled(false);
				return;
			}
		}
		
		if (editor != null)
			fTarget= (DeleteLineTarget) editor.getAdapter(DeleteLineTarget.class);
		else
			fTarget= null;
			
		setEnabled(fTarget != null);
	}
}
