/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import java.util.Map;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * This class does not define any tests, just convenience methods for other builder tests.
 */
public abstract class AbstractBuilderTest extends ResourceTest {

	private boolean autoBuilding;

	public AbstractBuilderTest(String name) {
		super(name);
	}

	/**
	 * Adds a new delta verifier builder to the given project.
	 */
	protected void addBuilder(IProject project, String builderName) throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setBuildSpec(new ICommand[] {createCommand(desc, builderName, "Project1Build1")});
		project.setDescription(desc, getMonitor());
	}

	/**
	 * Creates and returns a new command with the SortBuilder, and the TestBuilder.BUILD_ID 
	 * parameter set to the given value.
	 */
	protected ICommand createCommand(IProjectDescription description, String buildID) {
		return createCommand(description, SortBuilder.BUILDER_NAME, buildID);
	}

	/**
	 * Creates and returns a new command with the given builder name, and the TestBuilder.BUILD_ID 
	 * parameter set to the given value.
	 */
	protected ICommand createCommand(IProjectDescription description, String builderName, String buildID) {
		ICommand command = description.newCommand();
		Map args = command.getArguments();
		args.put(TestBuilder.BUILD_ID, buildID);
		command.setBuilderName(builderName);
		command.setArguments(args);
		return command;
	}

	/**
	 * Dirties the given file, forcing a build.
	 */
	protected void dirty(IFile file) throws CoreException {
		file.setContents(getRandomContents(), true, true, getMonitor());
	}

	/**
	 * Sets the workspace autobuilding to the desired value.
	 */
	protected void setAutoBuilding(boolean value) throws CoreException {
		IWorkspace workspace = getWorkspace();
		if (workspace.isAutoBuilding() == value)
			return;
		IWorkspaceDescription desc = workspace.getDescription();
		desc.setAutoBuilding(value);
		workspace.setDescription(desc);
	}

	/**
	 * Sets the workspace build order to just contain the given project.
	 */
	protected void setBuildOrder(IProject project) throws CoreException {
		IWorkspace workspace = getWorkspace();
		IWorkspaceDescription desc = workspace.getDescription();
		desc.setBuildOrder(new String[] {project.getName()});
		workspace.setDescription(desc);
	}

	/**
	 * Sets the workspace build order to contain the two given projects
	 */
	protected void setBuildOrder(IProject project1, IProject project2) throws CoreException {
		IWorkspace workspace = getWorkspace();
		IWorkspaceDescription desc = workspace.getDescription();
		desc.setBuildOrder(new String[] {project1.getName(), project2.getName()});
		workspace.setDescription(desc);
	}

	/**
	 * Saves the current auto-build flag value so it can be restored later.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		autoBuilding = getWorkspace().isAutoBuilding();

	}

	/**
	 * Restores the auto-build flag to its original value.
	 */
	protected void tearDown() throws Exception {
		//revert to default build order
		IWorkspace workspace = getWorkspace();
		IWorkspaceDescription desc = workspace.getDescription();
		desc.setBuildOrder(null);
		workspace.setDescription(desc);
		waitForBuild();
		setAutoBuilding(autoBuilding);
		super.tearDown();
	}
}
