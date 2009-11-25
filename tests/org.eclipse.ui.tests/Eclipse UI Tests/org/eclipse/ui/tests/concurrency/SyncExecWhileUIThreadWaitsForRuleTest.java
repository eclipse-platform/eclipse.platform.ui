/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.concurrency;

import junit.framework.TestCase;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

/**
 * This tests the simple traditional deadlock of a thread holding a scheduling rule trying
 * to perform a syncExec, while the UI thread is waiting for that scheduling rule.
 * UISynchronizer and UILockListener conspire to prevent deadlock in this case.
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=296056.
 */
public class SyncExecWhileUIThreadWaitsForRuleTest extends TestCase {
	class TestRule implements ISchedulingRule {
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	}

	public void testDeadlock() {
		final ISchedulingRule rule = new TestRule();
		final boolean[] blocked = new boolean[] {false};
		final boolean[] lockAcquired = new boolean[] {false};
		final SubMonitor beginRuleMonitor = SubMonitor.convert(null);
		Thread locking = new Thread("SyncExecWhileUIThreadWaitsForRuleTest") {
			public void run() {
				try {
					//first make sure this background thread owns the lock
					Job.getJobManager().beginRule(rule, null);
					//spawn an asyncExec that will cause the UI thread to be blocked
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							blocked[0] = true;
							Job.getJobManager().beginRule(rule, beginRuleMonitor);
							Job.getJobManager().endRule(rule);
							blocked[0] = false;
						}
					});
					//wait until the UI thread is blocked waiting for the lock
					while (!blocked[0]) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//now attempt to do a syncExec that also acquires the lock
					//this should succeed even while the above asyncExec is blocked, thanks to UISynchronizer
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							//use a timeout to avoid deadlock in case of regression
							Job.getJobManager().beginRule(rule, null);
							lockAcquired[0] = true;
							Job.getJobManager().endRule(rule);
						}
					});
				} finally {
					Job.getJobManager().endRule(rule);
				}
			}
		};
		locking.start();
		//create a thread that will cancel the monitor after 60 seconds so we don't hang the tests
		final long waitStart = System.currentTimeMillis();
		Thread canceler = new Thread("Canceler") {
			public void run() {
				while (true) {
					if (System.currentTimeMillis() - waitStart > 60000) {
						beginRuleMonitor.setCanceled(true);
						break;
					}
				}

			}
		};
		canceler.start();
		//wait until we succeeded to acquire the lock in the UI thread
		Display display = Display.getDefault();
		while (!lockAcquired[0]) {
			//spin event loop so that asyncExed above gets run
			try {
				if (!display.readAndDispatch())
					display.sleep();
			} catch (SWTException e) {
				fail("Deadlock occurred");
			}
		}
		//if the monitor was canceled then we got a deadlock
		assertFalse("deadlock occurred", beginRuleMonitor.isCanceled());
	}
}
