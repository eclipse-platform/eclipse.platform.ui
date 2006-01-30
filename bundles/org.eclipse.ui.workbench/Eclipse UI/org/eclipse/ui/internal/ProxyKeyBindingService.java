/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.2
 * 
 */
public final class ProxyKeyBindingService implements IKeyBindingService {

	private IServiceLocator fServiceLocator;

	private IContextService fContextService;

	private IHandlerService fHandlerService;

	private Map fActiveHandlers = new HashMap();

	/**
	 * Create the proxy key binding service.
	 * 
	 * @param site
	 */
	public ProxyKeyBindingService(IWorkbenchSite site) {
		fServiceLocator = site;
		fContextService = (IContextService) fServiceLocator
				.getService(IContextService.class);
		fHandlerService = (IHandlerService) fServiceLocator
				.getService(IHandlerService.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IKeyBindingService#getScopes()
	 */
	public String[] getScopes() {
		Collection c = fContextService.getActiveContextIds();
		if (c == null) {
			return null;
		}
		return (String[]) c.toArray(new String[c.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IKeyBindingService#registerAction(org.eclipse.jface.action.IAction)
	 */
	public void registerAction(IAction action) {
		unregisterAction(action);
		String commandId = action.getActionDefinitionId();
		if (commandId != null) {
			ActionHandler handler = new ActionHandler(action);
			IHandlerActivation activation = fHandlerService.activateHandler(
					commandId, handler);
			fActiveHandlers.put(commandId, activation);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IKeyBindingService#setScopes(java.lang.String[])
	 */
	public void setScopes(String[] scopes) {
		for (int i = 0; i < scopes.length; i++) {
			fContextService.activateContext(scopes[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IKeyBindingService#unregisterAction(org.eclipse.jface.action.IAction)
	 */
	public void unregisterAction(IAction action) {
		String commandId = action.getActionDefinitionId();
		if (commandId != null) {
			IHandlerActivation activation = (IHandlerActivation) fActiveHandlers
					.remove(commandId);
			if (activation != null) {
				fHandlerService.deactivateHandler(activation);
				activation.getHandler().dispose();
			}
		}
	}
}
