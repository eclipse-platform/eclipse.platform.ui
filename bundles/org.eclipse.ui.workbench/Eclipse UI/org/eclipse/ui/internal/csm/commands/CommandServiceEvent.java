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

import org.eclipse.ui.internal.csm.commands.api.ICommandService;
import org.eclipse.ui.internal.csm.commands.api.ICommandServiceEvent;

final class CommandServiceEvent implements ICommandServiceEvent {

	private boolean activeCommandIdsChanged;
	private ICommandService commandService;
	
	CommandServiceEvent(ICommandService commandService, boolean activeCommandIdsChanged) {
		if (commandService == null)
			throw new NullPointerException();
		
		this.activeCommandIdsChanged = activeCommandIdsChanged;
		this.commandService = commandService;
	}

	public ICommandService getCommandService() {
		return commandService;
	}
	
	public boolean haveActiveCommandIdsChanged() {
		return activeCommandIdsChanged;
	}
}
