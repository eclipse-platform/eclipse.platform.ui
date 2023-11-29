/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;
import org.eclipse.ui.views.markers.MarkerField;

/**
 * The ProblemFilterRegistryReader is the registry reader for declarative
 * problem filters. See the org.eclipse.ui.markerSupport extension point.
 *
 * @since 3.2
 */
public class MarkerSupportRegistry implements IExtensionChangeHandler {

	private static final String DESCRIPTION = "onDescription"; //$NON-NLS-1$

	/**
	 * The enabled attribute.
	 */
	public static final String ENABLED = "enabled"; //$NON-NLS-1$

	private static final Object ERROR = "ERROR";//$NON-NLS-1$

	private static final Object INFO = "INFO";//$NON-NLS-1$

	private static final Object WARNING = "WARNING";//$NON-NLS-1$

	private static final String MARKER_ID = "markerId"; //$NON-NLS-1$

	/**
	 * Filter enablement : A zero/negative integer implies that the limit is
	 * disabled.
	 */
	public static final String FILTER_LIMIT = "filterLimit"; //$NON-NLS-1$

	/**
	 * The tag for the marker support extension
	 */
	public static final String MARKER_SUPPORT = "markerSupport";//$NON-NLS-1$

	private static final Object ON_ANY = "ON_ANY"; //$NON-NLS-1$

	private static final Object ON_ANY_IN_SAME_CONTAINER = "ON_ANY_IN_SAME_CONTAINER";//$NON-NLS-1$

	private static final Object ON_SELECTED_AND_CHILDREN = "ON_SELECTED_AND_CHILDREN";//$NON-NLS-1$

	private static final Object ON_SELECTED_ONLY = "ON_SELECTED_ONLY"; //$NON-NLS-1$

	private static final Object PROBLEM_FILTER = "problemFilter";//$NON-NLS-1$

	private static final String SCOPE = "scope"; //$NON-NLS-1$

	private static final String SELECTED_TYPE = "selectedType"; //$NON-NLS-1$

	private static final String SEVERITY = "severity";//$NON-NLS-1$

	/**
	 * The key for marker type references.
	 */
	public static final String MARKER_TYPE_REFERENCE = "markerTypeReference"; //$NON-NLS-1$

	private static final String MARKER_CATEGORY = "markerTypeCategory";//$NON-NLS-1$

	/**
	 * The markerAttributeMapping element.
	 */
	public static final String ATTRIBUTE_MAPPING = "markerAttributeMapping"; //$NON-NLS-1$

	/**
	 * The tag for marker grouping.
	 */
	public static final String MARKER_GROUPING = "markerGrouping"; //$NON-NLS-1$
	/**
	 * The value attribute.
	 */
	public static final String VALUE = "value"; //$NON-NLS-1$

	/**
	 * The label attribute
	 */
	public static final String LABEL = "label"; //$NON-NLS-1$

	/**
	 * The attribute grouping element name.
	 */
	public static final String MARKER_ATTRIBUTE_GROUPING = "markerAttributeGrouping";//$NON-NLS-1$

	/**
	 * The constant for grouping entries.
	 */
	public static final String MARKER_GROUPING_ENTRY = "markerGroupingEntry"; //$NON-NLS-1$

	private static final Object SEVERITY_ID = "org.eclipse.ui.ide.severity";//$NON-NLS-1$

	/**
	 * The tag for content generators.
	 */
	static final String MARKER_CONTENT_GENERATOR = "markerContentGenerator"; //$NON-NLS-1$

	/**
	 * The tag for content generator.
	 */
	private static final String MARKER_CONTENT_GENERATOR_EXTENSION = "markerContentGeneratorExtension"; //$NON-NLS-1$

	private static final String MARKER_FIELD = "markerField"; //$NON-NLS-1$

	private static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	/**
	 * The bookmarks generator.
	 */
	public static final String BOOKMARKS_GENERATOR = "org.eclipse.ui.ide.bookmarksGenerator"; //$NON-NLS-1$
	/**
	 * The tasks generator.
	 */
	public static final String TASKS_GENERATOR = "org.eclipse.ui.ide.tasksGenerator"; //$NON-NLS-1$

	/**
	 * The problems generator.
	 */
	public static final String PROBLEMS_GENERATOR = "org.eclipse.ui.ide.problemsGenerator"; //$NON-NLS-1$


