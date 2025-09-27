/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ari Kast - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.texteditor.EditPosition;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;


/**
 * Goes to next edit position, ie travels forward in the edit position history
 * Acts as a complement to GotoLastEditPositionAction which travels backward in
 * the history.
 *
 * @since 3.15
 */
public class GotoNextEditPositionAction extends Action implements IWorkbenchWindowActionDelegate {

	/** The workbench window */
	private IWorkbenchWindow fWindow;
	/** The action */
	private IAction fAction;

	/**
	 * Creates a goto next edit action.
	 */
	public GotoNextEditPositionAction() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IAbstractTextEditorHelpContextIds.GOTO_NEXT_EDIT_POSITION_ACTION);
		setId(ITextEditorActionDefinitionIds.GOTO_NEXT_EDIT_POSITION);
		setActionDefinitionId(ITextEditorActionDefinitionIds.GOTO_NEXT_EDIT_POSITION);
		setEnabled(false);
	}

	@Override
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void run() {
		if (TextEditorPlugin.TraversalDirection.FORWARD == TextEditorPlugin.getDefault()
				.getEditHistoryTraversalDirection()) {
			TextEditorPlugin.getDefault().advanceEditPosition();
		}
		EditPosition editPosition = TextEditorPlugin.getDefault().getNextEditPosition();
		try {

			if (editPosition == null) {
				return;
			}

			final Position pos = editPosition.getPosition();
			if (pos == null || pos.isDeleted) {
				return;
			}

			IWorkbenchWindow window = getWindow();
			if (window == null) {
				return;
			}

			IWorkbenchPage page = window.getActivePage();

			IEditorPart editor;
			try {
				editor = page.openEditor(editPosition.getEditorInput(), editPosition.getEditorId());
			} catch (PartInitException ex) {
				IStatus status = new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK,
						"Go to Next Edit Location failed", ex); //$NON-NLS-1$
				TextEditorPlugin.getDefault().getLog().log(status);
				return;
			}

			// Optimization - could also use else branch
			if (editor instanceof ITextEditor textEditor) {
				textEditor.selectAndReveal(pos.offset, pos.length);
				return;
			}

			/*
			 * Workaround: send out a text selection XXX: Needs to be improved,
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=32214
			 */
			if (editor != null) {
				IEditorSite site = editor.getEditorSite();
				if (site == null) {
					return;
				}

				ISelectionProvider provider = editor.getEditorSite().getSelectionProvider();
				if (provider == null) {
					return;
				}

				provider.setSelection(new TextSelection(pos.offset, pos.length));
			}
		} finally {
			TextEditorPlugin.getDefault()
					.setEditHistoryTraversalDirection(TextEditorPlugin.TraversalDirection.FORWARD);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		boolean enabled= TextEditorPlugin.getDefault().getLastEditPosition() != null;
		setEnabled(enabled);
		action.setEnabled(enabled);

		// This is no longer needed once the action is enabled.
		if (!enabled) {
			 // adding the same action twice has no effect.
			TextEditorPlugin.getDefault().addLastEditPositionDependentAction(action);
			// this is always the same action for this instance
			fAction = action;
		}
	}

	/**
	 * Returns the workbench window.
	 *
	 * @return the workbench window
	 */
	private IWorkbenchWindow getWindow() {
		if (fWindow == null) {
			fWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		return fWindow;
	}

	@Override
	public void dispose() {
		fWindow= null;
		TextEditorPlugin.getDefault().removeLastEditPositionDependentAction(fAction);
	}
}
