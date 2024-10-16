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

/**
 * Should be implemented by editors that wish to contribute to the navigation
 * history. The message <code>createNavigationLocation</code> will be sent when
 * a new location is marked in the history.
 *
 * @since 2.1
 */
public interface INavigationLocationProvider {
	/**
	 * Creates an empty navigation location. The message <code>restoreState</code>
	 * will be sent to the location to restore its state.
	 *
	 * @return INavigationLocation
	 */
	INavigationLocation createEmptyNavigationLocation();

	/**
	 * Creates a navigation location describing the current state.
	 *
	 * @return INavigationLocation
	 */
	INavigationLocation createNavigationLocation();
}
