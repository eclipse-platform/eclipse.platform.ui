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
package org.eclipse.ui.tests.performance.layout;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.1
 */
public class ViewWidgetFactory extends TestWidgetFactory {

    private String viewId;
    private Control ctrl;
    
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
        final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
        final IPerspectiveDescriptor perspective1 = registry.findPerspectiveWithId("org.eclipse.ui.tests.util.EmptyPerspective");

        Assert.assertNotNull(perspective1);

		// Open a file.
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		activePage.setPerspective(perspective1);
		
		IViewPart part = activePage.showView(viewId, null, WorkbenchPage.VIEW_ACTIVATE);

		ctrl = getControl(part);
    }
    
    public void done() throws CoreException, WorkbenchException {
        
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
}
