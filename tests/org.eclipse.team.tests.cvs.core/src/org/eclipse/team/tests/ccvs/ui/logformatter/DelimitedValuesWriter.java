package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

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
