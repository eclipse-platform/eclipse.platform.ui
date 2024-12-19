/*******************************************************************************
 * Copyright (c) 2004, 2009, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 436344
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkbenchWindowConfigurerTest {


	private Display display = null;

	@BeforeEach
	public void setUp() throws Exception {
		assertNull(display);
		display = PlatformUI.createDisplay();
		assertNotNull(display);
	}

	@AfterEach
	public void tearDown() throws Exception {
		assertNotNull(display);
		display.dispose();
		assertTrue(display.isDisposed());

	}

	@Test
	public void testDefaults() {
		WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {

			@Override
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

	@Test
	public void test104558_T_T() throws Throwable {
		doTest104558(true, true);
	}

	@Test
	public void test104558_F_T() throws Throwable {
		doTest104558(false, true);
	}

	@Test
	public void test104558_T_F() throws Throwable {
		doTest104558(true, false);
	}

	@Test
	public void test104558_F_F() throws Throwable {
		doTest104558(false, false);
	}

	private void doTest104558(final boolean showPerspectiveBar, final boolean showCoolBar) {
		WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {
			@Override
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
					IWorkbenchWindowConfigurer workbenchWindowConfigurer) {
				return new WorkbenchWindowAdvisor(workbenchWindowConfigurer) {
					@Override
					public void preWindowOpen() {
						super.preWindowOpen();
						getWindowConfigurer().setShowCoolBar(showCoolBar);
						getWindowConfigurer().setShowPerspectiveBar(showPerspectiveBar);
					}

				};
			}

			@Override
			public void eventLoopIdle(Display disp) {
				IWorkbenchWindow activeWorkbenchWindow = getWorkbenchConfigurer()
						.getWorkbench().getActiveWorkbenchWindow();
				assertEquals(showCoolBar, ((WorkbenchWindow) activeWorkbenchWindow).getCoolBarVisible(),
						"testing showCoolBar=" + showCoolBar);
				assertEquals(showPerspectiveBar, ((WorkbenchWindow) activeWorkbenchWindow).getPerspectiveBarVisible(),
						"testing showPerspectiveBar=" + showPerspectiveBar);
				super.eventLoopIdle(disp);
			}

			@Override
			public void eventLoopException(Throwable exception) {
				throw new RuntimeException(exception);
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
	}


	// tests to ensure that all WorkbenchAdvisor API is called from the UI thread.
	@Test
	public void testThreading() {
		final ArrayList<String> results = new ArrayList<>();

		WorkbenchAdvisor advisor = new RCPTestWorkbenchAdvisor(1) {

			@Override
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
					IWorkbenchWindowConfigurer configurer) {
				return new WorkbenchWindowAdvisor(configurer) {

					private void ensureThread() {
						if (Display.getCurrent() != null) {
							return;
						}

						Exception e = new Exception();
						e.fillInStackTrace();
						StackTraceElement element = e.getStackTrace()[1];
						results.add(element.getClassName() + '.' + element.getMethodName());
					}

					@Override
					public ActionBarAdvisor createActionBarAdvisor(
							IActionBarConfigurer configurer) {
						ensureThread();

						return new ActionBarAdvisor(configurer) {

							@Override
							public void dispose() {
								ensureThread();
								super.dispose();
							}

							@Override
							protected void disposeAction(IAction action) {
								ensureThread();
								super.disposeAction(action);
							}

							@Override
							protected void disposeActions() {
								ensureThread();
								super.disposeActions();
							}

							@Override
							public void fillActionBars(int flags) {
								ensureThread();
								super.fillActionBars(flags);
							}

							@Override
							protected void fillCoolBar(ICoolBarManager coolBar) {
								ensureThread();
								super.fillCoolBar(coolBar);
							}

							@Override
							protected void fillMenuBar(IMenuManager menuBar) {
								ensureThread();
								super.fillMenuBar(menuBar);
							}

							@Override
							protected void fillStatusLine(
									IStatusLineManager statusLine) {
								ensureThread();
								super.fillStatusLine(statusLine);
							}

							@Override
							protected IAction getAction(String id) {
								ensureThread();
								return super.getAction(id);
							}

							@Override
							protected IActionBarConfigurer getActionBarConfigurer() {
								ensureThread();
								return super.getActionBarConfigurer();
							}

							@Override
							public boolean isApplicationMenu(String menuId) {
								ensureThread();
								return super.isApplicationMenu(menuId);
							}

							@Override
							protected void makeActions(IWorkbenchWindow window) {
								ensureThread();
								super.makeActions(window);
							}

							@Override
							protected void register(IAction action) {
								ensureThread();
								super.register(action);
							}

							@Override
							public IStatus restoreState(IMemento memento) {
								ensureThread();
								return super.restoreState(memento);
							}

							@Override
							public IStatus saveState(IMemento memento) {
								ensureThread();
								return super.saveState(memento);
							}};
					}

					@SuppressWarnings("removal")
					@Override
					public Control createEmptyWindowContents(Composite parent) {
						ensureThread();
						return super.createEmptyWindowContents(parent);
					}

					@SuppressWarnings("removal")
					@Override
					public void createWindowContents(Shell shell) {
						ensureThread();
						super.createWindowContents(shell);
					}

					@Override
					public void dispose() {
						ensureThread();
						super.dispose();
					}

					@Override
					protected IWorkbenchWindowConfigurer getWindowConfigurer() {
						ensureThread();
						return super.getWindowConfigurer();
					}

					@Override
					public void openIntro() {
						ensureThread();
						super.openIntro();
					}

					@Override
					public void postWindowClose() {
						ensureThread();
						super.postWindowClose();
					}

					@Override
					public void postWindowCreate() {
						ensureThread();
						super.postWindowCreate();
					}

					@Override
					public void postWindowOpen() {
						ensureThread();
						super.postWindowOpen();
					}

					@Override
					public void postWindowRestore() throws WorkbenchException {
						ensureThread();
						super.postWindowRestore();
					}

					@Override
					public void preWindowOpen() {
						ensureThread();
						super.preWindowOpen();
					}

					@Override
					public boolean preWindowShellClose() {
						ensureThread();
						return super.preWindowShellClose();
					}

					@Override
					public IStatus restoreState(IMemento memento) {
						ensureThread();
						return super.restoreState(memento);
					}

					@Override
					public IStatus saveState(IMemento memento) {
						ensureThread();
						return super.saveState(memento);
					}

				};
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, advisor);
		assertEquals(PlatformUI.RETURN_OK, code);

		if (!results.isEmpty()) {
			StringBuilder buffer = new StringBuilder("Window/action bar advisor methods called from non-UI threads:\n");
			for (String string : results) {
				buffer.append(string).append('\n');
			}
			fail(buffer.toString());
		}

	}
}
