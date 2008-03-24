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
 * AllErrorsParameters is the parameters for the error severity type.
 * 
 * @since 3.4
 * 
 */
public class AllErrorsParameters extends FiltersContributionParameters {

	private static Map errorsMap;
	static {
		errorsMap = new HashMap();
		errorsMap.put(IMarker.SEVERITY, new Integer(
				SeverityAndDescriptionFieldFilter.SEVERITY_ERROR));
	}

	/**
	 * Create a new instance of the reciever.
	 */
	public AllErrorsParameters() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.FiltersContributionParameters#getParameterValues()
	 */
	public Map getParameterValues() {
		return errorsMap;
	}

}
