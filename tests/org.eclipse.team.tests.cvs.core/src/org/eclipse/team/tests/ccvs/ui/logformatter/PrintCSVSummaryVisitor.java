package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public class PrintCSVSummaryVisitor implements ILogEntryVisitor {
	private static final String GROUP_DELIMITER = " / ";
	private DelimitedValuesWriter writer;
	private String caseName;
	private String groupName;
	
	/**
	 * Creates a visitor to print a log as comma-separated values.
	 * @param writer the delimited values writer
	 */
	public PrintCSVSummaryVisitor(DelimitedValuesWriter writer) {
		this.writer = writer;
	}
	
	public void visitRootEntry(RootEntry entry) {
		entry.acceptChildren(this);
	}
	
	public void visitCaseEntry(CaseEntry entry) {
		caseName = entry.getName();
		groupName = null;
		entry.acceptChildren(this);
	}

	public void visitGroupEntry(GroupEntry entry) {
		String oldGroupName = groupName;
		if (groupName == null) {
			groupName = entry.getName();
		} else {
			groupName += GROUP_DELIMITER + entry.getName();
		}
		entry.acceptChildren(this);
		groupName = oldGroupName;
	}
	
	public void visitTaskEntry(TaskEntry entry) {
		writer.printFields(new String[] {
			caseName, // case
			groupName, // group
			entry.getName() // task
		});
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
		// append the result fields (ms)
		Result[] results = entry.getResults();
		for (int i = 0; i < results.length; i++) {
			Result result = results[i];
			if (result.getRuns() == 0) continue;
			writer.printField(Integer.toString(result.getMillis() / result.getRuns()));
		}
		writer.endRecord();
	}	
}
