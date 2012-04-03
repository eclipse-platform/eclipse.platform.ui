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
package org.eclipse.ant.tests.core.tests;


import java.io.File;

import org.eclipse.ant.core.TargetInfo;
import org.eclipse.ant.tests.core.AbstractAntTest;
import org.eclipse.ant.tests.core.testplugin.AntTestChecker;
import org.eclipse.core.runtime.CoreException;


public class TargetTests extends AbstractAntTest {
	
	public TargetTests(String name) {
		super(name);
	}
	
	/**
	 * Ensures that a default target is found
	 */
	public void testDefaultTarget() throws CoreException {
		TargetInfo[] targets= getTargets("TestForEcho.xml");
		assertEquals("Should be two targets in TestForEcho.xml", 2, targets.length);
		assertTrue("Test for Echo should be the default target", targets[1].isDefault());
	}
	
	/**
	 * Ensures that targets are found in a build file with data types
	 */
	public void testGetTargetsWithDataTypes() throws CoreException {
		TargetInfo[] targets= getTargets("Bug32551.xml");
		assertEquals("Should be one targets in Bug32551.xml", 1, targets.length);
	}
	
	/**
	 * Ensures that targets are found in a buildfile with a fileset based on ant_home
	 * (that ant_home is set at parse time)
	 * Bug 42926.
	 */
	public void testGetTargetsWithAntHome() {
		System.getProperties().remove("ant.home");
		try {
			getTargets("Bug42926.xml");
		} catch (CoreException ce) {
			//classpathref was successful but the task is not defined
			String message = ce.getMessage();
			assertTrue("Core exception message not as expected: " + message, message.endsWith("Bug42926.xml:7: taskdef class com.foo.SomeTask cannot be found\n using the classloader AntClassLoader[]"));
		}
	}
	
	/**
	 * Ensures that target names are retrieved properly
	 */
	public void testTargetNames() throws CoreException {
		String[] targetNames= getTargetNames("TestForEcho.xml");
		assertEquals("Should be two targets in TestForEcho.xml", 2, targetNames.length);
		assertEquals("First name should be init", "init", targetNames[0]);
		assertEquals("Second name should be Test for Echo", "Test for Echo", targetNames[1]);
	}
	
	/**
	 * Ensures that target descriptions are retrieved properly
	 */
	public void testTargetDescription() throws CoreException {
		String[] targetDescriptions= getTargetDescriptions("TestForEcho.xml");
		assertEquals("Should be two targets in TestForEcho.xml", 2, targetDescriptions.length);
		assertNull("First description should be null", targetDescriptions[0]);
		assertEquals("Second description should be Calls other targets", "Calls other echos", targetDescriptions[1]);
	}
	
	/**
	 * Ensures that target projects are retrieved properly
	 */
	public void testTargetProject() throws CoreException {
		String targetProject= getProjectName("TestForEcho.xml", "Test for Echo");
		assertEquals("Project name should be Echo Test", "Echo Test", targetProject);
	}
	
	/**
	 * Ensures that target dependencies are retrieved properly
	 */
	public void testTargetDependencies() throws CoreException {
		String[] dependencies= getDependencies("TestForEcho.xml", "Test for Echo");
		assertNotNull("Dependencies should not be null", dependencies);
		assertEquals("Should be one dependency in Test for Echo", 1, dependencies.length);
		assertEquals("First dependency should be init", "init", dependencies[0]);
	}
	
	/**
	 * Runs an Ant build and ensure that the build file location is logged
	 */
	public void testRunScript() throws CoreException {
		run("TestForEcho.xml");
		String message= (String)AntTestChecker.getDefault().getMessages().get(0);
		assertTrue("Build file location should be logged as the first message", message != null && message.endsWith("AntTests" + File.separator + "buildfiles" + File.separator + "TestForEcho.xml"));
		assertSuccessful();
	}
}