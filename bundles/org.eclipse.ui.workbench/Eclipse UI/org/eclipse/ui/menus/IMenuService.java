/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.menus;

import org.eclipse.jface.menus.MenuElement;
import org.eclipse.jface.menus.SGroup;
import org.eclipse.jface.menus.SItem;
import org.eclipse.jface.menus.SMenu;
import org.eclipse.jface.menus.SWidget;

/**
 * <p>
 * Provides services related to the menu architecture within the workbench. This
 * service can be used to access the set of menu, tool bar and status line
 * contributions. It can also be used to contribute additional items to the
 * menu, tool bar and status line.
 * </p>
 * <p>
 * This interface should not be implemented or extended by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public interface IMenuService {

	public static final int TYPE_MENU = 0;

	public static final int TYPE_GROUP = 1;

	public static final int TYPE_ITEM = 2;

	public static final int TYPE_WIDGET = 3;

	/**
	 * Retrieves the group with the given identifier. If no such group exists,
	 * then an undefined group is created and returned. A group is a logical
	 * grouping of items and widgets within a menu.
	 * 
	 * @param groupId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A group with the given identifier, either defined or undefined.
	 */
	public SGroup getGroup(String groupId);

	/**
	 * Retrieves the item with the given identifier. If no such item exists,
	 * then an undefined item is created and returned. An item is a single entry
	 * in a menu, tool bar or status line.
	 * 
	 * @param itemId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return An item with the given identifier, either defined or undefined.
	 */
	public SItem getItem(String itemId);

	/**
	 * Retrieves the menu with the given identifier. If no such menu exists,
	 * then an undefined group is created and returned. A menu is either a
	 * top-level menu, a context menu, a cool bar or a tool bar.
	 * 
	 * @param menuId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A menu with the given identifier, either defined or undefined.
	 */
	public SMenu getMenu(String menuId);

	/**
	 * Retrieves the widget with the given identifier. If no such widget exists,
	 * then an undefined widget is created and returned. A widget is a custom
	 * contribution into a menu. This allows the plug-in to draw the widgets as
	 * desired.
	 * 
	 * @param widgetId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A widget with the given identifier, either defined or undefined.
	 */
	public SWidget getWidget(String widgetId);

	/**
	 * Retrieves the menu element of the given type with the given identifier.
	 * If no such menu element exists, then an undefined menu element of the
	 * given type is created and returned.
	 * 
	 * @param elementId
	 *            The identifier to find; must not be <code>null</code>.
	 * @param type
	 *            The type of the menu element to retrieve. This must be one of
	 *            <code>TYPE_MENU</code>, <code>TYPE_GROUP</code>,
	 *            <code>TYPE_ITEM</code>, or <code>TYPE_WIDGET</code>
	 * @return A menu element of the given type with the given identifier,
	 *         either defined or undefined.
	 */
	public MenuElement getMenuElement(String elementId, int type);

	/**
	 * <p>
	 * Reads the menu information from the registry and the preferences. This
	 * will overwrite any of the existing information in the menu service. This
	 * method is intended to be called during start-up. When this method
	 * completes, this menu service will reflect the current state of the
	 * registry and preference store.
	 * </p>
	 * <p>
	 * This will also attach listeners that will monitor changes to the registry
	 * and preference store and update appropriately.
	 * </p>
	 */
	public void readRegistry();
}
