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
package org.eclipse.jface.action;

/**
 * The <code>ICoolBarManager</code> interface provides protocol for managing
 * contributions to a cool bar.
 * An <code>ICoolBarManager</code> implements the IContributionManager Interface and accepts
 * any objects implementing the <code>IContributionItem</code> interface. In order for the 
 * <code>IContributionItem</code> objects to be filled in a cool bar widget they must provide
 * a <code>fillCoolBar(CoolBar, int)</code>.
 * <p>
 * This interface is internal to the framework; it should not be implemented outside
 * the framework.
 * </p>
 * <p>
 * This package provides a concrete cool bar manager implementation,
 * {@link CoolBarManager <code>CoolBarManager</code>}.
 * </p>
 * <p>
 * Also:
 * {@see <code>ToolBarContributionItem</code>
 * </p>
 */
public interface ICoolBarManager extends IContributionManager {
	
	/**
	 * Property name of a cool item's size (value <code>"size"</code>).
	 */
	public static final String SIZE = "size";
	
	/**
	 * Property name of a cool item's visiblity (value <code>"visibility"</code>)..
	 */
	public static final String VISIBILITY = "visibility";
	
	/**
	 * A convineince method to add a tool bar as a contribution item to the cool bar manager.
	 * Equivalent to add(new ToolBarContributionManager(toolBarManager))
	 * @param toolBarManager the tool bar manager to be added 
	 * 
	 * @see #org.eclipse.jface.action.ToolBarContributionItem
	 */
	public void add(IToolBarManager toolBarManager);
	
	/**
	 * Returns the style of the cool bar widget.
	 * @return integer indicating the style of the cool bar
	 */
	public int getStyle();
	
	/**
	 * Returns whether the coolbar widget is locked.
	 * @return <code>true</code> if cool item layout is locked, <code>false</code> otherwise
	 */
	public boolean getLockLayout();
	
	/**
	 * Locks or unlocks the layout of the cool bar widget. Once the cool bar is locked, cool 
	 * items cannot be repositioned by the user.
	 * @param value <code>true</code> to lock the coolbar, <code>false</code> to unlock.
	 */
	public void setLockLayout(boolean value);
	
	/**
	 * Returns the context menu used by the cool bar manager. This context menu is used globally
	 * throughtout the cool bar manager unless a cool item provides its own.
	 * @return the context menu manager or <code>null</code> if no context menu is defined.
	 * 
	 * @see #setContextMenuManager(IMenuManager)
	 */
	public IMenuManager getContextMenuManager();
	
	/**
	 * Replaces the context menu of the cool bar manager with the given menu manager.  
	 * @param contextMenuManager The contextMenuManager to set.
	 *
	 * @see #getContextMenuManager()
	 */
	public void setContextMenuManager(IMenuManager menuManager);
	
}
