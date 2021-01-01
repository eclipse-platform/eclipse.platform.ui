/*******************************************************************************
 * Copyright (c) 2011, 2020 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - bug fixes
 *     Christoph Läubrich - Bug 563459 - Enhance UISynchronize to make it more useful
 *******************************************************************************/
package org.eclipse.e4.ui.di;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.FutureTask;

/**
 * Widget toolkit abstract to synchronize back into the UI-Thread from other
 * threads
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.0
 */
public abstract class UISynchronize {
	/**
	 * Executes the runnable on the UI-Thread and blocks until the runnable is
	 * finished
	 *
	 * @param runnable the runnable to execute
	 */
	public abstract void syncExec(Runnable runnable);

	/**
	 * Schedules the runnable on the UI-Thread for execution and returns immediately
	 *
	 * @param runnable the runnable to execute
	 */
	public abstract void asyncExec(Runnable runnable);

	/**
	 * Checks if the given thread is the UI-Thread.
	 *
	 * @param thread to check
	 * @return <code>true</code> if the thread is the UI-Thread <code>false</code>
	 *         otherwise
	 * @since 1.3
	 */
	protected abstract boolean isUIThread(Thread thread);

	/**
	 * Shows a busy-indicator to the user while this runnable is executed, long
	 * running operations in the UI thread may freeze the UI.
	 *
	 * @param runnable the runnable to execute
	 * @since 1.3
	 */
	protected abstract void showBusyWhile(Runnable runnable);

	/**
	 * Request to perform an event dispatch cycle.
	 *
	 * @return <code>true</code> if there might be more work to perform
	 *         <code>false</code> otherwise
	 * @since 1.3
	 */
	protected abstract boolean dispatchEvents();

	/**
	 * Executes the given action inside the UI thread (either directly if the
	 * current thread is the UI-Thread or by using {@link #syncExec(Runnable)} if
	 * not returning the result to the caller
	 *
	 * @param action the action to perform
	 * @since 1.3
	 */
	public void exec(Runnable action) {
		Thread thread = Thread.currentThread();
		if (isUIThread(thread)) {
			action.run();
		} else {
			syncExec(action);
		}
	}

	/**
	 * Calls the given {@link Callable} inside the UI thread (either directly if the
	 * current thread is the UI Thread or by using {@link #syncExec(Runnable)} if
	 * not returning the result to the caller
	 *
	 * @param <T>    the return type of the {@link Callable}
	 * @param action the action to perform
	 * @return the value as a result of calling the {@link Callable}
	 * @throws InterruptedException if either the current or the background thread
	 *                              where interrupted while waiting for the result
	 * @throws ExecutionException   if the synchronous execution has thrown an
	 *                              exception
	 * @since 1.3
	 */
	public <T> T call(Callable<T> action) throws InterruptedException, ExecutionException {
		Thread thread = Thread.currentThread();
		if (isUIThread(thread)) {
			try {
				return action.call();
			} catch (Exception e) {
				if (e instanceof InterruptedException) {
					throw (InterruptedException) e;
				}
				throw new ExecutionException(e);
			}
		} else {
			FutureTask<T> task = new FutureTask<>(action);
			try {
				syncExec(task);
			} catch (RuntimeException e) {
				throw new ExecutionException(e);
			}
			return task.get();
		}
	}

	/**
	 * Performs the given action in a background (non-ui thread) showing a
	 * busy-indicator on a best-effort basis if called from the UI-Thread, otherwise
	 * simply executes the action
	 *
	 * @param action the action to be performed must not be <code>null</code>
	 * @throws InterruptedException if either the current or the background thread
	 *                              where interrupted while waiting for the result
	 *                              exception
	 * @since 1.3
	 */
	public void busyExec(Runnable action) throws InterruptedException {
		try {
			busyCall(() -> {
				action.run();
				return null;
			});
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * Performs the given action in a non-ui thread showing a busy-indicator on a
	 * best-effort basis and returning the result of the {@link Callable} to the
	 * caller
	 *
	 * @param <T>    return type of the {@link Callable}
	 * @param action the action to be performed must not be <code>null</code>
	 * @return the value as a result of calling the {@link Callable}
	 * @throws InterruptedException if either the current or the background thread
	 *                              where interrupted while waiting for the result
	 * @throws ExecutionException   if the concurrent execution has thrown an
	 *                              exception
	 * @since 1.3
	 */
	public <T> T busyCall(Callable<T> action) throws InterruptedException, ExecutionException {
		Objects.requireNonNull(action);
		FutureTask<T> task = new FutureTask<>(action);
		Thread thread = Thread.currentThread();
		if (isUIThread(thread)) {
			ForkJoinTask<?> fork = ForkJoinPool.commonPool().submit(task);
			showBusyWhile(() -> {
				while (!fork.isDone() && !Thread.currentThread().isInterrupted()) {
					if (dispatchEvents()) {
						continue;
					}
					Thread.yield();
				}
			});
		} else {
			task.run();
		}
		return task.get();
	}

}
