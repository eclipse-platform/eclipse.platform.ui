/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris.Dennis@invidi.com - http://bugs.eclipse.org/bugs/show_bug.cgi?id=29027
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.ISourceViewer;

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
		/*
		 * Implementation note: instead of computing any indentations needed
		 * (which we can't at this generic level), we simply insert a new
		 * line delimiter either at the end of the current line (normal) or
		 * the end of the previous line (reverse). By operating directly on
		 * the text widget, any auto-indent strategies can pick up on the
		 * delimiter and perform any content-dependent modifications.
		 */

		ITextEditor ed= getTextEditor();
		if (!(ed instanceof AbstractTextEditor))
			return;

		if (!validateEditorInputState())
			return;

		AbstractTextEditor editor= (AbstractTextEditor) ed;
		ISourceViewer sv= editor.getSourceViewer();
		if (sv == null)
			return;

		IDocument document= sv.getDocument();
		if (document == null)
			return;

		StyledText st= sv.getTextWidget();
		if (st == null || st.isDisposed())
			return;

		try {
			// get current line
			int widgetOffset= st.getCaretOffset();
			int offset= AbstractTextEditor.widgetOffset2ModelOffset(sv, widgetOffset);
			int currentLineNumber= document.getLineOfOffset(offset);
			IRegion currentLine= document.getLineInformation(currentLineNumber);

			int insertionOffset= -1;
			if (fAbove) {
				if (currentLineNumber != 0) {
					IRegion previousLine= document.getLineInformation(currentLineNumber - 1);
					insertionOffset= previousLine.getOffset() + previousLine.getLength();
				}
			} else {
				insertionOffset= currentLine.getOffset() + currentLine.getLength();
			}

			boolean updateCaret= true;
			int widgetInsertionOffset= AbstractTextEditor.modelOffset2WidgetOffset(sv, insertionOffset);
			if (widgetInsertionOffset == -1 && fAbove) {
				// assume that the previous line was not accessible
				// (e.g. folded, or we are on line 0)
				// -> we insert the newline at the beginning of the current line, after any leading WS
				insertionOffset= currentLine.getOffset() + getIndentationLength(document, currentLine);
				widgetInsertionOffset= AbstractTextEditor.modelOffset2WidgetOffset(sv, insertionOffset);
				updateCaret= false;
			}
			if (widgetInsertionOffset == -1)
				return;

			// mark caret
			Position caret= new Position(insertionOffset, 0);
			document.addPosition(caret);
			st.setSelectionRange(widgetInsertionOffset, 0);

			// operate directly on the widget
			st.replaceTextRange(widgetInsertionOffset, 0, st.getLineDelimiter());

			// restore caret unless an auto-indenter has already moved the caret
			// then leave it alone
			document.removePosition(caret);
			if (updateCaret && st.getSelection().x == widgetInsertionOffset) {
				int widgetCaret= AbstractTextEditor.modelOffset2WidgetOffset(sv, caret.getOffset());
				if (widgetCaret != -1)
					st.setSelectionRange(widgetCaret, 0);
				st.showSelection();
			}

		} catch (BadLocationException e) {
			// ignore
		}
	}

	/**
	 * Computes the indentation length of a line.
	 *
	 * @param document the document
	 * @param line the line
	 * @return the number of whitespace characters at the beginning of
	 *         <code>line</code>
	 * @throws BadLocationException on document access error
	 */
	private int getIndentationLength(IDocument document, IRegion line) throws BadLocationException {
		int pos= line.getOffset();
		int max= pos + line.getLength();
		while (pos < max) {
			if (!Character.isWhitespace(document.getChar(pos)))
				break;
			pos++;
		}
		return pos - line.getOffset();
	}
}
