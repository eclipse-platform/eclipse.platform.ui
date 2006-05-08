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
package org.eclipse.ui.tests.api.workbenchpart;

import junit.framework.Assert;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.0
 */
public class RawIViewPartTest extends UITestCase {
    /**
     * @param testName
     */
    public RawIViewPartTest(String testName) {
        super(testName);
    }

    WorkbenchWindow window;

    WorkbenchPage page;

    RawIViewPart view;

    IWorkbenchPartReference ref;

    boolean titleChangeEvent = false;

    boolean nameChangeEvent = false;

    boolean contentChangeEvent = false;

    private IPropertyListener propertyListener = new IPropertyListener() {
        /* (non-Javadoc)
         * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
         */
        public void propertyChanged(Object source, int propId) {
            switch (propId) {
            case IWorkbenchPartConstants.PROP_TITLE:
                titleChangeEvent = true;
                break;
            case IWorkbenchPartConstants.PROP_PART_NAME:
                nameChangeEvent = true;
                break;
            case IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION:
                contentChangeEvent = true;
                break;
            }
        }
    };

    protected void doSetUp() throws Exception {
        super.doSetUp();
        window = (WorkbenchWindow) openTestWindow();
        page = (WorkbenchPage) window.getActivePage();
        view = (RawIViewPart) page
                .showView("org.eclipse.ui.tests.workbenchpart.RawIViewPart");
        ref = page
                .findViewReference("org.eclipse.ui.tests.workbenchpart.RawIViewPart");
        ref.addPropertyListener(propertyListener);
        titleChangeEvent = false;
        nameChangeEvent = false;
        contentChangeEvent = false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
     */
    protected void doTearDown() throws Exception {
        view.removePropertyListener(propertyListener);
        page.hideView(view);
        super.doTearDown();
    }

    private void verifySettings(IWorkbenchPart part, String expectedTitle,
            String expectedPartName, String expectedContentDescription)
            throws Exception {
        Assert.assertEquals("Incorrect view title", expectedTitle, part
                .getTitle());

        Assert.assertEquals("Incorrect title in view reference", expectedTitle,
                ref.getTitle());
        Assert.assertEquals("Incorrect part name in view reference",
                expectedPartName, ref.getPartName());
        Assert.assertEquals("Incorrect content description in view reference",
                expectedContentDescription, ref.getContentDescription());
    }

    private void verifySettings(String expectedTitle, String expectedPartName,
            String expectedContentDescription) throws Exception {
        verifySettings(view, expectedTitle, expectedPartName,
                expectedContentDescription);
    }

    /**
     * Ensure that we've received the given property change events since the start of the test
     * 
     * @param titleEvent PROP_TITLE
     * @param nameEvent PROP_PART_NAME
     * @param descriptionEvent PROP_CONTENT_DESCRIPTION
     */
    private void verifyEvents(boolean titleEvent, boolean nameEvent,
            boolean descriptionEvent) {
        if (titleEvent) {
            Assert.assertEquals("Missing title change event", titleEvent,
                    titleChangeEvent);
        }
        if (nameEvent) {
            Assert.assertEquals("Missing name change event", nameEvent,
                    nameChangeEvent);
        }
        if (descriptionEvent) {
            Assert.assertEquals("Missing content description event",
                    descriptionEvent, contentChangeEvent);
        }
    }

    public void testDefaults() throws Throwable {
        verifySettings("SomeTitle", "RawIViewPart", "SomeTitle");
        verifyEvents(false, false, false);
    }

    public void testCustomTitle() throws Throwable {
        view.setTitle("CustomTitle");
        verifySettings("CustomTitle", "RawIViewPart", "CustomTitle");
        verifyEvents(true, false, true);
    }

    /**
     * Ensures that the content description is empty when the title is the same
     * as the default part name
     */
    public void testEmptyContentDescription() throws Throwable {
        view.setTitle("RawIViewPart");
        verifySettings("RawIViewPart", "RawIViewPart", "");
        verifyEvents(true, false, true);
    }
}
