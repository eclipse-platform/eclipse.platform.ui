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
package org.eclipse.core.tests.resources.perf;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.core.tests.resources.ResourceTest;

public class BenchMiscWorkspace extends ResourceTest {
	public static Test suite() {
		return new TestSuite(BenchMiscWorkspace.class);
		//		TestSuite suite = new TestSuite(BenchMiscWorkspace.class.getName());
		//		suite.addTest(new BenchMiscWorkspace("testGetProject"));
		//		return suite;
	}

	/**
	 * Constructor for BenchMiscWorkspace.
	 */
	public BenchMiscWorkspace() {
		super();
	}

	/**
	 * Constructor for BenchMiscWorkspace.
	 * @param name
	 */
	public BenchMiscWorkspace(String name) {
		super(name);
	}

	/**
	 * Benchmarks performing many empty operations.
	 */
	public void testNoOp() throws Exception {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final IWorkspaceRunnable noop = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
			}
		};
		//run a first operation to make sure no other jobs are running before starting timer
		ws.run(noop, null);
		waitForBuild();
		//now start the test
		new PerformanceTestRunner() {
			protected void test() {
				try {
					ws.run(noop, null);
				} catch (CoreException e) {
					fail("0.0", e);
				}
			}
		}.run(this, 10, 100000);
	}

	public void testGetProject() {
		new PerformanceTestRunner() {
			protected void test() {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				for (int i = 0; i < 2000; i++)
					root.getProject(Integer.toString(i));
			}
		}.run(this, 10, 1000);

	}
}
