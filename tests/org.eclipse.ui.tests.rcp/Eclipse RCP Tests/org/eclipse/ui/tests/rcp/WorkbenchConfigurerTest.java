/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.tests.harness.util.RCPTestWorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;

public class WorkbenchConfigurerTest extends TestCase {

    public WorkbenchConfigurerTest(String name) {
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
        WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {

            private IWorkbenchConfigurer configurer;

            public void initialize(IWorkbenchConfigurer c) {
                super.initialize(c);
                configurer = c;

                assertNotNull(c.getWorkbench());
                assertFalse(c.getSaveAndRestore());
                assertNotNull(c.getWorkbenchWindowManager());
            }

            public void postShutdown() {
                super.postShutdown();

                // *** This should be checked on all of the advisor callbacks
                //     but assume that if its still set in the last one, then it
                //     must have been set for all of them.
                assertFalse(configurer.emergencyClosing());
            }
        };

        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_OK, code);
    }

    public void testEmergencyClose() {
        WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(2) {

            private IWorkbenchConfigurer configurer;

            public void initialize(IWorkbenchConfigurer c) {
                super.initialize(c);
                configurer = c;

                assertNotNull(c.getWorkbench());
                assertFalse(c.getSaveAndRestore());
                assertNotNull(c.getWorkbenchWindowManager());
            }

            // emergencyClose as soon as possible
            public void eventLoopIdle(Display disp) {
                super.eventLoopIdle(disp);
                configurer.emergencyClose();
            }

            public void postShutdown() {
                super.postShutdown();

                // *** This should be checked on all of the advisor callbacks
                //     but assume that if its still set in the last one, then it
                //     must have been set for all of them.
                assertTrue(configurer.emergencyClosing());
            }
        };

        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_EMERGENCY_CLOSE, code);
    }
    

	// tests to ensure that all WorkbenchAdvisor API is called from the UI thread.
	public void testThreading() {
		final ArrayList results = new ArrayList();
		
		WorkbenchAdvisor advisor = new RCPTestWorkbenchAdvisor(1) {

			public void createWindowContents(
					IWorkbenchWindowConfigurer configurer, Shell shell) {
				ensureThread();
				super.createWindowContents(configurer, shell);
			}

			private void ensureThread() {
				if (Display.getCurrent() != null)
					return; 
				
				Exception e = new Exception();
				e.fillInStackTrace();
				results.add(e);
			}

			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
					IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				return super.createWorkbenchWindowAdvisor(configurer);
			}

			public void eventLoopException(Throwable exception) {
				ensureThread();
				super.eventLoopException(exception);
			}

			public void eventLoopIdle(Display display) {
				ensureThread();
				super.eventLoopIdle(display);
			}

			public void fillActionBars(IWorkbenchWindow window,
					IActionBarConfigurer configurer, int flags) {
				ensureThread();
				super.fillActionBars(window, configurer, flags);
			}

			public IAdaptable getDefaultPageInput() {
				ensureThread();
				return super.getDefaultPageInput();
			}

			public String getMainPreferencePageId() {
				ensureThread();
				return super.getMainPreferencePageId();
			}

			protected IWorkbenchConfigurer getWorkbenchConfigurer() {
				ensureThread();
				return super.getWorkbenchConfigurer();
			}

			public void initialize(IWorkbenchConfigurer configurer) {
				ensureThread();
				super.initialize(configurer);
			}

			public boolean isApplicationMenu(
					IWorkbenchWindowConfigurer configurer, String menuId) {
				ensureThread();
				return super.isApplicationMenu(configurer, menuId);
			}

			public void openIntro(IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				super.openIntro(configurer);
			}

			public boolean openWindows() {
				ensureThread();
				return super.openWindows();
			}

			public void postShutdown() {
				ensureThread();
				super.postShutdown();
			}

			public void postStartup() {
				ensureThread();
				super.postStartup();
			}

			public void postWindowClose(IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				super.postWindowClose(configurer);
			}

			public void postWindowCreate(IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				super.postWindowCreate(configurer);
			}

			public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				super.postWindowOpen(configurer);
			}

			public void postWindowRestore(IWorkbenchWindowConfigurer configurer)
					throws WorkbenchException {
				ensureThread();
				super.postWindowRestore(configurer);
			}

			public boolean preShutdown() {
				ensureThread();
				return super.preShutdown();
			}

			public void preStartup() {
				ensureThread();
				super.preStartup();
			}

			public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				super.preWindowOpen(configurer);
			}

			public boolean preWindowShellClose(
					IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				return super.preWindowShellClose(configurer);
			}

			public IStatus restoreState(IMemento memento) {
				ensureThread();
				return super.restoreState(memento);
			}

			public IStatus saveState(IMemento memento) {
				ensureThread();
				return super.saveState(memento);
			}

			public String getInitialWindowPerspectiveId() {
				//ensureThread();
				return null;
			}};
			
		int code = PlatformUI.createAndRunWorkbench(display, advisor);
		assertEquals(PlatformUI.RETURN_OK, code);
		
		if (!results.isEmpty()) {
			StringBuffer buffer = new StringBuffer("Advisor methods called from non-UI threads:\n");
			int count=0;
			for (Iterator i = results.iterator(); i.hasNext();) {
				Exception e = (Exception) i.next();
				StackTraceElement [] stack = e.getStackTrace();
				buffer.append("Failure ").append(++count).append('\n');
				for (int j = 1; j < Math.min(stack.length, 10); j++) {
					StackTraceElement stackTraceElement = stack[j];
					buffer.append(stackTraceElement.getClassName()).append('.')
							.append(stackTraceElement.getMethodName()).append(
									":").append(
									stackTraceElement.getLineNumber()).append(
									'\n');
				}
			}
			fail(buffer.toString());
		}

	}
}
