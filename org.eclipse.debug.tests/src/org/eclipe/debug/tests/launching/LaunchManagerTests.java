/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipe.debug.tests.launching;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.LaunchManager;

/**
 * 
 * Variety of tests for the {@link org.eclipse.debug.internal.core.LaunchManager}
 * 
 * @since 3.6
 */
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
		String configname = "launch_configuration";
		String name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertTrue("the name nust be '"+configname+"'", name.equals(configname));
	}
	
	/**
	 * Tests generating a launch configuration name with an unsupported char using
	 * the deprecated method
	 */
	public void testGenereateConfigNameBadChar() {
		String configname = "config:name";
		String name = getLaunchManager().generateUniqueLaunchConfigurationNameFrom(configname);
		assertEquals("config name should be '"+configname+"'", configname, name);
	}
	
	/**
	 * Tests that a valid name is accepted as is.
	 */
	public void testGenerateValidName() {
		String configname = "thisisavalidname";
		String name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertEquals("Should be the same as the seed", configname, name);
	}
	
	/**
	 * Tests generating a launch configuration name using a name that is an OS reserved
	 * name. Win 32 test only.
	 */
	public void testGenerateConfigNameReservedName() {
		if(Platform.OS_WIN32.equals(Platform.getOS())) {
			String configname = "aux";
			String name = getLaunchManager().generateUniqueLaunchConfigurationNameFrom(configname);
			assertEquals("config name should be 'aux'", configname, name);
		}
	}
	
	/**
	 * Tests generating a configuration name that contains an invalid character
	 */
	public void testGenerateBadConfigName() {
		String configname = "config:name";
		String name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertEquals("config name should be 'config_name'", "config_name", name);
	}
	
	/**
	 * Tests generating a name that conflicts with an OS reserved name. This test is for windows only as there 
	 * are no reserved names on other OS's.
	 */
	public void testGenerateConflictingName() {
		if(Platform.OS_WIN32.equals(Platform.getOS())) {
			String configname = "aux";
			String name = getLaunchManager().generateLaunchConfigurationName(configname);
			assertEquals("config name should be 'launch_configuration'", "launch_configuration", name);
		}
	}
	
	/**
	 * Tests generating a configuration name that contains an invalid character and where there
	 * is another config with the replaced name already
	 */
	public void testGenerateBadCharConflict() throws Exception {
		String configname = "config:name";
		String name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertEquals("config name should be 'config_name'", "config_name", name);
		getLaunchConfiguration(name);
		name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertEquals("config name should be 'config_name (1)'", "config_name (1)", name);
		ILaunchConfiguration config = getLaunchConfiguration("config_name");
		config.delete();
	}
	
	/**
	 * Tests generating a name that conflicts with an OS reserved name and that 
	 * there is a config with the replaced name already. This test is for windows only as there 
	 * are no reserved names on other OS's.
	 */
	public void testGenerateBadNameConflict() throws Exception {
		if(Platform.OS_WIN32.equals(Platform.getOS())) {
			String configname = "com2";
			String name = getLaunchManager().generateLaunchConfigurationName(configname);
			assertEquals("config name should be 'launch_configuration'", "launch_configuration", name);
			getLaunchConfiguration(name);
			name = getLaunchManager().generateLaunchConfigurationName(configname);
			assertEquals("config name should be 'launch_configuration (1)'", "launch_configuration (1)", name);
			ILaunchConfiguration config = getLaunchConfiguration("launch_configuration");
			config.delete();
		}
	}
		
	/**
	 * Tests the {@link org.eclipse.debug.core.ILaunchManager#isValidLaunchConfigurationName(String)} method for correctness
	 */
	public void testValidateConfigGoodName() {
		String configname = "configname";
		try {
			getLaunchManager().isValidLaunchConfigurationName(configname);
		}
		catch(IllegalArgumentException iae) {
			fail("the config name should not have thrown an exception during validation");
		}
	}
	
	/**
	 * Tests the {@link org.eclipse.debug.core.ILaunchManager#isValidLaunchConfigurationName(String)} method for correctness
	 */
	public void testValidateConfigBadCharName() {
		String configname = "config:name";
		try {
			getLaunchManager().isValidLaunchConfigurationName(configname);
		}
		catch(IllegalArgumentException iae) {
			return;
		}
		fail("the config name should have thrown an exception during validation");
	}
	
	/**
	 * Tests the {@link org.eclipse.debug.core.ILaunchManager#isValidLaunchConfigurationName(String)} method for correctness
	 */
	public void testValidateConfigBadName() {
		if(Platform.OS_WIN32.equals(Platform.getOS())) {
			String configname = "com1";
			try {
				getLaunchManager().isValidLaunchConfigurationName(configname);
			}
			catch(IllegalArgumentException iae) {
				return;
			}
			fail("the config name should have thrown an exception during validation");
		}
	}
	
	/**
	 * Tests that generating a configuration name when there exists a configuration with that name
	 * already properly updates a '(N)' counter at the end
	 */
	public void testGenerateNameExistingConfig() throws Exception {
		String configname = "x.y.z.configname";
		getLaunchConfiguration(configname);
		String name = getLaunchManager().generateLaunchConfigurationName(configname);
		assertEquals("the configuration name should have been "+configname+" (1)", configname+" (1)", name);
		getLaunchConfiguration(name);
		name = getLaunchManager().generateLaunchConfigurationName(name);
		assertEquals("the configuration name should have been "+configname+" (2)", configname+" (2)", name);
		ILaunchConfiguration config = getLaunchConfiguration(configname);
		if(config != null) {
			config.delete();
		}
		config = getLaunchConfiguration(configname +" (1)");
		if(config != null) {
			config.delete();
		}
	}
	
	/**
	 * Tests that removing an accelerator properly removes it without affecting the base string (readable) value
	 */
	public void testRemoveAcc() {
		String text = "&Remove";
		String label = LaunchManager.removeAccelerators(text);
		assertEquals("the label should be 'Remove'", "Remove", label);
		text = "Remo&ve";
		label = LaunchManager.removeAccelerators(text);
		assertEquals("the label should be 'Remove'", "Remove", label);
	}
}
