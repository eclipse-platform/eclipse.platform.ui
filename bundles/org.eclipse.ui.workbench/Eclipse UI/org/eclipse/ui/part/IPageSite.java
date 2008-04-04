/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchSite;

/**
 * The primary interface between a page and the outside world.
 * <p>
 * The workbench exposes its implemention of page sites via this interface,
 * which is not intended to be implemented or extended by clients.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
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
    public void registerContextMenu(String menuId, MenuManager menuManager,
            ISelectionProvider selectionProvider);

    /**
     * Returns the action bars for this page site.
     * Pages have exclusive use of their site's action bars.
     *
     * @return the action bars
     */
    public IActionBars getActionBars();
}
