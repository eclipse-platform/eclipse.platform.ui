/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.net.URI;
import org.eclipse.core.internal.utils.OldAssert;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Object for describing the characteristics of linked resources that are stored
 * in the project description.
 */
public class LinkDescription {

	private URI localLocation;

	/**
	 * The project relative path.
	 */
	private IPath path;
	/**
	 * The resource type (IResource.FILE or IResoruce.FOLDER)
	 */
	private int type;

	public LinkDescription() {
		this.path = Path.EMPTY;
		this.type = -1;
	}

	public LinkDescription(IResource linkedResource, URI location) {
		super();
		OldAssert.isNotNull(linkedResource);
		OldAssert.isNotNull(location);
		this.type = linkedResource.getType();
		this.path = linkedResource.getProjectRelativePath();
		this.localLocation = location;
	}

	public boolean equals(Object o) {
		if (!(o.getClass() == LinkDescription.class))
			return false;
		LinkDescription other = (LinkDescription) o;
		return localLocation.equals(other.localLocation) && type == other.type;
	}

	public URI getLocation() {
		return localLocation;
	}

	/**
	 * Returns the project relative path of the resource that is linked.
	 * @return the project relative path of the resource that is linked.
	 */
	public IPath getPath() {
		return path;
	}

	public int getType() {
		return type;
	}

	public int hashCode() {
		return type + localLocation.hashCode();
	}

	public void setLocation(URI location) {
		this.localLocation = location;
	}

	public void setPath(IPath path) {
		this.path = path;
	}

	public void setType(int type) {
		this.type = type;
	}
}
