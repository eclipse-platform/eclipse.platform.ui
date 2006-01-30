/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * AttributeMarkerGrouping is the configuration element for the
 * markerAttributeGrouping extension.
 * 
 * @since 3.2
 * 
 */
public class AttributeMarkerGrouping {

	private String attribute;

	private String markerType;

	private String defaultGroupingEntry;

	private IConfigurationElement element;

	/**
	 * Create a new instance of the receiver for the given attribute on the
	 * markerType with an optional default grouping.
	 * 
	 * @param attributeId
	 * @param markerId
	 * @param defaultEntry
	 * @param configElement
	 */
	public AttributeMarkerGrouping(String attributeId, String markerId,
			String defaultEntry, IConfigurationElement configElement) {
		attribute = attributeId;
		markerType = markerId;
		defaultGroupingEntry = defaultEntry;
		element = configElement;

	}

	/**
	 * Return the id of the default grouping.
	 * @return String or <code>null</code> if it is not defined.
	 */
	public String getDefaultGroupingEntry() {
		return defaultGroupingEntry;
	}

	/**
	 * Return the id of the marker type for this type.
	 * @return String
	 */
	public String getMarkerType() {
		return markerType;
	}

	/**
	 * Return the name of the attribute for the receiver.
	 * @return String
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * Return the IConfigurationElement for the receiver.
	 * @return IConfigurationElement
	 */
	public IConfigurationElement getElement() {
		return element;
	}

}
