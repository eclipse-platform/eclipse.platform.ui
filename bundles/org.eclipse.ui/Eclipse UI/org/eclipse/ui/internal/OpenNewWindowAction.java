package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * The <code>OpenNewWindowAction</code> is used to open a new
 * workbench window.
 */
public class OpenNewWindowAction  extends Action {
	private IWorkbenchWindow window;
/**
 * 
 */
public OpenNewWindowAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("OpenNewWindowAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("OpenNewWindowAction.toolTip")); //$NON-NLS-1$
	this.window = window;
}
/**
 * Open a new window in the default perspective.
 */
public void run() {
	try {
		IContainer element = ResourcesPlugin.getWorkspace().getRoot();
		IWorkbench wb = window.getWorkbench();
		wb.openWorkbenchWindow(element);
	} catch (WorkbenchException e) {
		MessageDialog.openError(window.getShell(), WorkbenchMessages.getString("OpenNewWindow.dialogTitle"), //$NON-NLS-1$
			e.getMessage());
	}
}
}
