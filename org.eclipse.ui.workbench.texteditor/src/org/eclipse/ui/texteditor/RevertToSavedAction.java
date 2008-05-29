/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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


/**
 * Action for abandoning changes made in the text editor since the last save
 * operation. The action is initially associated with a text editor via the
 * constructor, but that can be subsequently changed using <code>setEditor</code>.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RevertToSavedAction extends TextEditorAction {

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
	public RevertToSavedAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		getTextEditor().doRevertToSaved();
	}

	/*
	 * @see TextEditorAction#update()
	 */
	public void update() {
		setEnabled(getTextEditor().isDirty());
	}
}
