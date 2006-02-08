/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.application;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;

/**
 * Interface providing special access for configuring the action bars
 * of a workbench window.
 * <p>
 * Note that these objects are only available to the main application
 * (the plug-in that creates and owns the workbench).
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see org.eclipse.ui.application.WorkbenchAdvisor#fillActionBars
 * @since 3.0
 */
public interface IActionBarConfigurer {
    
    /**
     * Returns the workbench window configurer for the window
     * containing this configurer's action bars. 
     * 
     * @return the workbench window configurer
     * @since 3.1
     */
    public IWorkbenchWindowConfigurer getWindowConfigurer();
    
    
    /**
     * Returns the menu manager for the main menu bar of a workbench window.
     * 
     * @return the menu manager
     */
    public IMenuManager getMenuManager();

    /**
     * Returns the status line manager of a workbench window.
     * 
     * @return the status line manager
     */
    public IStatusLineManager getStatusLineManager();

    /**
     * Returns the cool bar manager of the workbench window.
     * 
     * @return the cool bar manager
     */
    public ICoolBarManager getCoolBarManager();
    
    /**
     * Creates a tool bar manager for the workbench window's tool bar.
     * The action bar advisor should use this factory method rather than
     * creating a <code>ToolBarManager</code> directly. 
	 *
     * @return the tool bar manager
     * @since 3.2
     */
    public IToolBarManager createToolBarManager();
        
    /**
     * Creates a toolbar contribution item for the window's tool bar.
     * The action bar advisor should use this factory method rather than
     * creating a <code>ToolBarContributionItem</code> directly. 
	 * 
     * @param toolBarManager a tool bar manager for the workbench window's tool bar
     * @param id the id of the contribution
     * @return the contribution item
	 * @since 3.2
     */
    public IContributionItem createToolBarContributionItem(IToolBarManager toolBarManager, String id);

    /**
     * Register the action as a global action with a workbench
     * window.
     * <p>
     * For a workbench retarget action 
     * ({@link org.eclipse.ui.actions.RetargetAction RetargetAction})
     * to work, it must be registered.
     * You should also register actions that will participate
     * in custom key bindings.
     * </p>
     *  
     * @param action the global action
     * @see org.eclipse.ui.actions.RetargetAction
     */
    public void registerGlobalAction(IAction action);

}
