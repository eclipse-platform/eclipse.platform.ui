/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;
import org.eclipse.ui.views.markers.MarkerField;

/**
 * ContentGeneratorDescriptor is the direct representation of the markerContentGenerator
 * extension point.
 * 
 * @since 3.6
 * 
 */
public class ContentGeneratorDescriptor {

	private static final String ATTRIBUTE_DEFAULT_MARKER_GROUPING = "defaultMarkerGrouping"; //$NON-NLS-1$
	private static final String ATTRIBUTE_VISIBLE = "visible"; //$NON-NLS-1$
	private static final String ELEMENT_MARKER_FIELD_CONFIGURATION = "markerFieldConfiguration"; //$NON-NLS-1$;
	private static final String MARKER_FIELD_REFERENCE = "markerFieldReference"; //$NON-NLS-1$

	private IConfigurationElement configurationElement;
	private MarkerField[] allFields;
	private Collection markerTypes;
	private MarkerField[] initialVisible;
	private Collection groups;
	private Collection generatorExtensions = new ArrayList();
	private Map allTypesTable;

	/**
	 * Create a new ContentGeneratorDescriptor
	 * 
	 * @param element
	 */
	public ContentGeneratorDescriptor(IConfigurationElement element) {
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
	 * Return whether or not all of {@link MarkerTypesModel} arein the
	 * selectedTypes.
	 * 
	 * @param selectedTypes
	 * @return boolean
	 */
	public boolean allTypesSelected(Collection selectedTypes) {
		return selectedTypes.containsAll(markerTypes);
	}

	/**
	 * Get the all of the fields that this content generator is using.
	 * 
	 * @return {@link MarkerField}[]
	 */
	public MarkerField[] getAllFields() {
		return allFields;
	}

	/**
	 * Get the category name from the receiver.
	 * 
	 * @return categoryName
	 */
	public String getCategoryName() {
		return configurationElement
				.getAttribute(ATTRIBUTE_DEFAULT_MARKER_GROUPING);

	}

	/**
	 * Return the configuration elements for the receiver.
	 * 
	 * @return IConfigurationElement[]
	 */
	public IConfigurationElement[] getFilterReferences() {
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
	public MarkerField[] getInitialVisible() {
		return initialVisible;
	}

	/**
	 * Get the markerGroups associated with the receiver.
	 * 
	 * @return Collection of {@link MarkerGroup}
	 */
	public Collection getMarkerGroups() {

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
	public String getName() {
		return configurationElement
				.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_NAME);
	}

	/**
	 * Return the type for typeId.
	 * 
	 * @param typeId
	 * @return {@link MarkerType} or <code>null</code> if it is not found.
	 */
	public MarkerType getType(String typeId) {
		Map all = getTypesTable();
		if (all.containsKey(typeId))
			return (MarkerType) all.get(typeId);
		return null;
	}

	/**
	 * Get the table that maps type ids to markerTypes.
	 * 
	 * @return Map of {@link String} to {@link MarkerType}
	 */
	public Map getTypesTable() {
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
