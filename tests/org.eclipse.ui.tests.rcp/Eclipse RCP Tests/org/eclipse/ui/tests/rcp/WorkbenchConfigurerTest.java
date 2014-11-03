/*******************************************************************************
 * Copyright (c) 2004, 2009, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 436344
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class WorkbenchConfigurerTest {


    private Display display = null;

	@Before
	public void setUp() {

        assertNull(display);
        display = PlatformUI.createDisplay();
        assertNotNull(display);
    }

	@After
	public void tearDown() {
        assertNotNull(display);
        display.dispose();
        assertTrue(display.isDisposed());

    }

	@Ignore
	@Test
	public void testDefaults() {
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {

			private IWorkbenchConfigurer configurer;

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				configurer = c;

				assertNotNull(c.getWorkbench());
				assertFalse(c.getSaveAndRestore());
				assertNotNull(c.getWorkbenchWindowManager());
			}

			@Override
			public void postShutdown() {
				super.postShutdown();

				// *** This should be checked on all of the advisor callbacks
				// but assume that if its still set in the last one, then it
				// must have been set for all of them.
				assertFalse(configurer.emergencyClosing());
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
	}

	@Ignore
	@Test
	public void testEmergencyClose() {
		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(2) {

			private IWorkbenchConfigurer configurer;

			@Override
			public void initialize(IWorkbenchConfigurer c) {
				super.initialize(c);
				configurer = c;

				assertNotNull(c.getWorkbench());
				assertFalse(c.getSaveAndRestore());
				assertNotNull(c.getWorkbenchWindowManager());
			}

			// emergencyClose as soon as possible
			@Override
			public void eventLoopIdle(Display disp) {
				super.eventLoopIdle(disp);
				configurer.emergencyClose();
			}

			@Override
			public void postShutdown() {
				super.postShutdown();

				// *** This should be checked on all of the advisor callbacks
				// but assume that if its still set in the last one, then it
				// must have been set for all of them.
				assertTrue(configurer.emergencyClosing());
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_EMERGENCY_CLOSE, code);
	}


	// tests to ensure that all WorkbenchAdvisor API is called from the UI thread.
	@Test
	public void testThreading() {
		final ArrayList<Exception> results = new ArrayList<Exception>();

		WorkbenchAdvisor advisor = new RCPTestWorkbenchAdvisor(1) {

			@Override
			public void createWindowContents(
					IWorkbenchWindowConfigurer configurer, Shell shell) {
				ensureThread();
				super.createWindowContents(configurer, shell);
			}

			private void ensureThread() {
				if (Display.getCurrent() != null) {
					return;
				}

				Exception e = new Exception();
				e.fillInStackTrace();
				results.add(e);
			}

			@Override
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
					IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				return super.createWorkbenchWindowAdvisor(configurer);
			}

			@Override
			public void eventLoopException(Throwable exception) {
				ensureThread();
				super.eventLoopException(exception);
			}

			@Override
			public void eventLoopIdle(Display display) {
				ensureThread();
				super.eventLoopIdle(display);
			}

			@Override
			public void fillActionBars(IWorkbenchWindow window,
					IActionBarConfigurer configurer, int flags) {
				ensureThread();
				super.fillActionBars(window, configurer, flags);
			}

			@Override
			public IAdaptable getDefaultPageInput() {
				ensureThread();
				return super.getDefaultPageInput();
			}

			@Override
			public String getMainPreferencePageId() {
				ensureThread();
				return super.getMainPreferencePageId();
			}

			@Override
			protected IWorkbenchConfigurer getWorkbenchConfigurer() {
				ensureThread();
				return super.getWorkbenchConfigurer();
			}

			@Override
			public void initialize(IWorkbenchConfigurer configurer) {
				ensureThread();
				super.initialize(configurer);
			}

			@Override
			public boolean isApplicationMenu(
					IWorkbenchWindowConfigurer configurer, String menuId) {
				ensureThread();
				return super.isApplicationMenu(configurer, menuId);
			}

			@Override
			public void openIntro(IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				super.openIntro(configurer);
			}

			@Override
			public boolean openWindows() {
				ensureThread();
				return super.openWindows();
			}

			@Override
			public void postShutdown() {
				ensureThread();
				super.postShutdown();
			}

			@Override
			public void postStartup() {
				ensureThread();
				super.postStartup();
			}

			@Override
			public void postWindowClose(IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				super.postWindowClose(configurer);
			}

			@Override
			public void postWindowCreate(IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				super.postWindowCreate(configurer);
			}

			@Override
			public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				super.postWindowOpen(configurer);
			}

			@Override
			public void postWindowRestore(IWorkbenchWindowConfigurer configurer)
					throws WorkbenchException {
				ensureThread();
				super.postWindowRestore(configurer);
			}

			@Override
			public boolean preShutdown() {
				ensureThread();
				return super.preShutdown();
			}

			@Override
			public void preStartup() {
				ensureThread();
				super.preStartup();
			}

			@Override
			public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				super.preWindowOpen(configurer);
			}

			@Override
			public boolean preWindowShellClose(
					IWorkbenchWindowConfigurer configurer) {
				ensureThread();
				return super.preWindowShellClose(configurer);
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

			@Override
			public String getInitialWindowPerspectiveId() {
				//ensureThread();
				return null;
			}};

		int code = PlatformUI.createAndRunWorkbench(display, advisor);
		assertEquals(PlatformUI.RETURN_OK, code);

		if (!results.isEmpty()) {
			StringBuffer buffer = new StringBuffer("Advisor methods called from non-UI threads:\n");
			int count=0;
			for (Exception e : results) {
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
