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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;

/**
 * MarkerGroupingEntry is the configuration object for the markerGroupingEntry
 * extension.
 * 
 * @since 3.2
 * 
 */
public class MarkerGroupingEntry {

	private static final String PRIORITY = "priority"; //$NON-NLS-1$

	private MarkerGroup markerGroup;
	private String label;
	private String id;
	private int sortPriority;

	/**
	 * Create a new instance of the receiver from element.
	 * @param element
	 */
	public MarkerGroupingEntry(IConfigurationElement element) {
		label = element.getAttribute(MarkerSupportRegistry.LABEL);
		id = element.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ID);
		sortPriority = Integer.valueOf(element.getAttribute(PRIORITY))
				.intValue();
	}

	/**
	 * Create a new instance of the receiver with just a label.
	 * @param label
	 */
	public MarkerGroupingEntry(String label) {
		this.label = label;
		sortPriority = 0;
	}

	/**
	 * Set the receiver as the default grouping entry for type markerType.
	 * 
	 * @param markerType
	 *            String
	 */
	public void setAsDefault(String markerType) {
		markerGroup.setAsDefault(markerType, this);

	}

	/**
	 * Return the id for the receiver.
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the group for the receiver.
	 * 
	 * @param group
	 */
	public void setGroup(MarkerGroup group) {
		markerGroup = group;

	}

	/**
	 * Get the label of the receiver.
	 * 
	 * @return String
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Return the priority of the receiver.
	 * 
	 * @return int
	 */
	public int getPriority() {
		return sortPriority;
	}

	/**
	 * Return the marker group for the receiver.
	 * 
	 * @return FieldMarkerGroup
	 */
	public MarkerGroup getMarkerGroup() {
		return markerGroup;
	}

}
