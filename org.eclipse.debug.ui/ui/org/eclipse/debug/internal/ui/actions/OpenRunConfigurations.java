package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Opens the launch config dialog in run mode.
 */
public class OpenRunConfigurations extends OpenLaunchConfigurationsAction {

	public OpenRunConfigurations(ILaunchConfigurationType configType) {
		super(configType);
	}

	/**
	 * @see OpenLaunchConfigurationsAction#getMode()
	 */
	protected String getMode() {
		return ILaunchManager.RUN_MODE;
	}

}
