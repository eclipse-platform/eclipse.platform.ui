/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
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
 */
public class MarkerContentGenerator {

	/**
	 * The IMemeto Tags
	 */
	private static final String TAG_COLUMN_VISIBILITY = "visible"; //$NON-NLS-1$
	private static final String TAG_FILTERS_SECTION = "filterGroups"; //$NON-NLS-1$
	private static final String TAG_GROUP_ENTRY = "filterGroup"; //$NON-NLS-1$
	private static final String TAG_AND = "andFilters"; //$NON-NLS-1$
	private static final String TAG_MARKER_LIMIT = "markerLimit"; //$NON-NLS-1$
	private static final String TAG_MARKER_LIMIT_ENABLED = "markerLimitEnabled"; //$NON-NLS-1$

	/*Use this to indicate filter change rather than a null*/
	private final Collection<MarkerFieldFilterGroup> FILTERS_CHANGED = Collections.emptySet();

	//Carries the description for the generator, as coded in the given extension point
	private ContentGeneratorDescriptor generatorDescriptor;

	// fields
	private MarkerField[] visibleFields;

	// filters
	private Collection<MarkerFieldFilterGroup> enabledFilters;
	private Collection<MarkerFieldFilterGroup> filters;
	private boolean andFilters;
	private int markerLimits = 100;
	private boolean markerLimitsEnabled = true;

	/**
	 * focusResources
	 */
	private IResource[] selectedResources = MarkerSupportInternalUtilities.EMPTY_RESOURCE_ARRAY;

	private Collection<IResource> currentResources = Collections.emptySet();

	private CachedMarkerBuilder builder;
	private String viewId;

	private IPropertyChangeListener filterPreferenceListener;
	private String initialDefaultCategoryName;

	/**
	 * Create a new MarkerContentGenerator
	 *
	 * @param viewId
	 * 				needed for backward compatibility
	 */
	public MarkerContentGenerator(ContentGeneratorDescriptor generatorDescriptor, CachedMarkerBuilder builder,
			String viewId) {
		this.generatorDescriptor = generatorDescriptor;
		this.viewId = viewId;
		setBuilder(builder);
	}

	/**
	 * Attach the generator to a builder
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
	 * @return boolean
	 */
	boolean allTypesSelected(Collection<MarkerType> selectedTypes) {
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
	 */
	void setVisibleFields(Collection<MarkerField> visible) {
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

		Collection<MarkerField> hidden = new HashSet<>();
		hidden.addAll(Arrays.asList(all));
		for (MarkerField element : visible) {
			hidden.remove(element);
		}
		return hidden.toArray();
	}

	void saveState(IMemento memento, MarkerField[] displayedFields) {
		for (MarkerField displayedField : displayedFields) {
			memento.createChild(TAG_COLUMN_VISIBILITY, displayedField
					.getConfigurationElement().getAttribute(
							MarkerSupportInternalUtilities.ATTRIBUTE_ID));
		}
	}

	void restoreState(IMemento memento) {
		initDefaults(memento);
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
			Collection<MarkerField> newVisible = new ArrayList<>();

			MarkerField[] all = getAllFields();
			Hashtable<String, MarkerField> allTable = new Hashtable<>();

			for (MarkerField element : all) {
				allTable.put(element.getConfigurationElement().getAttribute(
						MarkerSupportInternalUtilities.ATTRIBUTE_ID), element);
			}

			for (IMemento element : visible) {
				String key = element.getID();
				MarkerField field = allTable.get(key);
				if (field != null) {
					newVisible.add(field);
				}
			}

			visibleFields = new MarkerField[newVisible.size()];
			newVisible.toArray(visibleFields);
		}
	}

	private void initDefaults(IMemento memento) {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		markerLimitsEnabled = store.getBoolean(IDEInternalPreferences.USE_MARKER_LIMITS);
		markerLimits = store.getInt(IDEInternalPreferences.MARKER_LIMITS_VALUE);

		MarkerField[] initialFields = getInitialVisible();

		visibleFields = new MarkerField[initialFields.length];
		System.arraycopy(initialFields, 0, visibleFields, 0, initialFields.length);

		// On first startup, check if there is a custom group id set to be default
		if (memento == null) {
			initialDefaultCategoryName = store
					.getString(viewId + "." + IDEInternalPreferences.INITIAL_DEFAULT_MARKER_GROUPING); //$NON-NLS-1$
		}
	}

