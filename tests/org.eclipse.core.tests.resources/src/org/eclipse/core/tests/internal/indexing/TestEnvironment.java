package org.eclipse.core.tests.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface TestEnvironment {
	
	/**
	 * Returns the file name which will be used as a target for a
	 * particular test suite.  This will be the name of an IndexedStore, 
	 * an ObjectStore, or a PageStore file.
	 */
	public String getFileName();

	/**
	 * Prints a string on an output stream.  All testcase strings are informational only.
	 */
	public void print(String s);
	
	/**
	 * Prints a string on an output stream.  All testcase strings are informational only.
	 */
	public void println(String s);

	/**
	 * Prints a number on an output stream.  All testcase strings are informational only.
	 */
	public void print(int n, int width);

	/**
	 * Prints a blank line first then a full line
	 */
	public void printHeading(String s);
}
