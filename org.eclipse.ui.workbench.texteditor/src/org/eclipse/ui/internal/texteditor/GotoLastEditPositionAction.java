/*
 * Copyright (c) 2000, 2002 IBM Corp. and others..
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package org.eclipse.ui.internal.texteditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Goes to last edit position.
 * 
 * @see org.eclipse.ui.texteditor.EditPosition
 * @since 2.1
 */
public class GotoLastEditPositionAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow fWindow;
	private IAction fAction;

	public GotoLastEditPositionAction() {
		WorkbenchHelp.setHelp(this, IAbstractTextEditorHelpContextIds.GOTO_LAST_EDIT_POSITION_ACTION);
		setId(ITextEditorActionDefinitionIds.GOTO_LAST_EDIT_POSITION);
		setActionDefinitionId(ITextEditorActionDefinitionIds.GOTO_LAST_EDIT_POSITION);
		setEnabled(false);
	}

	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	public void run(IAction action) {
		run();
	}

	public void run() {
		EditPosition editPosition= TextEditorPlugin.getDefault().getLastEditPosition();
		if (editPosition == null)
			return;

		IWorkbenchWindow window= getWindow();
		if (window == null)
			return;
		
		IWorkbenchPage page= window.getActivePage();
			
		IEditorPart editor;
		try {
			editor= page.openEditor(editPosition.getEditorInput(), editPosition.getEditorId());
		} catch (PartInitException ex) {
			editor= null;
		}
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor= (ITextEditor)editor;
			Position pos= editPosition.getPosition();
			if (pos != null && !pos.isDeleted)
				textEditor.selectAndReveal(pos.offset, pos.length);
			
//			textEditor.getSelectionProvider().setSelection(editPosition.getSelection());
		}			
	}

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

	private IWorkbenchWindow getWindow() {
		if (fWindow == null)
			fWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return fWindow;
	}

	public void dispose() {
		fWindow= null;
		TextEditorPlugin.getDefault().removeLastEditPositionDependentAction(fAction);
	}
}
