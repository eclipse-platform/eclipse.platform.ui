/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.externaltools;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.ant.internal.ui.launchConfigurations.IAntLaunchConfigurationConstants;
import org.eclipse.ant.internal.ui.model.AntUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolMigration;

/**
 * Tests migration of Ant and External Tool configurations from old
 * formats to the current format.
 */
public class MigrationTests extends TestCase {
	
	/**
	 * Tests migration of arguments from an Eclipse 2.0 Ant buildfile
	 * configuration to a current launch configuration.
	 * 
	 * @throws CoreException
	 */
	public void test20AntMigration() throws CoreException {
		Map argumentMap= get20AntArgumentMap();
		ILaunchConfigurationWorkingCopy config = ExternalToolMigration.configFrom20ArgumentMap(argumentMap);
		assertEquals("Wrong configuration type", IAntLaunchConfigurationConstants.ID_ANT_BUILDER_LAUNCH_CONFIGURATION_TYPE, config.getType().getIdentifier());
		assertEquals("ant tool", config.getName());
		assertEquals("location", config.getAttribute(IExternalToolConstants.ATTR_LOCATION, ""));
		assertEquals("refresh scope", config.getAttribute(RefreshTab.ATTR_REFRESH_SCOPE, ""));
		String[] targets= AntUtil.getTargetsFromConfig(config);
		assertNotNull("No targets found", targets);
		assertEquals("Wrong number of targets", 2, targets.length);
		assertEquals("target1", targets[0]);
		assertEquals("target2", targets[1]);
		assertEquals(true, config.getAttribute(IExternalToolConstants.ATTR_CAPTURE_OUTPUT, false));
		assertEquals(true, config.getAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false));
		assertEquals("build kinds", config.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, ""));
		assertEquals("arg  ", config.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, ""));
		assertEquals("working dir", config.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, ""));
	}
	
	/**
	 * Returns a map of arguments for an Ant buildfile using
	 * Eclipse 2.0 arguments.
	 * 
	 * @return a map of 2.0 arguments for an Ant buildfile.
	 */
	private Map get20AntArgumentMap() {
		HashMap arguments= new HashMap();
		arguments.put(ExternalToolMigration.TAG_TOOL_TYPE, "org.eclipse.ui.externaltools.type.ant");
		arguments.put(ExternalToolMigration.TAG_TOOL_NAME, "ant tool");
		arguments.put(ExternalToolMigration.TAG_TOOL_LOCATION, "location");
		arguments.put(ExternalToolMigration.TAG_TOOL_REFRESH, "refresh scope");
		arguments.put(ExternalToolMigration.TAG_TOOL_ARGUMENTS, "arg ${ant_target:target1} ${ant_target:target2}");
		arguments.put(ExternalToolMigration.TAG_TOOL_SHOW_LOG, "true");
		arguments.put(ExternalToolMigration.TAG_TOOL_BLOCK, "false");
		arguments.put(ExternalToolMigration.TAG_TOOL_BUILD_TYPES, "build kinds");
		arguments.put(ExternalToolMigration.TAG_TOOL_DIRECTORY, "working dir");
		return arguments;
	}

	/**
	 * Tests migration of arguments from an Eclipse 2.0 Ant buildfile
	 * configuration to a current launch configuration.
	 * 
	 * @throws CoreException
	 */
	public void test20ProgramMigration() throws CoreException {
		Map argumentMap= get20ProgramArgumentMap();
		ILaunchConfigurationWorkingCopy config = ExternalToolMigration.configFrom20ArgumentMap(argumentMap);
		assertEquals("Wrong configuration type", IExternalToolConstants.ID_PROGRAM_BUILDER_LAUNCH_CONFIGURATION_TYPE, config.getType().getIdentifier());
		assertEquals("program tool", config.getName());
		assertEquals("location", config.getAttribute(IExternalToolConstants.ATTR_LOCATION, ""));
		assertEquals("refresh scope", config.getAttribute(RefreshTab.ATTR_REFRESH_SCOPE, ""));
		assertEquals(true, config.getAttribute(IExternalToolConstants.ATTR_CAPTURE_OUTPUT, false));
		assertEquals(true, config.getAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false));
		assertEquals("build kinds", config.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, ""));
		assertEquals("arg ${ant_target:target1} ${ant_target:target2}", config.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, ""));
		assertEquals("working dir", config.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, ""));
	}
	
	/**
	 * Returns a map of arguments for executing a program
	 * using Eclipse 2.0 arguments.
	 * 
	 * @return a map of 2.0 arguments for a program
	 */
	private Map get20ProgramArgumentMap() {
		HashMap arguments= new HashMap();
		arguments.put(ExternalToolMigration.TAG_TOOL_TYPE, "org.eclipse.ui.externaltools.type.program");
		arguments.put(ExternalToolMigration.TAG_TOOL_NAME, "program tool");
		arguments.put(ExternalToolMigration.TAG_TOOL_LOCATION, "location");
		arguments.put(ExternalToolMigration.TAG_TOOL_REFRESH, "refresh scope");
		arguments.put(ExternalToolMigration.TAG_TOOL_ARGUMENTS, "arg ${ant_target:target1} ${ant_target:target2}");
		arguments.put(ExternalToolMigration.TAG_TOOL_SHOW_LOG, "true");
		arguments.put(ExternalToolMigration.TAG_TOOL_BLOCK, "false");
		arguments.put(ExternalToolMigration.TAG_TOOL_BUILD_TYPES, "build kinds");
		arguments.put(ExternalToolMigration.TAG_TOOL_DIRECTORY, "working dir");
		return arguments;
	}
	
	public void test21Migration() {
	}
	
	/**
	 * Returns a map of arguments for executing a program
	 * using Eclipse 2.0 arguments.
	 * 
	 * @return a map of 2.0 arguments for a program
	 */
	private Map get21ProgramArgumentMap() {
		HashMap arguments= new HashMap();
		return arguments;
	}
	
}
