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
package org.eclipse.ui.tests.performance.layout;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * @since 3.1
 */
public class ViewWidgetFactory extends TestWidgetFactory {

    private String viewId;
    private Control ctrl;
    private IWorkbenchWindow window;
    
    public ViewWidgetFactory(String viewId) {
        this.viewId = viewId;
        Assert.assertNotNull(viewId);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.TestWidgetFactory#getMaxSize()
     */
    public Point getMaxSize() {
        return new Point(1024, 768);
    }

    public static Composite getControl(IViewPart part) {
		ViewSite site = (ViewSite)part.getSite();
		
		PartPane pane = site.getPane();
        
		return (Composite)pane.getControl();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.TestWidgetFactory#init()
     */
    public void init() throws CoreException, WorkbenchException {
    	// open the view in a new window
        window = PlatformUI.getWorkbench().openWorkbenchWindow(EmptyPerspective.PERSP_ID, UITestCase.getPageInput());
		IWorkbenchPage page = window.getActivePage();
        Assert.assertNotNull(page);

		IViewPart part = page.showView(viewId, null, IWorkbenchPage.VIEW_ACTIVATE);
        
        BasicPerformanceTest.waitForBackgroundJobs();
        
		ctrl = getControl(part);
        
        Point size = getMaxSize();
        ctrl.setBounds(0,0,size.x, size.y);
        window.getShell().setSize(size);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.TestWidgetFactory#getName()
     */
    public String getName() {
        return "View " + viewId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.TestWidgetFactory#getControl()
     */
    public Composite getControl() throws CoreException, WorkbenchException {
        return (Composite)ctrl;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.layout.TestWidgetFactory#done()
     */
    public void done() throws CoreException, WorkbenchException {
    	window.close();
    	super.done();
    }
    
}
