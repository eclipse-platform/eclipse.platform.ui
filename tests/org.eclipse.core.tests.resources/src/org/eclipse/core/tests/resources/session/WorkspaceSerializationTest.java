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
package org.eclipse.core.tests.resources.session;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.tests.resources.WorkspaceSessionTest;

public class WorkspaceSerializationTest extends WorkspaceSessionTest {
	protected static final String PROJECT = "CrashProject";
	protected static final String FOLDER = "CrashFolder";
	protected static final String FILE = "CrashFile";
	protected IWorkspace workspace;

	/**
	 * Creates a new WorkspaceSerializationTest.
	 */
	public WorkspaceSerializationTest() {
		super("");
	}

	/**
	 * Creates a new WorkspaceSerializationTest.
	 * @param name the name of the test method to run
	 */
	public WorkspaceSerializationTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		workspace = getWorkspace();
	}
}
