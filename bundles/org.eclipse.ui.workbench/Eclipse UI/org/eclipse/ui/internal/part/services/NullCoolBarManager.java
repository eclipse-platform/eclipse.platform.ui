/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;

/**
 * @since 3.1
 */
public class NullCoolBarManager extends NullContributionManager implements ICoolBarManager {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#add(org.eclipse.jface.action.IToolBarManager)
	 */
	public void add(IToolBarManager toolBarManager) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#getContextMenuManager()
	 */
	public IMenuManager getContextMenuManager() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#getLockLayout()
	 */
	public boolean getLockLayout() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#getStyle()
	 */
	public int getStyle() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#setContextMenuManager(org.eclipse.jface.action.IMenuManager)
	 */
	public void setContextMenuManager(IMenuManager menuManager) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#setLockLayout(boolean)
	 */
	public void setLockLayout(boolean value) {
		
	}

}
