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
package org.eclipse.team.tests.ccvs.core.provider;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

/**
 * @version 	1.0
 * @author 	${user}
 */
public class AllTestsProvider extends EclipseTest {

	/**
	 * Constructor for AllTests.
	 */
	public AllTestsProvider() {
		super();
	}

	/**
	 * Constructor for AllTests.
	 * @param name
	 */
	public AllTestsProvider(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		//suite.addTestSuite(ModuleTest.class);
		suite.addTest(ImportTest.suite());
		suite.addTest(RemoteResourceTest.suite());
		suite.addTest(CVSProviderTest.suite());
		suite.addTest(SyncElementTest.suite());
		suite.addTest(ResourceDeltaTest.suite());
		suite.addTest(WatchEditTest.suite());
		suite.addTest(LinkResourcesTest.suite());
		suite.addTest(IsModifiedTests.suite());
		return new CVSTestSetup(suite);
	}
}
