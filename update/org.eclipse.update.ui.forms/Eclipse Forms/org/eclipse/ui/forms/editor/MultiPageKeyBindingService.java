/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.editor;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * Implementation of <code>IKeyBindingService</code> for nested editors.
 * "Virtualizes" the key binding service by keeping the outer key binding service
 * up to date for the currently active nested editor.
 */
public class MultiPageKeyBindingService implements IKeyBindingService {

	private MultiPageKeyBindingEditorSite fSite;
	private String[] fScopes = new String[] { IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID };
	private HashMap fCommandIdToActionMap = new HashMap();

	public MultiPageKeyBindingService(MultiPageKeyBindingEditorSite site) {
		fSite = site;
	}

	/** 
	 * Is the corresponding nested editor active?
	 */
	private boolean isActive() {
		return fSite.isActive();
	}

	public void activate() {
		// register all actions with the outer service
		for (Iterator i = fCommandIdToActionMap.values().iterator(); i.hasNext();) {
			getOuterService().registerAction((IAction) i.next());
		}
	}

	public void deactivate() {
		// deregister all actions from the outer service
		for (Iterator i = fCommandIdToActionMap.values().iterator(); i.hasNext();) {
			getOuterService().unregisterAction((IAction) i.next());
		}
	}

	/**
	 * Returns the outer key binding service.
	 */
	private IKeyBindingService getOuterService() {
		return fSite.getMultiPageEditor().getSite().getKeyBindingService();
	}

	public String[] getScopes() {
		return (String[]) fScopes.clone();
	}

	public void setScopes(String[] scopes) {
		if (scopes == null || scopes.length < 1)
			throw new IllegalArgumentException();
		for (int i = 0; i < scopes.length; i++)
			if (scopes[i] == null)
				throw new IllegalArgumentException();
		fScopes = (String[]) scopes.clone();
		if (isActive())
			getOuterService().setScopes(scopes);
	}

	public void registerAction(IAction action) {
		// remember the registered action and forward to the outer service 
		// if the corresponding nested editor is active
		String command = action.getActionDefinitionId();
		if (command != null) {
			fCommandIdToActionMap.put(command, action);
			if (isActive())
				getOuterService().registerAction(action);
		}
	}

	public void unregisterAction(IAction action) {
		// forget the registered action and forward to the outer service 
		// if the corresponding nested editor is active
		String command = action.getActionDefinitionId();
		if (command != null) {
			fCommandIdToActionMap.remove(command);
			if (isActive())
				getOuterService().unregisterAction(action);
		}
	}

	public String getActiveAcceleratorScopeId() {
		return getScopes()[0];
	}

	public void setActiveAcceleratorScopeId(String scopeId) {
		setScopes(new String[] { scopeId });
	}

	public boolean processKey(KeyEvent event) {
		return false;
	}

	public void enable(boolean enable) {
	}
}
