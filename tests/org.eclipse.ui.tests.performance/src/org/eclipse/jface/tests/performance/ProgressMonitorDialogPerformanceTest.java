/*******************************************************************************
 * Copyright (c) 2007, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.performance;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * @since 3.3
 */
public class ProgressMonitorDialogPerformanceTest extends BasicPerformanceTest {

	public ProgressMonitorDialogPerformanceTest(String testName) {
		super(testName);
	}

	/**
	 * Create a new instance of the receiver.
	 */
	public ProgressMonitorDialogPerformanceTest(String testName, int tagging) {
		super(testName, tagging);

	}

	/**
	 * Test the time for doing a refresh.
	 */
	public void testLongNames() throws Throwable {

		tagIfNecessary("JFace - 10000 element task name in progress dialog",
				Dimension.ELAPSED_PROCESS);
		setDegradationComment("The test changed in 3.7. For details, consult bug 298952.");

		Display display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell(display));

		IRunnableWithProgress runnable = monitor -> {

			char[] chars = new char[10000];
			for (int i = 0; i < chars.length; i++) {
				chars[i] = 'A';
			}
			final String taskName = new String(chars);

			// warm up
			monitor.setTaskName(taskName);
			processEvents();

			// test
			for (int testCounter = 0; testCounter < 20; testCounter++) {
				startMeasuring();
				for (int counter = 0; counter < 30; counter++) {
				monitor.setTaskName(taskName);
				processEvents();
			}
				processEvents();
				stopMeasuring();
			}
		};

		dialog.run(false, true, runnable);

		commitMeasurements();
		assertPerformance();
	}

}
