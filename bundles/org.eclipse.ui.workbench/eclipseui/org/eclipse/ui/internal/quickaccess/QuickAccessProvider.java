/*******************************************************************************
4 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.quickaccess;

import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * Returns {@link QuickAccessElement}s. It implements a cache by default.
 *
 * @noreference This class is not intended to be referenced by clients.
 */
public abstract class QuickAccessProvider {

	/*
	 * Cached elements that are always returned
	 */
	private QuickAccessElement[] cacheSortedElements;

	/**
	 * Returns the unique ID of this provider.
	 *
	 * @return the unique ID
	 */
	public abstract String getId();

	/**
	 * Returns the name of this provider to be displayed to the user.
	 *
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * Returns the image descriptor for this provider.
	 *
	 * @return the image descriptor, or null if not defined
	 */
	public abstract ImageDescriptor getImageDescriptor();

	/**
	 * Returns the elements provided by this provider in all circumstances. Elements
	 * get filter downstream in the process, so this list can be greedy. The result
	 * may be cached and reused so this method may be invoked only once for a
	 * QuickAccess session.
	 *
	 * @return this provider's elements
	 */
	public abstract QuickAccessElement[] getElements();

	/**
	 * Returns the elements provided by this provider in all circumstances. Elements
	 * get filter downstream in the process, so this list can be greedy. The result
	 * isn't cached and reused, so this method will be invoked whenever user change
	 * input.
	 *
	 * @param filter  user input
	 * @return this provider's elements
	 */
	public QuickAccessElement[] getElements(String filter, IProgressMonitor monitor) {
		return new QuickAccessElement[0];
	}

	public QuickAccessElement[] getElementsSorted(String filter, IProgressMonitor monitor) {
		if (cacheSortedElements == null) {
			cacheSortedElements = getElements();
			if (cacheSortedElements == null) {
				cacheSortedElements = new QuickAccessElement[0];
			}
			Arrays.sort(cacheSortedElements, Comparator.comparing(QuickAccessElement::getSortLabel));
		}
		if (filter == null) {
			return cacheSortedElements;
		}
		QuickAccessElement[] filterSpecificElements = getElements(filter, monitor);
		if (filterSpecificElements == null || filterSpecificElements.length == 0) {
			return cacheSortedElements;
		}
		SortedSet<QuickAccessElement> res = new TreeSet<>(Comparator.comparing(QuickAccessElement::getSortLabel));
		res.addAll(Arrays.asList(cacheSortedElements));
		res.addAll(Arrays.asList(filterSpecificElements));
		return res.toArray(new QuickAccessElement[res.size()]);
	}

	/**
	 * Returns the element for the given ID if available, or null if no matching
	 * element is available.
	 *
	 * @param id         the ID of an element
	 * @param filterText optional, user filter that was used to find this element
	 *                   first. May be null.
	 * @return the element with the given ID, or null if not found.
	 */
	public QuickAccessElement findElement(String id, String filterText) {
		if (id == null) {
			return null;
		}
		if (cacheSortedElements != null) {
			for (QuickAccessElement element : cacheSortedElements) {
				if (id.equals(element.getId())) {
					return element;
				}
			}
		}
		return null;
	}

	/**
	 * Resets the cache, so next invocation of {@link #getElements()} and related
	 * method will retrigger computation of elements.
	 */
	public final void reset() {
		cacheSortedElements = null;
		doReset();
	}

	/**
	 * Additional operations to reset cache.
	 *
	 * @noreference This method is not intended to be referenced by clients. Use
	 *              {@link #reset()} instead.
	 */
	protected abstract void doReset();

	/**
	 * @return {@code true} if this provider requires UI operations to load its
	 *         elements, {@code false} otherwise.
	 */
	public boolean requiresUiAccess() {
		return false;
	}

}
