/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * MarkerFieldFilterGroup is the representation of a grouping of marker filters.
 * 
 * @since 3.4
 * 
 */
class MarkerFieldFilterGroup {

	private IConfigurationElement element;
	private int scope;
	private boolean enabled = true;

	/**
	 * Constant for any element.
	 */
	static final int ON_ANY = 0;

	/**
	 * Constant for any selected element only.
	 */
	static final int ON_SELECTED_ONLY = 1;

	/**
	 * Constant for selected element and children.
	 */
	static final int ON_SELECTED_AND_CHILDREN = 2;

	/**
	 * Constant for any element in same container.
	 */
	static final int ON_ANY_IN_SAME_CONTAINER = 3;

	/**
	 * Constant for on working set.
	 */
	static final int ON_WORKING_SET = 4;

	/**
	 * The attribute values for the scope
	 * 
	 */

	private static final String ATTRIBUTE_SCOPE = "scope"; //$NON-NLS-1$

	private static final String ATTRIBUTE_ON_ANY_IN_SAME_CONTAINER = "ON_ANY_IN_SAME_CONTAINER";//$NON-NLS-1$

	private static final String ATTRIBUTE_ON_SELECTED_AND_CHILDREN = "ON_SELECTED_AND_CHILDREN";//$NON-NLS-1$

	private static final String ATTRIBUTE_ON_SELECTED_ONLY = "ON_SELECTED_ONLY"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param configurationElement
	 */
	public MarkerFieldFilterGroup(IConfigurationElement configurationElement) {
		element = configurationElement;
		scope = processScope(element);
	}

	/**
	 * Process the scope attribute.
	 * 
	 * @param configurationElement
	 * @return int
	 */
	private int processScope(IConfigurationElement configurationElement) {
		String scopeValue = element.getAttribute(ATTRIBUTE_SCOPE);

		if (scopeValue.equals(ATTRIBUTE_ON_SELECTED_ONLY))
			return ON_SELECTED_ONLY;

		if (scopeValue.equals(ATTRIBUTE_ON_SELECTED_AND_CHILDREN))
			return ON_SELECTED_AND_CHILDREN;

		if (scopeValue.equals(ATTRIBUTE_ON_ANY_IN_SAME_CONTAINER))
			return ON_ANY_IN_SAME_CONTAINER;

		return ON_ANY;
	}

	/**
	 * Return the value of the scope.
	 * 
	 * @return int
	 * @see #ON_ANY
	 * @see #ON_ANY_IN_SAME_CONTAINER
	 * @see #ON_SELECTED_AND_CHILDREN
	 * @see #ON_SELECTED_ONLY
	 * @see #ON_WORKING_SET
	 */
	public int getScope() {
		return scope;
	}

	/**
	 * Return whether or not the receiver is enabled.
	 * 
	 * @return boolean
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Return the name of the receiver.
	 * 
	 * @return
	 */
	public String getName() {
		return element.getAttribute(MarkerUtilities.ATTRIBUTE_NAME);
	}

}
