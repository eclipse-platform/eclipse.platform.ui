/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
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
	 * The resource navigator.
	 */
	protected IResourceNavigator navigator;
	
	/**
	 * Constructs a new navigator action group and creates its actions.
	 * 
	 * @param navigator the resource navigator
	 */
	public ResourceNavigatorActionGroup(IResourceNavigator navigator) {
		this.navigator = navigator;
		makeActions();
	}
	
	/**
	 * Returns the resource navigator.
	 */
	public IResourceNavigator getNavigator() {
		return navigator;
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
	 * 
	 * @deprecated navigator actions are registered with KeyBindingService.
	 * 	There is no need to invoke actions manually and this is no longer 
	 * 	supported. API will be removed in the next release (2.1). 
 	 */
	public void handleKeyPressed(KeyEvent event) {
	}

}
