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
 * 
 * @see WorkbenchAdviser#fillActionBars
 * @since 3.0
 */
public interface IActionBarConfigurer {
	/**
	 * Returns the menu manager for the main menu bar of the workbench window.
	 * <p>
	 * Note that every workbench window has a menu bar, even though it may not
	 * be showing.
	 * </p>
	 * 
	 * @return the menu manager
	 */
	public IMenuManager getMenuManager();
		
	/**
	 * Adds a tool bar item with the given id to the tool bar of the workbench
	 * window. The new tool bar item is added after any existing ones.
	 * <p>
	 * Note that every workbench window has a tool bar, even though it may not
	 * be showing.
	 * </p>
	 * 
	 * @param id the id assigned to this tool bar
	 * @return the tool bar manager for the new tool bar item
	 */
	public IToolBarManager addToolBar(String id);
	
	/**
	 * Removes the tool bar item with the given id from the tool bar of the
	 * workbench window. Ignored if there is no tool bar item with the given id. 
	 * <p>
	 * Note that every workbench window has a tool bar, even though it may not
	 * be showing.
	 * </p>
	 * 
	 * @param id the tool bar id
	 */
	public void removeToolBar(String id);
	
	/**
	 * Returns the tool bar manager for the tool bar item with the given id
	 * to the tool bar of the workbench window. The new tool bar item is added
	 * after any existing ones.
	 * <p>
	 * Note that every workbench window has a tool bar, even though it may not
	 * be showing.
	 * </p>
	 * 
	 * @param id the id of the tool bar item
	 * @return the tool bar manager for the tool bar item with the given id
	 */
	public IToolBarManager getToolBar(String id);

	/**
	 * Adds a group to the tool bar of the workbench window. The new group is 
	 * added after any existing contributions to the tool bar.
	 * <p>
	 * Note that every workbench window has a tool bar, even though it may not
	 * be showing.
	 * </p>
	 *
	 * @param toolBarMgr the tool bar manager to add the group to 
	 * @param id the unique group identifier
	 * @param asSeperator whether the group should have a seperator
	 */
	public void addToolbarGroup(IToolBarManager toolBarMgr, String id, boolean asSeperator);
	
	/**
	 * Register the action as a global action with the workbench
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
	 * Adds a menu item to the tool bar of the workbench menu.
	 * <p>
	 * Note that every workbench window has a tool bar, even though it may not
	 * be showing.
	 * </p>
	 * 
	 * @param menuItem the action contribution item to add to the menu
	 */
	public void addToToolBarMenu(ActionContributionItem menuItem);
	
	/**
	 * Adds the special editor tool bar group to the tool bar of the workbench
	 * window. The new tool bar item is added after any existing ones. The id
	 * of editor tool bar item is always 
	 * {@link EDITOR_TOOLBAR_ID EDITOR_TOOLBAR_ID}, and consists of a canned
	 * arrangement of buttons pre-bound to editor-specific commands.
	 * <p>
	 * Note that every workbench window has a tool bar, even though it may not
	 * be showing.
	 * </p>
	 * 
	 * @return the tool bar manager for the new tool bar item
	 * @issue where is EDITOR_TOOLBAR_ID defined?
	 * @see EDITOR_TOOLBAR_ID;
	 */
	public void addEditorToolbarGroup();
	
	/**
	 * Returns the status line manager of the workbench window.
	 * <p>
	 * Note that every workbench window has a status line, even though it may
	 * not be showing.
	 * </p>
	 * 
	 * @return the status line manager
	 */
	public IStatusLineManager getStatusLineManager();
}