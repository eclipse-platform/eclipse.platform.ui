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

package org.eclipse.ui.internal.commands;

import org.eclipse.ui.commands.ICommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerServiceEvent;
import org.eclipse.ui.internal.util.Util;

public final class CommandHandlerServiceEvent implements ICommandHandlerServiceEvent {

	private ICommandHandlerService commandHandlerService;

	public CommandHandlerServiceEvent(ICommandHandlerService commandHandlerService)
		throws IllegalArgumentException {		
		super();
		
		if (commandHandlerService == null)
			throw new IllegalArgumentException();
		
		this.commandHandlerService = commandHandlerService;
	}

	public boolean equals(Object object) {
		if (!(object instanceof CommandHandlerServiceEvent))
			return false;

		CommandHandlerServiceEvent commandHandlerServiceEvent = (CommandHandlerServiceEvent) object;	
		return Util.equals(commandHandlerService, commandHandlerServiceEvent.commandHandlerService);
	}
	
	public ICommandHandlerService getCommandHandlerService() {
		return commandHandlerService;
	}
}
