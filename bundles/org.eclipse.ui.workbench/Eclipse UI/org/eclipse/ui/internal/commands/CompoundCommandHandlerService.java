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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.commands.CommandHandlerServiceEvent;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.ICommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerServiceListener;
import org.eclipse.ui.commands.ICompoundCommandHandlerService;
import org.eclipse.ui.internal.util.Util;

public final class CompoundCommandHandlerService
	extends AbstractCommandHandlerService
	implements ICompoundCommandHandlerService {
	private final ICommandHandlerServiceListener commandHandlerServiceListener =
		new ICommandHandlerServiceListener() {
		public void commandHandlerServiceChanged(CommandHandlerServiceEvent commandHandlerServiceEvent) {
			refreshHandlersByCommandId();
		}
	};
	private final HashSet commandHandlerServices = new HashSet();
	private Map handlersByCommandId = new HashMap();

	public CompoundCommandHandlerService() {
	}

	public void addCommandHandlerService(ICommandHandlerService commandHandlerService) {
		if (commandHandlerService == null)
			throw new NullPointerException();

		commandHandlerService.addCommandHandlerServiceListener(
			commandHandlerServiceListener);
		commandHandlerServices.add(commandHandlerService);
		refreshHandlersByCommandId();
	}

	public Map getHandlersByCommandId() {
		return Collections.unmodifiableMap(handlersByCommandId);
	}

	private void refreshHandlersByCommandId() {
		Map handlersByCommandId = new HashMap();

		for (Iterator iterator = commandHandlerServices.iterator();
			iterator.hasNext();
			) {
			ICommandHandlerService commandHandlerService =
				(ICommandHandlerService) iterator.next();

			// TODO: should do the intersection here!
			handlersByCommandId.putAll(
				commandHandlerService.getHandlersByCommandId());
		}

		setHandlersByCommandId(handlersByCommandId);
	}

	public void removeCommandHandlerService(ICommandHandlerService commandHandlerService) {
		if (commandHandlerService == null)
			throw new NullPointerException();

		commandHandlerServices.remove(commandHandlerService);
		commandHandlerService.removeCommandHandlerServiceListener(
			commandHandlerServiceListener);
		refreshHandlersByCommandId();
	}

	public void setHandlersByCommandId(Map handlersByCommandId) {
		handlersByCommandId =
			Util.safeCopy(handlersByCommandId, String.class, IHandler.class);
		boolean commandHandlerServiceChanged = false;
		Map commandEventsByCommandId = null;

		if (!this.handlersByCommandId.equals(handlersByCommandId)) {
			this.handlersByCommandId = handlersByCommandId;
			fireCommandHandlerServiceChanged(
				new CommandHandlerServiceEvent(this, true));
		}
	}
}