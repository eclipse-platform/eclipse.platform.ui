package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.xml.sax.Attributes;

public class RootEntry extends LogEntryContainer {
	private String sdkBuildId;
	private String timestamp;
	
	public RootEntry(LogEntryContainer parent, Attributes attributes) {
		this(parent, attributes.getValue("name"),
			attributes.getValue("sdkbuild"), attributes.getValue("timestamp"));
	}
	
	public RootEntry(LogEntryContainer parent, String name, String sdkBuildId, String timestamp) {
		super(parent, name);
		this.sdkBuildId = (sdkBuildId != null) ? sdkBuildId : "unknown";
		this.timestamp = (timestamp != null) ? timestamp : "unknown";
	}
	
	public void accept(ILogEntryVisitor visitor) {
		visitor.visitRootEntry(this);
	}
	
	/**
	 * Returns the SDK Build id.
	 */
	public String getSDKBuildId() {
		return sdkBuildId;
	}
	
	/**
	 * Returns the class name of the test case.
	 */
	public String getTimestamp() {
		return timestamp;
	}
}
