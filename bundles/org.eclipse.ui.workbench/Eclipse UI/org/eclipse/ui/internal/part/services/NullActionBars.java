/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars2;

/**
 * Null implementation of the IActionBars2 interface
 * 
 * @since 3.1
 */
public class NullActionBars implements IActionBars2 {

	private NullCoolBarManager coolbarManager = new NullCoolBarManager();
	private NullMenuManager menuManager = new NullMenuManager();
	private NullToolBarManager toolbarManager = new NullToolBarManager();
	private NullStatusLineManager statusLineManager = new NullStatusLineManager();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionBars#clearGlobalActionHandlers()
	 */
	public void clearGlobalActionHandlers() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionBars2#getCoolBarManager()
	 */
	public ICoolBarManager getCoolBarManager() {
		return coolbarManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionBars#getGlobalActionHandler(java.lang.String)
	 */
	public IAction getGlobalActionHandler(String actionId) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionBars#getMenuManager()
	 */
	public IMenuManager getMenuManager() {
		return menuManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionBars#getStatusLineManager()
	 */
	public IStatusLineManager getStatusLineManager() {
		return statusLineManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionBars#getToolBarManager()
	 */
	public IToolBarManager getToolBarManager() {
		return toolbarManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionBars#setGlobalActionHandler(java.lang.String, org.eclipse.jface.action.IAction)
	 */
	public void setGlobalActionHandler(String actionId, IAction handler) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionBars#updateActionBars()
	 */
	public void updateActionBars() {
	}

}
