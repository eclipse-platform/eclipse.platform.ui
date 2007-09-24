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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;
import org.eclipse.ui.views.markers.internal.Util;

/**
 * MarkerContentGenerator is the representation of the markerContentGenerator
 * extension point.
 * 
 * @since 3.4
 * 
 */
public class MarkerContentGenerator {

	private static final String ATTRIBUTE_DEFAULT_FOR_PERSPECTIVE = "defaultForPerspective"; //$NON-NLS-1$
	private static final String ATTRIBUTE_DEFAULT_MARKER_GROUPING = "defaultMarkerGrouping"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VISIBLE = "visible"; //$NON-NLS-1$
	static final Object CACHE_UPDATE_FAMILY = new Object();
	private static final String ELEMENT_MARKER_FIELD_FILTER_GROUP = "markerFieldFilterGrouping"; //$NON-NLS-1$;
	private static final IResource[] EMPTY_RESOURCE_ARRAY = new IResource[0];
	private static final String MARKER_FIELD_REFERENCE = "markerFieldReference"; //$NON-NLS-1$
	private static final String TAG_FILTERS_SECTION = "filterGroups"; //$NON-NLS-1$
	private static final String TAG_GROUP_ENTRY = "filterGroup"; //$NON-NLS-1$
	private static final Object VALUE_FALSE = "false"; //$NON-NLS-1$
	private static final String TAG_AND = "andFilters"; //$NON-NLS-1$
	private MarkerField[] allFields;
	private MarkerGroup categoryGroup;
	private IConfigurationElement configurationElement;
	private Collection enabledFilters;
	private Collection filters;
	private IResource[] focusResources = MarkerSupportInternalUtilities.EMPTY_RESOURCE_ARRAY;
	private Collection markerTypes;
	private MarkerField[] visibleFields;

	private IWorkingSet workingSet;
	private boolean andFilters = false;

	/**
	 * Create a new MarkerContentGenerator
	 * 
	 * @param element
	 */
	public MarkerContentGenerator(IConfigurationElement element) {
		configurationElement = element;
	}

	/**
	 * Add the resources in resourceMapping to the resourceCollection
	 * 
	 * @param resourceCollection
	 * @param resourceMapping
	 */
	private void addResources(Collection resourceCollection,
			ResourceMapping resourceMapping) {

		try {
			ResourceTraversal[] traversals = resourceMapping.getTraversals(
					ResourceMappingContext.LOCAL_CONTEXT,
					new NullProgressMonitor());
			for (int i = 0; i < traversals.length; i++) {
				ResourceTraversal traversal = traversals[i];
				IResource[] result = traversal.getResources();
				for (int j = 0; j < result.length; j++) {
					resourceCollection.add(result[j]);
				}
			}
		} catch (CoreException e) {
			StatusManager.getManager().handle(e.getStatus());
		}

	}

	/**
	 * Compute all of the markers for the receiver's type.
	 * 
	 * @param subMonitor
	 * @return MarkerEntry
	 */
	private Collection computeAllMarkers(SubProgressMonitor subMonitor) {
		Collection allMarkers = new HashSet();
		findMarkers(allMarkers, new IResource[] { ResourcesPlugin
				.getWorkspace().getRoot() }, null, IResource.DEPTH_INFINITE,
				subMonitor);
		return allMarkers;
	}

