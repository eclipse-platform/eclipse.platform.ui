package org.eclipse.core.tests.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;

public class StandAloneTestEnvironment implements TestEnvironment {
	
	protected final static String sep = File.separator;
	protected final static String driveLetter = "c:";
	protected final static String directoryName = driveLetter + sep + "tests" + sep;
	protected final static String fileName = directoryName + "test.dat";

	private PrintWriter out;
	
	public StandAloneTestEnvironment() {
//		out = new PrintWriter(System.out, true);
		try {
			out = new PrintWriter(new FileWriter(directoryName + sep + "test.txt"), true);
		} catch (IOException e) {
		}
	}
	
	public String getFileName() {
		return fileName;
	}

	public void print(String s) {
		if (out != null) out.print(s);
	}
	
	public void print(int n, int width) {
		StringBuffer b = new StringBuffer(width);
		String s = Integer.toString(n);
		if (s.length() > width) {
			for (int i = 0; i < width; i++) b.append("#");
		} else {
			for (int i = 0; i < width - s.length(); i++) b.append(" ");
			b.append(s);
		}
		print(b.toString());
	}

	public void println(String s) {
		if (out != null) out.println(s);
	}
	
	public void printHeading(String s) {
		if (out != null) {
			out.println();
			out.println(s);
		}
	}

}
