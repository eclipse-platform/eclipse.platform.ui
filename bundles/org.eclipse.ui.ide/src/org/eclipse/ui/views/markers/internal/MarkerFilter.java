/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * MarkerFilter is the class that defines a filter on markers in a
 * MarkerView.
 *
 */
public class MarkerFilter implements Cloneable {

	static final String TAG_ENABLED = "enabled"; //$NON-NLS-1$

	
	/**
	 * The tag for the scope.
	 */
	public static final String TAG_ON_RESOURCE = "onResource"; //$NON-NLS-1$

	private static final String TAG_SELECTED_TYPES = "selectedType"; //$NON-NLS-1$

	private static final String TAG_WORKING_SET = "workingSet"; //$NON-NLS-1$

	private static final String TAG_TYPES_DELIMITER = ":"; //$NON-NLS-1$

	/**
	 * New attribute to handle the selection status of marker types.
	 */
	public static final String TAG_SELECTION_STATUS = "selectionStatus"; //$NON-NLS-1$

	/**
	 * Attribute status true.
	 */
	public static final String SELECTED_FALSE = "false"; //$NON-NLS-1$

	/**
	 * Attribute status false.
	 */
	private static final String SELECTED_TRUE = "true"; //$NON-NLS-1$

	/**
	 * Constant for any element.
	 */
	public static final int ON_ANY = 0;

	/**
	 * Constant for any selected element only.
	 */
	public static final int ON_SELECTED_ONLY = 1;

	/**
	 * Constant for selected element and children.
	 */
	public static final int ON_SELECTED_AND_CHILDREN = 2;

	/**
	 * Constant for any element in same container.
	 */
	public static final int ON_ANY_IN_SAME_CONTAINER = 3;

	/**
	 * Constant for on working set.
	 */
	public static final int ON_WORKING_SET = 4;

	static final int DEFAULT_ON_RESOURCE = ON_ANY;

	static final boolean DEFAULT_ACTIVATION_STATUS = true;

	protected List rootTypes = new ArrayList();

	protected List selectedTypes = new ArrayList();

	protected IWorkingSet workingSet;

	protected int onResource;

	protected boolean enabled;

	private IResource[] focusResource;

	private Set cachedWorkingSet;

	// The human readable name for the filter
	private String name;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param filterName
	 *            The human readable name for the filter
	 * @param rootTypes
	 *            The types this filter will be applied to
	 */
	MarkerFilter(String filterName, String[] rootTypes) {

		name = filterName;

		for (int i = 0; i < rootTypes.length; i++) {
			MarkerType type = MarkerTypesModel.getInstance().getType(
					rootTypes[i]);

			if (!this.rootTypes.contains(type)) {
				this.rootTypes.add(type);
			}
		}
		resetState();
	}

	/**
	 * List all types known to this MarkerFilter.
	 * 
	 * @param types
	 *            list to be filled in with types
	 */
	public void addAllSubTypes(List types) {
		for (int i = 0; i < rootTypes.size(); i++) {
			MarkerType rootType = (MarkerType) rootTypes.get(i);
			addAllSubTypes(types, rootType);
		}
	}

	private void addAllSubTypes(List types, MarkerType type) {
		if (type == null) {
			return;
		}

		if (!types.contains(type)) {
			types.add(type);
		}

		MarkerType[] subTypes = type.getSubtypes();

		for (int i = 0; i < subTypes.length; i++) {
			addAllSubTypes(types, subTypes[i]);
		}
	}

