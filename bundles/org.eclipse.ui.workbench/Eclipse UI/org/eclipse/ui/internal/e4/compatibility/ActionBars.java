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

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.services.IServiceLocator;

public class ActionBars extends SubActionBars {

	private IToolBarManager toolbarManager;

	private IMenuManager menuManager;

	private MPart part;

	public ActionBars(final IActionBars parent, final IServiceLocator serviceLocator, MPart part) {
		super(parent, serviceLocator);
		this.part = part;
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
	 * @see org.eclipse.ui.IActionBars#getToolBarManager()
	 */
	public IToolBarManager getToolBarManager() {
		if (toolbarManager == null) {
			toolbarManager = new ToolBarManager(null);
		}
		return toolbarManager;
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
		if (toolbarManager != null) {
			System.err.println("update toolbar manager for " + part.getElementId()); //$NON-NLS-1$
			getToolBarManager().update(false);
		}
	}

}
