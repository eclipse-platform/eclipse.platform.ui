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
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IStickyViewDescriptor;
import org.eclipse.ui.tests.util.UITestCase;


/**
 * @since 3.0
 */
public class StickyViewTest extends UITestCase {

    /**
     * @param testName
     */
    public StickyViewTest(String testName) {
        super(testName);
    }

    /**
     * Tests to ensure that sticky views are opened in the same stack.
     */
    public void testStackPlacement() {
        IWorkbenchWindow window = openTestWindow();
        IWorkbenchPage page = window.getActivePage();
        
        try {
            IViewPart part1 = page.showView("org.eclipse.ui.tests.api.StickyView1");
            assertNotNull(part1);
            IViewPart part2 = page.showView("org.eclipse.ui.tests.api.StickyView2");
            assertNotNull(part2);
            IViewPart [] stack = page.getViewStack(part1);

            assertTrue(findInStack(stack, part1));
            assertTrue(findInStack(stack, part2));
            
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }
    
    /**
     * Tests to ensure that all views in a stack with a known sticky view are also sticky.
     */
    public void testStackContents() {
        IWorkbenchWindow window = openTestWindow();
        IWorkbenchPage page = window.getActivePage();
        
        try {
            IViewPart part1 = page.showView("org.eclipse.ui.tests.api.StickyView1");
            assertNotNull(part1);

            IViewPart [] stack = page.getViewStack(part1);
            
            for (int i = 0; i < stack.length; i++) {
                assertTrue(stack[i].getTitle(), isSticky(stack[i]));
            }            
        } catch (PartInitException e) {
            fail(e.getMessage());
        }        
    }

    private boolean isSticky(IViewPart part) {
        String id = part.getSite().getId();
        IStickyViewDescriptor [] descs = WorkbenchPlugin.getDefault().getViewRegistry().getStickyViews();
        for (int i = 0; i < descs.length; i++) {
            if (descs[i].getId().equals(id))
                return true;
        }
        return false;        
    }

    /**
     * Sticky views should remain after perspective reset.
     */
    public void testPerspectiveReset() {
        IWorkbenchWindow window = openTestWindow();   
        IWorkbenchPage page = window.getActivePage();
        try {
            page.showView("org.eclipse.ui.tests.api.StickyView1");
            page.resetPerspective();
            assertNotNull(page.findView("org.eclipse.ui.tests.api.StickyView1"));
        } catch (PartInitException e) {
            fail(e.getMessage());
        }        
    }
    
    /**
     * Tests that a sticky view is opened in successive perspectives.
     */
    public void testPerspectiveOpen() {
        IWorkbenchWindow window = openTestWindow();
        IWorkbenchPage page = window.getActivePage();
        
        try {
            page.showView("org.eclipse.ui.tests.api.StickyView1");
            page.setPerspective(WorkbenchPlugin.getDefault().getPerspectiveRegistry().findPerspectiveWithId("org.eclipse.ui.tests.api.SessionPerspective"));            
            assertNotNull(page.findView("org.eclipse.ui.tests.api.StickyView1"));
        } catch (PartInitException e) {
            fail(e.getMessage());
        }
    }
    
    private boolean findInStack(IViewPart[] stack, IViewPart target) {

        for (int i = 0; i < stack.length; i++) {
            if (stack[i] == target)
                return true;                
        }
        return false;        
    }
}
