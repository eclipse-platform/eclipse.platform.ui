package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;

public class PrintAverageMain {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java PrintAverageMain <log>");
			return;
		}
		File file = new File(args[0]);
		try {
			// read and merge the log
			RootEntry root = LogEntry.readLog(file);
			MergeRunsVisitor mergeVisitor = new MergeRunsVisitor(null);
			root.accept(mergeVisitor);
			root = mergeVisitor.getMergedRoot();
			// print the log summary
			root.accept(new PrintSummaryVisitor(System.out));
		} catch (Exception e) {
			System.err.println("An error occurred while parsing: " + file);
			e.printStackTrace();
			return;
		}
	}
}
