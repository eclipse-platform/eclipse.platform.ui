/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris.Dennis@invidi.com - http://bugs.eclipse.org/bugs/show_bug.cgi?id=29027
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;

/**
 * This action implements smart return.
 * Instead of breaking the line where we are, we do the following:
 * <p><b>Smart Enter</b>
 * <ul>
 * <li> if the caret is on a line containing any non-whitespace, a line is inserted below the 
 * current one and the caret moved to it,</li>
 * <li> if the caret is on a whitespace-only line, a line is inserted below the current line,
 * but the caret stays in its position.</li>
 * </ul>
 * </p>
 * <p><b>Smart Enter Inverse</b>
 * <ul>
 * <li> if the caret is on a line containing any non-whitespace, we insert a line above the 
 * current one and move the caret to it (i.e. it stays at the same offset in the widget),</li>
 * <li> if the caret is on a whitespace-only line, a line is inserted above the current line,
 * but the caret stays in its logical position (i.e., it gets shifted one line down in the
 * document, but keeps its position relative to the content following the caret).</li>
 * </ul>
 * </p>
 * @since 3.0
 */
public class InsertLineAction extends TextEditorAction {

	/** 
	 * <code>true</code> if this action inserts a line above the current (Smart Enter Inverse),
	 * <code>false</code> otherwise 
	 */
	protected boolean fAbove;

	/**
	 * Creates a new smart enter action.
	 * @param bundle the resource bundle
	 * @param prefix the prefix to use to get properties from <code>bundle</code>
	 * @param textEditor the editor that the action acts upon
	 * @param above whether new lines are inserted above or below the caret's line.
	 */
	public InsertLineAction(ResourceBundle bundle, String prefix, ITextEditor textEditor, boolean above) {
		super(bundle, prefix, textEditor);
		fAbove= above;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.TextEditorAction#update()
	 */
	public void update() {
		super.update();
		if (isEnabled())
			setEnabled(canModifyEditor());
	}

	/*
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		ITextEditor ed= getTextEditor();
		if (!(ed instanceof AbstractTextEditor))
			return;
		
		if (!validateEditorInputState())
			return;
		
		AbstractTextEditor editor= (AbstractTextEditor) ed;
		ISourceViewer sv= editor.getSourceViewer();
		if (sv == null)
			return;
		
		StyledText st= sv.getTextWidget();
		if (st == null || st.isDisposed())
			return;

		// get current line 
		int caretOffset= st.getCaretOffset();
		int lineNumber= st.getLineAtOffset(caretOffset);
		int lineOffset= st.getOffsetAtLine(lineNumber);
		int lineLength= getLineLength(st, lineNumber, lineOffset);

		// insert a new line relative to the current, depending on fAbove
		String line= st.getTextRange(lineOffset, lineLength);
		boolean whiteSpace= isWhitespace(line);
		String delimiter= st.getLineDelimiter();

		int insertionPoint; // where the new line should be inserted

		if (fAbove) {
			if (whiteSpace)
				insertionPoint= caretOffset;
			else
				insertionPoint= lineOffset + getIndentationLength(line);
		} else {
			insertionPoint= lineOffset + lineLength;
		}

		// operating directly on the widget we get all the auto-indentation for free
		st.replaceTextRange(insertionPoint, 0, delimiter);

		int newCaretOffset= -1;
		if (fAbove && !whiteSpace) {
			newCaretOffset=
				st.getOffsetAtLine(lineNumber) + getLineLength(st, lineNumber, lineOffset);
		} else if (fAbove || !whiteSpace) {
			int nextLine= lineNumber + 1;
			int nextLineOffset= st.getOffsetAtLine(nextLine);
			int nextLineLength= getLineLength(st, nextLine, nextLineOffset);
			newCaretOffset= nextLineOffset + nextLineLength;
		}
		if (newCaretOffset != -1) {
			st.setCaretOffset(newCaretOffset);
			st.showSelection();
		}
	}

	/**
	 * Determines the length of a line without the terminating line delimiter
	 * @param st the StyledText widget
	 * @param lineNumber the number of the line
	 * @param lineOffset the line's offset 
	 * @return the length of the line without terminating delimiter
	 */
	private int getLineLength(StyledText st, int lineNumber, int lineOffset) {
		int lineLength;
		if (st.getLineCount() == lineNumber + 1) { // end of display area, no next line
			lineLength= st.getCharCount() - lineOffset;
		} else {
			lineLength= st.getOffsetAtLine(lineNumber + 1); // next line offset
			lineLength -= lineOffset;
			lineLength -= st.getLineDelimiter().length(); // subtract line delimiter
		}
		return lineLength;
	}

	/**
	 * Computes the indentation of a line. 
	 * @param line - a non <code>null</code> string
	 * @return the number of whitespace characters at the beginning of <code>line</code>
	 */
	private int getIndentationLength(String line) {
		Assert.isNotNull(line);
		int pos;
		for (pos= 0; pos < line.length(); pos++) {
			if (!Character.isWhitespace(line.charAt(pos)))
				break;
		}
		return pos;
	}

	/**
	 * Checks if a string consists only of whitespace.
	 * @param string
	 * @return <code>true</code> if <code>string</code> consists of whitespace only,
	 * <code>false</code> otherwise.
	 */
	private boolean isWhitespace(String string) {
		if (string == null)
			return true;
		return string.trim().length() == 0;
	}
}