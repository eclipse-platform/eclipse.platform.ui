package org.eclipse.ant.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * Represents information about a target within an Ant build file.
 * @since 2.1
 */
public class TargetInfo {

	private String fName = null;
	private String fDescription = null;
	private boolean fIsDefault = false;

	/**
	 * Create a target information
	 * 
	 * @param name target name
	 * @param description a brief explanation of the target's purpose
	 * 		or <code>null</code> if not specified
	 * @param isDefault whether this is the build file default target
	 */
	/*package*/
	TargetInfo(String name, String description, boolean isDefault) {
		fName = name == null ? "" : name; //$NON-NLS-1$
		fDescription = description;
		fIsDefault = isDefault;
	}

	/**
	 * Returns the target name.
	 * 
	 * @return the target name
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Returns the target description or <code>null</code> if no
	 * description is provided.
	 * 
	 * @return the target description or <code>null</code> if none
	 */
	public String getDescription() {
		return fDescription;
	}

	/**
	 * Returns whether this is the build file default target.
	 * 
	 * @return whether this is the build file default target
	 */
	public boolean isDefault() {
		return fIsDefault;
	}
}