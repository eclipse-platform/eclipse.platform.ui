/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;
import org.junit.Test;

/**
 * DecoratorViewerTest is the abstract class of the tests for the viewers.
 */
public abstract class DecoratorViewerTest extends AbstractNavigatorTest {

	/**
	 * Create a new instance of the receiver.
	 */
	public DecoratorViewerTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();
		ForegroundColorDecorator.setUpColor();
		BackgroundColorDecorator.setUpColor();
		FontDecorator.setUpFont();
	}

	/**
	 * Test the background on the viewer.
	 */
	@Test
	public void testBackground() throws PartInitException, CoreException,
			InterruptedException {

		BackgroundColorDecorator.setUpColor();

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		Assert.isNotNull(page, "No active page");

		final IViewPart view = openView(page);
		((DecoratorTestPart) view).setUpForDecorators();


		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(BackgroundColorDecorator.ID, true);

		Job.getJobManager().join(DecoratorManager.FAMILY_DECORATE, null);

		dispatchDuringUpdates((DecoratorTestPart) view);
		backgroundCheck(view);
		manager.setEnabled(BackgroundColorDecorator.ID, false);

	}

	/**
	 * Check the background colors in the view
	 */
	protected abstract void backgroundCheck(IViewPart view);

	/**
	 * Test the foreground on the viewer.
	 */
	@Test
	public void testForeground() throws PartInitException, CoreException,
			InterruptedException {

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		Assert.isNotNull(page, "No active page");

		final IViewPart view = openView(page);

		((DecoratorTestPart) view).setUpForDecorators();


		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(ForegroundColorDecorator.ID, true);

		Job.getJobManager().join(DecoratorManager.FAMILY_DECORATE, null);
		dispatchDuringUpdates((DecoratorTestPart) view);

		foregroundCheck(view);
		manager.setEnabled(ForegroundColorDecorator.ID, false);

	}

	/**
	 * Read and dispatch while updates are occuring
	 */
	private void dispatchDuringUpdates(DecoratorTestPart view) {
		view.readAndDispatchForUpdates();

	}

	/**
	 * Check the foreground colors.
	 */
	protected abstract void foregroundCheck(IViewPart view);

	protected abstract IViewPart openView(IWorkbenchPage page)
			throws PartInitException;

	/**
	 * Test the font on the viewer.
	 */
	@Test
	public void testFont() throws PartInitException, CoreException,
			InterruptedException {

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		Assert.isNotNull(page, "No active page");


		final IViewPart view = openView(page);
		((DecoratorTestPart) view).setUpForDecorators();


		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(FontDecorator.ID, true);

		Job.getJobManager().join(DecoratorManager.FAMILY_DECORATE, null);

		dispatchDuringUpdates((DecoratorTestPart) view);
		fontCheck(view);

		manager.setEnabled(FontDecorator.ID, false);

	}

	/**
	 * Check the fonts in the view
	 */
	protected abstract void fontCheck(IViewPart view);

	@Override
	protected void doTearDown() throws Exception {

		super.doTearDown();
		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(ForegroundColorDecorator.ID, false);
		manager.setEnabled(BackgroundColorDecorator.ID, false);
		manager.setEnabled(FontDecorator.ID, false);

	}
}
