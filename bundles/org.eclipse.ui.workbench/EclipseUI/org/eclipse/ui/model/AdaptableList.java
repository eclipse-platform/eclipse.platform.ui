/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;

/**
 * A modifiable list of <code>IAdaptable</code> objects. The list is adaptable
 * to <code>IWorkbenchAdapter</code>, and can be used to display an arbitrary
 * set of adaptable objects in a viewer.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @since 3.0
 * @see org.eclipse.ui.model.IWorkbenchAdapter
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AdaptableList extends WorkbenchAdapter implements IAdaptable {

	protected List children = null;

	/**
	 * Creates a new adaptable list. All of the elements in the list must implement
	 * <code>IAdaptable</code>.
	 */
	public AdaptableList() {
		children = new ArrayList();
	}

	/**
	 * Creates a new adaptable list with the given initial capacity. All of the
	 * elements in the list must implement <code>IAdaptable</code>.
	 *
	 * @param initialCapacity the initial capacity of the list
	 */
	public AdaptableList(int initialCapacity) {
		children = new ArrayList(initialCapacity);
	}

	/**
	 * Creates a new adaptable list containing the given children.
	 *
	 * @param newChildren the list of children
	 */
	public AdaptableList(IAdaptable[] newChildren) {
		this(newChildren.length);
		children.addAll(Arrays.asList(newChildren));
	}

	/**
	 * Creates a new adaptable list containing the elements of the specified
	 * collection, in the order they are returned by the collection's iterator. All
	 * of the elements in the list must implement <code>IAdaptable</code>.
	 *
	 * @param c the initial elements of this list (element type:
	 *          <code>IAdaptable</code>)
	 */
	public AdaptableList(Collection c) {
		this(c.size());
		children.addAll(c);
	}

	/**
	 * Adds the given adaptable object to this list.
	 *
	 * @param adaptable the new element
	 * @return this list
	 */
	public AdaptableList add(IAdaptable adaptable) {
		Assert.isNotNull(adaptable);
		children.add(adaptable);
		return this;
	}

	/**
	 * Removes the given adaptable object from this list.
	 *
	 * @param adaptable the element to remove
	 */
	public void remove(IAdaptable adaptable) {
		Assert.isNotNull(adaptable);
		children.remove(adaptable);
	}

	/**
	 * Returns the number of children in this list.
	 *
	 * @return the length of this list
	 */
	public int size() {
		return children.size();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return adapter.cast(this);
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object o) {
		// @issue suspicious - does not reference parameter
		return children.toArray();
	}

	/**
	 * Returns the elements in this list.
	 *
	 * @return the elements in this list
	 */
	public Object[] getChildren() {
		return children.toArray();
	}

	/**
	 * Return the elements in this list in an array of the given type.
	 *
	 * @param type the type of the array to create
	 * @return the elements in the list
	 * @since 3.1
	 */
	public Object[] getTypedChildren(Class type) {
		return children.toArray((Object[]) Array.newInstance(type, children.size()));
	}

	@Override
	public String toString() {
		return children.toString();
	}
}
