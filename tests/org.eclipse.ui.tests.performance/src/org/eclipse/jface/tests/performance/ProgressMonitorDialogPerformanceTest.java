/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.performance;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;
import org.eclipse.ui.tests.performance.TestRunnable;

/**
 * @since 3.3
 *
 */
public class ProgressMonitorDialogPerformanceTest extends BasicPerformanceTest {

	/**
	 * @param testName
	 */
	public ProgressMonitorDialogPerformanceTest(String testName) {
		super(testName);
	}

	/**
	 * Create a new instance of the receiver.
	 * @param testName
	 * @param tagging
	 */
	public ProgressMonitorDialogPerformanceTest(String testName, int tagging) {
		super(testName, tagging);
		
	}

	/**
	 * Test the time for doing a refresh.
	 * 
	 * @throws Throwable
	 */
	public void testLongNames() throws Throwable {

		tagIfNecessary("JFace - 10000 element task name in progress dialog",
				Dimension.ELAPSED_PROCESS);
		
		Display display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}
		
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell(display));

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
				
				char[] chars = new char[10000];
				for (int i = 0; i < chars.length; i++) {
					chars[i] = 'A';
				}
				
				final String taskName = new String(chars);
				final IProgressMonitor finalMonitor = monitor;
				
				try {
					exercise(new TestRunnable() {
						public void run() {
							startMeasuring();
							finalMonitor.setTaskName(taskName);
							processEvents();
							stopMeasuring();
						}
					}, ViewerTest.MIN_ITERATIONS, ViewerTest.ITERATIONS,
							JFacePerformanceSuite.MAX_TIME);
				} catch (CoreException e) {
					fail(e.getMessage(), e);
				}

			

			}
		};

		try {
			dialog.run(false, true, runnable);
		} catch (InvocationTargetException e) {
			fail(e.getMessage(), e);
		} catch (InterruptedException e) {
			fail(e.getMessage(), e);
		}
		
		commitMeasurements();
		assertPerformance();
	}
	
}
