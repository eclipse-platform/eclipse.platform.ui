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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.AggregateWorkingSet;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

/**
 * MarkerContentGenerator is the representation of the markerContentGenerator
 * extension point.
 * 
 * @since 3.4
 * 
 */
public class MarkerContentGenerator {

	private static final String ATTRIBUTE_DEFAULT_MARKER_GROUPING = "defaultMarkerGrouping"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VISIBLE = "visible"; //$NON-NLS-1$

	/**
	 * The job family for content updates
	 */
	public static final Object CACHE_UPDATE_FAMILY = new Object();
	private static final String ELEMENT_MARKER_FIELD_CONFIGURATION = "markerFieldConfiguration"; //$NON-NLS-1$;
	private static final IResource[] EMPTY_RESOURCE_ARRAY = new IResource[0];
	private static final String MARKER_FIELD_REFERENCE = "markerFieldReference"; //$NON-NLS-1$

	private MarkerField[] allFields;
	private IConfigurationElement configurationElement;
	private Collection markerTypes;
	private MarkerField[] initialVisible;
	private Collection groups;
	private Collection generatorExtensions = new ArrayList();
	private Map allTypesTable;

	/**
	 * Create a new MarkerContentGenerator
	 * 
	 * @param element
	 */
	public MarkerContentGenerator(IConfigurationElement element) {
		configurationElement = element;
	}

	/**
	 * Add the groups defined in the receiver to the collection of groups.
	 * 
	 * @param groups
	 */
	private void addDefinedGroups(Collection groups) {
		// Add the ones in the receiver.
		addGroupsFrom(configurationElement, groups);
		// Add the extensions
		Iterator extensions = generatorExtensions.iterator();
		while (extensions.hasNext()) {
			addGroupsFrom((IConfigurationElement) extensions.next(), groups);
		}
	}

	/**
	 * Add the extensions to the receiver.
	 * 
	 * @param extensions
	 *            Collection of {@link IConfigurationElement}
	 */
	public void addExtensions(Collection extensions) {
		generatorExtensions = extensions;

	}

	/**
	 * Add all of the markerGroups defined in element.
	 * 
	 * @param groups
	 */
	private void addGroupsFrom(IConfigurationElement element, Collection groups) {
		IConfigurationElement[] groupings = element
				.getChildren(MarkerSupportRegistry.MARKER_GROUPING);

		for (int i = 0; i < groupings.length; i++) {

			groups.add(MarkerGroup.createMarkerGroup(groupings[i]));
		}
	}

