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
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IResourceActionFilter;
import org.eclipse.ui.actions.SimpleWildcardTester;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * An IWorkbenchAdapter that represents IResources.
 */
public abstract class WorkbenchResource extends WorkbenchAdapter
	implements IResourceActionFilter
{
/**
 *	Answer the appropriate base image to use for the resource.
 */
protected abstract ImageDescriptor getBaseImage(IResource resource);
/**
 * Returns an image descriptor for this object.
 */
public ImageDescriptor getImageDescriptor(Object o) {
	IResource resource = getResource(o);
	return resource == null ? null : getBaseImage(resource);
}
/**
 * getLabel method comment.
 */
public String getLabel(Object o) {
	IResource resource = getResource(o);
	return resource == null ? null : resource.getName();
}
/**
 * Returns the parent of the given object.  Returns null if the
 * parent is not available.
 */
public Object getParent(Object o) {
	IResource resource = getResource(o);
	return resource == null ? null : resource.getParent();
}
/**
 * Returns the resource corresponding to this object,
 * or null if there is none.
 */
protected IResource getResource(Object o) {
	if (o instanceof IResource) {
		return (IResource)o;
	}
	if (o instanceof IAdaptable) {
		return (IResource)((IAdaptable)o).getAdapter(IResource.class);
	}
	return null;
}
/**
 * Returns whether the specific attribute matches the state of the target
 * object.
 *
 * @param target the target object
 * @param name the attribute name
 * @param value the attribute value
 * @return <code>true</code> if the attribute matches; <code>false</code> otherwise
 */
public boolean testAttribute(Object target, String name, String value) {
	if (!(target instanceof IResource)) {
		return false;
	}
	IResource res = (IResource) target;
	if (name.equals(NAME)) {
		return SimpleWildcardTester.testWildcardIgnoreCase(value, 
			res.getName());
	} else if (name.equals(PATH)) {
		return SimpleWildcardTester.testWildcardIgnoreCase(value, 
			res.getFullPath().toString());
	} else if (name.equals(EXTENSION)) {
		return SimpleWildcardTester.testWildcardIgnoreCase(value, 
			res.getFileExtension());
	} else if (name.equals(READ_ONLY)) {
		return (res.isReadOnly() == value.equalsIgnoreCase("true"));//$NON-NLS-1$
	} else if (name.equals(PROJECT_NATURE)) {
		try {
			IProject proj = res.getProject();
			return proj.isAccessible() && proj.hasNature(value);
		} catch (CoreException e) {
			return false;		
		}
	} else if (name.equals(PERSISTENT_PROPERTY)) {
		return testProperty(res, true, false, value);
	} else if (name.equals(PROJECT_PERSISTENT_PROPERTY)) {
		return testProperty(res, true, true, value);
	} else if (name.equals(SESSION_PROPERTY)) {
		return testProperty(res, false, false, value);
	} else if (name.equals(PROJECT_SESSION_PROPERTY)) {
		return testProperty(res, false, true, value);
	} else if (name.equals(XML_FIRST_TAG)) {
		return testXMLProperty(res, name, value);
	} else if (name.equals(XML_DTD_NAME)) {
		return testXMLProperty(res, name, value);
	} 
	return false;
}

/**
 * Tests whether a session or persistent property on the resource or its project
 * matches the given value.
 * 
 * @param resource the resource to check
 * @param persistentFlag <code>true</code> for a persistent property, 
 *    <code>false</code> for a session property
 * @param projectFlag <code>true</code> to check the resource's project,
 *    <code>false</code> to check the resource itself
 * @param value the attribute value, which has either the form "propertyName"
 *    or "propertyName=propertyValue"
 * @return whether there is a match
 */
private boolean testProperty(IResource resource, boolean persistentFlag, boolean projectFlag, String value) {
	String propertyName;
	String expectedVal;
	int i = value.indexOf('=');
	if (i != -1) {
		propertyName = value.substring(0, i).trim();
		expectedVal = value.substring(i+1).trim();
	}
	else {
		propertyName = value.trim();
		expectedVal = null;
	}
	try {
		QualifiedName key;
		int dot = propertyName.lastIndexOf('.');
		if (dot != -1) {
			key = new QualifiedName(propertyName.substring(0, dot), propertyName.substring(dot+1));
		}
		else {
			key = new QualifiedName(null, propertyName);
		}
		IResource resToCheck = projectFlag ? resource.getProject() : resource;
		 // getProject() on workspace root can be null
		if (resToCheck == null) {
			return false;
		}
		if (persistentFlag) {
			String actualVal = resToCheck.getPersistentProperty(key);
			if (actualVal == null) {
				return false;
			}
			return expectedVal == null || expectedVal.equals(actualVal);
		}
		else {
			Object actualVal = resToCheck.getSessionProperty(key);
			if (actualVal == null) {
				return false;
			}
			return expectedVal == null || expectedVal.equals(actualVal.toString());
		}
	} catch (CoreException e) {
		// ignore
	}
	return false;		
}

/*
 * Test whether a given xml property matches that xml
 * element in the file.  Note that these properties will
 * be stored as persistent properties.  If the underlying 
 * xml file changes, the xml will be reparsed to re-retrieve
 * these property values.
 * 
 * @param resource the resource associated with the xml file
 * @param propertyName the name of the property we are looking for
 * @param value the value we expect to find
 * @return true if the value found for this property, matches
 *     the value passed in as a parameter.
 */
private boolean testXMLProperty(IResource resource, String propertyName, String value) {
	String expectedVal;
	expectedVal = value.trim();
	try {
		QualifiedName key;
		key = new QualifiedName(resource.getLocation().toString(), propertyName);
		IResource resToCheck = resource;
		if (resToCheck == null) {
			return false;
		}
		// Check to see if the persistent properties are stale
		long modTime = resToCheck.getModificationStamp();
		QualifiedName modKey = new QualifiedName(resource.getLocation().toString(), XML_LAST_MOD);
		String lastPropMod = resToCheck.getPersistentProperty(modKey);
		long realLastPropMod = lastPropMod == null ? 0L : new Long(lastPropMod).longValue();
		String actualVal = null;
		if (modTime != IResource.NULL_STAMP && realLastPropMod == modTime) {
			// Make sure we don't pick up stale information
			actualVal = resToCheck.getPersistentProperty(key);
		} else if (realLastPropMod > 0l){
			// Make sure that these persistent properties
			// are cleared so that we don't pick up any
			// stale values by mistake.  If we've never parsed
			// this file, however, we don't need to worry
			// about stale values.
			QualifiedName qname1 = new QualifiedName(resToCheck.getLocation().toString(), IResourceActionFilter.XML_DTD_NAME);
			QualifiedName qname2 = new QualifiedName(resToCheck.getLocation().toString(), IResourceActionFilter.XML_FIRST_TAG);
			try {
				resToCheck.setPersistentProperty(qname1, null);
				resToCheck.setPersistentProperty(qname2, null);
			} catch (CoreException c) {
				IStatus status = new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.ERROR, "Problem clearing stale xml properties", c); //$NON-NLS-1$ //$NON-NLS-2$
				Platform.getPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH).getLog().log(status); 
			}
		}
		
		// Either we have never parsed this file or we 
		// have parsed it but the file has changed since
		// the last time it was parsed.
		if (actualVal == null) {
			try {
				new PropertyParser().parseResource(resToCheck);
			} catch (Exception e) {
				IStatus status = new Status(IStatus.ERROR,IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.ERROR, "Problem parsing for xml properties", e); //$NON-NLS-1$ //$NON-NLS-2$
				Platform.getPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH).getLog().log(status); 
			}
			// Now recheck the persistent property as it may have
			// been populated.
			actualVal = resToCheck.getPersistentProperty(key);
			if (actualVal == null)
				return false;
		}
		return expectedVal == null || expectedVal.equals(actualVal);
	} catch (CoreException e) {
		// Just output a message to the log file and continue
		IStatus status = new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.ERROR, "Problem testing xml property", e); //$NON-NLS-1$ //$NON-NLS-2$
		Platform.getPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH).getLog().log(status); 
	}
	return false;		
}

}
