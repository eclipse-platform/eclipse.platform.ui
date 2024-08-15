/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
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
 *     Enda O'Brien, Pilz Ireland - PR #144
 ******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;
import org.eclipse.ui.views.markers.MarkerField;

/**
 * ContentGeneratorDescriptor is the direct representation of the markerContentGenerator
 * extension point.
 *
 * @since 3.6
 */
public class ContentGeneratorDescriptor {

	private static final String ATTRIBUTE_DEFAULT_MARKER_GROUPING = "defaultMarkerGrouping"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VISIBLE = "visible"; //$NON-NLS-1$
	private static final String ELEMENT_MARKER_FIELD_CONFIGURATION = "markerFieldConfiguration"; //$NON-NLS-1$;
	private static final String MARKER_FIELD_REFERENCE = "markerFieldReference"; //$NON-NLS-1$

	private IConfigurationElement configurationElement;
	private MarkerField[] allFields;
	private MarkerField[] allFieldsWithExtensions;
	private Collection<MarkerType> markerTypes;
	private MarkerField[] initialVisible;
	private MarkerField[] initialVisibleWithExtensions;
	private Collection<MarkerGroup> groups;
	private Collection<IConfigurationElement> generatorExtensions = new ArrayList<>();
	private Map<String, MarkerType> allTypesTable;

	/**
	 * Create a new ContentGeneratorDescriptor
	 */
	public ContentGeneratorDescriptor(IConfigurationElement element) {
		configurationElement = element;
	}

	/**
	 * Add the groups defined in the receiver to the collection of groups.
	 */
	private void addDefinedGroups(Collection<MarkerGroup> groupss) {
		// Add the ones in the receiver.
		addGroupsFrom(configurationElement, groupss);
		// Add the extensions
		Iterator<IConfigurationElement> extensions = generatorExtensions.iterator();
		while (extensions.hasNext()) {
			addGroupsFrom(extensions.next(), groupss);
		}
	}

	/**
	 * Add the extensions to the receiver.
	 *
	 * @param extensions
	 *            Collection of {@link IConfigurationElement}
	 */
	public void addExtensions(Collection<IConfigurationElement> extensions) {
		generatorExtensions = extensions;
		clearCaches();
	}

	/**
	 * Add all of the markerGroups defined in element.
	 */
	private void addGroupsFrom(IConfigurationElement element, Collection<MarkerGroup> groupss) {
		for (IConfigurationElement grouping : element.getChildren(MarkerSupportRegistry.MARKER_GROUPING)) {
			groupss.add(MarkerGroup.createMarkerGroup(grouping));
		}
	}

	/**
	 * Return whether or not all of {@link MarkerTypesModel} are in the
	 * selectedTypes.
	 *
	 * @return boolean
	 */
	public boolean allTypesSelected(Collection<MarkerType> selectedTypes) {
		return selectedTypes.containsAll(getMarkerTypes());
	}

	/**
	 * Get the all of the fields that this content generator is using.
	 *
	 * @return {@link MarkerField}[]
	 */
	public MarkerField[] getAllFields() {
		if (allFieldsWithExtensions == null) {
			List<MarkerField> fields = new ArrayList<>();
			fields.addAll(Arrays.asList(allFields));
			getExtensionsDescriptorsStream().map(d -> Arrays.asList(d.getAllFields())).flatMap(Collection::stream)
					.forEach(fields::add);
			allFieldsWithExtensions = fields.toArray(MarkerField[]::new);
		}
		return allFieldsWithExtensions;
	}

	/**
	 * Get the category name from the receiver.
	 *
	 * @return categoryName
	 */
	public String getCategoryName() {
		return configurationElement.getAttribute(ATTRIBUTE_DEFAULT_MARKER_GROUPING);
	}

	/**
	 * Return the configuration elements for the receiver.
	 *
	 * @return IConfigurationElement[]
	 */
	public IConfigurationElement[] getFilterReferences() {
		IConfigurationElement[] filterGroups = configurationElement.getChildren(ELEMENT_MARKER_FIELD_CONFIGURATION);
		if (generatorExtensions.isEmpty()) {
			return filterGroups;
		}
		Iterator<IConfigurationElement> extensions = generatorExtensions.iterator();
		Collection<IConfigurationElement> extendedElements = new ArrayList<>();
		while (extensions.hasNext()) {
			IConfigurationElement extension = extensions.next();
			IConfigurationElement[] extensionFilters = extension.getChildren(ELEMENT_MARKER_FIELD_CONFIGURATION);
			extendedElements.addAll(Arrays.asList(extensionFilters));
		}
		if (extendedElements.size() > 0) {
			return Stream.concat(Arrays.stream(filterGroups), extendedElements.stream())
					.toArray(IConfigurationElement[]::new);
		}
		return filterGroups;
	}

