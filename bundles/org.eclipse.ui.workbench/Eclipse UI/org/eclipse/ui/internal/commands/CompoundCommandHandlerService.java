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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.commands.CommandHandlerServiceEvent;
import org.eclipse.ui.commands.ICompoundCommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerService;
import org.eclipse.ui.commands.ICommandHandlerServiceListener;
import org.eclipse.ui.internal.util.Util;

public final class CompoundCommandHandlerService
	extends AbstractCommandHandlerService
	implements ICompoundCommandHandlerService {
	private Set activeCommandIds = new HashSet();

	private final ICommandHandlerServiceListener commandHandlerServiceListener =
		new ICommandHandlerServiceListener() {
		public void commandHandlerServiceChanged(CommandHandlerServiceEvent commandHandlerServiceEvent) {
			refreshActiveCommandIds();
		}
	};
	private final HashSet commandHandlerServices = new HashSet();

	public CompoundCommandHandlerService() {
	}

	public void addCommandHandlerService(ICommandHandlerService commandHandlerService) {
		if (commandHandlerService == null)
			throw new NullPointerException();

		commandHandlerService.addCommandHandlerServiceListener(
			commandHandlerServiceListener);
		commandHandlerServices.add(commandHandlerService);
		refreshActiveCommandIds();
	}

	public Set getActiveCommandIds() {
		return Collections.unmodifiableSet(activeCommandIds);
	}

	private void refreshActiveCommandIds() {
		Set activeCommandIds = new HashSet();

		for (Iterator iterator = commandHandlerServices.iterator();
			iterator.hasNext();
			) {
			ICommandHandlerService commandHandlerService =
				(ICommandHandlerService) iterator.next();
			activeCommandIds.addAll(
				commandHandlerService.getActiveCommandIds());
		}

		setActiveCommandIds(activeCommandIds);
	}

	public void removeCommandHandlerService(ICommandHandlerService commandHandlerService) {
		if (commandHandlerService == null)
			throw new NullPointerException();

		commandHandlerServices.remove(commandHandlerService);
		commandHandlerService.removeCommandHandlerServiceListener(
			commandHandlerServiceListener);
		refreshActiveCommandIds();
	}

	private void setActiveCommandIds(Set activeCommandIds) {
		activeCommandIds = Util.safeCopy(activeCommandIds, String.class);
		boolean commandHandlerServiceChanged = false;
		Map commandEventsByCommandId = null;

		if (!this.activeCommandIds.equals(activeCommandIds)) {
			this.activeCommandIds = activeCommandIds;
			fireCommandHandlerServiceChanged(
				new CommandHandlerServiceEvent(this, true));
		}
	}
}