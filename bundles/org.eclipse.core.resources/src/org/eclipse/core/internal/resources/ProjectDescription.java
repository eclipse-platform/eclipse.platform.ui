/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.internal.utils.Assert;
import java.util.*;

public class ProjectDescription extends ModelObject implements IProjectDescription {
	// fields
	protected IPath location;
	protected IProject[] projects;
	protected String[] natures;
	protected ICommand[] buildSpec;
	protected String comment =""; //$NON-NLS-1$
	protected boolean dirty = true;

	// constants
	private static IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
	private static String[] EMPTY_STRING_ARRAY = new String[0];
	private static ICommand[] EMPTY_COMMAND_ARRAY = new ICommand[0];
public ProjectDescription() {
	super();
	buildSpec = EMPTY_COMMAND_ARRAY;
	projects = EMPTY_PROJECT_ARRAY;
}
/**
 * Clears the dirty bit.  This should be used after saving descriptions to disk
 */
public void clean() {
	dirty = false;
}
/**
 * @see IProjectDescription
 */
public ICommand[] getBuildSpec() {
	return getBuildSpec(true);
}
public ICommand[] getBuildSpec(boolean makeCopy) {
	if (buildSpec == null)
		return EMPTY_COMMAND_ARRAY;
	return makeCopy ? (ICommand[]) buildSpec.clone() : buildSpec;
}
/**
 * @see IProjectDescription
 */
public String getComment() {
	return comment;
}
/**
 * @see IProjectDescription#getLocation
 */
public IPath getLocation() {
	return location;
}
/**
 * @see IProjectDescription
 */
public String[] getNatureIds() {
	return getNatureIds(true);
}
public String[] getNatureIds(boolean makeCopy) {
	if (natures == null)
		return EMPTY_STRING_ARRAY;
	return makeCopy ? (String[]) natures.clone() : natures;
}
/**
 * @see IProjectDescription
 */
public IProject[] getReferencedProjects() {
	return getReferencedProjects(true);
}
public IProject[] getReferencedProjects(boolean makeCopy) {
	if (projects == null)
		return EMPTY_PROJECT_ARRAY;
	return makeCopy ? (IProject[]) projects.clone() : projects;
}
/**
 * @see IProjectDescription#hasNature
 */
public boolean hasNature(String natureID) {
	String[] natureIDs = getNatureIds(false);
	for (int i = 0; i < natureIDs.length; ++i) {
		if (natureIDs[i].equals(natureID)) {
			return true;
		}
	}
	return false;
}
/**
 * Returns true if this resource has been modified since the last save
 * and false otherwise.
 */
public boolean isDirty() {
	return dirty;
}
/**
 * @see IProjectDescription
 */
public ICommand newCommand() {
	return new BuildCommand();
}
/**
 * @see IProjectDescription
 */
public void setBuildSpec(ICommand[] value) {
	Assert.isLegal(value != null);
	buildSpec = (ICommand[]) value.clone();
	dirty = true;
}
/**
 * @see IProjectDescription
 */
public void setComment(String value) {
	comment = value;
	dirty = true;
}
/**
 * @see IProjectDescription#setLocation
 */
public void setLocation(IPath location) {
	this.location = location;
	dirty = true;
}
/**
 * @see IProjectDescription
 */
public void setName(String value) {
	dirty = true;
	super.setName(value);
}
/**
 * @see IProjectDescription
 */
public void setNatureIds(String[] value) {
	natures = (String[]) value.clone();
	dirty = true;
}
/**
 * @see IProjectDescription
 */
public void setReferencedProjects(IProject[] value) {
	Assert.isLegal(value != null);
	IProject[] result = new IProject[value.length];
	int count = 0;
	for (int i = 0; i < value.length; i++) {
		IProject project = value[i];
		boolean found = false;
		// scan to see if there are any other projects by the same name
		for (int j = 0; j < value.length; j++) {
			if (i != j && project.equals(value[j]))
				found = true;
		}
		if (!found)
			result[count++] = project;
	}
	projects = new IProject[count];
	System.arraycopy(result, 0, projects, 0, count);
	dirty = true;
}
}
