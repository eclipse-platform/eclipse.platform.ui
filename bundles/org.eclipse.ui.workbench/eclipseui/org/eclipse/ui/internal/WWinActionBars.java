/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.services.IServiceLocator;

public class WWinActionBars implements IActionBars2 {
	private WorkbenchWindow window;

	/**
	 * PerspActionBars constructor comment.
	 */
	public WWinActionBars(WorkbenchWindow window) {
		super();
		this.window = window;
	}

	/**
	 * Clears the global action handler list.
	 */
	@Override
	public void clearGlobalActionHandlers() {
	}

	/**
	 * Returns the cool bar manager.
	 */
	@Override
	public ICoolBarManager getCoolBarManager() {
		return window.getCoolBarManager2();
	}

	/**
	 * Get the handler for a window action.
	 *
	 * @param actionID an action ID declared in the registry
	 * @return an action handler which implements the action ID, or
	 *         <code>null</code> if none is registered.
	 */
	@Override
	public IAction getGlobalActionHandler(String actionID) {
		return null;
	}

	/**
	 * Returns the menu manager. If items are added or removed from the manager be
	 * sure to call <code>updateActionBars</code>.
	 *
	 * @return the menu manager
	 */
	@Override
	public IMenuManager getMenuManager() {
		return window.getMenuManager();
	}

	@Override
	public final IServiceLocator getServiceLocator() {
		return window;
	}

	/**
	 * Returns the status line manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>.
	 *
	 * @return the status line manager
	 */
	@Override
	public IStatusLineManager getStatusLineManager() {
		return window.getStatusLineManager();
	}

	/**
	 * Returns the tool bar manager.
	 */
	@Override
	public IToolBarManager getToolBarManager() {
		// This should never be called
		Assert.isTrue(false);
		return null;
	}

	/**
	 * Add a handler for a window action.
	 *
	 * The standard action ID's for the workbench are defined in
	 * <code>IWorkbenchActionConstants</code>.
	 *
	 * @see IWorkbenchActionConstants
	 *
	 * @param actionID an action ID declared in the registry
	 * @param handler  an action which implements the action ID. <code>null</code>
	 *                 may be passed to deregister a handler.
	 */
	@Override
	public void setGlobalActionHandler(String actionID, IAction handler) {
	}

	/**
	 * Commits all UI changes. This should be called after additions or subtractions
	 * have been made to a menu, status line, or toolbar.
	 */
	@Override
	public void updateActionBars() {
		window.updateActionBars();
	}
}
