/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.properties;

import org.eclipse.core.runtime.IPath;

public class ResourceName {
	protected String qualifier = null;
	protected IPath path = null;
public ResourceName(String qualifier, IPath path) {
	super();
	this.qualifier = qualifier;
	this.path = path;
}
public boolean equals(Object other) {
	if (this == other)
		return true;
	if (!(other instanceof ResourceName))
		return false;
	ResourceName otherName = (ResourceName) other;
	if (qualifier == null) {
		if (otherName.getQualifier() != null)
			return false;
	} else
		if (!qualifier.equals(otherName.getQualifier()))
			return false;
	return path.equals(otherName.getPath());
}
public IPath getPath() {
	return path;
}
public String getQualifier() {
	return qualifier;
}
public int hashCode() {
	return path.hashCode();
}
public String toString() {
	return getQualifier() + " " + getPath().toString(); //$NON-NLS-1$
}
}
