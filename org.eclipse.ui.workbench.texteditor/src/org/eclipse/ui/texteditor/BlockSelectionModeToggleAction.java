/*******************************************************************************
 * Copyright (c) 2009 Avaloq Evolution AG, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public void run() {
		ITextEditor editor= getTextEditor();
		if (editor instanceof ITextEditorExtension5) {
			ITextEditorExtension5 ext5= (ITextEditorExtension5) editor;
			ext5.setBlockSelectionMode(!ext5.isBlockSelectionModeEnabled());
		}
		update(); // update in case anyone else has directly accessed the widget
	}

	@Override
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
