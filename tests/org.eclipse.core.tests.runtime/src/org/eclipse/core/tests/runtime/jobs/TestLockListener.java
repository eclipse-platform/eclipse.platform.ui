/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alexander Kurtakov <akurtako@redhat.com> - bug 458490
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import org.eclipse.core.runtime.jobs.LockListener;
import org.junit.Assert;

/**
 * A lock listener used for testing that ensures wait/release calls are correctly paired.
 */
public class TestLockListener extends LockListener {
	private boolean waiting;

	public synchronized void aboutToRelease() {
		waiting = false;
	}

	public synchronized boolean aboutToWait(Thread lockOwner) {
		waiting = true;
		return false;
	}

	public synchronized void assertNotWaiting(String message) {
		Assert.assertTrue(message, !waiting);
	}

}
