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
package org.eclipse.debug.internal.ui.views.console;


import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

class ProcessDropDownAction extends Action implements IMenuCreator, ILaunchListener {

	private ConsoleView fView;
	private Menu fMenu;
	
	public ProcessDropDownAction(ConsoleView view) {
		fView= view;
		setText(ActionMessages.getString("ProcessDropDownAction.Select_Process_1")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("ProcessDropDownAction.Display_output_of_selected_process._2")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_OS_PROCESS));
		setMenuCreator(this);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}

	public void dispose() {
		if (fMenu != null)
			fMenu.dispose();
		
		fView= null;
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		if (fMenu != null)
			fMenu.dispose();
		
		fMenu= new Menu(parent);
		int mode = fView.getMode();
		boolean terminatedLaunches = false;
		IProcess[] processes = DebugPlugin.getDefault().getLaunchManager().getProcesses();
		IProcess current = fView.getProcess();
		for (int i = 0; i < processes.length; i++) {
			IProcess process = processes[i];
			Action action = new ShowProcessAction(fView, process);  
			action.setChecked(mode == ConsoleView.MODE_SPECIFIC_PROCESS && process.equals(current));
			addActionToMenu(fMenu, action);
			ILaunch launch = process.getLaunch();
			if (launch.isTerminated()) {
				terminatedLaunches = true;
			}
		}
		if (processes.length > 0) {
			addMenuSeparator();
		}
				
		Action action = new ShowCurrentProcessAction(fView);
		action.setChecked(mode == ConsoleView.MODE_CURRENT_PROCESS);
		addActionToMenu(fMenu, action);
		
		if (terminatedLaunches) {
			action = new ConsoleRemoveAllTerminatedAction();
			addActionToMenu(fMenu, action);
		}		
			
		return fMenu;
	}
	
	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	protected void addMenuSeparator() {
		new MenuItem(fMenu, SWT.SEPARATOR);		
	}

	public void run() {
		// do nothing - this is a menu
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}

	/* (non-Javadoc)
	 * 
	 * Dispose the menu when a launch is removed, such that the actions in this
	 * menu do not hang on to removed processes and associated resources.
	 * 
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
		Display display = DebugUIPlugin.getStandardDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				if (fMenu != null) {
					fMenu.dispose();
				}
			}
		});
	}

}
