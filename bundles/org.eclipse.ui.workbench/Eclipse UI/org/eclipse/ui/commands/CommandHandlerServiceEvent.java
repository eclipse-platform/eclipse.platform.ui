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
 * An instance of this class describes changes to an instance of <code>ICommandHandlerService</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see ICommandHandlerServiceListener#commandHandlerServiceChanged
 */
public final class CommandHandlerServiceEvent {
	private boolean handlersByCommandIdChanged;
	private ICommandHandlerService commandHandlerService;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param commandHandlerService
	 *            the instance of the interface that changed.
	 * @param handlersByCommandIdChanged
	 *            true, iff the handlersByCommandId property changed.
	 */
	public CommandHandlerServiceEvent(
		ICommandHandlerService commandHandlerService,
		boolean handlersByCommandIdChanged) {
		if (commandHandlerService == null)
			throw new NullPointerException();

		this.commandHandlerService = commandHandlerService;
		this.handlersByCommandIdChanged = handlersByCommandIdChanged;
	}

	/**
	 * Returns the instance of the interface that changed.
	 * 
	 * @return the instance of the interface that changed. Guaranteed not to be
	 *         <code>null</code>.
	 */
	public ICommandHandlerService getCommandHandlerService() {
		return commandHandlerService;
	}

	/**
	 * Returns whether or not the handlersByCommandId property changed.
	 * 
	 * @return true, iff the handlersByCommandId property changed.
	 */
	public boolean haveHandlersByCommandIdChanged() {
		return handlersByCommandIdChanged;
	}
}
