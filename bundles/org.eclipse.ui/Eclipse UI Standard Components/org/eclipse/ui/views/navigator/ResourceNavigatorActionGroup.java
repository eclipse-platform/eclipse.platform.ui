package org.eclipse.ui.views.navigator;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/

import org.eclipse.swt.events.KeyEvent;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.actions.ActionGroup;

/**
 * This is the action group for all the resource navigator actions.
 * It delegates to several subgroups for most of the actions.
 * 
 * @see GotoActionGroup
 * @see OpenActionGroup
 * @see RefactorActionGroup
 * @see SortAndFilterActionGroup
 * @see WorkspaceActionGroup
 * 
 * @since 2.0
 */
public abstract class ResourceNavigatorActionGroup extends ActionGroup {

	/**
	 * The navigator.
	 */
	protected IResourceNavigator navigator;
	
	/**
	 * Constructs a new navigator action group and creates its actions.
	 */
	public ResourceNavigatorActionGroup(IResourceNavigator navigator) {
		this.navigator = navigator;
		makeActions();
	}
	
	/**
	 * Makes the actions contained in this action group.
	 */
	protected abstract void makeActions();
	
	/**
	 * Runs the default action in the group.
	 * Does nothing by default.
	 * 
	 * @param selection the current selection
	 */
	public void runDefaultAction(IStructuredSelection selection) {
	}

	/**
 	 * Handles a key pressed event by invoking the appropriate action.
	 * Does nothing by default.
 	 */
	public void handleKeyPressed(KeyEvent event) {
	}

}
