package org.eclipse.core.tests.resources.usecase;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.tests.harness.WorkspaceSessionTest;

public abstract class SnapshotTest extends WorkspaceSessionTest {

	/** project names */
	static final String PROJECT_1 = "MyProject";
	static final String PROJECT_2 = "Project2";

	/** activities */
	static final String COMMENT_1 = "COMMENT ONE";
	static final String COMMENT_2 = "COMMENT TWO";
public SnapshotTest() {
}
public SnapshotTest(String name) {
	super(name);
}
}
