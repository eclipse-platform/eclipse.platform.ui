/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.externaltools;

import java.util.Map;

import org.eclipse.ant.internal.launching.AntLaunchingUtil;
import org.eclipse.ant.launching.IAntLaunchConstants;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.externaltools.internal.registry.ExternalToolMigration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.RefreshTab;

/**
 * Tests migration of Ant and External Tool configurations from old
 * formats to the current format.
 */
public class MigrationTests extends AbstractExternalToolTest {
	
	/**
	 * Constructor
	 */
	public MigrationTests() {
		super("Migration Tests");
	}

	/**
	 * Tests migration of arguments from an Eclipse 2.0 Ant buildfile
	 * configuration to a current launch configuration.
	 * 
	 * @throws CoreException
	 */
	public void test20AntMigration() throws CoreException {
		Map argumentMap= get20AntArgumentMap();
		ILaunchConfigurationWorkingCopy config = ExternalToolMigration.configFromArgumentMap(argumentMap);
		assertNotNull("Migration failed", config);
		
		assertEquals("Wrong configuration type", IAntLaunchConstants.ID_ANT_BUILDER_LAUNCH_CONFIGURATION_TYPE, config.getType().getIdentifier());
		assertEquals("ant tool", config.getName());
		assertEquals("location", config.getAttribute(IExternalToolConstants.ATTR_LOCATION, ""));
		assertEquals("refresh scope", config.getAttribute(RefreshTab.ATTR_REFRESH_SCOPE, ""));
		String[] targets= AntLaunchingUtil.getTargetNames(config);
		assertNotNull("No targets found", targets);
		assertEquals("Wrong number of targets", 2, targets.length);
		assertEquals("target1", targets[0]);
		assertEquals("target2", targets[1]);
		assertEquals(true, config.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false));
		assertEquals(true, config.getAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false));
		assertEquals("build kinds", config.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, ""));
		assertEquals("arg  ", config.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, ""));
		assertEquals("working dir", config.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, ""));
	}
	
	/**
	 * Tests migration of arguments from an Eclipse 2.0 Ant buildfile
	 * configuration to a current launch configuration.
	 * 
	 * @throws CoreException
	 */
	public void test20ProgramMigration() throws CoreException {
		Map argumentMap= get20ProgramArgumentMap();
		ILaunchConfigurationWorkingCopy config = ExternalToolMigration.configFromArgumentMap(argumentMap);
		assertEquals("Wrong configuration type", IExternalToolConstants.ID_PROGRAM_BUILDER_LAUNCH_CONFIGURATION_TYPE, config.getType().getIdentifier());
		assertEquals("program tool", config.getName());
		assertEquals("location", config.getAttribute(IExternalToolConstants.ATTR_LOCATION, ""));
		assertEquals("refresh scope", config.getAttribute(RefreshTab.ATTR_REFRESH_SCOPE, ""));
		assertEquals(true, config.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false));
		assertEquals(true, config.getAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false));
		assertEquals("build kinds", config.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, ""));
		assertEquals("arg ${ant_target:target1} ${ant_target:target2}", config.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, ""));
		assertEquals("working dir", config.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, ""));
	}
	
	/**
	 * Tests migration of arguments from an Eclipse 2.1 Ant buildfile
	 * configuration to a current launch configuration.
	 * 
	 * @throws CoreException
	 */
	public void test21AntMigration() throws CoreException {
		Map argumentMap= get21AntArgumentMap();
		ILaunchConfigurationWorkingCopy config = ExternalToolMigration.configFromArgumentMap(argumentMap);
		assertNotNull("Migration failed", config);
		
		assertEquals("Wrong config type", IAntLaunchConstants.ID_ANT_BUILDER_LAUNCH_CONFIGURATION_TYPE, config.getType().getIdentifier());
		assertEquals("ant config", config.getName());
		assertEquals("location", config.getAttribute(IExternalToolConstants.ATTR_LOCATION, ""));
		assertEquals("working directory", config.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, ""));
		assertEquals(true, config.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false));
		assertEquals(true, config.getAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, false));
		assertEquals(true, config.getAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false));
		assertEquals(true, config.getAttribute(IExternalToolConstants.ATTR_PROMPT_FOR_ARGUMENTS, false));
		assertEquals("refresh scope", config.getAttribute(RefreshTab.ATTR_REFRESH_SCOPE, ""));
		assertEquals(true, config.getAttribute(RefreshTab.ATTR_REFRESH_RECURSIVE, false));
		assertEquals("build kinds", config.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, ""));
		assertEquals("arg1 arg2", config.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, ""));
		String[] targets= AntLaunchingUtil.getTargetNames(config);
		assertEquals("Wrong number of targets", 2, targets.length);
		assertEquals("target1", targets[0]);
		assertEquals("target2", targets[1]);
	}
	
	/**
	 * Tests migration of arguments from an Eclipse 2.1 program
	 * configuration to a current launch configuration.
	 * 
	 * @throws CoreException
	 */
	public void test21ProgramMigration() throws CoreException {
		Map argumentMap= get21ProgramArgumentMap();
		ILaunchConfigurationWorkingCopy config = ExternalToolMigration.configFromArgumentMap(argumentMap);
		assertNotNull("Migration failed", config);
		
		assertEquals("Wrong config type", IExternalToolConstants.ID_PROGRAM_BUILDER_LAUNCH_CONFIGURATION_TYPE, config.getType().getIdentifier());
		assertEquals("program config", config.getName());
		assertEquals("location", config.getAttribute(IExternalToolConstants.ATTR_LOCATION, ""));
		assertEquals("working directory", config.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, ""));
		assertEquals(true, config.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false));
		assertEquals(true, config.getAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, false));
		assertEquals(true, config.getAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false));
		assertEquals(true, config.getAttribute(IExternalToolConstants.ATTR_PROMPT_FOR_ARGUMENTS, false));
		assertEquals("refresh scope", config.getAttribute(RefreshTab.ATTR_REFRESH_SCOPE, ""));
		assertEquals(true, config.getAttribute(RefreshTab.ATTR_REFRESH_RECURSIVE, false));
		assertEquals("build kinds", config.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, ""));
		assertEquals("arg1 arg2", config.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, ""));
	}
}