	/**
	 * Return whether or not all of {@link MarkerTypesModel} arein
	 * the selectedTypes.
	 * @param selectedTypes
	 * @return boolean
	 */
	boolean allTypesSelected(Collection selectedTypes) {
		return selectedTypes.containsAll(markerTypes);
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
	 * @param returnMarkers
	 *            {@link Collection} of {@link IMarker}
	 * @param subMonitor
	 * @param filterGroup
	 * @param focusResources
	 *            the resource currently selected
	 */
	private void computeMarkers(Collection returnMarkers,
			SubProgressMonitor subMonitor, MarkerFieldFilterGroup filterGroup,
			IResource[] focusResources) {

		int filterType = filterGroup.getScope();
		
		filterGroup.refresh(); 
		
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
			findMarkers(returnMarkers, getResourcesInWorkingSet(filterGroup
					.getWorkingSet()), filterGroup, IResource.DEPTH_INFINITE,
					subMonitor);
		}
		}

	}

	/**
	 * Add all of the markers that pass the filters to results.
	 * 
	 * @param results
	 *            Collection of {@link IMarker}
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
	 * Iterate through the return markers. If they do not exist in matching
	 * remove them.
	 * 
	 * @param matching
	 * @param returnMarkers
	 */
	private void findIntersection(Collection matching, Collection returnMarkers) {
		HashSet removeMarkers = new HashSet();
		Iterator existing = returnMarkers.iterator();
		while (existing.hasNext()) {
			Object next = existing.next();
			if (matching.contains(next))
				continue;
			removeMarkers.add(next);
		}
		returnMarkers.removeAll(removeMarkers);

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
					Policy.handle(e);
				}
			}
		}
	
		monitor.done();
	}

	/**
	 * Re-generate all of the markers and filter them based on the enabled
	 * filters.
	 * 
	 * @param subMonitor
	 * @param andFilters
	 *            if <code>true</code> return the intersection of the filters
	 * @param focusResources
	 *            the current selected resources
	 * @param enabledFilters
	 *            the enabled {@link MarkerFieldFilterGroup}s to apply
	 * @return MarkerMap
	 */
	MarkerMap generateFilteredMarkers(SubProgressMonitor subMonitor,
			boolean andFilters, IResource[] focusResources,
			Collection enabledFilters) {

		Collection returnMarkers = null;
		if (enabledFilters.size() > 0) {
			Iterator filtersIterator = enabledFilters.iterator();
			if (andFilters) {
				Collection matching = new HashSet();
				while (filtersIterator.hasNext()) {

					computeMarkers(matching, subMonitor,
							(MarkerFieldFilterGroup) filtersIterator.next(),
							focusResources);
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
							(MarkerFieldFilterGroup) filtersIterator.next(),
							focusResources);
				}
			}

		} else
			returnMarkers = computeAllMarkers(subMonitor);
		MarkerEntry[] entries = new MarkerEntry[returnMarkers.size()];
		Iterator markers = returnMarkers.iterator();
		int index = 0;
		// Convert to entries
		while (markers.hasNext()) {
			entries[index] = new MarkerEntry((IMarker) markers.next());
			index++;
		}

		return new MarkerMap(entries);
	}

	/**
	 * Get the all of the fields that this content generator is using.
	 * 
	 * @return {@link MarkerField}[]
	 */
	MarkerField[] getAllFields() {
		return allFields;
	}

	/**
	 * Get the category name from the receiver.
	 */
	String getCategoryName() {
		return configurationElement
				.getAttribute(ATTRIBUTE_DEFAULT_MARKER_GROUPING);

	}

	/**
	 * Return the configuration elements for the receiver.
	 * 
	 * @return IConfigurationElement[]
	 */
	IConfigurationElement[] getFilterReferences() {
		IConfigurationElement[] filterGroups = configurationElement
				.getChildren(ELEMENT_MARKER_FIELD_CONFIGURATION);
		if (generatorExtensions.isEmpty())
			return filterGroups;
		Iterator extensions = generatorExtensions.iterator();
		Collection extendedElements = new ArrayList();
		while (extensions.hasNext()) {
			IConfigurationElement extension = (IConfigurationElement) extensions
					.next();
			IConfigurationElement[] extensionFilters = extension
					.getChildren(ELEMENT_MARKER_FIELD_CONFIGURATION);
			for (int i = 0; i < extensionFilters.length; i++) {
				extendedElements.add(extensionFilters[i]);
			}
		}
		if (extendedElements.size() > 0) {
			IConfigurationElement[] allGroups = new IConfigurationElement[filterGroups.length
					+ extendedElements.size()];
			System
					.arraycopy(filterGroups, 0, allGroups, 0,
							filterGroups.length);
			Iterator extras = extendedElements.iterator();
			int index = filterGroups.length;
			while (extras.hasNext()) {
				allGroups[index] = (IConfigurationElement) extras.next();
			}
			return allGroups;
		}
		return filterGroups;
	}

	/**
	 * Return the id of the receiver.
	 * 
	 * @return String
	 */
	public String getId() {
		return configurationElement
				.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID);
	}

	/**
	 * Get the list of initially visible fields
	 * 
	 * @return {@link MarkerField}[]
	 */
	MarkerField[] getInitialVisible() {
		return initialVisible;
	}

	/**
	 * Get the group called groupName from the receiver
	 * 
	 * @param groupName
	 * @return MarkerGroup or <code>null</code>
	 */
	MarkerGroup getMarkerGroup(String groupName) {
		Iterator groups = getMarkerGroups().iterator();
		while (groups.hasNext()) {
			MarkerGroup group = (MarkerGroup) groups.next();
			if (group.getId().equals(groupName))
				return group;
		}
		return null;
	}

	/**
	 * Get the markerGroups associated with the receiver.
	 * 
	 * @return Collection of {@link MarkerGroup}
	 */
	Collection getMarkerGroups() {

		if (groups == null) {
			groups = new HashSet();

			// Add the groups defined in the receiver
			addDefinedGroups(groups);

			if (getId().equals(MarkerSupportRegistry.PROBLEMS_GENERATOR)) {
				// Add the groups that reference the receiver.
				groups.addAll(MarkerSupportRegistry.getInstance()
						.getMarkerGroups());

			}
		}
		return groups;
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
						.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID);
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
	 * Return the name for the receiver.
	 * 
	 * @return String
	 */
	String getName() {
		return configurationElement
				.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_NAME);
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
	 * Get the resources in working set.
	 * 
	 * @param workingSet
	 * @return IResource[]
	 */
	private IResource[] getResourcesInWorkingSet(IWorkingSet workingSet) {

		if (workingSet == null)
			return new IResource[0];

		//Return workspace root for aggregates with no containing workingsets,ex. window working set
		if (workingSet.isAggregateWorkingSet()&&workingSet.isEmpty()){
			if(((AggregateWorkingSet) workingSet).getComponents().length==0)
				return new IResource[] { ResourcesPlugin.getWorkspace().getRoot()};
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
	 * Return the type for typeId.
	 * @param typeId
	 * @return {@link MarkerType} or <code>null</code> if
	 * it is not found.
	 */
	MarkerType getType(String typeId) {
		Map all = getTypesTable();
		if(all.containsKey(typeId))
			return (MarkerType) all.get(typeId);
		return null;
	}

	/**
	 * Get the table that maps type ids to markerTypes.
	 * @return Map of {@link String} to {@link MarkerType}
	 */
	private Map getTypesTable() {
		if (allTypesTable == null) {
			allTypesTable = new HashMap();
			
			Iterator allIterator = markerTypes.iterator();
			while (allIterator.hasNext()) {
				MarkerType next = (MarkerType) allIterator.next();
				allTypesTable.put(next.getId(), next);
			}
		}
		return allTypesTable;
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

		IConfigurationElement[] elements = configurationElement
				.getChildren(MARKER_FIELD_REFERENCE);
		Collection allFieldList = new ArrayList();
		Collection initialVisibleList = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			MarkerField field = registry.getField(elements[i]
					.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID));
			if (field == null)
				continue;
			allFieldList.add(field);
			if (!MarkerSupportInternalUtilities.VALUE_FALSE.equals(elements[i]
					.getAttribute(ATTRIBUTE_VISIBLE)))
				initialVisibleList.add(field);
		}

		allFields = new MarkerField[allFieldList.size()];
		allFieldList.toArray(allFields);

		initialVisible = new MarkerField[initialVisibleList.size()];
		initialVisibleList.toArray(initialVisible);

	}

	/**
	 * Remove the element from the generator extensions
	 * 
	 * @param element
	 */
	public void removeExtension(IConfigurationElement element) {
		generatorExtensions.remove(element);

	}
}
