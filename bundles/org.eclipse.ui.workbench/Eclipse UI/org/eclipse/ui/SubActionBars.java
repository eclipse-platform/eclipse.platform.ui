/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.internal.CoolItemToolBarManager;

/**
 * Generic implementation of the <code>IActionBars</code> interface.
 */
public class SubActionBars implements IActionBars {
	private IActionBars parent;
	private boolean active = false;
	private Map actionHandlers;
	private SubMenuManager menuMgr;
	private SubStatusLineManager statusLineMgr;
	private SubToolBarManager toolBarMgr;
	private ListenerList propertyChangeListeners = new ListenerList();
	private boolean actionHandlersChanged;

	/**
	 * Property constant for changes to action handlers.
	 */
	public static final String P_ACTION_HANDLERS = "org.eclipse.ui.internal.actionHandlers"; //$NON-NLS-1$
	
	/**
	 * Construct a new SubActionBars object.
	 */
	public SubActionBars(IActionBars parent) {
		this.parent = parent;
	}
	
	/**
	 * Activate the contributions.
	 */
	public void activate() {
		activate(true);
	}
	
	/**
	 * Activate the contributions.
	 * <p>
	 * Workaround for toolbar layout flashing when editors contribute
	 * large amounts of items. In this case we want to force the items
	 * to be visible/hidden only when required, otherwise just change
	 * the enablement state.</p>
	 */
	public void activate(boolean forceVisibility) {
		setActive(true);
	}
	
	/**
	 * Adds a property change listener.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener a property change listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}
	
	/**
	 * Clear the global action handlers.
	 */
	public void clearGlobalActionHandlers() {
		if (actionHandlers != null) {
			actionHandlers.clear();
			actionHandlersChanged = true;
		}
	}
	
	/**
	 * Returns a new sub menu manager.
	 *
	 * @param parent the parent menu manager
	 * @return the menu manager
	 */
	protected SubMenuManager createSubMenuManager(IMenuManager parent) {
		return new SubMenuManager(parent);
	}
	
	/**
	 * Returns a new sub toolbar manager.
	 *
	 * @param parent the parent toolbar manager
	 * @return the tool bar manager
	 */
	protected SubToolBarManager createSubToolBarManager(IToolBarManager parent) {
		return new SubToolBarManager(parent);
	}
	
	/**
	 * Deactivate the contributions.
	 */
	public void deactivate() {
		deactivate(true);
	}
	
	/**
	 * Deactivate the contributions.
	 * <p>
	 * Workaround for menubar/toolbar layout flashing when editors have 
	 * many contributions. In this case we want to force the contributions
	 * to be visible/hidden only when required, otherwise just change
	 * the enablement state.</p>
	 */
	public void deactivate(boolean forceHide) {
		setActive(false);
	}
	
	/**
	 * Dispose the contributions.
	 */
	public void dispose() {
		if (actionHandlers != null)
			actionHandlers.clear();
		if (menuMgr != null)
			menuMgr.removeAll();
		if (statusLineMgr != null)
			statusLineMgr.removeAll();
		if (toolBarMgr != null)
			toolBarMgr.removeAll();
		propertyChangeListeners.clear();
	}
	
