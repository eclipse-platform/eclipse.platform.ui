package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.xml.sax.Attributes;

/**
 * Holds the result of one iteration of tests.
 * Note that a test might be run multiple times per iteration, particularly if it
 * is of very short duration to reduce sampling error.  This behaviour is not supported
 * at this time, but will likely be of value in the future.
 */
public class Result {
	private int runs;
	private int millis;

	public Result(Attributes attributes) {
		this(1, Integer.parseInt(attributes.getValue("elapsed")));
	}
	public Result(int runs, int millis) {
		this.runs = runs;
		this.millis = millis;
	}
	public int getRuns() {
		return runs;
	}
	public int getMillis() {
		return millis;
	}
}