	/**
	 * Compute the marker for the supplied filter and add to return markers.
	 * 
	 * @param returnMarkers {@link Collection} of {@link IMarker}
	 * @param subMonitor
	 * @param filterGroup
	 */
	private void computeMarkers(Collection returnMarkers,
			SubProgressMonitor subMonitor, MarkerFieldFilterGroup filterGroup) {

		int filterType = filterGroup.getScope();

		switch (filterType) {
		case MarkerFieldFilterGroup.ON_ANY: {
			findMarkers(returnMarkers, new IResource[] { ResourcesPlugin
					.getWorkspace().getRoot() }, filterGroup,
					IResource.DEPTH_INFINITE, subMonitor);
			break;
		}
		case MarkerFieldFilterGroup.ON_SELECTED_ONLY: {
			findMarkers(returnMarkers, focusResources, filterGroup,
					IResource.DEPTH_ZERO, subMonitor);
			break;
		}
		case MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN: {
			findMarkers(returnMarkers, focusResources, filterGroup,
					IResource.DEPTH_INFINITE, subMonitor);
			break;
		}
		case MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER: {
			findMarkers(returnMarkers, getProjects(focusResources),
					filterGroup, IResource.DEPTH_INFINITE, subMonitor);
			break;
		}
		case MarkerFieldFilterGroup.ON_WORKING_SET: {
			findMarkers(returnMarkers, getResourcesInWorkingSet(), filterGroup,
					IResource.DEPTH_INFINITE, subMonitor);
		}
		}

	}

	/**
	 * Adds all markers in the given set of resources to the given list
	 * 
	 * @param results
	 *            The Collection to add new entries to
	 * @param resources
	 * @param group
	 *            the group to filter on. May be <code>null</code>.
	 * @param depth
	 * @param monitor
	 */
	private void findMarkers(Collection results, IResource[] resources,
			MarkerFieldFilterGroup group, int depth, IProgressMonitor monitor) {
		if (resources == null) {
			return;
		}

		// Optimisation: if a type appears in the selectedTypes list along with
		// all of its sub-types, then combine these in a single search.

		Collection selectedTypes = getMarkerTypes();

		// List of types that haven't been replaced by one of their super-types
		HashSet typesToSearch = new HashSet(selectedTypes.size());

		// List of types that appeared in selectedTypes along with all of their
		// sub-types
		HashSet includeAllSubtypes = new HashSet(selectedTypes.size());

		typesToSearch.addAll(selectedTypes);

		Iterator iter = selectedTypes.iterator();

		while (iter.hasNext()) {
			MarkerType type = (MarkerType) iter.next();

			Collection subtypes = Arrays.asList(type.getAllSubTypes());

			if (selectedTypes.containsAll(subtypes)) {
				typesToSearch.removeAll(subtypes);

				includeAllSubtypes.add(type);
			}
		}

		monitor.beginTask(MarkerMessages.MarkerFilter_searching, typesToSearch
				.size()
				* resources.length);

		// Use this hash set to determine if there are any resources in the
		// list that appear along with their parent.
		HashSet resourcesToSearch = new HashSet();

		// Insert all the resources into the Set
		for (int idx = 0; idx < resources.length; idx++) {
			IResource next = resources[idx];

			if (!next.exists())
				continue;

			if (resourcesToSearch.contains(next))
				monitor.worked(typesToSearch.size());
			else
				resourcesToSearch.add(next);
		}

		// Iterate through all the selected resources
		for (int resourceIdx = 0; resourceIdx < resources.length; resourceIdx++) {
			iter = typesToSearch.iterator();

			IResource resource = resources[resourceIdx];

			// Skip resources that don't exist
			if (!resource.isAccessible()) {
				continue;
			}

			if (depth == IResource.DEPTH_INFINITE) {
				// Determine if any parent of this resource is also in our
				// filter
				IResource parent = resource.getParent();
				boolean found = false;
				while (parent != null) {
					if (resourcesToSearch.contains(parent)) {
						found = true;
					}

					parent = parent.getParent();
				}

				// If a parent of this resource is also in the filter, we can
				// skip it
				// because we'll pick up its markers when we search the parent.
				if (found) {
					continue;
				}
			}

			// Iterate through all the marker types
			while (iter.hasNext()) {
				MarkerType markerType = (MarkerType) iter.next();
				try {
					// Only search for sub-types of the marker if we found all
					// of
					// its sub-types in the filter criteria.
					IMarker[] markers = resource.findMarkers(
							markerType.getId(), includeAllSubtypes
									.contains(markerType), depth);

					monitor.worked(1);

					filterMarkers(results, group, markers);

				} catch (CoreException e) {
					StatusManager.getManager().handle(e.getStatus());
				}
			}
		}

		monitor.done();
	}

