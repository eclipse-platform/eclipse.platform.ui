/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;

public class WorkbenchWindowConfigurerTest extends TestCase {

    public WorkbenchWindowConfigurerTest(String name) {
        super(name);
    }

    private Display display = null;

    protected void setUp() throws Exception {
        super.setUp();

        assertNull(display);
        display = PlatformUI.createDisplay();
        assertNotNull(display);
    }

    protected void tearDown() throws Exception {
        assertNotNull(display);
        display.dispose();
        assertTrue(display.isDisposed());

        super.tearDown();
    }

    public void testDefaults() {
        WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {

            public void fillActionBars(IWorkbenchWindow window,
                    IActionBarConfigurer actionBarConfig, int flags) {
                super.fillActionBars(window, actionBarConfig, flags);

                String tempTitle = "title"; //$NON-NLS-1$

                IWorkbenchWindowConfigurer windowConfig = workbenchConfig
                        .getWindowConfigurer(window);
                assertNotNull(windowConfig);

                assertEquals(window, windowConfig.getWindow());
                assertEquals(workbenchConfig, windowConfig
                        .getWorkbenchConfigurer());
                assertEquals(actionBarConfig, windowConfig
                        .getActionBarConfigurer());
                assertNotNull(windowConfig.getTitle());
                assertTrue(windowConfig.getShowCoolBar());
                assertTrue(windowConfig.getShowMenuBar());
                assertFalse(windowConfig.getShowPerspectiveBar());
                assertTrue(windowConfig.getShowStatusLine());

                windowConfig.setTitle(tempTitle);
                windowConfig.setShowCoolBar(false);
                windowConfig.setShowMenuBar(false);
                windowConfig.setShowPerspectiveBar(true);
                windowConfig.setShowStatusLine(false);
                assertEquals(tempTitle, windowConfig.getTitle());
                assertFalse(windowConfig.getShowCoolBar());
                assertFalse(windowConfig.getShowMenuBar());
                assertTrue(windowConfig.getShowPerspectiveBar());
                assertFalse(windowConfig.getShowStatusLine());

                // *** title is orginally null, but cannot set it back to null,
                // should that
                //     check be allowed?
                windowConfig.setTitle("");//$NON-NLS-1$
                windowConfig.setShowCoolBar(true);
                windowConfig.setShowMenuBar(true);
                windowConfig.setShowPerspectiveBar(false);
                windowConfig.setShowStatusLine(true);
            }
        };

        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_OK, code);
    }

	public void test104558() throws Throwable {
		doTest104558(true, true);
		doTest104558(false, true);
		doTest104558(true, false);
		doTest104558(false, false);
	}

	private void doTest104558(final boolean showPerspectiveBar, final boolean showCoolBar) {
		WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
					IWorkbenchWindowConfigurer workbenchWindowConfigurer) {
				return new WorkbenchWindowAdvisor(workbenchWindowConfigurer) {
					public void preWindowOpen() {
						super.preWindowOpen();
						getWindowConfigurer().setShowCoolBar(showCoolBar);
						getWindowConfigurer().setShowPerspectiveBar(showPerspectiveBar);
					}
					public void createWindowContents(Shell shell) {
						IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
						configurer.createPageComposite(shell);
					}
				};
			}
			
			public void eventLoopIdle(Display disp) {
				IWorkbenchWindow activeWorkbenchWindow = getWorkbenchConfigurer()
						.getWorkbench().getActiveWorkbenchWindow();
				assertEquals("testing showCoolBar=" + showCoolBar, showCoolBar, ((WorkbenchWindow)activeWorkbenchWindow).getCoolBarVisible());
				assertEquals("testing showPerspectiveBar=" + showPerspectiveBar, showPerspectiveBar, ((WorkbenchWindow)activeWorkbenchWindow).getPerspectiveBarVisible());
				super.eventLoopIdle(disp);
			}
			
			public void eventLoopException(Throwable exception) {
				throw new RuntimeException(exception);
			}
		};
	
		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
	}
    
    
}
