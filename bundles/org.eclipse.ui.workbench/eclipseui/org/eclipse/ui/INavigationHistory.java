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
 * Manages a list of entries to keep a history of locations on editors, enabling
 * the user to go back and forward without losing context.
 *
 * The history is a list of <code>INavigationLocation</code> and a pointer to
 * the current location. Whenever the back or forward action runs the history
 * restores the previous or next location.
 *
 * The back and/or forward actions should not change the content of the history
 * in any way.
 *
 * If the user steps N times in one direction (back or forward) and then N times
 * to the oposite direction, the editor and location should be exactly the same
 * as before.
 *
 * Clients must guarantee that the current location is always in the history,
 * which can be done either by marking a new location or by updating the current
 * location.
 *
 * Not intended to be implemented by clients.
 *
 * @since 2.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface INavigationHistory {
	/**
	 * Mark the current location into the history. This message should be sent by
	 * clients whenever significant changes in location are detected.
	 *
	 * The location is obtained by calling
	 * <code>INavigationLocationProvider.createNavigationLocation</code>
	 *
	 * @param part the editor part
	 */
	void markLocation(IEditorPart part);

	/**
	 * Returns the current location.
	 *
	 * @return the current location
	 */
	INavigationLocation getCurrentLocation();

	/**
	 * Returns all entries in the history.
	 *
	 * @return all entries in the history
	 */
	INavigationLocation[] getLocations();
}
