/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.util;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.presentations.SystemMenuClose;
import org.eclipse.ui.internal.presentations.SystemMenuMaximize;
import org.eclipse.ui.internal.presentations.SystemMenuMinimize;
import org.eclipse.ui.internal.presentations.SystemMenuMove;
import org.eclipse.ui.internal.presentations.SystemMenuRestore;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * Implements the system context menu that is used by the default presentation. Not 
 * intended to be subclassed by clients.
 * 
 * @since 3.1
 */
public class StandardViewSystemMenu implements ISystemMenu {

    /* package */ MenuManager menuManager = new MenuManager();
    private SystemMenuRestore restore;
    private SystemMenuMove move; 
    private SystemMenuMinimize minimize;
    private SystemMenuMaximize maximize;
    private SystemMenuClose close;
    
    /**
     * Create the standard view menu
     * 
     * @param site the site to associate the view with
     */
    public StandardViewSystemMenu(IStackPresentationSite site) {
        restore = new SystemMenuRestore(site);
        move = new SystemMenuMove(site, getMoveMenuText(), false);
        minimize = new SystemMenuMinimize(site);
        maximize = new SystemMenuMaximize(site);
        close = new SystemMenuClose(site);
        
        { // Initialize system menu
            menuManager.add(new GroupMarker("misc")); //$NON-NLS-1$
            menuManager.add(new GroupMarker("restore")); //$NON-NLS-1$
            menuManager.add(new UpdatingActionContributionItem(restore));

            menuManager.add(move);
            menuManager.add(new GroupMarker("size")); //$NON-NLS-1$
            menuManager.add(new GroupMarker("state")); //$NON-NLS-1$
            menuManager.add(new UpdatingActionContributionItem(minimize));

            menuManager.add(new UpdatingActionContributionItem(maximize));
            menuManager.add(new Separator("close")); //$NON-NLS-1$
            menuManager.add(close);

            site.addSystemActions(menuManager);
        } // End of system menu initialization

    }

    String getMoveMenuText() {
    	return WorkbenchMessages.ViewPane_moveView;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.util.ISystemMenu#show(org.eclipse.swt.graphics.Point, org.eclipse.ui.presentations.IPresentablePart)
     */
    public void show(Control parent, Point displayCoordinates, IPresentablePart currentSelection) {
        restore.update();
        move.setTarget(currentSelection);
        move.update();
        minimize.update();
        maximize.update();
        close.setTarget(currentSelection);
        
        Menu aMenu = menuManager.createContextMenu(parent);
        menuManager.update(true);
        aMenu.setLocation(displayCoordinates.x, displayCoordinates.y);
        aMenu.setVisible(true);
    }
    
    /**
     * Dispose resources associated with this menu
     */
    public void dispose() {
        menuManager.dispose();
        menuManager.removeAll();
    }

}
