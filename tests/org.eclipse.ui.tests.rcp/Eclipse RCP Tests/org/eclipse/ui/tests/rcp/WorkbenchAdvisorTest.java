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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkbenchAdvisorTest {


	private Display display = null;

	@BeforeEach
	public void setUp() {
		assertNull(display);
		display = PlatformUI.createDisplay();
		assertNotNull(display);
	}

	@AfterEach
	public void tearDown() {
		assertNotNull(display);
		display.dispose();
		assertTrue(display.isDisposed());

	}

	/**
	 * The workbench should be created before any of the advisor's life cycle
	 * methods are called. #initialize is the first one called, so check that
	 * the workbench has been been created by then.
	 */
	@Test
	public void testEarlyGetWorkbench() {
		WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {

			@Override
			public void initialize(IWorkbenchConfigurer configurer) {
				super.initialize(configurer);
				assertNotNull(PlatformUI.getWorkbench());
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
	}

	@Test
	public void testTwoDisplays() {
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1);

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
		assertFalse(display.isDisposed());
		display.dispose();
		assertTrue(display.isDisposed());

		display = PlatformUI.createDisplay();
		assertNotNull(display);

		WorkbenchAdvisorObserver wa2 = new WorkbenchAdvisorObserver(1);

		int code2 = PlatformUI.createAndRunWorkbench(display, wa2);
		assertEquals(PlatformUI.RETURN_OK, code2);
	}

	@Test
	public void testTrivialOpenClose() {
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {

			private boolean windowOpenCalled = false;

			private boolean windowCloseCalled = false;

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.getWorkbench().addWindowListener(new IWindowListener() {

					@Override
					public void windowActivated(IWorkbenchWindow window) {
						// do nothing
					}

					@Override
					public void windowDeactivated(IWorkbenchWindow window) {
						// do nothing
					}

					@Override
					public void windowClosed(IWorkbenchWindow window) {
						windowCloseCalled = true;
					}

					@Override
					public void windowOpened(IWorkbenchWindow window) {
						windowOpenCalled = true;
					}
				});
			}

			@Override
			public void preWindowOpen(IWorkbenchWindowConfigurer c) {
				assertFalse(windowOpenCalled);
				super.preWindowOpen(c);
			}

			@Override
			public void postWindowOpen(IWorkbenchWindowConfigurer c) {
				assertTrue(windowOpenCalled);
				super.postWindowOpen(c);
			}

			@Override
			public boolean preWindowShellClose(IWorkbenchWindowConfigurer c) {
				assertFalse(windowCloseCalled);
				return super.preWindowShellClose(c);
			}

			@Override
			@SuppressWarnings("deprecation")
			public void postWindowClose(IWorkbenchWindowConfigurer c) {
				// Commented out since postWindowClose seems to be called before IWindowListener.windowClosed(IWorkbenchWindow)
				// assertTrue(windowCloseCalled);
				super.postWindowClose(c);
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);

		wa.resetOperationIterator();
		wa.assertNextOperation(WorkbenchAdvisorObserver.INITIALIZE);
		wa.assertNextOperation(WorkbenchAdvisorObserver.PRE_STARTUP);
		wa.assertNextOperation(WorkbenchAdvisorObserver.PRE_WINDOW_OPEN);
		wa.assertNextOperation(WorkbenchAdvisorObserver.FILL_ACTION_BARS);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_WINDOW_RESTORE);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_WINDOW_OPEN);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_STARTUP);
		wa.assertNextOperation(WorkbenchAdvisorObserver.PRE_SHUTDOWN);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_SHUTDOWN);
		wa.assertAllOperationsExamined();
	}

	@Test
	public void testTrivialRestoreClose() {
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.setSaveAndRestore(true);
			}

			@Override
			public void eventLoopIdle(Display d) {
				workbenchConfig.getWorkbench().restart();
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_RESTART, code);
		assertFalse(display.isDisposed());
		display.dispose();
		assertTrue(display.isDisposed());

		display = PlatformUI.createDisplay();
		WorkbenchAdvisorObserver wa2 = new WorkbenchAdvisorObserver(1) {

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				c.setSaveAndRestore(true);
			}
		};

		int code2 = PlatformUI.createAndRunWorkbench(display, wa2);
		assertEquals(PlatformUI.RETURN_OK, code2);

		wa2.resetOperationIterator();
		wa2.assertNextOperation(WorkbenchAdvisorObserver.INITIALIZE);
		wa2.assertNextOperation(WorkbenchAdvisorObserver.PRE_STARTUP);
		wa2.assertNextOperation(WorkbenchAdvisorObserver.PRE_WINDOW_OPEN);
		wa2.assertNextOperation(WorkbenchAdvisorObserver.FILL_ACTION_BARS);
		wa2.assertNextOperation(WorkbenchAdvisorObserver.POST_WINDOW_RESTORE);
		wa2.assertNextOperation(WorkbenchAdvisorObserver.POST_WINDOW_OPEN);
		wa2.assertNextOperation(WorkbenchAdvisorObserver.POST_STARTUP);
		wa2.assertNextOperation(WorkbenchAdvisorObserver.PRE_SHUTDOWN);
		wa2.assertNextOperation(WorkbenchAdvisorObserver.POST_SHUTDOWN);
		wa2.assertAllOperationsExamined();
	}

	/**
	 * The WorkbenchAdvisor comment for #postStartup says it is ok to close
	 * things from in there.
	 */
	@Test
	public void testCloseFromPostStartup() {

		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {
			@Override
			public void postStartup() {
				super.postStartup();
				assertTrue(PlatformUI.getWorkbench().close());
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);

		wa.resetOperationIterator();
		wa.assertNextOperation(WorkbenchAdvisorObserver.INITIALIZE);
		wa.assertNextOperation(WorkbenchAdvisorObserver.PRE_STARTUP);
		wa.assertNextOperation(WorkbenchAdvisorObserver.PRE_WINDOW_OPEN);
		wa.assertNextOperation(WorkbenchAdvisorObserver.FILL_ACTION_BARS);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_WINDOW_RESTORE);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_WINDOW_OPEN);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_STARTUP);
		wa.assertNextOperation(WorkbenchAdvisorObserver.PRE_SHUTDOWN);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_SHUTDOWN);
		wa.assertAllOperationsExamined();
	}

	@Test
	public void testEventLoopCrash() {
		WorkbenchAdvisorExceptionObserver wa = new WorkbenchAdvisorExceptionObserver();

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
		assertTrue(wa.exceptionCaught);
	}

	@Test
	public void testFillAllActionBar() {
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {

			@Override
			public void fillActionBars(IWorkbenchWindow window,
					IActionBarConfigurer configurer, int flags) {
				super.fillActionBars(window, configurer, flags);

				assertEquals(ActionBarAdvisor.FILL_COOL_BAR, flags & ActionBarAdvisor.FILL_COOL_BAR);
				assertEquals(ActionBarAdvisor.FILL_MENU_BAR, flags & ActionBarAdvisor.FILL_MENU_BAR);
				assertEquals(ActionBarAdvisor.FILL_STATUS_LINE, flags & ActionBarAdvisor.FILL_STATUS_LINE);
				assertEquals(0, flags & ActionBarAdvisor.FILL_PROXY);
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
	}

	@Test
	public void testEmptyProgressRegion() {
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {
			@Override
			public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
				super.preWindowOpen(configurer);
				configurer.setShowProgressIndicator(false);
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
	}

	@Test
	public void testShellClose() {
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver() {

			@Override
			public void eventLoopIdle(Display disp) {
				super.eventLoopIdle(disp);

				Shell[] shells = disp.getShells();
				for (Shell shell : shells) {
					if (shell != null && !shell.isDisposed()) {
						shell.close();
					}
				}
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);

		wa.resetOperationIterator();
		wa.assertNextOperation(WorkbenchAdvisorObserver.INITIALIZE);
		wa.assertNextOperation(WorkbenchAdvisorObserver.PRE_STARTUP);
		wa.assertNextOperation(WorkbenchAdvisorObserver.PRE_WINDOW_OPEN);
		wa.assertNextOperation(WorkbenchAdvisorObserver.FILL_ACTION_BARS);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_WINDOW_RESTORE);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_WINDOW_OPEN);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_STARTUP);
		wa.assertNextOperation(WorkbenchAdvisorObserver.PRE_WINDOW_SHELL_CLOSE);
		wa.assertNextOperation(WorkbenchAdvisorObserver.PRE_SHUTDOWN);
		wa.assertNextOperation(WorkbenchAdvisorObserver.POST_SHUTDOWN);
		wa.assertAllOperationsExamined();
	}
}

class WorkbenchAdvisorExceptionObserver extends WorkbenchAdvisorObserver {

	public boolean exceptionCaught = false;

	private RuntimeException runtimeException;

	public WorkbenchAdvisorExceptionObserver() {
		super(2);
	}

	// this is used to indicate when the event loop has started
	@Override
	public void eventLoopIdle(Display disp) {
		super.eventLoopIdle(disp);

		// only crash the loop one time
		if (runtimeException != null) {
			return;
		}

		runtimeException = new RuntimeException();
		throw runtimeException;
	}

	@Override
	public void eventLoopException(Throwable exception) {
		// *** Don't let the parent log the exception since it makes for
		// confusing
		//     test results.

		exceptionCaught = true;
		assertEquals(runtimeException, exception);
	}
}
