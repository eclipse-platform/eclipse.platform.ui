/*******************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import junit.framework.*;

public class AllTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(CharsetTest.class);
		suite.addTestSuite(ContentDescriptionManagerTest.class);
		suite.addTestSuite(FilteredResourceTest.class);
		suite.addTestSuite(HiddenResourceTest.class);
		suite.addTestSuite(VirtualFolderTest.class);
		suite.addTestSuite(IFileTest.class);
		suite.addTestSuite(IFolderTest.class);
		suite.addTestSuite(IPathVariableTest.class);
		suite.addTestSuite(IProjectDescriptionTest.class);
		suite.addTestSuite(IProjectTest.class);
		suite.addTestSuite(IResourceChangeEventTest.class);
		suite.addTestSuite(IResourceChangeListenerTest.class);
		suite.addTestSuite(IResourceDeltaTest.class);
		suite.addTestSuite(IResourceTest.class);
		suite.addTestSuite(ISynchronizerTest.class);
		suite.addTestSuite(IWorkspaceRootTest.class);
		suite.addTestSuite(IWorkspaceTest.class);
		suite.addTestSuite(LinkedResourceTest.class);
		suite.addTestSuite(LinkedResourceWithPathVariableTest.class);
		suite.addTestSuite(LinkedResourceSyncMoveAndCopyTest.class);
		suite.addTestSuite(MarkerSetTest.class);
		suite.addTestSuite(MarkerTest.class);
		suite.addTestSuite(NatureTest.class);
		suite.addTestSuite(NonLocalLinkedResourceTest.class);
		suite.addTestSuite(ProjectOrderTest.class);
		suite.addTestSuite(ProjectScopeTest.class);
		suite.addTestSuite(ProjectSnapshotTest.class);
		suite.addTestSuite(ResourceAttributeTest.class);
		suite.addTestSuite(ResourceURLTest.class);
		suite.addTestSuite(TeamPrivateMemberTest.class);
		suite.addTestSuite(WorkspaceTest.class);
		return suite;
	}
}
