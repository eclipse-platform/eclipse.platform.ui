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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.INestableKeyBindingService;
import org.eclipse.ui.IWorkbenchSite;

public final class KeyBindingService implements INestableKeyBindingService {

	private SortedMap commandIdToActionMap = new TreeMap();
	private String[] scopes =
		new String[] { IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID };

	public KeyBindingService(IWorkbenchSite partSite) {
		if (partSite instanceof EditorSite) {
			EditorActionBuilder.ExternalContributor contributor =
				(EditorActionBuilder
					.ExternalContributor) ((EditorSite) partSite)
					.getExtensionActionBarContributor();

			if (contributor != null)
				registerExtendedActions(contributor.getExtendedActions());
		}
	}

	IAction getAction(String command) {
		IAction action = null;
		if (activeService instanceof KeyBindingService) {
			action = ((KeyBindingService) activeService).getAction(command);
		}

		if (action == null) {
			action = (IAction) commandIdToActionMap.get(command);
		}

		return action;
	}

	void registerExtendedActions(ActionDescriptor[] actionDescriptors) {
		if (actionDescriptors != null) {
			for (int i = 0; i < actionDescriptors.length; i++) {
				ActionDescriptor actionDescriptor = actionDescriptors[i];

				if (actionDescriptor != null) {
					IAction action = actionDescriptors[i].getAction();

					if (action != null
						&& action.getActionDefinitionId() != null)
						registerAction(action);
				}
			}
		}
	}

	/*
	 * @see IKeyBindingService#getScopes()
	 */
	public String[] getScopes() {
		// Get the nested scopes, if any.
		final String[] nestedScopes;
		if (activeService == null) {
			nestedScopes = null;
		} else {
			nestedScopes = activeService.getScopes();
		}

		// Build the list of active scopes
		Set activeScopes = new HashSet();
		for (int i = 0; i < scopes.length; i++) {
			activeScopes.add(scopes[i]);
		}
		if (nestedScopes != null) {
			for (int i = 0; i < nestedScopes.length; i++) {
				activeScopes.add(nestedScopes[i]);
			}
		}

		return (String[]) activeScopes.toArray(new String[activeScopes.size()]);
	}

	/*
	 * @see IKeyBindingService#setScopes(String[] scopes)
	 */
	public void setScopes(String[] scopes) throws IllegalArgumentException {
		if (scopes == null || scopes.length < 1)
			throw new IllegalArgumentException();

		this.scopes = (String[]) scopes.clone();

		for (int i = 0; i < scopes.length; i++)
			if (scopes[i] == null)
				throw new IllegalArgumentException();
	}

	/*
	 * @see IKeyBindingService#registerAction(IAction)
	 */
	public void registerAction(IAction action) {
		String command = action.getActionDefinitionId();

		if (command != null)
			commandIdToActionMap.put(command, action);
	}

	/*
	* @see IKeyBindingService#unregisterAction(IAction)
	*/
	public void unregisterAction(IAction action) {
		String command = action.getActionDefinitionId();

		if (command != null)
			commandIdToActionMap.remove(command);
	}

	/*
	 * @see IKeyBindingService#getActiveAcceleratorConfigurationId()
	 */
	public String getActiveAcceleratorConfigurationId() {
		return org
			.eclipse
			.ui
			.internal
			.commands
			.Manager
			.getInstance()
			.getKeyMachine()
			.getConfiguration();
	}

	/*
	 * @see IKeyBindingService#getActiveAcceleratorScopeId()
	 */
	public String getActiveAcceleratorScopeId() {
		return getScopes()[0];
	}

	/*
	 * @see IKeyBindingService#setActiveAcceleratorScopeId(String)
	 */
	public void setActiveAcceleratorScopeId(String scopeId)
		throws IllegalArgumentException {
		setScopes(new String[] { scopeId });
	}

	/*
	* @see IKeyBindingService#processKey(Event)
	*/
	public boolean processKey(KeyEvent event) {
		return false;
	}

	/*
	 * @see IKeyBindingService#registerAction(IAction)
	 */
	public void enable(boolean enable) {
	}

	private final Map nestedServices = new HashMap();
	private IKeyBindingService activeService = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.INestableKeyBindingService#activateKeyBindingService(java.lang.Object)
	 */
	public boolean activateKeyBindingService(IWorkbenchSite nestedSite) {
		// Check if we should do a deactivation.
		if (nestedSite == null) {
			if (activeService == null) {
				return false;
			} else {
				activeService = null;
				return true;
			}
		}

		// Attempt to activate a service.
		IKeyBindingService service =
			(IKeyBindingService) nestedServices.get(nestedSite);
		if (service == null) {
			return false;
		}

		activeService = service;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.INestableKeyBindingService#removeKeyBindingService(java.lang.Object)
	 */
	public boolean removeKeyBindingService(IWorkbenchSite nestedSite) {
		IKeyBindingService service =
			(IKeyBindingService) nestedServices.remove(nestedSite);
		if (service == null) {
			return false;
		}

		if (service.equals(activeService)) {
			activeService = null;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.INestableKeyBindingService#getKeyBindingService(java.lang.Object)
	 */
	public IKeyBindingService getKeyBindingService(IWorkbenchSite nestedSite) {
		if (nestedSite == null) {
			return null;
		}

		IKeyBindingService service =
			(IKeyBindingService) nestedServices.get(nestedSite);
		if (service == null) {
			service = new KeyBindingService(nestedSite);
			nestedServices.put(nestedSite, service);
		}

		return service;
	}
}
