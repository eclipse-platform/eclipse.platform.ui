/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.ui.tests.progress;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.progress.IProgressService;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that the E4
 * {@link IProgressService#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)}
 * can be called from a thread that is not the UI thread.
 */
public class E4ProgressServiceOffUIThreadTest extends AbstractProgressServiceOffUIThreadTest {

	private IProgressService progressService;

	@BeforeEach
	public void setUp() {
		IEclipseContext context = PlatformUI.getWorkbench().getService(IEclipseContext.class);
		progressService = context.get(IProgressService.class);
		assertNotNull(progressService, "E4 IProgressService not available");
	}

	@Override
	protected IRunnableContext getRunContext() {
		return progressService;
	}

	/** A nested E4 run(fork=true) inside an E3 worker must stay in that worker. */
	@Test
	public void testRun_fork_nestedE4InE3_runsInOuterWorkerThread() throws Exception {
		assertNestedRunsInOuterThread(PlatformUI.getWorkbench().getProgressService(), progressService);
	}

	/** A nested E3 run(fork=true) inside an E4 worker must stay in that worker. */
	@Test
	public void testRun_fork_nestedE3InE4_runsInOuterWorkerThread() throws Exception {
		assertNestedRunsInOuterThread(progressService, PlatformUI.getWorkbench().getProgressService());
	}

	private void assertNestedRunsInOuterThread(IRunnableContext outerService, IRunnableContext innerService)
			throws InterruptedException {
		AtomicReference<Thread> outerThread = new AtomicReference<>();
		AtomicReference<Thread> innerThread = new AtomicReference<>();
		AtomicReference<Throwable> thrown = new AtomicReference<>();
		IRunnableWithProgress inner = _ -> innerThread.set(Thread.currentThread());
		IRunnableWithProgress outer = _ -> {
			outerThread.set(Thread.currentThread());
			innerService.run(true, false, inner);
		};
		Thread caller = new Thread(() -> {
			try {
				outerService.run(true, false, outer);
			} catch (Throwable t) {
				thrown.set(t);
			}
		}, "E4ProgressServiceOffUIThreadTest-non-UI-caller");
		caller.setDaemon(true);
		caller.start();
		Display display = PlatformUI.getWorkbench().getDisplay();
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
		while (caller.isAlive() && System.nanoTime() < deadline) {
			if (!display.readAndDispatch()) {
				Thread.sleep(5);
			}
		}
		if (caller.isAlive()) {
			caller.interrupt();
			throw new AssertionError("Nested run() did not return in time");
		}
		if (thrown.get() != null) {
			throw new AssertionError("Unexpected exception from non-UI thread", thrown.get());
		}
		assertNotNull(innerThread.get(), "Inner runnable was not executed");
		assertSame(outerThread.get(), innerThread.get(), "Nested run(fork=true) must not fork again");
	}
}
