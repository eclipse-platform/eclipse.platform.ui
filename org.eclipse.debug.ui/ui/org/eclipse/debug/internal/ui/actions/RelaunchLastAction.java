package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.LaunchHistoryElement;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Re-runs or re-debugs the last launch.
 */
public class RelaunchLastAction extends Action implements IWorkbenchWindowActionDelegate {
	
	/**
	 * When this action is created as a delegate, this flag indicates
	 * whether the workbench presentation has had its associated
	 * images set.
	 */
	private boolean fInitializedImages = false;
	
	public RelaunchLastAction() {
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.RELAUNCH_LAST_ACTION });
		setActionImages(this);
	}
	
	/**
	 * @see IAction
	 */
	public void run() {
		final LaunchHistoryElement recent= DebugUIPlugin.getDefault().getLastLaunch();
		if (recent == null) {
			Display.getCurrent().beep();
		} else {
			if (!DebugUIPlugin.saveAndBuild()) {
				return;
			}
			BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
				public void run() {
					RelaunchActionDelegate.relaunch(recent);
				}
			});
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
	 * @see IActionDelegate
	 */
	public void selectionChanged(IAction action, ISelection selection){
		setActionImages(action);
	}
	
	protected void setActionImages(IAction action) {
		if (!fInitializedImages ) {
			action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_RELAUNCH));
			action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_RELAUNCH));
			action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_RELAUNCH));
			// only set the flag to true when setting images
			// for the associated workbench presentation
			fInitializedImages = action != this;
		} 
	}
}