	/**
	 * Add all of the markers that pass the filters to results.
	 * 
	 * @param results Collection of {@link IMarker}
	 * @param group
	 * @param markers
	 */
	private void filterMarkers(Collection results,
			MarkerFieldFilterGroup group, IMarker[] markers) {
		for (int idx = 0; idx < markers.length; idx++) {
			IMarker marker = markers[idx];
			if (group == null || group.select(marker))
				results.add(marker);
		}
	}

	/**
	 * Re-generate all of the markers and filter them based on the enabled
	 * filters.
	 * 
	 * @param subMonitor
	 * @return MarkerMap
	 */
	public MarkerMap generateFilteredMarkers(SubProgressMonitor subMonitor) {

		Collection filters = getEnabledFilters();
		Collection returnMarkers = null;
		if (filters.size() > 0) {
			Iterator filtersIterator = filters.iterator();
			if (andFilters) {
				Collection matching = new HashSet();
				while (filtersIterator.hasNext()) {

					computeMarkers(matching, subMonitor,
							(MarkerFieldFilterGroup) filtersIterator.next());
					if (returnMarkers == null)
						returnMarkers = new HashSet(matching);
					else
						findIntersection(matching, returnMarkers);
					matching.clear();
				}
				
			} else {
				returnMarkers = new HashSet();
				while (filtersIterator.hasNext()) {
					computeMarkers(returnMarkers, subMonitor,
							(MarkerFieldFilterGroup) filtersIterator.next());
				}
			}

		} else
			returnMarkers = computeAllMarkers(subMonitor);
		MarkerEntry[] entries = new MarkerEntry[returnMarkers.size()];
		Iterator markers = returnMarkers.iterator();
		int index = 0;
		//Convert to entries
		while(markers.hasNext()){
			entries[index] = new MarkerEntry((IMarker) markers.next());
			index ++;
		}
		
		return new MarkerMap(entries);
	}

	/**
	 * Iterate through the return markers. If they do not exist in matching remove them.
	 * @param matching
	 * @param returnMarkers
	 */
	private void findIntersection(Collection matching, Collection returnMarkers) {
		HashSet removeMarkers = new HashSet();
		Iterator existing = returnMarkers.iterator();
		while(existing.hasNext()){
			Object next = existing.next();
			if(matching.contains(next))
				continue;
			removeMarkers.add(next);
		}
		returnMarkers.removeAll(removeMarkers);
		
	}

	/**
	 * Get the all of the fields that this content generator is using.
	 * 
	 * @return {@link MarkerField}[]
	 */
	public MarkerField[] getAllFields() {
		return allFields;
	}

	/**
	 * Return all of the filters for the receiver.
	 * 
	 * @return Collection of MarkerFieldFilterGroup
	 */
	public Collection getAllFilters() {
		if (filters == null) {
			filters = new ArrayList();
			IConfigurationElement[] filterReferences = configurationElement
					.getChildren(ELEMENT_MARKER_FIELD_FILTER_GROUP);
			for (int i = 0; i < filterReferences.length; i++) {
				filters.add(new MarkerFieldFilterGroup(filterReferences[i],
						this));
			}
			// Apply the last settings
			loadFiltersPreference();

		}
		return filters;
	}

	/**
	 * Return the group used to generate categories.
	 * 
	 * @return MarkerGroup or <code>null</code>.
	 */
	public MarkerGroup getCategoryGroup() {

		return categoryGroup;
	}

