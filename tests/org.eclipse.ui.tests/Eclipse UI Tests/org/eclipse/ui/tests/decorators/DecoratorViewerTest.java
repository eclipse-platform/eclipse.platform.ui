/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;

/**
 * DecoratorViewerTest is the abstract class of the tests for the viewers.
 */
public abstract class DecoratorViewerTest extends AbstractNavigatorTest {

	public static boolean tableHit = false;

	public static boolean treeHit = false;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param testName
	 */
	public DecoratorViewerTest(String testName) {
		super(testName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();
		ForegroundColorDecorator.setUpColor();
		BackgroundColorDecorator.setUpColor();
		FontDecorator.setUpFont();
	}

	/**
	 * Test the background on the viewer.
	 * 
	 * @throws PartInitException
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public void testBackground() throws PartInitException, CoreException,
			InterruptedException {

		BackgroundColorDecorator.setUpColor();

		final IViewPart view = openViewAndClearFlags();

		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(BackgroundColorDecorator.ID, true);

		JobManager.getInstance().join(DecoratorManager.FAMILY_DECORATE, null);

		dispatchDuringUpdates((DecoratorTestPart) view);
		backgroundCheck(view);
		manager.setEnabled(BackgroundColorDecorator.ID, false);

	}

	private IViewPart openViewAndClearFlags() throws PartInitException {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		Assert.isNotNull(page, "No active page");
		IViewPart view = openView(page);
		((DecoratorTestPart) view).clearFlags();
		tableHit = false;
		treeHit = false;
		return view;

	}

	/**
	 * Check the background colors in the view
	 * 
	 * @param view
	 */
	protected abstract void backgroundCheck(IViewPart view);

	/**
	 * Test the foreground on the viewer.
	 * 
	 * @throws PartInitException
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public void testForeground() throws PartInitException, CoreException,
			InterruptedException {

		final IViewPart view = openViewAndClearFlags();

		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(ForegroundColorDecorator.ID, true);

		JobManager.getInstance().join(DecoratorManager.FAMILY_DECORATE, null);
		dispatchDuringUpdates((DecoratorTestPart) view);

		foregroundCheck(view);
		manager.setEnabled(ForegroundColorDecorator.ID, false);

	}

	/**
	 * Read and dispatch while updates are occuring
	 * 
	 */
	private void dispatchDuringUpdates(DecoratorTestPart view) {

		long startTime = System.currentTimeMillis();
		while (!view.updateHappened) {
			Display.getCurrent().readAndDispatch();
			if (System.currentTimeMillis() - startTime < 10000) {
				if (Platform.inDebugMode()) {
					// After 10 seconds time out
					Assert.isTrue(false,
							"Update never arrived after 10 seconds");
				} else
					return;
			}

		}

	}

	/**
	 * Check the foreground colors.
	 * 
	 * @param view
	 */
	protected abstract void foregroundCheck(IViewPart view);

	/**
	 * @param page
	 * @return
	 * @throws PartInitException
	 */
	protected abstract IViewPart openView(IWorkbenchPage page)
			throws PartInitException;

	/**
	 * Test the font on the viewer.
	 * 
	 * @throws PartInitException
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public void testFont() throws PartInitException, CoreException,
			InterruptedException {

		final IViewPart view = openViewAndClearFlags();

		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(FontDecorator.ID, true);

		JobManager.getInstance().join(DecoratorManager.FAMILY_DECORATE, null);

		dispatchDuringUpdates((DecoratorTestPart) view);
		fontCheck(view);

		manager.setEnabled(FontDecorator.ID, false);

	}

	/**
	 * Check the fonts in the view
	 * 
	 * @param view
	 */
	protected abstract void fontCheck(IViewPart view);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.navigator.AbstractNavigatorTest#doTearDown()
	 */
	protected void doTearDown() throws Exception {

		super.doTearDown();
		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(ForegroundColorDecorator.ID, false);
		manager.setEnabled(BackgroundColorDecorator.ID, false);
		manager.setEnabled(FontDecorator.ID, false);

	}
}
