/*******************************************************************************
 * Copyright (c) 2015, 2017 Tasktop Technologies and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Red Hat Inc. - Bugs 474127, 474132
 *******************************************************************************/
package org.eclipse.ui.tests.progress;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.progress.FinishedJobs;
import org.eclipse.ui.internal.progress.ProgressAnimationItem;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.progress.ProgressRegion;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.tests.harness.util.TestRunLogUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

public class ProgressAnimationItemTest {
	@Rule
	public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;

	private Shell shell;
	private ProgressAnimationItem animationItem;

	@Before
	public void setUp() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		shell = new Shell(display);
		shell.setSize(400, 300);
		shell.setLayout(new FillLayout());
		shell.open();
		Composite composite = new Composite(shell, SWT.V_SCROLL);
		animationItem = createProgressAnimationItem(composite);
	}

	@After
	public void tearDown() {
		FinishedJobs.getInstance().clearAll();
		shell.dispose();
	}

	@Test
	public void testSingleJobRefreshOnce() throws Exception {
		createAndScheduleJob();

		ProgressManager.getInstance().notifyListeners();
		refresh();

		assertSingleAccessibleListener();
	}

	@Test
	public void testTwoJobsRefreshOnce() throws Exception {
		createAndScheduleJob();
		createAndScheduleJob();

		ProgressManager.getInstance().notifyListeners();
		refresh();

		assertSingleAccessibleListener();
	}

	@Test
	public void testSingleJobRefreshTwice() throws Exception {
		createAndScheduleJob();

		ProgressManager.getInstance().notifyListeners();
		refresh();
		refresh();

		assertSingleAccessibleListener();
	}

	private ProgressAnimationItem createProgressAnimationItem(Composite composite) {
		ProgressRegion progressRegion = new ProgressRegion();
		progressRegion.createContents(composite);
		return (ProgressAnimationItem) progressRegion.getAnimationItem();
	}

	private static void createAndScheduleJob() throws InterruptedException {
		DummyJob job = new DummyJob("Keep me", Status.OK_STATUS);
		job.setProperty(IProgressConstants.KEEP_PROPERTY, true);
		job.schedule();
		job.join();
	}

	private void refresh() throws Exception {
		Method m = ProgressAnimationItem.class.getDeclaredMethod("refresh");
		m.setAccessible(true);
		m.invoke(animationItem);
	}

	private void assertSingleAccessibleListener() throws Exception {
		assertEquals(1, getAccessibleListenersSize(getToolBar(animationItem).getAccessible()));
	}

	private ToolBar getToolBar(ProgressAnimationItem animationItem) {
		Composite top = (Composite) animationItem.getControl();
		for (Control child : top.getChildren()) {
			if (child instanceof ToolBar) {
				return (ToolBar) child;
			}
		}
		return null;
	}

	/**
	 * Loads, using reflection, the internal accessible listeners vector from
	 * inside the Accessible and returns its size. If the collection is null,
	 * returns 0.
	 */
	private static int getAccessibleListenersSize(Accessible accessible) throws Exception {
		Field f = Accessible.class.getDeclaredField("accessibleListeners");
		f.setAccessible(true);
		Collection<?> accessibleListeners = (Collection<?>) f.get(accessible);
		return accessibleListeners == null ? 0 : accessibleListeners.size();
	}

}
