/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.ui.IPluginContribution;

/**
 * A plug-in contribution (UI element) which contains a launch configuration
 * type (Core element). Plug-in contributions are passed to the workbench
 * activity support to filter elements from the UI.
 */
class LaunchConfigurationTypeContribution implements IPluginContribution {
	
	protected ILaunchConfigurationType type;
	
	/**
	 * Creates a new plug-in contribution for the given type
	 * 
	 * @param type the launch configuration type
	 */
	public LaunchConfigurationTypeContribution(ILaunchConfigurationType type) {
		this.type= type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return type.getIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return type.getPluginIdentifier();
	}
	
}
