/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
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

	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];

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
	// The identifier for user filters
	private static String USER = "USER"; //$NON-NLS-1$

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

	protected CachedMarkerBuilder builder;

	private IConfigurationElement element;

	private Map EMPTY_MAP = new HashMap();
	private boolean enabled = true;
	protected MarkerFieldFilter[] fieldFilters;
	private int scope;
	private String name;
	private String id;

	/**
	 * The entry for testing filters. Cached to prevent garbage.
	 */
	private MarkerEntry testEntry = new MarkerEntry(null);
	private IWorkingSet workingSet;
	private Collection workingSetPaths;
	private IResource[] wSetResources;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param configurationElement
	 * @param markerBuilder
	 */
	public MarkerFieldFilterGroup(IConfigurationElement configurationElement,
			CachedMarkerBuilder markerBuilder) {
		element = configurationElement;
		builder = markerBuilder;
		initializeWorkingSet();
		scope = processScope();

		if (configurationElement == null)
			return;
		String enablementString = configurationElement
				.getAttribute(MarkerSupportRegistry.ENABLED);
		if (MarkerSupportInternalUtilities.FALSE.equals(enablementString))
			enabled = false;

	}

	/**
	 * Add resources and thier children's paths to the working set paths.
	 * 
	 * @param resources
	 */
	private void addResourcesAndChildrenPaths(IResource[] resources) {
		for (int idx = 0; idx < resources.length; idx++) {

			IResource currentResource = resources[idx];

			workingSetPaths.add(currentResource.getFullPath().toString());

			if (currentResource instanceof IContainer) {
				IContainer cont = (IContainer) currentResource;

				try {
					addResourcesAndChildrenPaths(cont.members());
				} catch (CoreException e) {
					Policy.handle(e);
				}
			}

		}
	}

	/**
	 * Get the root types for the receiver
	 * 
	 * @return Collection of {@link MarkerType}
	 */
	Collection getAllTypes() {
		return builder.getGenerator().getMarkerTypes();
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
		Map values = getValues();
		Collection filters = new ArrayList();
		MarkerField[] fields = builder.getVisibleFields();
		for (int i = 0; i < fields.length; i++) {
			MarkerFieldFilter fieldFilter = MarkerSupportInternalUtilities
					.generateFilter(fields[i]);
			if (fieldFilter != null) {
				filters.add(fieldFilter);

				// The type filter needs information from the generator
				if (fieldFilter instanceof MarkerTypeFieldFilter)
					// Show everything by default
					((MarkerTypeFieldFilter) fieldFilter)
							.setContentGenerator(builder.getGenerator());
				if (values != null)
					fieldFilter.initialize(values);
			}
		}
		fieldFilters = new MarkerFieldFilter[filters.size()];
		filters.toArray(fieldFilters);
	}

	/**
	 * Return the MarkerFieldFilter for field or <code>null</code> if there
	 * isn't one.
	 * 
	 * @param field
	 * @return MarkerFieldFilter
	 */
	public MarkerFieldFilter getFilter(MarkerField field) {
		MarkerFieldFilter[] filters = getFieldFilters();
		for (int i = 0; i < filters.length; i++) {
			if (filters[i].getField().equals(field))
				return filters[i];
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
			if (element == null)
				id = USER + String.valueOf(System.currentTimeMillis());
			else
				id = element
						.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_NAME);
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
			if (element == null)
				name = MarkerSupportInternalUtilities.EMPTY_STRING;
			else
				name = element
						.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_NAME);
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

		if (workingSet.isEmpty()) {
			return new IResource[] { ResourcesPlugin.getWorkspace().getRoot() };
		}

		IAdaptable[] elements = workingSet.getElements();
		List result = new ArrayList(elements.length);

		for (int idx = 0; idx < elements.length; idx++) {
			IResource next = (IResource) elements[idx]
					.getAdapter(IResource.class);

			if (next != null) {
				result.add(next);
			}
		}

		return (IResource[]) result.toArray(new IResource[result.size()]);
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
	 * Get the values defined for the receiver.
	 * 
	 * @return Map of values to apply to a {@link MarkerFieldFilter}
	 */
	private Map getValues() {

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
	 * Return all of the paths in the working set
	 * 
	 * @return Collection
	 */
	private Collection getWorkingSetPaths() {

		if (workingSetPaths == null) {
			workingSetPaths = new HashSet();
			addResourcesAndChildrenPaths(getResourcesInWorkingSet());
		}
		return workingSetPaths;

	}
	
	/**
	 * Gather the resource is in the working set
	 */
	private void computeWorkingSetResources() {
		if(workingSet!=null){
			 /* MarkerFieldFilterGroup will have to re-get the resources in 
			 * a working set for every marker it filters using the select method
			 * Or we may do this once before the markers are filtered.A IResourceChangeListener??		 
			 */
			wSetResources=getResourcesInWorkingSet();
		}
	}

	/**
	 * Return true if the resource is in the working set
	 * @param resource
	 * @return boolean
	 */
	private boolean isInWorkingSet(IResource resource) {
		if(wSetResources==null)computeWorkingSetResources();
		for (int i = 0; i < wSetResources.length; i++) {
			if(wSetResources[i].getFullPath().isPrefixOf(resource.getFullPath())){
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

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				setWorkingSet(page.getAggregateWorkingSet());
				if ((PlatformUI.getPreferenceStore()
						.getBoolean(IWorkbenchPreferenceConstants.USE_WINDOW_WORKING_SET_BY_DEFAULT)))
					setScope(ON_WORKING_SET);

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
		if (enabledString != null && enabledString.length() > 0)
			enabled = Boolean.valueOf(enabledString).booleanValue();

		Integer resourceSetting = memento
				.getInteger(MarkerFilter.TAG_ON_RESOURCE);

		if (resourceSetting != null)
			scope = resourceSetting.intValue();

		String workingSetName = memento.getString(TAG_WORKING_SET);

		if (workingSetName != null)
			setWorkingSet(PlatformUI.getWorkbench().getWorkingSetManager()
					.getWorkingSet(workingSetName));

		if (element == null) {
			String nameString = memento.getID();
			if (nameString != null && nameString.length() > 0)
				name = nameString;
			String idString = memento.getString(IMemento.TAG_ID);
			if (idString != null && idString.length() > 0)
				id = idString;

		}

		MarkerFieldFilter[] filters = getFieldFilters();
		for (int i = 0; i < filters.length; i++) {
			if (filters[i] instanceof CompatibilityFieldFilter)
				((CompatibilityFieldFilter) filters[i]).loadLegacySettings(
						memento, builder.getGenerator());
		}

	}

	/**
	 * Load the current settings from the child.
	 * 
	 * @param memento -
	 *            the memento to load from
	 */
	void loadSettings(IMemento memento) {

		String enabledString = memento.getString(TAG_ENABLED);
		if (enabledString != null && enabledString.length() > 0)
			enabled = Boolean.valueOf(enabledString).booleanValue();
		scope = memento.getInteger(TAG_SCOPE).intValue();

		String workingSetName = memento.getString(TAG_WORKING_SET);

		if (workingSetName != null)
			setWorkingSet(PlatformUI.getWorkbench().getWorkingSetManager()
					.getWorkingSet(workingSetName));

		Map filterMap = new HashMap();
		MarkerFieldFilter[] filters = getFieldFilters();
		for (int i = 0; i < filters.length; i++) {
			filterMap.put(MarkerSupportInternalUtilities.getId(filters[i]
					.getField()), filters[i]);

		}

		IMemento[] children = memento.getChildren(TAG_FIELD_FILTER_ENTRY);
		for (int i = 0; i < children.length; i++) {
			IMemento childMemento = children[i];
			String id = childMemento.getID();
			if (filterMap.containsKey(id)) {
				MarkerFieldFilter filter = (MarkerFieldFilter) filterMap
						.get(id);
				if (filter instanceof MarkerTypeFieldFilter) {
					((MarkerTypeFieldFilter) filter)
							.setContentGenerator(builder.getGenerator());
				}
				filter.loadSettings(childMemento);
			}

		}

		if (element == null) {
			String nameString = memento
					.getString(MarkerSupportInternalUtilities.ATTRIBUTE_NAME);
			if (nameString != null && nameString.length() > 0)
				name = nameString;
			String idString = memento.getString(IMemento.TAG_ID);
			if (idString != null && idString.length() > 0)
				id = idString;

		}

	}

	/**
	 * Make a working copy of the receiver.
	 * 
	 * @return MarkerFieldFilterGroup or <code> null</code> if it failed.
	 */
	MarkerFieldFilterGroup makeWorkingCopy() {
		MarkerFieldFilterGroup clone = new MarkerFieldFilterGroup(this.element,
				this.builder);
		if (populateClone(clone))
			return clone;
		return null;

	}

	/**
	 * Populate the clone and return true if successful.
	 * 
	 * @param clone
	 */
	protected boolean populateClone(MarkerFieldFilterGroup clone) {
		clone.scope = this.scope;
		clone.workingSet = this.workingSet;
		clone.enabled = this.enabled;
		clone.fieldFilters = new MarkerFieldFilter[getFieldFilters().length];
		clone.name = name;
		clone.id = id;
		for (int i = 0; i < fieldFilters.length; i++) {
			try {
				clone.fieldFilters[i] = (MarkerFieldFilter) fieldFilters[i]
						.getClass().newInstance();
				fieldFilters[i].populateWorkingCopy(clone.fieldFilters[i]);
			} catch (InstantiationException e) {
				StatusManager.getManager().handle(
						StatusUtil.newStatus(IStatus.ERROR, e
								.getLocalizedMessage(), e), StatusManager.SHOW);
				return false;
			} catch (IllegalAccessException e) {
				StatusManager.getManager().handle(
						StatusUtil.newStatus(IStatus.ERROR, e
								.getLocalizedMessage(), e), StatusManager.SHOW);
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

		if (element == null)
			return ON_ANY;

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
	 * Save the settings for the receiver in the memento.
	 * 
	 * @param memento
	 */
	void saveFilterSettings(IMemento memento) {
		memento.putString(TAG_ENABLED, String.valueOf(enabled));
		memento.putString(TAG_SCOPE, String.valueOf(scope));

		if (workingSet != null) {
			memento.putString(TAG_WORKING_SET, workingSet.getName());
		}

		if (element == null) {
			memento.putString(MarkerSupportInternalUtilities.ATTRIBUTE_NAME,
					getName());
			memento.putString(IMemento.TAG_ID, getID());
		}
		MarkerFieldFilter[] filters = getFieldFilters();

		for (int i = 0; i < filters.length; i++) {
			IMemento child = memento
					.createChild(TAG_FIELD_FILTER_ENTRY,
							MarkerSupportInternalUtilities.getId(filters[i]
									.getField()));
			filters[i].saveSettings(child);

		}

	}

	/**
	 * Return whether or not this IMarker is being shown.
	 * 
	 * @param marker
	 * @return <code>true</code> if it is being shown
	 */
	public boolean select(IMarker marker) {
		MarkerFieldFilter[] filters = getFieldFilters();
		testEntry.setMarker(marker);

		if (scope == ON_WORKING_SET && workingSet != null
				&& !workingSet.isEmpty()) {
			if (!isInWorkingSet(marker.getResource()))
				return false;
		}

		for (int i = 0; i < filters.length; i++) {
			if (filters[i].select(testEntry))
				continue;
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
		workingSetPaths = null;
	}

	/**
	 * IResourceChangeListener, a better option??
	 * 
	 */	
	void refresh() {
		 computeWorkingSetResources();
		 getWorkingSetPaths();
	}
}
