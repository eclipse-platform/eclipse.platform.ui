/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
	private static final ICommand[] EMPTY_COMMAND_ARRAY = new ICommand[0];
	// constants
	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	protected static boolean isReading = false;

	//flags to indicate when we are in the middle of reading or writing a
	// workspace description
	//these can be static because only one description can be read at once.
	protected static boolean isWriting = false;
	protected ICommand[] buildSpec = EMPTY_COMMAND_ARRAY;
	protected String comment = ""; //$NON-NLS-1$
	protected String defaultCharset;
	protected HashMap linkDescriptions = null;

	// fields
	protected IPath location = null;
	protected String[] natures = EMPTY_STRING_ARRAY;
	protected IProject[] staticRefs = EMPTY_PROJECT_ARRAY;
	protected IProject[] dynamicRefs = EMPTY_PROJECT_ARRAY;
	/*
	 * Cached union of static and dynamic references (duplicates omitted).
	 * This cache is not persisted.
	 */
	protected IProject[] cachedRefs = null;

	public ProjectDescription() {
		super();
	}

	public Object clone() {
		ProjectDescription clone = (ProjectDescription) super.clone();
		//don't want the clone to have access to our internal link locations
		// table
		clone.linkDescriptions = null;
		return clone;
	}
	/**
	 * Returns a copy of the given array with all duplicates removed
	 */
	private IProject[] copyAndRemoveDuplicates(IProject[] projects) {
		IProject[] result = new IProject[projects.length];
		int count = 0;
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			boolean found = false;
			// scan to see if there are any other projects by the same name
			for (int j = 0; j < count; j++)
				if (project.equals(result[j]))
					found = true;
			if (!found)
				result[count++] = project;
		}
		if (count < projects.length) {
			//shrink array
			IProject[] reduced = new IProject[count];
			System.arraycopy(result, 0, reduced, 0, count);
			return reduced;
		}
		return result;
	}
	/**
	 * Returns the union of the description's static and dyamic project references,
	 * with duplicates omitted. The calculation is optimized by caching the result
	 */
	public IProject[] getAllReferences(boolean makeCopy) {
		if (cachedRefs == null) {
			IProject[] statik = getReferencedProjects(false);
			IProject[] dynamic = getDynamicReferences(false);
			if (dynamic.length == 0) {
				cachedRefs = statik;
			} else if (statik.length == 0) {
				cachedRefs = dynamic;
			} else {
				//combine all references
				IProject[] result = new IProject[dynamic.length+statik.length];
				System.arraycopy(statik, 0, result, 0, statik.length);
				System.arraycopy(dynamic, 0, result, statik.length, dynamic.length);
				cachedRefs = copyAndRemoveDuplicates(result);
			}
		}
		//still need to copy the result to prevent tampering with the cache
		return makeCopy ? (IProject[])cachedRefs.clone() : cachedRefs;
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
	 * @see IProjectDescription
	 */
	public String getDefaultCharset() {
		return defaultCharset;
	}	
	/**
	 * @see IProjectDescription
	 */
	public IProject[] getDynamicReferences() {
		return getDynamicReferences(true);
	}
	public IProject[] getDynamicReferences(boolean makeCopy) {
		if (dynamicRefs == null)
			return EMPTY_PROJECT_ARRAY;
		return makeCopy ? (IProject[]) dynamicRefs.clone() : dynamicRefs;
	}
	/**
	 * Returns the link location for the given resource name. Returns null if
	 * no such link exists.
	 */
	public IPath getLinkLocation(String name) {
		if (linkDescriptions == null)
			return null;
		LinkDescription desc = (LinkDescription) linkDescriptions.get(name);
		return desc == null ? null : desc.getLocation();
	}
	/**
	 * Returns the map of link descriptions (String name -> LinkDescription).
	 * Since this method is only used internally, it never creates a copy.
	 * Returns null if the project does not have any linked resources.
	 * 
	 * @return HashMap
	 */
	public HashMap getLinks() {
		return linkDescriptions;
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
		if (staticRefs == null)
			return EMPTY_PROJECT_ARRAY;
		return makeCopy ? (IProject[]) staticRefs.clone() : staticRefs;
	}
	/**
	 * @see IProjectDescription#hasNature
	 */
	public boolean hasNature(String natureID) {
		String[] natureIDs = getNatureIds(false);
		for (int i = 0; i < natureIDs.length; ++i)
			if (natureIDs[i].equals(natureID))
				return true;
		return false;
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
	}
	/**
	 * @see IProjectDescription
	 */
	public void setComment(String value) {
		comment = value;
	}
	/**
	 * @see IProjectDescription
	 */
	public void setDefaultCharset(String value) {
		defaultCharset = value;
	}
	/**
	 * @see IProjectDescription
	 */
	public void setDynamicReferences(IProject[] value) {
		Assert.isLegal(value != null);
		dynamicRefs = copyAndRemoveDuplicates(value);
		cachedRefs = null;
	}
	/**
	 * Sets the map of link descriptions (String name -> LinkDescription).
	 * Since this method is only used internally, it never creates a copy. May
	 * pass null if this project does not have any linked resources
	 * 
	 * @return HashMap
	 */
	public void setLinkDescriptions(HashMap linkDescriptions) {
		this.linkDescriptions = linkDescriptions;
	}
	/**
	 * Sets the description of a link. Setting to a description of null will
	 * remove the link from the project description.
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
	 * @see IProjectDescription#setLocation
	 */
	public void setLocation(IPath location) {
		this.location = location;
	}
	/**
	 * @see IProjectDescription
	 */
	public void setName(String value) {
		super.setName(value);
	}
	/**
	 * @see IProjectDescription
	 */
	public void setNatureIds(String[] value) {
		natures = (String[]) value.clone();
	}
	/**
	 * @see IProjectDescription
	 */
	public void setReferencedProjects(IProject[] value) {
		Assert.isLegal(value != null);
		staticRefs = copyAndRemoveDuplicates(value);
		cachedRefs = null;
	}
}
