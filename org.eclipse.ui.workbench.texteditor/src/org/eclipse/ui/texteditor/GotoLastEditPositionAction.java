/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

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
 * Goes to last edit position.
 * 
 * @since 3.5
 */
public class GotoLastEditPositionAction extends Action implements IWorkbenchWindowActionDelegate {

	/** The workbench window */
	private IWorkbenchWindow fWindow;
	/** The action */
	private IAction fAction;

	/**
	 * Creates a goto last edit action.
	 */
	public GotoLastEditPositionAction() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IAbstractTextEditorHelpContextIds.GOTO_LAST_EDIT_POSITION_ACTION);
		setId(ITextEditorActionDefinitionIds.GOTO_LAST_EDIT_POSITION);
		setActionDefinitionId(ITextEditorActionDefinitionIds.GOTO_LAST_EDIT_POSITION);
		setEnabled(false);
	}

	/*
	 * @see IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		EditPosition editPosition= TextEditorPlugin.getDefault().getLastEditPosition();
		if (editPosition == null)
			return;

		final Position pos= editPosition.getPosition();
		if (pos == null || pos.isDeleted)
			return;

		IWorkbenchWindow window= getWindow();
		if (window == null)
			return;

		IWorkbenchPage page= window.getActivePage();

		IEditorPart editor;
		try {
			editor= page.openEditor(editPosition.getEditorInput(), editPosition.getEditorId());
		} catch (PartInitException ex) {
			return;
		}

		// Optimization - could also use else branch
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor= (ITextEditor)editor;
			textEditor.selectAndReveal(pos.offset, pos.length);
			return;
		}

		/*
		 * Workaround: send out a text selection
		 * XXX: Needs to be improved, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=32214
		 */
		if (editor != null) {
			IEditorSite site= editor.getEditorSite();
			if (site == null)
				return;

			ISelectionProvider provider= editor.getEditorSite().getSelectionProvider();
			if (provider == null)
				return;

			provider.setSelection(new TextSelection(pos.offset, pos.length));
		}
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		boolean enabled= TextEditorPlugin.getDefault().getLastEditPosition() != null;
		setEnabled(enabled);
		action.setEnabled(enabled);

		// This is no longer needed once the action is enabled.
		if (!enabled) {
			 // adding the same action twice has no effect.
			TextEditorPlugin.getDefault().addLastEditPositionDependentAction(action);
			// this is always the same action for this instance
			fAction= action;
		}
	}

	/**
	 * Returns the workbench window.
	 *
	 * @return the workbench window
	 */
	private IWorkbenchWindow getWindow() {
		if (fWindow == null)
			fWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return fWindow;
	}

	/*
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fWindow= null;
		TextEditorPlugin.getDefault().removeLastEditPositionDependentAction(fAction);
	}
}
