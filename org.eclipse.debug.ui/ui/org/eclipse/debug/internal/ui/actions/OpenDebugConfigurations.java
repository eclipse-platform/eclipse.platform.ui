package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Opens the launch config dialog in debug mode.
 */
public class OpenDebugConfigurations extends OpenLaunchConfigurationsAction {

	public OpenDebugConfigurations() {
		super();
	}

	public OpenDebugConfigurations(ILaunchConfigurationType configType) {
		super(configType);
	}
	
	/**
	 * @see OpenLaunchConfigurationsAction#getMode()
	 */
	protected String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}

}
