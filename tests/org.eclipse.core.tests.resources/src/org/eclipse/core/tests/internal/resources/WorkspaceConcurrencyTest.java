/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.harness.*;

/**
 * Tests concurrency issues when dealing with operations on the worspace
 */
public class WorkspaceConcurrencyTest extends EclipseWorkspaceTest {
	
	public static Test suite() {
		return new TestSuite(WorkspaceConcurrencyTest.class);
	}
	public WorkspaceConcurrencyTest() {
		super ("");
	}
	public WorkspaceConcurrencyTest(String name) {
		super (name);
	}
	/**
	 * Tests that it is possible to cancel a workspace operation when it is blocked
	 * by activity in another thread. This is a regression test for bug 56118.
	 */
	public void testCancelOnBlocked() {
		//create a dummy project
		ensureExistsInWorkspace(getWorkspace().getRoot().getProject("P1"), true);
		//add a resource change listener that blocks forever, thus 
		//simulating a scenario where workspace lock is held indefinitely
		final int[] barrier = new int[1];
		final Throwable[] error = new Throwable[1];
		IResourceChangeListener listener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				//block until we are told to do otherwise
				barrier[0] = TestBarrier.STATUS_START;
				try {
					TestBarrier.waitForStatus(barrier, TestBarrier.STATUS_DONE);
				} catch (Throwable e) {
					error[0] = e;
				}
			}
		};
		getWorkspace().addResourceChangeListener(listener);
		try {
			//create a thread that modifies the workspace. This should 
			//hang indefintely due to the misbehaving listener
			TestWorkspaceJob testJob = new TestWorkspaceJob(10);
			testJob.setTouch(true);
			testJob.setRule(getWorkspace().getRoot());
			testJob.schedule();
			
			//wait until blocked on the listener
			TestBarrier.waitForStatus(barrier, TestBarrier.STATUS_START);
			
			//create a second thread that attempts to modify the workspace, but immediately
			//cancels itself. This thread should terminate immediately with a cancelation exception
			final boolean[] canceled = new boolean[] {false};
			Thread t2 = new Thread(new Runnable() {
				public void run() {
					try {
						getWorkspace().run(new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) {
								//noop
							}
						}, new CancelingProgressMonitor());
					} catch (CoreException e) {
						fail("1.99", e);
					} catch (OperationCanceledException e) {
						canceled[0] = true;
					}
				}
			});
			t2.start();
			try {
				t2.join();
			} catch (InterruptedException e) {
				fail("1.88", e);
			}
			//should have canceled
			assertTrue("2.0", canceled[0]);
			
			//finally release the listener and ensure the first thread completes
			barrier[0] = TestBarrier.STATUS_DONE;
			try {
				testJob.join();
			} catch (InterruptedException e1) {
				//ignore
			}
			if (error[0] != null)
				fail("3.0", error[0]);
		} finally {
			getWorkspace().removeResourceChangeListener(listener);
		}
	}
}