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

public class AllTargetTests extends EclipseWorkspaceTest {

	/**
	 * Constructor for AllTargetTests.
	 */
	public AllTargetTests() {
		super();
	}

	/**
	 * Constructor for AllTargetTests.
	 * @param name
	 */
	public AllTargetTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(TargetProviderTests.suite());
		suite.addTest(RemoteResourceTests.suite());
		return new TargetTestSetup(suite);
	}
}
