package org.eclipse.ant.tests.core.tests;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.ant.core.TargetInfo;
import org.eclipse.ant.tests.core.AbstractAntTest;
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
		assertTrue("Should be two targets in TestForEcho.xml", targets.length == 2);
		assertTrue("Test for Echo should be the default target", targets[1].isDefault());
	}
	
	/**
	 * Ensures that target names are retrieved properly
	 */
	public void testTargetNames() throws CoreException {
		String[] targetNames= getTargetNames("TestForEcho.xml");
		assertTrue("Should be two targets in TestForEcho.xml", targetNames.length == 2);
		assertTrue("First name should be init", targetNames[0].equals("init"));
		assertTrue("Second name should be Test for Echo", targetNames[1].equals("Test for Echo"));
	}
	
	/**
	 * Ensures that target descriptions are retrieved properly
	 */
	public void testTargetDescription() throws CoreException {
		String[] targetDescriptions= getTargetDescriptions("TestForEcho.xml");
		assertTrue("Should be two targets in TestForEcho.xml", targetDescriptions.length == 2);
		assertNull("First description should be null", targetDescriptions[0]);
		assertTrue("Second description should be Calls other targets", targetDescriptions[1].equals("Calls other echos"));
	}
	
	/**
	 * Runs an Ant script
	 */
	public void testRunScript() throws CoreException {
		run("TestForEcho.xml");
	}
}

