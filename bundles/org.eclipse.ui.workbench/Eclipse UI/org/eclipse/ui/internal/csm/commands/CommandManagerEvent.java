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

package org.eclipse.ui.internal.csm.commands;

import org.eclipse.ui.internal.csm.commands.api.ICommandManager;
import org.eclipse.ui.internal.csm.commands.api.ICommandManagerEvent;

final class CommandManagerEvent implements ICommandManagerEvent {

	private boolean activeCommandIdsChanged;
	private ICommandManager commandManager;
	private boolean definedCommandIdsChanged;
	private boolean enabledCommandIdsChanged;

	CommandManagerEvent(ICommandManager commandManager, boolean activeCommandIdsChanged, boolean definedCommandIdsChanged, boolean enabledCommandIdsChanged) {
		if (commandManager == null)
			throw new NullPointerException();
		
		this.commandManager = commandManager;
		this.activeCommandIdsChanged = activeCommandIdsChanged;
		this.definedCommandIdsChanged = definedCommandIdsChanged;
		this.enabledCommandIdsChanged = enabledCommandIdsChanged;
	}

	public ICommandManager getCommandManager() {
		return commandManager;
	}

	public boolean haveActiveCommandIdsChanged() {
		return activeCommandIdsChanged;
	}
	
	public boolean haveDefinedCommandIdsChanged() {
		return definedCommandIdsChanged;
	}

	public boolean haveEnabledCommandIdsChanged() {
		return enabledCommandIdsChanged;
	}
}
