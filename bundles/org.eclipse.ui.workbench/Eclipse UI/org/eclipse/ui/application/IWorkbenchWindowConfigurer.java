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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Interface providing special access for configuring workbench windows.
 * <p>
 * Window configurer objects are in 1-1 correspondence with the workbench
 * windows they configure. Clients may use <code>get/setData</code> to
 * associate arbitrary state with the window configurer object.
 * </p>
 * <p>
 * Note that these objects are only available to the main application
 * (the plug-in that creates and owns the workbench).
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see IWorkbenchConfigurer#getWorkbenchWindowConfigurer
 * @see WorkbenchAdviser#preWindowOpen
 * @since 3.0
 */
public interface IWorkbenchWindowConfigurer {
	/**
	 * Returns the underlying workbench window.
	 * 
	 * @return the workbench window
	 */
	public IWorkbenchWindow getWindow();
	
	/**
	 * Returns the workbench configurer.
	 * 
	 * @return the workbench configurer
	 */
	public IWorkbenchConfigurer getWorkbenchConfigurer();
	
	/**
	 * Returns the title of the underlying workbench window.
	 * 
	 * @return the window title
	 */
	public String getTitle();
		
	/**
	 * Sets the title of the underlying workbench window.
	 * <p>
	 * Note that the window can have a title even if the window's title bar
	 * is not visible.
	 * </p>
	 * 
	 * @param title the window title
	 * @see #showTitleBar
	 */
	public void setTitle(String title);
	
	/**
	 * Returns whether the underlying workbench window has a title bar.
	 * <p>
	 * The initial value is controlled by the preference
	 * {@link IWorkbenchPreferences.SHOULD_SHOW_TITLE_BAR SHOULD_SHOW_TITLE_BAR}
	 * </p>
	 * 
	 * @return <code>true</code> for a title bar, and <code>false</code>
	 * for no title bar
	 */
	public boolean getShowTitleBar();

	/**
	 * Sets whether the underlying workbench window has a title bar.
	 * 
	 * @param show <code>true</code> for a title bar, and <code>false</code>
	 * for no title bar
	 */
	public void setShowTitleBar(boolean show);

	/**
	 * Returns whether the underlying workbench window has a title bar.
	 * <p>
	 * The initial value is controlled by the preference
	 * {@link IWorkbenchPreferences.SHOULD_SHOW_MENU_BAR SHOULD_SHOW_MENU_BAR}
	 * </p>
	 * 
	 * @return <code>true</code> for a title bar, and <code>false</code>
	 * for no title bar
	 */
	public boolean getShowMenuBar();

	/**
	 * Sets whether the underlying workbench window has a menu bar.
	 * 
	 * @param show <code>true</code> for a menu bar, and <code>false</code>
	 * for no menu bar
	 */
	public void setShowMenuBar(boolean show);

	/**
	 * Returns whether the underlying workbench window has a tool bar.
	 * <p>
	 * The initial value is controlled by the preference
	 * {@link IWorkbenchPreferences.SHOULD_SHOW_TOOL_BAR SHOULD_SHOW_TOOL_BAR}
	 * </p>
	 * 
	 * @return <code>true</code> for a tool bar, and <code>false</code>
	 * for no tool bar
	 */
	public boolean getShowToolBar();

	/**
	 * Sets whether the underlying workbench window has a tool bar.
	 * 
	 * @param show <code>true</code> for a tool bar, and <code>false</code>
	 * for no tool bar
	 */
	public void setShowToolBar(boolean show);

	/**
	 * Returns whether the underlying workbench window has a shortcut bar.
	 * <p>
	 * The initial value is controlled by the preference
	 * {@link IWorkbenchPreferences.SHOULD_SHOW_SHORTCUT_BAR SHOULD_SHOW_SHORTCUT_BAR}
	 * </p>
	 * 
	 * @return <code>true</code> for a shortcut bar, and <code>false</code>
	 * for no shortcut bar
	 */
	public boolean getShowShortcutBar();

	/**
	 * Sets whether the underlying workbench window has a shortcut bar.
	 * 
	 * @param show <code>true</code> for a shortcut bar, and <code>false</code>
	 * for no shortcut bar
	 */
	public void setShowShortcutBar(boolean show);

	/**
	 * Returns whether the underlying workbench window has a status line.
	 * <p>
	 * The initial value is controlled by the preference
	 * {@link IWorkbenchPreferences.SHOULD_SHOW_STATUS_LINE SHOULD_SHOW_STATUS_LINE}
	 * </p>
	 * 
	 * @return <code>true</code> for a status line, and <code>false</code>
	 * for no status line
	 */
	public boolean getShowStatusLine();

	/**
	 * Sets whether the underlying workbench window has a status line.
	 * 
	 * @param show <code>true</code> for a status line, and <code>false</code>
	 * for no status line
	 */
	public void setShowStatusLine(boolean show);
	
	/**
	 * Returns the data associated with this workbench window at the given key.
	 * 
	 * @param key the key
	 * @return the data, or <code>null</code> if there is no data at the given
	 * key
	 */
	public Object getData(String key);
	
	/**
	 * Sets the data associated with this workbench window at the given key.
	 * 
	 * @param key the key
	 * @param data the data, or <code>null</code> to delete existing data
	 */
	public void setData(String key, Object data);
	
	/**
	 * Returns the menu manager for the main menu bar of the workbench window.
	 * 
	 * @return the menu manager
	 * @issue can this return null if you specify no menu bar? looking at the code, it seems impossible
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
	 * @issue TBD String ADDITIONS = "...";
	 * @issue TBD String EDITOR_TOOLBAR_ID = "...";
	 */
	public IToolBarManager addToolbar(String id);
	
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
	public IToolBarManager getToolbar(String id);

	/**
	 * Adds the special editor tool bar group to the tool bar of the workbench
	 * window. The new tool bar item is added after any existing ones.
	 * <p>
	 * Note that every workbench window has a tool bar, even though it may not
	 * be showing.
	 * </p>
	 * 
	 * @return the tool bar manager for the new tool bar item
	 */
	public void addEditorToolbarGroup();
	
	/**
     * @issue TBD
	 */
	public void addToolbarGroup(String groupId);

	/**
	 * Returns the status line manager of the workbench window.
	 * 
	 * @return the status line manager
	 * @issue can this return null if you specify no menu bar? looking at the code, it seems impossible
	 */
	public IStatusLineManager getStatusLineManager();
}

