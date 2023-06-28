/*******************************************************************************
 * Copyright (c) 2015 Andrey Loskutov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;


/**
 * This action toggles the word wrap in the editor.
 *
 * @since 3.10
 */
final class WordWrapToggleAction extends TextEditorAction {

	/**
	 * Construct the action and initialize its state.
	 *
	 * @param resourceBundle  the resource bundle to construct label and tooltip from
	 * @param prefix  the prefix to use for constructing resource bundle keys
	 * @param editor  the editor this action is associated with
	 * @param checked initial toggle state
	 */
	public WordWrapToggleAction(ResourceBundle resourceBundle, String prefix, ITextEditor editor, boolean checked) {
		super(resourceBundle, prefix, editor, IAction.AS_CHECK_BOX);
		update(checked);
	}

	@Override
	public void run() {
		boolean newState = false;
		if (isWordWrapPossible()) {
			ITextEditorExtension6 ext6= (ITextEditorExtension6) getTextEditor();
			newState= !ext6.isWordWrapEnabled();
			ext6.setWordWrap(newState);
		}
		update(newState);
	}

	private void update(boolean checked) {
		setEnabled(isWordWrapPossible());
		setChecked(checked);
	}

	@Override
	public void update() {
		setEnabled(isWordWrapPossible());
		setChecked(isWordWrapEnabled());
	}

	private boolean isWordWrapPossible(){
		return getTextEditor() instanceof ITextEditorExtension6;
	}

	private boolean isWordWrapEnabled(){
		if (isWordWrapPossible()) {
			return ((ITextEditorExtension6)getTextEditor()).isWordWrapEnabled();
		}
		return false;
	}
}