	/**
	 * The all markers generator.
	 */
	public static final String ALL_MARKERS_GENERATOR = "org.eclipse.ui.ide.allMarkersGenerator"; //$NON-NLS-1$

	/**
	 * The id for the new markers view.
	 */
	public static final String MARKERS_ID = "org.eclipse.ui.ide.MarkersView"; //$NON-NLS-1$;

	private static final String ATTRIBUTE_GENERATOR_ID = "generatorId"; //$NON-NLS-1$

	private static MarkerSupportRegistry singleton;

	/**
	 * Get the instance of the registry.
	 *
	 * @return MarkerSupportRegistry
	 */
	public static synchronized MarkerSupportRegistry getInstance() {
		if (singleton == null) {
			singleton = new MarkerSupportRegistry();
		}
		return singleton;
	}

	private Map<String, ProblemFilter> registeredFilters = new HashMap<>();

	private Map<String, MarkerGroup> markerGroups = new HashMap<>();

	private Map<String, String> categories = new HashMap<>();

	private Map<String, TableComparator> hierarchyOrders = new HashMap<>();

	private MarkerType rootType;

	private Map<String, ContentGeneratorDescriptor> generators = new HashMap<>();

	private Map<String, MarkerField> fields = new HashMap<>();

	/**
	 * Create a new instance of the receiver and read the registry.
	 */
	private MarkerSupportRegistry() {
		IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(IDEWorkbenchPlugin.IDE_WORKBENCH,
				MARKER_SUPPORT);
		if (point == null) {
			return;
		}
		IExtension[] extensions = point.getExtensions();
		// initial population
		Map<String, Collection<MarkerGroupingEntry>> groupingEntries = new HashMap<>();
		Map<String, Collection<IConfigurationElement>> generatorExtensions = new HashMap<>();
		Map<String, MarkerGroupingEntry> entryIDsToEntries = new HashMap<>();
		Set<AttributeMarkerGrouping> attributeMappings = new HashSet<>();
		for (IExtension extension : extensions) {
			processExtension(tracker, extension, groupingEntries,
					entryIDsToEntries, attributeMappings, generatorExtensions);
		}
		postProcessExtensions(groupingEntries, entryIDsToEntries, attributeMappings, generatorExtensions);
		tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(point));
	}

	/**
	 * Process the extension and register the result with the tracker. Fill the
	 * map of groupingEntries and attribueMappings processed for post
	 * processing.
	 *
	 * @param groupIDsToEntries
	 *            Mapping of group names to the markerGroupingEntries registered
	 *            for them
	 * @param entryIDsToEntries
	 *            Mapping of entry ids to entries
	 * @param attributeMappings
	 *            the markerAttributeGroupings found
	 * @param generatorExtensions
	 *            the markerContentGenerator extensions keyed on group id
	 * @see #postProcessExtensions(Map, Map, Collection, Map)
	 */
	private void processExtension(IExtensionTracker tracker, IExtension extension,
			Map<String, Collection<MarkerGroupingEntry>> groupIDsToEntries,
			Map<String, MarkerGroupingEntry> entryIDsToEntries,
			Collection<AttributeMarkerGrouping> attributeMappings,
			Map<String, Collection<IConfigurationElement>> generatorExtensions) {
		IConfigurationElement[] elements = extension.getConfigurationElements();

		for (IConfigurationElement element : elements) {
			if (element.getName().equals(PROBLEM_FILTER)) {
				ProblemFilter filter = newFilter(element);
				registeredFilters.put(filter.getId(), filter);
				tracker.registerObject(extension, filter, IExtensionTracker.REF_STRONG);
				continue;
			}

			if (element.getName().equals(MARKER_GROUPING)) {
				MarkerGroup group = MarkerGroup.createMarkerGroup(element);
				markerGroups.put(group.getId(), group);
				tracker.registerObject(extension, group, IExtensionTracker.REF_STRONG);
				continue;
			}

			if (element.getName().equals(MARKER_GROUPING_ENTRY)) {
				MarkerGroupingEntry entry = new MarkerGroupingEntry(element);
				String groupName = element.getAttribute(MARKER_GROUPING);
				Collection<MarkerGroupingEntry> entries = groupIDsToEntries.get(groupName);
				if (entries == null) {
					entries = new HashSet<>();
				}

				entries.add(entry);
				groupIDsToEntries.put(groupName, entries);
				entryIDsToEntries.put(entry.getId(), entry);

				tracker.registerObject(extension, entry, IExtensionTracker.REF_STRONG);
				continue;
			}

			if (element.getName().equals(MARKER_ATTRIBUTE_GROUPING)) {
				AttributeMarkerGrouping grouping = new AttributeMarkerGrouping(element);
				attributeMappings.add(grouping);
				tracker.registerObject(extension, grouping, IExtensionTracker.REF_STRONG);
				continue;
			}

			if (element.getName().equals(MARKER_CATEGORY)) {
				String[] markerTypes = getMarkerTypes(element);
				String categoryName = element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_NAME);
				for (String markerType : markerTypes) {
					categories.put(markerType, categoryName);
				}
				tracker.registerObject(extension, categoryName, IExtensionTracker.REF_STRONG);
				continue;
			}

			if (element.getName().equals(MARKER_CONTENT_GENERATOR_EXTENSION)) {
				String generatorName = element.getAttribute(ATTRIBUTE_GENERATOR_ID);
				Collection<IConfigurationElement> extensionCollection = generatorExtensions.get(generatorName);
				if (extensionCollection == null) {
					extensionCollection = new ArrayList<>();
				}

				extensionCollection.add(element);
				generatorExtensions.put(generatorName, extensionCollection);
				tracker.registerObject(extension, element, IExtensionTracker.REF_STRONG);
				continue;
			}

			if (element.getName().equals(MARKER_CONTENT_GENERATOR)) {
				ContentGeneratorDescriptor generatorDesc = new ContentGeneratorDescriptor(element);
				generators.put(generatorDesc.getId(), generatorDesc);
				tracker.registerObject(extension, generatorDesc, IExtensionTracker.REF_STRONG);
				continue;
			}

			if (element.getName().equals(MARKER_FIELD)) {
				processMarkerField(tracker, extension, element);
				continue;
			}
		}
	}

	/**
	 * Create a table of MarkerFields
	 */
	private void processMarkerField(IExtensionTracker tracker, IExtension extension, IConfigurationElement element) {
		MarkerField field = null;
		try {
			field = (MarkerField) IDEWorkbenchPlugin.createExtension(element, ATTRIBUTE_CLASS);
			field.setConfigurationElement(element);
		} catch (CoreException e) {
			Policy.handle(e);
		}

		if (field != null) {
			fields.put(element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID), field);
		}
		tracker.registerObject(extension, field, IExtensionTracker.REF_STRONG);
	}

	/**
	 * Process the cross references after all of the extensions have been read.
	 *
	 * @param groupIDsToEntries
	 *            Mapping of group names to the markerGroupingEntries registered
	 *            for them
	 * @param entryIDsToEntries
	 *            Mapping of entry names to the mappings for them
	 * @param attributeMappings
	 *            the markerAttributeGroupings found
	 * @param generatorExtensions
	 *            map of generator id to generator descriptors
	 */
	private void postProcessExtensions(Map<String, Collection<MarkerGroupingEntry>> groupIDsToEntries,
			Map<String, MarkerGroupingEntry> entryIDsToEntries, Collection<AttributeMarkerGrouping> attributeMappings,
			Map<String, Collection<IConfigurationElement>> generatorExtensions) {
		processGroupingEntries(groupIDsToEntries);
		processAttributeMappings(entryIDsToEntries, attributeMappings);
		postProcessContentGenerators(generatorExtensions);
	}

	/**
	 * Set up the fields and filters
	 *
	 * @param generatorExtensions
	 *            the extensions to the generators,
	 */
	private void postProcessContentGenerators(Map<String, Collection<IConfigurationElement>> generatorExtensions) {
		Iterator<ContentGeneratorDescriptor> generatorIterator = generators.values().iterator();
		while (generatorIterator.hasNext()) {
			ContentGeneratorDescriptor generatorDesc = generatorIterator.next();
			generatorDesc.initializeFromConfigurationElement(this);
			Collection<IConfigurationElement> extensions = generatorExtensions.get(generatorDesc.getId());
			if (extensions != null) {
				generatorDesc.addExtensions(extensions);
			}
		}

	}

	/**
	 * Process the grouping entries into thier required grouping entries.
	 */
	private void processGroupingEntries(Map<String, Collection<MarkerGroupingEntry>> groupingEntries) {
		for (Entry<String, Collection<MarkerGroupingEntry>> entry : groupingEntries.entrySet()) {
			String nextGroupId = entry.getKey();
			MarkerGroup group = markerGroups.get(nextGroupId);
			if (group != null) {
				for (MarkerGroupingEntry markerGroupingEntry : entry.getValue()) {
					markerGroupingEntry.setGroup(group);
				}
			} else {
				for (MarkerGroupingEntry markerGroupingEntry : entry.getValue()) {
					IDEWorkbenchPlugin.log(NLS.bind("markerGroupingEntry {0} defines invalid group {1}", //$NON-NLS-1$
							new String[] { markerGroupingEntry.getId(), nextGroupId }));
				}
			}
		}
	}

	/**
	 * Process the attribute mappings into thier required grouping entries.
	 */
	private void processAttributeMappings(Map<String, MarkerGroupingEntry> entryIDsToEntries,
			Collection<AttributeMarkerGrouping> attributeMappings) {
		Iterator<AttributeMarkerGrouping> mappingsIterator = attributeMappings.iterator();
		while (mappingsIterator.hasNext()) {
			AttributeMarkerGrouping attributeGrouping = mappingsIterator.next();
			String defaultEntryId = attributeGrouping.getDefaultGroupingEntry();
			if (defaultEntryId != null) {
				MarkerGroupingEntry entry = entryIDsToEntries.get(defaultEntryId);
				if (entry != null) {
					entry.setAsDefault(attributeGrouping.getMarkerType());
				} else {
					IDEWorkbenchPlugin.log(NLS.bind(
							"Reference to invalid markerGroupingEntry {0}",//$NON-NLS-1$
							defaultEntryId));
				}
			}
			IConfigurationElement[] mappings = attributeGrouping.getElement().getChildren(ATTRIBUTE_MAPPING);

			for (IConfigurationElement mapping : mappings) {
				String entryId = mapping.getAttribute(MARKER_GROUPING_ENTRY);

				MarkerGroupingEntry entry = entryIDsToEntries.get(entryId);
				if (entry != null) {
					entry.getMarkerGroup().mapAttribute(attributeGrouping, entry, mapping.getAttribute(VALUE));
				} else {
					IDEWorkbenchPlugin.log(NLS.bind(
							"Reference to invaild markerGroupingEntry {0}", //$NON-NLS-1$
							defaultEntryId));
				}
			}
		}
	}

	/**
	 * Get the markerTypes defined in element.
	 *
	 * @return String[]
	 */
	private String[] getMarkerTypes(IConfigurationElement element) {
		IConfigurationElement[] types = element
				.getChildren(MARKER_TYPE_REFERENCE);
		String[] ids = new String[types.length];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = types[i].getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID);
		}
		return ids;
	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		Map<String, Collection<MarkerGroupingEntry>> groupIDsToEntries = new HashMap<>();
		Map<String, MarkerGroupingEntry> entryIDsToEntries = new HashMap<>();
		Map<String, Collection<IConfigurationElement>> generatorExtensions = new HashMap<>();
		Set<AttributeMarkerGrouping> attributeMappings = new HashSet<>();
		processExtension(tracker, extension, groupIDsToEntries,	entryIDsToEntries, attributeMappings, generatorExtensions);
		postProcessExtensions(groupIDsToEntries, entryIDsToEntries,	attributeMappings, generatorExtensions);
	}

	/**
	 * Get the collection of currently registered filters.
	 *
	 * @return Collection of ProblemFilter
	 */
	public Collection<ProblemFilter> getRegisteredFilters() {
		Collection<ProblemFilter> filteredFilters = new ArrayList<>();
		Iterator<ProblemFilter> registeredIterator = registeredFilters.values().iterator();
		while (registeredIterator.hasNext()) {
			ProblemFilter next = registeredIterator.next();
			if (next.isFilteredOutByActivity()) {
				continue;
			}
			filteredFilters.add(next);
		}
		return filteredFilters;
	}

	/**
	 * Get the constant for scope from element. Return -1 if there is no value.
	 *
	 * @return int one of MarkerView#ON_ANY MarkerView#ON_SELECTED_ONLY
	 *         MarkerView#ON_SELECTED_AND_CHILDREN
	 *         MarkerView#ON_ANY_IN_SAME_CONTAINER
	 */
	private int getScopeValue(IConfigurationElement element) {
		String scope = element.getAttribute(SCOPE);
		if (scope == null) {
			return -1;
		}
		if (scope.equals(ON_ANY)) {
			return MarkerFilter.ON_ANY;
		}
		if (scope.equals(ON_SELECTED_ONLY)) {
			return MarkerFilter.ON_SELECTED_ONLY;
		}
		if (scope.equals(ON_SELECTED_AND_CHILDREN)) {
			return MarkerFilter.ON_SELECTED_AND_CHILDREN;
		}
		if (scope.equals(ON_ANY_IN_SAME_CONTAINER)) {
			return MarkerFilter.ON_ANY_IN_SAME_CONTAINER;
		}
		return -1;
	}

	/**
	 * Get the constant for scope from element. Return -1 if there is no value.
	 *
	 * @return int one of MarkerView#ON_ANY MarkerView#ON_SELECTED_ONLY
	 *         MarkerView#ON_SELECTED_AND_CHILDREN
	 *         MarkerView#ON_ANY_IN_SAME_CONTAINER
	 */
	private int getSeverityValue(IConfigurationElement element) {
		String severity = element.getAttribute(SEVERITY);
		if (severity == null) {
			return -1;
		}
		if (severity.equals(INFO)) {
			return ProblemFilter.SEVERITY_INFO;
		}
		if (severity.equals(WARNING)) {
			return ProblemFilter.SEVERITY_WARNING;
		}
		if (severity.equals(ERROR)) {
			return ProblemFilter.SEVERITY_ERROR;
		}
		return -1;
	}

	/**
	 * Read the problem filters in the receiver.
	 *
	 * @param element
	 *            the filter element
	 * @return ProblemFilter
	 */
	private ProblemFilter newFilter(IConfigurationElement element) {
		ProblemFilter filter = new ProblemFilter(element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_NAME));

		filter.createContributionFrom(element);

		String enabledValue = element.getAttribute(ENABLED);
		filter.setEnabled(enabledValue == null || Boolean.parseBoolean(enabledValue));

		int scopeValue = getScopeValue(element);
		if (scopeValue >= 0) {
			filter.setOnResource(scopeValue);
		}

		String description = element.getAttribute(DESCRIPTION);
		if (description != null) {
			boolean contains = true;
			if (description.charAt(0) == '!') {// does not contain flag
				description = description.substring(1, description.length());
				contains = false;
			}
			filter.setContains(contains);
			filter.setDescription(description);
		}

		int severityValue = getSeverityValue(element);
		if (severityValue > 0) {
			filter.setSelectBySeverity(true);
			filter.setSeverity(severityValue);
		} else {
			filter.setSelectBySeverity(false);
		}

		List<MarkerType> selectedTypes = new ArrayList<>();
		for (IConfigurationElement type : element.getChildren(SELECTED_TYPE)) {
			String markerId = type.getAttribute(MARKER_ID);
			if (markerId != null) {
				MarkerType markerType = filter.getMarkerType(markerId);
				if (markerType == null) {
					IStatus status = new Status(IStatus.WARNING, IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.WARNING,
							NLS.bind(MarkerMessages.ProblemFilterRegistry_nullType,
									new Object[] { markerId, filter.getName() }),
							null);
					IDEWorkbenchPlugin.getDefault().getLog().log(status);
				} else {
					selectedTypes.add(markerType);
				}
			}
		}

		if (selectedTypes.size() > 0) {
			// specified
			filter.setSelectedTypes(selectedTypes);
		}
		return filter;
	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {

		for (Object object : objects) {
			if (object instanceof ProblemFilter) {
				registeredFilters.remove(object);
				continue;
			}

			if (object instanceof MarkerGroup) {
				markerGroups.remove(((MarkerGroup) object).getId());
				continue;
			}

			if (object instanceof MarkerGroupingEntry) {
				MarkerGroupingEntry entry = (MarkerGroupingEntry) object;
				entry.getMarkerGroup().remove(entry);
				continue;
			}

			if (object instanceof AttributeMarkerGrouping) {
				AttributeMarkerGrouping entry = (AttributeMarkerGrouping) object;
				entry.unmap();
				continue;
			}

			if (object instanceof String) {
				removeValues((String) object, categories);
				continue;
			}

			if (object instanceof MarkerField) {
				fields.remove(MarkerSupportInternalUtilities.getId((MarkerField) object));
				continue;
			}

			if (object instanceof ContentGeneratorDescriptor) {
				generators.remove(((ContentGeneratorDescriptor) object).getId());
				continue;
			}

			if (object instanceof IConfigurationElement) {
				IConfigurationElement element = (IConfigurationElement) object;
				ContentGeneratorDescriptor generatorDesc = generators.get(element.getAttribute(ATTRIBUTE_GENERATOR_ID));
				generatorDesc.removeExtension(element);
				continue;
			}
		}
	}

	/**
	 * Remove the value from all of the collection sets in cache. If the
	 * collection is empty remove the key as well.
	 */
	private void removeValues(String value, Map<String, String> cache) {
		Collection<String> keysToRemove = new ArrayList<>();
		for (Entry<String, String> entry : cache.entrySet()) {
			if (entry.getValue().equals(value)) {
				keysToRemove.add(entry.getKey());
			}
		}
		for (String toRemove : keysToRemove) {
			cache.remove(toRemove);
		}
	}

	/**
	 * Get the category associated with marker. Return <code>null</code> if
	 * there are none.
	 *
	 * @return String or <code>null</code>
	 */
	public String getCategory(IMarker marker) {
		try {
			return getCategory(marker.getType());
		} catch (CoreException e) {
			Policy.handle(e);
		}
		return null;
	}

	/**
	 * Get the category associated with markerType. Return <code>null</code>
	 * if there are none.
	 *
	 * @return String or <code>null</code>
	 */
	public String getCategory(String markerType) {
		return categories.get(markerType);
	}

	/**
	 * Return the TableSorter that corresponds to type.
	 *
	 * @return TableSorter
	 */
	public TableComparator getSorterFor(String type) {
		TableComparator sorter = hierarchyOrders.get(type);
		if (sorter != null) {
			return sorter;
		}

		sorter = findSorterInChildren(type, getRootType());
		if (sorter == null) {
			return new TableComparator(new IField[0], new int[0], new int[0]);
		}
		return sorter;
	}

	/**
	 * Return the list of root marker types.
	 *
	 * @return List of MarkerType.
	 */
	private MarkerType getRootType() {
		if (rootType == null) {
			rootType = (MarkerTypesModel.getInstance()).getType(IMarker.PROBLEM);
		}
		return rootType;
	}

	/**
	 * Find the best match sorter for typeName in the children. If it cannot be
	 * found then return <code>null</code>.
	 *
	 * @return TableSorter or <code>null</code>.
	 */
	private TableComparator findSorterInChildren(String typeName, MarkerType type) {
		for (MarkerType markerSubType : type.getAllSubTypes()) {
			MarkerType[] subtypes = markerSubType.getAllSubTypes();
			for (MarkerType subtype : subtypes) {
				TableComparator sorter = findSorterInChildren(typeName, subtype);
				if (sorter != null) {
					return sorter;
				}
			}
		}
		return hierarchyOrders.get(type.getId());
	}

	/**
	 * Return the FieldMarkerGroups in the receiver.
	 *
	 * @return Collection of {@link MarkerGroup}
	 */
	public Collection<MarkerGroup> getMarkerGroups() {
		return markerGroups.values();
	}

	/**
	 * Return the default groupfield.
	 *
	 * @return IField
	 */
	IField getDefaultGroupField() {
		return markerGroups.get(SEVERITY_ID).getField();
	}

	/**
	 * Get the generator descriptor for id
	 *
	 * @return ContentGeneratorDescriptor or <code>null</code>.
	 */
	public ContentGeneratorDescriptor getContentGenDescriptor (String id) {
		if (id != null) {
			return generators.get(id);
		}
		return null;
	}

	/**
	 * Return the default content generator descriptor.
	 *
	 * @return ContentGeneratorDescriptor
	 */
	public ContentGeneratorDescriptor getDefaultContentGenDescriptor () {
		return generators.get(PROBLEMS_GENERATOR);
	}

	/**
	 * Get the markerGroup associated with categoryName
	 *
	 * @return FieldMarkerGroup or <code>null</code>
	 */
	public MarkerGroup getMarkerGroup(String categoryName) {
		return markerGroups.get(categoryName);
	}

	/**
	 * Return the field that maps to id.
	 *
	 * @return {@link MarkerField} or <code>null</code>
	 */
	public MarkerField getField(String id) {
		return fields.get(id);
	}

}
