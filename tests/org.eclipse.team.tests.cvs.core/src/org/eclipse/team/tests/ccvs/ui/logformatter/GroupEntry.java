package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.xml.sax.Attributes;

public class GroupEntry extends LogEntryContainer {
	public GroupEntry(LogEntryContainer parent, Attributes attributes) {
		this(parent, attributes.getValue("name"));
	}
	
	public GroupEntry(LogEntryContainer parent, String name) {
		super(parent, name);
	}
	
	public void accept(ILogEntryVisitor visitor) {
		visitor.visitGroupEntry(this);
	}
}
