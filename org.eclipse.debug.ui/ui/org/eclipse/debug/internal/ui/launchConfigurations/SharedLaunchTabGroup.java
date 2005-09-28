/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * A tab group for the general shared tab for all launch configurations
 *
 *	@since 3.2
 */
public class SharedLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {

	/**
	 * The LaunchConfigType that was selected to show this tab
	 */
	private ILaunchConfigurationType fType;
	
	/**
	 * Default constructor
	 */
	public SharedLaunchTabGroup(ILaunchConfigurationType type) {
		fType = type;
	}//end constructor

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {new SharedLaunchTab(fType)}; 
		setTabs(tabs);
	}//end createTabs

}//end class
