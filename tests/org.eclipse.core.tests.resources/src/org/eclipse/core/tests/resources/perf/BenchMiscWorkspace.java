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
package org.eclipse.core.tests.resources.perf;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.resources.ResourceTest;

public class BenchMiscWorkspace extends ResourceTest {
	public static Test suite() {
		return new TestSuite(BenchMiscWorkspace.class);
		//		TestSuite suite = new TestSuite(BenchMiscWorkspace.class.getName());
		//		suite.addTest(new BenchMiscWorkspace("benchNoOp"));
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
		new CorePerformanceTest() {
			protected void operation() {
				try {
					ws.run(noop, null);
				} catch (CoreException e) {
					fail("0.0", e);
				}
			}
		}.run(this, 10, 100000);
	}
}