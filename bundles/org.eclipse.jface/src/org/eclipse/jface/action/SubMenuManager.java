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
package org.eclipse.jface.action;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

/**
 * A <code>SubMenuManager</code> is used to define a set of contribution
 * items within a parent manager.  Once defined, the visibility of the entire set can 
 * be changed as a unit.
 * <p>
 * A client may ask for and make additions to a submenu.  The visibility of these items
 * is also controlled by the visibility of the <code>SubMenuManager</code>.
 * </p>
 */
public class SubMenuManager extends SubContributionManager implements IMenuManager {
	/**
	 * Maps each submenu in the manager to a wrapper.  The wrapper is used to
	 * monitor additions and removals.  If the visibility of the manager is modified
	 * the visibility of the submenus is also modified.
	 */
	private Map mapMenuToWrapper;

	/**
	 * Constructs a new manager.
	 *
	 * @param mgr the parent manager.  All contributions made to the 
	 *      <code>SubMenuManager</code> are forwarded and appear in the
	 *      parent manager.
	 */
	public SubMenuManager(IMenuManager mgr) {
		super(mgr);
	}
	/**
	 * The default implementation of this <code>IContributionItem</code>
	 * method does nothing. Subclasses may override.
	 */
	public void dispose() {
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void addMenuListener(IMenuListener listener) {
		getParentMenuManager().addMenuListener(listener);
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void fill(Composite parent) {
		if (isVisible())
			getParentMenuManager().fill(parent);
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void fill(Menu parent, int index) {
		if (isVisible())
			getParentMenuManager().fill(parent, index);
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void fill(ToolBar parent, int index) {
		if (isVisible())
			getParentMenuManager().fill(parent, index);
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	/**
	 * <p>
	 * The menu returned is wrapped within a <code>SubMenuManager</code> to
	 * monitor additions and removals.  If the visibility of this menu is modified
	 * the visibility of the submenus is also modified.
	 * </p>
	 */
	public IMenuManager findMenuUsingPath(String path) {
		IContributionItem item = findUsingPath(path);
		if (item instanceof IMenuManager) {
			return (IMenuManager) item;
		}
		return null;
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 *
	 * Returns the item passed to us, not the wrapper.
	 *
	 * We use use the same algorithm as MenuManager.findUsingPath, but unwrap
	 * submenus along so that SubMenuManagers are visible.
	 */
	public IContributionItem findUsingPath(String path) {
		String id = path;
		String rest = null;
		int separator = path.indexOf('/');
		if (separator != -1) {
			id = path.substring(0, separator);
			rest = path.substring(separator + 1);
		}
		IContributionItem item = find(id); // unwraps item
		if (rest != null && item instanceof IMenuManager) {
			IMenuManager menu = (IMenuManager) item;
			item = menu.findUsingPath(rest);
		}
		return item;
	}
	/* (non-Javadoc)
	 * Method declared on IContributionManager.
	 *
	 * Returns the item passed to us, not the wrapper.
	 * In the case of menu's not added by this manager,
	 * ensure that we return a wrapper for the menu.
	 */
	public IContributionItem find(String id) {
		IContributionItem item = getParentMenuManager().find(id);
		if (item instanceof SubContributionItem)
			// Return the item passed to us, not the wrapper.
			item = unwrap(item);

		if (item instanceof IMenuManager) {
			// if it is a menu manager wrap it before returning
			IMenuManager menu = (IMenuManager) item;
			item = getWrapper(menu);
		}

		return item;
	}
	/**
	 * Returns the parent menu manager that this sub-manager contributes to.
	 */
	protected final IMenuManager getParentMenuManager() {
		// Cast is ok because that's the only
		// thing we accept in the construtor.
		return (IMenuManager)getParent();
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public String getId() {
		return getParentMenuManager().getId();
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public boolean getRemoveAllWhenShown() {
		return false;
	}
	/**
	 * Returns the menu wrapper for a menu manager.
	 * <p>
	 * The sub menus within this menu are wrapped within a <code>SubMenuManager</code> to
	 * monitor additions and removals.  If the visibility of this menu is modified
	 * the visibility of the sub menus is also modified.
	 * <p>
	 *
	 * @return the menu wrapper
	 */
	protected IMenuManager getWrapper(IMenuManager mgr) {
		if (mapMenuToWrapper == null) {
			mapMenuToWrapper = new HashMap(4);
		}
		SubMenuManager wrapper = (SubMenuManager) mapMenuToWrapper.get(mgr);
		if (wrapper == null) {
			wrapper = wrapMenu(mgr);
			mapMenuToWrapper.put(mgr, wrapper);
		}
		return wrapper;
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public boolean isDynamic() {
		return getParentMenuManager().isDynamic();
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public boolean isEnabled() {
		return isVisible() && getParentMenuManager().isEnabled();
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public boolean isGroupMarker() {
		return getParentMenuManager().isGroupMarker();
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public boolean isSeparator() {
		return getParentMenuManager().isSeparator();
	}
	/**
	 * Remove all contribution items.
	 */
	public void removeAll() {
		super.removeAll();
		if (mapMenuToWrapper != null) {
			Iterator iter = mapMenuToWrapper.values().iterator();
			while (iter.hasNext()) {
				SubMenuManager wrapper = (SubMenuManager) iter.next();
				wrapper.removeAll();
			}
			mapMenuToWrapper.clear();
			mapMenuToWrapper = null;
		}
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void removeMenuListener(IMenuListener listener) {
		getParentMenuManager().removeMenuListener(listener);
	}
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public void setParent(IContributionManager parent) {
		// do nothing, our "parent manager's" parent 
		// is set when it is added to a manager
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void setRemoveAllWhenShown(boolean removeAll) {
		Assert.isTrue(false, "Should not be called on submenu manager"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * Method declared on SubContributionManager.
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (mapMenuToWrapper != null) {
			Iterator iter = mapMenuToWrapper.values().iterator();
			while (iter.hasNext()) {
				SubMenuManager wrapper = (SubMenuManager) iter.next();
				wrapper.setVisible(visible);
			}
		}
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void update() {
		// This method is not governed by visibility.  The client may
		// call <code>setVisible</code> and then force an update.  At that
		// point we need to update the parent.
		getParentMenuManager().update();
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void update(boolean force) {
		// This method is not governed by visibility.  The client may
		// call <code>setVisible</code> and then force an update.  At that
		// point we need to update the parent.
		getParentMenuManager().update(force);
	}
	/* (non-Javadoc)
	 * Method declared on IMenuManager.
	 */
	public void updateAll(boolean force) {
		// This method is not governed by visibility.  The client may
		// call <code>setVisible</code> and then force an update.  At that
		// point we need to update the parent.
		getParentMenuManager().updateAll(force);
	}
	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public void update(String id) {
		getParentMenuManager().update(id);
	}
	/**
	 * Wraps a menu manager in a sub menu manager, and returns the new wrapper.
	 */
	protected SubMenuManager wrapMenu(IMenuManager menu) {
		SubMenuManager mgr = new SubMenuManager(menu);
		mgr.setVisible(isVisible());
		return mgr;
	}

}
