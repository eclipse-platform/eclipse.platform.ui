package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Date;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegateWithEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * This is the super class of the Run & Debug actions which appears in the desktop menu and toolbar.
 */
public abstract class ExecutionAction implements IActionDelegateWithEvent {
	
	/**
	 * @see IActionDelegateWithEvent#runWithEvent(IAction, Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		openLaunchConfigurationDialog();
	}

	/**
	 * Open the launch configuration dialog, passing in the current workbench selection.
	 */
	private void openLaunchConfigurationDialog() {
		IWorkbenchWindow dwindow= DebugUIPlugin.getActiveWorkbenchWindow();
		if (dwindow == null) {
			return;
		}
		IStructuredSelection selection= resolveSelection(dwindow);
		LaunchConfigurationDialog dialog = new LaunchConfigurationDialog(DebugUIPlugin.getShell(), selection, getMode());		
		dialog.setOpenMode(LaunchConfigurationDialog.LAUNCH_CONFIGURATION_DIALOG_LAUNCH_LAST);
		dialog.open();
	}
	
	/**
	 * Determines and returns the selection that provides context for the launch,
	 * or <code>null</code> if there is no selection.
	 */
	protected static IStructuredSelection resolveSelection(IWorkbenchWindow window) {
		if (window == null) {
			return null;
		}
		ISelection selection= window.getSelectionService().getSelection();
		if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			// there is no obvious selection - go fishing
			selection= null;
			IWorkbenchPage page= window.getActivePage();
			if (page == null) {
				//workspace is closed
				return null;
			}

			// first, see if there is an active editor, and try its input element
			IEditorPart editor= page.getActiveEditor();
			Object element= null;
			if (editor != null) {
				element= editor.getEditorInput();
			}

			if (selection == null && element != null) {
				selection= new StructuredSelection(element);
			}
		}
		return (IStructuredSelection)selection;
	}
	
	/**
	 * Returns the mode of a launcher to use for this action
	 */
	protected abstract String getMode();
	
}