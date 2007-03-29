/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.internal.ui.LaunchConfigurationTabExtension;
import org.eclipse.ui.IPluginContribution;

/**
 * This class provides a wrapper for a launch tab contribution so that it can be filtered from the UI 
 * via the use of capabilities
 * 
 * @since 3.3
 */
public class LaunchTabContribution implements IPluginContribution {

	LaunchConfigurationTabExtension fTab = null;
	
	public LaunchTabContribution(LaunchConfigurationTabExtension tab) {
		fTab = tab;
	}

	/**
	 * @see org.eclipse.ui.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return fTab.getIdentifier();
	}

	/**
	 * @see org.eclipse.ui.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return fTab.getPluginIdentifier();
	}

}
