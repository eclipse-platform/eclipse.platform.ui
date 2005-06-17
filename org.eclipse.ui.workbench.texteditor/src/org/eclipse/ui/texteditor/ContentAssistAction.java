/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.jface.text.ITextOperationTargetExtension;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.IWorkbenchPartSite;


/**
 * A content assist action which gets its target from its text editor.
 * <p>
 * The action is initially associated with a text editor via the constructor,
 * but can subsequently be changed using <code>setEditor</code>.</p>
 * <p>
 * If this class is used as is, it works by asking the text editor for its text operation target
 * (using <code>getAdapter(ITextOperationTarget.class)</code> and runs the content assist
 * operation on this target.
 * </p>
 * @since 2.0
 */
public final class ContentAssistAction extends TextEditorAction {

	/** The text operation target */
	private ITextOperationTarget fOperationTarget;

	/**
	 * Creates and initializes the action for the given text editor.
	 * The action configures its visual representation from the given resource
	 * bundle. The action works by asking the text editor at the time for its
	 * text operation target adapter (using
	 * <code>getAdapter(ITextOperationTarget.class)</code>. The action runs the
	 * content assist operation on this target.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 */
	public ContentAssistAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	/**
	 * Runs the content assist operation on the editor's text operation target.
	 */
	public void run() {
		if (fOperationTarget != null) {

			ITextEditor editor= getTextEditor();
			if (editor != null && validateEditorInputState()) {

				Display display= null;

				IWorkbenchPartSite site= editor.getSite();
				Shell shell= site.getShell();
				if (shell != null && !shell.isDisposed())
					display= shell.getDisplay();

				BusyIndicator.showWhile(display, new Runnable() {
					public void run() {
						fOperationTarget.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
					}
				});
			}
		}
	}

	/**
	 * The <code>ContentAssistAction</code> implementation of this
	 * <code>IUpdate</code> method discovers the operation through the current
	 * editor's <code>ITextOperationTarget</code> adapter, and sets the
	 * enabled state accordingly.
	 */
	public void update() {

		ITextEditor editor= getTextEditor();

		if (fOperationTarget == null && editor!= null)
			fOperationTarget= (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);

		if (fOperationTarget == null) {
			setEnabled(false);
			return;
		}

		if (fOperationTarget instanceof ITextOperationTargetExtension) {
			ITextOperationTargetExtension targetExtension= (ITextOperationTargetExtension) fOperationTarget;
			targetExtension.enableOperation(ISourceViewer.CONTENTASSIST_PROPOSALS, canModifyEditor());
		}

		setEnabled(fOperationTarget.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS));
	}

	/**
	 * @see TextEditorAction#setEditor(ITextEditor)
	 */
	public void setEditor(ITextEditor editor) {
		super.setEditor(editor);
		fOperationTarget= null;
	}
}
