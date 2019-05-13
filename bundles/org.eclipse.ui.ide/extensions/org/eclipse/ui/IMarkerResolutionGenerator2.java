/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui;

import org.eclipse.core.resources.IMarker;

/**
 * A marker resolution generator should implement this interface rather than
 * <code>IMarkerResolutionGenerator</code> if it can determine whether a particular marker
 * has any resolutions more efficiently than computing all the resolutions.
 *
 * @since 2.1
 */
public interface IMarkerResolutionGenerator2 extends IMarkerResolutionGenerator {

	/**
	 * Returns whether there are any resolutions for the given marker.
	 *
	 * @param marker the marker
	 * @return <code>true</code> if there are resolutions for the given marker,
	 *   <code>false</code> if not
	 */
	public boolean hasResolutions(IMarker marker);
}

