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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

/**
 * 
 */
public class VerboseJobListener implements IJobChangeListener {
	public void aboutToRun(Job job) {
		System.out.println("[" + Thread.currentThread() + "] aboutToRun: " + job);
	}
	public void scheduled(Job job) {
		System.out.println("[" + Thread.currentThread() + "] scheduled: " + job);
	}
	public void done(Job job, IStatus result) {
		System.out.println("[" + Thread.currentThread() + "] finished: " + job);
	}
	public void running(Job job) {
		System.out.println("[" + Thread.currentThread() + "] running: " + job);
	}
	public void sleeping(Job job) {
		System.out.println("[" + Thread.currentThread() + "] sleeping: " + job);
	}
	public void awake(Job job) {
		System.out.println("[" + Thread.currentThread() + "] awake: " + job);
	}
}
