/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;

import junit.framework.TestCase;

/**
 * Common function for launch related tests.
 */
public abstract class AbstractLaunchTest extends TestCase {

	/**
	 * Constructs a test with the given name.
	 * 
	 * @param name
	 */
	public AbstractLaunchTest(String name) {
		super(name);
	}

	/**
	 * Returns the launch manager.
	 * 
	 * @return launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}	
	
	/**
	 * Returns the singleton instance of the <code>LaunchConfigurationManager</code>
	 * 
	 * @return the singleton instance of the <code>LaunchConfigurationManager</code>
	 */
	protected LaunchConfigurationManager getLaunchConfigurationManager() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
	}	
	
	/**
	 * Returns a launch configuration with the given name, creating one if required.
	 * 
	 * @param name configuration name
	 * @return launch configuration
	 * @throws CoreException
	 */
	protected ILaunchConfiguration getLaunchConfiguration(String name) throws CoreException {
		ILaunchManager manager = getLaunchManager();
		ILaunchConfiguration[] configurations = manager.getLaunchConfigurations();
		for (int i = 0; i < configurations.length; i++) {
			ILaunchConfiguration config = configurations[i];
			if (config.getName().equals(name)) {
				return config;
			}
		}
		 ILaunchConfigurationType type = getLaunchManager().getLaunchConfigurationType(LaunchConfigurationTests.ID_TEST_LAUNCH_TYPE);
		 ILaunchConfigurationWorkingCopy wc = type.newInstance(null, name);
		 ILaunchConfiguration saved = wc.doSave();
		 return saved;
	}	
}
