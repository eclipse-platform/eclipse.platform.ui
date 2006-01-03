package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * The ProblemFilterRegistryReader is the registry reader for declarative
 * problem filters. See the org.eclipse.ui.markerSupport extension point.
 * 
 * @since 3.2
 * 
 */
public class MarkerSupportRegistry implements IExtensionChangeHandler {

	private static final String DESCRIPTION = "onDescription"; //$NON-NLS-1$

	private static final String ENABLED = "enabled"; //$NON-NLS-1$

	private static final Object ERROR = "ERROR";//$NON-NLS-1$

	static final String ID = "id"; //$NON-NLS-1$

	private static final Object INFO = "INFO";//$NON-NLS-1$

	private static final Object WARNING = "WARNING";//$NON-NLS-1$

	private static final String MARKER_ID = "markerId"; //$NON-NLS-1$

	/**
	 * The tag for the marker support extension
	 */
	public static final String MARKER_SUPPORT = "markerSupport";//$NON-NLS-1$

	private static final String NAME = "name"; //$NON-NLS-1$

	private static final Object ON_ANY = "ON_ANY"; //$NON-NLS-1$

	private static final Object ON_ANY_IN_SAME_CONTAINER = "ON_ANY_IN_SAME_CONTAINER";//$NON-NLS-1$

	private static final Object ON_SELECTED_AND_CHILDREN = "ON_SELECTED_AND_CHILDREN";//$NON-NLS-1$

	private static final Object ON_SELECTED_ONLY = "ON_SELECTED_ONLY"; //$NON-NLS-1$

	private static final Object PROBLEM_FILTER = "problemFilter";//$NON-NLS-1$

	private static final String SCOPE = "scope"; //$NON-NLS-1$

	private static final String SELECTED_TYPE = "selectedType"; //$NON-NLS-1$

	private static final String SEVERITY = "severity";//$NON-NLS-1$

	private static final Object ATTRIBUTE_CATEGORY_REFERENCE = "markerAttributeReference"; //$NON-NLS-1$

	private static final String MARKER_TYPE_REFERENCE = "markerTypeReference"; //$NON-NLS-1$

	private static final Object MARKER_CATEGORY = "markerCategory";//$NON-NLS-1$

	private static final Object HIERARCHY = "hierarchy";//$NON-NLS-1$

	private static final String HIERARCHY_REFERENCE = "hierarchyReference";//$NON-NLS-1$

	private static final String TYPE = "type";//$NON-NLS-1$

	private static final String PATH = "PATH";//$NON-NLS-1$

	private static final String MESSAGE = "MESSAGE";//$NON-NLS-1$

	private static final String RESOURCE = "RESOURCE";//$NON-NLS-1$

	private static final String SEVERITY_HIERARCHY = "SEVERITY";//$NON-NLS-1$

	private static final String DIRECTION = "direction";//$NON-NLS-1$

	private static final String ASCENDING = "ASCENDING"; //$NON-NLS-1$

	private static final String ATTRIBUTE_MAPPING = "markerAttributeMapping"; //$NON-NLS-1$

	private static final String ATTRIBUTE = "attribute"; //$NON-NLS-1$

	private static final String VALUE = "value"; //$NON-NLS-1$

	private static MarkerSupportRegistry singleton;

	// Create a lock so that initiization happens in one thread
	private static Object creationLock = new Object();

	/**
	 * Get the instance of the registry.
	 * 
	 * @return ProblemFilterRegistry
	 */
	public static MarkerSupportRegistry getInstance() {
		if (singleton == null) {
			synchronized (creationLock) {
				if (singleton == null)// May have been created by blocking
					// thread
					singleton = new MarkerSupportRegistry();
			}
		}
		return singleton;
	}

	private Collection registeredFilters = new ArrayList();

	private HashMap attributeCategoryProviders = new HashMap();

	private HashMap categories = new HashMap();

	private HashMap hierarchyOrders = new HashMap();

	private MarkerType rootType;

