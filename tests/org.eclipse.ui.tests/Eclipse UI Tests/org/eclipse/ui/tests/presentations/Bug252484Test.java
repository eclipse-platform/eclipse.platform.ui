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

package org.eclipse.ui.tests.presentations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.testing.IWorkbenchPartTestable;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test for Bug 252484. The scenario being tested is a FastView which
 * gets closed in one perspective would leave the same view disabled
 * in any other in which it was visible.
 * 
 * @since 3.5
 */
public final class Bug252484Test extends UITestCase {

    /**
     * Constructs a new instance of this test case.
     * 
     * @param testName
     *            The name of the test
     */
    public Bug252484Test(final String testName) {
        super(testName);
    }
    
    /**
     * Test for Bug 252484. The scenario being tested is a FastView which
     * gets closed in one perspective would leave the same view disabled
     * in any other in which it was visible.
     * 
     * @since 3.5
     */
    public void testFastViewClose() throws CoreException {
        // Open a new test window onto the Java Perspective
        final IWorkbenchWindow window = openTestWindow("org.eclipse.jdt.ui.JavaPerspective");
        assertNotNull(window);

        WorkbenchPage page = (WorkbenchPage) window.getActivePage();
        IPerspectiveDescriptor javaPersp = page.getPerspective();

        // Ensure that the view is showing
        page.showView("org.eclipse.jdt.ui.PackageExplorer");
        
        // Now open the Debug perspective
		IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor debugPersp = reg.findPerspectiveWithLabel("Debug");
        
        page.setPerspective(debugPersp);

        // Show the package explorer
        page.showView("org.eclipse.jdt.ui.PackageExplorer");
        IViewReference pkgExp = page.findViewReference("org.eclipse.jdt.ui.PackageExplorer");
        
        // Now, make it a fast view and then hide it (this induced the defect)
        page.addFastView(pkgExp);
        page.hideView(pkgExp);
        
        // Switch back to the Java perspective and ensure that the explorer is still enabled
        page.setPerspective(javaPersp);
        pkgExp = page.findViewReference("org.eclipse.jdt.ui.PackageExplorer");
        
        IWorkbenchPartSite site = pkgExp.getPart(true).getSite();
        IWorkbenchPartTestable testable = (IWorkbenchPartTestable) site.getAdapter(IWorkbenchPartTestable.class);
        Control ctrl = testable.getControl();
        assertTrue("The view's control should be enabled", ctrl.isEnabled());
    }
}
