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

import org.eclipse.jface.text.IMarkRegionTarget;

/**
 * An action to handle emacs-like marked regions.
 *
 * @since 2.0
 */
public class MarkAction extends TextEditorAction {

	/** Sets the mark. */
	public static final int SET_MARK= 0;
	/** Clears the mark. */
	public static final int CLEAR_MARK= 1;
	/** Swaps the mark and the cursor position. */
	public static final int SWAP_MARK= 2;

	/** The mark action type. */
	private final int fType;

	/**
	 * Constructor for MarkAction.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @param type the mark action type, must be one of
	 * <code>SET_MARK</code>, <code>CLEAR_MARK</code> or <code>SWAP_MARK</code>.
	 */
	public MarkAction(ResourceBundle bundle, String prefix, ITextEditor editor, int type) {
		super(bundle, prefix, editor);
		fType= type;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {

		ITextEditor editor= getTextEditor();
		if (editor == null)
			return;

		IMarkRegionTarget target= (IMarkRegionTarget) editor.getAdapter(IMarkRegionTarget.class);
		if (target == null)
			return;

		switch (fType) {
		case SET_MARK:
			target.setMarkAtCursor(true);
			break;

		case CLEAR_MARK:
			target.setMarkAtCursor(false);
			break;

		case SWAP_MARK:
			target.swapMarkAndCursor();
			break;
		}
	}
}
