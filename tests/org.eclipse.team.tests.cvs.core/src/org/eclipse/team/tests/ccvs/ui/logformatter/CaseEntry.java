package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.xml.sax.Attributes;

public class CaseEntry extends LogEntryContainer {
	private String className;
	
	public CaseEntry(LogEntryContainer parent, Attributes attributes) {
		this(parent, attributes.getValue("name"), attributes.getValue("class"));
	}
	
	public CaseEntry(LogEntryContainer parent, String name, String className) {
		super(parent, name);
		this.className = (className != null) ? className : "unknown";
	}
	
	public void accept(ILogEntryVisitor visitor) {
		visitor.visitCaseEntry(this);
	}
	
	/**
	 * Returns the class name of the test case.
	 */
	public String getClassName() {
		return className;
	}	
}
