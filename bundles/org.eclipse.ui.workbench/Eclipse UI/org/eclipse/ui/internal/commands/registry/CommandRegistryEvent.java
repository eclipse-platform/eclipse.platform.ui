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

package org.eclipse.ui.internal.commands.registry;

import org.eclipse.ui.commands.ICommandRegistry;
import org.eclipse.ui.commands.ICommandRegistryEvent;
import org.eclipse.ui.internal.util.Util;

final class CommandRegistryEvent implements ICommandRegistryEvent {

	private ICommandRegistry commandRegistry;

	CommandRegistryEvent(ICommandRegistry commandRegistry) {
		super();
		
		if (commandRegistry == null)
			throw new NullPointerException();
		
		this.commandRegistry = commandRegistry;
	}

	public boolean equals(Object object) {
		if (!(object instanceof CommandRegistryEvent))
			return false;

		CommandRegistryEvent commandRegistryEvent = (CommandRegistryEvent) object;	
		return Util.equals(commandRegistry, commandRegistryEvent.commandRegistry);
	}
	
	public ICommandRegistry getCommandRegistry() {
		return commandRegistry;
	}
}
