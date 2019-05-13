/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Global copy action for the welcome editor.
 */
public class WelcomeEditorCopyAction extends Action {
	private WelcomeEditor editorPart;

	public WelcomeEditorCopyAction(WelcomeEditor editor) {
		editorPart = editor;
		setText(IDEWorkbenchMessages.WelcomeEditor_copy_text);
	}

	@Override
	public void run() {
		editorPart.getCurrentText().copy();
	}
}
