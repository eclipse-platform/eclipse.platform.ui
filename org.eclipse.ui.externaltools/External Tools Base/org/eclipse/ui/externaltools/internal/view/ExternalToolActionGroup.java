package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.externaltools.action.RunExternalToolAction;
import org.eclipse.ui.externaltools.action.RunWithExternalToolAction;
import org.eclipse.ui.externaltools.model.ExternalTool;

/**
 * The action group for all of the external tools view actions.
 */
public class ExternalToolActionGroup extends ActionGroup {
	private ExternalToolView view;
	private TextActionHandler textActionHandler;
	private NewExternalToolAction newAction;
	private DuplicateExternalToolAction copyAction;
	private DeleteExternalToolAction delAction;
	private RenameExternalToolAction renameAction;
	private RefreshViewAction refreshAction;
	private RunExternalToolAction runAction;
	private RunWithExternalToolAction runWithAction;
	private EditExternalToolPropertiesAction editAction;
	
	public ExternalToolActionGroup(ExternalToolView view) {
		super();
		this.view = view;
		makeActions();
	}
	
	/* (non-Javadoc)
	 * Method declared on ActionGroup.
	 */
	public void fillActionBars(IActionBars actionBars) {
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(runAction);
		toolBar.add(newAction);
		toolBar.add(delAction);
		toolBar.add(refreshAction);
		
		textActionHandler = new TextActionHandler(actionBars); // hooks handlers
		textActionHandler.setCopyAction(copyAction);
		textActionHandler.setDeleteAction(delAction);
		renameAction.setTextActionHandler(textActionHandler);
		
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.REFRESH,
			refreshAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.RENAME, 
			renameAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.PROPERTIES,
			editAction);
	}
	
	/* (non-Javadoc)
	 * Method declared on ActionGroup.
	 */
	public void fillContextMenu(IMenuManager menu) {
		updateActionEnablement();
		
		menu.add(newAction);
		menu.add(new Separator());

		menu.add(copyAction);
		menu.add(delAction);
		menu.add(renameAction);
		menu.add(new Separator());

		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
		menu.add(new Separator());

		menu.add(refreshAction);
		menu.add(new Separator());
		
		menu.add(runAction);
		menu.add(runWithAction);
		menu.add(new Separator());

		menu.add(editAction);
	}
	
	/**
	 * Returns the external tool if found in the selection,
	 * or <code>null</code> if none.
	 */
	private ExternalTool getSelectedTool(IStructuredSelection selection) {
		Object element = selection.getFirstElement();
		if (element instanceof ExternalTool)
			return (ExternalTool)element;
		return null;
	}
	
	/**
 	 * Handles a key pressed event by invoking the appropriate action.
 	 */
	public void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			delAction.run();
			return;
		}
		
		if (event.keyCode == SWT.F2 && event.stateMask == 0) {
			renameAction.run();
			return;
		}

		if (event.keyCode == SWT.F5 && event.stateMask == 0) {
			refreshAction.run();
			return;
		}
	}

	/**
	 * Runs the default action by invoking the appropriate action
	 */
	public void runDefaultAction(IStructuredSelection selection) {
		ExternalTool tool = getSelectedTool(selection);
		runAction.setTool(tool);
		runAction.run();
	}

	/**
	 * Create the actions contained in this group.
	 */
	protected void makeActions() {
		IWorkbenchPage page = view.getSite().getPage();
		newAction = new NewExternalToolAction();
		copyAction = new DuplicateExternalToolAction(page);
		delAction = new DeleteExternalToolAction(page);
		renameAction = new RenameExternalToolAction(view);
		refreshAction = new RefreshViewAction(page);
		runAction = new RunExternalToolAction(page.getWorkbenchWindow());
		runWithAction = new RunWithExternalToolAction(page.getWorkbenchWindow());
		editAction = new EditExternalToolPropertiesAction(view);
	}
	
	/* (non-Javadoc)
	 * Method declared on ActionGroup.
	 */
	public void updateActionBars() {
		updateActionEnablement();
	} 

	/**
	 * Updates the enabled state of the group's actions
	 * based on the current context selection
	 */
	private void updateActionEnablement() {
		ExternalTool selectedTool = getSelectedTool(
			(IStructuredSelection) getContext().getSelection());

		copyAction.setSelectedTool(selectedTool);
		delAction.setSelectedTool(selectedTool);
		renameAction.setSelectedTool(selectedTool);
		runAction.setTool(selectedTool);
		runWithAction.setTool(selectedTool);
		editAction.setSelectedTool(selectedTool);
	}
}
