package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The primary interface between a page and the outside world.
 * <p>
 * The workbench exposes its implemention of page sites via this interface,
 * which is not intended to be implemented or extended by clients.
 * </p>
 */

public interface IPageSite extends IWorkbenchSite {
	/**
	 * Registers a pop-up menu with a particular id for extension.
	 * <p>
	 * Within the workbench one plug-in may extend the pop-up menus for a view
	 * or editor within another plug-in.  In order to be eligible for extension,
	 * the menu must be registered by calling <code>registerContextMenu</code>.
	 * Once this has been done the workbench will automatically insert any action 
	 * extensions which exist.
	 * </p>
	 * <p>
	 * A unique menu id must be provided for each registered menu. This id should
	 * be published in the Javadoc for the page.
	 * </p>
	 * <p>
	 * Any pop-up menu which is registered with the workbench should also define a  
	 * <code>GroupMarker</code> in the registered menu with id 
	 * <code>IWorkbenchActionConstants.MB_ADDITIONS</code>.  Other plug-ins will use this 
	 * group as a reference point for insertion.  The marker should be defined at an 
	 * appropriate location within the menu for insertion.  
	 * </p>
	 *
	 * @param menuId the menu id
	 * @param menuManager the menu manager
	 * @param selectionProvider the selection provider
	 */
	public void registerContextMenu(
		String menuId,
		MenuManager menuManager,
		ISelectionProvider selectionProvider);
	/**
	 * Returns the action bars for this page site.
	 * Pages have exclusive use of their site's action bars.
	 *
	 * @return the action bars
	 */
	public IActionBars getActionBars();
}
