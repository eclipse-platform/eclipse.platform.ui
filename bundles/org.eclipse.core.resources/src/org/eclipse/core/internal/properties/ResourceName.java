package org.eclipse.core.internal.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
//
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
	return getQualifier() + " " + getPath().toString();
}
}
