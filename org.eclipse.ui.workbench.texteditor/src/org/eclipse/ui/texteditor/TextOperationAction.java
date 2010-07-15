/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.ITextOperationTarget;

import org.eclipse.ui.IWorkbenchPartSite;


/**
 * An action which gets a text operation target from its text editor.
 * <p>
 * The action is initially associated with a text editor via the constructor,
 * but can subsequently be changed using <code>setEditor</code>.</p>
 * <p>
 * If this class is used as is, it works by asking the text editor for its
 * text operation target adapter (using <code>getAdapter(ITextOperationTarget.class)</code>.
 * The action runs this operation with the pre-configured opcode.</p>
 */
public final class TextOperationAction extends TextEditorAction {

	/** The text operation code */
	private int fOperationCode= -1;
	/** The text operation target */
	private ITextOperationTarget fOperationTarget;
	/**
	 * Indicates whether this action can be executed on read only editors
	 * @since 2.0
	 */
	private boolean fRunsOnReadOnly= false;

	/**
	 * Flag to prevent running twice trough {@link #update()}
	 * when creating this action.
	 * @since 3.2
	 */
	private boolean fAllowUpdate= false;

	/**
	 * Creates and initializes the action for the given text editor and operation
	 * code. The action configures its visual representation from the given resource
	 * bundle. The action works by asking the text editor at the time for its
	 * text operation target adapter (using
	 * <code>getAdapter(ITextOperationTarget.class)</code>. The action runs that
	 * operation with the given opcode.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @param operationCode the operation code
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 */
	public TextOperationAction(ResourceBundle bundle, String prefix, ITextEditor editor, int operationCode) {
		super(bundle, prefix, editor);
		fOperationCode= operationCode;
		fAllowUpdate= true;
		update();
	}

	/**
	 * Creates and initializes the action for the given text editor and operation
	 * code. The action configures its visual representation from the given resource
	 * bundle. The action works by asking the text editor at the time for its
	 * text operation target adapter (using
	 * <code>getAdapter(ITextOperationTarget.class)</code>. The action runs that
	 * operation with the given opcode.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @param operationCode the operation code
	 * @param runsOnReadOnly <code>true</code> if action can be executed on read-only files
	 *
	 * @see TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
	 * @since 2.0
	 */
	public TextOperationAction(ResourceBundle bundle, String prefix, ITextEditor editor, int operationCode, boolean runsOnReadOnly) {
		super(bundle, prefix, editor);
		fOperationCode= operationCode;
		fRunsOnReadOnly= runsOnReadOnly;
		fAllowUpdate= true;
		update();
	}

	/**
	 * The <code>TextOperationAction</code> implementation of this
	 * <code>IAction</code> method runs the operation with the current
	 * operation code.
	 */
	public void run() {
		if (fOperationCode == -1 || fOperationTarget == null)
			return;

		ITextEditor editor= getTextEditor();
		if (editor == null)
			return;

		if (!fRunsOnReadOnly && !validateEditorInputState())
			return;

		Display display= null;

		IWorkbenchPartSite site= editor.getSite();
		Shell shell= site.getShell();
		if (shell != null && !shell.isDisposed())
			display= shell.getDisplay();

		BusyIndicator.showWhile(display, new Runnable() {
			public void run() {
				fOperationTarget.doOperation(fOperationCode);
			}
		});
	}

	/**
	 * The <code>TextOperationAction</code> implementation of this
	 * <code>IUpdate</code> method discovers the operation through the current
	 * editor's <code>ITextOperationTarget</code> adapter, and sets the
	 * enabled state accordingly.
	 */
	public void update() {
		if (!fAllowUpdate)
			return;

		super.update();

		if (!fRunsOnReadOnly && !canModifyEditor()) {
			setEnabled(false);
			return;
		}

		ITextEditor editor= getTextEditor();
		if (fOperationTarget == null && editor!= null && fOperationCode != -1)
			fOperationTarget= (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);

		boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
		setEnabled(isEnabled);
	}

	/*
	 * @see TextEditorAction#setEditor(ITextEditor)
	 */
	public void setEditor(ITextEditor editor) {
		super.setEditor(editor);
		fOperationTarget= null;
	}
}
