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
package org.eclipse.core.tests.resources.usecase;

import junit.framework.Test;
import org.eclipse.core.tests.harness.WorkspaceSessionTest;
import org.eclipse.core.tests.resources.AutomatedTests;
import org.eclipse.core.tests.resources.session.TestBug30015;
import org.eclipse.core.tests.session.WorkspaceSessionTestSuite;

/**
 * Runs all the snapshot usecase tests as a single session test.
 * Each test method will run a different snapshot test.
 */
public class SnapshotTest extends WorkspaceSessionTest {

	/** project names */
	static final String PROJECT_1 = "MyProject";
	static final String PROJECT_2 = "Project2";

	/** activities */
	static final String COMMENT_1 = "COMMENT ONE";
	static final String COMMENT_2 = "COMMENT TWO";

	public SnapshotTest() {
		super();
	}

	public SnapshotTest(String name) {
		super(name);
	}

	public void test1() {
		Snapshot1Test test = new Snapshot1Test();
		test.testCreateMyProject();
		test.testCreateProject2();
		test.testSnapshotWorkspace();
	}

	public void test2() {
		Snapshot2Test test = new Snapshot2Test();
		test.testVerifyPreviousSession();
		test.testChangeMyProject();
		test.testChangeProject2();
		test.testSnapshotWorkspace();
	}

	public void test3() {
		Snapshot3Test test = new Snapshot3Test();
		test.testVerifyPreviousSession();
		test.testSaveWorkspace();
	}

	public void test4() {
		Snapshot4Test test = new Snapshot4Test();
		test.testVerifyPreviousSession();
		test.testChangeMyProject();
		test.testChangeProject2();
	}

	public void test5() {
		Snapshot5Test test = new Snapshot5Test();
		test.testVerifyPreviousSession();
		test.cleanUp();
	}
	
	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, SnapshotTest.class);
	}
	
}