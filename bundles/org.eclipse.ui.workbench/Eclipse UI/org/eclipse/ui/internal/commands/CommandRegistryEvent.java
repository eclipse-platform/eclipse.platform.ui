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

import org.eclipse.ui.commands.ICommandRegistry;
import org.eclipse.ui.commands.ICommandRegistryEvent;

final class CommandRegistryEvent implements ICommandRegistryEvent {

	private ICommandRegistry commandRegistry;

	CommandRegistryEvent(ICommandRegistry commandRegistry) {
		if (commandRegistry == null)
			throw new NullPointerException();
		
		this.commandRegistry = commandRegistry;
	}

	public ICommandRegistry getCommandRegistry() {
		return commandRegistry;
	}
}
