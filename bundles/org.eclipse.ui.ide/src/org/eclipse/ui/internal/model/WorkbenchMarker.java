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

package org.eclipse.ui.internal.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMarkerActionFilter;
import org.eclipse.ui.actions.SimpleWildcardTester;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Model object for adapting IMarker objects to the IWorkbenchAdapter
 * interface.
 */
public class WorkbenchMarker
	extends WorkbenchAdapter
	implements IMarkerActionFilter {
/**
 * @see IWorkbenchAdapter#getImageDescriptor
 */
public ImageDescriptor getImageDescriptor(Object o) {
	if(!(o instanceof IMarker))
			return null;
	return WorkbenchPlugin.getDefault().getMarkerImageProviderRegistry().getImageDescriptor((IMarker)o);
}
/**
 * Returns the name of this element.  This will typically
 * be used to assign a label to this object when displayed
 * in the UI.
 */
public String getLabel(Object o) {
	IMarker marker = (IMarker) o;
	return marker.getAttribute(IMarker.MESSAGE, "");//$NON-NLS-1$
}
/**
 * Returns the logical parent of the given object in its tree.
 */
public Object getParent(Object o) {
	return ((IMarker)o).getResource();
}
/**
 * Returns whether the specific attribute matches the state of the target
 * object.
 *
 * @param taret the target object
 * @param name the attribute name
 * @param value the attriute value
 * @return <code>true</code> if the attribute matches; <code>false</code> otherwise
 */
public boolean testAttribute(Object target, String name, String value) {
	IMarker marker = (IMarker) target;
	if (name.equals(TYPE)) {
		try {
			return value.equals(marker.getType());
		} catch (CoreException e) {
			return false;
		}
	} else if (name.equals(SUPER_TYPE)) {
		try {
			return marker.isSubtypeOf(value);
		} catch (CoreException e) {
			return false;
		}
	} else if (name.equals(PRIORITY)) {
		return testIntegerAttribute(marker, IMarker.PRIORITY, value);
	} else if (name.equals(SEVERITY)) {
		return testIntegerAttribute(marker, IMarker.SEVERITY, value);
	} else if (name.equals(MESSAGE)) {
		try {
			String msg = (String)marker.getAttribute(IMarker.MESSAGE);
			if (msg == null)
				return false;
			return SimpleWildcardTester.testWildcardIgnoreCase(value, msg);
		} catch (CoreException e) {
			return false;
		}
	} else if (name.equals(DONE)) {
		try {
			value = value.toLowerCase();
			Boolean done = (Boolean)marker.getAttribute(IMarker.DONE);
			if (done == null)
				return false;
			return (done.booleanValue() == value.equals("true"));//$NON-NLS-1$
		} catch (CoreException e) {
			return false;
		}
	} else if (name.equals(RESOURCE_TYPE)) {
		int desiredType = 0;
		
		try {
			desiredType = Integer.parseInt(value);
		} catch (NumberFormatException eNumberFormat) {			
		}
		
		if (!(desiredType == IResource.FILE || desiredType == IResource.FOLDER ||  
			desiredType == IResource.PROJECT || desiredType == IResource.ROOT))
			return false;
				
		return (marker.getResource().getType() & desiredType) > 0;  
	}
	return false;
}
/**
 * Returns whether the specific integer attribute matches a value.
 */
private boolean testIntegerAttribute(IMarker marker, String attrName, String value) {
	Integer i1, i2;
	try {
		i1 = (Integer)marker.getAttribute(attrName);
		if (i1 == null)
			return false;
	} catch (CoreException e) {
		return false;
	}
	try {
		i2 = Integer.valueOf(value);
	} catch (NumberFormatException e) {
		return false;
	}
	return i1.equals(i2);
}
}
