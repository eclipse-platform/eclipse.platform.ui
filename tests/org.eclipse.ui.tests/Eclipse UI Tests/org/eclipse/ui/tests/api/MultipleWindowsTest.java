/*******************************************************************************
 * Copyright (c) 2016 Brian de Alwis and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brian de Alwis - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertNotSame;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.harness.util.TestRunLogUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * A set of tests for multiple monitor situations that ensures interactions are
 * isolated to the respective window.
 */
public class MultipleWindowsTest {
	@Rule
	public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;

	IWorkbench wb;
	IWorkbenchWindow win1;
	IWorkbenchWindow win2;

	@Before
	public void setUp() throws WorkbenchException {
		IWorkbench wb = PlatformUI.getWorkbench();
		win1 = wb.openWorkbenchWindow(null);
		win2 = wb.openWorkbenchWindow(null);
		assertNotSame(win1, win2);
	}

	@After
	public void tearDown() {
		if (win1 != null) {
			win1.close();
			win1 = null;
		}
		if (win2 != null) {
			win2.close();
			win2 = null;
		}
	}

	/**
	 * @see <a href="http://eclip.se/493335"> Bug 493335 - [WorkingSets] Setting
	 *      window working set affects all windows</a>
	 */
	@Test
	public void testIndependentWorkingSets() {
		assertNotSame(win1.getActivePage().getAggregateWorkingSet(), win2.getActivePage().getAggregateWorkingSet());
	}
}
