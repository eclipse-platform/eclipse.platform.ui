/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * AttributeMarkerGrouping is the configuration element for the
 * markerAttributeGrouping extension.
 * 
 * @since 3.2
 * 
 */
public class AttributeMarkerGrouping {
	
	private static final String DEFAULT_GROUPING_ENTRY = "defaultGroupingEntry";//$NON-NLS-1$

	private static final String MARKER_TYPE = "markerType";//$NON-NLS-1$

	private static final String ATTRIBUTE = "attribute"; //$NON-NLS-1$

	private String attribute;

	private String markerType;

	private String defaultGroupingEntry;

	private IConfigurationElement element;

	// A list of groups we are associated with for unloading
	private Collection groups = new HashSet();

	/**
	 * Create a new instance of the receiver from element.
	 * @param element
	 */
	public AttributeMarkerGrouping(IConfigurationElement element) {

		attribute = element.getAttribute(ATTRIBUTE);
		markerType = element.getAttribute(MARKER_TYPE);
		defaultGroupingEntry = element.getAttribute(DEFAULT_GROUPING_ENTRY);
		this.element = element;
	}

	/**
	 * Return the id of the default grouping.
	 * 
	 * @return String or <code>null</code> if it is not defined.
	 */
	public String getDefaultGroupingEntry() {
		return defaultGroupingEntry;
	}

	/**
	 * Return the id of the marker type for this type.
	 * 
	 * @return String
	 */
	public String getMarkerType() {
		return markerType;
	}

	/**
	 * Return the name of the attribute for the receiver.
	 * 
	 * @return String
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * Return the IConfigurationElement for the receiver.
	 * 
	 * @return IConfigurationElement
	 */
	public IConfigurationElement getElement() {
		return element;
	}

	/**
	 * Add markerGroup to the list of referenced groups.
	 * 
	 * @param markerGroup
	 */
	public void addGroup(MarkerGroup markerGroup) {
		groups.add(markerGroup);

	}

	/**
	 * Unmap the receiver from the groups
	 */
	public void unmap() {
		Iterator referencedGroups = groups.iterator();
		while (referencedGroups.hasNext()) {
			MarkerGroup group = (MarkerGroup) referencedGroups.next();
			group.unmap(this);
		}

	}

}