	/**
	 * Adds all markers in the given set of resources to the given list
	 * 
	 * @param resultList
	 * @param resources
	 * @param markerTypeId
	 * @param depth
	 * @throws CoreException
	 */
	private List findMarkers(IResource[] resources, int depth, int limit,
			IProgressMonitor mon, boolean ignoreExceptions)
			throws CoreException {
		if (resources == null) {
			return Collections.EMPTY_LIST;
		}

		List resultList = new ArrayList(resources.length * 2);

		// Optimization: if a type appears in the selectedTypes list along with
		// all of its
		// subtypes, then combine these in a single search.

		// List of types that haven't been replaced by one of their supertypes
		HashSet typesToSearch = new HashSet(selectedTypes.size());

		// List of types that appeared in selectedTypes along with all of their
		// subtypes
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

		mon.beginTask(MarkerMessages.MarkerFilter_searching, typesToSearch
				.size()
				* resources.length);

		// Use this hash set to determine if there are any resources in the
		// list that appear along with their parent.
		HashSet resourcesToSearch = new HashSet();

		// Insert all the resources into the hashset
		for (int idx = 0; idx < resources.length; idx++) {
			IResource next = resources[idx];

			if (!next.exists()) {
				continue;
			}

			if (resourcesToSearch.contains(next)) {
				mon.worked(typesToSearch.size());
			} else {
				resourcesToSearch.add(next);
			}
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

				// Only search for subtypes of the marker if we found all of its
				// subtypes in the filter criteria.
				IMarker[] markers = resource.findMarkers(markerType.getId(),
						includeAllSubtypes.contains(markerType), depth);

				mon.worked(1);

				for (int idx = 0; idx < markers.length; idx++) {
					ConcreteMarker marker;
					try {
						marker = MarkerList.createMarker(markers[idx]);
					} catch (CoreException e) {
						if (ignoreExceptions) {
							continue;
						}
						throw e;

					}

					if (limit != -1 && resultList.size() >= limit) {
						return resultList;
					}

					if (selectMarker(marker)) {
						resultList.add(marker);
					}
				}
			}
		}

		mon.done();

