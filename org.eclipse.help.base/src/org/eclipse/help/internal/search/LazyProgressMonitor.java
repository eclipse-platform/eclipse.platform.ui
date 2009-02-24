/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;
import org.eclipse.core.runtime.*;
/**
 * Progress Monitor, that accumulates work without communicating it immidiately
 * to the underlying monitor. The work is sent in larger chunks for performance
 * reasons.
 */
class LazyProgressMonitor extends ProgressMonitorWrapper {
	// maximum number of times worked() should be called
	// on underlying progress monitor
	private static final int MAX_STEPS = 100;
	private final IProgressMonitor monitor;
	private int totalWork;
	private int work;
	private int lastWorked;
	private int treshold;
	protected LazyProgressMonitor(IProgressMonitor monitor) {
		super(monitor);
		this.monitor = monitor;
	}
	/**
	 * @see IProgressMonitor#beginTask
	 */
	public void beginTask(String name, int totalWork) {
		if (totalWork > 0) {
			this.totalWork = totalWork;
		}
		monitor.beginTask(name, totalWork);
		work = 0;
		lastWorked = 0;
		treshold = 1 + totalWork / MAX_STEPS;
	}
	/**
	 * @see IProgressMonitor#worked
	 */
	public void worked(int newWork) {
		this.work += newWork;
		if (work >= treshold) {
			monitor.worked(work - lastWorked);
			lastWorked = work;
			treshold = work + 1 + totalWork / MAX_STEPS;
		}

	}
}
