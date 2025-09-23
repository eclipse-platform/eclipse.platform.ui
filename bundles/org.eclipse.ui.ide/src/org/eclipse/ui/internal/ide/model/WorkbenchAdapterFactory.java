/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fair Isaac Corporation <Hemant.Singh@Gmail.com> - http://bugs.eclipse.org/333590
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 461762
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.eclipse.ui.model.IWorkbenchAdapter3;

/**
 * Dispenses adapters for various core objects.
 * Returns IWorkbenchAdapter adapters, used for displaying,
 * navigating, and populating menus for core objects.
 */
public class WorkbenchAdapterFactory implements IAdapterFactory {

	private final WorkbenchWorkspace workspaceAdapter = new WorkbenchWorkspace();

	private final WorkbenchRootResource rootAdapter = new WorkbenchRootResource();

	private final WorkbenchProject projectAdapter = new WorkbenchProject();

	private final WorkbenchFolder folderAdapter = new WorkbenchFolder();

	private final WorkbenchFile fileAdapter = new WorkbenchFile();

	private final WorkbenchMarker markerAdapter = new WorkbenchMarker();

	private final ResourceFactory resourceFactory = new ResourceFactory();

	private final WorkspaceFactory workspaceFactory = new WorkspaceFactory();

	/**
	 * Returns the IActionFilter for an object.
	 */
	protected IActionFilter getActionFilter(Object o) {
		if (o instanceof IResource) {
			switch (((IResource) o).getType()) {
			case IResource.FILE:
				return fileAdapter;
			case IResource.FOLDER:
				return folderAdapter;
			case IResource.PROJECT:
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
	 * @param o the adaptable object being queried
	 *   (usually an instance of <code>IAdaptable</code>)
	 * @param adapterType the type of adapter to look up
	 * @return a object castable to the given adapter type,
	 *    or <code>null</code> if this adapter provider
	 *    does not have an adapter of the given type for the
	 *    given object
	 */
	@Override
	public <T> T getAdapter(Object o, Class<T> adapterType) {
		if (adapterType.isInstance(o)) {
			return adapterType.cast(o);
		}
		if (adapterType == IWorkbenchAdapter.class
				|| adapterType == IWorkbenchAdapter2.class
				|| adapterType == IWorkbenchAdapter3.class) {
			return adapterType.cast(getWorkbenchElement(o));
		}
		if (adapterType == IPersistableElement.class) {
			return adapterType.cast(getPersistableElement(o));
		}
		if (adapterType == IElementFactory.class) {
			return adapterType.cast(getElementFactory(o));
		}
		if (adapterType == IActionFilter.class) {
			return adapterType.cast(getActionFilter(o));
		}
		if (adapterType == IUndoContext.class) {
			return adapterType.cast(getUndoContext(o));
		}
		return null;
	}

	/**
	 * Returns the collection of adapter types handled by this
	 * provider.
	 * <p>
	 * This method is generally used by an adapter manager
	 * to discover which adapter types are supported, in advance
	 * of dispatching any actual <code>getAdapter</code> requests.
	 * </p>
	 *
	 * @return the collection of adapter types
	 */
	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class, IWorkbenchAdapter2.class,
				IWorkbenchAdapter3.class, IElementFactory.class,
				IPersistableElement.class, IActionFilter.class,
				IUndoContext.class };
	}

	/**
	 * Returns an object which is an instance of IElementFactory
	 * associated with the given object. Returns <code>null</code> if
	 * no such object can be found.
	 */
	protected IElementFactory getElementFactory(Object o) {
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
	protected IPersistableElement getPersistableElement(Object o) {
		if (o instanceof IResource) {
			return new ResourceFactory((IResource) o);
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
	protected IWorkbenchAdapter getWorkbenchElement(Object o) {
		if (o instanceof IResource) {
			switch (((IResource) o).getType()) {
			case IResource.FILE:
				return fileAdapter;
			case IResource.FOLDER:
				return folderAdapter;
			case IResource.PROJECT:
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

	/**
	 * Returns the IUndoContext for an object.
	 */
	protected IUndoContext getUndoContext(Object o) {
		if (o instanceof IWorkspace) {
			return PlatformUI.getWorkbench().getOperationSupport().getUndoContext();
		}
		return null;
	}
}
