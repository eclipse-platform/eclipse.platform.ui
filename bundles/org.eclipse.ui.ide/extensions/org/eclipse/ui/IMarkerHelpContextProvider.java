/*******************************************************************************
 * Copyright (c) 2019 Tim Neumann <tim.neumann@advantest.com> and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tim Neumann <tim.neumann@advantest.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.resources.IMarker;

/**
 * Provides a help context for a given marker.
 *
 * @since 3.15
 */
public interface IMarkerHelpContextProvider {

	/**
	 * Get the help context for the given marker.
	 *
	 * @param marker The marker to get the help Context for.
	 * @return The help context for the marker or null if this provider does not
	 *         have a context for the given marker.
	 */
	public String getHelpContextForMarker(IMarker marker);

	/**
	 * Whether this provider <b>may</b> have a context for the given marker.
	 * <p>
	 * If this method returns false, this provider does definitely not have a
	 * context for this marker. If this method returns true,
	 * {@link #getHelpContextForMarker(IMarker)} may still return null.
	 * </p>
	 * <p>
	 * Implementations should consider always returning true, if there is no
	 * efficient way to check this.
	 * </p>
	 *
	 * @param marker The marker to check.
	 * @return Whether this provider <b>may</b> have a context for the given marker.
	 */
	public boolean hasHelpContextForMarker(IMarker marker);
}
