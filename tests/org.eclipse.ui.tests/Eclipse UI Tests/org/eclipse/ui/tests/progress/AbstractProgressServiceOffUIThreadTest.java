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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@code run(boolean, boolean, IRunnableWithProgress)} of a progress
 * service can be called from a thread that is not the UI thread. The subclasses
 * provide the progress service (E3 or E4) to test.
 */
public abstract class AbstractProgressServiceOffUIThreadTest {

	private static final long TIMEOUT_MILLIS = 30_000;

	/** Long enough for a progress dialog to appear if one was opened. */
	private static final long LONG_OPERATION_MILLIS = 2_000;

	/** Returns the progress service to test. */
	protected abstract IRunnableContext getRunContext();

	/**
	 * Runs {@code action} on a new non-UI thread and returns what it threw, or
	 * {@code null}. The UI thread keeps dispatching events while it waits. Fails if
	 * the action does not finish in time.
	 */
	private Throwable runOnNonUiThread(ThrowingRunnable action) throws InterruptedException {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Throwable[] thrown = new Throwable[1];
		Thread nonUiThread = new Thread(() -> {
			try {
				action.run();
			} catch (Throwable t) {
				thrown[0] = t;
			}
		}, "AbstractProgressServiceOffUIThreadTest-non-UI-caller");
		nonUiThread.setDaemon(true);
		nonUiThread.start();
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (nonUiThread.isAlive() && System.currentTimeMillis() < deadline) {
			if (!display.readAndDispatch()) {
				Thread.sleep(5);
			}
		}
		if (nonUiThread.isAlive()) {
			nonUiThread.interrupt();
			throw new AssertionError(
					"run() called from a non-UI thread did not return within " + TIMEOUT_MILLIS + " ms");
		}
		return thrown[0];
	}

	private void assertRunsWithoutError(ThrowingRunnable action) throws InterruptedException {
		Throwable thrown = runOnNonUiThread(action);
		if (thrown != null) {
			throw new AssertionError("Unexpected exception from non-UI thread", thrown);
		}
	}

	/** Without {@code fork} the runnable must run in the calling thread. */
	@Test
	public void testRun_noFork_callFromNonUIThread_runsInCallingThread() throws Exception {
		Thread[] caller = new Thread[1];
		Thread[] runner = new Thread[1];
		assertRunsWithoutError(() -> {
			caller[0] = Thread.currentThread();
			getRunContext().run(false, true, _ -> runner[0] = Thread.currentThread());
		});
		assertNotNull(runner[0], "Runnable was not executed");
		assertSame(caller[0], runner[0], "fork=false must run the runnable in the calling thread");
		assertNotSame(PlatformUI.getWorkbench().getDisplay().getThread(), runner[0]);
	}

	/**
	 * With {@code fork} the runnable runs in a separate thread (neither the caller
	 * nor the UI thread) and the caller blocks until the runnable finished.
	 */
	@Test
	public void testRun_fork_callFromNonUIThread_runsInSeparateThread_andBlocksUntilFinished() throws Exception {
		Thread[] caller = new Thread[1];
		Thread[] runner = new Thread[1];
		AtomicBoolean finished = new AtomicBoolean();
		AtomicBoolean finishedWhenReturned = new AtomicBoolean();
		assertRunsWithoutError(() -> {
			caller[0] = Thread.currentThread();
			getRunContext().run(true, true, _ -> {
				runner[0] = Thread.currentThread();
				Thread.sleep(300);
				finished.set(true);
			});
			finishedWhenReturned.set(finished.get());
		});
		assertNotNull(runner[0], "Runnable was not executed");
		assertNotSame(caller[0], runner[0], "fork=true must run the runnable in a separate thread");
		assertNotSame(PlatformUI.getWorkbench().getDisplay().getThread(), runner[0]);
		assertFalse(ModalContext.isModalContextThread(runner[0]), "Must not be a ModalContext thread");
		assertTrue(finishedWhenReturned.get(), "run() returned before the runnable finished");
	}

