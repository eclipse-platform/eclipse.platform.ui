package org.eclipse.team.internal.ccvs.core.response.custom;
import org.eclipse.team.internal.ccvs.core.response.*;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class CVSTag {
	// Tag is a branch
	public final static int BRANCH_TAG = 1;
	// Tag is a version
	public final static int VERSION_TAG = 2;
	
	// Tag name
	private String name;
    // Tag type
	private int type;
	
	public CVSTag(String name, int type) {
		this.name = name;
		this.type = type;
	}
	/**
	 * Returns the tag name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Returns the tag type
	 */
	public int getType() {
		return type;
	}
}

