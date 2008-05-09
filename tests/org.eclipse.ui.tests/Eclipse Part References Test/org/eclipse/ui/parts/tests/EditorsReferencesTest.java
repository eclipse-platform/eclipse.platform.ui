/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.parts.tests.util.PartsTestUtil;
import org.eclipse.ui.parts.tests.util.PartsWorkbenchAdvisor;

/**
 * Test case to ensure that editor references are created when neededed. Also
 * ensures that zooming behaves correctly on start up (Bug 64043).
 */
public class EditorsReferencesTest extends TestCase {

    private Display display;

    /**
     * Constructor.
     * 
     * @param testName
     *            The test's name.
     */
    public EditorsReferencesTest(String testName) {
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
     * Test that only file0's part has been created (ideally).
     *  
     */
    public void testActivePartFile0() {
        openEditors(0);
        newDisplay();
        checkEditorsParts(0);

    }

    /**
     * Test that only file1's part has been created (ideally).
     *  
     */
    public void testActivePartFile1() {
        openEditors(1);
        newDisplay();
        checkEditorsParts(1);

    }

    /**
     * Test that only file2's part has been created (ideally).
     *  
     */
    public void testActivePartFile2() {
        openEditors(2);
        newDisplay();
        checkEditorsParts(2);

    }

    /**
     * Test that zooming file0 on start up and navigating to other editors
     * behaves correcly.
     *  
     */
    public void testZoomActivePartFile0() {
		// These tests are hard-wired to the pre-3.3 zoom behaviour
		// Run them anyway to ensure that we preserve the 3.0 mechanism
        IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
        boolean curMinMaxState = apiStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX);
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);
        
        openEditors(0);
        newDisplay();
        zoomEditor(0);
        
        // Restore the previous state (just in case)
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, curMinMaxState);
    }

    /**
     * Test that zooming file1 on start up and navigating to other editors
     * behaves correcly.
     *  
     */
    public void testZoomActivePartFile1() {
		// These tests are hard-wired to the pre-3.3 zoom behaviour
		// Run them anyway to ensure that we preserve the 3.0 mechanism
        IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
        boolean curMinMaxState = apiStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX);
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);
        
        openEditors(1);
        newDisplay();
        zoomEditor(1);
        
        // Restore the previous state (just in case)
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, curMinMaxState);
    }

    /**
     * Test that zooming file2 on start up and navigating to other editors
     * behaves correcly.
     *  
     */
    public void testZoomActivePartFile2() {
		// These tests are hard-wired to the pre-3.3 zoom behaviour
		// Run them anyway to ensure that we preserve the 3.0 mechanism
        IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
        boolean curMinMaxState = apiStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX);
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);
        
        openEditors(2);
        newDisplay();
        zoomEditor(2);
        
        // Restore the previous state (just in case)
        apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, curMinMaxState);
    }

    /**
     * Open all the editors.
     * 
     * @param lastFileToOpen
     *            The index of the last file to be opened.
     */
    private void openEditors(final int lastFileToOpen) {
        PartsWorkbenchAdvisor wa = new PartsWorkbenchAdvisor() {
            protected void validate(IWorkbenchPage page) {
                for (int index = 0; index < PartsTestUtil.numOfParts; index++) {
                    if (index != lastFileToOpen)
                        PartsTestUtil.openEditor(PartsTestUtil
                                .getFileName(index), page);
                }
                PartsTestUtil.openEditor(PartsTestUtil
                        .getFileName(lastFileToOpen), page);
                assertEquals(page.getEditorReferences().length,
                        PartsTestUtil.numOfParts);
                assertEquals(page.getActiveEditor().getTitle(), PartsTestUtil
                        .getFileName(lastFileToOpen));

            }
        };
        PlatformUI.createAndRunWorkbench(display, wa);

    }

    /**
     * Check that the active editor's part has been created.
     * 
     * @param lastFileOpened
     *            The active file's index.
     */
    private void checkEditorsParts(final int lastFileOpened) {

        PartsWorkbenchAdvisor wa = new PartsWorkbenchAdvisor() {
            protected void validate(IWorkbenchPage page) {
                String activeFileName = PartsTestUtil
                        .getFileName(lastFileOpened);
                assertEquals(page.getEditorReferences().length,
                        PartsTestUtil.numOfParts);
                assertTrue(page.getActivePart() instanceof IEditorPart);
                IEditorPart activeEditorPart = page.getActiveEditor();
                assertEquals(activeEditorPart.getTitle(), activeFileName);
                IEditorReference[] editorReferences = page
                        .getEditorReferences();
                int numActiveParts = 0;
                for (int index = 0; index < editorReferences.length; index++) {
                    if (editorReferences[index].getEditor(false) != null)
                        numActiveParts++;

                }
                //TODO: Ideally, the number of active parts would be 1
                assertTrue(numActiveParts <= 2);
            }
        };

        PlatformUI.createAndRunWorkbench(display, wa);

    }

    /**
     * Zoom the active editor and navigate to the other open editors.
     * 
     * @param editorIndex
     *            The active editor's index.
     */
    public void zoomEditor(final int editorIndex) {
        PartsWorkbenchAdvisor wa = new PartsWorkbenchAdvisor() {
            protected void validate(IWorkbenchPage page) {
                IWorkbenchPartReference activePartReference = page
                        .getActivePartReference();
                String activePartReferenceTitle = activePartReference
                        .getTitle();
                assertTrue(activePartReference instanceof IEditorReference);
                assertEquals(activePartReferenceTitle, PartsTestUtil
                        .getFileName(editorIndex));

                IWorkbenchPart activePart = page.getActivePart();
                assertTrue(activePart instanceof IEditorPart);

                PartsTestUtil.zoom(activePart);
                assertTrue(PartsTestUtil.isZoomed(activePart));

                IEditorReference[] editorReferences = page
                        .getEditorReferences();
                String currentEditorTitle = null;
                for (int index = 0; index < editorReferences.length; index++) {
                    currentEditorTitle = editorReferences[index].getTitle();
                    if (!currentEditorTitle.equals(activePartReferenceTitle)) {
                        PartsTestUtil.openEditor(currentEditorTitle, page);
                        activePartReferenceTitle = currentEditorTitle;
                    }
                }

                activePart = page.getActivePart();
                assertTrue(activePart instanceof IEditorPart);

                if (PartsTestUtil.isZoomed(activePart))
                    PartsTestUtil.zoom(activePart);

            }
        };
        PlatformUI.createAndRunWorkbench(display, wa);
    }

    /**
     * Dispose of the old display and create a new one.
     */
	private void newDisplay() {
		disposeDisplay();
        createDisplay();
	}
}
