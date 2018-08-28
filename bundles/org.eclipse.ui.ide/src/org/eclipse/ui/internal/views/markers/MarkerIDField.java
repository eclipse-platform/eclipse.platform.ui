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

import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;


/**
 * @since 3.4
 *
 */
public class MarkerIDField extends MarkerField {

	@Override
	public String getValue(MarkerItem item) {
		if (item.getMarker() != null) {
			return String.valueOf(((MarkerSupportItem) item).getID());
		}
		return MarkerSupportInternalUtilities.EMPTY_STRING;
	}

	@Override
	public int compare(MarkerItem item1, MarkerItem item2) {
		long id1 = ((MarkerSupportItem) item1).getID();
		long id2 = ((MarkerSupportItem) item2).getID();
		if (id1 < id2) {
			return -1;
		}
		if (id1 > id2) {
			return 1;
		}
		return 0;
	}

}
