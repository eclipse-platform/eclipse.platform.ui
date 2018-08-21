/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		TargetInfo[] targets = getTargets("TestForEcho.xml"); //$NON-NLS-1$
		assertEquals("Should be two targets in TestForEcho.xml", 2, targets.length); //$NON-NLS-1$
		assertTrue("Test for Echo should be the default target", targets[1].isDefault()); //$NON-NLS-1$
	}

	/**
	 * Ensures that targets are found in a build file with data types
	 */
	public void testGetTargetsWithDataTypes() throws CoreException {
		TargetInfo[] targets = getTargets("Bug32551.xml"); //$NON-NLS-1$
		assertEquals("Should be one targets in Bug32551.xml", 1, targets.length); //$NON-NLS-1$
	}

	/**
	 * Ensures that targets are found in a buildfile with a fileset based on ant_home (that ant_home is set at parse time) Bug 42926.
	 */
	public void testGetTargetsWithAntHome() {
		System.getProperties().remove("ant.home"); //$NON-NLS-1$
		try {
			getTargets("Bug42926.xml"); //$NON-NLS-1$
		}
		catch (CoreException ce) {
			// classpathref was successful but the task is not defined
			String message = ce.getMessage();
			assertTrue("Core exception message not as expected: " + message, message.endsWith("Bug42926.xml:7: taskdef class com.foo.SomeTask cannot be found\n using the classloader AntClassLoader[]")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Ensures that target names are retrieved properly
	 */
	public void testTargetNames() throws CoreException {
		String[] targetNames = getTargetNames("TestForEcho.xml"); //$NON-NLS-1$
		assertEquals("Should be two targets in TestForEcho.xml", 2, targetNames.length); //$NON-NLS-1$
		assertEquals("First name should be init", "init", targetNames[0]); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Second name should be Test for Echo", "Test for Echo", targetNames[1]); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Ensures that target descriptions are retrieved properly
	 */
	public void testTargetDescription() throws CoreException {
		String[] targetDescriptions = getTargetDescriptions("TestForEcho.xml"); //$NON-NLS-1$
		assertEquals("Should be two targets in TestForEcho.xml", 2, targetDescriptions.length); //$NON-NLS-1$
		assertNull("First description should be null", targetDescriptions[0]); //$NON-NLS-1$
		assertEquals("Second description should be Calls other targets", "Calls other echos", targetDescriptions[1]); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Ensures that target projects are retrieved properly
	 */
	public void testTargetProject() throws CoreException {
		String targetProject = getProjectName("TestForEcho.xml", "Test for Echo"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Project name should be Echo Test", "Echo Test", targetProject); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Ensures that target dependencies are retrieved properly
	 */
	public void testTargetDependencies() throws CoreException {
		String[] dependencies = getDependencies("TestForEcho.xml", "Test for Echo"); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull("Dependencies should not be null", dependencies); //$NON-NLS-1$
		assertEquals("Should be one dependency in Test for Echo", 1, dependencies.length); //$NON-NLS-1$
		assertEquals("First dependency should be init", "init", dependencies[0]); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Runs an Ant build and ensure that the build file location is logged
	 */
	public void testRunScript() throws CoreException {
		run("TestForEcho.xml"); //$NON-NLS-1$
		String message = AntTestChecker.getDefault().getMessages().get(0);
		assertTrue("Build file location should be logged as the first message", message != null && message.endsWith("AntTests" + File.separator + "buildfiles" + File.separator + "TestForEcho.xml")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertSuccessful();
	}
}