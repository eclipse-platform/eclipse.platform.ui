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
import org.eclipse.ui.commands.ICommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerServiceListener;
import org.eclipse.ui.commands.ICompoundCommandHandlerService;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.internal.util.Util;

public final class CompoundCommandHandlerService
	extends AbstractCommandHandlerService
	implements ICompoundCommandHandlerService {
	private final ICommandHandlerServiceListener commandHandlerServiceListener =
		new ICommandHandlerServiceListener() {
		public void commandHandlerServiceChanged(CommandHandlerServiceEvent commandHandlerServiceEvent) {
			update();
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
		update();
	}

	public Map getHandlersByCommandId() {
		return Collections.unmodifiableMap(handlersByCommandId);
	}

	public void removeCommandHandlerService(ICommandHandlerService commandHandlerService) {
		if (commandHandlerService == null)
			throw new NullPointerException();

		commandHandlerServices.remove(commandHandlerService);
		commandHandlerService.removeCommandHandlerServiceListener(
			commandHandlerServiceListener);
		update();
	}

	private void setHandlersByCommandId(Map handlersByCommandId) {
		handlersByCommandId =
			Util.safeCopy(
				handlersByCommandId,
				String.class,
				IHandler.class,
				false,
				true);
		boolean commandHandlerServiceChanged = false;
		Map commandEventsByCommandId = null;

		if (!this.handlersByCommandId.equals(handlersByCommandId)) {
			this.handlersByCommandId = handlersByCommandId;
			fireCommandHandlerServiceChanged(
				new CommandHandlerServiceEvent(this, true));
		}
	}

	private void update() {
		Map handlersByCommandId = new HashMap();

		for (Iterator iterator = commandHandlerServices.iterator();
			iterator.hasNext();
			) {
			ICommandHandlerService commandHandlerService =
				(ICommandHandlerService) iterator.next();

			for (Iterator iterator2 =
				commandHandlerService
					.getHandlersByCommandId()
					.entrySet()
					.iterator();
				iterator2.hasNext();
				) {
				Map.Entry entry = (Map.Entry) iterator2.next();
				String commandId = (String) entry.getKey();
				IHandler handler = (IHandler) entry.getValue();

				if (!handlersByCommandId.containsKey(commandId))
					handlersByCommandId.put(commandId, handler);
				else if (!handlersByCommandId.get(commandId).equals(handler))
					handlersByCommandId.put(commandId, null);
			}
		}

		setHandlersByCommandId(handlersByCommandId);
	}
}