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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.LegacyHandlerSubmissionExpression;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.MultiPageEditorSite;

/**
 * @since 3.2
 * 
 */
public final class ProxyKeyBindingService implements IKeyBindingService {

	private IWorkbenchSite fSite;

	private IContextService fContextService;

	private IHandlerService fHandlerService;

	private Map fActiveHandlers = new HashMap();

	private Collection fEnabledContexts = new ArrayList();

	/**
	 * Create the proxy key binding service.
	 * 
	 * @param site
	 */
	public ProxyKeyBindingService(IWorkbenchSite site) {
		fSite = site;

		// get the global services ... this is necessary
		// to replicate legacy behaviour.
		fContextService = (IContextService) fSite.getWorkbenchWindow()
				.getWorkbench().getService(IContextService.class);
		fHandlerService = (IHandlerService) fSite.getWorkbenchWindow()
				.getWorkbench().getService(IHandlerService.class);
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
					commandId, handler, new LegacyHandlerSubmissionExpression(
							null, null, getPartSite()));
			fActiveHandlers.put(commandId, activation);
		}
	}

	/**
	 * This method implements a workaround because site is not known to the
	 * workbench.
	 * 
	 * @return the correct part site for the handler expression
	 */
	private IWorkbenchPartSite getPartSite() {
		/*
		 * TODO This only works for a single level of MultiPageEditorSites. I
		 * don't believe it will work if there is a multi-page editor site
		 * within a multi-page editor site.
		 */
		if (fSite instanceof MultiPageEditorSite) {
			return ((MultiPageEditorSite) fSite).getMultiPageEditor().getSite();
		}
		return (IWorkbenchPartSite) fSite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IKeyBindingService#setScopes(java.lang.String[])
	 */
	public void setScopes(String[] scopes) {
		if (!fEnabledContexts.isEmpty()) {
			fContextService.deactivateContexts(fEnabledContexts);
			fEnabledContexts.clear();
		}
		for (int i = 0; i < scopes.length; i++) {
			fEnabledContexts.add(fContextService.activateContext(scopes[i],
					new LegacyHandlerSubmissionExpression(null, null,
							getPartSite())));
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
