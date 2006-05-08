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

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;

public class IPerspectiveListenerTest extends UITestCase implements
        IPerspectiveListener {
    private int fEvent;

    private IWorkbenchWindow fWindow;

    private IWorkbenchPage fPageMask;

    private IPerspectiveDescriptor fPerMask;

    /**
     * bit masks for events
     */
    public static final int NONE = 0x00, OPEN = 0x01, CLOSED = 0x02,
            ACTIVATED = 0x04, CHANGED = 0x08;

    public IPerspectiveListenerTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        fEvent = NONE;
        fWindow = openTestWindow();
        fWindow.addPerspectiveListener(this);
    }

    protected void doTearDown() throws Exception {
        fWindow.removePerspectiveListener(this);
        super.doTearDown();
    }

    public void testPerspectiveActivated() {
        /*
         * Commented out because until test case can be updated to work
         * with new window/page/perspective implementation
         * 
         fPageMask = fWindow.getActivePage();
         fPerMask = fWorkbench.getPerspectiveRegistry().findPerspectiveWithId(EmptyPerspective.PERSP_ID );
         fPageMask.setPerspective( fPerMask );
         
         assertEquals( isActivated( fEvent ), true );
         */
    }

    public void testPerspectiveChanged() {
        /*
         * Commented out because until test case can be updated to work
         * with new window/page/perspective implementation
         * 
         fPageMask = fWindow.getActivePage();
         fPerMask = fWorkbench.getPerspectiveRegistry().findPerspectiveWithId(EmptyPerspective.PERSP_ID );
         fPageMask.setPerspective( fPerMask );
         
         assertEquals( isActivated( fEvent ), true );
         */
    }

    /**
     * @see IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
     */
    public void perspectiveActivated(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {
        if (page == fPageMask && perspective == fPerMask)
            fEvent |= ACTIVATED;
    }

    /**
     * @see IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
     */
    public void perspectiveChanged(IWorkbenchPage page,
            IPerspectiveDescriptor perspective, String changeId) {
        if (page == fPageMask && perspective == fPerMask)
            fEvent |= CHANGED;
    }

    public static boolean isOpen(int bits) {
        return ((bits & OPEN) != 0);
    }

    public static boolean isClosed(int bits) {
        return ((bits & CLOSED) != 0);
    }

    public static boolean isActivated(int bits) {
        return ((bits & ACTIVATED) != 0);
    }

    public static boolean isChanged(int bits) {
        return ((bits & CLOSED) != 0);
    }
}
