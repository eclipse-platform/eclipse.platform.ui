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

import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandEvent;
import org.eclipse.ui.internal.util.Util;

final class CommandEvent implements ICommandEvent {

	private ICommand command;

	CommandEvent(ICommand command)
		throws IllegalArgumentException {		
		super();
		
		if (command == null)
			throw new IllegalArgumentException();
		
		this.command = command;
	}

	public boolean equals(Object object) {
		if (!(object instanceof CommandEvent))
			return false;

		CommandEvent commandEvent = (CommandEvent) object;	
		return Util.equals(command, commandEvent.command);
	}
	
	public ICommand getCommand() {
		return command;
	}
}
