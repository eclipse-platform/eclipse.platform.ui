/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;


import java.util.ResourceBundle;

/**
 * Skeleton of a standard text editor action. The action is 
 * initially associated with a text editor via the constructor,
 * but can subsequently be changed using <code>setEditor</code>.
 * Subclasses must implement the <code>run</code> method and if
 * required override the <code>update</code> method.
 */
public abstract class TextEditorAction extends ResourceAction implements IUpdate {
	
	/** The action's editor */
	private ITextEditor fTextEditor;
	
	/**
	 * Creates and initializes the action for the given text editor. The action
	 * configures its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see ResourceAction#ResourceAction
	 */
	protected TextEditorAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix);
		setEditor(editor);
		update();
	}

	/**
	 * Creates and initializes the action for the given text editor. The action
	 * configures its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @param style the style of this action
	 * @see ResourceAction#ResourceAction
	 * @since 3.0
	 */
	protected TextEditorAction(ResourceBundle bundle, String prefix, ITextEditor editor, int style) {
		super(bundle, prefix, style);
		setEditor(editor);
		update();
	}
	
	/**
	 * Returns the action's text editor.
	 *
	 * @return the action's text editor
	 */
	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}
	
	/**
	 * Retargets this action to the given editor.
	 *
	 * @param editor the new editor, or <code>null</code> if none
	 */
	public void setEditor(ITextEditor editor) {
		fTextEditor= editor;
	}
	
	/**
	 * Always enables this action if it is connected to a text editor.
	 * If the asocciated editor is <code>null</code>, the action is disabled.
	 * Subclasses may override.
	 */
	public void update() {
		setEnabled(getTextEditor() != null);
	}	
}
