/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import junit.framework.*;

public class AllTests extends TestCase {

	public AllTests() {
		super(null);
	}

	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(CharsetTest.suite());
		suite.addTest(ContentDescriptionManagerTest.suite());
		suite.addTest(HiddenResourceTest.suite());
		suite.addTest(IFileTest.suite());
		suite.addTest(IFolderTest.suite());
		suite.addTest(IPathVariableTest.suite());
		suite.addTest(IProjectDescriptionTest.suite());
		suite.addTest(IProjectTest.suite());
		suite.addTest(IResourceChangeEventTest.suite());
		suite.addTest(IResourceChangeListenerTest.suite());
		suite.addTest(IResourceDeltaTest.suite());
		suite.addTest(IResourceTest.suite());
		suite.addTest(ISynchronizerTest.suite());
		suite.addTest(IWorkspaceRootTest.suite());
		suite.addTest(IWorkspaceTest.suite());
		suite.addTest(LinkedResourceTest.suite());
		suite.addTest(LinkedResourceWithPathVariableTest.suite());
		suite.addTest(MarkerSetTest.suite());
		suite.addTest(MarkerTest.suite());
		suite.addTest(NatureTest.suite());
		suite.addTest(NonLocalLinkedResourceTest.suite());
		suite.addTest(ProjectOrderTest.suite());
		suite.addTest(ResourceAttributeTest.suite());
		suite.addTest(ResourceURLTest.suite());
		suite.addTest(TeamPrivateMemberTest.suite());
		suite.addTest(WorkspaceTest.suite());
		return suite;
	}
}
