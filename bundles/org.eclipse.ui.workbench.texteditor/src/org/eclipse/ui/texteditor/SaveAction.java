/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;


/**
 * Action for saving recent changes made in the text editor. The action is initially associated with
 * a text editor via the constructor, but that can be subsequently changed using
 * <code>setEditor</code>.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @deprecated As of 3.5, replaced by {@link org.eclipse.ui.actions.ActionFactory#SAVE}
 */
@Deprecated(forRemoval = true, since = "2025-12")
public class SaveAction extends TextEditorAction {

	/**
	 * Creates a new action for the given text editor. The action configures its
	 * visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 */
	public SaveAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	@Override
	public void run() {
		getTextEditor().getSite().getPage().saveEditor(getTextEditor(), false);
	}

	@Override
	public void update() {
		setEnabled(getTextEditor().isDirty());
	}
}
