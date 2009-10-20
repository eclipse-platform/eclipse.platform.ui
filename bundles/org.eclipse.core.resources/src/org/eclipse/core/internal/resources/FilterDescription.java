/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.Iterator;
import java.util.LinkedList;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Class for describing the characteristics of filters that are stored
 * in the project description.
 */
public class FilterDescription implements Comparable, IResourceFilter {

	private String id;
	private Object arguments;

	/**
	 * The project relative path.
	 */
	private IPath path;
	/**
	 * The resource type (IResourceFilter.INCLUDE_ONLY or IResourceFilter.EXCLUDE_ALL) and/or IResourceFilter.INHERITABLE
	 */
	private int type;

	public FilterDescription() {
		this.path = Path.EMPTY;
		this.type = -1;
		this.id = null;
		this.arguments = null;
	}

	public FilterDescription(IResource resource, int type, String filterID, Object arguments) {
		super();
		Assert.isNotNull(resource);
		Assert.isNotNull(filterID);
		this.type = type;
		this.path = resource.getProjectRelativePath();
		this.id = filterID;
		this.arguments = arguments;
	}

	private FilterDescription(IPath projectRelativePath, int type, String filterID, Object arguments) {
		super();
		Assert.isNotNull(projectRelativePath);
		Assert.isNotNull(filterID);
		this.type = type;
		this.path = projectRelativePath;
		this.id = filterID;
		this.arguments = arguments;
	}

	public boolean equals(Object o) {
		if (!(o.getClass() == FilterDescription.class))
			return false;
		FilterDescription other = (FilterDescription) o;
		return path.equals(other.path) && type == other.type && id.equals(other.id) && ((arguments == null) ? (arguments == other.arguments) : (arguments.equals(other.arguments)));
	}

	public String getFilterID() {
		return id;
	}

	public Object getArguments() {
		return arguments;
	}

	/**
	 * Returns the project relative path of the resource that is filtered.
	 * @return the project relative path of the resource that is filtered.
	 */
	public IPath getProjectRelativePath() {
		return path;
	}

	public int getType() {
		return type;
	}

	public int hashCode() {
		return type + path.hashCode() + id.hashCode();
	}

	public void setFilterID(String id) {
		this.id = id;
	}

	public void setArguments(Object arguments) {
		this.arguments = arguments;
	}

	public void setPath(IPath path) {
		this.path = path;
	}

	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Compare filter descriptions in a way that sorts them topologically by path.
	 */
	public int compareTo(Object o) {
		FilterDescription that = (FilterDescription) o;
		IPath path1 = this.getProjectRelativePath();
		IPath path2 = that.getProjectRelativePath();
		int count1 = path1.segmentCount();
		int compare = count1 - path2.segmentCount();
		if (compare != 0)
			return compare;
		for (int i = 0; i < count1; i++) {
			compare = path1.segment(i).compareTo(path2.segment(i));
			if (compare != 0)
				return compare;
		}
		return 0;
	}

	public boolean isInheritable() {
		return (getType() & IResourceFilter.INHERITABLE) != 0;
	}

	public static LinkedList copy(LinkedList originalDescriptions, IPath projectRelativePath) {
		LinkedList copy = new LinkedList();
		Iterator it = originalDescriptions.iterator();
		while (it.hasNext()) {
			FilterDescription desc = (FilterDescription) it.next();
			FilterDescription newDesc = new FilterDescription(projectRelativePath, desc.getType(), desc.getFilterID(), desc.getArguments());
			copy.add(newDesc);
		}
		return copy;
	}

	public String getId() {
		return id;
	}

	public IPath getPath() {
		return path;
	}

	public IProject getProject() {
		return null;
	}
}
