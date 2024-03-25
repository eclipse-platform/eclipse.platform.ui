/*******************************************************************************
 * Copyright (c) 2007, 2022 IBM Corporation and others.
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

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.ui.views.markers.MarkerViewUtil;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

/**
 * The DeltaMarkerEntry is the class that wraps an {@link IMarkerDelta} for testing.
 *
 * @since 3.6
 */
class DeltaMarkerEntry extends MarkerEntry {

	private final IMarkerDelta markerDelta;

	/**
	 * Create a new instance of the receiver.
	 */
	public DeltaMarkerEntry(IMarkerDelta markerDelta) {
		super(markerDelta.getMarker());
		this.markerDelta=markerDelta;
	}

	@Override
	Object getAttributeValue(String attribute) {
		Object value = getCachedValueOrCompute(attribute, () -> {
			return markerDelta.getAttribute(attribute);
		});
		return value;
	}

	@Override
	long getID() {
		return markerDelta.getId();
	}

	@Override
	String getMarkerTypeName() {
		return MarkerTypesModel.getInstance().getType(markerDelta.getType())
				.getLabel();
	}

	@Override
	public String getPath() {
		Object value = getCachedValueOrCompute(MarkerViewUtil.PATH_ATTRIBUTE,
				() -> super.getPath(markerDelta.getResource()));
		return (String) value;
	}

}
