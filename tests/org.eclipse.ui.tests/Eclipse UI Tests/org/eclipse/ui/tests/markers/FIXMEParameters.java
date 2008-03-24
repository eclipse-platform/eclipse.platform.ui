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

package org.eclipse.ui.tests.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.views.markers.FiltersContributionParameters;
import org.eclipse.ui.views.markers.MarkerSupportConstants;

public class FIXMEParameters extends FiltersContributionParameters {
	
	private static Map fixmeMap;
	static {
		fixmeMap = new HashMap();
		fixmeMap.put(MarkerSupportConstants.CONTAINS_KEY, "FIXME"); //$NON-NLS-1$
	}

	/**
	 * The parameters for the fixme test.
	 */
	public FIXMEParameters() {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.FiltersContributionParameters#getParameterValues()
	 */
	public Map getParameterValues() {
		return fixmeMap;
	}

}
