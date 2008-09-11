/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Benjamin Muskalla - initial API and implementation - https://bugs.eclipse.org/bugs/show_bug.cgi?id=41573
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;


/**
 * Action for joining two or more lines together by deleting the
 * line delimiters and trimming the whitespace between them.
 *
 * @since 3.3
 */
public class JoinLinesAction extends TextEditorAction {

	private String fJoint= null;


	/**
	 * Creates a line joining action.
	 *
	 * @param bundle the resource bundle for UI strings
	 * @param prefix the prefix for the property keys into <code>bundle</code>
	 * @param editor the editor
	 * @param joint the string to put between the lines
	 */
	public JoinLinesAction(ResourceBundle bundle, String prefix, ITextEditor editor, String joint) {
		super(bundle, prefix, editor);
		Assert.isLegal(joint != null);
		fJoint= joint;
		update();
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {

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

		int startLine= selection.getStartLine();
		int endLine= selection.getEndLine();
		try {
			int caretOffset= joinLines(document, startLine, endLine);
			if (caretOffset > -1)
				editor.selectAndReveal(caretOffset, 0);
		} catch (BadLocationException e) {
			// should not happen
		}

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
	 * @see org.eclipse.ui.texteditor.TextEditorAction#update()
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
		setEnabled(editor.isEditable());
	}

	/**
	 * Joins several text lines to one line.
	 *
	 * @param document the document
	 * @param startLine the start line
	 * @param endLine the end line
	 * @return the new caret offset
	 * @throws BadLocationException if the document is accessed with wrong line or offset
	 */
	private int joinLines(IDocument document, int startLine, int endLine) throws BadLocationException {
		if (startLine == document.getNumberOfLines() - 1) {
			// do nothing because we are in the last line
			return -1;
		}

		if (startLine == endLine)
			endLine++; // append join with the next line

		StringBuffer buffer= new StringBuffer();
		for (int line= startLine; line <= endLine; line++) {
			buffer.append(trim(document, line, line == startLine));
			if (line != endLine)
				buffer.append(fJoint);
		}

		int startLineOffset= document.getLineOffset(startLine);
		int endLineOffset= document.getLineOffset(endLine)	+ document.getLineLength(endLine) - getLineDelimiterLength(document, endLine);
		String replaceString= buffer.toString();
		document.replace(startLineOffset, endLineOffset - startLineOffset, replaceString);
		return startLineOffset + replaceString.length();
	}

	private String trim(IDocument document, int line, boolean ignoreLeadingWhitespace) throws BadLocationException {
		int lineOffset= document.getLineOffset(line);
		int lineLength= document.getLineLength(line);
		lineLength= lineLength - getLineDelimiterLength(document, line);
		if (!ignoreLeadingWhitespace)
			return document.get(lineOffset, lineLength).trim();

		while (lineLength > 0 && Character.isWhitespace(document.getChar(lineOffset + lineLength - 1)))
			lineLength--;

		return document.get(lineOffset, lineLength);
	}

	private int getLineDelimiterLength(IDocument document, int line) throws BadLocationException {
		String lineDelimiter= document.getLineDelimiter(line);
		return lineDelimiter != null ? lineDelimiter.length() : 0;

	}

}
