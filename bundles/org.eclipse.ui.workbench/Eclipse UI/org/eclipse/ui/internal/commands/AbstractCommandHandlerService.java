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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.commands.CommandHandlerServiceEvent;
import org.eclipse.ui.commands.ICommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerServiceListener;

public abstract class AbstractCommandHandlerService
	implements ICommandHandlerService {
	private List commandHandlerServiceListeners;

	protected AbstractCommandHandlerService() {
	}

	public void addCommandHandlerServiceListener(ICommandHandlerServiceListener commandHandlerServiceListener) {
		if (commandHandlerServiceListener == null)
			throw new NullPointerException();

		if (commandHandlerServiceListeners == null)
			commandHandlerServiceListeners = new ArrayList();

		if (!commandHandlerServiceListeners
			.contains(commandHandlerServiceListener))
			commandHandlerServiceListeners.add(
				commandHandlerServiceListener);
	}

	protected void fireCommandHandlerServiceChanged(CommandHandlerServiceEvent commandHandlerServiceEvent) {
		if (commandHandlerServiceEvent == null)
			throw new NullPointerException();

		if (commandHandlerServiceListeners != null)
			for (int i = 0; i < commandHandlerServiceListeners.size(); i++)
				(
					(
						ICommandHandlerServiceListener) commandHandlerServiceListeners
							.get(
						i)).commandHandlerServiceChanged(
					commandHandlerServiceEvent);
	}

	public void removeCommandHandlerServiceListener(ICommandHandlerServiceListener commandHandlerServiceListener) {
		if (commandHandlerServiceListener == null)
			throw new NullPointerException();

		if (commandHandlerServiceListeners != null)
			commandHandlerServiceListeners.remove(
				commandHandlerServiceListener);
	}
}
