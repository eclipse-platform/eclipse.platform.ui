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

package org.eclipse.ui.internal.views.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.views.markers.FiltersContributionParameters;

/**
 * SelectedWarningsParameters is the parameters for warnings.
 * 
 * @since 3.4
 * 
 */
public class SelectedWarningsParameters extends FiltersContributionParameters {

	private static Map warningsMap;
	static {
		warningsMap = new HashMap();
		warningsMap.put(IMarker.SEVERITY, new Integer(
				SeverityAndDescriptionFieldFilter.SEVERITY_WARNING));
	}

	/**
	 * Create a new instance of the receiver.
	 */
	public SelectedWarningsParameters() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.FiltersContributionParameters#getParameterValues()
	 */
	public Map getParameterValues() {
		return warningsMap;
	}

}
