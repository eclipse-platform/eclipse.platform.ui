package org.eclipse.team.tests.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