	/**
	 * Notifies any property change listeners that a property has changed.
	 * Only listeners registered at the time this method is called are notified.
	 *
	 * @param event the property change event
	 *
	 * @see IPropertyChangeListener#propertyChange
	 */
	protected void firePropertyChange(PropertyChangeEvent event) {
		Object[] listeners = propertyChangeListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}
	}
	
	/**
	 * Notifies any property change listeners if the
	 * global action handlers have changed
	 */
	protected void fireActionHandlersChanged() {
		if (actionHandlersChanged) {
			// Doesn't actually pass the old and new values
			firePropertyChange(new PropertyChangeEvent(this, P_ACTION_HANDLERS, null, null));
			actionHandlersChanged = false;
		}
	}
	
	/**
	 * Return whether the manager is currently active or not.
	 */
	protected final boolean getActive() {
		return active;
	}
	
	/**
	 * Get the handler for a window action.
	 *
	 * @param actionID an action ID declared in the registry
	 * @return an action handler which implements the action ID, or
	 *		<code>null</code> if none is registered.
	 */
	public IAction getGlobalActionHandler(String actionID) {
		if (actionHandlers == null)
			return null;
		return (IAction) actionHandlers.get(actionID);
	}
	
	/**
	 * Returns the complete list of active global action handlers.
	 * If there are no global action handlers registered return null.
	 */
	public Map getGlobalActionHandlers() {
		return actionHandlers;
	}
	
	/**
	 * Returns the abstract menu manager.  If items are added or
	 * removed from the manager be sure to call <code>updateActionBars</code>.
	 *
	 * @return the menu manager
	 */
	public IMenuManager getMenuManager() {
		if (menuMgr == null) {
			menuMgr = createSubMenuManager(parent.getMenuManager());
			menuMgr.setVisible(active);
		}
		return menuMgr;
	}

	/**
	 * Return the parent action bar manager.
	 */
	protected final IActionBars getParent() {
		return parent;
	}
	
	/**
	 * Returns the status line manager.  If items are added or
	 * removed from the manager be sure to call <code>updateActionBars</code>.
	 *
	 * @return the status line manager
	 */
	public IStatusLineManager getStatusLineManager() {
		if (statusLineMgr == null) {
			statusLineMgr = new SubStatusLineManager(parent.getStatusLineManager());
			statusLineMgr.setVisible(active);
		}
		return statusLineMgr;
	}
	
	/**
	 * Returns the tool bar manager.  If items are added or
	 * removed from the manager be sure to call <code>updateActionBars</code>.
	 *
	 * @return the tool bar manager
	 */
	public IToolBarManager getToolBarManager() {
		if (toolBarMgr == null) {
			toolBarMgr = createSubToolBarManager(parent.getToolBarManager());
			toolBarMgr.setVisible(active);
		}
		return toolBarMgr;
	}
	
	/**
	 * Return whether the sub menu manager has been created yet.
	 */
	protected final boolean isSubMenuManagerCreated() {
		return menuMgr != null;
	}
	
	/**
	 * Return whether the sub toolbar manager has been created yet.
	 */
	protected final boolean isSubToolBarManagerCreated() {
		return toolBarMgr != null;
	}

	/**
	 * Return whether the sub status line manager has been created yet.
	 */
	protected final boolean isSubStatusLineManagerCreated() {
		return statusLineMgr != null;
	}

	/**
	 * Notification that the target part for the action bars has changed.
	 */
	public void partChanged(IWorkbenchPart part) {
	}
	
	/**
	 * Removes the given property change listener.
	 * Has no effect if an identical listener is not registered.
	 *
	 * @param listener a property change listener
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}

	/**
	 * Sets the active flag. Clients should not call this method
	 * directly unless they are overriding the setActive() method.
	 */
	protected final void basicSetActive(boolean active) {
		this.active = active;
	}
		
	/**
	 * Activate / deactivate the contributions.
	 */
	protected void setActive(boolean set) {
		active = set;
		if (menuMgr != null)
			menuMgr.setVisible(set);

		if (statusLineMgr != null)
			statusLineMgr.setVisible(set);

		if (toolBarMgr != null)
			toolBarMgr.setVisible(set);
	}
	
	/**
	 * Add a handler for a window action.
	 *
	 * @param actionID an action ID declared in the registry
	 * @param handler an action which implements the action ID.  
	 *		<code>null</code> may be passed to deregister a handler.
	 */
	public void setGlobalActionHandler(String actionID, IAction handler) {
		if (handler != null) {
			if (actionHandlers == null)
				actionHandlers = new HashMap(11);
			actionHandlers.put(actionID, handler);
		} else {
			if (actionHandlers != null)
				actionHandlers.remove(actionID);
		}
		actionHandlersChanged = true;
	}
	
	/**
	 * Commits all UI changes.  This should be called
	 * after additions or subtractions have been made to a 
	 * menu, status line, or toolbar.
	 */
	public void updateActionBars() {
		IToolBarManager mgr = getToolBarManager();
		if (mgr instanceof CoolItemToolBarManager) {
			// fix for 20988
			mgr.update(false);
		} 
		parent.updateActionBars();
		fireActionHandlersChanged();
	}
}
