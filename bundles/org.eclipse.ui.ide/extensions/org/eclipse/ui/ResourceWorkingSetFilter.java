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
package org.eclipse.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A resource working set filter filters resources from a view that
 * are neither a parent nor children of a working set element.
 *
 * @since 2.0
 */
public class ResourceWorkingSetFilter extends ViewerFilter {
	private IWorkingSet workingSet = null;

	private IAdaptable[] cachedWorkingSet = null;

	/**
	 * Returns the active working set the filter is working with.
	 *
	 * @return the active working set
	 */
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}

	/**
	 * Sets the active working set.
	 *
	 * @param workingSet the working set the filter should work with
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
	}

	/**
	 * Determines if an element should be filtered out.
	 *
	 * @see ViewerFilter#select(Viewer, Object, Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (workingSet == null || (workingSet.isAggregateWorkingSet() &&
				workingSet.isEmpty())) {
			return true;
		}

		IResource resource = Adapters.adapt(element, IResource.class);
		if (resource != null) {
			return isEnclosed(resource);
		}
		return true;
	}

	/**
	 * Returns if the given resource is enclosed by a working set element.
	 * The IContainmentAdapter of each working set element is used for the
	 * containment test. If there is no IContainmentAdapter for a working
	 * set element, a simple resource based test is used.
	 *
	 * @param element resource to test for enclosure by a working set
	 * 	element
	 * @return true if element is enclosed by a working set element and
	 * 	false otherwise.
	 */
	private boolean isEnclosed(IResource element) {
		IPath elementPath = element.getFullPath();
		IAdaptable[] workingSetElements = cachedWorkingSet;

		// working set elements won't be cached if select is called
		// directly, outside filter. fixes bug 14500.
		if (workingSetElements == null) {
			workingSetElements = workingSet.getElements();
		}

		for (IAdaptable workingSetElement : workingSetElements) {
			IContainmentAdapter containmentAdapter = workingSetElement.getAdapter(IContainmentAdapter.class);

			// if there is no IContainmentAdapter defined for the working
			// set element type fall back to using resource based
			// containment check
			if (containmentAdapter != null) {
				if (containmentAdapter.contains(workingSetElement, element,
						IContainmentAdapter.CHECK_CONTEXT
								| IContainmentAdapter.CHECK_IF_CHILD
								| IContainmentAdapter.CHECK_IF_ANCESTOR
								| IContainmentAdapter.CHECK_IF_DESCENDANT)) {
					return true;
				}
			} else if (isEnclosedResource(element, elementPath,
					workingSetElement)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns if the given resource is enclosed by a working set element.
	 * A resource is enclosed if it is either a parent of a working set
	 * element, a child of a working set element or a working set element
	 * itself.
	 * Simple path comparison is used. This is only guaranteed to return
	 * correct results for resource working set elements.
	 *
	 * @param element resource to test for enclosure by a working set
	 * 	element
	 * @param elementPath full, absolute path of the element to test
	 * @return true if element is enclosed by a working set element and
	 * 	false otherwise.
	 */
	private boolean isEnclosedResource(IResource element, IPath elementPath,
			IAdaptable workingSetElement) {
		if (workingSetElement.equals(element)) {
			return true;
		}
		IResource workingSetResource = Adapters.adapt(workingSetElement, IResource.class);
		if (workingSetResource != null) {
			IPath resourcePath = workingSetResource.getFullPath();
			if (resourcePath.isPrefixOf(elementPath)) {
				return true;
			}
			if (elementPath.isPrefixOf(resourcePath)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Filters out elements that are neither a parent nor a child of
	 * a working set element.
	 *
	 * @see ViewerFilter#filter(Viewer, Object, Object[])
	 */
	@Override
	public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
		Object[] result = null;
		if (workingSet != null) {
			cachedWorkingSet = workingSet.getElements();
		}
		try {
			result = super.filter(viewer, parent, elements);
		} finally {
			cachedWorkingSet = null;
		}
		return result;
	}
}
