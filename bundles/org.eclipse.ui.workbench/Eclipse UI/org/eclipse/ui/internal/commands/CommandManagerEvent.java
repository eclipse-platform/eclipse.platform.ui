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

import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.ICommandManagerEvent;
import org.eclipse.ui.internal.util.Util;

final class CommandManagerEvent implements ICommandManagerEvent {

	private ICommandManager commandManager;

	CommandManagerEvent(ICommandManager commandManager)
		throws IllegalArgumentException {		
		super();
		
		if (commandManager == null)
			throw new IllegalArgumentException();
		
		this.commandManager = commandManager;
	}

	public boolean equals(Object object) {
		if (!(object instanceof CommandManagerEvent))
			return false;

		CommandManagerEvent commandManagerEvent = (CommandManagerEvent) object;	
		return Util.equals(commandManager, commandManagerEvent.commandManager);
	}
	
	public ICommandManager getCommandManager() {
		return commandManager;
	}
}
