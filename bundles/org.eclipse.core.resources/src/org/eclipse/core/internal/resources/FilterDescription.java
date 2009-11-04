/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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
public class FilterDescription implements Comparable, IResourceFilterDescription {

	private long id;

	/**
	 * The project relative path.
	 */
	private IPath path;
	/**
	 * The resource type (IResourceFilter.INCLUDE_ONLY or IResourceFilter.EXCLUDE_ALL) and/or IResourceFilter.INHERITABLE
	 */
	private int type;

	private IFileInfoMatcherDescription matcherDescription;

	public FilterDescription() {
		this.path = Path.EMPTY;
		this.type = -1;
	}

	public FilterDescription(IResource resource, int type, IFileInfoMatcherDescription matcherDescription) {
		super();
		Assert.isNotNull(resource);
		this.type = type;
		this.path = resource.getProjectRelativePath();
		this.matcherDescription = matcherDescription;
	}

	public FilterDescription(IPath projectRelativePath, int type, IFileInfoMatcherDescription matcherDescription) {
		super();
		Assert.isNotNull(projectRelativePath);
		this.type = type;
		this.path = projectRelativePath;
		this.matcherDescription = matcherDescription;
	}

	/**
	 * Compare filter descriptions in a way that sorts them topologically by path.
	 */
	public int compareTo(Object o) {
		FilterDescription that = (FilterDescription) o;
		IPath path1 = this.getPath();
		IPath path2 = that.getPath();
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
		return (getType() & IResourceFilterDescription.INHERITABLE) != 0;
	}

	public static LinkedList copy(LinkedList originalDescriptions, IPath projectRelativePath) {
		LinkedList copy = new LinkedList();
		Iterator it = originalDescriptions.iterator();
		while (it.hasNext()) {
			FilterDescription desc = (FilterDescription) it.next();
			FilterDescription newDesc = new FilterDescription(projectRelativePath, desc.getType(), desc.getFileInfoMatcherDescription());
			copy.add(newDesc);
		}
		return copy;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setPath(IPath path) {
		this.path = path;
	}

	public IPath getPath() {
		return path;
	}

	public IProject getProject() {
		return null;
	}

	public void setProject(IProject project) {
		//
	}

	public IFileInfoMatcherDescription getFileInfoMatcherDescription() {
		return matcherDescription;
	}

	public void setFileInfoMatcherDescription(IFileInfoMatcherDescription matcherDescription) {
		this.matcherDescription = matcherDescription;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilterDescription other = (FilterDescription) obj;
		if (id != other.id)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
}
