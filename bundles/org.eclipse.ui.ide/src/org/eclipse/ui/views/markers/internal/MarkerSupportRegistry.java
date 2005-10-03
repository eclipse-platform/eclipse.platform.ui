package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

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

	private static final String ID = "id"; //$NON-NLS-1$

	private static final Object INFO = "INFO";//$NON-NLS-1$

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

	private static MarkerSupportRegistry singleton;

	private static final Object VIEW_MAPPING = "viewMapping";//$NON-NLS-1$

	private static final Object WARNING = "WARNING";//$NON-NLS-1$

	private static final String VIEW_ID = "viewId";//$NON-NLS-1$

	/**
	 * The ViewMapping is the marker view pairing represenation.
	 * 
	 * @since 3.1
	 * 
	 */
	private class ViewMapping {
		String markerId;

		private Collection viewIds;

		Collection definitions = new HashSet();

		/**
		 * Create a new instance of the receiver.
		 * 
		 * @param marker
		 */
		ViewMapping(String marker) {
			markerId = marker;
		}

		/**
		 * Get the ids of the views associated with the receiver.
		 * 
		 * @return Collection
		 */
		Collection getViewIds() {
			if (viewIds == null) {
				viewIds = new ArrayList();
				Iterator definitionIterator = definitions.iterator();
				while (definitionIterator.hasNext()) {
					ViewMappingDefinition element = (ViewMappingDefinition) definitionIterator
							.next();
					viewIds.add(element.viewId);
				}
			}
			return viewIds;
		}

		/**
		 * Remove the definition and clear the view ids.
		 * 
		 * @param definition
		 */
		void remove(ViewMappingDefinition definition) {
			definitions.remove(definition);
			viewIds = null;
		}

		/**
		 * Add the definition and clear the view ids.
		 * 
		 * @param definition
		 */
		void add(ViewMappingDefinition definition) {
			definitions.add(definition);
			viewIds = null;
		}
	}

	/**
	 * The ViewMappingDefinition is the defining object that can get added or
	 * removed with extension changes.
	 * 
	 * @since 3.1
	 * 
	 */
	private class ViewMappingDefinition {
		String markerId;

		String viewId;

		/**
		 * Create a new instance of the receiver.
		 * 
		 * @param marker
		 * @param view
		 *            id of the view.
		 */
		ViewMappingDefinition(String marker, String view) {
			markerId = marker;
			viewId = view;
		}
	}

	/**
	 * Get the instance of the registry.
	 * 
	 * @return ProblemFilterRegistry
	 */
	public static MarkerSupportRegistry getInstance() {
		if (singleton == null)
			singleton = new MarkerSupportRegistry();
		return singleton;
	}

	private Collection registeredFilters = new ArrayList();

	private Hashtable filterToViewMappings = new Hashtable();

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
			if (element.getName().equals(VIEW_MAPPING)) {
				ViewMappingDefinition definition = buildViewMapping(element);
				tracker.registerObject(extension, definition,
						IExtensionTracker.REF_STRONG);
				continue;
			}
		}
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
		return registeredFilters;
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

		filter.setId(element.getAttribute(ID));

		String enabledValue = element.getAttribute(ENABLED);
		filter.setEnabled(enabledValue == null
				|| Boolean.valueOf(enabledValue).booleanValue());

		int scopeValue = getScopeValue(element);
		if (scopeValue > 0) {
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
		filter.setSelectedTypes(selectedTypes);

		return filter;

	}

	/**
	 * Build all a view mappings from the extension.
	 * 
	 * @param element
	 *            the element to build the mapping from.
	 * @return ViewMappingDefinition
	 */
	private ViewMappingDefinition buildViewMapping(IConfigurationElement element) {
		String markerId = element.getAttribute(MARKER_ID);
		String viewId = element.getAttribute(VIEW_ID);

		ViewMappingDefinition definition = new ViewMappingDefinition(markerId,
				viewId);

		ViewMapping mapping;
		if (filterToViewMappings.containsKey(markerId)) {
			mapping = (ViewMapping) filterToViewMappings.get(markerId);
		} else {
			mapping = new ViewMapping(markerId);
			filterToViewMappings.put(markerId, mapping);
		}

		mapping.add(definition);

		return definition;

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
			if (objects[i] instanceof ViewMappingDefinition) {
				ViewMappingDefinition definition = (ViewMappingDefinition) objects[i];
				if (filterToViewMappings.containsKey(definition.markerId)) {
					((ViewMapping) filterToViewMappings
							.get(definition.markerId)).remove(definition);
				}
			}
		}

	}

	/**
	 * Return the views associated with markerId.
	 * @param markerId
	 * @return Collection
	 */
	public Collection getViews(String markerId) {
		if(filterToViewMappings.containsKey(markerId)){
			return ((ViewMapping) filterToViewMappings.get(markerId)).getViewIds();
		}
		return null;
			
	}

}
