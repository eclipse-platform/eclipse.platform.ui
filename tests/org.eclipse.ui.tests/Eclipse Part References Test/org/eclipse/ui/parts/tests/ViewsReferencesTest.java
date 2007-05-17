/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *******************************************************************************/
package org.eclipse.ui.parts.tests;

import junit.framework.TestCase;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.PrefUtil;
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
        createDisplay();
    }

	private void createDisplay() {
		display = PlatformUI.createDisplay();
	}

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        disposeDisplay();
        super.tearDown();
    }

	private void disposeDisplay() {
		display.dispose();
		display = null;
	}

    /**
     * Test that only view0's part has been created (ideally).
     *  
     */
    public void testActivePartView0() {
        openViews(0);
        newDisplay();
        checkViewsParts(0);
    }

    /**
     * Dispose of the old display and create a new one.
     */
	private void newDisplay() {
		disposeDisplay();
        createDisplay();
	}

    /**
     * Test that only view1's part has been created (ideally).
     *  
     */
    public void testActivePartView1() {
        openViews(1);
        newDisplay();
        checkViewsParts(1);

    }

    /**
     * Test that only view2's part has been created (ideally).
     *  
     */
    public void testActivePartView2() {
        openViews(2);
        newDisplay();
        checkViewsParts(2);
    }

    /**
     * Test that zooming view0 on start up and navigating to other views behaves
     * correcly.
     *  
     */
    public void testZoomActivePartView0() {
		// These tests are hard-wired to the pre-3.3 zoom behaviour
		// Run them anyway to ensure that we preserve the 3.0 mechanism
        IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
        boolean curMinMaxState = apiStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX);
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);
        
        openViews(0);
        newDisplay();
        zoomView(0);
        
        // Restore the previous state (just in case)
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, curMinMaxState);
    }

    /**
     * Test that zooming view1 on start up and navigating to other views behaves
     * correcly.
     *  
     */
    public void testZoomActivePartView1() {
		// These tests are hard-wired to the pre-3.3 zoom behaviour
		// Run them anyway to ensure that we preserve the 3.0 mechanism
        IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
        boolean curMinMaxState = apiStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX);
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);
        
        openViews(1);
        newDisplay();
        zoomView(1);
        
        // Restore the previous state (just in case)
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, curMinMaxState);
    }

    /**
     * Test that zooming view2 on start up and navigating to other views behaves
     * correcly.
     *  
     */
    public void testZoomActivePartView2() {
		// These tests are hard-wired to the pre-3.3 zoom behaviour
		// Run them anyway to ensure that we preserve the 3.0 mechanism
        IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
        boolean curMinMaxState = apiStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX);
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);
        
        openViews(2);
        newDisplay();
        zoomView(2);
        
        // Restore the previous state (just in case)
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, curMinMaxState);
    }

    /**
     * Open all the views.
     * 
     * @param lastViewToOpen
     *            The index of the last view to be opened.
     */
    private void openViews(final int lastViewToOpen) {
        PartsWorkbenchAdvisor wa = new PartsWorkbenchAdvisor() {
            protected void validate(IWorkbenchPage page) {
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
            protected void validate(IWorkbenchPage page) {
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
            protected void validate(IWorkbenchPage page) {
                IWorkbenchPartReference activePartReference = page
                        .getActivePartReference();
                String activePartReferenceId = activePartReference.getId();
                assertTrue(activePartReference instanceof IViewReference);
                assertEquals(activePartReferenceId, PartsTestUtil
                        .getView(viewIndex));

                IWorkbenchPart activePart = page.getActivePart();
                assertTrue(activePart instanceof IViewPart);

                PartsTestUtil.zoom(activePart);
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
                    PartsTestUtil.zoom(activePart);

            }
        };
        PlatformUI.createAndRunWorkbench(display, wa);
    }
}
