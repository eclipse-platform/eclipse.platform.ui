/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 * 
 */
public abstract class QuickAccessProvider {

	private QuickAccessElement[] sortedElements;

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
	 * Returns the elements provided by this provider.
	 * 
	 * @return this provider's elements
	 */
	public abstract QuickAccessElement[] getElements();

	public QuickAccessElement[] getElementsSorted() {
		if (sortedElements == null) {
			sortedElements = getElements();
			Arrays.sort(sortedElements, new Comparator() {
				public int compare(Object o1, Object o2) {
					QuickAccessElement e1 = (QuickAccessElement) o1;
					QuickAccessElement e2 = (QuickAccessElement) o2;
					return e1.getLabel().compareTo(e2.getLabel());
				}
			});
		}
		return sortedElements;
	}
	
	/**
	 * Returns the element for the given ID if available, or null if no matching
	 * element is available.
	 * 
	 * @param id
	 *            the ID of an element
	 * @return the element with the given ID, or null if not found.
	 */
	public abstract QuickAccessElement getElementForId(String id);
}
