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
package org.eclipse.core.tests.resources;

import junit.framework.*;

public class AllTests extends TestCase {
/**
 * AllTests constructor comment.
 * @param name java.lang.String
 */
public AllTests() {
	super(null);
}
/**
 * AllTests constructor comment.
 * @param name java.lang.String
 */
public AllTests(String name) {
	super(name);
}
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(IFileTest.suite());
		suite.addTest(IFolderTest.suite());
		suite.addTest(IProjectTest.suite());
		suite.addTest(IPathVariableTest.suite());
		suite.addTest(IResourceChangeEventTest.suite());
		suite.addTest(IResourceChangeListenerTest.suite());
		suite.addTest(IResourceDeltaTest.suite());
		suite.addTest(IResourceTest.suite());
		suite.addTest(ISynchronizerTest.suite());
		suite.addTest(IWorkspaceRootTest.suite());
		suite.addTest(IWorkspaceTest.suite());
		suite.addTest(MarkerSetTest.suite());
		suite.addTest(MarkerTest.suite());
		suite.addTest(NatureTest.suite());
		suite.addTest(ProjectOrderTest.suite());
		suite.addTest(ResourceURLTest.suite());
		suite.addTest(TeamPrivateMemberTest.suite());
		suite.addTest(WorkspaceTest.suite());
		suite.addTest(LinkedResourceTest.suite());
		suite.addTest(LinkedResourceWithPathVariableTest.suite());
		return suite;
	}
}
