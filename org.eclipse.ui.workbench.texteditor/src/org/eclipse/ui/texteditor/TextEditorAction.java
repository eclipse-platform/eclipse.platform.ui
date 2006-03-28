/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 * Skeleton of a standard text editor action. The action is
 * initially associated with a text editor via the constructor,
 * but can subsequently be changed using <code>setEditor</code>.
 * Subclasses must implement the <code>run</code> method and if
 * required override the <code>update</code> method.
 * <p>
 * Subclasses that may modify the editor content should use {@link #canModifyEditor()}
 * in their <code>update</code> code to check whether updating the editor is most
 * likely possible (even if it is read-only - this may change for editor contents
 * that are under version control) and {@link #validateEditorInputState()} before
 * actually modifying the editor contents.
 * </p>
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
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
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
	 * @see ResourceAction#ResourceAction(ResourceBundle, String, int)
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
	 * If the associated editor is <code>null</code>, the action is disabled.
	 * Subclasses may override.
	 */
	public void update() {
		setEnabled(getTextEditor() != null);
	}

	/**
	 * Checks the editor's modifiable state. Returns <code>true</code> if the editor can be modified,
	 * taking in account the possible editor extensions.
	 *
	 * <p>If the editor implements <code>ITextEditorExtension2</code>,
	 * this method returns {@link ITextEditorExtension2#isEditorInputModifiable()};<br> else if the editor
	 * implements <code>ITextEditorExtension</code>, it returns {@link ITextEditorExtension#isEditorInputReadOnly()};<br>
	 * else, {@link ITextEditor#isEditable()} is returned, or <code>false</code> if the editor is <code>null</code>.</p>
	 *
	 * <p>There is only a difference to {@link #validateEditorInputState()} if the editor implements
	 * <code>ITextEditorExtension2</code>.</p>
	 *
	 * @return <code>true</code> if a modifying action should be enabled, <code>false</code> otherwise
	 * @since 3.0
	 */
	protected boolean canModifyEditor() {
		ITextEditor editor= getTextEditor();
		if (editor instanceof ITextEditorExtension2)
			return ((ITextEditorExtension2) editor).isEditorInputModifiable();
		else if (editor instanceof ITextEditorExtension)
			return !((ITextEditorExtension) editor).isEditorInputReadOnly();
		else if (editor != null)
			return editor.isEditable();
		else
			return false;
	}

	/**
	 * Checks and validates the editor's modifiable state. Returns <code>true</code> if an action
	 * can proceed modifying the editor's input, <code>false</code> if it should not.
	 *
	 * <p>If the editor implements <code>ITextEditorExtension2</code>,
	 * this method returns {@link ITextEditorExtension2#validateEditorInputState()};<br> else if the editor
	 * implements <code>ITextEditorExtension</code>, it returns {@link ITextEditorExtension#isEditorInputReadOnly()};<br>
	 * else, {@link ITextEditor#isEditable()} is returned, or <code>false</code> if the editor is <code>null</code>.</p>
	 *
	 * <p>There is only a difference to {@link #canModifyEditor()} if the editor implements
	 * <code>ITextEditorExtension2</code>.</p>
	 *
	 * @return <code>true</code> if a modifying action can proceed to modify the underlying document, <code>false</code> otherwise
	 * @since 3.0
	 */
	protected boolean validateEditorInputState() {
		ITextEditor editor= getTextEditor();
		if (editor instanceof ITextEditorExtension2)
			return ((ITextEditorExtension2) editor).validateEditorInputState();
		else if (editor instanceof ITextEditorExtension)
			return !((ITextEditorExtension) editor).isEditorInputReadOnly();
		else if (editor != null)
			return editor.isEditable();
		else
			return false;
	}
}
