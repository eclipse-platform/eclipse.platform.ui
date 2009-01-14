/*******************************************************************************
 * Copyright (c) 2009 Avaloq Evolution AG, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Eicher (Avaloq Evolution AG) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;


/**
 * This action toggles the block (aka column) selection mode.
 * 
 * @since 3.5
 */
final class BlockSelectionModeToggleAction extends TextEditorAction {

	/**
	 * Construct the action and initialize its state.
	 * 
	 * @param resourceBundle  the resource bundle to construct label and tooltip from
	 * @param prefix  the prefix to use for constructing resource bundle keys
	 * @param editor  the editor this action is associated with
	 */
	public BlockSelectionModeToggleAction(ResourceBundle resourceBundle, String prefix, ITextEditor editor) {
		super(resourceBundle, prefix, editor, IAction.AS_CHECK_BOX);
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		ITextEditor editor= getTextEditor();
		if (editor instanceof ITextEditorExtension5) {
			ITextEditorExtension5 ext5= (ITextEditorExtension5) editor;
			ext5.setBlockSelectionMode(!ext5.isBlockSelectionModeEnabled());
		}
		update(); // update in case anyone else has directly accessed the widget
	}

	/*
	 * @see org.eclipse.ui.texteditor.TextEditorAction#update()
	 */
	public void update() {
		ITextEditor editor= getTextEditor();
		if (editor instanceof ITextEditorExtension5) {
			ITextEditorExtension5 ext5= (ITextEditorExtension5) editor;
			setEnabled(true);
			setChecked(ext5.isBlockSelectionModeEnabled());
			return;
		}
		setEnabled(false);
		setChecked(false);
	}
}
