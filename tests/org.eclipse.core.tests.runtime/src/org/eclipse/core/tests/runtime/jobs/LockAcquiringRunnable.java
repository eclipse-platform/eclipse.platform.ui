/**********************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import java.util.Random;
import org.eclipse.core.runtime.jobs.ILock;

public class LockAcquiringRunnable implements Runnable {
	private ILock[] locks;
	private Random random = new Random();
	private boolean alive;
	private boolean done;

	/**
	 * This runnable will randomly acquire the given lock for
	 * random periods of time, in the given order
	 */
	public LockAcquiringRunnable(ILock[] locks) {
		this.locks = locks;
		this.alive = true;
		done = false;
	}

	public void kill() {
		alive = false;
	}

	public void run() {
		while (alive) {
			try {
				Thread.sleep(random.nextInt(500));
			} catch (InterruptedException e) {
				//ignore
			}
			for (int i = 0; i < locks.length; i++) {
				locks[i].acquire();
				try {
					Thread.sleep(random.nextInt(500));
				} catch (InterruptedException e1) {
					//ignore
				}
			}
			//release all locks
			for (int i = locks.length; --i >= 0;) {
				locks[i].release();
			}
		}
		done = true;
	}

	public void isDone() {
		while (!done) {
			try {
				Thread.yield();
				Thread.sleep(100);
				Thread.yield();
			} catch (InterruptedException e) {
				//ignore
			}
		}
	}
}