/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.application;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;

/**
 * Interface providing special access for configuring the action bars
 * of a workbench window.
 * <p>
 * Note that these objects are only available to the main application
 * (the plug-in that creates and owns the workbench).
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see org.eclipse.ui.application.WorkbenchAdvisor#fillActionBars
 * @since 3.0
 */
public interface IActionBarConfigurer {
	/**
	 * Returns the menu manager for the main menu bar of a workbench window.
	 * 
	 * @return the menu manager
	 */
	public IMenuManager getMenuManager();
		
	/**
	 * Adds a tool bar with the given id to the tool bars of a workbench
	 * window. The new tool bar is added after any existing ones.
	 * 
	 * @param id the id assigned to this tool bar
	 * @return the tool bar manager for the new tool bar
	 */
	public IToolBarManager addToolBar(String id);
	
	/**
	 * Removes the tool bar with the given id from the tool bar of a
	 * workbench window. Ignored if there is no tool bar with the given id. 
	 * 
	 * @param id the tool bar id
	 */
	public void removeToolBar(String id);
	
	/**
	 * Returns the tool bar manager for the tool bar with the given id,
	 * or <code>null</code> if there is no such tool bar.
	 * 
	 * @param id the id of the tool bar item
	 * @return the tool bar manager or <code>null</code>
	 */
	public IToolBarManager getToolBar(String id);

	/**
	 * Adds a group to the tool bar of a workbench window. The new group is 
	 * added after any existing contributions to the tool bar.
	 *
	 * @param toolBarMgr the tool bar manager to add the group to 
	 * @param id the unique group identifier
	 * @param asSeparator whether the group should have a seperator
	 */
	public void addToolBarGroup(IToolBarManager toolBarMgr, String id, boolean asSeparator);
	
	/**
	 * Register the action as a global action with a workbench
	 * window.
	 * <p>
	 * For a workbench retarget action 
	 * ({@link org.eclipse.ui.actions.RetargetAction RetargetAction})
	 * to work, it must be registered.
	 * You should also register actions that will participate
	 * in custom key bindings.
	 * </p>
	 *  
	 * @param action the global action
	 * @see org.eclipse.ui.actions.RetargetAction
	 */
	public void registerGlobalAction(IAction action);
	
	/**
	 * Adds a menu item to the context menu for the tool bars of a workbench window.
	 * 
	 * @param menuItem the action contribution item to add to the menu
	 */
	public void addToToolBarMenu(ActionContributionItem menuItem);
	
	/**
	 * Adds the special editor tool bar group to the tool bar of a workbench
	 * window. The new tool bar item is added after any existing ones. The id
	 * of editor tool bar item is always 
	 * {@link EDITOR_TOOLBAR_ID EDITOR_TOOLBAR_ID}, and consists of a canned
	 * arrangement of buttons pre-bound to editor-specific commands.
	 * 
	 * @return the tool bar manager for the new tool bar item
	 * @issue where is EDITOR_TOOLBAR_ID defined?
	 */
	public void addEditorToolBarGroup();
	
	/**
	 * Returns the status line manager of a workbench window.
	 * 
	 * @return the status line manager
	 */
	public IStatusLineManager getStatusLineManager();
}