/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.TestCase;
import org.eclipse.core.runtime.SubMonitor;

/**
 * Ensures that creating a SubMonitor with a small number of
 * ticks will not prevent it from reporting accurate progress.
 */
public class SubMonitorSmallTicksTest extends TestCase {

	private TestProgressMonitor topmostMonitor;
	private SubMonitor smallTicksChild;
	private long startTime;

	private static int TOTAL_WORK = 1000;

	protected void setUp() throws Exception {
		topmostMonitor = new TestProgressMonitor();
		smallTicksChild = SubMonitor.convert(topmostMonitor, 10);
		super.setUp();
		startTime = System.currentTimeMillis();
	}

	public void testWorked() {
		SubMonitor bigTicksChild = smallTicksChild.newChild(10).setWorkRemaining(TOTAL_WORK);
		for (int i = 0; i < TOTAL_WORK; i++) {
			bigTicksChild.worked(1);
		}
		bigTicksChild.done();
	}

	public void testInternalWorked() {
		double delta = 10.0d / TOTAL_WORK;

		for (int i = 0; i < TOTAL_WORK; i++) {
			smallTicksChild.internalWorked(delta);
		}
	}

	protected void tearDown() throws Exception {
		smallTicksChild.done();
		topmostMonitor.done();
		long endTime = System.currentTimeMillis();
		SubMonitorTest.reportPerformance(getClass().getName(), getName(), startTime, endTime);
		topmostMonitor.assertOptimal();
		super.tearDown();
	}

}
