/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.session;

import junit.framework.Test;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.tests.resources.AutomatedTests;
import org.eclipse.core.tests.resources.WorkspaceSessionTest;
import org.eclipse.core.tests.session.WorkspaceSessionTestSuite;

/**
 * Test for bug 426263
 */
public class TestBug426263 extends WorkspaceSessionTest {
	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, TestBug426263.class);
	}

	public TestBug426263() {
		super();
	}

	public TestBug426263(String name) {
		super(name);
	}

	public void testBug() {
		IPathVariableManager manager = getWorkspace().getPathVariableManager();
		assertFalse(manager.isUserDefined("ECLIPSE_HOME"));
	}
}
