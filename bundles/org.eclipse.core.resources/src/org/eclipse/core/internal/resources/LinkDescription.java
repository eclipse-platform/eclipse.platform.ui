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

import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Object for describing the characteristics of linked resources that are stored
 * in the project description.
 */
public class LinkDescription {
	private String name;
	private IPath localLocation;
	private int type;
public LinkDescription(IResource linkedResource, IPath location) {
	super();
	Assert.isNotNull(linkedResource);
	Assert.isNotNull(location);
	this.type = linkedResource.getType();
	this.name = linkedResource.getName();
	this.localLocation = location;
}
public LinkDescription(String name, int type, IPath localLocation) {
	this.name = name;
	this.type = type;
	this.localLocation = localLocation;
}
public LinkDescription() {
	this.name = ""; //$NON-NLS-1$
	this.type = -1;
	this.localLocation = Path.EMPTY;
}
public boolean equals(Object o) {
	if (!(o.getClass() == LinkDescription.class))
		return false;
	LinkDescription other = (LinkDescription)o;
	return localLocation.equals(other.localLocation) && type == other.type;
}
public IPath getLocation() {
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
public void setName (String name) {
	this.name = name;
}
public void setType (int type) {
	this.type = type;
}
public void setLocation(IPath location) {
	this.localLocation = location;
}
}