		return resultList;
	}

	/**
	 * Subclasses should override to determine if the given marker passes the
	 * filter.
	 * 
	 * @param marker
	 * @return <code>true</code> if the marker passes the filter and
	 *         <code>false</code> otherwise
	 */
	protected boolean selectMarker(ConcreteMarker marker) {
		return true;
	}

	/**
	 * Searches the workspace for markers that pass this filter.
	 * 
	 * @return Collection of markers.
	 */
	Collection findMarkers(IProgressMonitor mon, boolean ignoreExceptions)
			throws CoreException {

		List unfiltered = Collections.EMPTY_LIST;

		if (!isEnabled()) {
			unfiltered = findMarkers(new IResource[] { ResourcesPlugin
					.getWorkspace().getRoot() }, IResource.DEPTH_INFINITE, -1,
					mon, ignoreExceptions);
		} else {
			// int limit = getFilterOnMarkerLimit() ? getMarkerLimit() + 1 : -1;
			int limit = -1;

			switch (getOnResource()) {
			case ON_ANY: {
				unfiltered = findMarkers(new IResource[] { ResourcesPlugin
						.getWorkspace().getRoot() }, IResource.DEPTH_INFINITE,
						limit, mon, ignoreExceptions);
				break;
			}
			case ON_SELECTED_ONLY: {
				unfiltered = findMarkers(focusResource, IResource.DEPTH_ZERO,
						limit, mon, ignoreExceptions);
				break;
			}
			case ON_SELECTED_AND_CHILDREN: {
				unfiltered = findMarkers(focusResource,
						IResource.DEPTH_INFINITE, limit, mon, ignoreExceptions);
				break;
			}
			case ON_ANY_IN_SAME_CONTAINER: {
				unfiltered = findMarkers(getProjects(focusResource),
						IResource.DEPTH_INFINITE, limit, mon, ignoreExceptions);
				break;
			}
			case ON_WORKING_SET: {
				unfiltered = findMarkers(getResourcesInWorkingSet(),
						IResource.DEPTH_INFINITE, limit, mon, ignoreExceptions);
			}
			}
		}

		if (unfiltered == null) {
			unfiltered = Collections.EMPTY_LIST;
		}

		return unfiltered;
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
	 * Returns a set of strings representing the full pathnames to every
	 * resource directly or indirectly contained in the working set. A resource
	 * is in the working set iff its path name can be found in this set.
	 * 
	 * @return Set
	 */
	private Set getWorkingSetAsSetOfPaths() {
		if (cachedWorkingSet == null) {
			HashSet result = new HashSet();

			addResourcesAndChildren(result, getResourcesInWorkingSet());

			cachedWorkingSet = result;
		}

		return cachedWorkingSet;
	}

	/***************************************************************************
	 * Adds the paths of all resources in the given array to the given set.
	 */
	private void addResourcesAndChildren(HashSet result, IResource[] resources) {
		for (int idx = 0; idx < resources.length; idx++) {

			IResource currentResource = resources[idx];

			result.add(currentResource.getFullPath().toString());

			if (currentResource instanceof IContainer) {
				IContainer cont = (IContainer) currentResource;

				try {
					addResourcesAndChildren(result, cont.members());
				} catch (CoreException e) {
					// Ignore errors
				}
			}

		}
	}

	/**
	 * Returns the set of projects that contain the given set of resources.
	 * 
	 * @param resources
	 * @return IProject[]
	 */
	static IProject[] getProjects(IResource[] resources) {
		if (resources == null) {
			return new IProject[0];
		}

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

	/**
	 * Return whether or not the receiver would select the marker.
	 * 
	 * @param marker
	 * @return boolean
	 */
	public boolean select(ConcreteMarker marker) {
		if (!isEnabled()) {
			return true;
		}

		return selectByType(marker) && selectBySelection(marker)
				&& selectMarker(marker);
	}

	private boolean selectByType(ConcreteMarker marker) {
		return selectedTypes.contains(MarkerTypesModel.getInstance().getType(
				marker.getType()));
	}

	/**
	 * Returns whether the specified marker should be filter out or not.
	 * 
	 * @param marker
	 *            the marker to test
	 * @return true=the marker should not be filtered out false=the marker
	 *         should be filtered out
	 */
	private boolean selectBySelection(ConcreteMarker marker) {
		if (onResource == ON_ANY || marker == null) {
			return true;
		}

		if (focusResource == null) {
			return true;
		}

		IResource resource = marker.getResource();

		if (onResource == ON_WORKING_SET) {

			if (resource != null) {
				return isEnclosed(resource);
			}

		} else if (onResource == ON_ANY_IN_SAME_CONTAINER) {
			IProject project = resource.getProject();

			if (project == null) {
				return false;
			}

			for (int i = 0; i < focusResource.length; i++) {
				IProject selectedProject = focusResource[i].getProject();

				if (selectedProject == null) {
					continue;
				}

				if (project.equals(selectedProject)) {
					return true;
				}
			}
		} else if (onResource == ON_SELECTED_ONLY) {
			for (int i = 0; i < focusResource.length; i++) {
				if (resource.equals(focusResource[i])) {
					return true;
				}
			}
		} else if (onResource == ON_SELECTED_AND_CHILDREN) {
			for (int i = 0; i < focusResource.length; i++) {
				IResource parentResource = resource;

				while (parentResource != null) {
					if (parentResource.equals(focusResource[i])) {
						return true;
					}

					parentResource = parentResource.getParent();
				}
			}
		}

		return false;
	}

	/**
	 * Returns if the given resource is enclosed by a working set element.
	 * Previous versions of this method used IContainmentAdapter for containment
	 * tests. For performance reasons, this is no longer possible. Code that
	 * relies on this behavior should be updated appropriately.
	 * 
	 * @param element
	 *            resource to test for enclosure by a working set element
	 * @return true if element is enclosed by a working set element and false
	 *         otherwise.
	 */
	private boolean isEnclosed(IResource element) {
		if (workingSet == null) {
			return false;
		}

		if (workingSet.isEmpty()) {
			return true; // Everything is in an empty working set
		}
		Set workingSetPaths = getWorkingSetAsSetOfPaths();

		return workingSetPaths.contains(element.getFullPath().toString());
	}

	/**
	 * <ul>
	 * <li><code>MarkerFilter.ON_ANY</code> if showing items associated with
	 * any resource.</li>
	 * <li><code>MarkerFilter.ON_SELECTED_ONLY</code> if showing items
	 * associated with the selected resource within the workbench.</li>
	 * <li><code>MarkerFilter.ON_SELECTED_AND_CHILDREN</code> if showing
	 * items associated with the selected resource within the workbench and its
	 * children.</li>
	 * <li><code>MarkerFilter.ON_ANY_OF_SAME_PROJECT</code> if showing items
	 * in the same project as the selected resource within the workbench.</li>
	 * <li><code>MarkerFilter.ON_WORKING_SET</code> if showing items in some
	 * working set.</li>
	 * </ul>
	 * 
	 * @return int
	 */
	public int getOnResource() {
		return onResource;
	}

	/**
	 * Sets the type of filtering by selection.
	 * 
	 * @param onResource
	 *            must be one of:
	 *            <ul>
	 *            <li><code>MarkerFilter.ON_ANY_RESOURCE</code></li>
	 *            <li><code>MarkerFilter.ON_SELECTED_RESOURCE_ONLY</code></li>
	 *            <li><code>MarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN</code></li>
	 *            <li><code>MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT</code></li>
	 *            <li><code>MarkerFilter.ON_WORKING_SET</code></li>
	 *            </ul>
	 */
	void setOnResource(int onResource) {
		if (onResource >= ON_ANY && onResource <= ON_WORKING_SET) {
			this.onResource = onResource;
		}
	}

	/**
	 * @return the selected resource(s) withing the workbench.
	 */
	IResource[] getFocusResource() {
		return focusResource;
	}

	/**
	 * Sets the focused resources.
	 * 
	 * @param resources
	 */
	public void setFocusResource(IResource[] resources) {
		focusResource = resources;
	}

	/**
	 * @return
	 * <ul>
	 * <li><code>true</code> if the filter is enabled.</li>
	 * <li><code>false</code> if the filter is not enabled.</li>
	 * </ul>
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * <b>Warning:</b> for internal package use only. Return the root marker
	 * types.
	 * 
	 * @return the root marker types.
	 */
	public List getRootTypes() {
		return rootTypes;
	}

	/**
	 * <b>Warning:</b> for internal package use only. Return the selected
	 * types.
	 * 
	 * @return the selected marker types to be displayed.
	 */
	public List getSelectedTypes() {
		return selectedTypes;
	}

	/**
	 * Find the typeModel entry that matches id.
	 * 
	 * @param id
	 *            the ID for a marker type
	 * @return MarkerType or <code>null</code> if it is not found.
	 */
	public MarkerType getMarkerType(String id) {
		return MarkerTypesModel.getInstance().getType(id);
	}

	/**
	 * @return the current working set or <code>null</code> if no working set
	 *         is defined.
	 */
	IWorkingSet getWorkingSet() {
		return workingSet;
	}

	/**
	 * Sets the enablement state of the filter.
	 */
	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Sets the current working set.
	 */
	void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
		cachedWorkingSet = null;
	}

	/**
	 * Reset to the default state.
	 */
	void resetState() {
		enabled = DEFAULT_ACTIVATION_STATUS;
		onResource = DEFAULT_ON_RESOURCE;
		selectedTypes.clear();
		addAllSubTypes(selectedTypes);
		setWorkingSet(null);
	}

	/**
	 * Restore the state in the memento.
	 * 
	 * @param memento
	 */
	public final void restoreState(IMemento memento) {
		resetState();
		restoreFilterSettings(memento);

	}

	/**
	 * Restore the state of the receiver in the supplied settings. This is kept
	 * for backwards compatibility with 3.1 dialog settings.
	 * 
	 * @param settings
	 */
	public void restoreFilterSettings(IDialogSettings settings) {

		resetState();

		String setting = settings.get(TAG_ENABLED);

		if (setting != null) {
			enabled = Boolean.valueOf(setting).booleanValue();
		}

		setting = settings.get(TAG_ON_RESOURCE);

		if (setting != null) {
			try {
				onResource = Integer.parseInt(setting);
			} catch (NumberFormatException eNumberFormat) {
			}
		}

		// new selection list attribute
		// format is "id:(true|false):"
		setting = settings.get(TAG_SELECTION_STATUS);

		if (setting != null) {
			selectedTypes.clear();

			// get the complete list of types
			List newTypes = new ArrayList();
			addAllSubTypes(newTypes);

			StringTokenizer stringTokenizer = new StringTokenizer(setting);

			while (stringTokenizer.hasMoreTokens()) {
				String id = stringTokenizer.nextToken(TAG_TYPES_DELIMITER);
				String status = null;
				if (stringTokenizer.hasMoreTokens()) {
					status = stringTokenizer.nextToken(TAG_TYPES_DELIMITER);
				}

				MarkerType markerType = MarkerTypesModel.getInstance().getType(
						id);
				if (markerType != null) {
					newTypes.remove(markerType);

					// add the type to the selected list
					if (!SELECTED_FALSE.equals(status)
							&& !selectedTypes.contains(markerType)) {
						selectedTypes.add(markerType);
					}
				}
			}

			// any types we know about that weren't either true or
			// false in the selection attribute are new. By default,
			// new marker types will be selected=true
			for (int i = 0; i < newTypes.size(); ++i) {
				selectedTypes.add(newTypes.get(i));
			}
		} else {
			// the settings didn't contain the new selection attribute
			// so check for the old selection attribute.
			// format is just "id:"
			setting = settings.get(TAG_SELECTED_TYPES);

			if (setting != null) {
				generateSelectedTypes(setting);
			}

		}

		setting = settings.get(TAG_WORKING_SET);

		if (setting != null) {
			setWorkingSet(PlatformUI.getWorkbench().getWorkingSetManager()
					.getWorkingSet(setting));
		}
	}

	/**
	 * Set the selected types based on the value.
	 * 
	 * @param selectedTypesValue
	 */
	void generateSelectedTypes(String selectedTypesValue) {
		selectedTypes.clear();
		StringTokenizer stringTokenizer = new StringTokenizer(
				selectedTypesValue);

		while (stringTokenizer.hasMoreTokens()) {
			MarkerType markerType = getMarkerType(stringTokenizer
					.nextToken(TAG_TYPES_DELIMITER));

			if (markerType != null && !selectedTypes.contains(markerType)) {
				selectedTypes.add(markerType);
			}
		}
	}

	/**
	 * Find the markerType matching typeName
	 * 
	 * @param typeName
	 * @return MarkerType
	 */
	MarkerType findMarkerType(String typeName) {
		return MarkerTypesModel.getInstance().getType(typeName);
	}

	/**
	 * Restore the state of the receiver in the supplied settings.
	 * 
	 * @param memento
	 */
	protected void restoreFilterSettings(IMemento memento) {
		String setting = memento.getString(TAG_ENABLED);

		if (setting != null) {
			enabled = Boolean.valueOf(setting).booleanValue();
		}

		Integer resourceSetting = memento.getInteger(TAG_ON_RESOURCE);

		if (resourceSetting != null) {
			onResource = resourceSetting.intValue();
		}

		// new selection list attribute
		// format is "id:(true|false):"
		setting = memento.getString(TAG_SELECTION_STATUS);

		if (setting != null) {
			selectedTypes.clear();

			// get the complete list of types
			List newTypes = new ArrayList();
			addAllSubTypes(newTypes);

			StringTokenizer stringTokenizer = new StringTokenizer(setting);

			while (stringTokenizer.hasMoreTokens()) {
				String id = stringTokenizer.nextToken(TAG_TYPES_DELIMITER);
				String status = null;
				if (stringTokenizer.hasMoreTokens()) {
					status = stringTokenizer.nextToken(TAG_TYPES_DELIMITER);
				}

				MarkerType markerType = MarkerTypesModel.getInstance().getType(
						id);
				if (markerType != null) {
					newTypes.remove(markerType);

					// add the type to the selected list
					if (!SELECTED_FALSE.equals(status)
							&& !selectedTypes.contains(markerType)) {
						selectedTypes.add(markerType);
					}
				}
			}

			// any types we know about that weren't either true or
			// false in the selection attribute are new. By default,
			// new marker types will be selected=true
			for (int i = 0; i < newTypes.size(); ++i) {
				selectedTypes.add(newTypes.get(i));
			}
		} else {
			// the settings didn't contain the new selection attribute
			// so check for the old selection attribute.
			// format is just "id:"
			setting = memento.getString(TAG_SELECTED_TYPES);

			if (setting != null) {
				generateSelectedTypes(setting);
			}

		}

		setting = memento.getString(TAG_WORKING_SET);

		if (setting != null) {
			setWorkingSet(PlatformUI.getWorkbench().getWorkingSetManager()
					.getWorkingSet(setting));
		}
	}

	/**
	 * Save the filter settings for the receiver.
	 * 
	 * @param settings
	 */
	public void saveFilterSettings(IMemento settings) {

		settings.putString(TAG_ENABLED, String.valueOf(enabled));
		settings.putInteger(TAG_ON_RESOURCE, onResource);

		String markerTypeIds = ""; //$NON-NLS-1$

		List includedTypes = new ArrayList();
		addAllSubTypes(includedTypes);
		for (int i = 0; i < includedTypes.size(); i++) {
			MarkerType markerType = (MarkerType) includedTypes.get(i);
			markerTypeIds += markerType.getId() + TAG_TYPES_DELIMITER;
			if (selectedTypes.contains(markerType)) {
				markerTypeIds += SELECTED_TRUE + TAG_TYPES_DELIMITER;
			} else {
				markerTypeIds += SELECTED_FALSE + TAG_TYPES_DELIMITER;
			}
		}

		settings.putString(TAG_SELECTION_STATUS, markerTypeIds);

		if (workingSet != null) {
			settings.putString(TAG_WORKING_SET, workingSet.getName());
		}
	}

	/**
	 * Get the name of the receiver
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Make a clone of the receiver.
	 * 
	 * @return MarkerFilter
	 * @throws CloneNotSupportedException
	 */
	public MarkerFilter makeClone() throws CloneNotSupportedException {
		return (MarkerFilter) this.clone();
	}

	/**
	 * Set the selected types.
	 * 
	 * @param selectedTypes
	 *            List of MarkerType.
	 */
	public void setSelectedTypes(List selectedTypes) {
		this.selectedTypes = selectedTypes;
	}

}
