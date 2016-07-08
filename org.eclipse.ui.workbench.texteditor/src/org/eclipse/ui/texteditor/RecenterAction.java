/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Daesung Ha <nberserk@gmail.com> - supports top and bottom scrolling - https://bugs.eclipse.org/bugs/show_bug.cgi?id=412267
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.jface.text.source.ISourceViewer;


/**
 * An action to handle emacs-like recenter.
 * This function scrolls the selected window to put the cursor at the middle/top/bottom of the screen.
 *
 * @since 3.3
 */
public class RecenterAction extends TextEditorAction {

	private static final int RECENTER_MIDDLE= 0;
	private static final int RECENTER_TOP= 1;
	private static final int RECENTER_BOTTOM= 2;
	private static final int RECENTER_POS_SIZE= 3;

	private int fPrevOffset= -1;
	private int fDestPos;

	/**
	 * Creates a new action for the given text editor. The action configures its
	 * visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 */
	public RecenterAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	@Override
	public void run() {
		ITextEditor editor= getTextEditor();
		if (!(editor instanceof AbstractTextEditor))
			return;

		ISourceViewer viewer= ((AbstractTextEditor)editor).getSourceViewer();
		if (viewer == null)
			return;

		StyledText st= viewer.getTextWidget();
		if (st == null)
			return;

		// compute the number of lines displayed
		int height= st.getClientArea().height;
		int lineHeight= st.getLineHeight();
		int rowsPerScreen= height / lineHeight;

		int caretOffset= st.getCaretOffset();
		int caretLine= st.getLineAtOffset(caretOffset);
		if (caretOffset==fPrevOffset) { // if successive call in same position
			fDestPos++;
			fDestPos%= RECENTER_POS_SIZE;
		}else{
			fDestPos= RECENTER_MIDDLE;
		}
		fPrevOffset= caretOffset;

		int line= 0;
		switch (fDestPos) {
			case RECENTER_MIDDLE:
				line= Math.max(0, (caretLine - rowsPerScreen / 2));
				break;
			case RECENTER_TOP:
				line= caretLine;
				break;
			case RECENTER_BOTTOM:
				line= Math.max(0, caretLine - rowsPerScreen + 1);
				break;
			default:
				break;
		}
		st.setTopIndex(line);
	}
}