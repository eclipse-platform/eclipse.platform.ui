/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.part.IPageSite;

/**
 * An interface that allows for participation in the creation of an IOConsolePage
 * and for defining console specific additions to the console view and page's menus.
 * 
 * @since 3.1
 */
public interface IConsolePageParticipant extends IAdaptable {
    /**
     * Called during Page initialization. Marks the start of the 
     * Page Participant's lifecycle.
     */
    public void init(IPageSite site, IConsole console);
    
    /**
     * Disposed the Page Participant
     */
    public void dispose();
    
    /**
     * Called during configuration of the context menu. Gives the 
     * Page Participant a chance to add actions to the context 
     * menu
     * 
     * @param menu The context menu's Menu Manager
     */
    public void contextMenuAboutToShow(IMenuManager menu);
    
    /**
     * Called during configuration of the tool bar. Allows the Page
     * Participant to add actions to the tool bar.
     * 
     * @param mgr The tool bar manager.
     */
    public void configureToolBar(IToolBarManager mgr);
}
