/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
 *     Broadcom Corp. - James Blackburn -  Fix for Bug 305529 -
 *     					[Markers] NPE in MarkerFieldEditor if MarkerFieldConfiguration scope is unset
 *     Kit Lo (IBM) - Bug 542713 - Empty entries in Show menu after switching UI language to German
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.AggregateWorkingSet;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.FiltersContributionParameters;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.MarkerFilter;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.eclipse.ui.views.markers.internal.MarkerType;

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

	private static final String ATTRIBUTE_VALUES = "values"; //$NON-NLS-1$


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

	static final String TAG_ENABLED = "enabled"; //$NON-NLS-1$
	private static final String TAG_SCOPE = "scope"; //$NON-NLS-1$
	private static final String TAG_FIELD_FILTER_ENTRY = "fieldFilter"; //$NON-NLS-1$
	private static final String TAG_WORKING_SET = "workingSet"; //$NON-NLS-1$
	private static final String TAG_LIMIT = "filterLimit"; //$NON-NLS-1$
	// The identifier for user filters
	private static String USER = "USER"; //$NON-NLS-1$

	protected MarkerContentGenerator generator;

	private IConfigurationElement element;

	private Map<String, String> EMPTY_MAP = new HashMap<>();
	private boolean enabled = true;
	protected MarkerFieldFilter[] fieldFilters;
	private int scope;
	private int limit;

	private String name;
	private String id;

	private IWorkingSet workingSet;
	private IResource[] wSetResources;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param configurationElement
	 * @param markerBuilder
	 */
	public MarkerFieldFilterGroup(IConfigurationElement configurationElement, MarkerContentGenerator markerBuilder) {
		element = configurationElement;
		generator = markerBuilder;
		initializeWorkingSet();
		scope = processScope();

		if (configurationElement == null) {
			return;
		}
		String stringValue = configurationElement.getAttribute(MarkerSupportRegistry.ENABLED);
		if (MarkerSupportInternalUtilities.FALSE.equals(stringValue)) {
			enabled = false;
		}
		stringValue = configurationElement.getAttribute(MarkerSupportRegistry.FILTER_LIMIT);
		if (stringValue == null || stringValue.isEmpty()) {
			limit = -1;
		}
	}

	/**
	 * Get the root types for the receiver
	 *
	 * @return Collection of {@link MarkerType}
	 */
	Collection<MarkerType> getAllTypes() {
		return generator.getMarkerTypes();
	}

	/**
	 * Get the filters registered on the receiver.
	 *
	 * @return MarkerFieldFilter[]
	 */
	private MarkerFieldFilter[] getFieldFilters() {
		if (fieldFilters == null) {
			calculateFilters();
		}
		return fieldFilters;
	}

	/**
	 * Calculate the filters for the receiver.
	 */
	protected void calculateFilters() {
		Map<String, String> values = getValues();
		Collection<MarkerFieldFilter> filters = new ArrayList<>();
		for (MarkerField visibleField : generator.getVisibleFields()) {
			MarkerFieldFilter fieldFilter = MarkerSupportInternalUtilities.generateFilter(visibleField);
			if (fieldFilter != null) {
				filters.add(fieldFilter);

				// The type filter needs information from the generator
				if (fieldFilter instanceof MarkerTypeFieldFilter) {
					// Show everything by default
					((MarkerTypeFieldFilter) fieldFilter).setContentGenerator(generator);
				}
				if (values != null) {
					fieldFilter.initialize(values);
				}
			}
		}
		MarkerFieldFilter[] newFilters = new MarkerFieldFilter[filters.size()];
		filters.toArray(newFilters);
		fieldFilters = newFilters;
	}

	/**
	 * Return the MarkerFieldFilter for field or <code>null</code> if there
	 * isn't one.
	 *
	 * @param field
	 * @return MarkerFieldFilter
	 */
	public MarkerFieldFilter getFilter(MarkerField field) {
		for (MarkerFieldFilter filter : getFieldFilters()) {
			if (filter.getField().equals(field)) {
				return filter;
			}
		}
		return null;
	}

	/**
	 * Return the id of the receiver.
	 *
	 * @return String
	 */
	public String getID() {
		if (id == null) {
			if (element == null) {
				id = USER + System.currentTimeMillis();
			} else {
				id = element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID);
			}
		}
		return id;
	}

	/**
	 * Return the name of the receiver.
	 *
	 * @return String
	 */
	public String getName() {
		if (name == null) {
			if (element == null) {
				name = MarkerSupportInternalUtilities.EMPTY_STRING;
			} else {
				name = element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_NAME);
			}
		}
		return name;
	}

	/**
	 * Return the resources in the working set. If it is empty then return the
	 * workspace root.
	 *
	 * @return IResource[]
	 */
	IResource[] getResourcesInWorkingSet() {
		if (workingSet == null) {
			return new IResource[0];
		}

		//Return workspace root for aggregates with no containing workingsets,ex. window working set
		if (workingSet.isAggregateWorkingSet()&&workingSet.isEmpty()){
			if(((AggregateWorkingSet) workingSet).getComponents().length==0) {
				return new IResource[] { ResourcesPlugin.getWorkspace().getRoot()};
			}
		}

		IAdaptable[] elements = workingSet.getElements();
		List<IResource> result = new ArrayList<>(elements.length);

		for (IAdaptable adaptable : elements) {
			IResource next = Adapters.adapt(adaptable, IResource.class);
			if (next != null) {
				result.add(next);
			}
		}

		return result.toArray(new IResource[result.size()]);
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
	@SuppressWarnings("javadoc")
	public int getScope() {
		return scope;
	}

	/**
	 * Get the values defined for the receiver.
	 *
	 * @return Map of values to apply to a {@link MarkerFieldFilter}
	 */
	private Map<String, String> getValues() {

		try {
			String className = null;
			if (element != null) {
				className = element.getAttribute(ATTRIBUTE_VALUES);
				if (className != null) {
					FiltersContributionParameters parameters = (FiltersContributionParameters) IDEWorkbenchPlugin
							.createExtension(element, ATTRIBUTE_VALUES);
					return parameters.getParameterValues();
				}
			}
		} catch (CoreException e) {
			Policy.handle(e);
			return null;
		}
		return EMPTY_MAP;
	}

	/**
	 * Get the working set for the receiver.
	 *
	 * @return IWorkingSet
	 */
	IWorkingSet getWorkingSet() {
		return workingSet;
	}

	/**
	 * Gather the resource is in the working set
	 */
	private void computeWorkingSetResources() {
		if(workingSet!=null){
			 /* MarkerFieldFilterGroup will have to re-get the resources in
			 * a working set for every marker it filters using the select method
			 * Or we may do this once before the markers are filtered.
			 */
			wSetResources=getResourcesInWorkingSet();
		}else{
			wSetResources = new IResource[] { ResourcesPlugin.getWorkspace().getRoot() };
		}
	}

	/**
	 * Return true if the resource is in the working set
	 * @param resource
	 * @return boolean
	 */
	private boolean isInWorkingSet(IResource resource) {
		if (wSetResources == null) {
			computeWorkingSetResources();
		}
		for (IResource wSetResource : wSetResources) {
			if(wSetResource.getFullPath().isPrefixOf(resource.getFullPath())){
				return true;
			}
		}
		return false;
	}

	/**
	 * Initialise the working set for the receiver. Use the window working set
	 * for the working set and set the scope to ON_WORKING_SET if they are to be
	 * used by default.
	 */
	private void initializeWorkingSet() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				setWorkingSet(page.getAggregateWorkingSet());
				if ((PlatformUI.getPreferenceStore()
						.getBoolean(IWorkbenchPreferenceConstants.USE_WINDOW_WORKING_SET_BY_DEFAULT))) {
					setScope(ON_WORKING_SET);
				}
			}
		}
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
	 * Return whether or not this is a system or user group.
	 *
	 * @return boolean <code>true</code> if it is a system group.
	 */
	public boolean isSystem() {
		return element != null;
	}

	/**
	 * Load the settings from the legacy child.
	 *
	 * @param memento
	 */
	void legacyLoadSettings(IMemento memento) {

		String enabledString = memento.getString(TAG_ENABLED);
		if (enabledString != null && enabledString.length() > 0) {
			enabled = Boolean.parseBoolean(enabledString);
		}

		Integer resourceSetting = memento.getInteger(MarkerFilter.TAG_ON_RESOURCE);

		if (resourceSetting != null) {
			scope = resourceSetting.intValue();
		}

		String workingSetName = memento.getString(TAG_WORKING_SET);

		if (workingSetName != null) {
			setWorkingSet(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName));
		}

		if (element == null) {
			String nameString = memento.getID();
			if (nameString != null && nameString.length() > 0) {
				name = nameString;
			}
			String idString = memento.getString(IMemento.TAG_ID);
			if (idString != null && idString.length() > 0) {
				id = idString;
			}
		}

		for (MarkerFieldFilter filter : getFieldFilters()) {
			if (filter instanceof CompatibilityFieldFilter) {
				((CompatibilityFieldFilter) filter).loadLegacySettings(memento, generator);
			}
		}
	}

	/**
	 * Load the current settings from the child.
	 *
	 * @param memento -
	 *            the memento to load from
	 */
	void loadSettings(IMemento memento) {
		String stringValue = memento.getString(TAG_ENABLED);
		if (stringValue != null && stringValue.length() > 0){
			enabled = Boolean.parseBoolean(stringValue);
		}
		scope = memento.getInteger(TAG_SCOPE).intValue();

		String workingSetName = memento.getString(TAG_WORKING_SET);

		if (workingSetName != null) {
			setWorkingSet(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName));
		}

		stringValue = memento.getString(TAG_LIMIT);
		if (stringValue != null && stringValue.length() > 0) {
			setLimit(Integer.parseInt(stringValue));
		}

		Map<String, MarkerFieldFilter> filterMap = new HashMap<>();
		for (MarkerFieldFilter filter : getFieldFilters()) {
			filterMap.put(MarkerSupportInternalUtilities.getId(filter.getField()), filter);
		}

		for (IMemento childMemento : memento.getChildren(TAG_FIELD_FILTER_ENTRY)) {
			String filterId = childMemento.getID();
			MarkerFieldFilter filter = filterMap.get(filterId);
			if (filter != null) {
				if (filter instanceof MarkerTypeFieldFilter) {
					((MarkerTypeFieldFilter) filter).setContentGenerator(generator);
				}
				filter.loadSettings(childMemento);
			}
		}

		if (element == null) {
			String nameString = memento	.getString(MarkerSupportInternalUtilities.ATTRIBUTE_NAME);
			if (nameString != null && nameString.length() > 0) {
				name = nameString;
			}
			String idString = memento.getString(IMemento.TAG_ID);
			if (idString != null && idString.length() > 0) {
				id = idString;
			}
		}
	}

	/**
	 * Make a working copy of the receiver.
	 *
	 * @return MarkerFieldFilterGroup or <code> null</code> if it failed.
	 */
	MarkerFieldFilterGroup makeWorkingCopy() {
		MarkerFieldFilterGroup clone = new MarkerFieldFilterGroup(this.element, this.generator);
		if (populateClone(clone)) {
			return clone;
		}
		return null;
	}

	/**
	 * Populate the clone and return true if successful.
	 *
	 * @param clone
	 */
	protected boolean populateClone(MarkerFieldFilterGroup clone) {
		clone.scope = this.scope;
		clone.limit = limit;
		clone.workingSet = this.workingSet;
		clone.enabled = this.enabled;
		clone.fieldFilters = new MarkerFieldFilter[getFieldFilters().length];
		clone.name = name;
		clone.id = id;
		for (int i = 0; i < fieldFilters.length; i++) {
			try {
				clone.fieldFilters[i] = fieldFilters[i].getClass().getDeclaredConstructor().newInstance();
				fieldFilters[i].populateWorkingCopy(clone.fieldFilters[i]);
			} catch (InstantiationException | IllegalArgumentException | IllegalAccessException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				StatusManager.getManager().handle(StatusUtil.newError(e), StatusManager.SHOW);
				return false;
			}
		}
		return true;
	}

	/**
	 * Process the scope attribute.
	 *
	 * @return int
	 */
	private int processScope() {
		if (element == null) {
			return ON_ANY;
		}
		String scopeValue = element.getAttribute(ATTRIBUTE_SCOPE);
		if (ATTRIBUTE_ON_SELECTED_ONLY.equals(scopeValue)) {
			return ON_SELECTED_ONLY;
		}
		if (ATTRIBUTE_ON_SELECTED_AND_CHILDREN.equals(scopeValue)) {
			return ON_SELECTED_AND_CHILDREN;
		}
		if (ATTRIBUTE_ON_ANY_IN_SAME_CONTAINER.equals(scopeValue)) {
			return ON_ANY_IN_SAME_CONTAINER;
		}
		return ON_ANY;
	}

	/**
	 * Save the settings for the receiver in the memento.
	 *
	 * @param memento
	 */
	void saveFilterSettings(IMemento memento) {
		memento.putString(TAG_ENABLED, String.valueOf(enabled));
		memento.putString(TAG_SCOPE, String.valueOf(scope));
		memento.putString(TAG_LIMIT, String.valueOf(limit));

		if (workingSet != null) {
			memento.putString(TAG_WORKING_SET, workingSet.getName());
		}

		if (element == null) {
			memento.putString(MarkerSupportInternalUtilities.ATTRIBUTE_NAME, getName());
			memento.putString(IMemento.TAG_ID, getID());
		}

		for (MarkerFieldFilter filter : getFieldFilters()) {
			IMemento child = memento.createChild(TAG_FIELD_FILTER_ENTRY,
					MarkerSupportInternalUtilities.getId(filter.getField()));
			filter.saveSettings(child);
		}
	}

	/**
	 * Return whether or not this MarkerEntry can be shown.
	 *
	 * @param entry
	 *
	 * @return <code>true</code> if it can be shown
	 */
	public boolean select(MarkerEntry entry) {
		MarkerFieldFilter[] filters = getFieldFilters();
		if (scope == ON_WORKING_SET && workingSet != null) {
			if (!workingSet.isAggregateWorkingSet()){
				if (!isInWorkingSet(entry.getMarker().getResource())) {
					return false;
				}
			}
			//skip this for aggregates with no containing workingsets, ex. window working set
			else if(((AggregateWorkingSet) workingSet).getComponents().length!=0){
				if (!isInWorkingSet(entry.getMarker().getResource())) {
					return false;
				}
			}
		}

		for (MarkerFieldFilter filter : filters) {
			if (filter.select(entry)) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * Set whether or not the receiver is enabled.
	 *
	 * @param enabled
	 *            The enabled to set.
	 */
	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Set the name of the receiver.
	 *
	 * @param newName
	 */
	public void setName(String newName) {
		name = newName;

	}

	/**
	 * Set the scope of the receiver.
	 *
	 * @param newScope
	 */
	public void setScope(int newScope) {
		scope = newScope;
	}

	/**
	 * Set the working set of the receiver.
	 *
	 * @param workingSet
	 */
	void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
	}

	/**
	 * @return Returns -1 for no limit else the limit.
	 */
	int getLimit() {
		return limit;
	}

	/**
	 * @param limit
	 *            The limit to set, -1 or 0 for no limit.
	 */
	void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * Refresh the MarkerFieldFilterGroup .
	 */
	void refresh() {
		if (getScope() == ON_WORKING_SET) {
			computeWorkingSetResources();
		}
	}

	public boolean selectByFilters(MarkerEntry entry) {
		return select(entry);
	}

	public boolean selectByScope(MarkerEntry entry, IResource[] resources) {
		int scopeVal = getScope();
		switch (scopeVal) {
		case MarkerFieldFilterGroup.ON_ANY: {
			return true;
		}
		case MarkerFieldFilterGroup.ON_SELECTED_ONLY: {
			IPath  markerPath=entry.getMarker().getResource().getFullPath();
			for (IResource resource : resources) {
				if(markerPath.equals(resource.getFullPath())){
					return true;
				}
			}
			return false;
		}
		case MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN: {
			IPath  markerPath=entry.getMarker().getResource().getFullPath();
			for (IResource resource : resources) {
				if(resource.getFullPath().isPrefixOf(markerPath)){
					return true;
				}
			}
			return false;
		}
		case MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER: {
			IPath  markerProjectPath=entry.getMarker().getResource().getFullPath();
			IProject[] projects=MarkerResourceUtil.getProjects(resources);
			for (IProject project : projects) {
				if(project.getFullPath().isPrefixOf(markerProjectPath)){
					return true;
				}
			}
			return false;
		}
		case MarkerFieldFilterGroup.ON_WORKING_SET: {
			return isInWorkingSet(entry.getMarker().getResource());
		}
		}
		return true;
	}
}
