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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.*;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Dispenses adapters for various core objects.
 * Returns IWorkbenchAdapter adapters, used for displaying,
 * navigating, and populating menus for core objects.
 */
class WorkbenchAdapterFactory implements IAdapterFactory {
	private Object workspaceAdapter = new WorkbenchWorkspace();
	private Object rootAdapter = new WorkbenchRootResource();
	private Object projectAdapter = new WorkbenchProject();
	private Object folderAdapter = new WorkbenchFolder();
	private Object fileAdapter = new WorkbenchFile();

	private Object markerAdapter = new WorkbenchMarker();
	
	private Object resourceFactory = new ResourceFactory();
	private Object workspaceFactory = new WorkspaceFactory();
/**
 * Returns the IActionFilter for an object.
 */
protected Object getActionFilter(Object o) {
	if (o instanceof IResource) {
		switch (((IResource) o).getType()) {
			case IResource.FILE :
				return fileAdapter;
			case IResource.FOLDER :
				return folderAdapter;
			case IResource.PROJECT :
				return projectAdapter;
		}
	}
	if (o instanceof IMarker) {
		return markerAdapter;
	}
	return null;
}
/**
 * Returns an object which is an instance of the given class
 * associated with the given object. Returns <code>null</code> if
 * no such object can be found.
 *
 * @param adaptableObject the adaptable object being queried
 *   (usually an instance of <code>IAdaptable</code>)
 * @param adapterType the type of adapter to look up
 * @return a object castable to the given adapter type, 
 *    or <code>null</code> if this adapter provider 
 *    does not have an adapter of the given type for the
 *    given object
 */
public Object getAdapter(Object o, Class adapterType) {
	if (adapterType.isInstance(o)) {
		return o;
	}
	if (adapterType == IWorkbenchAdapter.class) {
		return getWorkbenchElement(o);
	}
	if (adapterType == IPersistableElement.class) {
		return getPersistableElement(o);
	}
	if (adapterType == IElementFactory.class) {
		return getElementFactory(o);
	}
	if (adapterType == IActionFilter.class) {
		return getActionFilter(o);
	}
	return null;
}
/**
 * Returns the collection of adapater types handled by this
 * provider.
 * <p>
 * This method is generally used by an adapter manager
 * to discover which adapter types are supported, in adavance
 * of dispatching any actual <code>getAdapter</code> requests.
 * </p>
 *
 * @return the collection of adapter types
 */
public Class[] getAdapterList() {
	return new Class[] {
		IWorkbenchAdapter.class,
		IElementFactory.class,
		IPersistableElement.class,
		IActionFilter.class
	};
}
/**
 * Returns an object which is an instance of IElementFactory
 * associated with the given object. Returns <code>null</code> if
 * no such object can be found.
 */
protected Object getElementFactory(Object o) {
	if (o instanceof IResource) {
		return resourceFactory;
	}
	if (o instanceof IWorkspace) {
		return workspaceFactory;
	}
	return null;
}
/**
 * Returns an object which is an instance of IPersistableElement
 * associated with the given object. Returns <code>null</code> if
 * no such object can be found.
 */
protected Object getPersistableElement(Object o) {
	if (o instanceof IResource) {
		return new ResourceFactory((IResource)o);
	}
	if (o instanceof IWorkspace) {
		return workspaceFactory;
	}
	return null;
}
/**
 * Returns an object which is an instance of IWorkbenchAdapter
 * associated with the given object. Returns <code>null</code> if
 * no such object can be found.
 */
protected Object getWorkbenchElement(Object o) {
	if (o instanceof IResource) {
		switch (((IResource) o).getType()) {
			case IResource.FILE :
				return fileAdapter;
			case IResource.FOLDER :
				return folderAdapter;
			case IResource.PROJECT :
				return projectAdapter;
		}
	}
	if (o instanceof IWorkspaceRoot) {
		return rootAdapter;
	}
	if (o instanceof IWorkspace) {
		return workspaceAdapter;
	}
	if (o instanceof IMarker) {
		return markerAdapter;
	}
	return null;
}
}
