package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. 
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.WorkbenchEditorsDialog;

/**
 * Implements a action to open a dialog showing all open editors
 * and the recent closed editors.
 */
public class WorkbenchEditorsAction extends Action {

	WorkbenchWindow window;

	/**
	 * Constructor for NavigateWorkbenchAction.
	 * @param text
	 */
	public WorkbenchEditorsAction(WorkbenchWindow window) {
		super(WorkbenchMessages.getString("WorkbenchEditorsAction.label")); //$NON-NLS-1$
		setAccelerator(SWT.CTRL | SWT.SHIFT | 'W');
		this.window = window;
		WorkbenchHelp.setHelp(this, IHelpContextIds.WORKBENCH_EDITORS_ACTION);
	}
	public void run() {
		new WorkbenchEditorsDialog(window).open();
	}
}