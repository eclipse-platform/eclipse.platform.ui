package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.xml.sax.SAXException;

public class PrintDiffMain {
	public static void main(String[] args) {
		Parser parser = new Parser();
		if (! parser.parse(args)) {
			System.err.println("Usage: <newer log> <older log> [-out <file>] [-csv] [-t <thresh>] [-i]");
			System.err.println("  -out <file> : specify the output file, default is console");
			System.err.println("  -csv        : produce comma separated values data");
			System.err.println("  -t <thresh> : minimum non-negligible absolute % change");
			System.err.println("  -i          : ignore negligible changes in results");
			return;
		}
		try {
			PrintStream ps = System.out;
			try {
				if (parser.outputFile != null) ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(parser.outputFile)));
				printDiff(ps, parser.newerLogFile, parser.olderLogFile, parser.csv, parser.thresh, parser.ignore);
			} finally {
				if (ps != System.out) ps.close();
			}
		} catch (Exception e) {
			System.err.println("An error occurred:");
			e.printStackTrace();
			return;
		}
	}

	private static void printDiff(PrintStream ps, File newerLogFile, File olderLogFile,
		boolean csv, int thresh, boolean ignore) throws IOException, SAXException {
		// read and merge newer log
		RootEntry newerRoot = LogEntry.readLog(newerLogFile);
		MergeRunsVisitor mergeVisitor = new MergeRunsVisitor(null);
		newerRoot.accept(mergeVisitor);
		newerRoot = mergeVisitor.getMergedRoot();

		// read and merge older log
		RootEntry olderRoot = LogEntry.readLog(olderLogFile);
		olderRoot.accept(mergeVisitor);
		olderRoot = mergeVisitor.getMergedRoot();

		// format options
		StringBuffer options = new StringBuffer();
		if (thresh != 0) {
			options.append("-t ");
			options.append(Integer.toString(thresh));
			options.append(" ");
		}
		if (ignore) options.append("-i ");

		// format log file
		if (csv) {
			DelimitedValuesWriter writer = new DelimitedValuesWriter(ps, ",", true /*quoted*/);
			// print header
			writer.printRecord(new String[] { "", "Newer", "Older" });
			writer.printRecord(new String[] { "Log File", newerLogFile.toString(), olderLogFile.toString() });
			writer.printRecord(new String[] { "Generated", newerRoot.getTimestamp(), olderRoot.getTimestamp() });
			writer.printRecord(new String[] { "SDK Build", newerRoot.getSDKBuildId(), olderRoot.getSDKBuildId() });
			writer.endRecord();
			writer.printRecord(new String[] { "Options", "'" + options.toString() });
			writer.endRecord();
			writer.printRecord(new String[] { "", "", "",
				"Newer", "", "", "",
				"Older", "", "", "",
				"", "", "" });
			writer.printRecord(new String[] { "Case", "Group", "Task",
				"Runs", "Avg. (ms)", "95% C.I. (ms)", "95% C.I. (%)",
				"Runs", "Avg. (ms)", "95% C.I. (ms)", "95% C.I. (%)",
				"Change", "Diff (ms)", "Diff (%)" });
			// print quoted CSV data
			PrintCSVDiffVisitor diffVisitor = new PrintCSVDiffVisitor(writer, olderRoot, thresh, ignore);
			newerRoot.accept(diffVisitor);
		} else {
			// print header
			ps.println("=== LOG DIFF ===");
			ps.println("Newer File: " + newerLogFile);
			ps.println("  Generated: " + newerRoot.getTimestamp());
			ps.println("  SDK Build: " + newerRoot.getSDKBuildId());
			ps.println("Older File: " + olderLogFile);
			ps.println("  Generated: " + olderRoot.getTimestamp());
			ps.println("  SDK Build: " + olderRoot.getSDKBuildId());
			ps.println("Options: " + options.toString());
			ps.println();
			// compute and print the differences
			PrintTextDiffVisitor diffVisitor = new PrintTextDiffVisitor(ps, olderRoot, thresh, ignore);
			newerRoot.accept(diffVisitor);
		}
	}
	
	private static class Parser extends ArgumentParser {
		public File newerLogFile = null;
		public File olderLogFile = null;
		public File outputFile = null;
		public boolean csv = false;
		public int thresh = 0;
		public boolean ignore = false;

		protected boolean handleFinished() {
			return newerLogFile != null && olderLogFile != null;
		}
		protected boolean handleArgument(int index, String arg) {
			if (index == 0) {
				newerLogFile = new File(arg);
			} else if (index == 1) {
				olderLogFile = new File(arg);
			} else {
				return false;
			}
			return true;
		}
		protected boolean handleOption(String option, String arg) {
			if ("-out".equals(option)) {
				if (arg == null) return false;
				outputFile = new File(arg);
			} else if ("-csv".equals(option)) {
				if (arg != null) return false;
				csv = true;
			} else if ("-t".equals(option)) {
				if (arg == null) return false;
				try {
					thresh = Integer.parseInt(arg, 10);
				} catch (NumberFormatException e) {
					return false;
				}
				if (thresh < 0) return false;
			} else if ("-i".equals(option)) {
				if (arg != null) return false;
				ignore = true;
			} else {
				return false;
			}
			return true;
		}
	}
}
