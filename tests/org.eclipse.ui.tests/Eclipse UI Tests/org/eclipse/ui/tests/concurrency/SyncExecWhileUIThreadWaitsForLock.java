/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.concurrency;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.jobs.ILock;

import org.eclipse.core.runtime.jobs.Job;

import junit.framework.TestCase;

/**
 * This tests the simple traditional deadlock of a thread holding a lock trying
 * to perform a syncExec, while the UI thread is waiting for that lock.
 * UISynchronizer and UILockListener conspire to prevent deadlock in this case.
 */
public class SyncExecWhileUIThreadWaitsForLock extends TestCase {
	public void testDeadlock() {
		final ILock lock = Job.getJobManager().newLock();
		final boolean[] blocked = new boolean[] {false};
		final boolean[] lockAcquired= new boolean[] {false};
		Thread locking = new Thread("SyncExecWhileUIThreadWaitsForLock") {
			@Override
			public void run() {
				try {
					//first make sure this background thread owns the lock
					lock.acquire();
					//spawn an asyncExec that will cause the UI thread to be blocked
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							blocked[0] = true;
							lock.acquire();
							lock.release();
							blocked[0] = false;
						}
					});
					//wait until the UI thread is blocked waiting for the lock
					while (!blocked[0]) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
					//now attempt to do a syncExec that also acquires the lock
					//this should succeed even while the above asyncExec is blocked, thanks to UISynchronizer
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							try {
								//use a timeout to avoid deadlock in case of regression
								if (lock.acquire(60000)) {
									//this flag is used to verify that we actually acquired the lock
									lockAcquired[0] = true;
									lock.release();
								}
							} catch (InterruptedException e) {
							}
						}
					});
				} finally {
					lock.release();
				}
			}
		};
		locking.start();
		//wait until we succeeded to acquire the lock in the UI thread
		long waitStart = System.currentTimeMillis();
		Display display = Display.getDefault();
		while (!lockAcquired[0]) {
			//spin event loop so that asyncExed above gets run
			if (!display.readAndDispatch()) {
				display.sleep();
			}
			//if we waited too long, fail the test
			if (System.currentTimeMillis()-waitStart > 60000) {
				assertTrue("Deadlock occurred", false);
			}
		}
		//if we get here, the test succeeded
	}
}
