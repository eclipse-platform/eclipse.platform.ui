/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import junit.framework.Assert;

/**
 * Blocks the current thread until the given variable is set to the given value
 * Times out after a predefined period to avoid hanging tests
 */
public class StatusChecker {
	public static final int STATUS_WAIT_FOR_START = 0;
	public static final int STATUS_START = 1;
	public static final int STATUS_WAIT_FOR_RUN = 2;
	public static final int STATUS_RUNNING = 3;
	public static final int STATUS_WAIT_FOR_DONE = 4;
	public static final int STATUS_DONE = 5;
	public static final int STATUS_BLOCKED = 5;
	
	public static void waitForStatus(int [] location, int index, int status, int timeout) {
		int i = 0;
		while(location[index] != status) {
			try {
				Thread.yield();
				Thread.sleep(100);
				Thread.yield();
			} catch (InterruptedException e) {
				
			}
			//sanity test to avoid hanging tests
			Assert.assertTrue("Timeout waiting for status to change from " + getStatus(location[index]) + " to " + getStatus(status), i++ < timeout);
		}
	}
	public static void waitForStatus(int [] location, int status) {
		waitForStatus(location, 0, status, 100);
	}

	private static String getStatus(int status) {
		switch(status) {
			case STATUS_WAIT_FOR_START:
				return "WAIT_FOR_START"; 
			case STATUS_START:
				return "START";
			case STATUS_WAIT_FOR_RUN:
				return "WAIT_FOR_RUN";
			case STATUS_RUNNING:
				return "RUNNING";
			case STATUS_WAIT_FOR_DONE:
				return "WAIT_FOR_DONE";
			case STATUS_DONE:
				return "DONE";
			default:
				return "UNKNOWN_STATUS";
		}
	}
	
}
