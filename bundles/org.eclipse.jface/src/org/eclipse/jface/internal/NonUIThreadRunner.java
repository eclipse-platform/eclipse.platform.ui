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
package org.eclipse.jface.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;
import org.eclipse.jface.operation.ModalContext;

/**
 * Runs an {@link IRunnableWithProgress} for an {@link IRunnableContext} on a
 * thread that is not a UI thread, without any UI. Without fork the runnable
 * runs in the calling thread. With fork it runs in a separate thread, unless
 * the caller already is a {@link ModalContext} thread or a thread forked by
 * this class. The caller blocks until the runnable has finished. Exceptions are
 * mapped like in {@link ModalContext}.
 */
public final class NonUIThreadRunner {

	private NonUIThreadRunner() {
		// utility class
	}

	/**
	 * Runs the runnable in the calling thread or, with {@code fork}, in a separate
	 * thread. The calling thread blocks until the runnable has finished. When it
	 * forks, a runnable that implements {@link IThreadListener} is notified with
	 * the separate thread before it runs and with the calling thread afterwards.
	 *
	 * @param fork     {@code true} to run the runnable in a separate thread
	 * @param runnable the runnable to run
	 * @throws InvocationTargetException if the runnable or its listener fails
	 * @throws InterruptedException      if the runnable was canceled or interrupted
	 */
	public static void run(boolean fork, IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		// Like ModalContext#run: a nested run in a ModalContext thread or in a
		// worker forked by this class stays in the current thread, so the
		// nested runnable keeps the scheduling rules and locks of the outer
		// runnable.
		Thread current = Thread.currentThread();
		if (fork && !ModalContext.isModalContextThread(current) && !(current instanceof RunWorkerThread)) {
			runInSeparateThread(runnable);
		} else {
			runInCallingThread(runnable);
		}
	}

	private static void runInCallingThread(IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		// Same exception mapping as ModalContext#runInCurrentThread
		try {
			runnable.run(new NullProgressMonitor());
		} catch (OperationCanceledException e) {
			InterruptedException interrupted = new InterruptedException(e.getLocalizedMessage());
			interrupted.initCause(e);
			throw interrupted;
		} catch (RuntimeException | Error e) {
			throw new InvocationTargetException(e);
		}
	}

	/** Marks the threads that run a forked runnable of this class. */
	private static final class RunWorkerThread extends Thread {
		RunWorkerThread(Runnable target) {
			super(target);
		}
	}

	private static void runInSeparateThread(IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		// A separate thread honors the "fork" contract of IRunnableContext.
		// ModalContext is not used: its forked thread ends with
		// Display#syncExec, which would deadlock a non-UI caller while the UI
		// thread waits for it. Same thread listener and exception handling as
		// ModalContext
		Thread callingThread = Thread.currentThread();
		Throwable[] thrown = new Throwable[1];
		Thread worker = new RunWorkerThread(() -> {
			try {
				runnable.run(new NullProgressMonitor());
			} catch (Throwable t) {
				thrown[0] = t;
			}
			if (runnable instanceof IThreadListener listener) {
				try {
					listener.threadChange(callingThread);
				} catch (RuntimeException | Error e) {
					if (thrown[0] == null) {
						thrown[0] = e;
					}
				}
			}
		});
		boolean started = true;
		boolean callerInterrupted = false;
		if (runnable instanceof IThreadListener listener) {
			try {
				listener.threadChange(worker);
			} catch (RuntimeException | Error e) {
				thrown[0] = e;
				started = false;
			}
		}
		if (started) {
			worker.start();
			// The caller must block until the runnable finished, even if it is
			// interrupted meanwhile: forward the interrupt to the worker once, keep waiting
			// and restore the interrupt flag of the caller afterwards, unless an
			// InterruptedException is thrown to the caller.
			while (true) {
				try {
					worker.join();
					break;
				} catch (InterruptedException e) {
					if (!callerInterrupted) {
						callerInterrupted = true;
						worker.interrupt();
					}
				}
			}
		}
		Throwable t = thrown[0];
		if (callerInterrupted && !(t instanceof InterruptedException) && !(t instanceof OperationCanceledException)) {
			// No InterruptedException reaches the caller: restore its interrupt flag
			callingThread.interrupt();
		}
		if (t instanceof InvocationTargetException ite) {
			throw ite;
		} else if (t instanceof InterruptedException ie) {
			throw ie;
		} else if (t instanceof OperationCanceledException) {
			InterruptedException interrupted = new InterruptedException(t.getLocalizedMessage());
			interrupted.initCause(t);
			throw interrupted;
		} else if (t != null) {
			throw new InvocationTargetException(t);
		}
	}
}
