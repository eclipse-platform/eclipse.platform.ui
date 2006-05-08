/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;

/**
 * DecoratorViewerTest is the abstract class of the tests for the viewers.
 */
public abstract class DecoratorViewerTest extends AbstractNavigatorTest {

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

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		Assert.isNotNull(page, "No active page");

		final IViewPart view = openView(page);
		((DecoratorTestPart) view).setUpForDecorators();
		

		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(BackgroundColorDecorator.ID, true);
		
		Platform.getJobManager().join(DecoratorManager.FAMILY_DECORATE, null);

		dispatchDuringUpdates((DecoratorTestPart) view);
		backgroundCheck(view);
		manager.setEnabled(BackgroundColorDecorator.ID, false);

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

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		Assert.isNotNull(page, "No active page");

		final IViewPart view = openView(page);

		((DecoratorTestPart) view).setUpForDecorators();
		

		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(ForegroundColorDecorator.ID, true);
		
		Platform.getJobManager().join(DecoratorManager.FAMILY_DECORATE, null);
		dispatchDuringUpdates((DecoratorTestPart) view);

		foregroundCheck(view);
		manager.setEnabled(ForegroundColorDecorator.ID, false);

	}

	/**
	 * Read and dispatch while updates are occuring
	 * 
	 */
	private void dispatchDuringUpdates(DecoratorTestPart view) {
		view.readAndDispatchForUpdates();

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

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		Assert.isNotNull(page, "No active page");

		
		final IViewPart view = openView(page);
		((DecoratorTestPart) view).setUpForDecorators();
		

		IDecoratorManager manager = WorkbenchPlugin.getDefault()
				.getDecoratorManager();
		manager.setEnabled(FontDecorator.ID, true);
		
		Platform.getJobManager().join(DecoratorManager.FAMILY_DECORATE, null);

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
