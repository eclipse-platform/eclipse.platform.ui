/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.HashMap;

import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;

public class ProjectDescription extends ModelObject implements IProjectDescription {
	// constants
	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final ICommand[] EMPTY_COMMAND_ARRAY = new ICommand[0];
	
	// fields
	protected IPath location = null;
	protected IProject[] projects = EMPTY_PROJECT_ARRAY;
	protected String[] natures = EMPTY_STRING_ARRAY;
	protected ICommand[] buildSpec = EMPTY_COMMAND_ARRAY;
	protected HashMap linkDescriptions = null;
	protected String comment = ""; //$NON-NLS-1$
	protected boolean dirty = true;
	
	//flags to indicate when we are in the middle of reading or writing a workspace description
	//these can be static because only one description can be read at once.
	protected static boolean isWriting = false;
	protected static boolean isReading = false;
	
public ProjectDescription() {
	super();
}

/**
 * Clears the dirty bit.  This should be used after saving descriptions to disk
 */
public void clean() {
	dirty = false;
}
public Object clone() {
	ProjectDescription clone = (ProjectDescription) super.clone();
	//don't want the clone to have access to our internal link locations table
	clone.linkDescriptions = null;
	return clone;
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
 * Returns the link location for the given resource name.  Returns null
 * if no such link exists.
 */
public IPath getLinkLocation(String name) {
	if (linkDescriptions == null)
		return null;
	LinkDescription desc = (LinkDescription)linkDescriptions.get(name);
	return desc == null ? null : desc.getLocation();
}
/**
 * Returns the map of link descriptions (String name -> LinkDescription).
 * Since this method is only used internally, it never creates a copy.
 * Returns null if the project does not have any linked resources.
 * @return HashMap
 */
public HashMap getLinks() {
	return linkDescriptions;
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
 * Sets the description of a link.  Setting to a description
 * of null will remove the link from the project description.
 */
public void setLinkLocation(String name, LinkDescription description) {
	if (description != null) {
		//addition or modification
		if (linkDescriptions == null)
			linkDescriptions = new HashMap(10);
		linkDescriptions.put(name, description);
	} else {
		//removal
		if (linkDescriptions != null) {
			linkDescriptions.remove(name);
			if (linkDescriptions.size() == 0)
				linkDescriptions = null;
		}
	}
}
/**
 * Sets the map of link descriptions (String name -> LinkDescription).
 * Since this method is only used internally, it never creates a copy.
 * May pass null if this project does not have any linked resources
 * @return HashMap
 */
public void setLinkDescriptions(HashMap linkDescriptions) {
	this.linkDescriptions = linkDescriptions;
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
