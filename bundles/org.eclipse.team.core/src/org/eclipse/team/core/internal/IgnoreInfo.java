package org.eclipse.team.core.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.core.IIgnoreInfo;

public class IgnoreInfo implements IIgnoreInfo {
	private String pattern;
	private boolean enabled;
	
	IgnoreInfo(String pattern, boolean enabled) {
		this.pattern = pattern;
		this.enabled = enabled;
	}
	public String getPattern() {
		return pattern;
	}
	public boolean getEnabled() {
		return enabled;
	}
}