	/**
	 * Return a new instance of the receiver with the field
	 * 
	 * @return MarkerComparator
	 */
	public MarkerComparator getComparator() {

		MarkerField field = null;
		if (getCategoryGroup() != null)
			field = getCategoryGroup().getMarkerField();
		return new MarkerComparator(field, getAllFields());
	}

	/**
	 * Get the id of the perspective this content generator is the default for.
	 * 
	 * @return String or <code>null</code>.
	 */
	public String getDefaultPerspectiveId() {
		return configurationElement
				.getAttribute(ATTRIBUTE_DEFAULT_FOR_PERSPECTIVE);
	}

	/**
	 * Return the currently enabled filters.
	 * 
	 * @return Collection of MarkerFieldFilterGroup
	 */
	public Collection getEnabledFilters() {
		if (enabledFilters == null) {
			enabledFilters = new HashSet();
			Iterator filtersIterator = getAllFilters().iterator();
			while (filtersIterator.hasNext()) {
				MarkerFieldFilterGroup next = (MarkerFieldFilterGroup) filtersIterator
						.next();
				if (next.isEnabled())
					enabledFilters.add(next);
			}
		}
		return enabledFilters;
	}

	/**
	 * Return a collection of all of the configuration fields for this generator
	 * 
	 * @return Collection of {@link FilterConfigurationArea}
	 */
	public Collection createFilterConfigurationFields() {
		Collection result = new ArrayList();
		for (int i = 0; i < visibleFields.length; i++) {
			FilterConfigurationArea area = visibleFields[i]
					.generateFilterArea();
			if (area != null)
				result.add(area);

		}
		return result;
	}

	/**
	 * Return the id of the receiver.
	 * 
	 * @return String
	 */
	public String getId() {
		return configurationElement
				.getAttribute(MarkerSupportConstants.ATTRIBUTE_ID);
	}

	/**
	 * Return the markerTypes for the receiver.
	 * 
	 * @return Collection of {@link MarkerType}
	 */
	public Collection getMarkerTypes() {
		if (markerTypes == null) {
			markerTypes = new HashSet();
			IConfigurationElement[] markerTypeElements = configurationElement
					.getChildren(MarkerSupportRegistry.MARKER_TYPE_REFERENCE);
			for (int i = 0; i < markerTypeElements.length; i++) {
				IConfigurationElement configurationElement = markerTypeElements[i];
				String elementName = configurationElement
						.getAttribute(MarkerSupportConstants.ATTRIBUTE_ID);
				MarkerType[] types = MarkerTypesModel.getInstance().getType(
						elementName).getAllSubTypes();
				for (int j = 0; j < types.length; j++) {
					markerTypes.add(types[j]);
				}
				markerTypes.add(MarkerTypesModel.getInstance().getType(
						elementName));
			}
			if (markerTypes.isEmpty()) {
				MarkerType[] types = MarkerTypesModel.getInstance().getType(
						IMarker.PROBLEM).getAllSubTypes();
				for (int i = 0; i < types.length; i++) {
					markerTypes.add(types[i]);
				}
			}
		}
		return markerTypes;
	}

	/**
	 * Get the name for the preferences for the receiver.
	 * 
	 * @return String
	 */
	private String getMementoPreferenceName() {
		return getClass().getName() + getId();
	}

	/**
	 * Return the name for the receiver.
	 * 
	 * @return String
	 */
	public String getName() {
		return configurationElement
				.getAttribute(MarkerSupportConstants.ATTRIBUTE_NAME);
	}

	/**
	 * Return all of the projects being shown.
	 * 
	 * @param focusResources
	 * @return IResource[]
	 */
	private IResource[] getProjects(IResource[] focusResources) {

		if (focusResources.length == 0)
			return EMPTY_RESOURCE_ARRAY;
		HashSet projects = new HashSet();

		for (int idx = 0; idx < focusResources.length; idx++) {
			projects.add(focusResources[idx].getProject());
		}
		if (projects.isEmpty())
			return EMPTY_RESOURCE_ARRAY;
		return (IResource[]) projects.toArray(new IResource[projects.size()]);

	}

