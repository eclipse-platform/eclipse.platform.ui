package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PrintDiffVisitor implements ILogEntryVisitor {
	private PrintStream os;
	private RootEntry olderRoot;
	private int threshold; // threshold for negligible changes
	private boolean ignoreNegligible; // if true, ignores negligible changes
	private LogEntryContainer olderParent; // corresponding parent in older root
	private List diffText; // list of things to print
	private String indent;
	
	/**
	 * Creates a visitor to print a summary of the changes between a log
	 * and an older one.  Optionally ignores differences within a certain threshold.
	 * Does not print older entries for which there are no corresponding newer ones.
	 * 
	 * @param os the output stream
	 * @param olderRoot the root of the older log
	 * @param threshold the smallest non-negligible difference
	 * @param ignoreNegligible if true, does not display negligible changes
	 */
	public PrintDiffVisitor(PrintStream os, RootEntry olderRoot, int threshold, boolean ignoreNegligible) {
		this.os = os;
		this.olderRoot = olderRoot;
		this.olderParent = null;
		this.threshold = threshold;
		this.ignoreNegligible = ignoreNegligible;
		this.diffText = null;
		this.indent = "";
	}
	
	private void visitContainer(LogEntryContainer container) {
		LogEntryContainer prevOlderParent = olderParent;
		indent += "  ";
		if (olderParent != null) {
			olderParent = (LogEntryContainer) olderParent.findMember(
				container.getName(), container.getClass());
		}
		container.acceptChildren(this);
		indent = indent.substring(2);
		olderParent = prevOlderParent;
	}

	/**
	 * Prints the root entry information.
	 */
	public void visitRootEntry(RootEntry entry) {
		olderParent = olderRoot;
		entry.acceptChildren(this);
	}
	
	/**
	 * Visits all of the subgroups and subtasks of the newer log's test cases
	 */
	public void visitCaseEntry(CaseEntry entry) {
		StringBuffer line = new StringBuffer(indent);
		line.append("%%% ");
		line.append(entry.getName());
		line.append(", class=");
		line.append(entry.getClassName());
		line.append(':');
		os.println(line);
		diffText = null;
		visitContainer(entry);
		if (diffText != null) {
			Iterator it = diffText.iterator();
			while (it.hasNext()) os.println((String) it.next());
		}
		diffText = null;
		os.println();
	}

	/**
	 * Visits all of the subgroups and subtasks of the newer log's test cases
	 */
	public void visitGroupEntry(GroupEntry entry) {
		List oldDiffText = diffText;
		diffText = null;
		visitContainer(entry);
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
	
	/**
	 * Prints the average amount of time spent by a task.
	 */
	public void visitTaskEntry(TaskEntry task) {
		int diff = Integer.MAX_VALUE;
		TaskEntry olderTask = null;
		if (olderParent != null) {
			olderTask = (TaskEntry) olderParent.findMember(task.getName(), TaskEntry.class);
			if (olderTask != null && task.getTotalRuns() != 0 && olderTask.getTotalRuns() != 0) {
				diff = task.getAverageMillis() - olderTask.getAverageMillis();
				// ignore negligible changes
				if (ignoreNegligible && Math.abs(diff) <= threshold) return;
			}
		}
		// print task description
		if (diffText == null) diffText = new LinkedList(); // using a list for speedy prepending
		StringBuffer line = new StringBuffer(indent);
		line.append("- ");
		line.append(task.getName());
		line.append(": ");
		diffText.add(line.toString());
		
		// print new entry performance
		printTaskEntry("  newer: ", task);
		
		// print older entry performance
		if (olderTask == null) return;
		printTaskEntry("  older: ", olderTask);
		
		// print difference
		if (diff == Integer.MAX_VALUE) return;
		line = new StringBuffer(indent);
		line.append("  diff : ");
		if (Math.abs(diff) > threshold) {
			if (diff > 0) line.append("SLOWER");
			else line.append("FASTER");
			line.append(" by ");
			line.append(Integer.toString(Math.abs(diff)));
			line.append(" ms avg.");
		} else {
			line.append("NEGLIGIBLE");
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
			}
		} else {
			line.append("skipped!");
		}
		diffText.add(line.toString());
	}
}
