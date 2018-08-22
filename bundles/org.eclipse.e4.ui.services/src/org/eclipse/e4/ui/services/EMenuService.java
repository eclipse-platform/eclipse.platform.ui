/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.services;

/**
 * Provide for management of different menus.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.1
 */
public interface EMenuService {

	/**
	 * Create a menu for this control and hook it up with the MPopupMenu.
	 *
	 * @param parent
	 *            The parent for the context menu. A Control in SWT.
	 * @param menuId
	 *            the ID of the menu to use
	 * @return <code>true</code> if registration succeeded else <code>false</code>
	 */
	boolean registerContextMenu(Object parent, String menuId);

}
