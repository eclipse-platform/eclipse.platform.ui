/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ui.synchronize;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.tests.resources.ResourceTest;

public class AllTeamSynchronizeTests extends ResourceTest {

	/**
	 * Constructor for CVSClientTest.
	 */
	public AllTeamSynchronizeTests() {
		super();
	}

	/**
	 * Constructor for CVSClientTest.
	 * @param name
	 */
	public AllTeamSynchronizeTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(TestDiffNodePresentationModel.suite());
		suite.addTest(SyncInfoSetContentProviderTest.suite());
		return suite;
	}
}

