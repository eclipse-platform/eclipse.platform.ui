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