	/**
	 * Return a collection of all of the configuration fields for this generator
	 *
	 * @return Collection of {@link FilterConfigurationArea}
	 */
	Collection<FilterConfigurationArea> createFilterConfigurationFields() {
		Collection<FilterConfigurationArea> result = new ArrayList<>();
		for (MarkerField visibleField : visibleFields) {
			FilterConfigurationArea area = MarkerSupportInternalUtilities
					.generateFilterArea(visibleField);
			if (area != null)
				result.add(area);

		}
		return result;
	}

	/**
	 * Get the category name from the receiver.
	 */
	String getCategoryName() {
		if (initialDefaultCategoryName != null) {
			return initialDefaultCategoryName;
		}
		return generatorDescriptor.getCategoryName();
	}

	/**
	 * Return all of the filters for the receiver.
	 *
	 * @return Collection of MarkerFieldFilterGroup
	 */
	Collection<MarkerFieldFilterGroup> getAllFilters() {
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
	Collection<MarkerFieldFilterGroup> getEnabledFilters() {
		if (enabledFilters == null || enabledFilters == FILTERS_CHANGED) {
			Collection<MarkerFieldFilterGroup> enabled = new HashSet<>();
			Iterator<MarkerFieldFilterGroup> filtersIterator = getAllFilters().iterator();
			while (filtersIterator.hasNext()) {
				MarkerFieldFilterGroup next = filtersIterator.next();
				if (next.isEnabled()) {
					enabled.add(next);
				}
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
		Collection<MarkerFieldFilterGroup> allFilters = getEnabledFilters();
		Iterator<MarkerFieldFilterGroup> enabled = allFilters.iterator();
		while (enabled.hasNext()) {
			MarkerFieldFilterGroup group = enabled.next();
			group.setEnabled(false);
		}
		allFilters.clear();
		writeFiltersPreference();
		requestMarkerUpdate();
	}

	/**
	 * Add group to the enabled filters.
	 */
	void toggleFilter(MarkerFieldFilterGroup group) {
		Collection<MarkerFieldFilterGroup> enabled = getEnabledFilters();
		if (enabled.remove(group)) {
			group.setEnabled(false);
		} else {
			group.setEnabled(true);
			enabled.add(group);
		}
		writeFiltersPreference();
		requestMarkerUpdate();
	}

	/**
	 * Update the filters.
	 */
	void updateFilters(Collection<MarkerFieldFilterGroup> newFilters, boolean newAndFilters) {
		setAndFilters(newAndFilters);
		this.filters = newFilters;
		enabledFilters = FILTERS_CHANGED;
		writeFiltersPreference();
		requestMarkerUpdate();
	}

	/**
	 * Set whether the filters are being ANDed or ORed.
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
	Collection<MarkerFieldFilterGroup> getDeclaredFilters() {
		List<MarkerFieldFilterGroup> filterList = new ArrayList<>();
		IConfigurationElement[] filterReferences = generatorDescriptor.getFilterReferences();
		for (IConfigurationElement filterReference : filterReferences) {
			filterList.add(new MarkerFieldFilterGroup(filterReference, this));
		}

		// Honour the deprecated problemFilters
		if (viewId != null && viewId.equals(IPageLayout.ID_PROBLEM_VIEW)) {
			Iterator<ProblemFilter> problemFilters = MarkerSupportRegistry.getInstance()
					.getRegisteredFilters().iterator();
			while (problemFilters.hasNext()) {
				filterList.add(new CompatibilityMarkerFieldFilterGroup(problemFilters.next(), this));
			}
		}
		return filterList;
	}


	private void loadLimitSettings(IMemento memento) {
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
	}

	/**
	 * Load the settings from the memento.
	 */
	private void loadFilterSettings(IMemento memento) {
		if (memento == null) {
			return;
		}

		Boolean andValue = memento.getBoolean(TAG_AND);
		if (andValue != null) {
			setAndFilters(andValue.booleanValue());
		}
		IMemento children[] = memento.getChildren(TAG_GROUP_ENTRY);

		for (IMemento element : children) {
			IMemento child = element;
			String id = child.getString(IMemento.TAG_ID);
			if (id == null) {
				continue;
			}
			if (!loadGroupWithID(child, id)) {
				// Did not find a match must have been added by the user
				loadUserFilter(child);
			}
		}
	}

	/**
	 * Load the filters defined in memento string.
	 */
	private void loadFiltersFrom(String mementoString) {
		if (mementoString.equals(IPreferenceStore.STRING_DEFAULT_DEFAULT)) {
			return;
		}

		try {
			XMLMemento root = XMLMemento.createReadRoot(new StringReader(mementoString));
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
		loadFiltersFrom(IDEWorkbenchPlugin.getDefault().getPreferenceStore().getString(getMementoPreferenceName()));
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
	 * @return <code>true</code> if a matching group was found
	 */
	private boolean loadGroupWithID(IMemento child, String id) {
		Iterator<MarkerFieldFilterGroup> groups = getAllFilters().iterator();

		while (groups.hasNext()) {
			MarkerFieldFilterGroup group = groups.next();
			if (id.equals(group.getID())) {
				group.loadSettings(child);
				return true;
			}
		}
		return false;
	}


	/**
	 * Load the user supplied filter
	 */
	private void loadUserFilter(IMemento child) {
		MarkerFieldFilterGroup newGroup = new MarkerFieldFilterGroup(null, this);
		newGroup.loadSettings(child);
		getAllFilters().add(newGroup);
	}

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
			filterPreferenceListener = event -> {
				if (event.getProperty().equals(getMementoPreferenceName())) {
					rebuildFilters();
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
	 */
	private void writeFiltersSettings(XMLMemento memento) {
		memento.putBoolean(TAG_AND, andFilters());
		Iterator<MarkerFieldFilterGroup> groups = getAllFilters().iterator();
		while (groups.hasNext()) {
			MarkerFieldFilterGroup group = groups.next();
			IMemento child = memento.createChild(TAG_GROUP_ENTRY, group.getID());
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
	 * @return MarkerGroup or <code>null</code>
	 */
	MarkerGroup getMarkerGroup(String groupName) {
		Iterator<MarkerGroup> groups = getMarkerGroups().iterator();
		while (groups.hasNext()) {
			MarkerGroup group = groups.next();
			if (group.getId().equals(groupName)) {
				return group;
			}
		}
		return null;
	}

	/**
	 * Get the markerGroups associated with the receiver.
	 *
	 * @return Collection of {@link MarkerGroup}
	 */
	Collection<MarkerGroup> getMarkerGroups() {
		return generatorDescriptor.getMarkerGroups();
	}

	/**
	 * Return the markerTypes for the receiver.
	 *
	 * @return Collection of {@link MarkerType}
	 */
	public Collection<MarkerType> getMarkerTypes() {
		return generatorDescriptor.getMarkerTypes();
	}

	/**
	 * Return the markerTypes for the receiver.
	 *
	 * @return Array of type Ids
	 */
	public String[] getTypes() {
		Collection<MarkerType> types = getMarkerTypes();
		String[] ids = new String[types.size()];
		Iterator<MarkerType> iterator = types.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			ids[i++] = iterator.next().getId();
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

	static boolean select(MarkerEntry entry, IResource[] selResources,
			Collection<MarkerFieldFilterGroup> enabledFilters, boolean andFilters) {
		if (enabledFilters.size() > 0) {
			Iterator<MarkerFieldFilterGroup> filtersIterator = enabledFilters.iterator();
			if (andFilters) {
				while (filtersIterator.hasNext()) {
					MarkerFieldFilterGroup group = filtersIterator.next();
					if (!group.selectByScope(entry, selResources) || !group.selectByFilters(entry)) {
						return false;
					}
				}
				return true;
			}

			while (filtersIterator.hasNext()) {
				MarkerFieldFilterGroup group = filtersIterator.next();
				if (group.selectByScope(entry, selResources) && group.selectByFilters(entry)) {
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
	 */
	void internalUpdateSelectedElements(Object[] elements) {
		Collection<IResource> resourceCollection = new ArrayList<>();
		for (Object element : elements) {
			if (element instanceof IResource) {
				resourceCollection.add((IResource) element);
			} else {
				MarkerResourceUtil.addResources(resourceCollection, ((ResourceMapping) element));
			}
		}
		IResource[] newSelection = new IResource[resourceCollection.size()];
		resourceCollection.toArray(newSelection);
		selectedResources = newSelection;
	}

	/**
	 * Update the receiver for a change in selection.
	 *
	 * @param forceUpdate <code>true</code> if update must be done, <code>false</code> to only update when needed
	 */
	void updateSelectedResource(Object[] newElements, boolean forceUpdate) {
		if (forceUpdate || updateNeededForSelection(newElements)) {
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
		Iterator<MarkerFieldFilterGroup> enabled = getEnabledFilters().iterator();

		while (enabled.hasNext()) {
			MarkerFieldFilterGroup filter = enabled.next();

			int scope = filter.getScope();
			if (scope == MarkerFieldFilterGroup.ON_ANY || scope == MarkerFieldFilterGroup.ON_WORKING_SET) {
				continue;
			}

			if (newElements == null || newElements.length < 1) {
				continue;
			}

			if (selectedResources.length == 0) {
				return true; // We had nothing now we have something
			}

			if (Arrays.equals(selectedResources, newElements)) {
				continue;
			}

			if (scope == MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER) {
				Collection<IProject> oldProjects = MarkerResourceUtil.getProjectsAsCollection(selectedResources);
				Collection<IProject> newProjects = MarkerResourceUtil.getProjectsAsCollection(newElements);

				if (oldProjects.size() == newProjects.size() && newProjects.containsAll(oldProjects)) {
					continue;
				}
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
		IResource[] selected = selectedResources;
		IResource[] resources = new IResource[selected.length];
		System.arraycopy(selected, 0, resources, 0, selected.length);
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
	 */
	Collection<IResource> getResourcesForBuild() {
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
	 */
	Collection<MarkerEntry> generateMarkerEntries(IProgressMonitor monitor) {
		List<MarkerEntry> result = new LinkedList<>();
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
	 */
	boolean generateMarkerEntries(Collection<MarkerEntry> result,IProgressMonitor monitor) {
		String[] typeIds = getTypes();
		boolean includeSubTypes = builder.includeMarkerSubTypes();
		return gatherMarkers(typeIds, includeSubTypes, result, monitor);
	}

	/**
	 * Gather markers into result.
	 */
	boolean gatherMarkers(String[] typeIds, boolean includeSubTypes,
			Collection<MarkerEntry> result, IProgressMonitor monitor) {
		try {
			Collection<IResource> resources = getResourcesForBuild();
			if (includeSubTypes) {
				// Optimize and calculate super types
				String[] superTypes = MarkerResourceUtil.getMutuallyExclusiveSupersIds(typeIds);
				if (monitor.isCanceled()) {
					return false;
				}
				for (String superType : superTypes) {
					boolean success = internalGatherMarkers(resources, superType, includeSubTypes, result, monitor);
					if (!success || monitor.isCanceled()) {
						return false;
					}
				}
			} else {
				for (String typeId : typeIds) {
					boolean success = internalGatherMarkers(resources, typeId, includeSubTypes, result, monitor);
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
	 */
	private boolean internalGatherMarkers(Collection<IResource> resources, String typeId,
			boolean includeSubTypes, Collection<MarkerEntry> result, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return false;
		}
		IResource[] selected = getSelectedResources();
		Collection<MarkerFieldFilterGroup> enabled = getEnabledFilters();
		boolean filtersAreANDed = andFilters();
		Iterator<IResource> iterator = resources.iterator();
		while (iterator.hasNext()) {
			IMarker[] markers = null;
			try {
				IResource resource = iterator.next();
				if (!resource.isAccessible()) {
					continue;
				}
				markers = resource.findMarkers(typeId, includeSubTypes, IResource.DEPTH_INFINITE);
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
			int lenght = markers.length;
			for (int i = 0; i < lenght; i++) {
				entry = new MarkerEntry(markers[i]);
				if (select(entry, selected, enabled, filtersAreANDed)) {
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
