package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.ILaunchManager;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Opens the launch config dialog in run mode.
 */
public class OpenRunConfigurations extends OpenLaunchConfigurationsAction {

	/**
	 * Creates a new action to open the launch configurations
	 * dialog in debug mode.
	 */
	public OpenRunConfigurations() {
		super("R&un Configurations...");
	}
	
	/**
	 * @see OpenLaunchConfigurationsAction#getMode()
	 */
	protected String getMode() {
		return ILaunchManager.RUN_MODE;
	}

}
