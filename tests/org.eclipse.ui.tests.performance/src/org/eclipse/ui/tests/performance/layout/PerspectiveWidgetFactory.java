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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.1
 */
public class PerspectiveWidgetFactory extends TestWidgetFactory {

    private String perspectiveId;
    private IWorkbenchWindow window;
    
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
    public void init() throws WorkbenchException {
    	// open the perspective in a new window
        window = PlatformUI.getWorkbench().openWorkbenchWindow(perspectiveId, UITestCase.getPageInput());
		IWorkbenchPage page = window.getActivePage();
        Assert.assertNotNull(page);
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
        return window.getShell();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.performance.layout.TestWidgetFactory#done()
     */
    public void done() throws CoreException, WorkbenchException {
    	window.close();
    	super.done();
    }
}
