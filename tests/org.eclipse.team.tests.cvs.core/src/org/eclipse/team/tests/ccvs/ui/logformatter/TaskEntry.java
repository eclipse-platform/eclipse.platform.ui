package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.xml.sax.Attributes;

public class TaskEntry extends LogEntry {
	private int totalMillis = 0;
	private int totalRuns = 0;
	
	public TaskEntry(LogEntryContainer parent, Attributes attributes) {
		this(parent, attributes.getValue("name"));
	}
	
	public TaskEntry(LogEntryContainer parent, String name) {
		super(parent, name);
	}
	
	public void accept(ILogEntryVisitor visitor) {
		visitor.visitTaskEntry(this);
	}
	
	/**
	 * Returns the average number of milliseconds elapsed, or -1 if unknown.
	 */
	public int getAverageMillis() {
		if (totalRuns == 0) return -1;
		return totalMillis / totalRuns;
	}
	
	/**
	 * Returns the total number over all runs, or 0 if no runs.
	 */
	public int getTotalMillis() {
		return totalMillis;
	}
	
	/**
	 * Returns the number of times this task was run.
	 */
	public int getTotalRuns() {
		return totalRuns;
	}
	
	/**
	 * Adds a set of runs.
	 * @param millisElapsed the number of milliseconds elapsed over all runs
	 * @param runs the number of runs to add
	 */
	public void addRuns(int millisElapsed, int runs) {
		totalMillis += millisElapsed;
		totalRuns += runs;
	}
	
	/*
	 * Adds some results corresponding to this task.
	 */
	void addResult(Attributes attributes) {
		int elapsed = 0;
		int runs = 0;
		boolean aborted = false;
		String value = attributes.getValue("elapsed");
		if (value != null) {
			elapsed = Integer.parseInt(value, 10);
			runs = 1;
		}
		addRuns(elapsed, runs);
	}
}
