package org.eclipse.ui.internal.views.markers;
/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.views.markers.FiltersContributionParameters;
import org.eclipse.ui.views.markers.MarkerSupportConstants;

/**
 * TodoFiltersContributionParameters is the filter to just show TODOs.
 * @since 3.4
 *
 */
public class TodoFiltersContributionParameters extends
		FiltersContributionParameters {
	
	private static Map todoMap;
	static {
		todoMap = new HashMap();
		todoMap.put(MarkerSupportConstants.CONTAINS_KEY, "TODO"); //$NON-NLS-1$
	}

	/**
	 * Return a new instance of the receiver.
	 */
	public TodoFiltersContributionParameters() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.provisional.views.markers.api.FiltersContributionParameters#getParameterValues()
	 */
	public Map getParameterValues() {
		return todoMap;
	}

}
