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

package org.eclipse.ui.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.commands.IMutableCommandHandlerService;
import org.eclipse.ui.contexts.IMutableContextActivationService;
import org.eclipse.ui.internal.commands.ActionHandler;

final class KeyBindingService implements IKeyBindingService {
	private Map handlersByCommandId = new HashMap();
	private IMutableCommandHandlerService mutableCommandHandlerService;
	private IMutableContextActivationService mutableContextActivationService;

	KeyBindingService(
		IMutableCommandHandlerService mutableCommandHandlerService,
		IMutableContextActivationService mutableContextActivationService) {
		super();
		this.mutableCommandHandlerService = mutableCommandHandlerService;
		this.mutableContextActivationService = mutableContextActivationService;
	}

	public String[] getScopes() {
		Set scopes = mutableContextActivationService.getActiveContextIds();
		return (String[]) scopes.toArray(new String[scopes.size()]);
	}

	public void setScopes(String[] scopes) {
		mutableContextActivationService.setActiveContextIds(
			new HashSet(Arrays.asList(scopes)));
	}

	public void registerAction(IAction action) {
		String commandId = action.getActionDefinitionId();

		if (commandId != null) {
			handlersByCommandId.put(commandId, new ActionHandler(action));
			mutableCommandHandlerService.setHandlersByCommandId(
				handlersByCommandId);
		}
	}

	public void unregisterAction(IAction action) {
		String commandId = action.getActionDefinitionId();

		if (commandId != null) {
			handlersByCommandId.remove(commandId);
			mutableCommandHandlerService.setHandlersByCommandId(
				handlersByCommandId);
		}
	}
}