	/** The thread listener is told about the worker first and the caller last. */
	@Test
	public void testRun_fork_callFromNonUIThread_notifiesThreadListener() throws Exception {
		List<Thread> notified = Collections.synchronizedList(new ArrayList<>());
		Thread[] caller = new Thread[1];
		Thread[] runner = new Thread[1];
		assertRunsWithoutError(() -> {
			caller[0] = Thread.currentThread();
			getRunContext().run(true, true,
					new ThreadListeningRunnable(notified, () -> runner[0] = Thread.currentThread()));
		});
		assertEquals(Arrays.asList(runner[0], caller[0]), notified);
	}

	@Test
	public void testRun_noFork_callFromNonUIThread_doesNotNotifyThreadListener() throws Exception {
		List<Thread> notified = Collections.synchronizedList(new ArrayList<>());
		assertRunsWithoutError(() -> getRunContext().run(false, true, new ThreadListeningRunnable(notified, () -> {
			// nothing to do
		})));
		assertTrue(notified.isEmpty(), "No thread change happens without fork");
	}

	/**
	 * An exception of the listener before the start means the runnable is not run.
	 */
	@Test
	public void testRun_fork_callFromNonUIThread_threadListenerFailsBeforeStart_runnableNotRun() throws Exception {
		IllegalStateException failure = new IllegalStateException("expected");
		AtomicBoolean ran = new AtomicBoolean();
		Throwable thrown = runOnNonUiThread(() -> getRunContext().run(true, true,
				new ThreadListeningRunnable(new ArrayList<>(), () -> ran.set(true)) {
					@Override
					public void threadChange(Thread thread) {
						throw failure;
					}
				}));
		assertSame(failure, assertInstanceOf(InvocationTargetException.class, thrown).getCause());
		assertFalse(ran.get(), "Runnable must not run when the thread listener failed");
	}

	/**
	 * An exception of the listener at the end is reported if nothing else failed.
	 */
	@Test
	public void testRun_fork_callFromNonUIThread_threadListenerFailsAtEnd_reported() throws Exception {
		IllegalStateException failure = new IllegalStateException("expected");
		AtomicBoolean first = new AtomicBoolean(true);
		Throwable thrown = runOnNonUiThread(
				() -> getRunContext().run(true, true, new ThreadListeningRunnable(new ArrayList<>(), () -> {
					// nothing to do
				}) {
					@Override
					public void threadChange(Thread thread) {
						if (!first.getAndSet(false)) {
							throw failure;
						}
					}
				}));
		assertSame(failure, assertInstanceOf(InvocationTargetException.class, thrown).getCause());
	}

