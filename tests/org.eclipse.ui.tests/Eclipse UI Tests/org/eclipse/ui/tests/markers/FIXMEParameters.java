/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
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

package org.eclipse.ui.tests.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.views.markers.FiltersContributionParameters;
import org.eclipse.ui.views.markers.MarkerSupportConstants;

public class FIXMEParameters extends FiltersContributionParameters {

	private static Map<String, String> fixmeMap;
	static {
		fixmeMap = new HashMap<>();
		fixmeMap.put(MarkerSupportConstants.CONTAINS_KEY, "FIXME"); //$NON-NLS-1$
	}

	/**
	 * The parameters for the fixme test.
	 */
	public FIXMEParameters() {

	}

	@Override
	public Map getParameterValues() {
		return fixmeMap;
	}

}
