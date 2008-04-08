/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.application;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Public base class for configuring the action bars of a workbench window.
 * <p>
 * An application should declare a subclass of <code>ActionBarAdvisor</code>
 * and override methods to configure a window's action bars to suit the needs of the
 * particular application.
 * </p>
 * <p>
 * The following advisor methods are called at strategic points in the
 * workbench's lifecycle (all occur within the dynamic scope of the call
 * to {@link PlatformUI#createAndRunWorkbench PlatformUI.createAndRunWorkbench}):
 * <ul>
 * <li><code>fillActionBars</code> - called after <code>WorkbenchWindowAdvisor.preWindowOpen</code>
 * to configure a window's action bars</li>
 * </ul>
 * </p>
 * 
 * @see WorkbenchWindowAdvisor#createActionBarAdvisor(IActionBarConfigurer)
 * 
 * @since 3.1
 */
public class ActionBarAdvisor {

    /**
     * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
     * operation is not filling the action bars of an actual workbench window,
     * but rather a proxy (used for perspective customization).
     */
    public static final int FILL_PROXY = 0x01;

    /**
     * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
     * operation is supposed to fill (or describe) the workbench window's menu
     * bar.
     */
    public static final int FILL_MENU_BAR = 0x02;

    /**
     * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
     * operation is supposed to fill (or describe) the workbench window's cool
     * bar.
     */
    public static final int FILL_COOL_BAR = 0x04;

    /**
     * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
     * operation is supposed to fill (or describe) the workbench window's status
     * line.
     */
    public static final int FILL_STATUS_LINE = 0x08;

    
    private IActionBarConfigurer actionBarConfigurer;
    
    private Map actions = new HashMap();
    
    /**
     * Creates a new action bar advisor to configure a workbench
     * window's action bars via the given action bar configurer.
     * 
     * @param configurer the action bar configurer
     */
    public ActionBarAdvisor(IActionBarConfigurer configurer) {
        Assert.isNotNull(configurer);
        actionBarConfigurer = configurer;
    }
    
    /**
     * Returns the action bar configurer.
     * 
     * @return the action bar configurer
     */
    protected IActionBarConfigurer getActionBarConfigurer() {
        return actionBarConfigurer;
    }

    /**
     * Configures the action bars using the given action bar configurer.
     * Under normal circumstances, <code>flags</code> does not include
     * <code>FILL_PROXY</code>, meaning this is a request to fill the action
     * bars of the corresponding workbench window; the
     * remaining flags indicate which combination of
     * the menu bar (<code>FILL_MENU_BAR</code>),
     * the tool bar (<code>FILL_COOL_BAR</code>),
     * and the status line (<code>FILL_STATUS_LINE</code>) are to be filled.
     * <p>
     * If <code>flags</code> does include <code>FILL_PROXY</code>, then this
     * is a request to describe the actions bars of the given workbench window
     * (which will already have been filled);
     * again, the remaining flags indicate which combination of the menu bar,
     * the tool bar, and the status line are to be described.
     * The actions included in the proxy action bars can be the same instances
     * as in the actual window's action bars. Calling <code>ActionFactory</code>
     * to create new action instances is not recommended, because these
     * actions internally register listeners with the window and there is no
     * opportunity to dispose of these actions.
     * </p>
     * <p>
     * This method is called just after {@link WorkbenchWindowAdvisor#preWindowOpen()}.
     * Clients must not call this method directly (although super calls are okay).
     * The default implementation calls <code>makeActions</code> if
     * <code>FILL_PROXY</code> is specified, then calls <code>fillMenuBar</code>, 
     * <code>fillCoolBar</code>, and <code>fillStatusLine</code>
     * if the corresponding flags are specified.
     * </p>
     * <p> 
     * Subclasses may override, but it is recommended that they override the
     * methods mentioned above instead.
     * </p>
     * 
     * @param flags bit mask composed from the constants
     * {@link #FILL_MENU_BAR FILL_MENU_BAR},
     * {@link #FILL_COOL_BAR FILL_COOL_BAR},
     * {@link #FILL_STATUS_LINE FILL_STATUS_LINE},
     * and {@link #FILL_PROXY FILL_PROXY}
     */
    public void fillActionBars(int flags) {
        if ((flags & FILL_PROXY) == 0) {
            makeActions(actionBarConfigurer.getWindowConfigurer().getWindow());
        }
        if ((flags & FILL_MENU_BAR) != 0) {
            fillMenuBar(actionBarConfigurer.getMenuManager());
        }
        if ((flags & FILL_COOL_BAR) != 0) {
            fillCoolBar(actionBarConfigurer.getCoolBarManager());
        }
        if ((flags & FILL_STATUS_LINE) != 0) {
            fillStatusLine(actionBarConfigurer.getStatusLineManager());
        }
    }
        
    /**
     * Instantiates the actions used in the fill methods.
     * Use {@link #register(IAction)} to register the action with the key binding service
     * and add it to the list of actions to be disposed when the window is closed.
     * 
     * @param window the window containing the action bars
     */
    protected void makeActions(IWorkbenchWindow window) {
        // do nothing
    }

    /**
     * Registers the given action with the key binding service 
     * (by calling {@link IActionBarConfigurer#registerGlobalAction(IAction)}),
     * and adds it to the list of actions to be disposed when the window is closed.
     * <p>
     * In order to participate in key bindings, the action must have an action
     * definition id (aka command id), and a corresponding command extension.
     * See the <code>org.eclipse.ui.commands</code> extension point documentation
     * for more details. 
     * </p>
     * 
     * @param action the action to register, this cannot be <code>null</code>
     * 
     * @see IAction#setActionDefinitionId(String)
     * @see #disposeAction(IAction)
     */
    protected void register(IAction action) {
    	Assert.isNotNull(action, "Action must not be null"); //$NON-NLS-1$
        String id = action.getId();
        Assert.isNotNull(id, "Action must not have null id"); //$NON-NLS-1$
        getActionBarConfigurer().registerGlobalAction(action);
        actions.put(id, action);
    }
    
    /**
     * Returns the action with the given id, or <code>null</code> if not found.
     * 
     * @param id the action id
     * @return the action with the given id, or <code>null</code> if not found
     * @see IAction#getId()
     */
    protected IAction getAction(String id) {
        return (IAction) actions.get(id);
    }
    
    /**
     * Fills the menu bar with the main menus for the window.
     * <p>
     * The default implementation does nothing.
     * Subclasses may override.
     * </p>
     * 
     * @param menuBar the menu manager for the menu bar
     */
    protected void fillMenuBar(IMenuManager menuBar) {
        // do nothing
    }
    
    /**
     * Fills the cool bar with the main toolbars for the window.
     * <p>
     * The default implementation does nothing.
     * Subclasses may override.
     * </p>
     * 
     * @param coolBar the cool bar manager
     */
    protected void fillCoolBar(ICoolBarManager coolBar) {
        // do nothing
    }
    
    /**
     * Fills the status line with the main status line contributions 
     * for the window.
     * <p>
     * The default implementation does nothing.
     * Subclasses may override.
     * </p>
     * 
     * @param statusLine the status line manager
     */
    protected void fillStatusLine(IStatusLineManager statusLine) {
        // do nothing
    }    

    /**
     * Returns whether the menu with the given id is an application menu of the
     * given window. This is used during OLE "in place" editing.  Application
     * menus should be preserved during menu merging. All other menus may be
     * removed from the window.
     * <p>
     * The default implementation returns false. Subclasses may override.
     * </p>
     * 
     * @param menuId the menu id
     * @return <code>true</code> for application menus, and <code>false</code>
     * for part-specific menus
     */
    public boolean isApplicationMenu(String menuId) {
        // default: not an application menu
        return false;
    }

    /**
     * Disposes this action bar advisor.
     * Called when the window is being closed.
     * This should dispose any allocated resources and remove any added listeners.
     * <p>
     * The default implementation calls <code>disposeActions()</code>.
     * Subclasses may extend.
     * </p>
     */
    public void dispose() {
        disposeActions();
    }
    
    /**
     * Disposes all actions added via <code>register(IAction)</code>
     * using <code>disposeAction(IAction)</code>.
     */
    protected void disposeActions() {
        for (Iterator i = actions.values().iterator(); i.hasNext();) {
            IAction action = (IAction) i.next();
            disposeAction(action);
        }
        actions.clear();
    }
    
    /**
     * Disposes the given action.
     * <p>
     * The default implementation checks whether the action is an instance
     * of <code>ActionFactory.IWorkbenchAction</code> and calls its 
     * <code>dispose()</code> method if so.
     * Subclasses may extend.
     * </p>
     * 
     * @param action the action to dispose
     */
    protected void disposeAction(IAction action) {
        if (action instanceof ActionFactory.IWorkbenchAction) {
            ((ActionFactory.IWorkbenchAction) action).dispose();
        }
    }
	
	/**
	 * Saves arbitrary application-specific state information
     * for this action bar advisor.
     * <p>
     * The default implementation simply returns an OK status.
     * Subclasses may extend or override.
     * </p>
	 * 
	 * @param memento the memento in which to save the advisor's state
	 * @return a status object indicating whether the save was successful
	 * @since 3.1
	 */
	public IStatus saveState(IMemento memento) {
		return Status.OK_STATUS;
	}
	
	/**
	 * Restores arbitrary application-specific state information
     * for this action bar advisor.
     * <p>
     * The default implementation simply returns an OK status.
     * Subclasses may extend or override.
     * </p>
	 * 
     * @param memento the memento from which to restore the advisor's state
	 * @return a status object indicating whether the restore was successful
	 * @since 3.1
	 */
	public IStatus restoreState(IMemento memento) {
		return Status.OK_STATUS;
	}

}
