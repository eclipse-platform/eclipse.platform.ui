/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     Brian de Alwis (MTI) - bug 432826: accumulate task-name too
 *     Christoph Laeubrich - Bug 552683 remove deprecated api
 *******************************************************************************/
package org.eclipse.jface.operation;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.widgets.Display;

/**
 * A progress monitor that accumulates <code>setTaskName</code>,
 * <code>worked</code> and <code>subtask</code> calls in the following way by
 * wrapping a standard progress monitor:
 * <ul>
 * <li>When a <code>setTaskName</code>, <code>worked</code> or
 * <code>subtask</code> call occurs the first time, the progress monitor posts a
 * runnable into the asynchronous SWT event queue.</li>
 * <li>Subsequent calls to <code>setTaskName</code>, <code>worked</code> or
 * <code>subtask</code> do not post a new runnable as long as a previous
 * runnable still exists in the SWT event queue. In this case, the progress
 * monitor just updates the internal state of the runnable that waits in the SWT
 * event queue for its execution. If no runnable exists, a new one is created
 * and posted into the event queue.
 * </ul>
 * <p>
 * This class is internal to the framework; clients outside JFace should not use
 * this class.
 * </p>
 */
/* package */class AccumulatingProgressMonitor extends ProgressMonitorWrapper {

	/**
	 * The display.
	 */
	private Display display;
	private static final boolean LOG_BEGIN_TASK = Boolean
			.getBoolean("AccumulatingProgressMonitor.logBeginTaskViolations"); //$NON-NLS-1$

	/**
	 * The collector, or <code>null</code> if none.
	 */
	private Collector collector;

	private String currentTask = ""; //$NON-NLS-1$

	private volatile boolean taskStarted;
	private volatile Exception taskStartedStack;

	private class Collector implements Runnable {
		private String taskName;

		private String subTask;

		private double worked;

		private IProgressMonitor monitor;

		/**
		 * Create a new collector.
		 */
		public Collector(String taskName, String subTask, double work,
				IProgressMonitor monitor) {
			this.taskName = taskName;
			this.subTask = subTask;
			this.worked = work;
			this.monitor = monitor;
		}

		/**
		 * Set the task name
		 */
		public void setTaskName(String name) {
			this.taskName = name;
		}

		/**
		 * Add worked to the work.
		 */
		public void worked(double workedIncrement) {
			this.worked = this.worked + workedIncrement;
		}

		/**
		 * Set the subTask name.
		 */
		public void subTask(String subTaskName) {
			this.subTask = subTaskName;
		}

		/**
		 * Run the collector.
		 */
		@Override
		public void run() {
			clearCollector(this);
			if (taskName != null) {
				monitor.setTaskName(taskName);
			}
			if (subTask != null) {
				monitor.subTask(subTask);
			}
			if (worked > 0) {
				monitor.internalWorked(worked);
			}
		}
	}

	/**
	 * Creates an accumulating progress monitor wrapping the given one
	 * that uses the given display.
	 *
	 * @param monitor the actual progress monitor to be wrapped
	 * @param display the SWT display used to forward the calls
	 *  to the wrapped progress monitor
	 */
	public AccumulatingProgressMonitor(IProgressMonitor monitor, Display display) {
		super(monitor);
		Assert.isNotNull(display);
		this.display = display;
	}

	@Override
	public void beginTask(final String name, final int totalWork) {
		if (taskStarted) {
			if (LOG_BEGIN_TASK) {
				Exception e = new IllegalStateException(
						"beginTask should only be called once per instance. At least call done() before further invocations", //$NON-NLS-1$
						taskStartedStack);
				Policy.getLog().log(Status.warning(e.getLocalizedMessage(), e));
			}
			done(); // workaround client error
		}
		if (LOG_BEGIN_TASK) {
			taskStartedStack = new IllegalStateException(
					"beginTask(" + name + ", " + totalWork + ") was called here previously"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		taskStarted = true;
		synchronized (this) {
			collector = null;
		}
		display.asyncExec(() -> {
			currentTask = name;
			getWrappedProgressMonitor().beginTask(name, totalWork);
		});
	}

	/**
	 * Clears the collector object used to accumulate work and subtask calls if
	 * it matches the given one.
	 */
	private synchronized void clearCollector(Collector collectorToClear) {
		// Check if the accumulator is still using the given collector.
		// If not, don't clear it.
		if (this.collector == collectorToClear) {
			this.collector = null;
		}
	}

	/**
	 * Creates a collector object to accumulate work and subtask calls.
	 */
	private void createCollector(String taskName, String subTask, double work) {
		collector = new Collector(taskName, subTask, work,
				getWrappedProgressMonitor());
		display.asyncExec(collector);
	}

	@Override
	public void done() {
		if (!taskStarted) {
			// ignore call to done() if beginTask() was not called!
			// Otherwise an otherwise already started delegate would be finished
			// see https://github.com/eclipse-jdt/eclipse.jdt.ui/issues/61
			return;
		}
		taskStarted = false;
		taskStartedStack = null;
		synchronized (this) {
			collector = null;
		}
		display.asyncExec(() -> getWrappedProgressMonitor().done());
	}

	@Override
	public synchronized void internalWorked(final double work) {
		if (collector == null) {
			createCollector(null, null, work);
		} else {
			collector.worked(work);
		}
	}

	@Override
	public synchronized void setTaskName(final String name) {
		currentTask = name;
		if (collector == null) {
			createCollector(name, null, 0);
		} else {
			collector.setTaskName(name);
		}
	}

	@Override
	public synchronized void subTask(final String name) {
		if (collector == null) {
			createCollector(null, name, 0);
		} else {
			collector.subTask(name);
		}
	}

	@Override
	public synchronized void worked(int work) {
		internalWorked(work);
	}

	@Override
	public void clearBlocked() {

		// Don't bother with a collector as this should only ever
		// happen once and prevent any more progress.
		final IProgressMonitor pm = getWrappedProgressMonitor();
		if (pm == null) {
			return;
		}

		display.asyncExec(() -> {
			pm.clearBlocked();
			Dialog.getBlockedHandler().clearBlocked();
		});
	}

	@Override
	public void setBlocked(final IStatus reason) {

		// Don't bother with a collector as this should only ever
		// happen once and prevent any more progress.
		final IProgressMonitor pm = getWrappedProgressMonitor();
		if (pm == null) {
			return;
		}

		display.asyncExec(() -> {
			pm.setBlocked(reason);
			//Do not give a shell as we want it to block until it opens.
			Dialog.getBlockedHandler().showBlocked(pm, reason, currentTask);
		});
	}
}
