/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.misc.Assert;

/**
 * DecoratorViewerTest is the abstract class of the tests for 
 * the viewers.
 */
public abstract class DecoratorViewerTest extends AbstractNavigatorTest {

	/**
	 * Create a new instance of the receiver.
	 * @param testName
	 */
	public DecoratorViewerTest(String testName) {
		super(testName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();

	}

	/**
	 * Test the background on the viewer.
	 * @throws PartInitException
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public void testBackground() throws PartInitException, CoreException, InterruptedException {

		BackgroundColorDecorator.setUpColor();

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		Assert.isNotNull(page, "No active page");

		final IViewPart view = openView(page);

		IDecoratorManager manager = WorkbenchPlugin.getDefault().getDecoratorManager();
		manager.setEnabled(BackgroundColorDecorator.ID, true);

		JobManager.getInstance().join(DecoratorManager.FAMILY_DECORATE, null);

		backgroundCheck(view);

		manager.setEnabled(BackgroundColorDecorator.ID, false);

	}

	/**
	 * Check the background colors in the view
	 * @param view
	 */
	protected abstract void backgroundCheck(IViewPart view);

	/**
	 * Test the foreground on the viewer.
	 * @throws PartInitException
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public void testForeground() throws PartInitException, CoreException, InterruptedException {

		ForegroundColorDecorator.setUpColor();

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		Assert.isNotNull(page, "No active page");

		final IViewPart view = openView(page);

		IDecoratorManager manager = WorkbenchPlugin.getDefault().getDecoratorManager();
		manager.setEnabled(ForegroundColorDecorator.ID, true);

		JobManager.getInstance().join(DecoratorManager.FAMILY_DECORATE, null);

		foregroundCheck(view);

		manager.setEnabled(ForegroundColorDecorator.ID, false);

	}

	/**
	 * Check the foreground colors.
	 * @param view
	 */
	protected abstract void foregroundCheck(IViewPart view);

	/**
	 * @param page
	 * @return
	 * @throws PartInitException
	 */
	protected abstract IViewPart openView(IWorkbenchPage page) throws PartInitException ;

	/**
	 * Test the font on the viewer.
	 * @throws PartInitException
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public void testFont() throws PartInitException, CoreException, InterruptedException {

		FontDecorator.setUpFont();

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		Assert.isNotNull(page, "No active page");

		final IViewPart view = openView(page);

		IDecoratorManager manager = WorkbenchPlugin.getDefault().getDecoratorManager();
		manager.setEnabled(FontDecorator.ID, true);

		JobManager.getInstance().join(DecoratorManager.FAMILY_DECORATE, null);

		fontCheck(view);
		manager.setEnabled(FontDecorator.ID, false);

	}

	/**
	 * Check the fonts in the view
	 * @param view
	 */
	protected abstract void fontCheck(IViewPart view);
}
