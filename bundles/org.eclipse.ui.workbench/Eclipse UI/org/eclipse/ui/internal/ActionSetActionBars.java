package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.SubActionBars;

/**
 * This class represents the action bars for an action set.
 */
public class ActionSetActionBars extends SubActionBars {
	private String actionSetId;
	private CoolItemToolBarManager coolItemToolBarMgr;
	/**
	 * Constructs a new action bars object
	 */
	public ActionSetActionBars(IActionBars parent, String actionSetId) {
		super(parent);
		this.actionSetId = actionSetId;
	}
	/* (non-Javadoc)
	 * Inherited from SubActionBars.
	 */
	protected SubMenuManager createSubMenuManager(IMenuManager parent) {
		return new ActionSetMenuManager(parent, actionSetId);
	}
	/* (non-Javadoc)
	 * Inherited from SubActionBars.
	 */
	protected SubToolBarManager createSubToolBarManager(IToolBarManager parent) {
		// return null, action sets are managed by CoolItemToolBarManagers
		return null;
	}
	/**
	 * Dispose the contributions.
	 */
	public void dispose() {
		super.dispose();
		if (coolItemToolBarMgr != null)
			coolItemToolBarMgr.removeAll();
	}
	/**
	 * Returns the tool bar manager.  If items are added or
	 * removed from the manager be sure to call <code>updateActionBars</code>.
	 *
	 * @return the tool bar manager
	 */
	public IToolBarManager getToolBarManager() {
		IToolBarManager parentMgr = getParent().getToolBarManager();
		if (parentMgr == null) {
			return null;
		}
		if (coolItemToolBarMgr == null) {
			// Create a CoolBar item for this action bar.
			CoolBarManager cBarMgr = ((CoolBarManager) parentMgr);
			coolItemToolBarMgr = new CoolItemToolBarManager(cBarMgr.getStyle());
			// Just create the CoolBarContributionItem, PluginActionSetBuilder will add the item to
			// the CoolBarManager.
			new CoolBarContributionItem(cBarMgr, coolItemToolBarMgr, actionSetId);
			coolItemToolBarMgr.setVisible(getActive());
		}
		return coolItemToolBarMgr;
	}
	/**
	 * Activate / Deactivate the contributions.
	 */
	protected void setActive(boolean set) {
		super.setActive(set);
		if (coolItemToolBarMgr != null)
			coolItemToolBarMgr.setVisible(set);
	}
}
