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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.1
 */
public class PerspectiveWidgetFactory extends TestWidgetFactory {

    private String perspectiveId;
    
    public PerspectiveWidgetFactory(String initialPerspective) {
        perspectiveId = initialPerspective;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.TestWidgetFactory#getMaxSize()
     */
    public Point getMaxSize() {
        return new Point(1024, 768);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.TestWidgetFactory#init()
     */
    public void init() {
        final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
        final IPerspectiveDescriptor perspective1 = registry.findPerspectiveWithId(perspectiveId);

        Assert.assertNotNull("Unknown perspective id: " + perspectiveId 
                + " (probably indicates a bug in the test suite)", perspective1);

		// Open a file.
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		activePage.setPerspective(perspective1);
    }
    
    public void done() {
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.TestWidgetFactory#getName()
     */
    public String getName() {
        return "Perspective " + perspectiveId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.TestWidgetFactory#getControl()
     */
    public Composite getControl() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

}
