/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.propertytester;

import junit.framework.TestSuite;
import org.eclipse.core.internal.propertytester.FilePropertyTester;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.tests.resources.ResourceTest;

public class FilePropertyTesterTest extends ResourceTest {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public static TestSuite suite() {
		return new TestSuite(FilePropertyTesterTest.class);
	}

	public void testFilePropertyTester() throws Throwable {
		FilePropertyTester tester = new FilePropertyTester();
		IProgressMonitor monitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("project1");
		project.create(monitor);
		project.open(monitor);
		IFile target = project.getFile("tmp.txt");
		String method = "contentTypeId";
		String expected = "org.eclipse.core.runtime.text";
		String kindOf = "kindOf";
		String useFilename = "useFilenameOnly";

		boolean ret;
		ret = tester.test(target, method, new String[] {}, expected);
		assertFalse("1.0", ret);
		ret = tester.test(target, method, new String[] {kindOf}, expected);
		assertFalse("1.1", ret);
		ret = tester.test(target, method, new String[] {useFilename}, expected);
		assertTrue("1.2", ret);
		ret = tester.test(target, method, new String[] {kindOf, useFilename}, expected);
		assertTrue("1.3", ret);

		target.create(getRandomContents(), true, getMonitor());

		ret = tester.test(target, method, new String[] {}, expected);
		assertTrue("2.0", ret);
		ret = tester.test(target, method, new String[] {kindOf}, expected);
		assertTrue("2.1", ret);
		ret = tester.test(target, method, new String[] {useFilename}, expected);
		assertTrue("2.2", ret);
		ret = tester.test(target, method, new String[] {kindOf, useFilename}, expected);
		assertTrue("2.3", ret);

		project.delete(true, monitor);
	}

}