	/**
	 * Return the id of the receiver.
	 *
	 * @return String
	 */
	public String getId() {
		return configurationElement.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID);
	}

	/**
	 * Get the list of initially visible fields
	 *
	 * @return {@link MarkerField}[]
	 */
	public MarkerField[] getInitialVisible() {
		if (initialVisibleWithExtensions == null) {
			List<MarkerField> fields = new ArrayList<>();
			fields.addAll(Arrays.asList(initialVisible));
			getExtensionsDescriptorsStream().map(d -> Arrays.asList(d.getInitialVisible())).flatMap(Collection::stream)
					.forEach(fields::add);
			initialVisibleWithExtensions = fields.toArray(MarkerField[]::new);
		}
		return initialVisibleWithExtensions;
	}

	/**
	 * Get the markerGroups associated with the receiver.
	 *
	 * @return Collection of {@link MarkerGroup}
	 */
	public Collection<MarkerGroup> getMarkerGroups() {
		if (groups == null) {
			groups = new TreeSet<>((mg1, mg2) -> mg1.getMarkerField().getName().compareTo(mg2.getMarkerField().getName()));

			// Add the groups defined in the receiver
			addDefinedGroups(groups);

			if (getId().equals(MarkerSupportRegistry.PROBLEMS_GENERATOR)) {
				// Add the groups that reference the receiver.
				groups.addAll(MarkerSupportRegistry.getInstance().getMarkerGroups());
			}
		}
		return groups;
	}

	/**
	 * Return the markerTypes for the receiver.
	 *
	 * @return Collection of {@link MarkerType}
	 */
	public Collection<MarkerType> getMarkerTypes() {
		if (markerTypes == null) {
			markerTypes = new HashSet<>();
			IConfigurationElement[] markerTypeElements = configurationElement.getChildren(MarkerSupportRegistry.MARKER_TYPE_REFERENCE);
			for (IConfigurationElement configElement : markerTypeElements) {
				String elementName = configElement.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID);

				String application = configElement.getAttribute(MarkerSupportInternalUtilities.APPLICATION) == null
						? MarkerSupportInternalUtilities.TYPE_AND_SUBTYPE
						: configElement.getAttribute(MarkerSupportInternalUtilities.APPLICATION);

				switch (application) {
				case MarkerSupportInternalUtilities.TYPE_ONLY:
					markerTypes.add(MarkerTypesModel.getInstance().getType(elementName));
					break;
				case MarkerSupportInternalUtilities.SUB_TYPES_ONLY:
					markerTypes.addAll(
							Arrays.asList(MarkerTypesModel.getInstance().getType(elementName).getAllSubTypes()));
					break;
				case MarkerSupportInternalUtilities.TYPE_AND_SUBTYPE:
				default:
					markerTypes.addAll(
							Arrays.asList(MarkerTypesModel.getInstance().getType(elementName).getAllSubTypes()));
					markerTypes.add(MarkerTypesModel.getInstance().getType(elementName));
				}
			}
			if (markerTypes.isEmpty()) {
				MarkerType[] types = MarkerTypesModel.getInstance().getType(IMarker.PROBLEM).getAllSubTypes();
				markerTypes.addAll(Arrays.asList(types));
			}
			getExtensionsDescriptorsStream().map(ContentGeneratorDescriptor::getMarkerTypes).flatMap(Collection::stream)
					.forEach(markerTypes::add);
		}
		return markerTypes;
	}

	/**
	 * Return the name for the receiver.
	 *
	 * @return String
	 */
	public String getName() {
		return configurationElement
				.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_NAME);
	}

	/**
	 * Return the type for typeId.
	 *
	 * @return {@link MarkerType} or <code>null</code> if it is not found.
	 */
	public MarkerType getType(String typeId) {
		return getTypesTable().get(typeId);
	}

	/**
	 * Get the table that maps type ids to markerTypes.
	 *
	 * @return Map of {@link String} to {@link MarkerType}
	 */
	public Map<String, MarkerType> getTypesTable() {
		if (allTypesTable == null) {
			allTypesTable = new HashMap<>();

			Iterator<MarkerType> allIterator = getMarkerTypes().iterator();
			while (allIterator.hasNext()) {
				MarkerType next = allIterator.next();
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

		IConfigurationElement[] elements = configurationElement.getChildren(MARKER_FIELD_REFERENCE);
		Collection<MarkerField> allFieldList = new ArrayList<>();
		Collection<MarkerField> initialVisibleList = new ArrayList<>();
		for (IConfigurationElement element : elements) {
			MarkerField field = registry.getField(element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID));
			if (field == null) {
				continue;
			}
			allFieldList.add(field);
			if (!MarkerSupportInternalUtilities.VALUE_FALSE.equals(element.getAttribute(ATTRIBUTE_VISIBLE))) {
				initialVisibleList.add(field);
			}
		}

		allFields = new MarkerField[allFieldList.size()];
		allFieldList.toArray(allFields);

		initialVisible = new MarkerField[initialVisibleList.size()];
		initialVisibleList.toArray(initialVisible);

	}

	/**
	 * Remove the element from the generator extensions
	 */
	public void removeExtension(IConfigurationElement element) {
		generatorExtensions.remove(element);
		clearCaches();
	}

	private void clearCaches() {
		allFieldsWithExtensions = null;
		initialVisibleWithExtensions = null;
		markerTypes = null;
		groups = null;
		allTypesTable = null;
	}

	private Stream<ContentGeneratorDescriptor> getExtensionsDescriptorsStream() {
		if (generatorExtensions != null) {
			MarkerSupportRegistry registry = MarkerSupportRegistry.getInstance();
			return generatorExtensions.stream()
					.map(extensionConfigElem -> extensionConfigElem
							.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID))
					.filter(id -> id != null && !id.isBlank())
					.map(contentGeneratorId -> registry.getContentGenDescriptor(contentGeneratorId))
					.filter(generator -> generator != null);
		}
		return Stream.empty();
	}

}
