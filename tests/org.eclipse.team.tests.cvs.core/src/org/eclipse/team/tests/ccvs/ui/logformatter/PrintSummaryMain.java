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

public class PrintSummaryMain {
	public static void main(String[] args) {
		Parser parser = new Parser();
		if (! parser.parse(args)) {
			System.err.println("Usage: <log file> [-out <file>] [-csv] [-raw]");
			System.err.println("  -out <file> : specify the output file, default is console");
			System.err.println("  -csv        : produce comma separated values data");
			System.err.println("  -raw        : do not merge results from successive iterations");
			return;
		}
		try {
			PrintStream ps = System.out;
			try {
				if (parser.outputFile != null) ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(parser.outputFile)));
				printLog(ps, parser.logFile, parser.csv, parser.raw);
			} finally {
				if (ps != System.out) ps.close();
			}
		} catch (Exception e) {
			System.err.println("An error occurred:");
			e.printStackTrace();
			return;
		}
	}

	private static void printLog(PrintStream ps, File logFile, boolean csv, boolean raw)
		throws IOException, SAXException {
		// read and merge the log
		RootEntry root = LogEntry.readLog(logFile);
		if (! raw) {
			MergeRunsVisitor mergeVisitor = new MergeRunsVisitor(null);
			root.accept(mergeVisitor);
			root = mergeVisitor.getMergedRoot();
		}

		// format options
		StringBuffer options = new StringBuffer();
		if (raw) options.append("-raw ");

		// format log file
		if (csv) {
			DelimitedValuesWriter writer = new DelimitedValuesWriter(ps, ",", true /*quoted*/);
			// print header
			writer.printRecord(new String[] { "Log File", logFile.toString() });
			writer.printRecord(new String[] { "Generated", root.getTimestamp() });
			writer.printRecord(new String[] { "SDK Build", root.getSDKBuildId() });
			writer.endRecord();
			writer.printRecord(new String[] { "Options", "'" + options.toString() });
			writer.endRecord();
			writer.printRecord(new String[] { "Case", "Group", "Task",
				"Runs", "Avg. (ms)", "95% C.I. (ms)", "95% C.I. (%)", "Results (ms)" });
			// print quoted CSV data
			PrintCSVSummaryVisitor visitor = new PrintCSVSummaryVisitor(writer);
			root.accept(visitor);
		} else {
			// print header
			ps.println("=== LOG SUMMARY ===");
			ps.println("File: " + logFile);
			ps.println("  Generated: " + root.getTimestamp());
			ps.println("  SDK Build: " + root.getSDKBuildId());
			ps.println("Options: " + options.toString());
			ps.println();
			// print the log summary
			root.accept(new PrintTextSummaryVisitor(ps));
		}
	}
	
	private static class Parser extends ArgumentParser {
		public File logFile = null;
		public File outputFile = null;
		public boolean csv = false;
		public boolean raw = false;

		protected boolean handleFinished() {
			return logFile != null;
		}
		protected boolean handleArgument(int index, String arg) {
			if (index == 0) {
				logFile = new File(arg);
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
			} else if ("-raw".equals(option)) {
				if (arg != null) return false;
				raw = true;
			} else {
				return false;
			}
			return true;
		}
	}
}
