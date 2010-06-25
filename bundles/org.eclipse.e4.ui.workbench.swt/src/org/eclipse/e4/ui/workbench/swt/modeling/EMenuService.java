/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.modeling;

import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;

/**
 * Provide for management of different menus.
 */
public interface EMenuService {

	/**
	 * Create a menu for this control and hook it up with the MPopupMenu.
	 * 
	 * @param parent
	 *            The parent for the context menu. A Control in SWT.
	 * @param menuId
	 *            the ID of the menu to use
	 */
	MPopupMenu registerContextMenu(Object parent, String menuId);

}
