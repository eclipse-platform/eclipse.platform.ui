package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

public interface ILogEntryVisitor {
	public void visitRootEntry(RootEntry entry);
	public void visitCaseEntry(CaseEntry entry);
	public void visitGroupEntry(GroupEntry entry);
	public void visitTaskEntry(TaskEntry entry);
}
