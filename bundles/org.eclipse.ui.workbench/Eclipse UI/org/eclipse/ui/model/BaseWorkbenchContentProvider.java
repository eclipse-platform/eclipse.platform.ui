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
 *******************************************************************************/
package org.eclipse.ui.model;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Tree content provider for objects that can be adapted to the interface
 * {@link org.eclipse.ui.model.IWorkbenchAdapter IWorkbenchAdapter}.
 * <p>
 * This class may be instantiated, or subclassed.
 * </p>
 *
 * @see IWorkbenchAdapter
 * @since 3.0
 */
public class BaseWorkbenchContentProvider implements ITreeContentProvider {

	/**
	 * Creates a new workbench content provider.
	 */
	public BaseWorkbenchContentProvider() {
		super();
	}

	@Override
	public void dispose() {
		// do nothing
	}

	/**
	 * Returns the implementation of IWorkbenchAdapter for the given object. Returns
	 * null if the adapter is not defined or the object is not adaptable.
	 *
	 * @param element the element
	 * @return the corresponding workbench adapter object
	 */
	protected IWorkbenchAdapter getAdapter(Object element) {
		return Adapters.adapt(element, IWorkbenchAdapter.class);
	}

	@Override
	public Object[] getChildren(Object element) {
		IWorkbenchAdapter adapter = getAdapter(element);
		if (adapter != null) {
			return adapter.getChildren(element);
		}
		return new Object[0];
	}

	@Override
	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	@Override
	public Object getParent(Object element) {
		IWorkbenchAdapter adapter = getAdapter(element);
		if (adapter != null) {
			return adapter.getParent(element);
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

}
