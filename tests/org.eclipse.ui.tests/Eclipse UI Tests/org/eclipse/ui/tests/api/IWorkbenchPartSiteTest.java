/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests the site for an IWorkbenchPart.
 */
public abstract class IWorkbenchPartSiteTest extends UITestCase {

    protected IWorkbenchWindow fWindow;

    protected IWorkbenchPage fPage;

    /**
     * Constructor for IWorkbenchPartSiteTest
     */
    public IWorkbenchPartSiteTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        fWindow = openTestWindow();
        fPage = fWindow.getActivePage();
    }

    public void testGetId() throws Throwable {
        // From Javadoc: "Returns the part registry extension id for 
        // this workbench site's part."

        IWorkbenchPart part = createTestPart(fPage);
        IWorkbenchPartSite site = part.getSite();
        assertEquals(getTestPartId(), site.getId());
    }

    public void testGetPage() throws Throwable {
        // From Javadoc: "Returns the page containing this workbench 
        // site's part."

        IWorkbenchPart part = createTestPart(fPage);
        IWorkbenchPartSite site = part.getSite();
        assertEquals(fPage, site.getPage());
    }

    public void testGetPluginId() throws Throwable {
        // From Javadoc: "Returns the unique identifier of the 
        // plug-in that defines this workbench site's part."

        IWorkbenchPart part = createTestPart(fPage);
        IWorkbenchPartSite site = part.getSite();
        assertEquals(getTestPartPluginId(), site.getPluginId());
    }

    public void testGetRegisteredName() throws Throwable {
        // From Javadoc: "Returns the registered name for this 
        // workbench site's part."

        IWorkbenchPart part = createTestPart(fPage);
        IWorkbenchPartSite site = part.getSite();
        assertEquals(getTestPartName(), site.getRegisteredName());
    }

    public void testGetShell() throws Throwable {
        // From Javadoc: "Returns the shell containing this 
        // workbench site's part"

        IWorkbenchPart part = createTestPart(fPage);
        IWorkbenchPartSite site = part.getSite();
        assertEquals(fWindow.getShell(), site.getShell());
    }

    public void testGetWorkbenchWindow() throws Throwable {
        // From Javadoc: "Returns the workbench window 
        // containing this workbench site's part."

        IWorkbenchPart part = createTestPart(fPage);
        IWorkbenchPartSite site = part.getSite();
        assertEquals(fWindow, site.getWorkbenchWindow());
    }

    public void testGetSelectionProvider() throws Throwable {
        // From Javadoc: "'Get' returns the selection provider 
        // for this workbench site's part.  

        IWorkbenchPart part = createTestPart(fPage);
        IWorkbenchPartSite site = part.getSite();
        assertTrue(part instanceof MockWorkbenchPart);
        MockWorkbenchPart mock = (MockWorkbenchPart) part;
        assertEquals(mock.getSelectionProvider(), site.getSelectionProvider());
    }

    public void testSetSelectionProvider() throws Throwable {
        // From Javadoc: 'Set' sets the selection provider.

        // Set selection provider to null.
        IWorkbenchPart part = createTestPart(fPage);
        IWorkbenchPartSite site = part.getSite();
        site.setSelectionProvider(null);
        assertNull(site.getSelectionProvider());

        // Set selection provider to real.
        MockSelectionProvider provider = new MockSelectionProvider();
        site.setSelectionProvider(provider);
        assertEquals(provider, site.getSelectionProvider());
    }
    
    public void testINestableService() throws Throwable {
    	IWorkbenchPart part = createTestPart(fPage);
    	IWorkbenchPartSite site = part.getSite();
    	DummyService service = (DummyService) site.getService(DummyService.class);

    	assertTrue(service.isActive());
    	if(part instanceof IViewPart)
    		fPage.hideView((IViewPart) part);
    	else
    		fPage.closeEditor((IEditorPart) part, false);
    	assertFalse(service.isActive());
    	
    }
    

    /**
     * Creates a test part in the page.	
     */
    abstract protected IWorkbenchPart createTestPart(IWorkbenchPage page)
            throws Throwable;

    /**
     * Returns the expected id for a test part.
     */
    abstract protected String getTestPartId() throws Throwable;

    /**
     * Returns the expected name for a test part.
     */
    abstract protected String getTestPartName() throws Throwable;

    /**
     * Returns the expected id for a test part plugin.  Subclasses may
     * override this.
     */
    protected String getTestPartPluginId() throws Throwable {
        return "org.eclipse.ui.tests";
    }
}

