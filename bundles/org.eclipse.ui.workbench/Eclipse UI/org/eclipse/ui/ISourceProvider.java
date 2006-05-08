/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * @param listener
	 *            The listener to add; must not be <code>null</code>.
	 */
	public void addSourceProviderListener(ISourceProviderListener listener);

	/**
	 * Allows the source provider an opportunity to clean up resources (e.g.,
	 * listeners) before being released. This method should be called by the
	 * creator after the source provider has been removed from all the services
	 * with which it was registered.
	 */
	public void dispose();

	/**
	 * Returns the current state of the sources tracked by this provider. This
	 * is used to provide a view of the world if the event loop is busy and
	 * things are some state has already changed.
	 * 
	 * @return A map of variable names (<code>String</code>) to variable
	 *         values (<code>Object</code>). This may be empty, and may be
	 *         <code>null</code>.
	 */
	public Map getCurrentState();

	/**
	 * Returns the names of those sources provided by this class. This is used
	 * by clients of source providers to determine which source providers they
	 * actually need.
	 * 
	 * @return An array of source names. This value should never be
	 *         <code>null</code> or empty.
	 */
	public String[] getProvidedSourceNames();

	/**
	 * Removes a listener from this source provider. This listener will be
	 * notified whenever the corresponding source changes.
	 * 
	 * @param listener
	 *            The listener to remove; must not be <code>null</code>.
	 */
	public void removeSourceProviderListener(ISourceProviderListener listener);
}
