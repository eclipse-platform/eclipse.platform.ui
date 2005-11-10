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
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.resources.IResource;

/**
 * Object for describing the characteristics of linked resources that are stored
 * in the project description.
 */
public class LinkDescription {
	
	private String name;
	private URI localLocation;
	private int type;

	public LinkDescription(IResource linkedResource, URI location) {
		super();
		Assert.isNotNull(linkedResource);
		Assert.isNotNull(location);
		this.type = linkedResource.getType();
		this.name = linkedResource.getName();
		this.localLocation = location;
	}

	public LinkDescription(String name, int type, URI localLocation) {
		this.name = name;
		this.type = type;
		this.localLocation = localLocation;
	}

	public LinkDescription() {
		this.name = ""; //$NON-NLS-1$
		this.type = -1;
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

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public int hashCode() {
		return type + localLocation.hashCode();
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setLocation(URI location) {
		this.localLocation = location;
	}
}
