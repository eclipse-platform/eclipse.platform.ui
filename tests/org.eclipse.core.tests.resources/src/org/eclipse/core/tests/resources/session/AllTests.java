/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.session;

import junit.framework.*;

public class AllTests extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(SampleSessionTest.suite());
		suite.addTest(TestBug93473.suite());
		suite.addTest(TestSave.suite());
		suite.addTest(Test1G1N9GZ.suite());
		suite.addTest(TestCloseNoSave.suite());
		suite.addTest(TestMultiSnap.suite());
		suite.addTest(TestSaveCreateProject.suite());
		suite.addTest(TestSaveSnap.suite());
		suite.addTest(TestSaveWithClosedProject.suite());
		suite.addTest(TestSnapSaveSnap.suite());
		suite.addTest(TestBug6995.suite());
		suite.addTest(TestInterestingProjectPersistence.suite());
		suite.addTest(TestBuilderDeltaSerialization.suite());
		suite.addTest(Test1GALH44.suite());
		suite.addTest(TestMissingBuilder.suite());
		suite.addTest(TestClosedProjectLocation.suite());
		suite.addTest(FindDeletedMembersTest.suite());
		suite.addTest(TestBug20127.suite());
		suite.addTest(TestBug12575.suite());
		suite.addTest(WorkspaceDescriptionTest.suite());
		suite.addTest(TestBug30015.suite());
		suite.addTest(TestMasterTableCleanup.suite());
		suite.addTest(ProjectPreferenceSessionTest.suite());
		suite.addTest(TestBug113943.suite());
		suite.addTest(TestCreateLinkedResourceInHiddenProject.suite());
		// this one comes from org.eclipse.core.tests.resources.saveparticipant
		// comment this out until we have a better solution for running these tests
		// (keeping their contents inside this plugin as subdirs and dynamically installing
		// seems to be a promising approach)
		//suite.addTest(SaveParticipantTest.suite());		
		//session tests from other packages  
		suite.addTest(org.eclipse.core.tests.resources.regression.TestMultipleBuildersOfSameType.suite());
		suite.addTest(org.eclipse.core.tests.resources.usecase.SnapshotTest.suite());
		return suite;
	}
}