	/**
	 * Get the resources in the current working set.
	 * 
	 * @return IResource[]
	 */
	private IResource[] getResourcesInWorkingSet() {

		// TODO hook up working sets
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
	 * Get the fields that this content generator is displaying.
	 * 
	 * @return {@link MarkerField}[]
	 */
	public MarkerField[] getVisibleFields() {
		return visibleFields;
	}

	/**
	 * Initialise the receiver from the configuration element. This is done as a
	 * post processing step.
	 * 
	 * @param registry
	 *            the MarkerSupportRegistry being used to initialise the
	 *            receiver.
	 */
	public void initializeFromConfigurationElement(
			MarkerSupportRegistry registry) {
		String categoryName = configurationElement
				.getAttribute(ATTRIBUTE_DEFAULT_MARKER_GROUPING);
		if (categoryName != null) {
			MarkerGroup group = registry.getMarkerGroup(categoryName);
			if (group != null)
				categoryGroup = group;
		}

		IConfigurationElement[] elements = configurationElement
				.getChildren(MARKER_FIELD_REFERENCE);
		Collection allFieldList = new ArrayList();
		Collection visibleFieldList = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			MarkerField field = registry.getField(elements[i]
					.getAttribute(MarkerSupportConstants.ATTRIBUTE_ID));
			if (field == null)
				continue;
			allFieldList.add(field);
			if (!VALUE_FALSE
					.equals(elements[i].getAttribute(ATTRIBUTE_VISIBLE)))
				visibleFieldList.add(field);
		}

		allFields = new MarkerField[allFieldList.size()];
		allFieldList.toArray(allFields);

		visibleFields = new MarkerField[visibleFieldList.size()];
		visibleFieldList.toArray(visibleFields);

	}

	/**
	 * Return whether or not we are showing a hierarchy,.
	 * 
	 * @return <code>true</code> if a hierarchy is being shown.
	 */
	public boolean isShowingHierarchy() {
		return categoryGroup != null;
	}

	/**
	 * Load the filters preference.
	 */
	private void loadFiltersPreference() {

		String mementoString = IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore().getString(getMementoPreferenceName());

		if (mementoString.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT))
			return;

