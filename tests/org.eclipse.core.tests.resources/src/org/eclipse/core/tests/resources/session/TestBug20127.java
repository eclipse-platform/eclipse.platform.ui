/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources.session;

import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.internal.builders.DeltaVerifierBuilder;
import org.eclipse.core.tests.internal.builders.TestBuilder;

/**
 * Tests regression of bug 20127 - error restoring builder state after
 * project move.
 */
public class TestBug20127 extends WorkspaceSerializationTest {
/**
 * Setup.  Creates a project with a builder, with a built state,
 * autobuild off.
 */
public void test1() {
	IProject project = workspace.getRoot().getProject("Project1");
	ensureExistsInWorkspace(project, true);
	try {
		//turn off autobuild
		IWorkspaceDescription desc = workspace.getDescription();
		desc.setAutoBuilding(false);
		workspace.setDescription(desc);
	
		//create a project and configure builder
		IProjectDescription description = project.getDescription();
		ICommand command = description.newCommand();
		Map args = command.getArguments();
		args.put(TestBuilder.BUILD_ID, "Project1Build1");
		command.setBuilderName(DeltaVerifierBuilder.BUILDER_NAME);
		command.setArguments(args);
		description.setBuildSpec(new ICommand[] {command});
		project.setDescription(description, getMonitor());

		//initial build 
		workspace.build(IncrementalProjectBuilder.FULL_BUILD, getMonitor());
		
		getWorkspace().save(true, getMonitor());
	} catch (CoreException e) {
		fail("1.0", e);
	}
}
/**
 * Rename the project without invoking any builds.
 */
public void test2() {
	try {
		IProject project = workspace.getRoot().getProject("Project1");
		IProjectDescription desc = project.getDescription();
		desc.setName("MovedProject");
		project.move(desc, IResource.NONE, getMonitor());
		workspace.save(true, getMonitor());
	} catch (CoreException e) {
		fail ("1.0", e);
	}
}
/**
 * If this session starts correctly then the bug is fixed
 */
public void test3() {
	IProject oldLocation = workspace.getRoot().getProject("Project1");
	IProject newLocation = workspace.getRoot().getProject("MovedProject");

	assertTrue("1.0", !oldLocation.exists());
	assertTrue("1.0", newLocation.exists());
	assertTrue("1.1", newLocation.isOpen());
	try {
		workspace.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, getMonitor());
	} catch (CoreException e) {
		fail("1.99", e);
	}
}
}
