/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui;


public class PerformanceTimer {
	private long startTime;
	private int totalMillis;
	private String name;
	
	/**
	 * Creates a timer, initially not running.
	 */
	public PerformanceTimer(String name) {
		this.totalMillis = 0;
		this.name = name;
	}
	
	/**
	 * Starts the timer.  Timer must not be running.
	 */
	public void start() {
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * Stops the timer.  Timer must be running.
	 */
	public void stop() {
		totalMillis += System.currentTimeMillis() - startTime;
		startTime = 0;
	}
	
	/**
	 * Returns the total number of milliseconds elapsed over all measured intervals.
	 */
	public int getTotalMillis() {
		return totalMillis;
	}
	
	/**
	 * Returns the name of this timer.
	 */
	public String getName() {
		return name;
	}
}
