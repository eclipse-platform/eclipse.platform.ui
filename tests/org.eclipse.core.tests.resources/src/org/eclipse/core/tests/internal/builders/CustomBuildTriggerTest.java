/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.builders;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

/**
 * These tests exercise the function added in Eclipse 3.1 to allow a builder
 * to specify what build triggers it responds to.  Related API includes:
 * ICommand#isConfigurable()
 * ICommand.isBuilding(int)
 * ICommand.setBuilding(int, boolean)
 * The "isConfigurable" attribute in the builder extension schema
 */
public class CustomBuildTriggerTest extends AbstractBuilderTest {
	public static Test suite() {
		return new TestSuite(CustomBuildTriggerTest.class);
	}

	public CustomBuildTriggerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		SortBuilder.resetSingleton();
		CustomTriggerBuilder.resetSingleton();
	}
	/**
	 * Tests that a builder that does not declare itself as configurable
	 * is not configurable.
	 */
	public void testConfigurable() {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("PROJECT" + 1);
		ICommand command = null;
		try {
			// Turn auto-building off
			setAutoBuilding(false);
			// Create some resources
			project.create(getMonitor());
			project.open(getMonitor());
			// Create and set a build specs for project 
			IProjectDescription desc = project.getDescription();
			desc.setBuildSpec(new ICommand[] {createCommand(desc, CustomTriggerBuilder.BUILDER_NAME, "Build0")});
			project.setDescription(desc, getMonitor());
			command = project.getDescription().getBuildSpec()[0];
		} catch (CoreException e) {
			fail("0.99", e);
		}
		assertTrue("1.0", command.isConfigurable());
		//ensure that setBuilding has effect
		assertTrue("1.1", command.isBuilding(IncrementalProjectBuilder.AUTO_BUILD));
		command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, false);
		assertTrue("1.2", !command.isBuilding(IncrementalProjectBuilder.AUTO_BUILD));

		assertTrue("1.3", command.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD));
		command.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, false);
		assertTrue("1.4", !command.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD));
		
		assertTrue("1.5", command.isBuilding(IncrementalProjectBuilder.FULL_BUILD));
		command.setBuilding(IncrementalProjectBuilder.FULL_BUILD, false);
		assertTrue("1.6", !command.isBuilding(IncrementalProjectBuilder.FULL_BUILD));
		
		assertTrue("1.7", command.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD));
		command.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, false);
		assertTrue("1.8", !command.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD));
		
		try {
			//set the command back into the project for change to take effect
			IProjectDescription desc = project.getDescription();
			desc.setBuildSpec(new ICommand[] {command});
			project.setDescription(desc, getMonitor());
		} catch (CoreException e1) {
			fail("1.99", e1);
		}

		//ensure the builder is not called
		try {
			project.build(IncrementalProjectBuilder.FULL_BUILD, getMonitor());
		} catch (CoreException e) {
			fail("2.99", e);
		}
		CustomTriggerBuilder builder = CustomTriggerBuilder.getInstance();
		assertTrue("2.0", builder == null);

		try {
			project.build(IncrementalProjectBuilder.CLEAN_BUILD, getMonitor());
		} catch (CoreException e) {
			fail("2.91", e);
		}
		builder = CustomTriggerBuilder.getInstance();
		assertTrue("2.1", builder == null);

		try {
			project.touch(getMonitor());
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, getMonitor());
		} catch (CoreException e) {
			fail("3.99", e);
		}
		builder = CustomTriggerBuilder.getInstance();
		assertTrue("3.0", builder == null);
		try {
			setAutoBuilding(true);
			project.touch(getMonitor());
		} catch (CoreException e) {
			fail("4.99", e);
		}
		builder = CustomTriggerBuilder.getInstance();
		assertTrue("4.0", builder == null);
		
		//turn the builder back on and make sure it runs
		command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, true);
		command.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, true);
		command.setBuilding(IncrementalProjectBuilder.FULL_BUILD, true);
		command.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, true);
		try {
			//set the command back into the project for change to take effect
			setAutoBuilding(false);
			IProjectDescription desc = project.getDescription();
			desc.setBuildSpec(new ICommand[] {command});
			project.setDescription(desc, getMonitor());
		} catch (CoreException e1) {
			fail("5.99", e1);
		}

		//ensure the builder is called
		try {
			project.build(IncrementalProjectBuilder.FULL_BUILD, getMonitor());
		} catch (CoreException e) {
			fail("6.99", e);
		}
		builder = CustomTriggerBuilder.getInstance();
		assertTrue("6.1", builder.wasFullBuild());

		try {
			project.build(IncrementalProjectBuilder.FULL_BUILD, getMonitor());
		} catch (CoreException e) {
			fail("7.99", e);
		}
		builder = CustomTriggerBuilder.getInstance();
		assertTrue("7.1", builder.wasFullBuild());

		try {
			project.build(IncrementalProjectBuilder.CLEAN_BUILD, getMonitor());
		} catch (CoreException e) {
			fail("8.99", e);
		}
		assertTrue("8.1", builder.wasCleanBuild());
}	
	/**
	 * Tests that a builder that does not declare itself as configurable
	 * is not configurable.
	 */
	public void testNonConfigurable() {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("PROJECT" + 1);
		ICommand command = null;
		try {
			// Turn auto-building off
			setAutoBuilding(false);
			// Create some resources
			project.create(getMonitor());
			project.open(getMonitor());
			// Create and set a build specs for project 
			IProjectDescription desc = project.getDescription();
			desc.setBuildSpec(new ICommand[] {createCommand(desc, "Build0")});
			project.setDescription(desc, getMonitor());
			command = project.getDescription().getBuildSpec()[0];
		} catch (CoreException e) {
			fail("0.99", e);
		}
		assertTrue("1.0", !command.isConfigurable());
		//ensure that setBuilding has no effect
		command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, false);
		assertTrue("1.1", command.isBuilding(IncrementalProjectBuilder.AUTO_BUILD));
		command.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, false);
		assertTrue("1.2", command.isBuilding(IncrementalProjectBuilder.CLEAN_BUILD));
		command.setBuilding(IncrementalProjectBuilder.FULL_BUILD, false);
		assertTrue("1.3", command.isBuilding(IncrementalProjectBuilder.FULL_BUILD));
		command.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, false);
		assertTrue("1.4", command.isBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD));

		//set the command back into the project for change to take effect
		try {
			IProjectDescription desc = project.getDescription();
			desc.setBuildSpec(new ICommand[] {command});
			project.setDescription(desc, getMonitor());
		} catch (CoreException e1) {
			fail("1.99", e1);
		}

		//ensure that builder is still called
		try {
			project.build(IncrementalProjectBuilder.FULL_BUILD, getMonitor());
		} catch (CoreException e) {
			fail("2.99", e);
		}
		SortBuilder builder = SortBuilder.getInstance();
		assertTrue("2.0", builder.wasBuilt());
		assertTrue("2.1", builder.wasFullBuild());
		assertEquals("2.2", command, builder.getCommand());

		try {
			project.touch(getMonitor());
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, getMonitor());
		} catch (CoreException e) {
			fail("3.99", e);
		}
		assertTrue("3.0", builder.wasBuilt());
		assertTrue("3.1", builder.wasIncrementalBuild());
	}
}