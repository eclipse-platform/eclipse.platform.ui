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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.harness.util.RCPTestWorkbenchAdvisor;
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

	public void test104558_T_T() throws Throwable {
		doTest104558(true, true);
	}
	
	public void test104558_F_T() throws Throwable {
		doTest104558(false, true);
	}
	
	public void test104558_T_F() throws Throwable {
		doTest104558(true, false);
	}
	
	public void test104558_F_F() throws Throwable {
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
	

	// tests to ensure that all WorkbenchAdvisor API is called from the UI thread.
	public void testThreading() {
		final ArrayList results = new ArrayList();
		
		WorkbenchAdvisor advisor = new RCPTestWorkbenchAdvisor(1) {

			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
					IWorkbenchWindowConfigurer configurer) {
				WorkbenchWindowAdvisor advisor = new WorkbenchWindowAdvisor(configurer) {

					private void ensureThread() {
						if (Display.getCurrent() != null)
							return; 
						
						Exception e = new Exception();
						e.fillInStackTrace();
						StackTraceElement element = e.getStackTrace()[1];
						results.add(element.getClassName() + '.' + element.getMethodName());
					}
					
					public ActionBarAdvisor createActionBarAdvisor(
							IActionBarConfigurer configurer) {
						ensureThread();
						
						ActionBarAdvisor advisor = new ActionBarAdvisor(configurer) {

							public void dispose() {
								ensureThread();
								super.dispose();
							}

							protected void disposeAction(IAction action) {
								ensureThread();
								super.disposeAction(action);
							}

							protected void disposeActions() {
								ensureThread();
								super.disposeActions();
							}

							public void fillActionBars(int flags) {
								ensureThread();
								super.fillActionBars(flags);
							}

							protected void fillCoolBar(ICoolBarManager coolBar) {
								ensureThread();
								super.fillCoolBar(coolBar);
							}

							protected void fillMenuBar(IMenuManager menuBar) {
								ensureThread();
								super.fillMenuBar(menuBar);
							}

							protected void fillStatusLine(
									IStatusLineManager statusLine) {
								ensureThread();
								super.fillStatusLine(statusLine);
							}

							protected IAction getAction(String id) {
								ensureThread();
								return super.getAction(id);
							}

							protected IActionBarConfigurer getActionBarConfigurer() {
								ensureThread();
								return super.getActionBarConfigurer();
							}

							public boolean isApplicationMenu(String menuId) {
								ensureThread();
								return super.isApplicationMenu(menuId);
							}

							protected void makeActions(IWorkbenchWindow window) {
								ensureThread();
								super.makeActions(window);
							}

							protected void register(IAction action) {
								ensureThread();
								super.register(action);
							}

							public IStatus restoreState(IMemento memento) {
								ensureThread();
								return super.restoreState(memento);
							}

							public IStatus saveState(IMemento memento) {
								ensureThread();
								return super.saveState(memento);
							}};
						return advisor;
					}

					public Control createEmptyWindowContents(Composite parent) {
						ensureThread();
						return super.createEmptyWindowContents(parent);
					}

					public void createWindowContents(Shell shell) {
						ensureThread();
						super.createWindowContents(shell);
					}

					public void dispose() {
						ensureThread();
						super.dispose();
					}

					protected IWorkbenchWindowConfigurer getWindowConfigurer() {
						ensureThread();
						return super.getWindowConfigurer();
					}

					public void openIntro() {
						ensureThread();
						super.openIntro();
					}

					public void postWindowClose() {
						ensureThread();
						super.postWindowClose();
					}

					public void postWindowCreate() {
						ensureThread();
						super.postWindowCreate();
					}

					public void postWindowOpen() {
						ensureThread();
						super.postWindowOpen();
					}

					public void postWindowRestore() throws WorkbenchException {
						ensureThread();
						super.postWindowRestore();
					}

					public void preWindowOpen() {
						ensureThread();
						super.preWindowOpen();
					}

					public boolean preWindowShellClose() {
						ensureThread();
						return super.preWindowShellClose();
					}

					public IStatus restoreState(IMemento memento) {
						ensureThread();
						return super.restoreState(memento);
					}

					public IStatus saveState(IMemento memento) {
						ensureThread();
						return super.saveState(memento);
					}
					
				};
				return advisor;
			}
		};
			
		int code = PlatformUI.createAndRunWorkbench(display, advisor);
		assertEquals(PlatformUI.RETURN_OK, code);
		
		if (!results.isEmpty()) {
			StringBuffer buffer = new StringBuffer("Window/action bar advisor methods called from non-UI threads:\n");
			for (Iterator i = results.iterator(); i.hasNext();) {
				String string = (String) i.next();
				buffer.append(string).append('\n');
			}
			fail(buffer.toString());
		}

	}
}
