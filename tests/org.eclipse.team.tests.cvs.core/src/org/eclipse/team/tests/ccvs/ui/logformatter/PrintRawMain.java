package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.PrintStream;

public class PrintRawMain {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java PrintSummaryMain <log>");
			return;
		}
		File file = new File(args[0]);
		try {
			// read the log
			RootEntry root = LogEntry.readLog(file);

			// print header
			PrintStream ps = System.out;
			ps.println("=== RAW TEST LOG SUMMARY ===");
			ps.println("File: " + file);
			ps.println("  Generated: " + root.getTimestamp());
			ps.println("  SDK Build: " + root.getSDKBuildId());
			ps.println();

			// print the log summary
			root.accept(new PrintSummaryVisitor(ps));
		} catch (Exception e) {
			System.err.println("An error occurred while parsing: " + file);
			e.printStackTrace();
			return;
		}
	}
}
