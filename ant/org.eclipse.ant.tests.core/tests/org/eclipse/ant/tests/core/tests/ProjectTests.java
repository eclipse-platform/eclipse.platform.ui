package org.eclipse.ant.tests.core.tests;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.ant.tests.core.AbstractAntTest;
import org.eclipse.ant.tests.core.testplugin.AntTestChecker;
import org.eclipse.core.runtime.CoreException;


public class ProjectTests extends AbstractAntTest {
	
	public ProjectTests(String name) {
		super(name);
	}
	
	/**
	 * Tests that the three properties that should always be set are correct
	 */
	public void testBasePropertiesSet() throws CoreException {
		String buildFileName="TestForEcho.xml"; 
		run(buildFileName);
		assertTrue("eclipse.running should have been set as true", "true".equals(AntTestChecker.getDefault().getUserProperty("eclipse.running")));
		assertTrue("ant.file should have been set as the build file name", buildFileName.equals(AntTestChecker.getDefault().getUserProperty("ant.file")));
		assertNotNull("ant.version should have been set", AntTestChecker.getDefault().getUserProperty("ant.version"));
	}
	
	/**
	 * Tests that the three properties that should always be set are correct
	 */
	public void testBasePropertiesSetForDefaultBuildFile() throws CoreException {
		String buildFileName="build.xml"; 
		run((String)null, null, false);
		assertTrue("eclipse.running should have been set as true", "true".equals(AntTestChecker.getDefault().getUserProperty("eclipse.running")));
		assertTrue("ant.file should have been set as the build file name", buildFileName.equals(AntTestChecker.getDefault().getUserProperty("ant.file")));
		assertNotNull("ant.version should have been set", AntTestChecker.getDefault().getUserProperty("ant.version"));
	}
}

