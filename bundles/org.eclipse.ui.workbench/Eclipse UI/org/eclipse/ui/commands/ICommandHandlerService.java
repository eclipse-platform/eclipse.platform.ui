/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands;

import java.util.Map;

/**
 * An instance of this interface allows clients to manage command handlers.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see CommandHandlerServiceFactory
 */
public interface ICommandHandlerService {

	/**
	 * Registers an instance of <code>ICommandHandlerServiceListener</code>
	 * to listen for changes to properties of this instance.
	 * 
	 * @param commandHandlerServiceListener
	 *            the instance to register. Must not be <code>null</code>.
	 *            If an attempt is made to register an instance which is
	 *            already registered with this instance, no operation is
	 *            performed.
	 */
	void addCommandHandlerServiceListener(ICommandHandlerServiceListener commandHandlerServiceListener);

	/**
	 * Returns the map of handlers by command identifier.
	 * <p>
	 * Notification is sent to all registered listeners if this property
	 * changes.
	 * </p>
	 * 
	 * @return the map of handlers by command identifier. This map may be
	 *         empty, but its keys and values are guaranteed not to be <code>null</code>.
	 *         If this map is not empty, its key set is guaranteed to only
	 *         contain instances of <code>String</code> and its value set is
	 *         guaranteed to only contain instances of <code>IHandler</code>
	 */
	Map getHandlersByCommandId();

	/**
	 * Unregisters an instance of <code>ICommandHandlerServiceListener</code>
	 * listening for changes to properties of this instance.
	 * 
	 * @param commandHandlerServiceListener
	 *            the instance to unregister. Must not be <code>null</code>.
	 *            If an attempt is made to unregister an instance which is not
	 *            already registered with this instance, no operation is
	 *            performed.
	 */
	void removeCommandHandlerServiceListener(ICommandHandlerServiceListener commandHandlerServiceListener);
}