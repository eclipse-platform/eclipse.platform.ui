package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public class PrintCSVDiffVisitor extends PrintDiffVisitor {
	private static final String GROUP_DELIMITER = " / ";
	private DelimitedValuesWriter writer;
	private String caseName;
	private String groupName;

	/**
	 * Creates a diff visitor that generates CSV output.
	 * 
	 * @param writer the delimited values writer
	 * @see PrintDiffVisitor
	 */
	public PrintCSVDiffVisitor(DelimitedValuesWriter writer, RootEntry olderRoot, int threshold, boolean ignoreNegligible) {
		super(olderRoot, threshold, ignoreNegligible);
		this.writer = writer;
	}
	
	protected void visitRootEntry(RootEntry entry, RootEntry olderEntry) {
		entry.acceptChildren(this);
	}
	
	protected void visitCaseEntry(CaseEntry entry, CaseEntry olderEntry) {
		caseName = entry.getName();
		groupName = null;
		entry.acceptChildren(this);
	}
	
	protected void visitGroupEntry(GroupEntry entry, GroupEntry olderEntry) {
		String oldGroupName = groupName;
		if (groupName == null) {
			groupName = entry.getName();
		} else {
			groupName += GROUP_DELIMITER + entry.getName();
		}
		entry.acceptChildren(this);
		groupName = oldGroupName;
	}
	
	protected void visitTaskEntry(TaskEntry entry, TaskEntry olderEntry) {
		writer.printFields(new String[] {
			caseName, // case
			groupName, // group
			entry.getName() // task
		});
		printTaskEntry(entry);
		printTaskEntry(olderEntry);
		if (entry.getTotalRuns() != 0 && olderEntry.getTotalRuns() != 0) {
			int olderMean = olderEntry.getAverageMillis();
			int diff = entry.getAverageMillis() - olderMean;
			if (isNegligible(entry, olderEntry)) {
				writer.printField("NEGLIGIBLE");
			} else {
				writer.printField(diff > 0 ? "SLOWER" : "FASTER"); // change type
			}
			writer.printField(Integer.toString(Math.abs(diff))); // change
			if (olderMean != 0) {
				writer.printField(Util.formatPercentageRatio(Math.abs(diff), olderMean)); // % change
			} else {
				writer.printField("");
			}
		} else {
			writer.printFields(new String[] { "", "", "" });
		}
		writer.endRecord();
	}
	
	protected void printTaskEntry(TaskEntry entry) {
		if (entry.getTotalRuns() != 0) {
			int mean = entry.getAverageMillis();
			writer.printFields(new String[] {
				Integer.toString(entry.getTotalRuns()), // runs
				Integer.toString(mean) // average
			});
			if (entry.getTotalRuns() > 1 && mean != 0) {
				int confidence = entry.getConfidenceInterval();
				writer.printFields(new String[] {
					Integer.toString(confidence), // 95% confidence interval
					Util.formatPercentageRatio(confidence, mean) // 95% c.i. as a percentage
				});
			} else {
				writer.printFields(new String[] { "", "" });
			}
		} else {
			writer.printFields(new String[] { "0", "", "", "" });
		}
	}
}
