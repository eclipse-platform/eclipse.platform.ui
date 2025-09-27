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

import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * Action for jumping to a particular annotation in the editor's text viewer.
 * <p>
 * This action only runs if <code>getTextEditor()</code>
 * implements {@link org.eclipse.ui.texteditor.ITextEditorExtension4}.</p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GotoAnnotationAction extends TextEditorAction {

	/**
	 * The navigation direction.
	 * <code>true</code> to go to next and <code>false</code> to go to previous annotation.
	 */
	private final boolean fForward;

	/**
	 * Creates a new action for the given text editor. The action configures its
	 * visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @param forward <code>true</code> to go to next and <code>false</code> to go to previous annotation
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 */
	public GotoAnnotationAction(ResourceBundle bundle, String prefix, ITextEditor editor, boolean forward) {
		super(bundle, prefix, editor);
		fForward= forward;
		setHelpContextId(fForward ? IAbstractTextEditorHelpContextIds.GOTO_NEXT_ANNOTATION_ACTION : IAbstractTextEditorHelpContextIds.GOTO_PREVIOUS_ANNOTATION_ACTION);
	}

	/**
	 * Creates a new action for the given text editor. The action configures its
	 * visual representation from the given resource bundle.
	 *
	 * @param editor the text editor
	 * @param forward <code>true</code> to go to next and <code>false</code> to go to previous annotation
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 */
	public GotoAnnotationAction(ITextEditor editor, boolean forward) {
		this(EditorMessages.getBundleForConstructedKeys(), forward ? "Editor.GotoNextAnnotation." : "Editor.GotoPreviousAnnotation.", editor, forward); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void run() {
		ITextEditor editor= getTextEditor();
		if (editor instanceof ITextEditorExtension4) {
			((ITextEditorExtension4)editor).gotoAnnotation(fForward);
		}
	}

	@Override
	public void setEditor(ITextEditor editor) {
		super.setEditor(editor);
		update();
	}

	@Override
	public void update() {
		ITextEditor editor= getTextEditor();
		if (!(editor instanceof AbstractTextEditor)) {
			setEnabled(false);
			return;
		}

		IAnnotationModel model= editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		setEnabled(model != null);
	}
}
