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


import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.SubActionBars;

/**
 * An editor container manages the services for an editor.
 */
public class ViewActionBars extends SubActionBars {
	private ViewPane pane;
	/**
	 * ViewActionBars constructor comment.
	 */
	public ViewActionBars(IActionBars parent, ViewPane pane) {
		super(parent);
		this.pane = pane;
	}
	/**
	 * Returns the menu manager.  If items are added or
	 * removed from the manager be sure to call <code>updateActionBars</code>.
	 *
	 * @return the menu manager
	 */
	public IMenuManager getMenuManager() {
		return pane.getMenuManager();
	}
	/**
	 * Returns the tool bar manager.  If items are added or
	 * removed from the manager be sure to call <code>updateActionBars</code>.
	 *
	 * @return the tool bar manager
	 */
	public IToolBarManager getToolBarManager() {
		return pane.getToolBarManager();
	}
	/**
	 * Commits all UI changes.  This should be called
	 * after additions or subtractions have been made to a 
	 * menu, status line, or toolbar.
	 */
	public void updateActionBars() {
		pane.updateActionBars();
		getStatusLineManager().update(false);
		fireActionHandlersChanged();
	}
}
