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


import java.io.PrintStream;

public class DelimitedValuesWriter {
	private static final String BEGIN_QUOTE = "\"";
	private static final String END_QUOTE = "\"";
	private PrintStream ps;
	private String delimiter;
	private boolean quoted;
	private boolean firstField;
	
	public DelimitedValuesWriter(PrintStream ps, String delimiter, boolean quoted) {
		this.ps = ps;
		this.delimiter = delimiter;
		this.quoted = quoted;
		this.firstField = true;
	}
	
	public void printField(String field) {
		if (firstField) {
			firstField = false;
		} else {
			ps.print(delimiter);
		}
		if (quoted) ps.print(BEGIN_QUOTE);
		ps.print(field);
		if (quoted) ps.print(END_QUOTE);
	}
	public void printFields(String[] fields) {
		for (int i = 0; i < fields.length; i++) {
			printField(fields[i]);
		}
	}
	public void printRecord(String[] fields) {
		printFields(fields);
		endRecord();
	}
	public void endRecord() {
		ps.println();
		firstField = true;
	}
}
