/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.locks;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * 
 */
class TestJob extends Job {
	private String name;
	private int ticks;
	private int tickLength;
	public TestJob(String name) {
		this(name, 10, 100);
	}
	public TestJob(String name, int ticks, int tickLength) {
		this.name = name;
		this.ticks = ticks;
		this.tickLength = tickLength;
	}
	
	public IStatus run(IProgressMonitor monitor) {
		//must have positive work
		monitor.beginTask(name, ticks <= 0 ? 1 : ticks);
		try {
			for (int i = 0; i < ticks; i++) {
				monitor.subTask("Tick: " + i);
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				try {
					Thread.sleep(tickLength);
				} catch (InterruptedException e) {
				}
				monitor.worked(1);
			}
			if (ticks <= 0)
				monitor.worked(1);
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}
	public String toString() {
		if (name == null)
			return super.toString();
		return name + "(" + super.toString() + ")";
	}
}