package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PrintTextDiffVisitor extends PrintDiffVisitor {
	private PrintStream os;
	private List diffText; // list of things to print
	private String indent;
	
	/**
	 * Creates a diff visitor that generates text output.
	 * 
	 * @param os the output stream
	 * @see PrintDiffVisitor
	 */
	public PrintTextDiffVisitor(PrintStream os, RootEntry olderRoot, int threshold, boolean ignoreNegligible) {
		super(olderRoot, threshold, ignoreNegligible);
		this.os = os;
		this.diffText = null;
		this.indent = "";
	}

	protected void visitRootEntry(RootEntry entry, RootEntry olderEntry) {
		entry.acceptChildren(this);
	}
	
	protected void visitCaseEntry(CaseEntry entry, CaseEntry olderEntry) {
		String oldIndent = indent;
		indent += "  ";
		StringBuffer line = new StringBuffer(indent);
		line.append("%%% ");
		line.append(entry.getName());
		line.append(", class=");
		line.append(entry.getClassName());
		line.append(':');
		os.println(line);
		diffText = null;
		entry.acceptChildren(this);
		if (diffText != null) {
			Iterator it = diffText.iterator();
			while (it.hasNext()) os.println((String) it.next());
		}
		diffText = null;
		os.println();
		indent = oldIndent;
	}

	protected void visitGroupEntry(GroupEntry entry, GroupEntry olderEntry) {
		String oldIndent = indent;
		List oldDiffText = diffText;
		indent += "  ";
		diffText = null;
		entry.acceptChildren(this);
		indent = oldIndent;
		if (diffText != null) {			
			StringBuffer line = new StringBuffer(indent);
			line.append("+ ");
			line.append(entry.getName());
			line.append(':');
			diffText.add(0, line.toString());
			if (oldDiffText != null) diffText.addAll(0, oldDiffText);
		} else {
			diffText = oldDiffText;
		}
	}
	
	protected void visitTaskEntry(TaskEntry entry, TaskEntry olderEntry) {
		// print task description
		if (diffText == null) diffText = new LinkedList(); // using a list for speedy prepending
		StringBuffer line = new StringBuffer(indent);
		line.append("- ");
		line.append(entry.getName());
		line.append(": ");
		diffText.add(line.toString());
		
		// print new entry performance
		printTaskEntry("  newer: ", entry);
		
		// print older entry performance
		if (olderEntry == null) return;
		printTaskEntry("  older: ", olderEntry);
		
		// print difference
		if (entry.getTotalRuns() == 0 || olderEntry.getTotalRuns() == 0) return;
		int olderMean = olderEntry.getAverageMillis();
		int diff = entry.getAverageMillis() - olderMean;
		line = new StringBuffer(indent);
		line.append("  diff : ");
		
		if (isDifferenceUncertain(entry, olderEntry)) {
			line.append("UNCERTAIN");
		} else if (isDifferenceNegligible(entry, olderEntry)) {
			line.append("NEGLIGIBLE");
		} else {
			line.append(diff > 0 ? "SLOWER" : "FASTER");
			line.append(" by ");
			line.append(Integer.toString(Math.abs(diff)));
			line.append(" ms");
			if (olderEntry.getAverageMillis() != 0) {
				line.append(" = ");
				line.append(Util.formatPercentageRatio(Math.abs(diff), olderMean));
			}
			line.append(" avg.");
		}
		diffText.add(line.toString());
	}
	
	protected void printTaskEntry(String prefix, TaskEntry task) {
		StringBuffer line = new StringBuffer(indent);
		line.append(prefix);
		if (task.getTotalRuns() != 0) {
			int averageTime = task.getAverageMillis();
			line.append(Integer.toString(averageTime));
			line.append(" ms");
			if (task.getTotalRuns() > 1) {
				line.append(" avg. over ");
				line.append(Integer.toString(task.getTotalRuns()));
				line.append(" runs");
				if (averageTime != 0) {
					int confidence = task.getConfidenceInterval();
					line.append(" (95% C.I. +/- ");
					line.append(Integer.toString(confidence));
					line.append(" ms = ");
					line.append(Util.formatPercentageRatio(confidence, averageTime));
					line.append(")");
				}
			}
		} else {
			line.append("skipped!");
		}
		diffText.add(line.toString());
	}
}
