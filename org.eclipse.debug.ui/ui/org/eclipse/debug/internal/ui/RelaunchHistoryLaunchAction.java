package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunch;import org.eclipse.debug.core.ILaunchManager;import org.eclipse.debug.ui.IDebugUIConstants;import org.eclipse.jface.action.Action;import org.eclipse.jface.resource.ImageDescriptor;import org.eclipse.swt.custom.BusyIndicator;import org.eclipse.swt.widgets.Display;import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Re-launches a previous launch.
 */
public class RelaunchHistoryLaunchAction extends Action {

	protected static final DelegatingModelPresentation fgPresentation= new DelegatingModelPresentation();
	protected ILaunch fLaunch;
	protected String fMode;
	
	public RelaunchHistoryLaunchAction(ILaunch launch, String mode) {
		super();
		fLaunch= launch;
		fMode= mode;
		setText(fgPresentation.getLaunchText(launch));
		ImageDescriptor descriptor= null;
		if (launch.getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
			descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG);
		} else {
			descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN);
		}

		if (descriptor != null) {
			setImageDescriptor(descriptor);
		}
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.RELAUNCH_HISTORY_ACTION });
	}

	/**
	 * @see IAction
	 */
	public void run() {
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				RelaunchActionDelegate.relaunch(fLaunch, fMode);
			}
		});
	}
}
