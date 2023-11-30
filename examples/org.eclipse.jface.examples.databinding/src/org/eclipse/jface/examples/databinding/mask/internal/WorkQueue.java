/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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
 ******************************************************************************/
package org.eclipse.jface.examples.databinding.mask.internal;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @since 3.2
 */
public class WorkQueue {

	private boolean updateScheduled = false;

	private boolean paintListenerAttached = false;

	private final Deque<Runnable> pendingWork = new LinkedList<>();

	private final Display d;

	private final Set<Runnable> pendingWorkSet = new HashSet<>();

	private final Runnable updateJob = () -> {
		doUpdate();
		updateScheduled = false;
	};

	private final Listener paintListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			paintListenerAttached = false;
			d.removeFilter(SWT.Paint, this);
			doUpdate();
		}
	};

	public WorkQueue(Display targetDisplay) {
		d = targetDisplay;
	}

	private void doUpdate() {
		for (;;) {
			Runnable next;
			synchronized (pendingWork) {
				if (pendingWork.isEmpty()) {
					break;
				}
				next = pendingWork.removeFirst();
				pendingWorkSet.remove(next);
			}

			next.run();
		}

	}

	/**
	 * Schedules some work to happen in the UI thread as soon as possible. If
	 * possible, the work will happen before the next control redraws. The given
	 * runnable will only be run once. Has no effect if this runnable has
	 * already been queued for execution.
	 *
	 * @param work
	 *            runnable to execute
	 */
	public void runOnce(Runnable work) {
		synchronized (pendingWork) {
			if (pendingWorkSet.contains(work)) {
				return;
			}

			pendingWorkSet.add(work);

			asyncExec(work);
		}
	}

	/**
	 * Schedules some work to happen in the UI thread as soon as possible. If
	 * possible, the work will happen before the next control redraws. Unlike
	 * runOnce, calling asyncExec twice with the same runnable will cause that
	 * runnable to run twice.
	 *
	 * @param work
	 *            runnable to execute
	 */
	public void asyncExec(Runnable work) {
		synchronized (pendingWork) {
			pendingWork.add(work);
			if (!updateScheduled) {
				updateScheduled = true;
				d.asyncExec(updateJob);
			}

			// If we're in the UI thread, add an event filter to ensure
			// the work happens ASAP
			if (Display.getCurrent() == d) {
				if (!paintListenerAttached) {
					paintListenerAttached = true;
					d.addFilter(SWT.Paint, paintListener);
				}
			}
		}
	}

	/**
	 * Cancels a previously-scheduled runnable. Has no effect if the given
	 * runnable was not previously scheduled or has already executed.
	 *
	 * @param toCancel
	 *            runnable to cancel
	 */
	public void cancelExec(Runnable toCancel) {
		synchronized (pendingWork) {
			pendingWork.remove(toCancel);
			pendingWorkSet.remove(toCancel);
		}
	}

	/**
	 * Cancels all pending work.
	 */
	public void cancelAll() {
		synchronized (pendingWork) {
			pendingWork.clear();
			pendingWorkSet.clear();
		}
	}
}
