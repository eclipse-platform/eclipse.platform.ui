/******************************************************************************* 
 * Copyright (c) 2003, 2004 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation 
 *******************************************************************************/
package org.eclipse.ui.parts.tests;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.parts.tests.util.PartsTestUtil;
import org.eclipse.ui.parts.tests.util.PartsWorkbenchAdvisor;

/**
 * Test case to ensure that view references are created when neededed. Also
 * ensures that zooming behaves correctly on start up (Bug 64043).
 */
public class ViewsReferencesTest extends TestCase {

	private Display display;

	/**
	 * Constructor.
	 * 
	 * @param testName
	 *            The test's name.
	 */
	public ViewsReferencesTest(String testName) {
		super(testName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		display = PlatformUI.createDisplay();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		display.dispose();
		super.tearDown();
	}

	/**
	 * Test that only view0's part has been created (ideally).
	 *  
	 */
	public void testActivePartView0() {
		openViews(0);
		checkViewsParts(0);

	}

	/**
	 * Test that only view1's part has been created (ideally).
	 *  
	 */
	public void testActivePartView1() {
		openViews(1);
		checkViewsParts(1);

	}

	/**
	 * Test that only view2's part has been created (ideally).
	 *  
	 */
	public void testActivePartView2() {
		openViews(2);
		checkViewsParts(2);
	}

	/**
	 * Test that zooming view0 on start up and navigating to other views behaves
	 * correcly.
	 *  
	 */
	public void testZoomActivePartView0() {
		openViews(0);
		zoomView(0);
	}

	/**
	 * Test that zooming view1 on start up and navigating to other views behaves
	 * correcly.
	 *  
	 */
	public void testZoomActivePartView1() {
		openViews(1);
		zoomView(1);
	}

	/**
	 * Test that zooming view2 on start up and navigating to other views behaves
	 * correcly.
	 *  
	 */
	public void testZoomActivePartView2() {
		openViews(2);
		zoomView(2);
	}

	/**
	 * Open all the views.
	 * 
	 * @param lastViewToOpen
	 *            The index of the last view to be opened.
	 */
	private void openViews(final int lastViewToOpen) {
		PartsWorkbenchAdvisor wa = new PartsWorkbenchAdvisor() {
			public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
				super.postWindowOpen(configurer);
				IWorkbenchPage page = configurer.getWindow().getActivePage();
				try {
					for (int index = 0; index < PartsTestUtil.numOfParts; index++) {
						if (index != lastViewToOpen)
							page.showView(PartsTestUtil.getView(index));

					}
					page.showView(PartsTestUtil.getView(lastViewToOpen));
				} catch (PartInitException e) {
					e.printStackTrace(System.err);
				}
				assertEquals(page.getViewReferences().length,
						PartsTestUtil.numOfParts);
				assertTrue(page.getActivePart() instanceof IViewPart);
				IViewPart activePart = (IViewPart) page.getActivePart();
				assertEquals(activePart.getViewSite().getId(), PartsTestUtil
						.getView(lastViewToOpen));

			}
		};
		PlatformUI.createAndRunWorkbench(display, wa);

	}

	/**
	 * Check that the active view's part has been created.
	 * 
	 * @param lastViewOpened
	 *            The active file's index.
	 */
	private void checkViewsParts(final int lastViewOpened) {

		PartsWorkbenchAdvisor wa = new PartsWorkbenchAdvisor() {
			public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
				super.postWindowOpen(configurer);

				IWorkbenchPage page = configurer.getWindow().getActivePage();
				String activeViewId = PartsTestUtil.getView(lastViewOpened);
				assertEquals(page.getViewReferences().length,
						PartsTestUtil.numOfParts);
				assertTrue(page.getActivePart() instanceof IViewPart);

				IViewPart activeViewPart = (IViewPart) page.getActivePart();
				assertEquals(activeViewPart.getViewSite().getId(), activeViewId);

				IViewReference[] viewReferences = page.getViewReferences();
				int numActiveParts = 0;
				for (int index = 0; index < viewReferences.length; index++) {
					if (viewReferences[index].getView(false) != null)
						numActiveParts++;

				}
				// TODO: Ideally, the number of active parts would be 1
				assertTrue(numActiveParts <= 2);
			}
		};

		PlatformUI.createAndRunWorkbench(display, wa);
	}

	/**
	 * Zoom the active view and navigate to the other open views.
	 * 
	 * @param viewIndex
	 *            The active view's index.
	 */
	public void zoomView(final int viewIndex) {
		PartsWorkbenchAdvisor wa = new PartsWorkbenchAdvisor() {
			public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
				super.postWindowOpen(configurer);

				IWorkbenchPage page = configurer.getWindow().getActivePage();
				IWorkbenchPartReference activePartReference = page
						.getActivePartReference();
				String activePartReferenceId = activePartReference.getId();
				assertTrue(activePartReference instanceof IViewReference);
				assertEquals(activePartReferenceId, PartsTestUtil
						.getView(viewIndex));

				IWorkbenchPart activePart = page.getActivePart();
				assertTrue(activePart instanceof IViewPart);

				PartsTestUtil.zoom(activePart, (WorkbenchPage) page);
				assertTrue(PartsTestUtil.isZoomed(activePart));

				IViewReference[] viewReferences = page.getViewReferences();
				String currentViewId = null;

				for (int index = 0; index < viewReferences.length; index++) {
					currentViewId = viewReferences[index].getId();
					if (!currentViewId.equals(activePartReferenceId)) {
						try {
							page.showView(currentViewId);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
						activePartReferenceId = currentViewId;
					}
				}

				activePart = page.getActivePart();
				assertTrue(activePart instanceof IViewPart);

				if (PartsTestUtil.isZoomed(activePart))
					PartsTestUtil.zoom(activePart, (WorkbenchPage) page);

			}
		};
		PlatformUI.createAndRunWorkbench(display, wa);
	}
}
