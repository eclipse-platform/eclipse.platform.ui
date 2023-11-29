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

package org.eclipse.ui.views.markers;

import java.util.Map;


/**
 * A MarkerFieldParameters is a class that specifies a Map of parameters
 * to be passed to a {@link MarkerFieldFilter}.
 * @since 3.4
 */
public abstract class FiltersContributionParameters {

	/**
	 * Returns a map keyed names for parameter values. The values should be
	 * actual values that will be interpreted by the {@link MarkerFieldFilter}
	 * these parameters are designed for.
	 *
	 * Note that these parameters will be sent to the MarkerFieldFilter for
	 * every visible {@link MarkerField} in a markers view.
	 *
	 *
	 * @return A map of the name of the parameter value (<code>String</code>)
	 *         to the actual value of the parameter (<code>String</code>).
	 */
	public abstract Map/*<String, String>*/ getParameterValues();
}
