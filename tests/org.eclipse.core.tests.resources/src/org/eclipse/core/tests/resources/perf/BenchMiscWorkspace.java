/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.resources.perf;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.harness.CorePerformanceTest;

public class BenchMiscWorkspace extends CorePerformanceTest {
public static Test suite() { 
	TestSuite suite= new TestSuite();
	suite.addTest(new BenchMiscWorkspace("benchNoOp"));
 	return suite;
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
public void benchNoOp() throws Exception {
	final int REPEAT = 100000;
	IWorkspace ws = ResourcesPlugin.getWorkspace();
	
	IWorkspaceRunnable noop = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
		}
	};
	startBench();
	for (int i = 0; i < REPEAT; i++) {
		ws.run(noop, null);
	}
	stopBench("benchNoOp", REPEAT);
}
}
