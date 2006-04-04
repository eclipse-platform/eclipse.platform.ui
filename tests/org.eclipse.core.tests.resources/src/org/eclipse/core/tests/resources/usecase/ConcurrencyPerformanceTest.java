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
package org.eclipse.core.tests.resources.usecase;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.core.tests.resources.ResourceTest;

public class ConcurrencyPerformanceTest extends ResourceTest {
	public ConcurrencyPerformanceTest() {
		super("");
	}

	public ConcurrencyPerformanceTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ConcurrencyPerformanceTest.class);
	}

	public void testSimpleCalls() {
		final IWorkspaceRunnable job = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
				// do nothing
			}
		};
		new PerformanceTestRunner() {
			protected void test() {
				try {
					getWorkspace().run(job, null);
				} catch (CoreException e) {
					fail("1.0", e);
				}
			}
		}.run(this, 10, 50);
	}
}
