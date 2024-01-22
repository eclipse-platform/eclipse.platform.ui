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
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map.Entry;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IActionFilter;

/**
 * An ObjectFilterTest is used to read an object filter from XML, and evaluate
 * the results for a given object.
 */
public class ObjectFilterTest {
	private HashMap<String, String> filterElements;

	/**
	 * Create a new object filter.
	 */
	public ObjectFilterTest() {
		// do nothing
	}

	/**
	 * Add a filter element to the test. This element must contain a name value
	 * filter pair, as defined by the <code>org.eclipse.ui.actionFilters</code>
	 * extension point.
	 */
	public boolean addFilterElement(IConfigurationElement element) {
		String name = element.getAttribute("name");//$NON-NLS-1$
		if (name == null) {
			return false;
		}

		// Get positive property.
		String value = element.getAttribute("value");//$NON-NLS-1$
		if (value == null) {
			return false;
		}
		if (filterElements == null) {
			filterElements = new HashMap();
		}
		filterElements.put(name, value);
		return true;
	}

	/**
	 * Returns whether the object filter correctly matches a given object. The
	 * results will be <code>true</code> if there is a filter match.
	 * <p>
	 * If <code>adaptable is true</code>, the result will also be <code>true</code>
	 * if the object is a wrapper for a resource, and the resource produces a filter
	 * match.
	 * </p>
	 *
	 * @param object the object to examine
	 * @return <code>true</code> if there is a filter match.
	 */
	public boolean matches(Object object, boolean adaptable) {
		// Optimize it.
		if (filterElements == null) {
			return true;
		}

		// Try out the object.
		if (this.preciselyMatches(object)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns whether the object filter correctly matches a given object.
	 */
	private boolean preciselyMatches(Object object) {
		// Get the action filter.
		IActionFilter filter = Adapters.adapt(object, IActionFilter.class);
		if (filter == null) {
			return false;
		}

		// Run the action filter.
		for (Entry<String, String> entry : filterElements.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (!filter.testAttribute(object, name, value)) {
				return false;
			}
		}
		return true;
	}
}
