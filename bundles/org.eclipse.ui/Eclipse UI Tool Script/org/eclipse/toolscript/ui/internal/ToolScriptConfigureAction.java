package org.eclipse.toolscript.ui.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * This action will display the tool script configuration dialog.
 * In addition, as a tool bar item, it's drop down list will include
 * tool scripts to run directly.
 */
public class ToolScriptConfigureAction extends ActionDelegate implements IWorkbenchWindowPulldownDelegate {
	private IWorkbenchWindow window;
	
	/**
	 * Creates the tool script configure action
	 */
	public ToolScriptConfigureAction() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		if (!action.isEnabled())
			return;
		ToolScriptConfigurationDialog dialog;
		dialog = new ToolScriptConfigurationDialog(window.getShell());
		dialog.open();
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowPulldownDelegate.
	 */
	public Menu getMenu(Control parent) {
		return null;
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowActionDelegate.
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowActionDelegate.
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
