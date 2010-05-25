package org.eclipse.e4.ui.workbench.swt.modeling;

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
	void registerContextMenu(Menu menu, String menuId);

}
