/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.session;

import java.util.*;
import junit.framework.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.tests.harness.FileSystemHelper;

public class WorkspaceSessionTestSuite extends SessionTestSuite {

	private IPath instanceLocation = FileSystemHelper.getRandomLocation(FileSystemHelper.getTempDir());
	// should the test cases be run in alphabetical order?
	private boolean shouldSort;

	public WorkspaceSessionTestSuite(String pluginId) {
		super(pluginId);
	}

	public WorkspaceSessionTestSuite(String pluginId, Class theClass) {
		super(pluginId, theClass);
		this.shouldSort = true;
	}

	public WorkspaceSessionTestSuite(String pluginId, Class theClass, String name) {
		super(pluginId, theClass, name);
		this.shouldSort = true;
	}

	public WorkspaceSessionTestSuite(String pluginId, String name) {
		super(pluginId, name);
	}

	/**
	 * Ensures setup uses this suite's instance location.
	 */
	protected Setup getSetup() {
		Setup base = super.getSetup();
		base.setInstanceLocation(instanceLocation.toOSString());
		return base;
	}

	/**
	 * Ensures workspace location is empty before running the first test, and after
	 * running the last test. Also sorts the test cases to be run if this suite was
	 * created by reifying a test case class.
	 */
	public void run(TestResult result) {
		try {
			if (!shouldSort) {
				super.run(result);
				return;
			}
			// we have to sort the tests cases 			
			Test[] allTests = new Test[testCount()];
			Enumeration e = tests();
			for (int i = 0; i < allTests.length; i++)
				allTests[i] = (Test) e.nextElement();
			Arrays.sort(allTests, new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((TestCase) o1).getName().compareTo(((TestCase) o2).getName());
				}
			});
			// now run the tests in order
			for (int i = 0; i < allTests.length && !result.shouldStop(); i++)
				runTest(allTests[i], result);
		} finally {
			FileSystemHelper.clear(instanceLocation.toFile());
		};

	}

}