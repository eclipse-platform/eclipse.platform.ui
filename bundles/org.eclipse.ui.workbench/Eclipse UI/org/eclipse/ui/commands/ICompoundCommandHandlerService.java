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

/**
 * An instance of this interface allows clients to manage command handler.
 * <p>
 * The list of active commands in this instance is the union of the lists of
 * active commands in all instances of <code>ICommandHandlerService</code>
 * added via the method <code>addCommandHandlerService</code>.
 * </p>
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
public interface ICompoundCommandHandlerService
	extends ICommandHandlerService {

	/**
	 * Adds an instance of <code>ICommandHandlerService</code> to this
	 * instance.
	 * 
	 * @param commandHandlerService
	 *            the instance to add to this instance.
	 */
	void addCommandHandlerService(ICommandHandlerService commandHandlerService);

	/**
	 * Removes an instance of <code>ICommandHandlerService</code> from
	 * this instance.
	 * 
	 * @param commandHandlerService
	 *            the instance to remove from this instance.
	 */
	void removeCommandHandlerService(ICommandHandlerService commandHandlerService);
}