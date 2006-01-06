/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.intro;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.tests.api.PerspectiveWithFastView;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.0
 */
public class IntroTest extends UITestCase {

    IWorkbenchWindow window = null;

    private IntroDescriptor oldDesc;

    /**
     * @param testName
     */
    public IntroTest(String testName) {
        super(testName);
    }
    
    public void testCloseInFastViewPerspective() {
    	testClose(PerspectiveWithFastView.PERSP_ID);
	}

    public void testCloseInEmptyPerspective() {
    	testClose(EmptyPerspective.PERSP_ID);
	}

    public void testCloseInNonEmptyPerspective() {
    	testClose("org.eclipse.ui.resourcePerspective");
    }

    private void testClose(String perspectiveId) {
		IPerspectiveDescriptor descriptor = window.getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(
						perspectiveId);
		window.getActivePage().setPerspective(descriptor);

		IIntroManager introManager = window.getWorkbench().getIntroManager();
		IIntroPart part = introManager.showIntro(window, false);
		introManager.closeIntro(part);

		assertTrue(((WorkbenchWindow) window).getCoolBarVisible());
		assertTrue(((WorkbenchWindow) window).getPerspectiveBarVisible());
	}
    
    public void testShow() {
        IIntroManager introManager = window.getWorkbench().getIntroManager();
        IIntroPart part = introManager.showIntro(window, false);
        assertNotNull(part);
        assertFalse(introManager.isIntroStandby(part));
        introManager.closeIntro(part);
        assertNull(introManager.getIntro());

        part = introManager.showIntro(window, true);
        assertNotNull(part);
        assertTrue(introManager.isIntroStandby(part));
        assertTrue(introManager.closeIntro(part));
        assertNull(introManager.getIntro());
    }

    public void testStandby() {
        IWorkbench workbench = window.getWorkbench();
        IIntroPart part = workbench.getIntroManager().showIntro(window, false);
        assertNotNull(part);
        assertFalse(workbench.getIntroManager().isIntroStandby(part));
        workbench.getIntroManager().setIntroStandby(part, true);
        assertTrue(workbench.getIntroManager().isIntroStandby(part));
        assertTrue(workbench.getIntroManager().closeIntro(part));
        assertNull(workbench.getIntroManager().getIntro());
    }

    /**
     * Open the intro, change perspective, close the intro (ensure it still 
     * exists), change back to the first perspective, close the intro, ensure 
     * that it no longer exists.
     */
    public void testPerspectiveChange() {
        IWorkbench workbench = window.getWorkbench();
        IIntroPart part = workbench.getIntroManager().showIntro(window, false);
        assertNotNull(part);
        IWorkbenchPage activePage = window.getActivePage();
        IPerspectiveDescriptor oldDesc = activePage.getPerspective();
        activePage.setPerspective(WorkbenchPlugin.getDefault()
                .getPerspectiveRegistry().findPerspectiveWithId(
                        "org.eclipse.ui.tests.api.SessionPerspective"));
        assertFalse(workbench.getIntroManager().closeIntro(part));
        assertNotNull(workbench.getIntroManager().getIntro());

        activePage.setPerspective(oldDesc);
        assertTrue(workbench.getIntroManager().closeIntro(part));
        assertNull(workbench.getIntroManager().getIntro());
    }

    public void testPerspectiveReset() {
        IWorkbench workbench = window.getWorkbench();
        IIntroPart part = workbench.getIntroManager().showIntro(window, false);
        assertNotNull(part);
        window.getActivePage().resetPerspective();
        part = workbench.getIntroManager().getIntro();
        assertNotNull(part);
        assertFalse(workbench.getIntroManager().isIntroStandby(part));

        workbench.getIntroManager().setIntroStandby(part, true);
        window.getActivePage().resetPerspective();
        part = workbench.getIntroManager().getIntro();
        assertNotNull(part);
        assertTrue(workbench.getIntroManager().isIntroStandby(part));
        assertTrue(workbench.getIntroManager().closeIntro(part));
        assertNull(workbench.getIntroManager().getIntro());
    }
    
    /**
	 * Test to ensure that the part is properly nulled out when the intro is
	 * closed via the view close mechanism.
	 */
	public void testViewClosure() {
		IWorkbench workbench = window.getWorkbench();
		IIntroPart part = workbench.getIntroManager().showIntro(window, false);
		assertNotNull(part);
		IViewPart viewPart = window.getActivePage().findView(
				IIntroConstants.INTRO_VIEW_ID);
		assertNotNull(viewPart);
		window.getActivePage().hideView(viewPart);
		assertNull(workbench.getIntroManager().getIntro());
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
     */
    protected void doSetUp() throws Exception {
        super.doSetUp();
        oldDesc = Workbench.getInstance().getIntroDescriptor();
        IntroDescriptor testDesc = (IntroDescriptor) WorkbenchPlugin
                .getDefault().getIntroRegistry().getIntro(
                        "org.eclipse.ui.testintro");
        Workbench.getInstance().setIntroDescriptor(testDesc);
        window = openTestWindow();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
     */
    protected void doTearDown() throws Exception {
        super.doTearDown();
        Workbench.getInstance().setIntroDescriptor(oldDesc);
    }
}
