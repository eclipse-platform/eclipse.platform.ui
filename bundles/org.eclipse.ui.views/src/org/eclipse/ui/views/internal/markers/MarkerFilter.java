/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.internal.markers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IContainmentAdapter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.internal.WorkbenchPlugin;


/**
 * Filters IMarker's based on their attributes and the current selection within the workbench.
 */
public class MarkerFilter implements IFilter {
	
	private static final String TAG_DIALOG_SECTION = "filter"; //$NON-NLS-1$
	private static final String TAG_ENABLED = "enabled"; //$NON-NLS-1$
	private static final String TAG_FILTER_ON_MARKER_LIMIT = "filterOnMarkerLimit"; //$NON-NLS-1$
	private static final String TAG_MARKER_LIMIT = "markerLimit"; //$NON-NLS-1$
	private static final String TAG_ON_RESOURCE = "onResource"; //$NON-NLS-1$
	private static final String TAG_SELECTED_TYPES = "selectedType"; //$NON-NLS-1$
	private static final String TAG_WORKING_SET = "workingSet"; //$NON-NLS-1$
	private static final String TAG_TYPES_DELIMITER = ":"; //$NON-NLS-1$
	
	// Filter on resource constants
	static final int ON_ANY_RESOURCE = 0;
	static final int ON_SELECTED_RESOURCE_ONLY = 1;
	static final int ON_SELECTED_RESOURCE_AND_CHILDREN = 2;
	static final int ON_ANY_RESOURCE_OF_SAME_PROJECT = 3;
	static final int ON_WORKING_SET = 4;

	static final int DEFAULT_MARKER_LIMIT = 2000;
	static final boolean DEFAULT_FILTER_ON_MARKER_LIMIT = true;
	static final int DEFAULT_ON_RESOURCE = ON_ANY_RESOURCE;
	static final boolean DEFAULT_ACTIVATION_STATUS = true;

	protected List rootTypes = new ArrayList();
	protected List selectedTypes = new ArrayList();
	protected IWorkingSet workingSet;
	protected int onResource;
	protected boolean filterOnMarkerLimit;
	protected boolean enabled;
	protected int markerLimit;
	
	private MarkerTypesModel typesModel;
	
	private IResource[] focusResource;
	
	public MarkerFilter(String[] rootTypes) {
		typesModel = new MarkerTypesModel();
		for (int i = 0; i < rootTypes.length; i++) {
			MarkerType type = typesModel.getType(rootTypes[i]);
			if (!this.rootTypes.contains(type)) {
				this.rootTypes.add(type);
			}
		}
	}
	
	private void addAllSubTypes() {
		for (int i = 0; i < rootTypes.size(); i++) {
			MarkerType rootType = (MarkerType) rootTypes.get(i);
			addAllSubTypes(rootType);
		}
	}
	
	private void addAllSubTypes(MarkerType type) {
		if (type == null) {
			return;
		}
		if (!selectedTypes.contains(type)) {
			selectedTypes.add(type);
		}
		MarkerType[] subTypes = type.getSubtypes();
		for (int i = 0; i < subTypes.length; i++) {
			addAllSubTypes(subTypes[i]);
		}
	} 
	
