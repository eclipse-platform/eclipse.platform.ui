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

import java.util.ArrayList;

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;

/**
 */
public class ProjectNatureDescriptor implements IProjectNatureDescriptor {
	protected String id;
	protected String label;
	protected String[] requiredNatures;
	protected String[] natureSets;
	protected String[] builderIds;
	
	//descriptors that are in a dependency cycle are never valid
	protected boolean hasCycle = false;
	//colours used by cycle detection algorithm
	protected byte colour = 0;
	
/**
 * Creates a new descriptor based on the given extension markup.
 * @exception CoreException if the given nature extension is not correctly formed.
 */
protected ProjectNatureDescriptor(IExtension natureExtension) throws CoreException {
	readExtension(natureExtension);
}
protected void fail() throws CoreException {
	fail(Policy.bind("natures.invalidDefinition", id)); //$NON-NLS-1$
}
protected void fail(String reason) throws CoreException {
	throw new ResourceException(
		new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, 1, reason, null));
}
/**
 * Returns the IDs of the incremental builders that this nature claims to
 * own.  These builders do not necessarily exist in the registry.
 */
public String[] getBuilderIds() {
	return builderIds;
}
/**
 * @see IProjectNatureDescriptor#getNatureId()
 */
public String getNatureId() {
	return id;
}
/**
 * @see IProjectNatureDescriptor#getLabel()
 */
public String getLabel() {
	return label;
}
/**
 * @see IProjectNatureDescriptor#getRequiredNatureIds()
 */
public String[] getRequiredNatureIds() {
	return requiredNatures;
}
/**
 * @see IProjectNatureDescriptor#getNatureSetIds()
 */
public String[] getNatureSetIds() {
	return natureSets;
}
/**
 * Initialize this nature descriptor based on the provided extension point.
 */
protected void readExtension(IExtension natureExtension) throws CoreException {
	//read the extension
	id = natureExtension.getUniqueIdentifier();
	if (id == null) {
		fail(Policy.bind("natures.missingIdentifier")); //$NON-NLS-1$
	}
	label = natureExtension.getLabel();
	IConfigurationElement[] elements = natureExtension.getConfigurationElements();
	int count = elements.length;
	ArrayList requiredList = new ArrayList(count);
	ArrayList setList = new ArrayList(count);
	ArrayList builderList = new ArrayList(count);
	for (int i = 0; i < count; i++) {
		IConfigurationElement element = elements[i];
		String name = element.getName();
		if (name.equalsIgnoreCase("requires-nature")) { //$NON-NLS-1$
			String attribute = element.getAttribute("id"); //$NON-NLS-1$
			if (attribute == null)
				fail();
			requiredList.add(attribute);
		} else if (name.equalsIgnoreCase("one-of-nature")) { //$NON-NLS-1$
			String attribute = element.getAttribute("id"); //$NON-NLS-1$
			if (attribute == null)
				fail();
			setList.add(attribute);
		} else if (name.equalsIgnoreCase("builder")) { //$NON-NLS-1$
			String attribute = element.getAttribute("id"); //$NON-NLS-1$
			if (attribute == null)
				fail();
			builderList.add(attribute);
		}
	}
	requiredNatures = (String[])requiredList.toArray(new String[requiredList.size()]);
	natureSets = (String[])setList.toArray(new String[setList.size()]);;	
	builderIds = (String[]) builderList.toArray(new String[builderList.size()]);
}
/**
 * Prints out a string representation for debugging purposes only.
 */
public String toString() {
	return "ProjectNatureDescriptor(" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
}
}