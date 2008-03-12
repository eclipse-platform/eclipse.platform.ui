/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.internal.ProblemFilter;

/**
 * ProblemsSeverityAndDescriptionFieldFilter is the filter used by the problems
 * view.
 * 
 * @since 3.4
 * 
 */
public class ProblemsSeverityAndDescriptionFieldFilter extends
		SeverityAndDescriptionFieldFilter {

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.views.markers.DescriptionFieldFilter#select(org.eclipse.ui.views.markers.MarkerItem)
	 */
	public boolean select(MarkerItem item) {

		IMarker marker = item.getMarker();
		if (marker == null)
			return false;

		int markerSeverity = item.getAttributeValue(IMarker.SEVERITY, -1);
		if (markerSeverity < 0)
			return false;
		if (checkSeverity(markerSeverity))
			return super.select(item);
		return false;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.views.markers.DescriptionFieldFilter#loadLegacySettings(org.eclipse.ui.IMemento, org.eclipse.ui.internal.views.markers.MarkerContentGenerator)
	 */
	void loadLegacySettings(IMemento memento, MarkerContentGenerator generator) {

		super.loadLegacySettings(memento,generator);
		Integer severitySetting = memento
				.getInteger(ProblemFilter.TAG_SEVERITY);

		if (severitySetting != null) {
			selectedSeverities = severitySetting.intValue();
		}

	
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.DescriptionFieldFilter#initialize(org.eclipse.ui.views.markers.internal.ProblemFilter)
	 */
	public void initialize(ProblemFilter problemFilter) {
		super.initialize(problemFilter);
		if (problemFilter.getSeverity() > 0)
			selectedSeverities = problemFilter.getSeverity();
	}
}
