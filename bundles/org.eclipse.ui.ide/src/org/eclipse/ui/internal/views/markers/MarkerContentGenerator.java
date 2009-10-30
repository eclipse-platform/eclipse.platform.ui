/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.FilterConfigurationArea;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.internal.ContentGeneratorDescriptor;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;
import org.eclipse.ui.views.markers.internal.ProblemFilter;
import org.eclipse.ui.views.markers.internal.Util;

/**
 * MarkerContentGenerator is the representation of the markerContentGenerator
 * extension point.
 * 
 * @since 3.4
 * 
 */
public class MarkerContentGenerator {

	/**
	 * The IMemeto Tags
	 */
	private static final String TAG_COLUMN_VISIBILITY = "visible"; //$NON-NLS-1$
	private static final String TAG_FILTERS_SECTION = "filterGroups"; //$NON-NLS-1$
	private static final String TAG_GROUP_ENTRY = "filterGroup"; //$NON-NLS-1$
	private static final String TAG_AND = "andFilters"; //$NON-NLS-1$
	private static final String TAG_LEGACY_FILTER_ENTRY = "filter"; //$NON-NLS-1$
	
	/**
	 * The job family for content updates
	 */

	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];

	private ContentGeneratorDescriptor generatorDescriptor;

	// fields
	private MarkerField[] visibleFields;

	// filters
	private Collection enabledFilters;
	private Collection filters;
	private boolean andFilters = false;

	/**
	 * focusResources
	 * 
	 */
	private IResource[] selectedResources = MarkerSupportInternalUtilities.EMPTY_RESOURCE_ARRAY;

	private CachedMarkerBuilder builder;
	private String viewId;

	private IPropertyChangeListener filterPreferenceListener;

	/**
	 * Create a new MarkerContentGenerator
	 * 
	 * @param generatorDescriptor
	 * @param builder 
	 * @param viewId 
	 * 				needed for backward compatibility
	 */
	public MarkerContentGenerator(
			ContentGeneratorDescriptor generatorDescriptor,
			CachedMarkerBuilder builder, String viewId) {
		this.generatorDescriptor = generatorDescriptor;
		this.viewId = viewId;
		setBuilder(builder);
	}

	/**
	 * Attach the generator to a builder
	 * 
	 * @param builder
	 */
	void setBuilder(CachedMarkerBuilder builder) {
		this.builder = builder;
		initializePreferenceListener();
		this.builder.setGenerator(this);
	}

	/**
	 * @return Returns the builder this attached to.
	 */
	CachedMarkerBuilder getBuilder() {
		return builder;
	}

	/**
	 * If attached to a builder, request marker update.
	 */
	void requestMarkerUpdate() {
		if (builder != null) {
			builder.scheduleUpdate(0L);
		}
	}

	/**
	 * Return whether or not all of {@link MarkerTypesModel} arein the
	 * selectedTypes.
	 * 
	 * @param selectedTypes
	 * @return boolean
	 */
	boolean allTypesSelected(Collection selectedTypes) {
		return generatorDescriptor.allTypesSelected(selectedTypes);
	}

	/**
	 * Get the all of the fields that this content generator is using.
	 * 
	 * @return {@link MarkerField}[]
	 */
	MarkerField[] getAllFields() {
		return generatorDescriptor.getAllFields();
	}

	/**
	 * Get the fields that this content generator is displaying.
	 * 
	 * @return {@link MarkerField}[]
	 */
	MarkerField[] getVisibleFields() {
		return visibleFields;
	}

	/**
	 * Set the visible fields.
	 * 
	 * @param visible
	 */
	void setVisibleFields(Collection visible) {

		MarkerField[] newFields = new MarkerField[visible.size()];
		visible.toArray(newFields);
		visibleFields = newFields;

	}

	/**
	 * Return the fields not being shown currently.
	 * 
	 * @return Object[]
	 */
	Object[] getHiddenFields() {
		MarkerField[] all = getAllFields();
		MarkerField[] visible = getVisibleFields();

		Collection hidden = new HashSet();
		for (int i = 0; i < all.length; i++) {
			hidden.add(all[i]);
		}
		for (int i = 0; i < visible.length; i++) {
			hidden.remove(visible[i]);
		}
		return hidden.toArray();
	}

	void initialise(IMemento memento) {
		initialiseVisibleFields(memento);
	}

	void saveSate(IMemento memento, MarkerField[] displayedFields) {
		for (int i = 0; i < displayedFields.length; i++) {
			memento.createChild(TAG_COLUMN_VISIBILITY, displayedFields[i]
					.getConfigurationElement().getAttribute(
							MarkerSupportInternalUtilities.ATTRIBUTE_ID));
		}
	}

	void restoreState(IMemento memento) {
		initialiseVisibleFields(memento);
	}

	/**
	 * Initialize the visible fields based on the initial settings or the
	 * contents of the {@link IMemento}
	 * 
	 * @param memento
	 *            IMemento
	 */
	private void initialiseVisibleFields(IMemento memento) {

		if (memento == null
				|| memento.getChildren(TAG_COLUMN_VISIBILITY).length == 0) {
			MarkerField[] initialFields = getInitialVisible();

			visibleFields = new MarkerField[initialFields.length];
			System.arraycopy(initialFields, 0, visibleFields, 0,
					initialFields.length);
			return;
		}

		IMemento[] visible = memento.getChildren(TAG_COLUMN_VISIBILITY);
		Collection newVisible = new ArrayList();

		MarkerField[] all = getAllFields();
		Hashtable allTable = new Hashtable();

		for (int i = 0; i < all.length; i++) {
			allTable.put(all[i].getConfigurationElement().getAttribute(
					MarkerSupportInternalUtilities.ATTRIBUTE_ID), all[i]);
		}

		for (int i = 0; i < visible.length; i++) {
			String key = visible[i].getID();
			if (allTable.containsKey(key)) {
				newVisible.add(allTable.get(key));
			}
		}

		visibleFields = new MarkerField[newVisible.size()];
		newVisible.toArray(visibleFields);
	}

	/**
	 * Return a collection of all of the configuration fields for this generator
	 * 
	 * @return Collection of {@link FilterConfigurationArea}
	 */
	Collection createFilterConfigurationFields() {
		Collection result = new ArrayList();
		for (int i = 0; i < visibleFields.length; i++) {
			FilterConfigurationArea area = MarkerSupportInternalUtilities
					.generateFilterArea(visibleFields[i]);
			if (area != null)
				result.add(area);

		}
		return result;
	}

	/**
	 * Get the category name from the receiver.
	 */
	String getCategoryName() {
		return generatorDescriptor.getCategoryName();

	}

	/**
	 * Return all of the filters for the receiver.
	 * 
	 * @return Collection of MarkerFieldFilterGroup
	 */
	Collection getAllFilters() {
		if (filters == null) {
			filters = getDeclaredFilters();
			// Apply the last settings
			loadFiltersPreference();

		}
		return filters;
	}

	/**
	 * Return the currently enabled filters.
	 * 
	 * @return Collection of MarkerFieldFilterGroup
	 */
	Collection getEnabledFilters() {
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
	 * Rebuild the list of filters
	 */
	protected void rebuildFilters() {
		filters = null;
		enabledFilters = null;
		requestMarkerUpdate();
	}

	/**
	 * Disable all of the filters in the receiver.
	 */
	void disableAllFilters() {
		Collection allFilters = getEnabledFilters();
		Iterator enabled = allFilters.iterator();
		while (enabled.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) enabled
					.next();
			group.setEnabled(false);
		}
		allFilters.clear();
		writeFiltersPreference();
		requestMarkerUpdate();
	}

	/**
	 * Add group to the enabled filters.
	 * 
	 * @param group
	 */
	void toggleFilter(MarkerFieldFilterGroup group) {
		Collection enabled = getEnabledFilters();
		if (enabled.remove(group)) // true if it was present
			group.setEnabled(false);

		else {
			group.setEnabled(true);
			enabled.add(group);
		}
		writeFiltersPreference();
		requestMarkerUpdate();
	}

	/**
	 * Update the filters.
	 * 
	 * @param filters
	 * @param
	 */
	void updateFilters(Collection filters, boolean andFilters) {
		setAndFilters(andFilters);
		this.filters = filters;
		enabledFilters = null;
		writeFiltersPreference();
		requestMarkerUpdate();
	}

	/**
	 * Set whether the filters are being ANDed or ORed.
	 * 
	 * @param and
	 */
	void setAndFilters(boolean and) {
		andFilters = and;
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
	 * @return Collection of declared MarkerFieldFilterGroup(s)
	 */
	Collection getDeclaredFilters() {
		List filters = new ArrayList();
		IConfigurationElement[] filterReferences = generatorDescriptor.getFilterReferences();
		for (int i = 0; i < filterReferences.length; i++) {
			filters.add(new MarkerFieldFilterGroup(filterReferences[i], this));
		}

		// Honour the deprecated problemFilters
		if (viewId != null && viewId.equals(IPageLayout.ID_PROBLEM_VIEW)) {
			Iterator problemFilters = MarkerSupportRegistry.getInstance()
					.getRegisteredFilters().iterator();
			while (problemFilters.hasNext())
				filters.add(new CompatibilityMarkerFieldFilterGroup(
						(ProblemFilter) problemFilters.next(), this));
		}
		return filters;
	}

	/**
	 * Get the name of the filters preference for the receiver,
	 * 
	 * @return String
	 */
	private String getLegacyFiltersPreferenceName() {
		if (viewId != null && viewId.equals(IPageLayout.ID_BOOKMARKS))
			return IDEInternalPreferences.BOOKMARKS_FILTERS;
		if (viewId != null && viewId.equals(IPageLayout.ID_TASK_LIST))
			return IDEInternalPreferences.TASKS_FILTERS;
		return IDEInternalPreferences.PROBLEMS_FILTERS;

	}

	/**
	 * Load the settings from the memento.
	 * 
	 * @param memento
	 */
	private void loadFilterSettings(IMemento memento) {

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
	 * Load the filters defined in memento string.
	 * 
	 * @param mementoString
	 */
	private void loadFiltersFrom(String mementoString) {
		if (mementoString.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT))
			return;

		try {
			loadFilterSettings(XMLMemento.createReadRoot(new StringReader(
					mementoString)));
		} catch (WorkbenchException e) {
			StatusManager.getManager().handle(e.getStatus());
		}
	}

	/**
	 * Load the filters preference.
	 */
	private void loadFiltersPreference() {

		loadFiltersFrom(IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getString(getMementoPreferenceName()));

		String legacyFilters = getLegacyFiltersPreferenceName();
		String migrationPreference = legacyFilters
				+ MarkerSupportInternalUtilities.MIGRATE_PREFERENCE_CONSTANT;

		if (IDEWorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(
				migrationPreference))
			return;// Already migrated

		// Load any defined in a pre 3.4 workbench
		loadLegacyFiltersFrom(IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore().getString(legacyFilters));

		// Mark as migrated
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(
				migrationPreference, true);
	}

	/**
	 * @return preferenceName
	 */
	private String getMementoPreferenceName() {
		return CachedMarkerBuilder.getMementoPreferenceName(viewId);
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
	 * Load the legacy filter into the system.
	 * 
	 * @param child
	 */
	private void loadLegacyFilter(IMemento child) {
		MarkerFieldFilterGroup newGroup = new MarkerFieldFilterGroup(null, this);
		newGroup.legacyLoadSettings(child);
		getAllFilters().add(newGroup);

	}

	/**
	 * Load the pre-3.4 filters.
	 * 
	 * @param mementoString
	 */
	private void loadLegacyFiltersFrom(String mementoString) {

		if (mementoString.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT))
			return;
		IMemento memento;
		try {
			memento = XMLMemento
					.createReadRoot(new StringReader(mementoString));
			restoreLegacyFilters(memento);
		} catch (WorkbenchException e) {
			StatusManager.getManager().handle(e.getStatus());
			return;
		}

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
	 * Restore the pre-3.4 filters.
	 * 
	 * @param memento
	 */
	private void restoreLegacyFilters(IMemento memento) {

		IMemento[] sections = null;
		if (memento != null)
			sections = memento.getChildren(TAG_LEGACY_FILTER_ENTRY);

		for (int i = 0; i < sections.length; i++) {
			IMemento child = sections[i];
			String id = child.getString(IMemento.TAG_ID);
			if (id == null)
				continue;
			loadLegacyFilter(child);
		}

	}

	/**
	 * 
	 */
	private void writeFiltersPreference() {
		XMLMemento memento = XMLMemento.createWriteRoot(TAG_FILTERS_SECTION);

		writeFiltersSettings(memento);

		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
		} catch (IOException e) {
			IDEWorkbenchPlugin.getDefault().getLog().log(Util.errorStatus(e));
		}
		// TODO: We need to migrate this the current class
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().putValue(
				getMementoPreferenceName(),
				writer.toString());
		IDEWorkbenchPlugin.getDefault().savePluginPreferences();
	}

	/**
	 * Create a preference listener for any preference updates.
	 */
	private void initializePreferenceListener() {
		if (filterPreferenceListener == null) {
			filterPreferenceListener = new IPropertyChangeListener() {

				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals(getMementoPreferenceName())) {
						rebuildFilters();
					}

				}
			};
			IDEWorkbenchPlugin.getDefault().getPreferenceStore()
					.addPropertyChangeListener(filterPreferenceListener);
		}
	}

	/**
	 * Write the settings for the filters to the memento.
	 * 
	 * @param memento
	 */
	private void writeFiltersSettings(XMLMemento memento) {

		memento.putBoolean(TAG_AND, andFilters());

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
	 * Return the id of the receiver.
	 * 
	 * @return String
	 */
	public String getId() {
		return generatorDescriptor.getId();
	}

	/**
	 * @param viewId
	 *            if attached to a view
	 */
	void setViewID(String viewId) {
		this.viewId = viewId;
	}

	/**
	 * @return Returns the viewId of view it is attached to or null otherwise.
	 */
	public Object getViewId() {
		return viewId;
	}

	/**
	 * Get the list of initially visible fields
	 * 
	 * @return {@link MarkerField}[]
	 */
	MarkerField[] getInitialVisible() {
		return generatorDescriptor.getInitialVisible();
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
		return generatorDescriptor.getMarkerGroups();
	}

	/**
	 * Return the markerTypes for the receiver.
	 * 
	 * @return Collection of {@link MarkerType}
	 */
	public Collection getMarkerTypes() {
		return generatorDescriptor.getMarkerTypes();
	}

	/**
	 * Return the markerTypes for the receiver.
	 * 
	 * @return Array of type Ids
	 */
	public String[] getTypes() {
		Collection types = getMarkerTypes();
		String[] ids = new String[types.size()];
		Iterator iterator = types.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			ids[i++] = ((MarkerType) iterator.next()).getId();
		}
		return ids;
	}

	/**
	 * Return the name for the receiver.
	 * 
	 * @return String
	 */
	String getName() {
		return generatorDescriptor.getName();
	}

	/**
	 * Return the type for typeId.
	 * 
	 * @param typeId
	 * @return {@link MarkerType} or <code>null</code> if it is not found.
	 */
	MarkerType getType(String typeId) {
		return generatorDescriptor.getType(typeId);
	}

	/**
	 * Select the given MarkerEntry
	 * 	@return <code>true</code> if it matches all enabled filters 
	 */
	boolean select(MarkerEntry entry) {
		 Collection enabledFilters=getEnabledFilters();
		IResource[] resources =getSelectedResources();
		boolean andFilters = andFilters();
		if (enabledFilters.size() > 0) {
			Iterator filtersIterator = enabledFilters.iterator();
			if (andFilters) {
				while (filtersIterator.hasNext()) {
					MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) filtersIterator
							.next();
					if (!group.selectByScope(entry, resources)
							|| !group.selectByFilters(entry)) {
						return false;
					}
				}
				return true;
			}

			while (filtersIterator.hasNext()) {
				MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) filtersIterator
						.next();
				if (group.selectByScope(entry, resources)
						&& group.selectByFilters(entry)) {
					return true;
				}
			}
			return false;

		}
		return true;
	}

	/**
	 * Update the focus resources from list. If there is an update required
	 * return <code>true</code>. This method assumes that there are filters on
	 * resources enabled.
	 * 
	 * @param elements
	 */
	void internalUpdateSelectedElements(Object[] elements) {
		Collection resourceCollection = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof IResource) {
				resourceCollection.add(elements[i]);
			} else {
				addResources(resourceCollection,
						((ResourceMapping) elements[i]));
			}
		}

		selectedResources = new IResource[resourceCollection.size()];
		resourceCollection.toArray(selectedResources);
	}

	/**
	 * Update the receiver for a change in selection.
	 * 
	 * @param newElements
	 */
	void updateSelectedResource(Object[] newElements) {
		if (updateNeededForSelection(newElements)) {
			internalUpdateSelectedElements(newElements);
			requestMarkerUpdate();
		}
	}

	/**
	 * Return whether or not the list contains a resource that will require
	 * update.
	 * 
	 * @return boolean <code>true</code> if update is required.
	 */
	boolean updateNeededForSelection(Object[] newElements) {

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

			if (selectedResources.length == 0)
				return true; // We had nothing now we have something

			if (Arrays.equals(selectedResources, newElements))
				continue;

			if (scope == MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER) {
				Collection oldProjects = getProjectsAsCollection(selectedResources);
				Collection newProjects = getProjectsAsCollection(newElements);

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
	 * @return list of selected resources
	 */
	IResource[] getSelectedResources(){
		return selectedResources;
	}

	/**
	 * @return list of resource we want to collect markers for taking various
	 *         enabled filters into account.
	 */
	Collection getResourcesForBuild() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		Collection resourceSet = getResourcesFor(getEnabledFilters(), andFilters());
		// return common most parents
		if (andFilters()) {
			// Skip Anding the resources (optimization).
			//TODO: include the elaborate logic later ?
		}
		Set ressouceSetClone = new HashSet(resourceSet);
		Iterator cloneIterator = ressouceSetClone.iterator();
		while (cloneIterator.hasNext()) {
			IResource resource = (IResource) cloneIterator.next();
			Iterator iterator = resourceSet.iterator();
			while (iterator.hasNext()) {
				IResource resToRemove = (IResource) iterator.next();
				if(resource.equals(resToRemove)){
					continue;
				}
				if (resource.getFullPath()
						.isPrefixOf(resToRemove.getFullPath())) {
					iterator.remove();
				}
				if(resource.equals(root)){
					resourceSet.clear();
					resourceSet.add(root);
					return resourceSet;
				}
			}
		}
		return resourceSet;
	}

	/**
	 * @param enabledFilters
	 * @param andFilters
	 * @return
	 */
	private Collection getResourcesFor(Collection enabledFilters,
			boolean andFilters) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (enabledFilters.size() == 0) {
			List list = new ArrayList(1);
			list.add(root);
			return list;
		}
		Set resourceSet = new HashSet();
		Iterator filtersIterator = enabledFilters.iterator();
		while (filtersIterator.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) filtersIterator
					.next();
			switch (group.getScope()) {
			case MarkerFieldFilterGroup.ON_ANY: {
				resourceSet.add(root);
				break;
			}
			case MarkerFieldFilterGroup.ON_SELECTED_ONLY:
			case MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN: {
				for (int i = 0; i < selectedResources.length; i++) {
					resourceSet.add(selectedResources[i]);
				}
				break;
			}
			case MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER: {
				IResource[] resources = getProjects(selectedResources);
				for (int i = 0; i < resources.length; i++) {
					resourceSet.add(resources[i]);
				}
				break;
			}
			case MarkerFieldFilterGroup.ON_WORKING_SET: {
				group.refresh();
				IResource[] resources = group.getResourcesInWorkingSet();
				for (int i = 0; i < resources.length; i++) {
					resourceSet.add(resources[i]);
				}
				break;
			}
			}
			if (!andFilters && resourceSet.contains(root)) {
				List list = new ArrayList(1);
				list.add(root);
				return list;
			}
		}
		return resourceSet;
	}

	void dispose() {

	}

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

	/**
	 * Add the resources in resourceMapping to the resourceCollection
	 * 
	 * @param resourceCollection
	 * @param resourceMapping
	 */
	static void addResources(Collection resourceCollection,
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
			Policy.handle(e);
		}

	}
}
