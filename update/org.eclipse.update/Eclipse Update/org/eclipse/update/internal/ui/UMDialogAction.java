package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IConfigurationElement;import org.eclipse.core.runtime.IExecutableExtension;import org.eclipse.jface.action.IAction;import org.eclipse.jface.viewers.ISelection;import org.eclipse.ui.IWorkbenchWindow;import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 */
public class UMDialogAction implements IWorkbenchWindowActionDelegate, IExecutableExtension {
	private IWorkbenchWindow _window = null;
	public UMDialogAction() {

	}

	public void dispose() {
	}
	/**
	 * Initializes the action delegate with the workbench window it will work in.
	 */
	public void init(IWorkbenchWindow window) {
		_window = window;
	}
	/**
	 */
	public void run(IAction a) {
		UMDialog dialog = new UMDialog(_window.getShell());
		dialog.open();
	}
	/**
	 * Selection in the workbench has changed. Plugin provider
	 * can use it to change the availability of the action
	 * or to modify other presentation properties.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	/**
	 * Initializes the action with data from the xml declaration
	 */
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
	}
}