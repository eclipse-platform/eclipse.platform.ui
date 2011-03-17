/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
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
	private static final String TAG_MARKER_LIMIT = "markerLimit"; //$NON-NLS-1$
	private static final String TAG_MARKER_LIMIT_ENABLED = "markerLimitEnabled"; //$NON-NLS-1$
	
	/*Use this to indicate filter change rather than a null*/
	private final Collection FILTERS_CHANGED = Collections.EMPTY_SET;

	//Carries the description for the generator, as coded in the given extension point
	private ContentGeneratorDescriptor generatorDescriptor;

	// fields
	private MarkerField[] visibleFields;

	// filters
	private Collection enabledFilters;
	private Collection filters;
	private boolean andFilters = false;
	private int markerLimits = 100;
	private boolean markerLimitsEnabled = true;

	/**
	 * focusResources
	 * 
	 */
	private IResource[] selectedResources = MarkerSupportInternalUtilities.EMPTY_RESOURCE_ARRAY;
	
	private Collection currentResources = Collections.EMPTY_SET;

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

	void saveState(IMemento memento, MarkerField[] displayedFields) {

		for (int i = 0; i < displayedFields.length; i++) {
			memento.createChild(TAG_COLUMN_VISIBILITY, displayedFields[i]
					.getConfigurationElement().getAttribute(
							MarkerSupportInternalUtilities.ATTRIBUTE_ID));
		}
	}

	void restoreState(IMemento memento) {

		initDefaults();

		if (memento == null) {
			return;
		}
		
		Integer limits = memento.getInteger(TAG_MARKER_LIMIT);
		if (limits != null) {
			markerLimits = limits.intValue();
		}

		Boolean limitsEnabled = memento.getBoolean(TAG_MARKER_LIMIT_ENABLED);
		if (limitsEnabled != null) {
			markerLimitsEnabled = limitsEnabled.booleanValue();
		}
		
		if (memento.getChildren(TAG_COLUMN_VISIBILITY).length != 0) {

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
	}

	private void initDefaults() {
		
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore();
		markerLimitsEnabled = store
				.getBoolean(IDEInternalPreferences.USE_MARKER_LIMITS);
		markerLimits = store.getInt(IDEInternalPreferences.MARKER_LIMITS_VALUE);

		MarkerField[] initialFields = getInitialVisible();

		visibleFields = new MarkerField[initialFields.length];
		System.arraycopy(initialFields, 0, visibleFields, 0,
				initialFields.length);

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
		if (filters == null || filters == FILTERS_CHANGED) {
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
		if (enabledFilters == null || enabledFilters == FILTERS_CHANGED) {
			Collection enabled = new HashSet();
			Iterator filtersIterator = getAllFilters().iterator();
			while (filtersIterator.hasNext()) {
				MarkerFieldFilterGroup next = (MarkerFieldFilterGroup) filtersIterator
						.next();
				if (next.isEnabled())
					enabled.add(next);
			}
			enabledFilters = enabled;
		}
		return enabledFilters;
	}

	/**
	 * Rebuild the list of filters
	 */
	protected void rebuildFilters() {
		filters = FILTERS_CHANGED;
		enabledFilters = FILTERS_CHANGED;
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
	 * @param andFilters
	 */
	void updateFilters(Collection filters, boolean andFilters) {
		setAndFilters(andFilters);
		this.filters = filters;
		enabledFilters = FILTERS_CHANGED;
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
	 * @return Returns the markerLimits.
	 */
	public int getMarkerLimits() {
		return markerLimits;
	}
	
	/**
	 * @param markerLimits The markerLimits to set.
	 */
	public void setMarkerLimits(int markerLimits) {
		this.markerLimits = markerLimits;
	}
	
	/**
	 * @return Returns the markerLimitsEnabled.
	 */
	public boolean isMarkerLimitsEnabled() {
		return markerLimitsEnabled;
	}
	
	/**
	 * @param markerLimitsEnabled The markerLimitsEnabled to set.
	 */
	public void setMarkerLimitsEnabled(boolean markerLimitsEnabled) {
		this.markerLimitsEnabled = markerLimitsEnabled;
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

	private void loadLimitSettings(IMemento memento) {
		
		if (memento == null)
			return;

		Integer limits = memento.getInteger(TAG_MARKER_LIMIT);
		if (limits != null) {
			markerLimits = limits.intValue();
		}

		Boolean limitsEnabled = memento.getBoolean(TAG_MARKER_LIMIT_ENABLED);
		if (limitsEnabled != null) {
			markerLimitsEnabled = limitsEnabled.booleanValue();
		}
		
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
			XMLMemento root = XMLMemento.createReadRoot(new StringReader(
					mementoString));
			loadLimitSettings(root);
			loadFilterSettings(root);
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

		writeLimitSettings(memento);
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

	private void writeLimitSettings(XMLMemento memento) {
		
		memento.putInteger(TAG_MARKER_LIMIT, markerLimits);
		memento.putBoolean(TAG_MARKER_LIMIT_ENABLED, markerLimitsEnabled);

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
		try {
			return select(entry, getSelectedResources(), getEnabledFilters(), andFilters());
		} finally {
			entry.clearCache();
		}
	}

	boolean select(MarkerEntry entry, IResource[] selResources,
			Collection enabledFilters, boolean andFilters) {
		if (enabledFilters.size() > 0) {
			Iterator filtersIterator = enabledFilters.iterator();
			if (andFilters) {
				while (filtersIterator.hasNext()) {
					MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) filtersIterator
							.next();
					if (!group.selectByScope(entry, selResources)
							|| !group.selectByFilters(entry)) {
						return false;
					}
				}
				return true;
			}

			while (filtersIterator.hasNext()) {
				MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) filtersIterator
						.next();
				if (group.selectByScope(entry, selResources)
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
				MarkerResourceUtil.addResources(resourceCollection,
						((ResourceMapping) elements[i]));
			}
		}
		IResource[] newSelection = new IResource[resourceCollection.size()];
		resourceCollection.toArray(newSelection);
		selectedResources = newSelection;
	}

	/**
	 * Update the receiver for a change in selection.
	 * 
	 * @param newElements
	 */
	void updateSelectedResource(Object[] newElements) {
		if (updateNeededForSelection(newElements)) {
			internalUpdateSelectedElements(newElements);
			// See comments below and Bug 296695
			// if (contentChanged()) {
			requestMarkerUpdate();
			// }
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
				Collection oldProjects = MarkerResourceUtil
						.getProjectsAsCollection(selectedResources);
				Collection newProjects = MarkerResourceUtil
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
	 * @return list of selected resources
	 */
	IResource[] getSelectedResources() {
		IResource[] selected=selectedResources;
		IResource[] resources = new IResource[selected.length];
		System.arraycopy(selected, 0, resources, 0,
				selected.length);
		return resources;
	}

	/**
	 * Note:As opposed to the previous scheme, the reason we gather markers only
	 * for the "effective"(ored/anded) resource collection is because collecting
	 * for individual filters and then adding them to a Set to remove duplicates
	 * is a lot more time-consuming than collecting only once,filtering once and
	 * adding to a list once.As a pre-filtering step, the
	 * MarkerFieldFilterGroup#selectByScope uses IPath comparison for selection,
	 * which happens real quickly.Also when filters are Anded we get a chance to
	 * gather only on resources that actually matter.And we get a tool to check
	 * at various places.
	 * 
	 * @return list of resource we want to collect markers for taking various
	 *         enabled filters into account.
	 * 
	 */
	Collection getResourcesForBuild() {
		currentResources = MarkerResourceUtil.computeResources(
				getSelectedResources(), getEnabledFilters(), andFilters());
		return currentResources;
	}
	
	/*
	 * See Bug 296695: This method is trickier than it may seem/appears to be.If
	 * it is ever desired to use this, it would need to be *RE-IMPLEMENTED* and
	 * would need a good amount of testing with various combination of filters
	 * and scopes. The key here is to understand and getting it right how filter
	 * scope and our trimmed, optimized resources and selected resource elements
	 * interact.
	 * Another possible way to check if content has changed is by
	 * comparing the markers gathered freshly with the previously gathered
	 * markers(cache them when an update is not canceled), whether this up to a
	 * visible limit, or completely, or selected filters we have to see. I am
	 * assuming that this takes little time to do. If this is done prior to
	 * sorting a good amount of time can be saved; we still save the UI time if
	 * checked after sorting. In the sorted case we can use a Binary search as
	 * well.Anyhow if this does take up time, we should skip this method.
	 */
//	/**
//	 * Change in markers itself is taken care of by the IResourceChangeListener,
//	 * We can think about change in the resource content when filters have
//	 * changed or selections have changed and the particular update we perform
//	 * manually is not required at all since nothing had changed.This is
//	 * particularly useful when a filter is set to 'On Selected element scope'.A
//	 * change in a filter is a combination of both its scope and other settings.
//	 *
//	 *
//	 * @return true if the resource-content has changed due to change in filter
//	 *         settings or selection. false if content has not change or an
//	 *         update has cleared the changes.
//	 */
//	boolean contentChanged() {
//		if (enabledFilters == null || enabledFilters == FILTERS_CHANGED) {
//			/*
//			 * TODO:Find a more narrowing way to check if active filters have
//			 * actually changed.Right now the update filter method set the
//			 * enabled filters to null. TODO: We should use a preference
//			 * listener for this We can 'optimally' use it for filter change
//			 * only on fixing the above.
//			 */
//			return true;
//		}
//		Collection current = MarkerResourceUtil.computeResources(
//				getSelectedResources(), getEnabledFilters(), andFilters());
//		Collection activeResources = currentResources;
//		if (current.size() != activeResources.size()) {
//			// changed
//			return true;
//		}
//		Iterator iterator = activeResources.iterator();
//		boolean needsUpdate = false;
//		while (!needsUpdate && iterator.hasNext()) {
//			Object object = iterator.next();
//			if (!current.contains(object)) {
//				needsUpdate = true;
//			}
//		}
//		return needsUpdate;
//	}

	/**
	 * Refresh gathered markers entries
	 * 
	 * @param monitor
	 */
	Collection generateMarkerEntries(IProgressMonitor monitor) {
		List result = new LinkedList();
		String[] typeIds = getTypes();
		boolean includeSubTypes = builder.includeMarkerSubTypes();
		boolean cancelled = gatherMarkers(typeIds, includeSubTypes, result,
				monitor);
		if (cancelled) {
			result.clear();
		}
		return result;
	}

	/**
	 * Refresh gathered markers entries
	 * @param result
	 * @param monitor
	 */
	boolean generateMarkerEntries(Collection result,IProgressMonitor monitor) {
		String[] typeIds = getTypes();
		boolean includeSubTypes = builder.includeMarkerSubTypes();
		return gatherMarkers(typeIds, includeSubTypes, result, monitor);
	}

	/**
	 * Gather markers into result.
	 * @param typeIds
	 * @param includeSubTypes
	 * @param result
	 * @param monitor
	 */
	boolean gatherMarkers(String[] typeIds, boolean includeSubTypes,
			Collection result, IProgressMonitor monitor) {
		try {
			Collection resources = getResourcesForBuild();
			if (includeSubTypes) {
				// Optimize and calculate super types
				String[] superTypes = MarkerResourceUtil
						.getMutuallyExclusiveSupersIds(typeIds);
				if (monitor.isCanceled()) {
					return false;
				}
				for (int i = 0; i < superTypes.length; i++) {
					boolean success = internalGatherMarkers(resources,superTypes[i],
							includeSubTypes, result, monitor);
					if (!success || monitor.isCanceled()) {
						return false;
					}
				}
			} else {
				for (int i = 0; i < typeIds.length; i++) {
					boolean success = internalGatherMarkers(resources,typeIds[i],
							includeSubTypes, result, monitor);
					if (!success || monitor.isCanceled()) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			//do not propagate but do show the error
			MarkerSupportInternalUtilities.showViewError(e);
			return false;
		} finally {
		}
		return true;
	}

	/**
	 * A helper to the
	 * {@link #gatherMarkers(String[], boolean, Collection, IProgressMonitor)}
	 * 
	 * @param resources
	 * @param typeId
	 * @param includeSubTypes
	 * @param result
	 * @param monitor
	 */
	private boolean internalGatherMarkers(Collection resources, String typeId,
			boolean includeSubTypes, Collection result, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return false;
		}
		IResource[] selected = getSelectedResources();
		Collection filters = getEnabledFilters();
		boolean andFilters = andFilters();
		Iterator iterator = resources.iterator();
		while (iterator.hasNext()) {
			IMarker[] markers = null;
			try {
				IResource resource = (IResource) iterator.next();
				if (!resource.isAccessible()) {
					continue;
				}
				markers = resource.findMarkers(typeId, includeSubTypes,
						IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				MarkerSupportInternalUtilities.logViewError(e);
			}
			if (markers == null) {
				continue;
			}
			if (monitor.isCanceled()) {
				return false;
			}
			MarkerEntry entry = null;
			int lenght =  markers.length;
			for (int i = 0; i < lenght; i++) {
				entry = new MarkerEntry(markers[i]);
				if (select(entry, selected, filters, andFilters)) {
					result.add(entry);
				}
				entry.clearCache();
				if (i % 500 == 0) {
					if (monitor.isCanceled()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	void dispose() {
		if (filterPreferenceListener != null) {
			IDEWorkbenchPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(filterPreferenceListener);
			filterPreferenceListener = null;
		}
	}
}
