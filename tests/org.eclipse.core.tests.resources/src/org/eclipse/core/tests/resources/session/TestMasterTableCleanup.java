/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.session;

import java.util.Properties;
import junit.framework.Test;
import org.eclipse.core.internal.resources.TestingSupport;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.resources.AutomatedTests;
import org.eclipse.core.tests.session.WorkspaceSessionTestSuite;

/**
 * This is an internal test that makes sure the workspace master table does
 * not contain any stable entries after restart
 */
public class TestMasterTableCleanup extends WorkspaceSerializationTest {
	private static final String CLOSE_OPEN = "CloseOpen";
	private static final String CLOSE_DELETE = "CloseDelete";

	public TestMasterTableCleanup() {
		super();
	}

	public TestMasterTableCleanup(String name) {
		super(name);
	}

	/**
	 * Setup.  Two scenarios with stale entries were:
	 *  1) Project that was closed and then opened
	 *  2) Project that was closed and then deleted
	 */
	public void test1() {
		IProject closeOpen = getWorkspace().getRoot().getProject(CLOSE_OPEN);
		try {
			closeOpen.create(null);
			closeOpen.open(null);
			closeOpen.close(null);
			closeOpen.open(null);

			IProject closeDelete = getWorkspace().getRoot().getProject(CLOSE_DELETE);
			closeDelete.create(null);
			closeDelete.open(null);
			closeDelete.close(null);
			closeDelete.delete(IResource.NONE, null);

			getWorkspace().save(true, null);
		} catch (CoreException e) {
			fail("1.99", e);
		}
	}

	/**
	 * Verify. Ensure safe table does not contain stale entries.
	 */
	public void test2() {
		Properties masterTable = TestingSupport.getMasterTable();
		//ensure master table does not contain entries for stale projects
		IProject closeOpen = getWorkspace().getRoot().getProject(CLOSE_OPEN);
		IProject closeDelete = getWorkspace().getRoot().getProject(CLOSE_DELETE);
		assertTrue("2.0", !masterTable.containsKey(closeOpen.getFullPath().append(".tree").toString()));
		assertTrue("2.1", !masterTable.containsKey(closeDelete.getFullPath().append(".tree").toString()));
	}

	public static Test suite() {
		return new WorkspaceSessionTestSuite(AutomatedTests.PI_RESOURCES_TESTS, TestMasterTableCleanup.class);
	}
}
