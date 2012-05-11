/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

/**
 * 
 */
public class VerboseJobListener implements IJobChangeListener {
	public void aboutToRun(IJobChangeEvent event) {
		System.out.println("[" + Thread.currentThread() + "] aboutToRun: " + event.getJob());
	}

	public void scheduled(IJobChangeEvent event) {
		System.out.println("[" + Thread.currentThread() + "] scheduled: " + event.getJob());
	}

	public void done(IJobChangeEvent event) {
		System.out.println("[" + Thread.currentThread() + "] finished: " + event.getJob());
	}

	public void running(IJobChangeEvent event) {
		System.out.println("[" + Thread.currentThread() + "] running: " + event.getJob());
	}

	public void sleeping(IJobChangeEvent event) {
		System.out.println("[" + Thread.currentThread() + "] sleeping: " + event.getJob());
	}

	public void awake(IJobChangeEvent event) {
		System.out.println("[" + Thread.currentThread() + "] awake: " + event.getJob());
	}
}
