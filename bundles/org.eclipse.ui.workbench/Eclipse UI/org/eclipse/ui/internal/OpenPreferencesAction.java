/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;

/**
 * Open the preferences dialog
 */
public class OpenPreferencesAction extends Action {
	protected IWorkbenchWindow window;
/**
 * Create a new <code>OpenPreferenceAction</code>
 * This default constructor allows the the action to be called from the welcome page.
 */
public OpenPreferencesAction() {
	this(((Workbench)PlatformUI.getWorkbench()).getActiveWorkbenchWindow());
}
/**
 * Create a new <code>OpenPreferenceAction</code> and initialize it 
 * from the given resource bundle.
 */
public OpenPreferencesAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("OpenPreferences.text")); //$NON-NLS-1$
	this.window = window;
	setToolTipText(WorkbenchMessages.getString("OpenPreferences.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.OPEN_PREFERENCES_ACTION);
}
/**
 * Perform the action: open the preference dialog.
 */
public void run() {
	PreferenceManager pm = WorkbenchPlugin.getDefault().getPreferenceManager();
	
	if (pm != null) {
		PreferenceDialog d = new WorkbenchPreferenceDialog(window.getShell(), pm);
		d.create();
		WorkbenchHelp.setHelp(d.getShell(), IHelpContextIds.PREFERENCE_DIALOG);
		d.open();	
	}
}
}
