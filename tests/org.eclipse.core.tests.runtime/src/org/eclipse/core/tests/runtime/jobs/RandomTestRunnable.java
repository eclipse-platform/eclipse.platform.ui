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

import java.util.Random;
import org.eclipse.core.runtime.jobs.ILock;

public class RandomTestRunnable extends Thread {
	private ILock[] locks;
	private Random random = new Random();
	private boolean alive;
	private boolean needRandomization;

	/**
	 * This runnable will randomly acquire the given locks for
	 * random periods of time, in the given order, or in random order (if specified)
	 */
	public RandomTestRunnable(ILock[] locks, String name, boolean addRandomness) {
		super(name);
		this.locks = new ILock[locks.length];
		for (int i = 0; i < locks.length; i++) {
			this.locks[i] = locks[i];
		}
		this.alive = true;
		this.needRandomization = addRandomness;
	}

	public void kill() {
		alive = false;
	}

	public void run() {
		while (alive) {
			if (needRandomization) {
				for (int i = 0; i < locks.length; i++) {
					int nextFlip = random.nextInt(locks.length);
					ILock temp = locks[i];
					locks[i] = locks[nextFlip];
					locks[nextFlip] = temp;
				}
			}
			for (int i = 0; i < locks.length; i++) {
				locks[i].acquire();
				try {
					Thread.sleep(random.nextInt(500));
				} catch (InterruptedException e1) {
				}
			}
			//release all locks
			for (int i = locks.length; --i >= 0;) {
				locks[i].release();
			}
		}
	}

}
