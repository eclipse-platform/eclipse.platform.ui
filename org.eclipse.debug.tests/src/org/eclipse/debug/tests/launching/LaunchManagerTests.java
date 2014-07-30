/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.launching;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.LaunchManager;

/**
 *
 * Variety of tests for the {@link org.eclipse.debug.internal.core.LaunchManager}
 *
 * @since 3.6
 */
@SuppressWarnings("deprecation")
public class LaunchManagerTests extends AbstractLaunchTest {

	/**
	 * Constructor
	 * @param name
	 */
	public LaunchManagerTests(String name) {
		super(name);
	}

	/**
	 * Tests generating a valid launch configuration name
	 */
	public void testGenereateConfigName() {
		String configname = "launch_configuration"; //$NON-NLS-1$
		String name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertTrue("the name nust be '" + configname + "'", name.equals(configname)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Tests generating a launch configuration name with an unsupported char using
	 * the deprecated method
	 */
	public void testGenereateConfigNameBadChar() {
		String configname = "config:name"; //$NON-NLS-1$
		String name = getLaunchManager().generateUniqueLaunchConfigurationNameFrom(configname);
		assertEquals("config name should be '" + configname + "'", configname, name); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Tests that a valid name is accepted as is.
	 */
	public void testGenerateValidName() {
		String configname = "thisisavalidname"; //$NON-NLS-1$
		String name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertEquals("Should be the same as the seed", configname, name); //$NON-NLS-1$
	}

	/**
	 * Tests generating a launch configuration name using a name that is an OS reserved
	 * name. Win 32 test only.
	 */
	public void testGenerateConfigNameReservedName() {
		if(Platform.OS_WIN32.equals(Platform.getOS())) {
			String configname = "aux"; //$NON-NLS-1$
			String name = getLaunchManager().generateUniqueLaunchConfigurationNameFrom(configname);
			assertEquals("config name should be 'aux'", configname, name); //$NON-NLS-1$
		}
	}

	/**
	 * Tests generating a configuration name that contains an invalid character
	 */
	public void testGenerateBadConfigName() {
		String configname = "config:name"; //$NON-NLS-1$
		String name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertEquals("config name should be 'config_name'", "config_name", name); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Tests generating a name that conflicts with an OS reserved name. This test is for windows only as there
	 * are no reserved names on other OS's.
	 */
	public void testGenerateConflictingName() {
		if(Platform.OS_WIN32.equals(Platform.getOS())) {
			String configname = "aux"; //$NON-NLS-1$
			String name = getLaunchManager().generateLaunchConfigurationName(configname);
			assertEquals("config name should be 'launch_configuration'", "launch_configuration", name); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Tests generating a configuration name that contains an invalid character and where there
	 * is another config with the replaced name already
	 */
	public void testGenerateBadCharConflict() throws Exception {
		String configname = "config:name"; //$NON-NLS-1$
		String name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertEquals("config name should be 'config_name'", "config_name", name); //$NON-NLS-1$ //$NON-NLS-2$
		getLaunchConfiguration(name);
		name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertEquals("config name should be 'config_name (1)'", "config_name (1)", name); //$NON-NLS-1$ //$NON-NLS-2$
		ILaunchConfiguration config = getLaunchConfiguration("config_name"); //$NON-NLS-1$
		config.delete();
	}

	/**
	 * Tests generating a name that conflicts with an OS reserved name and that
	 * there is a config with the replaced name already. This test is for windows only as there
	 * are no reserved names on other OS's.
	 */
	public void testGenerateBadNameConflict() throws Exception {
		if(Platform.OS_WIN32.equals(Platform.getOS())) {
			String configname = "com2"; //$NON-NLS-1$
			String name = getLaunchManager().generateLaunchConfigurationName(configname);
			assertEquals("config name should be 'launch_configuration'", "launch_configuration", name); //$NON-NLS-1$ //$NON-NLS-2$
			getLaunchConfiguration(name);
			name = getLaunchManager().generateLaunchConfigurationName(configname);
			assertEquals("config name should be 'launch_configuration (1)'", "launch_configuration (1)", name); //$NON-NLS-1$ //$NON-NLS-2$
			ILaunchConfiguration config = getLaunchConfiguration("launch_configuration"); //$NON-NLS-1$
			config.delete();
		}
	}

	/**
	 * Tests the {@link org.eclipse.debug.core.ILaunchManager#isValidLaunchConfigurationName(String)} method for correctness
	 */
	public void testValidateConfigGoodName() {
		String configname = "configname"; //$NON-NLS-1$
		try {
			getLaunchManager().isValidLaunchConfigurationName(configname);
		}
		catch(IllegalArgumentException iae) {
			fail("the config name should not have thrown an exception during validation"); //$NON-NLS-1$
		}
	}

	/**
	 * Tests the {@link org.eclipse.debug.core.ILaunchManager#isValidLaunchConfigurationName(String)} method for correctness
	 */
	public void testValidateConfigBadCharName() {
		String configname = "config:name"; //$NON-NLS-1$
		try {
			getLaunchManager().isValidLaunchConfigurationName(configname);
		}
		catch(IllegalArgumentException iae) {
			return;
		}
		fail("the config name should have thrown an exception during validation"); //$NON-NLS-1$
	}

	/**
	 * Tests the {@link org.eclipse.debug.core.ILaunchManager#isValidLaunchConfigurationName(String)} method for correctness
	 */
	public void testValidateConfigBadName() {
		if(Platform.OS_WIN32.equals(Platform.getOS())) {
			String configname = "com1"; //$NON-NLS-1$
			try {
				getLaunchManager().isValidLaunchConfigurationName(configname);
			}
			catch(IllegalArgumentException iae) {
				return;
			}
			fail("the config name should have thrown an exception during validation"); //$NON-NLS-1$
		}
	}

	/**
	 * Tests that generating a configuration name when there exists a configuration with that name
	 * already properly updates a '(N)' counter at the end
	 */
	public void testGenerateNameExistingConfig() throws Exception {
		String configname = "x.y.z.configname"; //$NON-NLS-1$
		getLaunchConfiguration(configname);
		String name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertEquals("the configuration name should have been " + configname + " (1)", configname + " (1)", name); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		getLaunchConfiguration(name);
		name = getLaunchManager().generateLaunchConfigurationName(name);
		assertEquals("the configuration name should have been " + configname + " (2)", configname + " (2)", name); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ILaunchConfiguration config = getLaunchConfiguration(configname);
		if(config != null) {
			config.delete();
		}
		config = getLaunchConfiguration(configname + " (1)"); //$NON-NLS-1$
		if(config != null) {
			config.delete();
		}
	}

	/**
	 * Tests that removing an accelerator properly removes it without affecting the base string (readable) value
	 */
	public void testRemoveAcc() {
		String text = "&Remove"; //$NON-NLS-1$
		String label = LaunchManager.removeAccelerators(text);
		assertEquals("the label should be 'Remove'", "Remove", label); //$NON-NLS-1$ //$NON-NLS-2$
		text = "Remo&ve"; //$NON-NLS-1$
		label = LaunchManager.removeAccelerators(text);
		assertEquals("the label should be 'Remove'", "Remove", label); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
