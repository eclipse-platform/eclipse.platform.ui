package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugStatusConstants;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class ControlActionDelegate implements IWorkbenchWindowActionDelegate {
	
	private String fMode= ILaunchManager.DEBUG_MODE;

	/**
	 * It's crucial that delegate actions have a zero-arg constructor so that
	 * they can be reflected into existence when referenced in an action set
	 * in the plugin's plugin.xml file.
	 */
	public ControlActionDelegate() {
	}
	
	/**
	 * Not all ControlActionDelegates have an owner, only those that aren't
	 * specified as part of an action set in plugin.xml.  For those delegates,
	 * that do have a ControlAction owner, this is the place to do any
	 * action specific initialization.
	 */
	public void initializeForOwner(ControlAction controlAction) {
		setActionImages(controlAction);
		LaunchesViewer provider= (LaunchesViewer)controlAction.getSelectionProvider();
		IContentProvider contentProvider= provider.getContentProvider();
		setMode(ILaunchManager.DEBUG_MODE);
		if (contentProvider instanceof ProcessesContentProvider) {
			setMode(ILaunchManager.RUN_MODE);
		}	
	}
	
	/**
	 * Do the specific action using the current selection.
	 */
	public void run() {
		LaunchesView view= getLaunchesView(getMode());
		if (view == null) {
			return;
		}
		IStructuredSelection selection= (IStructuredSelection)view.getSite().getSelectionProvider().getSelection();
		
		final Iterator enum= selection.iterator();
		String pluginId= DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier();
		final MultiStatus ms= 
			new MultiStatus(pluginId, IDebugStatusConstants.REQUEST_FAILED, getStatusMessage(), null); 
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				while (enum.hasNext()) {
					Object element= enum.next();
					if (isEnabledFor(element)) {
						try {
							doAction(element);
						} catch (DebugException e) {
							ms.merge(e.getStatus());
						}
					}
				}
			}
		});
		if (!ms.isOK()) {
			DebugUIPlugin.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), getErrorDialogTitle(), getErrorDialogMessage(), ms);
		}		
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose(){
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action){
		run();
	}

	/**
	 * Only interested in selection changes in the launches view.
	 * Set the icons for this action on the first selection changed
	 * event.  This is necessary because the XML currently only
	 * supports setting the enabled icon.  
	 * 
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection s) {
		LaunchesView view= getLaunchesView(getMode());
		if (view == null) {
			action.setEnabled(false);
			return;
		}
		IStructuredSelection selection= (IStructuredSelection)view.getSite().getSelectionProvider().getSelection();
		action.setEnabled(getEnableStateForSelection(selection));
	}
	
	/**
	 * Return whether the action should be enabled or not based on the given selection.
	 */
	public boolean getEnableStateForSelection(IStructuredSelection selection) {
		if (selection.size() == 0) {
			return false;
		}
		Iterator enum= selection.iterator();
		int count= 0;
		while (enum.hasNext()) {
			count++;
			if (count > 1 && !enableForMultiSelection()) {
				return false;
			}
			Object element= enum.next();
			if (!isEnabledFor(element)) {
				return false;
			}
		}
		return true;		
	}
	
	/**
	 * Returns whether this action should be enabled if there is
	 * multi selection.
	 */
	protected boolean enableForMultiSelection() {
		return true;
	}
	
	protected LaunchesView getLaunchesView(String mode) {
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		return
			DebugUIPlugin.getDefault().findDebugPart(window, mode);
	}
	
	protected String getMode() {
		return fMode;
	}

	protected void setMode(String mode) {
		fMode = mode;
	}
	
	/**
	 * Does the specific action of this action to the process.
	 */
	protected abstract void doAction(Object element) throws DebugException;

	/**
	 * Returns whether this action will work for the given element
	 */
	public abstract boolean isEnabledFor(Object element);
	
	/**
	 * Returns this action's help context id
	 */
	protected abstract String getHelpContextId();
	
	/**
	 * Set the enabled, disabled & hover icons for this action delegate
	 */
	protected abstract void setActionImages(IAction action);
	
	/**
	 * Returns the String to use as an error dialog title for
	 * a failed action.
	 */
	protected abstract String getErrorDialogTitle();
	
	/**
	 * Returns the String to use as an error dialog message for
	 * a failed action.
	 */
	protected abstract String getErrorDialogMessage();
	
	/**
	 * Returns the String to use as a status message for
	 * a failed action.
	 */
	protected abstract String getStatusMessage();
	
	/**
	 * Returns the text for this action.
	 */
	protected abstract String getText();
	
	/**
	 * Returns the tool tip text for this action.
	 */
	protected abstract String getToolTipText();
}