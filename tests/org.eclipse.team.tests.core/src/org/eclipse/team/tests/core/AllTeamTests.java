/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.tests.core;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class AllTeamTests extends EclipseWorkspaceTest {

	/**
	 * Constructor for CVSClientTest.
	 */
	public AllTeamTests() {
		super();
	}

	/**
	 * Constructor for CVSClientTest.
	 * @param name
	 */
	public AllTeamTests(String name) {
		super(name);
	}

	/*
	 * ORDER IS IMPORTANT: Run compatibility and resource tests before any other!!!
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(RepositoryProviderTests.suite());
		suite.addTest(StreamTests.suite());
		return suite;
	}
}

