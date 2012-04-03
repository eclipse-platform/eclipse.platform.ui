/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.tests.ui.externaltools;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.ant.launching.IAntLaunchConstants;
import org.eclipse.ant.tests.ui.testplugin.AbstractAntUITest;
import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.externaltools.internal.model.BuilderCoreUtils;
import org.eclipse.core.externaltools.internal.registry.ExternalToolMigration;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ui.externaltools.internal.model.BuilderUtils;

/**
 * Abstract {@link Test} class for external tools
 * 
 * @since 3.5.100 org.eclipse.ant.tests.ui
 */
public abstract class AbstractExternalToolTest extends AbstractAntUITest {

	static final String EXT_BUILD_FILE_NAME = "ext-builders.xml";
	
	/**
	 * Constructor
	 * @param name
	 */
	public AbstractExternalToolTest(String name) {
		super(name);
	}
	
	/**
	 * Creates a new external tool builder for the given project from the given {@link ILaunchConfiguration}
	 * 
	 * @param project the parent project
	 * @param name the name of the config
	 * @param args the argument map to set in the new configuration
	 * @return a new Ant build {@link ILaunchConfiguration} or <code>null</code>
	 * @throws Exception
	 */
	protected ILaunchConfiguration createExternalToolBuilder(IProject project, String name, Map args) throws Exception {
		IFolder dir = getProject().getFolder(BuilderCoreUtils.BUILDER_FOLDER_NAME);
		if(!dir.exists()) {
			dir.create(true, true, null);
		}
		ILaunchConfigurationType type = AbstractAntUITest.getLaunchManager().getLaunchConfigurationType(IAntLaunchConstants.ID_ANT_BUILDER_LAUNCH_CONFIGURATION_TYPE);
		if(type != null) {
			ILaunchConfigurationWorkingCopy config = type.newInstance(dir, name);
			config.setAttributes(args);
			return config.doSave();
		}
		return null;
	}
	
	/**
	 * Creates a new external tool Ant build configuration that has never been saved
	 * @param project
	 * @param name
	 * @param args
	 * @return
	 * @throws Exception
	 */
	protected ILaunchConfigurationWorkingCopy createExternalToolBuilderWorkingCopy(IProject project, String name, Map args) throws Exception {
		IFolder dir = getProject().getFolder(BuilderCoreUtils.BUILDER_FOLDER_NAME);
		if(!dir.exists()) {
			dir.create(true, true, null);
		}
		ILaunchConfigurationType type = AbstractAntUITest.getLaunchManager().getLaunchConfigurationType(IAntLaunchConstants.ID_ANT_BUILDER_LAUNCH_CONFIGURATION_TYPE);
		if(type != null) {
			ILaunchConfigurationWorkingCopy config = type.newInstance(dir, name);
			config.setAttributes(args);
			return config;
		}
		return null;
	}
	
	/**
	 * Creates a new empty {@link ICommand}
	 * @return the new build {@link ICommand}
	 * @throws Exception
	 */
	protected ICommand createEmptyBuildCommand() throws Exception {
		return getProject().getDescription().newCommand();
	}
	
	/**
	 * Creates a new builder {@link ICommand}
	 * @param config
	 * @return the new builder {@link ICommand}
	 * @throws Exception
	 */
	protected ICommand createBuildCommand(ILaunchConfiguration config) throws Exception {
		return BuilderUtils.commandFromLaunchConfig(getProject(), config);
	}
	
	/**
	 * Returns a map of arguments for an Ant buildfile using
	 * Eclipse 2.0 arguments.
	 * 
	 * @return a map of 2.0 arguments for an Ant buildfile.
	 */
	protected Map get20AntArgumentMap() {
		HashMap arguments= new HashMap();
		arguments.put(ExternalToolMigration.TAG_VERSION, "2.0");
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
	 * Returns a map of arguments for executing a program
	 * using Eclipse 2.0 arguments.
	 * 
	 * @return a map of 2.0 arguments for a program
	 */
	protected Map get20ProgramArgumentMap() {
		HashMap arguments= new HashMap();
		arguments.put(ExternalToolMigration.TAG_VERSION, "2.0");
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
	
	/**
	 * Returns a map of arguments for executing an Ant
	 * buildfile using Eclipse 2.1 arguments.
	 * 
	 * @return a map of 2.1 arguments for an Ant buildfile
	 */
	protected Map get21AntArgumentMap() {
		HashMap arguments= new HashMap();
		arguments.put(ExternalToolMigration.TAG_VERSION, "2.1");
		arguments.put(ExternalToolMigration.TAG_NAME, "ant config");
		arguments.put(ExternalToolMigration.TAG_TYPE, ExternalToolMigration.TOOL_TYPE_ANT_BUILD);
		arguments.put(ExternalToolMigration.TAG_LOCATION, "location");
		arguments.put(ExternalToolMigration.TAG_WORK_DIR, "working directory");
		arguments.put(ExternalToolMigration.TAG_CAPTURE_OUTPUT, "true");
		arguments.put(ExternalToolMigration.TAG_SHOW_CONSOLE, "true");
		arguments.put(ExternalToolMigration.TAG_SHOW_CONSOLE, "true");
		arguments.put(ExternalToolMigration.TAG_RUN_BKGRND, "true");
		arguments.put(ExternalToolMigration.TAG_PROMPT_ARGS, "true");
		arguments.put(ExternalToolMigration.TAG_REFRESH_SCOPE, "refresh scope");
		arguments.put(ExternalToolMigration.TAG_REFRESH_RECURSIVE, "true");
		arguments.put(ExternalToolMigration.TAG_RUN_BUILD_KINDS, "build kinds");
		arguments.put(ExternalToolMigration.TAG_ARGS, "arg1 arg2");
		arguments.put(ExternalToolMigration.TAG_EXTRA_ATTR, ExternalToolMigration.RUN_TARGETS_ATTRIBUTE + "=target1,target2");
		return arguments;
	}
	
	/**
	 * Returns a map of arguments for executing a program
	 * buildfile using Eclipse 2.1 arguments.
	 * 
	 * @return a map of 2.1 arguments for a program
	 */
	protected Map get21ProgramArgumentMap() {
		HashMap arguments= new HashMap();
		arguments.put(ExternalToolMigration.TAG_VERSION, "2.1");
		arguments.put(ExternalToolMigration.TAG_NAME, "program config");
		arguments.put(ExternalToolMigration.TAG_TYPE, IExternalToolConstants.TOOL_TYPE_PROGRAM);
		arguments.put(ExternalToolMigration.TAG_LOCATION, "location");
		arguments.put(ExternalToolMigration.TAG_WORK_DIR, "working directory");
		arguments.put(ExternalToolMigration.TAG_CAPTURE_OUTPUT, "true");
		arguments.put(ExternalToolMigration.TAG_SHOW_CONSOLE, "true");
		arguments.put(ExternalToolMigration.TAG_SHOW_CONSOLE, "true");
		arguments.put(ExternalToolMigration.TAG_RUN_BKGRND, "true");
		arguments.put(ExternalToolMigration.TAG_PROMPT_ARGS, "true");
		arguments.put(ExternalToolMigration.TAG_REFRESH_SCOPE, "refresh scope");
		arguments.put(ExternalToolMigration.TAG_REFRESH_RECURSIVE, "true");
		arguments.put(ExternalToolMigration.TAG_RUN_BUILD_KINDS, "build kinds");
		arguments.put(ExternalToolMigration.TAG_ARGS, "arg1 arg2");
		return arguments;
	}
}
