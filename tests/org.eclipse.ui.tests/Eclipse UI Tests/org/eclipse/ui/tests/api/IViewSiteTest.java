/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

public class IViewSiteTest extends IWorkbenchPartSiteTest {

    /**
     * Constructor for IViewPartSiteTest
     */
    public IViewSiteTest(String testName) {
        super(testName);
    }

    /**
     * @see IWorkbenchPartSiteTest#getTestPartName()
     */
    protected String getTestPartName() throws Throwable {
        return MockViewPart.NAME;
    }

    /**
     * @see IWorkbenchPartSiteTest#getTestPartId()
     */
    protected String getTestPartId() throws Throwable {
        return MockViewPart.ID;
    }

    /**
     * @see IWorkbenchPartSiteTest#createTestPart(IWorkbenchPage)
     */
    protected IWorkbenchPart createTestPart(IWorkbenchPage page)
            throws Throwable {
        return page.showView(MockViewPart.ID);
    }

    public void testGetActionBars() throws Throwable {
        // From Javadoc: "Returns the action bars for this part site."

        IViewPart view = (IViewPart) createTestPart(fPage);
        IViewSite site = view.getViewSite();
        assertNotNull(site.getActionBars());
    }

}

