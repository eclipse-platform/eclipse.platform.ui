package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.help.*;

/**
 * Open the preferences dialog
 */
public class OpenPreferencesAction extends Action {
	protected IWorkbenchWindow window;
/**
 * Create a new <code>OpenPreferenceAction</code> and initialize it 
 * from the given resource bundle.
 */
public OpenPreferencesAction(IWorkbenchWindow window) {
	super("&Preferences");
	this.window = window;
	setToolTipText("Open the preferences dialog");
}
/**
 * Perform the action: open the preference dialog.
 */
public void run() {
	PreferenceManager pm = WorkbenchPlugin.getDefault().getPreferenceManager();
	
	if (pm != null) {
		PreferenceDialog d = new PreferenceDialog(window.getShell(), pm);
		d.create();
		WorkbenchHelp.setHelp(d.getShell(), new Object[]{IHelpContextIds.PREFERENCE_DIALOG});
		d.open();	
	}
}
}
