package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public abstract class PrintDiffVisitor implements ILogEntryVisitor {
	protected RootEntry olderRoot;
	protected int threshold; // threshold for negligible changes
	protected boolean ignoreNegligible; // if true, ignores negligible changes
	protected LogEntryContainer olderParent; // corresponding parent in older root

	/**
	 * Creates a visitor to print a summary of the changes between a log
	 * and an older one.  Optionally ignores differences within a certain threshold.
	 * Does not print older entries for which there are no corresponding newer ones.
	 * 
	 * @param olderRoot the root of the older log
	 * @param threshold the minimum non-negligible % change
	 * @param ignoreNegligible if true, does not display negligible changes
	 */
	public PrintDiffVisitor(RootEntry olderRoot, int threshold, boolean ignoreNegligible) {
		this.olderRoot = olderRoot;
		this.olderParent = null;
		this.threshold = threshold;
		this.ignoreNegligible = ignoreNegligible;
	}
	
	protected abstract void visitRootEntry(RootEntry entry, RootEntry olderEntry);
	protected abstract void visitCaseEntry(CaseEntry entry, CaseEntry olderEntry);
	protected abstract void visitGroupEntry(GroupEntry entry, GroupEntry olderEntry);
	protected abstract void visitTaskEntry(TaskEntry entry, TaskEntry olderEntry);

	public void visitRootEntry(RootEntry entry) {
		olderParent = olderRoot;
		visitRootEntry(entry, olderRoot);
	}
	
	public void visitCaseEntry(CaseEntry entry) {
		LogEntryContainer prevOlderParent = olderParent;
		if (olderParent != null) {
			olderParent = (LogEntryContainer) olderParent.findMember(entry.getName(), CaseEntry.class);
		}
		visitCaseEntry(entry, (CaseEntry) olderParent);
		olderParent = prevOlderParent;
	}

	public void visitGroupEntry(GroupEntry entry) {
		LogEntryContainer prevOlderParent = olderParent;
		if (olderParent != null) {
			olderParent = (LogEntryContainer) olderParent.findMember(entry.getName(), GroupEntry.class);
		}
		visitGroupEntry(entry, (GroupEntry) olderParent);
		olderParent = prevOlderParent;
	}

	public void visitTaskEntry(TaskEntry entry) {
		TaskEntry olderEntry = null;
		if (olderParent != null) {
			olderEntry = (TaskEntry) olderParent.findMember(entry.getName(), TaskEntry.class);
		}
		if (ignoreNegligible && isDifferenceNegligible(entry, olderEntry)) return;
		visitTaskEntry(entry, olderEntry);
	}
	
	protected boolean isDifferenceNegligible(TaskEntry newerEntry, TaskEntry olderEntry) {
		if (newerEntry.getTotalRuns() == 0 || olderEntry.getTotalRuns() == 0) return false;
		int olderMean = olderEntry.getAverageMillis();
		if (olderMean == 0) return false;
		int newerMean = newerEntry.getAverageMillis();
		int diff = Math.abs(newerMean - olderMean);
		return diff * 100 / olderMean < threshold;
	}
	
	protected boolean isDifferenceUncertain(TaskEntry newerEntry, TaskEntry olderEntry) {
		if (newerEntry.getTotalRuns() == 0 || olderEntry.getTotalRuns() == 0) return false;
		int olderMean = olderEntry.getAverageMillis();
		int newerMean = newerEntry.getAverageMillis();
		int diff = Math.abs(newerMean - olderMean);
		int diffCI = newerEntry.getConfidenceInterval() + olderEntry.getConfidenceInterval();
		return diff < diffCI;
	}
}
