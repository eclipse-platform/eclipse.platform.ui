/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.team.tests.ccvs.core.cvsresources.AllTestsCVSResources;
import org.eclipse.team.tests.ccvs.core.provider.AllTestsProvider;
import org.eclipse.team.tests.ccvs.core.subscriber.AllTestsTeamSubscriber;

public class AllTests extends EclipseTest {

	/**
	 * Constructor for CVSClientTest.
	 */
	public AllTests() {
		super();
	}

	/**
	 * Constructor for CVSClientTest.
	 * @param name
	 */
	public AllTests(String name) {
		super(name);
	}

	/*
	 * ORDER IS IMPORTANT: Run compatibility and resource tests before any other!!!
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();
		//suite.addTest(org.eclipse.team.tests.ccvs.core.compatible.AllTestsCompatibility.suite());
		suite.addTest(AllTestsCVSResources.suite());
		suite.addTest(AllTestsProvider.suite());
		suite.addTest(AllTestsTeamSubscriber.suite());
		return new CVSTestSetup(suite);
	}
}

