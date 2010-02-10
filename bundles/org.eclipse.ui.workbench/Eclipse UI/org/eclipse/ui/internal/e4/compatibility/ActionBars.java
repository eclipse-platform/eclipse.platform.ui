/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.util.HashMap;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.services.IServiceLocator;

public class ActionBars implements IActionBars {

	private IToolBarManager manager;

	private IMenuManager menuManager;

	private StatusLineManager statusLineManager;

	private MPart part;

	private HashMap<String, IAction> actions = new HashMap<String, IAction>();

	ActionBars(MPart part) {
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#clearGlobalActionHandlers()
	 */
	public void clearGlobalActionHandlers() {
		// TODO compat this really should be clearing registered handlers
		actions.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#getGlobalActionHandler(java.lang.String)
	 */
	public IAction getGlobalActionHandler(String actionId) {
		// FIXME compat getGlobalActionHandler
		return actions.get(actionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#getMenuManager()
	 */
	public IMenuManager getMenuManager() {
		if (menuManager == null) {
			menuManager = new MenuManager();
		}
		return menuManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#getServiceLocator()
	 */
	public IServiceLocator getServiceLocator() {
		// FIXME compat create a delegation implementation for this?
		return new IServiceLocator() {
			public boolean hasService(Class api) {
				return part.getContext().containsKey(api.getName());
			}

			public Object getService(Class api) {
				return part.getContext().get(api.getName());
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#getStatusLineManager()
	 */
	public IStatusLineManager getStatusLineManager() {
		if (statusLineManager == null) {
			statusLineManager = new StatusLineManager();
		}
		return statusLineManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#getToolBarManager()
	 */
	public IToolBarManager getToolBarManager() {
		if (manager == null) {
			MToolBar toolbar = part.getToolbar();
			if (toolbar == null) {
				toolbar = MApplicationFactory.eINSTANCE.createToolBar();
				part.setToolbar(toolbar);
			}

			ToolBar toolBarWidget = (ToolBar) toolbar.getWidget();
			manager = new ToolBarManager(toolBarWidget);
		}
		return manager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#setGlobalActionHandler(java.lang.String,
	 * org.eclipse.jface.action.IAction)
	 */
	public void setGlobalActionHandler(String actionId, IAction handler) {
		// FIXME compat setGlobalActionHandler: needs to actually register
		// handlers
		actions.put(actionId, handler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionBars#updateActionBars()
	 */
	public void updateActionBars() {
		// FIXME compat: updateActionBars : should do someting useful
		getStatusLineManager().update(false);
		getMenuManager().update(false);
		if (manager != null) {
			getToolBarManager().update(false);
		}
	}

}
