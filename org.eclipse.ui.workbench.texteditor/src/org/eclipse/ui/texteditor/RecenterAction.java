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
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.jface.text.source.ISourceViewer;

/**
 * An action to handle emacs-like recenter.
 * This function scrolls the selected window to put the cursor at the middle of the screen.
 *
 * @since 3.3
 */
public class RecenterAction extends TextEditorAction {

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

	/*
	 * @see IAction#run()
	 */
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

		int caretOffset= st.getCaretOffset();
		int caretLine= st.getLineAtOffset(caretOffset);

		int topLine= Math.max(0, (caretLine - (height / (lineHeight * 2))));
		st.setTopIndex(topLine);
	}
}
