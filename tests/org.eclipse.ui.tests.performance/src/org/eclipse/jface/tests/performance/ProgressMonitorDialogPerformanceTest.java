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
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.reportTimings;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * @since 3.3
 */
public class ProgressMonitorDialogPerformanceTest {

	@RegisterExtension
	CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

	/**
	 * Test the time for doing a refresh.
	 */
	@Test
	public void testLongNames(TestInfo testInfo) throws Throwable {
		Display display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}

		Shell shell = new Shell(display);
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);

		List<Long> timings = new ArrayList<>();

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
				long before = System.nanoTime();
				for (int counter = 0; counter < 30; counter++) {
					monitor.setTaskName(taskName);
					processEvents();
				}
				processEvents();
				timings.add(System.nanoTime() - before);
			}
		};

		try {
			dialog.run(false, true, runnable);

			assertEquals(20, timings.size());
			reportTimings(getClass().getSimpleName() + "." + testInfo.getDisplayName(), timings);
		} finally {
			shell.dispose();
		}
	}

}
