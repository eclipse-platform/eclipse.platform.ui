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
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.commands.CommandHandlerServiceEvent;
import org.eclipse.ui.commands.IMutableCommandHandlerService;
import org.eclipse.ui.internal.util.Util;

public final class MutableCommandHandlerService
	extends AbstractCommandHandlerService
	implements IMutableCommandHandlerService {
	private Set activeCommandIds = new HashSet();

	public MutableCommandHandlerService() {
	}

	public Set getActiveCommandIds() {
		return Collections.unmodifiableSet(activeCommandIds);
	}

	public void setActiveCommandIds(Set activeCommandIds) {
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
