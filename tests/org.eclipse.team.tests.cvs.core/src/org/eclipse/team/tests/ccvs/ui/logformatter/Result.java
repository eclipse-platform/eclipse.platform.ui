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
package org.eclipse.team.tests.ccvs.ui.logformatter;


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
