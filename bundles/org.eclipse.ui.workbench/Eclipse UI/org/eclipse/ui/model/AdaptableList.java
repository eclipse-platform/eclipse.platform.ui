/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.model;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A modifiable list of <code>IAdaptable</code> objects.
 * The list is adaptable to <code>IWorkbenchAdapter</code>, and can be used to
 * display an arbitrary set of adaptable objects in a viewer.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * 
 * @since 3.0
 * @see org.eclipse.ui.model.IWorkbenchAdapter
 */
public final class AdaptableList extends ArrayList implements IAdaptable {
	
	/**
	 * Workbench adapter for this list.
	 */
	private class WorkbenchAdapter implements IWorkbenchAdapter {
		public ImageDescriptor getImageDescriptor(Object object) {
			// list has no image
			return null;
		}
		public String getLabel(Object o) {
			// list has no label
			return null;
		}
		public Object getParent(Object o) {
			// list has no parent
			return null;
		}
		public Object[] getChildren(Object o) {
			// list's elements are the children
			return toArray();
		}
	}

	/**
	 * Workbench adapter for this list.
	 */
	private final WorkbenchAdapter myAdapter = new WorkbenchAdapter();

	/**
	 * Creates a new adaptable list. All of the elements in the list must 
	 * implement <code>IAdaptable</code>.
	 */
	public AdaptableList() {
		super();
	}

	/**
	 * Creates a new adaptable list with the given initial capacity.
	 * All of the elements in the list must implement <code>IAdaptable</code>.
	 * 
	 * @param initialCapacity the initial capacity of the list
	 */
	public AdaptableList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Creates a new adaptable list containing the elements of the specified
	 * collection, in the order they are returned by the collection's iterator.
	 * All of the elements in the list must implement <code>IAdaptable</code>.
	 * 
	 * @param c the initial elements of this list (element type: 
	 * <code>IAdaptable</code>)
	 */
	public AdaptableList(Collection c) {
		super(c);
	}

	/* (non-Javadoc)
	 * @see IAdaptable#getAdapter
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return myAdapter;
		}
		return null;
	}
}
