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

/**
 * A <code>SubCoolBarManager</code> monitors the additional and removal of 
 * items from a parent manager so that visibility of the entire set can be changed as a
 * unit.
 */
public class SubCoolBarManager	extends SubContributionManager implements ICoolBarManager {

	/**
	 * Constructs a new manager.
	 *
	 * @param mgr the parent manager.  All contributions made to the 
	 *      <code>SubCoolBarManager</code> are forwarded and appear in the
	 *      parent manager.
	 */
	public SubCoolBarManager(ICoolBarManager mgr) {
		super(mgr);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#add(org.eclipse.jface.action.IToolBarManager)
	 */
	public void add(IToolBarManager toolBarManager) {
		super.add(new ToolBarContributionItem(toolBarManager));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#getStyle()
	 */
	public int getStyle() {
		// It is okay to cast down since we only accept coolBarManager objects in the
		// constructor
		return ((CoolBarManager)getParent()).getStyle();
	}
	
	/**
	 * Returns the parent coolbar manager that this sub-manager contributes to.
	 */
	protected final ICoolBarManager getParentCoolBarManager() {
		// Cast is ok because that's the only
		// thing we accept in the construtor.
		return (ICoolBarManager)getParent();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#isLayoutLocked()
	 */
	public boolean getLockLayout() {
		return getParentCoolBarManager().getLockLayout();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#lockLayout(boolean)
	 */
	public void setLockLayout(boolean value) {}

	/* (non-Javadoc)
	 * SubCoolBarManagers do not have control of the global context menu.
	 */
	public IMenuManager getContextMenuManager() {
		return null;
	}

	/* (non-Javadoc)
	 * In SubCoolBarManager we do nothing.
	 */
	public void setContextMenuManager(IMenuManager menuManager) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#update(boolean)
	 */
	public void update(boolean force) {
		// This method is not governed by visibility.  The client may
		// call <code>setVisible</code> and then force an update.  At that
		// point we need to update the parent.
		getParentCoolBarManager().update(force);

	}

}
