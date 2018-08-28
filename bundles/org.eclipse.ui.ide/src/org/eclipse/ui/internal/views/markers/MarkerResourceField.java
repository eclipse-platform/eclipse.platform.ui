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

import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.MarkerViewUtil;

/**
 * MarkerResourceField is the field that specifies the resource column.
 *
 * @since 3.4
 *
 */
public class MarkerResourceField extends MarkerField {

	@Override
	public String getValue(MarkerItem item) {
		if (item.getMarker() == null)
			return MarkerSupportInternalUtilities.EMPTY_STRING;

		return TextProcessor.process(item.getAttributeValue(MarkerViewUtil.NAME_ATTRIBUTE,
				item.getMarker().getResource().getName()));

	}
}
