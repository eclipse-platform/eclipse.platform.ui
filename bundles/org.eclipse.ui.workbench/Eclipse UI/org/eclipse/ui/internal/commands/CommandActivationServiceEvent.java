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

import org.eclipse.ui.commands.ICommandActivationService;
import org.eclipse.ui.commands.ICommandActivationServiceEvent;
import org.eclipse.ui.internal.util.Util;

public final class CommandActivationServiceEvent implements ICommandActivationServiceEvent {

	private ICommandActivationService commandActivationService;

	public CommandActivationServiceEvent(ICommandActivationService commandActivationService)
		throws IllegalArgumentException {		
		super();
		
		if (commandActivationService == null)
			throw new IllegalArgumentException();
		
		this.commandActivationService = commandActivationService;
	}

	public boolean equals(Object object) {
		if (!(object instanceof CommandActivationServiceEvent))
			return false;

		CommandActivationServiceEvent commandActivationServiceEvent = (CommandActivationServiceEvent) object;	
		return Util.equals(commandActivationService, commandActivationServiceEvent.commandActivationService);
	}
	
	public ICommandActivationService getCommandActivationService() {
		return commandActivationService;
	}
}
