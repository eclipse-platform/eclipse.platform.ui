package org.eclipse.core.internal.resources;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

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
	fail(Policy.bind("natures.invalidDefinition", id));
}
protected void fail(String reason) throws CoreException {
	throw new ResourceException(
		new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, 1, reason, null));
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
		fail(Policy.bind("natures.missingIdentifier"));
	}
	label = natureExtension.getLabel();
	IConfigurationElement[] elements = natureExtension.getConfigurationElements();
	int count = elements.length;
	ArrayList requiredList = new ArrayList(count);
	ArrayList setList = new ArrayList(count);
	for (int i = 0; i < count; i++) {
		IConfigurationElement element = elements[i];
		String name = element.getName();
		if (name.equalsIgnoreCase("requires-nature")) {
			String attribute = element.getAttribute("id");
			if (attribute == null)
				fail();
			requiredList.add(attribute);
		} else if (name.equalsIgnoreCase("one-of-nature")) {
			String attribute = element.getAttribute("id");
			if (attribute == null)
				fail();
			setList.add(attribute);
		}
	}
	requiredNatures = (String[])requiredList.toArray(new String[requiredList.size()]);
	natureSets = (String[])setList.toArray(new String[setList.size()]);;	
}
/**
 * Prints out a string representation for debugging purposes only.
 */
public String toString() {
	return "ProjectNatureDescriptor(" + id + ")";
}
}