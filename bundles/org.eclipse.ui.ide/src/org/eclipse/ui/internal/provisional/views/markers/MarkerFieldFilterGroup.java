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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * MarkerFieldFilterGroup is the representation of a grouping of marker filters.
 * 
 * @since 3.4
 * 
 */
class MarkerFieldFilterGroup {

	private static final String ATTRIBUTE_ON_ANY_IN_SAME_CONTAINER = "ON_ANY_IN_SAME_CONTAINER";//$NON-NLS-1$
	private static final String ATTRIBUTE_ON_SELECTED_AND_CHILDREN = "ON_SELECTED_AND_CHILDREN";//$NON-NLS-1$
	private static final String ATTRIBUTE_ON_SELECTED_ONLY = "ON_SELECTED_ONLY"; //$NON-NLS-1$

	/**
	 * The attribute values for the scope
	 * 
	 */

	private static final String ATTRIBUTE_SCOPE = "scope"; //$NON-NLS-1$

	/**
	 * Constant for any element.
	 */
	static final int ON_ANY = 0;

	/**
	 * Constant for any element in same container.
	 */
	static final int ON_ANY_IN_SAME_CONTAINER = 3;

	/**
	 * Constant for selected element and children.
	 */
	static final int ON_SELECTED_AND_CHILDREN = 2;

	/**
	 * Constant for any selected element only.
	 */
	static final int ON_SELECTED_ONLY = 1;

	/**
	 * Constant for on working set.
	 */
	static final int ON_WORKING_SET = 4;
	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
	private static final String ATTRIBUTE_VALUES = "values"; //$NON-NLS-1$

	/**
	 * Returns the set of projects that contain the given set of resources.
	 * 
	 * @param resources
	 * @return IProject[]
	 */
	static IProject[] getProjects(IResource[] resources) {
		if (resources == null)
			return EMPTY_PROJECT_ARRAY;

		Collection projects = getProjectsAsCollection(resources);

		return (IProject[]) projects.toArray(new IProject[projects.size()]);
	}

	/**
	 * Return the projects for the elements.
	 * 
	 * @param elements
	 *            collection of IResource or IResourceMapping
	 * @return Collection of IProject
	 */
	static Collection getProjectsAsCollection(Object[] elements) {
		HashSet projects = new HashSet();

		for (int idx = 0; idx < elements.length; idx++) {
			if (elements[idx] instanceof IResource) {
				projects.add(((IResource) elements[idx]).getProject());
			} else {
				IProject[] mappingProjects = (((ResourceMapping) elements[idx])
						.getProjects());
				for (int i = 0; i < mappingProjects.length; i++) {
					projects.add(mappingProjects[i]);
				}
			}

		}

		return projects;
	}

	private IConfigurationElement element;

	private boolean enabled = true;

	private int scope;
	private MarkerContentGenerator contentGenerator;
	private MarkerFieldFilter[] fieldFilters;
	private Map EMPTY_MAP = new HashMap();

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param configurationElement
	 * @param generator
	 */
	public MarkerFieldFilterGroup(IConfigurationElement configurationElement,
			MarkerContentGenerator generator) {
		element = configurationElement;
		scope = processScope(element);
		contentGenerator = generator;
	}

	/**
	 * Return the name of the receiver.
	 * 
	 * @return
	 */
	public String getName() {
		return element.getAttribute(MarkerUtilities.ATTRIBUTE_NAME);
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
	 * Return whether or not this IMarker is being shown.
	 * 
	 * @param marker
	 * @return <code>true</code> if it is being shown
	 */
	public boolean select(IMarker marker) {
		MarkerFieldFilter[] filters = getFieldFilters();
		for (int i = 0; i < filters.length; i++) {
			if (filters[i].select(marker))
				continue;
			return false;
		}
		return true;
	}

	/**
	 * Get the filters registered on the receiver.
	 * 
	 * @return MarkerFieldFilter[]
	 */
	private MarkerFieldFilter[] getFieldFilters() {
		if (fieldFilters == null) {
			Map values = getValues();
			Collection filters = new ArrayList();
			MarkerField[] fields = contentGenerator.getVisibleFields();
			for (int i = 0; i < fields.length; i++) {
				MarkerFieldFilter fieldFilter = fields[i].generateFilter();
				if (fieldFilter != null) {
					filters.add(fieldFilter);
					if (values != null)
						fieldFilter.initialize(values);
				}
			}
			fieldFilters = new MarkerFieldFilter[filters.size()];
			filters.toArray(fieldFilters);
		}
		return fieldFilters;
	}

	/**
	 * Get the values defined for the receiver.
	 * 
	 * @return Map of values to apply to a {@link MarkerFieldFilter}
	 */
	private Map getValues() {

		try {
			String className = element.getAttribute(ATTRIBUTE_VALUES);
			if (className != null) {
				FiltersContributionParameters parameters = (FiltersContributionParameters) IDEWorkbenchPlugin
						.createExtension(element, ATTRIBUTE_VALUES);
				return parameters.getParameterValues();
			}
		} catch (CoreException e) {
			StatusManager.getManager().handle(e.getStatus());
			return null;
		}
		return EMPTY_MAP;

	}

	/**
	 * Get all of the filter configuration areas defined on the receiver.
	 * 
	 * @return Collection of FilterConfigurationArea
	 */
	Collection getFieldFilterAreas() {

		Collection areas = new ArrayList();
		MarkerField[] fields = contentGenerator.getVisibleFields();
		for (int i = 0; i < fields.length; i++) {
			FilterConfigurationArea area = fields[i].generateFilterArea();
			if (area != null) {
				areas.add(area);
			}
		}
		return areas;
	}

	/**
	 * Make a working copy of the receiver.
	 * 
	 * @return MarkerFieldFilterGroup
	 */
	public MarkerFieldFilterGroup makeWorkingCopy() {
		MarkerFieldFilterGroup clone = new MarkerFieldFilterGroup(this.element,
				this.contentGenerator);
		clone.scope = this.scope;
		clone.enabled = this.enabled;
		clone.fieldFilters = new MarkerFieldFilter[fieldFilters.length];
		for (int i = 0; i < fieldFilters.length; i++) {
			clone.fieldFilters[i] = fieldFilters[i].makeWorkingCopy();
		}
		return clone;
		
	}

	/**
	 * Set the scope of the receiver.
	 * @param newScope
	 */
	public void setScope(int newScope) {
		scope = newScope;		
	}
}
