/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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

import java.util.Map;

/**
 * <p>
 * A provider of notifications for when a change has occurred to a particular
 * type of source. These providers can be given to the appropriate service, and
 * this service will then re-evaluate the appropriate pieces of its internal
 * state in response to these changes.
 * </p>
 * <p>
 * It is recommended that clients subclass <code>AbstractSourceProvider</code>
 * instead, as this provides some common support for listeners.
 * </p>
 *
 * @since 3.1
 * @see org.eclipse.ui.handlers.IHandlerService
 * @see org.eclipse.ui.ISources
 */
public interface ISourceProvider {

	/**
	 * Adds a listener to this source provider. This listener will be notified
	 * whenever the corresponding source changes.
	 *
	 * @param listener The listener to add; must not be <code>null</code>.
	 */
	void addSourceProviderListener(ISourceProviderListener listener);

	/**
	 * Allows the source provider an opportunity to clean up resources (e.g.,
	 * listeners) before being released. This method should be called by the creator
	 * after the source provider has been removed from all the services with which
	 * it was registered.
	 */
	void dispose();

	/**
	 * Returns the current state of the sources tracked by this provider. This is
	 * used to provide a view of the world if the event loop is busy and things are
	 * some state has already changed.
	 * <p>
	 * For use with core expressions, this map should contain
	 * IEvaluationContext#UNDEFINED_VARIABLE for properties which are only sometimes
	 * available.
	 * </p>
	 *
	 * @return A map of variable names (<code>String</code>) to variable values
	 *         (<code>Object</code>). This may be empty, and may be
	 *         <code>null</code>.
	 */
	Map getCurrentState();

	/**
	 * Returns the names of those sources provided by this class. This is used by
	 * clients of source providers to determine which source providers they actually
	 * need.
	 *
	 * @return An array of source names. This value should never be
	 *         <code>null</code> or empty.
	 */
	String[] getProvidedSourceNames();

	/**
	 * Removes a listener from this source provider. This listener will be notified
	 * whenever the corresponding source changes.
	 *
	 * @param listener The listener to remove; must not be <code>null</code>.
	 */
	void removeSourceProviderListener(ISourceProviderListener listener);
}
