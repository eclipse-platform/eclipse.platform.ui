/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.menus;

/**
 * Provides utilities and constants for use with the new menus API.
 * 
 * @since 3.3
 * 
 */
public class MenuUtil {
	/** Main Menu */
	public final static String MAIN_MENU = "menu:org.eclipse.ui.main.menu"; //$NON-NLS-1$
	/** Main ToolBar (CoolBar) */
	public final static String MAIN_TOOLBAR = "toolbar:org.eclipse.ui.main.toolbar"; //$NON-NLS-1$

	/** -Any- Popup Menu */
	public final static String ANY_POPUP = "popup:org.eclipse.ui.popup.any"; //$NON-NLS-1$

	/** Top Left Trim Area */
	public final static String TRIM_COMMAND1 = "toolbar:org.eclipse.ui.trim.command1"; //$NON-NLS-1$
	/** Top Right Trim Area */
	public final static String TRIM_COMMAND2 = "toolbar:org.eclipse.ui.trim.command2"; //$NON-NLS-1$
	/** Left Vertical Trim Area */
	public final static String TRIM_VERTICAL1 = "toolbar:org.eclipse.ui.trim.vertical1"; //$NON-NLS-1$
	/** Right Vertical Trim Area */
	public final static String TRIM_VERTICAL2 = "toolbar:org.eclipse.ui.trim.vertical2"; //$NON-NLS-1$
	/** Bottom (Status) Trim Area */
	public final static String TRIM_STATUS = "toolbar:org.eclipse.ui.trim.status"; //$NON-NLS-1$

	/**
	 * Contributions of targets to this location will be included with the show
	 * in menu.
	 * 
	 * @since 3.4
	 */
	public final static String SHOW_IN_MENU_ID = "popup:org.eclipse.ui.menus.showInMenu"; //$NON-NLS-1$

	/**
	 * @param id
	 *            The menu's id
	 * @return The locator URI for a menu with the given id
	 */
	public static String menuUri(String id) {
		return "menu:" + id; //$NON-NLS-1$
	}

	/**
	 * @param id
	 *            The id of the menu
	 * @param location
	 *            The relative location specifier
	 * @param refId
	 *            The id of the menu element to be relative to
	 * @return A location URI formatted with the given parameters
	 */
	public static String menuAddition(String id, String location, String refId) {
		return menuUri(id) + '?' + location + '=' + refId;
	}

	/**
	 * Convenience method to create a standard menu addition The resulting
	 * string has the format: "menu:[id]?after=additions"
	 * 
	 * @param id
	 *            The id of the root element to contribute to
	 * @return The formatted string
	 */
	public static String menuAddition(String id) {
		return menuAddition(id, "after", "additions"); //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * @param id
	 *            The toolbar's id
	 * @return The lcoation URI for a toolbar with the given id
	 */
	public static String toolbarUri(String id) {
		return "toolbar:" + id; //$NON-NLS-1$
	}

	/**
	 * @param id
	 *            The id of the toolbar
	 * @param location
	 *            The relative location specifier
	 * @param refId
	 *            The id of the toolbar element to be relative to
	 * @return A location URI formatted with the given parameters
	 */
	public static String toolbarAddition(String id, String location,
			String refId) {
		return toolbarUri(id) + '?' + location + '=' + refId;
	}

	/**
	 * Convenience method to create a standard toolbar addition The resulting
	 * string has the format: "toolbar:[id]?after=additions"
	 * 
	 * @param id
	 *            The id of the root element to contribute to
	 * @return The formatted string
	 */
	public static String toolbarAddition(String id) {
		return toolbarAddition(id, "after", "additions"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