		try {
			loadSettings(XMLMemento.createReadRoot(new StringReader(
					mementoString)));
		} catch (WorkbenchException e) {
			StatusManager.getManager().handle(e.getStatus());
		}
	}

	/**
	 * Load the settings from the memento.
	 * 
	 * @param memento
	 */
	private void loadSettings(IMemento memento) {

		if (memento == null)
			return;

		Boolean andValue = memento.getBoolean(TAG_AND);
		if (andValue != null)
			setAndFilters(andValue.booleanValue());
		IMemento children[] = memento.getChildren(TAG_GROUP_ENTRY);

		for (int i = 0; i < children.length; i++) {
			IMemento child = children[i];
			String id = child.getString(IMemento.TAG_ID);
			if (id == null)
				continue;
			if (!loadGroupWithID(child, id))

				// Did not find a match must have been added by the user
				loadUserFilter(child);
		}

	}

	/**
	 * Load the group with id from the child if there is a matching system group
	 * registered.
	 * 
	 * @param child
	 * @param id
	 * @return <code>true</code> if a matching group was found
	 */
	private boolean loadGroupWithID(IMemento child, String id) {
		Iterator groups = getAllFilters().iterator();

		while (groups.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) groups
					.next();
			if (id.equals(group.getID())) {
				group.loadSettings(child);
				return true;
			}
		}
		return false;
	}

	/**
	 * Load the user supplied filter
	 * 
	 * @param child
	 */
	private void loadUserFilter(IMemento child) {
		MarkerFieldFilterGroup newGroup = new MarkerFieldFilterGroup(null, this);
		newGroup.loadSettings(child);
		getAllFilters().add(newGroup);
	}

	/**
	 * Set the category group
	 * 
	 * @param group
	 */
	void setCategoryGroup(MarkerGroup group) {
		this.categoryGroup = group;

	}

	/**
	 * Set the filters for the receiver.
	 * 
	 * @param newFilters
	 */
	public void setFilters(Collection newFilters) {
		filters = newFilters;
		enabledFilters = null;

		XMLMemento memento = XMLMemento.createWriteRoot(TAG_FILTERS_SECTION);

		writeFiltersSettings(memento);

		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
		} catch (IOException e) {
			IDEWorkbenchPlugin.getDefault().getLog().log(Util.errorStatus(e));
		}

		IDEWorkbenchPlugin.getDefault().getPreferenceStore().putValue(
				getMementoPreferenceName(), writer.toString());
		IDEWorkbenchPlugin.getDefault().savePluginPreferences();
	}

	/**
	 * Add group to the enabled filters.
	 * 
	 * @param group
	 */
	public void toggleFilter(MarkerFieldFilterGroup group) {
		Collection enabled = getEnabledFilters();
		if (enabled.remove(group)) {// true if it was present
			group.setEnabled(false);
			return;
		}
		group.setEnabled(true);
		enabled.add(group);
	}

	/**
	 * Update the focus resources from list. If there is an update required
	 * return <code>true</code>. This method assumes that there are filters
	 * on resources enabled.
	 * 
	 * @param elements
	 */
	void updateFocusElements(Object[] elements) {
		Collection resourceCollection = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof IResource) {
				resourceCollection.add(elements[i]);
			} else {
				addResources(resourceCollection,
						((ResourceMapping) elements[i]));
			}
		}

		focusResources = new IResource[resourceCollection.size()];
		resourceCollection.toArray(focusResources);
	}

	/**
	 * Return whether or not the list contains a resource that will require
	 * regeneration.
	 * 
	 * @return boolean <code>true</code> if regeneration is required.
	 */
	boolean updateNeeded(Object[] newElements) {

		Iterator filters = getEnabledFilters().iterator();

		while (filters.hasNext()) {
			MarkerFieldFilterGroup filter = (MarkerFieldFilterGroup) filters
					.next();

			int scope = filter.getScope();
			if (scope == MarkerFieldFilterGroup.ON_ANY
					|| scope == MarkerFieldFilterGroup.ON_WORKING_SET)
				continue;

			if (newElements == null || newElements.length < 1)
				continue;

			if (focusResources.length == 0)
				return true; // We had nothing now we have something

			if (Arrays.equals(focusResources, newElements))
				continue;

			if (scope == MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER) {
				Collection oldProjects = MarkerFieldFilterGroup
						.getProjectsAsCollection(focusResources);
				Collection newProjects = MarkerFieldFilterGroup
						.getProjectsAsCollection(newElements);

				if (oldProjects.size() == newProjects.size()
						&& newProjects.containsAll(oldProjects))
					continue;
				return true;// Something must be different
			}
			return true;
		}

		return false;
	}

	/**
	 * Write the settings for the filters to the memento.
	 * 
	 * @param memento
	 */
	private void writeFiltersSettings(XMLMemento memento) {

		memento.putBoolean(TAG_AND, andFilters);

		Iterator groups = getAllFilters().iterator();
		while (groups.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) groups
					.next();
			IMemento child = memento
					.createChild(TAG_GROUP_ENTRY, group.getID());
			group.saveFilterSettings(child);
		}

	}

	/**
	 * Return whether the filters are being ANDed or ORed.
	 * 
	 * @return boolean
	 */
	boolean andFilters() {
		return andFilters;
	}

	/**
	 * Set whether the filters are being ANDed or ORed.
	 * 
	 * @param and
	 */
	void setAndFilters(boolean and) {
		andFilters = and;
	}

}