	/**
	 * Create a new instance of the receiver and read the registry.
	 */
	private MarkerSupportRegistry() {
		IExtensionTracker tracker = PlatformUI.getWorkbench()
				.getExtensionTracker();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(IDEWorkbenchPlugin.IDE_WORKBENCH,
						MARKER_SUPPORT);
		if (point == null)
			return;
		IExtension[] extensions = point.getExtensions();
		// initial population
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			processExtension(tracker, extension);
		}
		tracker.registerHandler(this, ExtensionTracker
				.createExtensionPointFilter(point));

	}

	/**
	 * Process the extension and register the result with the tracker.
	 * 
	 * @param tracker
	 * @param extension
	 */
	private void processExtension(IExtensionTracker tracker,
			IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for (int j = 0; j < elements.length; j++) {
			IConfigurationElement element = elements[j];
			if (element.getName().equals(PROBLEM_FILTER)) {
				ProblemFilter filter = newFilter(element);
				registeredFilters.add(filter);
				tracker.registerObject(extension, filter,
						IExtensionTracker.REF_STRONG);

				continue;
			}
			if (element.getName().equals(ATTRIBUTE_CATEGORY_REFERENCE)) {

				String[] markerTypes = getMarkerTypes(element);
				AttributeCategoryProvider provider = getProvider(element);

				if (provider != null) {
					for (int i = 0; i < markerTypes.length; i++) {
						String markerType = markerTypes[i];
						Collection providers;
						if (attributeCategoryProviders.containsKey(markerType))
							providers = (Collection) attributeCategoryProviders
									.get(markerType);
						else
							providers = new ArrayList();
						providers.add(provider);
						attributeCategoryProviders.put(markerType, providers);
						tracker.registerObject(extension, provider,
								IExtensionTracker.REF_STRONG);
					}

				}
			}

			if (element.getName().equals(MARKER_CATEGORY)) {

				String[] markerTypes = getMarkerTypes(element);
				String categoryName = element.getAttribute(NAME);

				for (int i = 0; i < markerTypes.length; i++) {
					categories.put(markerTypes[i], categoryName);

				}
				tracker.registerObject(extension, categoryName,
						IExtensionTracker.REF_STRONG);
			}

			if (element.getName().equals(HIERARCHY)) {

				String markerType = getMarkerTypes(element)[0];

				IConfigurationElement[] types = element
						.getChildren(HIERARCHY_REFERENCE);
				IField[] properties = new IField[types.length];
				int[] directions = new int[types.length];
				for (int i = 0; i < types.length; i++) {
					properties[i] = getFieldFor(types[i].getAttribute(TYPE));
					String direction = types[i].getAttribute(DIRECTION);
					if (direction == ASCENDING)
						directions[i] = TableSorter.ASCENDING;
					else
						directions[i] = TableSorter.DESCENDING;
				}

				int[] priorities = new int[properties.length];
				for (int i = 0; i < priorities.length; i++) {
					priorities[i] = i;
				}

				TableSorter sorter = new TableSorter(properties, priorities,
						directions);
				hierarchyOrders.put(markerType, sorter);
				tracker.registerObject(extension, sorter,
						IExtensionTracker.REF_STRONG);

			}
		}
	}

	/**
	 * Return the field that matches attribute
	 * 
	 * @param attribute
	 * @return IField or <code>null</code> if there is no matching field.
	 */
	private IField getFieldFor(String attribute) {
		if (attribute.equals(PATH))
			return new FieldFolder();
		if (attribute.equals(MESSAGE))
			return new FieldMessage();
		if (attribute.equals(SEVERITY_HIERARCHY))
			return new FieldSeverity();
		if (attribute.equals(RESOURCE))
			return new FieldResource();

		return null;

	}

	/**
	 * Get the markerTypes defined in element.
	 * 
	 * @param element
	 * @return String[]
	 */
	private String[] getMarkerTypes(IConfigurationElement element) {
		IConfigurationElement[] types = element
				.getChildren(MARKER_TYPE_REFERENCE);
		String[] ids = new String[types.length];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = types[i].getAttribute(ID);
		}
		return ids;
	}

	/**
	 * Create an ICategoryProvider from element.
	 * 
	 * @param element
	 * @return ICategoryProvider
	 */
	private AttributeCategoryProvider getProvider(
			final IConfigurationElement element) {

		IConfigurationElement[] mappings = element
				.getChildren(ATTRIBUTE_MAPPING);

		AttributeCategoryProvider provider = new AttributeCategoryProvider(
				element.getAttribute(ATTRIBUTE));
		for (int i = 0; i < mappings.length; i++) {
			IConfigurationElement mappingElement = mappings[i];
			provider.put(mappingElement.getAttribute(VALUE), mappingElement
					.getAttribute(NAME));
		}

		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker,
	 *      org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		processExtension(tracker, extension);
	}

	/**
	 * Get the collection of currently registered filters.
	 * 
	 * @return Collection of ProblemFilter
	 */
	public Collection getRegisteredFilters() {
		Collection filteredFilters = new ArrayList();
		Iterator registeredIterator = registeredFilters.iterator();
		while (registeredIterator.hasNext()) {
			ProblemFilter next = (ProblemFilter) registeredIterator.next();
			if (next.isFilteredOutByActivity())
				continue;
			filteredFilters.add(next);
		}

		return filteredFilters;
	}

	/**
	 * Get the constant for scope from element. Return -1 if there is no value.
	 * 
	 * @param element
	 * @return int one of MarkerView#ON_ANY MarkerView#ON_SELECTED_ONLY
	 *         MarkerView#ON_SELECTED_AND_CHILDREN
	 *         MarkerView#ON_ANY_IN_SAME_CONTAINER
	 */
	private int getScopeValue(IConfigurationElement element) {
		String scope = element.getAttribute(SCOPE);
		if (scope == null)
			return -1;
		if (scope.equals(ON_ANY))
			return MarkerFilter.ON_ANY;
		if (scope.equals(ON_SELECTED_ONLY))
			return MarkerFilter.ON_SELECTED_ONLY;
		if (scope.equals(ON_SELECTED_AND_CHILDREN))
			return MarkerFilter.ON_SELECTED_AND_CHILDREN;
		if (scope.equals(ON_ANY_IN_SAME_CONTAINER))
			return MarkerFilter.ON_ANY_IN_SAME_CONTAINER;

		return -1;
	}

	/**
	 * Get the constant for scope from element. Return -1 if there is no value.
	 * 
	 * @param element
	 * @return int one of MarkerView#ON_ANY MarkerView#ON_SELECTED_ONLY
	 *         MarkerView#ON_SELECTED_AND_CHILDREN
	 *         MarkerView#ON_ANY_IN_SAME_CONTAINER
	 */
	private int getSeverityValue(IConfigurationElement element) {
		String severity = element.getAttribute(SEVERITY);
		if (severity == null)
			return -1;
		if (severity.equals(INFO))
			return ProblemFilter.SEVERITY_INFO;
		if (severity.equals(WARNING))
			return ProblemFilter.SEVERITY_WARNING;
		if (severity.equals(ERROR))
			return ProblemFilter.SEVERITY_ERROR;

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
		ProblemFilter filter = new ProblemFilter(element.getAttribute(NAME));

		filter.createContributionFrom(element);

		String enabledValue = element.getAttribute(ENABLED);
		filter.setEnabled(enabledValue == null
				|| Boolean.valueOf(enabledValue).booleanValue());

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
		} else
			filter.setSelectBySeverity(false);

		List selectedTypes = new ArrayList();
		IConfigurationElement[] types = element.getChildren(SELECTED_TYPE);
		for (int j = 0; j < types.length; j++) {
			String markerId = types[j].getAttribute(MARKER_ID);
			if (markerId != null) {
				MarkerType type = filter.getMarkerType(markerId);
				if (type == null) {
					IStatus status = new Status(IStatus.WARNING,
							IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.WARNING,
							MarkerMessages.ProblemFilterRegistry_nullType, null);
					IDEWorkbenchPlugin.getDefault().getLog().log(status);
				} else
					selectedTypes.add(type);
			}
		}

		if (selectedTypes.size() > 0) // Only set the types if there are any
			// specified
			filter.setSelectedTypes(selectedTypes);

		return filter;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension,
	 *      java.lang.Object[])
	 */
	public void removeExtension(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof ProblemFilter)
				registeredFilters.remove(objects[i]);

			if (objects[i] instanceof AttributeCategoryProvider) {
				removeValues(objects[i], attributeCategoryProviders);
			}

			if (objects[i] instanceof String) {
				removeValues(objects[i], categories);
			}

			if (objects[i] instanceof TableSorter) {
				removeValues(objects[i], hierarchyOrders);
			}

		}

	}

	/**
	 * Remove the value from all of the collection sets in cache. If the
	 * collection is empty remove the key as well.
	 * 
	 * @param value
	 * @param cache
	 */
	private void removeValues(Object value, HashMap cache) {
		Collection keysToRemove = new ArrayList();
		Iterator keys = cache.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object next = cache.get(key);
			if (next instanceof Collection) {
				Collection collection = (Collection) next;
				if (collection.contains(value)) {
					collection.remove(value);
					if (collection.isEmpty())
						keysToRemove.add(key);
					break;
				}
			} else {
				if (cache.get(key).equals(value))
					keysToRemove.add(key);
			}
		}
		Iterator keysToRemoveIterator = keysToRemove.iterator();
		while (keysToRemoveIterator.hasNext()) {
			cache.remove(keysToRemoveIterator.next());
		}
	}

	/**
	 * Get the AttributeCategoryProvider associated with marker. Return
	 * <code>null</code> if there are none.
	 * 
	 * @param marker
	 * @return ICategoryProvider[] or <code>null</code>
	 */
	public AttributeCategoryProvider[] getAttributeCategoryProviders(
			IMarker marker) {
		try {
			return getAttributeCategoryProviders(marker.getType());
		} catch (CoreException e) {
			Util.log(e);
			return null;
		}

	}

	/**
	 * Return the AttributeCategoryProvider for markers of type marker type.
	 * 
	 * @param markerType
	 * @return ISubCategoryProvider[] or <code>null</code>
	 */
	public AttributeCategoryProvider[] getAttributeCategoryProviders(String markerType) {
		Object providers;
		providers = attributeCategoryProviders.get(markerType);

		if (providers == null)
			return null;
		Collection providerCollection = (Collection) providers;
		AttributeCategoryProvider[] providerArray = new AttributeCategoryProvider[providerCollection
				.size()];
		providerCollection.toArray(providerArray);
		return providerArray;
	}

	/**
	 * Get the category associated with marker. Return <code>null</code> if
	 * there are none.
	 * 
	 * @param marker
	 * @return String or <code>null</code>
	 */
	public String getCategory(IMarker marker) {
		try {
			return getCategory(marker.getType());
		} catch (CoreException e) {
			Util.log(e);
		}
		return null;
	}

	/**
	 * Get the category associated with markerType. Return <code>null</code>
	 * if there are none.
	 * 
	 * @param markerType
	 * @return String or <code>null</code>
	 */
	public String getCategory(String markerType) {
		if (categories.containsKey(markerType))
			return (String) categories.get(markerType);
		return null;
	}

	/**
	 * Return the TableSorter that corresponds to type.
	 * 
	 * @param type
	 * @return TableSorter
	 */
	public TableSorter getSorterFor(String type) {
		if (hierarchyOrders.containsKey(type))
			return (TableSorter) hierarchyOrders.get(type);

		TableSorter sorter = findSorterInChildren(type, getRootType());
		if (sorter == null)
			return new TableSorter(new IField[0], new int[0], new int[0]);
		return sorter;
	}

	/**
	 * Return the list of root marker types.
	 * 
	 * @return List of MarkerType.
	 */
	private MarkerType getRootType() {
		if (rootType == null) {
			rootType = (MarkerTypesModel.getInstance())
					.getType(IMarker.PROBLEM);
		}
		return rootType;
	}

	/**
	 * Find the best match sorter for typeName in the children. If it cannot be
	 * found then return <code>null</code>.
	 * 
	 * @param typeName
	 * @param type
	 * @return TableSorter or <code>null</code>.
	 */
	private TableSorter findSorterInChildren(String typeName, MarkerType type) {

		MarkerType[] types = type.getAllSubTypes();
		TableSorter defaultSorter = null;
		if (hierarchyOrders.containsKey(type.getId()))
			defaultSorter = (TableSorter) hierarchyOrders.get(type.getId());

		for (int i = 0; i < types.length; i++) {
			MarkerType[] subtypes = types[i].getAllSubTypes();
			for (int j = 0; j < subtypes.length; j++) {
				TableSorter sorter = findSorterInChildren(typeName, subtypes[j]);
				if (sorter != null)
					return sorter;
			}
		}
		return defaultSorter;

	}

}
