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
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.ExceptionHandler;

/**
 * Used to run an event loop whenever progress monitor methods are invoked.
 * <p>
 * This is needed since editor save operations are done in the UI thread.
 * Although save operations should be written to do the work in the non-UI
 * thread, this was not done for 1.0, so this was added to keep the UI live
 * (including allowing the cancel button to work).
 */
public class EventLoopProgressMonitor extends ProgressMonitorWrapper implements IProgressMonitor {
	/**
	 * Threshold for how often the event loop is spun, in ms.
	 */
	private static int T_THRESH = 100;

	/**
	 * Maximum amount of time to spend processing events, in ms.
	 */
	private static int T_MAX = 50;

	/**
	 * Last time the event loop was spun.
	 */
	private long lastTime = System.currentTimeMillis();

	/**
	 * The task name is the name of the current task in the event loop.
	 */
	private String taskName;

	/**
	 * Constructs a new instance of the receiver and forwards to monitor.
	 */
	public EventLoopProgressMonitor(IProgressMonitor monitor) {
		super(monitor);
	}

	/**
	 * @see IProgressMonitor#beginTask
	 */
	@Override
	public void beginTask(String name, int totalWork) {
		super.beginTask(name, totalWork);
		taskName = name;
		runEventLoop();
	}

	@Override
	public void clearBlocked() {
		Dialog.getBlockedHandler().clearBlocked();
	}

	/**
	 * @see IProgressMonitor#done
	 */
	@Override
	public void done() {
		super.done();
		taskName = null;
		runEventLoop();
	}

	/**
	 * @see IProgressMonitor#internalWorked
	 */
	@Override
	public void internalWorked(double work) {
		super.internalWorked(work);
		runEventLoop();
	}

	/**
	 * @see IProgressMonitor#isCanceled
	 */
	@Override
	public boolean isCanceled() {
		runEventLoop();
		return super.isCanceled();
	}

	/**
	 * Runs an event loop.
	 */
	private void runEventLoop() {
		// Only run the event loop so often, as it is expensive on some platforms
		// (namely Motif).
		long t = System.currentTimeMillis();
		if (t - lastTime < T_THRESH) {
			return;
		}
		// Run the event loop.
		Display disp = Display.getCurrent();
		if (disp == null) {
			return;
		}
		lastTime = t;

		// Initialize an exception handler from the window class.
		ExceptionHandler handler = ExceptionHandler.getInstance();

		for (;;) {
			try {
				if (!disp.readAndDispatch()) {
					break;
				}
			} catch (Throwable e) {// Handle the exception the same way as the workbench
				handler.handleException(e);
				break;
			}

			// Only run the event loop for so long.
			// Otherwise, this would never return if some other thread was
			// constantly generating events.
			if (System.currentTimeMillis() - t > T_MAX) {
				break;
			}
		}
	}

	@Override
	public void setBlocked(IStatus reason) {
		Dialog.getBlockedHandler().showBlocked(this, reason, taskName);
	}

	/**
	 * @see IProgressMonitor#setCanceled
	 */
	@Override
	public void setCanceled(boolean b) {
		super.setCanceled(b);
		taskName = null;
		runEventLoop();
	}

	/**
	 * @see IProgressMonitor#setTaskName
	 */
	@Override
	public void setTaskName(String name) {
		super.setTaskName(name);
		taskName = name;
		runEventLoop();
	}

	/**
	 * @see IProgressMonitor#subTask
	 */
	@Override
	public void subTask(String name) {
		// Be prepared in case the first task was null
		if (taskName == null) {
			taskName = name;
		}
		super.subTask(name);
		runEventLoop();
	}

	/**
	 * @see IProgressMonitor#worked
	 */
	@Override
	public void worked(int work) {
		super.worked(work);
		runEventLoop();
	}

	/**
	 * Return the name of the current task.
	 *
	 * @return Returns the taskName.
	 */
	protected String getTaskName() {
		return taskName;
	}
}
