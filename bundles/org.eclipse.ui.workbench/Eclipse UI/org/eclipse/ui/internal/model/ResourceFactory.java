/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal.model;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.*;

/**
 * The ResourceFactory is used to save and recreate an IResource object.
 * As such, it implements the IPersistableElement interface for storage
 * and the IElementFactory interface for recreation.
 *
 * @see IMemento
 * @see IPersistableElement
 * @see IElementFactory
 */
public class ResourceFactory implements IElementFactory, IPersistableElement {
	
	// These persistence constants are stored in XML.  Do not
	// change them.
	private static final String TAG_PATH = "path";//$NON-NLS-1$
	private static final String TAG_TYPE = "type";//$NON-NLS-1$
	private static final String FACTORY_ID = "org.eclipse.ui.internal.model.ResourceFactory";//$NON-NLS-1$

	// IPersistable data.
	private IResource res;
/**
 * Create a ResourceFactory.  This constructor is typically used
 * for our IElementFactory side.
 */
public ResourceFactory() {
}
/**
 * Create a ResourceFactory.  This constructor is typically used
 * for our IPersistableElement side.
 */
public ResourceFactory(IResource input) {
	res = input;
}
/**
 * @see IElementFactory
 */
public IAdaptable createElement(IMemento memento) {
	// Get the file name.
	String fileName = memento.getString(TAG_PATH);
	if (fileName == null)
		return null;

	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	IPath path = new Path(fileName); 
	String type = memento.getString(TAG_TYPE);
	if (type == null) {
		// Old format memento. Create an IResource using findMember. 
		// Will return null for resources in closed projects.
		res = root.findMember(path);
	}
	else {
		int resourceType = Integer.parseInt(type);
		
		if (resourceType == IResource.ROOT) {
			res = root;		
		}
		else {
			IPath location = root.getLocation().append(path);	
			if (resourceType == IResource.PROJECT || resourceType == IResource.FOLDER) {
				res = root.getContainerForLocation(location);
			}
			else if (resourceType == IResource.FILE) {
				res = root.getFileForLocation(location);
			}
		}
	}
	return res;	
}
/**
 * @see IPersistableElement.
 */
public String getFactoryId() {
	return FACTORY_ID;
}
/**
 * @see IPersistableElement
 */
public void saveState(IMemento memento) {
	memento.putString(TAG_PATH, res.getFullPath().toString());			
	memento.putString(TAG_TYPE, Integer.toString(res.getType()));
}
}
