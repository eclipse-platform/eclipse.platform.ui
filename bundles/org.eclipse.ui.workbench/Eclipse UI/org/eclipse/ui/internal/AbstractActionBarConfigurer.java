/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * Abstract base implementation of an IActionBarConfigurer.
 * 
 * @since 3.0
 */
public abstract class AbstractActionBarConfigurer implements IActionBarConfigurer {
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IActionBarConfigurer
	 */
	public abstract IStatusLineManager getStatusLineManager(); 
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IActionBarConfigurer
	 */
	public abstract IMenuManager getMenuManager();

	/**
	 * Returns the internal coolbar manager.
	 * 
	 * @return the coolbar manager
	 */
	public abstract CoolBarManager getCoolBarManager(); 
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IActionBarConfigurer
	 */
	public IToolBarManager addToolBar(String id) {
		if (id == null || id.length() < 1) {
			throw new IllegalArgumentException();
		}
		CoolBarManager cBarMgr = getCoolBarManager();
		CoolBarContributionItem cBarItem = new CoolBarContributionItem(cBarMgr, id);
		cBarMgr.add(cBarItem);
		cBarItem.setVisible(true);
		return cBarItem.getToolBarManager();
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IActionBarConfigurer
	 */
	public void removeToolBar(String id) {
		if (id == null || id.length() < 1) {
			throw new IllegalArgumentException();
		}
		CoolBarManager cBarMgr = getCoolBarManager();
		cBarMgr.remove(id);
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IActionBarConfigurer
	 */
	public IToolBarManager getToolBar(String id) {
		if (id == null || id.length() < 1) {
			throw new IllegalArgumentException();
		}
		CoolBarManager cBarMgr = getCoolBarManager();
		CoolBarContributionItem cBarItem = (CoolBarContributionItem) cBarMgr.find(id);
		if (cBarItem != null) {
			return cBarItem.getToolBarManager();
		} else {
			return null;
		}
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IActionBarConfigurer
	 */
	public void addToolBarGroup(IToolBarManager toolBarMgr, String id, boolean asSeparator) {
		if (id == null || id.length() < 1) {
			throw new IllegalArgumentException();
		}
		if (!(toolBarMgr instanceof CoolItemToolBarManager)) {
			throw new IllegalArgumentException();
		}
		((CoolItemToolBarManager) toolBarMgr).addBaseGroup(id, asSeparator);
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IActionBarConfigurer
	 */
	public void registerGlobalAction(IAction action) {
		// do nothing by default

	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IActionBarConfigurer
	 */
	public void addToToolBarMenu(ActionContributionItem menuItem) {
		if (menuItem == null) {
			throw new IllegalArgumentException();
		}
		CoolBarManager cBarMgr = getCoolBarManager();
		cBarMgr.addToMenu(menuItem);
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IActionBarConfigurer
	 */
	public void addEditorToolBarGroup() {
		// do nothing by default
	}
}
