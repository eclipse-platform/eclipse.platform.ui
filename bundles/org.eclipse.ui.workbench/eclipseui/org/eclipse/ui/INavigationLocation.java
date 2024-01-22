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
 * Represents the context marked for the user in the navigation history.
 *
 * Not intended to be implemented by clients. Clients should subclass
 * NavigationLocation instead.
 *
 * @since 2.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface INavigationLocation {

	/**
	 * Disposes of this location and frees any allocated resource.
	 */
	void dispose();

	/**
	 * Release any state kept by this location. Any relevant state should be saved
	 * by the previous call of saveState(IMemento). This object will not be used
	 * until restoreState is called again.
	 */
	void releaseState();

	/**
	 * Persists the state of this location into the <code>memento</code>
	 *
	 * @param memento the storage were the state should be saved into.
	 */
	void saveState(IMemento memento);

	/**
	 * Restore the state of this location from the <code>memento</code>
	 *
	 * @param memento the storage were the state was saved into.
	 */
	void restoreState(IMemento memento);

	/**
	 * Restore the context saved by this location.
	 */
	void restoreLocation();

	/**
	 * Merge the receiver into <code>currentLocation</code>. Return true if the two
	 * locations could be merged otherwise return false.
	 * <p>
	 * This message is sent to all locations before being added to the history;
	 * given the change to the new location to merge itself into the current
	 * location minimizing the number of entries in the navigation history.
	 * </p>
	 *
	 * @param currentLocation where the receiver should be merged into
	 * @return boolean true if the merge was possible
	 */
	boolean mergeInto(INavigationLocation currentLocation);

	/**
	 * Returns the input used for this location. Returns <code>null</code> if the
	 * receiver's state has been released.
	 *
	 * @return the input for this location
	 */
	Object getInput();

	/**
	 * Returns the display name for this location. This name is used in the
	 * navigation history list.
	 *
	 * @return the display name
	 */
	String getText();

	/**
	 * Sets the location's input.
	 * <p>
	 * Should not be called by clients.
	 * </p>
	 *
	 * @param input the editor input.
	 */
	void setInput(Object input);

	/**
	 * The message <code>update</code> is sent to the active location before another
	 * location becomes active.
	 */
	void update();
}
