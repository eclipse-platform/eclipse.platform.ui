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
import junit.framework.TestSuite;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.resources.CorePerformanceTest;

public class ConcurrencyPerformanceTest extends CorePerformanceTest {
	public ConcurrencyPerformanceTest() {
		super("");
	}

	public ConcurrencyPerformanceTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ConcurrencyPerformanceTest.class);
	}

	public void testSimpleCalls() throws CoreException {

		IWorkspaceRunnable job = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
				// do nothing
			}
		};
		startBench();
		int repeat = 50;
		for (int i = 0; i < repeat; i++) {
			getWorkspace().run(job, null);
		}
		stopBench("testSimpleCalls", repeat);
	}
}