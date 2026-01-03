/*******************************************************************************
 * Copyright (c) 2016, 2026 InterSystems Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vasili Gulevich                   - Bug 501404
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IDEWorkbenchAdvisorTest {
	private static final String PLUGIN_ID = "org.eclipse.ui.ide.application.tests";
	private Display display = null;
	private ISchedulingRule rule;

	private static final class SaveHook implements ISaveParticipant, Closeable {
		public ISaveContext saving = null;
		public ISaveContext rollback = null;
		public ISaveContext prepareToSave = null;
		public ISaveContext doneSaving = null;
		public final IWorkspace workspace = ResourcesPlugin.getWorkspace();

		public SaveHook() throws CoreException {
			workspace.addSaveParticipant(PLUGIN_ID, this);
		}

		@Override
		public void saving(ISaveContext context) {
			saving = context;
		}

		@Override
		public void rollback(ISaveContext context) {
			rollback = context;
		}

		@Override
		public void prepareToSave(ISaveContext context) {
			prepareToSave = context;
		}

		@Override
		public void doneSaving(ISaveContext context) {
			doneSaving = context;
		}

		@Override
		public void close() {
			workspace.removeSaveParticipant(PLUGIN_ID);
		}
	}

	@BeforeEach
	public void setUp() {
		assertNull(display);
		display = PlatformUI.createDisplay();
		assertNotNull(display);
		rule = ResourcesPlugin.getWorkspace().getRoot();
	}

	@AfterEach
	public void tearDown() throws Exception {
		try {
			Job.getJobManager().endRule(rule);
		} catch (IllegalArgumentException e) {
			// ignore, if the rule is not held by this thread
		}
		dispatchDisplay();
		display.dispose();
		assertTrue(display.isDisposed());
		display = null;

	}

	/**
	 * Workbench shutdown should not deadlock when invoked from workspace
	 * operation
	 *
	 * Regression test for bug 501404 Timeout annotation parameter can't be
	 * used, as it makes test to be executed in non-UI thread.
	 */
	@Test
	public void testShutdownWithLockedWorkspace() throws CoreException {
		try (SaveHook saveHook = new SaveHook()) {
			IDEWorkbenchAdvisor advisor = new IDEWorkbenchAdvisor() {
				@Override
				public void postStartup() {
					super.postStartup();
					display.asyncExec(() -> {
						Job.getJobManager().beginRule(rule, null);
						try {
							PlatformUI.getWorkbench().close();
						} finally {
							Job.getJobManager().endRule(rule);
						}
					});
				}
			};

			int returnCode = PlatformUI.createAndRunWorkbench(display, advisor);
			assertEquals(PlatformUI.RETURN_OK, returnCode);
			dispatchDisplay();

			assertNotNull(saveHook.prepareToSave);
			assertNotNull(saveHook.saving);
			assertNotNull(saveHook.prepareToSave);
			assertNotNull(saveHook.doneSaving);
			assertNull(saveHook.rollback);
		}
	}

	/**
	 * Workbench shutdown should not deadlock when invoked from workspace
	 * operation
	 *
	 * Regression test for bug 501404 Timeout annotation parameter can't be
	 * used, as it makes test to be executed in non-UI thread.
	 */
	@Test
	public void testShutdownWithForeverLockedWorkspace() throws CoreException {
		AtomicInteger logs = new AtomicInteger();
		final int expectedLogs = 4;
		ILogListener listener = (status, plugin) -> {
			if (status.getMessage().equals(IDEWorkbenchMessages.ProblemsSavingWorkspace)) {
				logs.addAndGet(1);
			}
		};
		try (SaveHook saveHook = new SaveHook()) {
			IDEWorkbenchPlugin.getDefault().getLog().addLogListener(listener);
			IDEWorkbenchAdvisor advisor = new IDEWorkbenchAdvisor(100, null) {
				@Override
				public void postStartup() {
					super.postStartup();

					display.asyncExec(() -> {
						Job.getJobManager().beginRule(rule, null);
						PlatformUI.getWorkbench().close();
						// no end rule
					});
				}

				@Override
				public void postShutdown() {
					super.postShutdown();
					long start = System.currentTimeMillis();
					long stop = start + 5_000;
					while (logs.get() < expectedLogs && System.currentTimeMillis() < stop) {
						try {
							dispatchDisplay();
							Thread.sleep(50);
						} catch (Exception e) {
							// ignore
						}
					}
				}
			};

			int returnCode = PlatformUI.createAndRunWorkbench(display, advisor);
			assertEquals(PlatformUI.RETURN_OK, returnCode);
			dispatchDisplay();

			assertNull(saveHook.prepareToSave);
			assertNull(saveHook.saving);
			assertNull(saveHook.prepareToSave);
			assertNull(saveHook.doneSaving);
			assertNull(saveHook.rollback);
			String message = "IDEWorkbenchAdvisor did not properly reported failed disconnect";
			assertEquals(expectedLogs, logs.get(), message);
		} finally {
			IDEWorkbenchPlugin.getDefault().getLog().removeLogListener(listener);
		}
	}

	/**
	 * Workbench shutdown should disconnect workspace if it is not locked
	 *
	 * Regression test for bug 501404 Timeout annotation parameter can't be
	 * used, as it makes test to be executed in non-UI thread.
	 */
	@Test
	public void testShutdownWithUnlockedWorkspace() throws CoreException {
		try (SaveHook saveHook = new SaveHook()) {
			IDEWorkbenchAdvisor advisor = new IDEWorkbenchAdvisor() {
				@Override
				public void postStartup() {
					super.postStartup();
					display.asyncExec(() -> {
						PlatformUI.getWorkbench().close();
					});
				}
			};
			int returnCode = PlatformUI.createAndRunWorkbench(display, advisor);
			assertEquals(PlatformUI.RETURN_OK, returnCode);
			dispatchDisplay();

			assertNotNull(saveHook.prepareToSave);
			assertNotNull(saveHook.saving);
			assertNotNull(saveHook.prepareToSave);
			assertNotNull(saveHook.doneSaving);
			assertNull(saveHook.rollback);
		}

	}

	/**
	 * Process display events until there are none left
	 */
	private void dispatchDisplay() {
		while (display.readAndDispatch()) {
		}
	}

}
