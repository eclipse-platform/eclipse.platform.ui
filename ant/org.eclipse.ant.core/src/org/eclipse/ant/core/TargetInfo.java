package org.eclipse.ant.core;

import java.util.List;

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

	private String name = null;
	private String description = null;
	private String project = null;
	private String[] dependencies = null;
	private boolean isDefault = false;

	/**
	 * Create a target information
	 * 
	 * @param name target name
	 * @param description a brief explanation of the target's purpose
	 * 		or <code>null</code> if not specified
	 * @param project enclosing project's name
	 * @param dependencies names of prerequisite projects 
	 * @param isDefault whether this is the build file default target
	 */
	/*package*/
	TargetInfo(String name, String description, String project, String[] dependencies, boolean isDefault) {
		this.name = name == null ? "" : name; //$NON-NLS-1$
		this.description = description;
		this.project = project;
		this.dependencies = dependencies;
		this.isDefault = isDefault;
	}

	/**
	 * Returns the target name.
	 * 
	 * @return the target name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the target description or <code>null</code> if no
	 * description is provided.
	 * 
	 * @return the target description or <code>null</code> if none
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the name of the enclosing project.
	 * 	 * @return the project name	 */
	public String getProject() {
		return project;
	}
	
	/**
	 * Return the names of the targets that this target depends on.
	 * 	 * @return the dependent names	 */
	public String[] getDependencies() {
		return dependencies;
	}

	/**
	 * Returns whether this is the build file default target.
	 * 
	 * @return whether this is the build file default target
	 */
	public boolean isDefault() {
		return isDefault;
	}
}