	/**
	 * If the caller is interrupted while waiting, {@code run} still returns only
	 * after the runnable finished, the interrupt flag of the caller is restored and
	 * the runnable saw the interrupt.
	 */
	@Test
	public void testRun_fork_callFromNonUIThread_callerInterrupted_stillWaitsKeepsInterruptAndInterruptsWorker()
			throws Exception {
		CountDownLatch started = new CountDownLatch(1);
		AtomicBoolean finished = new AtomicBoolean();
		AtomicBoolean workerInterrupted = new AtomicBoolean();
		AtomicBoolean finishedWhenReturned = new AtomicBoolean();
		AtomicBoolean interruptedAfterwards = new AtomicBoolean();
		Throwable[] thrown = new Throwable[1];
		Thread caller = new Thread(() -> {
			try {
				getRunContext().run(true, true, _ -> {
					started.countDown();
					try {
						Thread.sleep(TIMEOUT_MILLIS);
					} catch (InterruptedException e) {
						workerInterrupted.set(true);
					}
					// Keep running a little after the interrupt
					Thread.sleep(300);
					finished.set(true);
				});
			} catch (Throwable t) {
				thrown[0] = t;
			}
			finishedWhenReturned.set(finished.get());
			interruptedAfterwards.set(Thread.currentThread().isInterrupted());
		}, "AbstractProgressServiceOffUIThreadTest-non-UI-caller");
		caller.setDaemon(true);
		caller.start();
		assertTrue(started.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), "Runnable did not start");
		caller.interrupt();
		caller.join(TIMEOUT_MILLIS);
		if (caller.isAlive()) {
			throw new AssertionError("run() did not return within " + TIMEOUT_MILLIS + " ms");
		}
		assertNull(thrown[0]);
		assertTrue(workerInterrupted.get(), "The interrupt must be forwarded to the runnable");
		assertTrue(finishedWhenReturned.get(), "run() returned before the runnable finished");
		assertTrue(interruptedAfterwards.get(), "Interrupt flag of the caller must be restored");
	}

	/**
	 * If the caller is interrupted while waiting and the runnable then throws an
	 * {@link InterruptedException}, {@code run} throws it to the caller and the
	 * interrupt flag of the caller is not restored.
	 */
	@Test
	public void testRun_fork_callFromNonUIThread_callerInterrupted_runnableThrowsInterruptedException_thrownAndFlagCleared()
			throws Exception {
		CountDownLatch started = new CountDownLatch(1);
		AtomicBoolean workerInterrupted = new AtomicBoolean();
		AtomicBoolean interruptedAfterwards = new AtomicBoolean(true);
		Throwable[] thrown = new Throwable[1];
		Thread caller = new Thread(() -> {
			try {
				getRunContext().run(true, true, _ -> {
					started.countDown();
					try {
						Thread.sleep(TIMEOUT_MILLIS);
					} catch (InterruptedException e) {
						workerInterrupted.set(true);
						throw e;
					}
				});
			} catch (Throwable t) {
				thrown[0] = t;
			}
			interruptedAfterwards.set(Thread.currentThread().isInterrupted());
		}, "AbstractProgressServiceOffUIThreadTest-non-UI-caller");
		caller.setDaemon(true);
		caller.start();
		assertTrue(started.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), "Runnable did not start");
		caller.interrupt();
		caller.join(TIMEOUT_MILLIS);
		if (caller.isAlive()) {
			throw new AssertionError("run() did not return within " + TIMEOUT_MILLIS + " ms");
		}
		assertTrue(workerInterrupted.get(), "The interrupt must be forwarded to the runnable");
		assertInstanceOf(InterruptedException.class, thrown[0]);
		assertFalse(interruptedAfterwards.get(), "Interrupt flag of the caller must not be restored");
	}

	/**
	 * A rule of the calling thread can be moved to the forked thread and back in
	 * the thread listener notifications.
	 */
	@Test
	public void testRun_fork_callFromNonUIThread_ruleTransferredInThreadListener() throws Exception {
		ISchedulingRule rule = new ISchedulingRule() {
			@Override
			public boolean contains(ISchedulingRule other) {
				return other == this;
			}

			@Override
			public boolean isConflicting(ISchedulingRule other) {
				return other == this;
			}
		};
		AtomicReference<ISchedulingRule> ruleInRunnable = new AtomicReference<>();
		assertRunsWithoutError(() -> {
			Job.getJobManager().beginRule(rule, null);
			try {
				getRunContext().run(true, false, new RuleTransferringRunnable(rule, ruleInRunnable));
			} finally {
				Job.getJobManager().endRule(rule);
			}
		});
		assertSame(rule, ruleInRunnable.get(), "The runnable must run with the transferred rule");
	}

	/** Moves {@code rule} to the thread of every thread change notification. */
	private static class RuleTransferringRunnable implements IRunnableWithProgress, IThreadListener {
		private final ISchedulingRule rule;
		private final AtomicReference<ISchedulingRule> ruleInRunnable;

		RuleTransferringRunnable(ISchedulingRule rule, AtomicReference<ISchedulingRule> ruleInRunnable) {
			this.rule = rule;
			this.ruleInRunnable = ruleInRunnable;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			ruleInRunnable.set(Job.getJobManager().currentRule());
		}

		@Override
		public void threadChange(Thread thread) {
			Job.getJobManager().transferRule(rule, thread);
		}
	}

	/** Records every thread change and runs {@code action} in the runnable. */
	private static class ThreadListeningRunnable implements IRunnableWithProgress, IThreadListener {
		private final List<Thread> notified;
		private final Runnable action;

		ThreadListeningRunnable(List<Thread> notified, Runnable action) {
			this.notified = notified;
			this.action = action;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			action.run();
		}

		@Override
		public void threadChange(Thread thread) {
			notified.add(thread);
		}
	}

	@Test
	public void testRun_noFork_callFromNonUIThread_noDialogShown() throws Exception {
		assertNoDialogFromNonUIThread(false);
	}

	@Test
	public void testRun_fork_callFromNonUIThread_noDialogShown() throws Exception {
		assertNoDialogFromNonUIThread(true);
	}

	private void assertNoDialogFromNonUIThread(boolean fork) throws Exception {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Set<Shell> preexistingShells = new HashSet<>(Arrays.asList(display.getShells()));
		AtomicBoolean dialogShown = new AtomicBoolean();
		Listener showListener = event -> {
			if (event.widget instanceof Shell shell && !preexistingShells.contains(shell)
					&& shell.getData() instanceof ProgressMonitorDialog) {
				dialogShown.set(true);
			}
		};
		display.addFilter(SWT.Show, showListener);
		try {
			assertRunsWithoutError(() -> getRunContext().run(fork, true, _ -> Thread.sleep(LONG_OPERATION_MILLIS)));
		} finally {
			display.removeFilter(SWT.Show, showListener);
		}
		assertFalse(dialogShown.get(), "No progress dialog must open when run() is called from a non-UI thread");
	}

	@Test
	public void testRun_noFork_callFromNonUIThread_runtimeExceptionWrappedInInvocationTargetException()
			throws Exception {
		assertRuntimeExceptionIsWrapped(false);
	}

	@Test
	public void testRun_fork_callFromNonUIThread_runtimeExceptionWrappedInInvocationTargetException() throws Exception {
		assertRuntimeExceptionIsWrapped(true);
	}

	private void assertRuntimeExceptionIsWrapped(boolean fork) throws Exception {
		IllegalArgumentException failure = new IllegalArgumentException("expected");
		Throwable thrown = runOnNonUiThread(() -> getRunContext().run(fork, true, _ -> {
			throw failure;
		}));
		InvocationTargetException ite = assertInstanceOf(InvocationTargetException.class, thrown);
		assertSame(failure, ite.getCause());
	}

	@Test
	public void testRun_noFork_callFromNonUIThread_operationCanceledExceptionMappedToInterruptedException()
			throws Exception {
		assertOperationCanceledExceptionIsMapped(false);
	}

	@Test
	public void testRun_fork_callFromNonUIThread_operationCanceledExceptionMappedToInterruptedException()
			throws Exception {
		assertOperationCanceledExceptionIsMapped(true);
	}

	private void assertOperationCanceledExceptionIsMapped(boolean fork) throws Exception {
		OperationCanceledException failure = new OperationCanceledException("expected");
		Throwable thrown = runOnNonUiThread(() -> getRunContext().run(fork, true, _ -> {
			throw failure;
		}));
		InterruptedException interrupted = assertInstanceOf(InterruptedException.class, thrown);
		assertSame(failure, interrupted.getCause());
	}

	@Test
	public void testRun_noFork_callFromNonUIThread_errorWrappedInInvocationTargetException() throws Exception {
		assertErrorIsWrapped(false);
	}

	@Test
	public void testRun_fork_callFromNonUIThread_errorWrappedInInvocationTargetException() throws Exception {
		assertErrorIsWrapped(true);
	}

	private void assertErrorIsWrapped(boolean fork) throws Exception {
		LinkageError failure = new LinkageError("expected");
		Throwable thrown = runOnNonUiThread(() -> getRunContext().run(fork, true, _ -> {
			throw failure;
		}));
		InvocationTargetException ite = assertInstanceOf(InvocationTargetException.class, thrown);
		assertSame(failure, ite.getCause());
	}

	@Test
	public void testRun_noFork_callFromNonUIThread_monitorIsUsable() throws Exception {
		assertMonitorIsUsable(false);
	}

	@Test
	public void testRun_fork_callFromNonUIThread_monitorIsUsable() throws Exception {
		assertMonitorIsUsable(true);
	}

	private void assertMonitorIsUsable(boolean fork) throws Exception {
		IProgressMonitor[] received = new IProgressMonitor[1];
		assertRunsWithoutError(() -> getRunContext().run(fork, true, monitor -> {
			received[0] = monitor;
			monitor.beginTask("task", 2);
			monitor.worked(1);
			monitor.worked(1);
			monitor.done();
		}));
		assertNotNull(received[0], "Runnable did not receive a monitor");
	}

	@Test
	public void testRun_noFork_callFromNonUIThread_invocationTargetExceptionPropagated() throws Exception {
		assertInvocationTargetExceptionIsPropagated(false);
	}

	@Test
	public void testRun_fork_callFromNonUIThread_invocationTargetExceptionPropagated() throws Exception {
		assertInvocationTargetExceptionIsPropagated(true);
	}

	private void assertInvocationTargetExceptionIsPropagated(boolean fork) throws Exception {
		Exception cause = new Exception("expected");
		Throwable thrown = runOnNonUiThread(() -> getRunContext().run(fork, true, _ -> {
			throw new InvocationTargetException(cause);
		}));
		InvocationTargetException ite = assertInstanceOf(InvocationTargetException.class, thrown);
		assertSame(cause, ite.getCause());
	}

	@Test
	public void testRun_noFork_callFromNonUIThread_interruptedExceptionPropagated() throws Exception {
		assertInterruptedExceptionIsPropagated(false);
	}

	@Test
	public void testRun_fork_callFromNonUIThread_interruptedExceptionPropagated() throws Exception {
		assertInterruptedExceptionIsPropagated(true);
	}

	private void assertInterruptedExceptionIsPropagated(boolean fork) throws Exception {
		Throwable thrown = runOnNonUiThread(() -> getRunContext().run(fork, true, _ -> {
			throw new InterruptedException("expected");
		}));
		assertInstanceOf(InterruptedException.class, thrown);
	}

	/**
	 * Nested {@code run} calls from a runnable that runs in a {@code ModalContext}
	 * thread (forked from the UI thread) run in that same thread, like nested
	 * {@code ModalContext#run} calls.
	 */
	@Test
	public void testRun_nestedInModalContextThread_runsInSameThread() throws Exception {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Thread[] outer = new Thread[1];
		Thread[] innerFork = new Thread[1];
		Thread[] innerNoFork = new Thread[1];
		ModalContext.run(_ -> {
			outer[0] = Thread.currentThread();
			getRunContext().run(true, true, _ -> innerFork[0] = Thread.currentThread());
			getRunContext().run(false, true, _ -> innerNoFork[0] = Thread.currentThread());
		}, true, new NullProgressMonitor(), display);
		assertNotNull(outer[0], "Outer runnable was not executed");
		assertTrue(ModalContext.isModalContextThread(outer[0]), "Expected a ModalContext thread");
		assertSame(outer[0], innerFork[0], "Nested fork=true must run in the ModalContext thread");
		assertSame(outer[0], innerNoFork[0], "Nested fork=false must run in the ModalContext thread");
	}

	/** A nested {@code run(true, ...)} from a forked worker runs in that worker. */
	@Test
	public void testRun_fork_nestedInOwnWorkerThread_runsInSameThread() throws Exception {
		Thread[] caller = new Thread[1];
		Thread[] outer = new Thread[1];
		Thread[] inner = new Thread[1];
		assertRunsWithoutError(() -> {
			caller[0] = Thread.currentThread();
			getRunContext().run(true, true, _ -> {
				outer[0] = Thread.currentThread();
				getRunContext().run(true, true, _ -> inner[0] = Thread.currentThread());
			});
		});
		assertNotNull(outer[0], "Outer runnable was not executed");
		assertNotSame(caller[0], outer[0]);
		assertSame(outer[0], inner[0], "Nested fork=true must run in the worker thread of the outer run");
	}

	/**
	 * A job that owns a scheduling rule can call {@code run(false, ...)}: the
	 * runnable runs in the job thread and can acquire the same rule again.
	 */
	@Test
	public void testRun_noFork_inJobOwningRule_runnableCanAcquireSameRule() throws Exception {
		ISchedulingRule rule = new ISchedulingRule() {
			@Override
			public boolean contains(ISchedulingRule other) {
				return other == this;
			}

			@Override
			public boolean isConflicting(ISchedulingRule other) {
				return other == this;
			}
		};
		AtomicReference<Throwable> thrown = new AtomicReference<>();
		AtomicBoolean ran = new AtomicBoolean();
		Job job = Job.create("rule owner", _ -> {
			try {
				getRunContext().run(false, true, _ -> {
					Job.getJobManager().beginRule(rule, null);
					try {
						ran.set(true);
					} finally {
						Job.getJobManager().endRule(rule);
					}
				});
			} catch (Throwable t) {
				thrown.set(t);
			}
			return Status.OK_STATUS;
		});
		job.setRule(rule);
		job.setSystem(true);
		job.schedule();
		Display display = PlatformUI.getWorkbench().getDisplay();
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (job.getState() != Job.NONE && System.currentTimeMillis() < deadline) {
			if (!display.readAndDispatch()) {
				Thread.sleep(5);
			}
		}
		if (job.getState() != Job.NONE) {
			job.cancel();
			throw new AssertionError("Job did not finish within " + TIMEOUT_MILLIS + " ms");
		}
		assertNull(thrown.get());
		assertTrue(ran.get(), "Runnable was not executed");
	}

	/**
	 * A non-UI caller must not depend on the UI thread: while the UI thread blocks
	 * waiting for the caller (without dispatching events), {@code run(true, ...)}
	 * must still finish.
	 */
	@Test
	public void testRun_fork_callFromNonUIThread_doesNotDependOnUIThread() throws Exception {
		Display display = PlatformUI.getWorkbench().getDisplay();
		AtomicBoolean ran = new AtomicBoolean();
		Thread caller = new Thread(() -> {
			try {
				getRunContext().run(true, true, _ -> {
					Thread.sleep(100);
					ran.set(true);
				});
			} catch (Exception e) {
				// ran stays false
			}
		}, "AbstractProgressServiceOffUIThreadTest-non-UI-caller");
		caller.setDaemon(true);
		caller.start();
		// Block the UI thread without dispatching events.
		caller.join(TIMEOUT_MILLIS);
		boolean stillAlive = caller.isAlive();
		if (stillAlive) {
			caller.interrupt();
			// Let the UI thread pump events so that the suite can recover.
			long deadline = System.currentTimeMillis() + 5_000;
			while (caller.isAlive() && System.currentTimeMillis() < deadline) {
				if (!display.readAndDispatch()) {
					Thread.sleep(5);
				}
			}
		}
		assertFalse(stillAlive, "run() from a non-UI thread depends on the UI thread");
		assertTrue(ran.get(), "Runnable was not executed");
	}

	private interface ThrowingRunnable {
		void run() throws Exception;
	}
}
