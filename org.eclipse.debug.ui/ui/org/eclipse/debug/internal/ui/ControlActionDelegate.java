package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;import org.eclipse.core.runtime.MultiStatus;import org.eclipse.debug.core.*;import org.eclipse.jface.action.IAction;import org.eclipse.jface.viewers.*;import org.eclipse.swt.custom.BusyIndicator;import org.eclipse.swt.widgets.Display;import org.eclipse.ui.IWorkbenchWindow;import org.eclipse.ui.IWorkbenchWindowActionDelegate;import org.eclipse.ui.actions.SelectionProviderAction;

public abstract class ControlActionDelegate implements IWorkbenchWindowActionDelegate {

	protected static final String ERROR= "error.";
	protected static final String STATUS= "status";
	
	protected String fMode= ILaunchManager.DEBUG_MODE;

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
		LaunchesViewer provider= (LaunchesViewer)controlAction.getSelectionProvider();
		IContentProvider contentProvider= provider.getContentProvider();
		fMode= ILaunchManager.DEBUG_MODE;
		if (contentProvider instanceof ProcessesContentProvider) {
			fMode= ILaunchManager.RUN_MODE;
		}	
	}
	
	/**
	 * Do the specific action using the current selection.
	 */
	public void run() {
		LaunchesView view= getLaunchesView(fMode);
		if (view == null) {
			return;
		}
		IStructuredSelection selection= (IStructuredSelection)view.getSite().getSelectionProvider().getSelection();
		
		final Iterator enum= selection.iterator();
		String pluginId= DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier();
		final MultiStatus ms= 
			new MultiStatus(pluginId, IDebugStatusConstants.REQUEST_FAILED, DebugUIUtils.getResourceString(getPrefix() + STATUS), null); 
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
			DebugUIUtils.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), getPrefix() + ERROR, ms);
		}		
	}

	/**
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void dispose(){
	}

	/**
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void init(IWorkbenchWindow window){
	}

	/**
	 * @see IActionDelegate
	 */
	public void run(IAction action){
		run();
	}

	/**
	 * Only interested in selection changes in the launches view
	 * @see IActionDelegate
	 */
	public void selectionChanged(IAction action, ISelection s) {
		LaunchesView view= getLaunchesView(fMode);
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
			if (count > 1 && enableForMultiSelection()) {
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

	/**
	 * Does the specific action of this action to the process.
	 */
	protected abstract void doAction(Object element) throws DebugException;

	/**
	 * Returns the resource bundle prefix for this action
	 */
	protected abstract String getPrefix();

	/**
	 * Returns whether this action will work for the given element
	 */
	public abstract boolean isEnabledFor(Object element);
	
	/**
	 * Returns this action's help context id
	 */
	protected abstract String getHelpContextId();
	
}