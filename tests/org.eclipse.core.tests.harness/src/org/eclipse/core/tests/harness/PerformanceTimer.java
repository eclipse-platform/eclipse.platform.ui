/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.harness;

/**
 * The timer class used by performance tests.
 */
class PerformanceTimer {
	private String fName;
	private long fElapsedTime;
	private long fStartTime;
/**
 * 
 */
public PerformanceTimer(String name) {
	fName = name;
	fElapsedTime = 0;
	fStartTime = 0;
}
/**
 * Return the elapsed time.
 */
public long getElapsedTime() {
	return fElapsedTime;
}
/**
 * Return the timer name.
 */
public String getName() {
	return fName;
}
/**
 * Start the timer.
 */
public void startTiming() {
	fStartTime = System.currentTimeMillis();
}
/**
 * Stop the timer, add the elapsed time to the total.
 */
public void stopTiming() {
	if (fStartTime == 0) return;
	long timeNow = System.currentTimeMillis();
	fElapsedTime += (timeNow - fStartTime);
	fStartTime = 0;
}
}
