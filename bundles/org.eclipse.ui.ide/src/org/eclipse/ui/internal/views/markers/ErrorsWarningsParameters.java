/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
 * ErrorsWarningsParameters is the parameters for errors and warnings.
 * 
 * @since 3.5
 * 
 */
public class ErrorsWarningsParameters extends FiltersContributionParameters {

	private static Map parametersMap;
	static {
		parametersMap = new HashMap();
		parametersMap.put(IMarker.SEVERITY, new Integer(
				SeverityAndDescriptionFieldFilter.SEVERITY_WARNING|SeverityAndDescriptionFieldFilter.SEVERITY_ERROR));
	}

	/**
	 * Create a new instance of the receiver.
	 */
	public ErrorsWarningsParameters() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.FiltersContributionParameters#getParameterValues()
	 */
	public Map getParameterValues() {
		return parametersMap;
	}

}
