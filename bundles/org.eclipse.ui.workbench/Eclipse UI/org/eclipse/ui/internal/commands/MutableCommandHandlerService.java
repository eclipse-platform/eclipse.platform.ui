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
import java.util.Map;

import org.eclipse.ui.commands.CommandHandlerServiceEvent;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IMutableCommandHandlerService;

import org.eclipse.ui.internal.util.Util;

public final class MutableCommandHandlerService
	extends AbstractCommandHandlerService
	implements IMutableCommandHandlerService {
	private Map handlersByCommandId = new HashMap();

	public MutableCommandHandlerService() {
	}

	public Map getHandlersByCommandId() {
		return Collections.unmodifiableMap(handlersByCommandId);
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
