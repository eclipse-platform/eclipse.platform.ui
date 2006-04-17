/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.explorer;

/**
 * Creates the context menu for a task explorer
 */

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.cheatsheets.composite.model.TaskStateUtilities;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.internal.provisional.cheatsheets.TaskExplorer;

public class TreeExplorerMenu {
	
	private TaskExplorer explorer;

	public TreeExplorerMenu(TaskExplorer explorer) {
		this.explorer = explorer;
		MenuManager menuMgr = new MenuManager(null);
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(explorer.getControl());
		explorer.getControl().setMenu(menu);
	}

	protected void fillContextMenu(IMenuManager manager) {
		ISelection selection = explorer.getSelectionProvider().getSelection();
		ICompositeCheatSheetTask selectedTask = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
			if (structuredSelection.size() == 1) {
			selectedTask = (ICompositeCheatSheetTask)(structuredSelection).getFirstElement();		
			}
		}
		if (selectedTask == null) return;
		
		// Start
	    StartAction startAction = new StartAction(selectedTask);
	    startAction.setEnabled(TaskStateUtilities.isStartEnabled(selectedTask));
		manager.add(startAction);
		
		// Skip
	    SkipAction skipAction = new SkipAction(selectedTask);
	    skipAction.setEnabled(TaskStateUtilities.isSkipEnabled(selectedTask));
		manager.add(skipAction);
		
		// Restart
		Action restartAction;
		if (selectedTask.getParent() == null) {
			restartAction = new RestartAllAction(selectedTask.getCompositeCheatSheet());
		} else {
			restartAction = new ResetTaskAction(selectedTask);
			restartAction.setEnabled(selectedTask.getState() != ICompositeCheatSheetTask.NOT_STARTED);
		}
		manager.add(restartAction);	
	}

}
