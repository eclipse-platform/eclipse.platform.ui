package org.eclipse.e4.ui.workbench.swt.modeling;

import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.swt.widgets.Menu;

/**
 * Provide for management of different menus.
 */
public interface EMenuService {

	/**
	 * Hook up this menu with the context menu in the model.
	 * 
	 * @param menu
	 *            the SWT menu
	 * @param menuId
	 *            the ID of the menu to use
	 */
	MPopupMenu registerContextMenu(Menu menu, String menuId);

}
