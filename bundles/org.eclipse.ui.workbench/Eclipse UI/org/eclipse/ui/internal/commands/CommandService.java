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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.ICommandServiceEvent;
import org.eclipse.ui.commands.ICommandServiceListener;
import org.eclipse.ui.internal.util.Util;

public final class CommandService implements ICommandService {

	private Set activeCommandIds = new HashSet();
	private List commandServiceListeners;

	public CommandService() {
	}

	public void addCommandServiceListener(ICommandServiceListener commandServiceListener) {
		if (commandServiceListener == null)
			throw new NullPointerException();
			
		if (commandServiceListeners == null)
			commandServiceListeners = new ArrayList();
		
		if (!commandServiceListeners.contains(commandServiceListener))
			commandServiceListeners.add(commandServiceListener);
	}

	public Set getActiveCommandIds() {
		return Collections.unmodifiableSet(activeCommandIds);
	}
	
	public void removeCommandServiceListener(ICommandServiceListener commandServiceListener) {
		if (commandServiceListener == null)
			throw new NullPointerException();
			
		if (commandServiceListeners != null)
			commandServiceListeners.remove(commandServiceListener);
	}
		
	public void setActiveCommandIds(Set activeCommandIds) {
		activeCommandIds = Util.safeCopy(activeCommandIds, String.class);
		boolean commandServiceChanged = false;
		Map commandEventsByCommandId = null;

		if (!this.activeCommandIds.equals(activeCommandIds)) {
			this.activeCommandIds = activeCommandIds;
			fireCommandServiceChanged(new CommandServiceEvent(this, true));	
		}
	}

	private void fireCommandServiceChanged(ICommandServiceEvent commandServiceEvent) {
		if (commandServiceEvent == null)
			throw new NullPointerException();
		
		if (commandServiceListeners != null)
			for (int i = 0; i < commandServiceListeners.size(); i++)
				((ICommandServiceListener) commandServiceListeners.get(i)).commandServiceChanged(commandServiceEvent);
	}
}