	/**
	 * @see org.eclipse.ui.views.internal.markerregistry.IFilter#filter(java.util.List)
	 */
	public Object[] filter(Object[] elements) {
		if (elements == null)	
			return new Object[0];
		List filteredElements = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			Object element = elements[i];
			if (select(element)) {
				filteredElements.add(element); 
			}
		}
		return filteredElements.toArray();
	}

	/**
	 * @see org.eclipse.ui.views.internal.markerregistry.IFilter#select(java.lang.Object)
	 */
	public boolean select(Object item) {
		if (!isEnabled()) {
			return true;
		}
		
		if (!(item instanceof IMarker))
			return false;

		IMarker marker = (IMarker) item;
		return selectByType(marker) && selectBySelection(marker);
	}
	
	private boolean selectByType(IMarker marker) {
		String type;
		try {
			type = marker.getType();
		}
		catch (CoreException e) {
			return false;
		}
		
		MarkerType markerType = typesModel.getType(type);
		if (selectedTypes.contains(markerType)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns whether the specified marker should be filter out or not.
	 * 
	 * @param marker the marker to test
	 * @return 
	 * 	true=the marker should not be filtered out
	 * 	false=the marker should be filtered out
	 */
	private boolean selectBySelection(IMarker marker) {
		if (onResource == ON_ANY_RESOURCE || marker == null) {
			return true;
		}
		if (focusResource == null) {
			return true;
		}
		IResource resource = marker.getResource();
		if (onResource == ON_WORKING_SET) {
			if (workingSet == null) {
				return true;
			}			
			if (resource != null) {
				return isEnclosed(resource);
			}
		}
		else if (onResource == ON_ANY_RESOURCE_OF_SAME_PROJECT) {
			IProject project = resource.getProject();
			for (int i = 0; i < focusResource.length; i++) {
				IProject selectedProject = focusResource[i].getProject();
				if (project.equals(selectedProject)) {
					return true;
				}
			}
		}
		else if (onResource == ON_SELECTED_RESOURCE_ONLY) {
			for (int i = 0; i < focusResource.length; i++) {
				if (resource.equals(focusResource[i])) {
					return true;
				}
			}
		}
		else if (onResource == ON_SELECTED_RESOURCE_AND_CHILDREN) {
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
	 * The IContainmentAdapter of each working set element is used for the
	 * containment test. If there is no IContainmentAdapter for a working 
	 * set element, a simple resource based test is used. 
	 * 
	 * @param element resource to test for enclosure by a working set
	 * 	element 
	 * @return true if element is enclosed by a working set element and 
	 * 	false otherwise. 
	 */
	private boolean isEnclosed(IResource element) {
		IPath elementPath = element.getFullPath();
		IAdaptable[] workingSetElements = workingSet.getElements();
		
		if (elementPath.isEmpty() || elementPath.isRoot()) {
			return false;
		}
		for (int i = 0; i < workingSetElements.length; i++) {
			IAdaptable workingSetElement = workingSetElements[i];
			IContainmentAdapter containmentAdapter = (IContainmentAdapter) workingSetElement.getAdapter(IContainmentAdapter.class);
			
			// if there is no IContainmentAdapter defined for the working  
			// set element type fall back to using resource based  
			// containment check 
			if (containmentAdapter != null) {
				if (containmentAdapter.contains(workingSetElement, element, IContainmentAdapter.CHECK_CONTEXT | IContainmentAdapter.CHECK_IF_CHILD | IContainmentAdapter.CHECK_IF_DESCENDANT))
					return true;
			} else if (isEnclosedResource(element, elementPath, workingSetElement)) {
				return true;
			}		
		}
		return false;
	}

	/**
	 * Returns if the given resource is enclosed by a working set element.
	 * A resource is enclosed if it is either a parent of a working set 
	 * element, a child of a working set element or a working set element
	 * itself.
	 * Simple path comparison is used. This is only guaranteed to return
	 * correct results for resource working set elements. 
	 * 
	 * @param element resource to test for enclosure by a working set
	 * 	element
	 * @param elementPath full, absolute path of the element to test 
	 * @return true if element is enclosed by a working set element and 
	 * 	false otherwise. 
	 */
	private boolean isEnclosedResource(IResource element, IPath elementPath, IAdaptable workingSetElement) {
		IResource workingSetResource = null;
		
		if (workingSetElement.equals(element))
			return true;
		if (workingSetElement instanceof IResource) {
			workingSetResource = (IResource) workingSetElement;
		}
		else {
			workingSetResource = (IResource) workingSetElement.getAdapter(IResource.class);
		}	
		if (workingSetResource != null) {
			IPath resourcePath = workingSetResource.getFullPath();
			if (resourcePath.isPrefixOf(elementPath))
				return true;
		}
		return false;
	}

	/**
	 * @return the defined limit on the number of markers to be displayed.
	 */
	public int getMarkerLimit() {
		return markerLimit;
	}

	/**
	 * Sets the limit on the number of markers to be displayed.
	 * 
	 * @param the new limit
	 */
	public void setMarkerLimit(int i) {
		markerLimit = i;
	}

	/**
	 * @return <ul>
	 * <li><code>MarkerFilter.ON_ANY_RESOURCE</code> if showing items associated with any resource.</li>
	 * <li><code>MarkerFilter.ON_SELECTED_RESOURCE_ONLY</code> if showing items associated with
	 * the selected resource within the workbench.</li>
	 * <li><code>MarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN</code> if showing items associated with
	 * the selected resource within the workbench and its children.</li>
	 * <li><code>MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT</code> if showing items in the same project
	 * as the selected resource within the workbench.</li>
	 * <li><code>MarkerFilter.ON_WORKING_SET</code> if showing items in some working set.</li>
	 * </ul>
	 */
	public int getOnResource() {
		return onResource;
	}

	/**
	 * Sets the type of filtering by selection.
	 * 
	 * @param onResource must be one of:
	 * <ul>
	 * <li><code>MarkerFilter.ON_ANY_RESOURCE</code></li>
	 * <li><code>MarkerFilter.ON_SELECTED_RESOURCE_ONLY</code></li>
	 * <li><code>MarkerFilter.ON_SELECTED_RESOURCE_AND_CHILDREN</code></li>
	 * <li><code>MarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT</code></li>
	 * <li><code>MarkerFilter.ON_WORKING_SET</code></li>
	 * </ul>
	 */
	public void setOnResource(int onResource) {
		if (onResource >= ON_ANY_RESOURCE && onResource <= ON_WORKING_SET) {
			this.onResource = onResource;
		}
	}

	/**
	 * @return the selected resource(s) withing the workbench.
	 */
	public IResource[] getFocusResource() {
		return focusResource;
	}

	/**
	 * Sets the focused resources.
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
	 * @return
	 * <ul>
	 * <li><code>true</code> if filtering by marker limit is enabled.</li>
	 * <li><code>false</code> if filtering by marker limit is not enabled.</li>
	 * </ul>
	 */
	public boolean getFilterOnMarkerLimit() {
		return filterOnMarkerLimit;
	}

	/**
	 * @return the root marker types.
	 */
	public List getRootTypes() {
		return rootTypes;
	}

	/**
	 * @return the selected marker types to be displayed.
	 */
	public List getSelectedTypes() {
		return selectedTypes;
	}

	/**
	 * @return the current working set or <code>null</code> if no working set is defined.
	 */
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}

	/**
	 * Sets the enablement state of the filter.
	 */
	public void setEnabled(boolean b) {
		enabled = b;
	}

	/**
	 * Sets the enablement state of filtering by marker limit.
	 */
	public void setFilterOnMarkerLimit(boolean b) {
		filterOnMarkerLimit = b;
	}

	/**
	 * Sets the selected marker types to be displayed. The List <b>MUST ONLY</b> contain 
	 * <code>MarkerType</code> objects.
	 */
	public void setSelectedTypes(List list) {
		selectedTypes = list;
	}

	/**
	 * Sets the current working set.
	 */
	public void setWorkingSet(IWorkingSet set) {
		workingSet = set;
	}
	
	/**
	 * Saves the filter state in a section of the dialog settings.
	 * 
	 * @param dialogSettings the dialog settings where the filter will save its state.
	 */
	public void saveState(IDialogSettings dialogSettings) {
		if (dialogSettings == null) {
			return;
		}

		IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			settings = dialogSettings.addNewSection(TAG_DIALOG_SECTION);
		}
		
		settings.put(TAG_ENABLED, enabled);
		settings.put(TAG_FILTER_ON_MARKER_LIMIT, filterOnMarkerLimit);
		settings.put(TAG_MARKER_LIMIT, markerLimit);
		settings.put(TAG_ON_RESOURCE, onResource);
		
		String types = ""; //$NON-NLS-1$
		for (int i = 0; i < selectedTypes.size(); i++) {
			MarkerType type = (MarkerType) selectedTypes.get(i);
			types += type.getId() + TAG_TYPES_DELIMITER;
		}
		settings.put(TAG_SELECTED_TYPES, types);
		
		if (workingSet != null) {
			settings.put(TAG_WORKING_SET, workingSet.getName());
		}
	}
	
	/**
	 * Attempts to restore its state from the provided dialog settings. If the IDialogSettings object
	 * does not contain a section named "filter", the filter resets its state.
	 * 
	 * @param dialogSettings that contains state information about the filter.
	 */
	public void restoreState(IDialogSettings dialogSettings) {
		if (dialogSettings == null) {
			resetState();
			return;
		}
		
		IDialogSettings settings = dialogSettings.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			resetState();
			return;
		}
		
		String setting = settings.get(TAG_ENABLED);
		if (setting == null) {
			enabled = DEFAULT_ACTIVATION_STATUS;
		}
		else {
			enabled = settings.getBoolean(TAG_ENABLED);
		}
		setting = settings.get(TAG_FILTER_ON_MARKER_LIMIT);
		if (setting == null) {
			filterOnMarkerLimit = DEFAULT_FILTER_ON_MARKER_LIMIT;
		}
		else {
			filterOnMarkerLimit = Boolean.valueOf(setting).booleanValue();
		}
		setting = settings.get(TAG_MARKER_LIMIT);
		if (setting == null) {
			markerLimit = DEFAULT_MARKER_LIMIT;
		}
		else {
			try {
				markerLimit = Integer.parseInt(setting); 
			}
			catch (NumberFormatException e) {
				markerLimit = DEFAULT_MARKER_LIMIT;
			}
		}
		setting = settings.get(TAG_ON_RESOURCE);
		if (setting == null) {
			setOnResource(DEFAULT_ON_RESOURCE);
		}
		else {
			try {
				setOnResource(Integer.parseInt(setting));
			}
			catch (NumberFormatException e) {
				setOnResource(DEFAULT_ON_RESOURCE);
			}
		}
		restoreWorkingSet(settings.get(TAG_WORKING_SET));

		String types = settings.get(TAG_SELECTED_TYPES);
		if (types == null) {
			addAllSubTypes();
		}
		else {
			for (StringTokenizer tokenizer = new StringTokenizer(types); tokenizer.hasMoreTokens();) {
				String token = tokenizer.nextToken(TAG_TYPES_DELIMITER);
				MarkerType type = typesModel.getType(token);
				if (type != null && !selectedTypes.contains(type)) {
					selectedTypes.add(type);
				}
			}
		}
	}
	
	/**
	 * Restores the saved working set, if any.
	 * 
	 * @param the saved working set name or null
	 */
	private void restoreWorkingSet(String workingSetName) {
		if (workingSetName != null) {
			IWorkingSetManager workingSetManager = WorkbenchPlugin.getDefault().getWorkingSetManager();
			IWorkingSet workingSet = workingSetManager.getWorkingSet(workingSetName);
			
			if (workingSet != null) {
				this.workingSet = workingSet;
			}
		}
	}

	/**
	 * Resets the state of the filter to its default settings.
	 */
	public void resetState() {
		enabled = DEFAULT_ACTIVATION_STATUS;
		filterOnMarkerLimit = DEFAULT_FILTER_ON_MARKER_LIMIT;
		markerLimit = DEFAULT_MARKER_LIMIT;
		onResource = DEFAULT_ON_RESOURCE;
		addAllSubTypes();
	}
	
}
