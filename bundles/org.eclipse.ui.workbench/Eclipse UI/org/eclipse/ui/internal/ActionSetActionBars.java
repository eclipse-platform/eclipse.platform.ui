package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.ArrayList;

import org.eclipse.jface.action.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.SubActionBars;

/**
 * This class represents the action bars for an action set.
 */
public class ActionSetActionBars extends SubActionBars {
	private String actionSetId;
	private CoolItemToolBarManager coolItemToolBarMgr;
	private ArrayList adjunctContributions = new ArrayList();
	/**
	 * Constructs a new action bars object
	 */
	public ActionSetActionBars(IActionBars parent, String actionSetId) {
		super(parent);
		this.actionSetId = actionSetId;
	}
	/* package */ void addAdjunctContribution(ContributionItem item) {
		adjunctContributions.add(item);
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
		if (coolItemToolBarMgr == null) return;
		CoolBarManager parentMgr = (CoolBarManager)coolItemToolBarMgr.getParentManager();

		IContributionItem[] items = coolItemToolBarMgr.getItems();
		// remove the action set's items from its action bar, don't use 
		// removeAll since other items from other actions sets may be in
		// the action bar's cool item
		for (int i=0; i<items.length; i++) {
			IContributionItem item = items[i];
			if (item instanceof PluginActionCoolBarContributionItem) {
				PluginActionCoolBarContributionItem actionSetItem = (PluginActionCoolBarContributionItem) item;
				if (actionSetItem.getActionSetId().equals(actionSetId)) {
					coolItemToolBarMgr.remove(item);
				}
			} else {
				// leave separators and group markers intact, doing
				// so allows ordering to be maintained when action sets
				// are removed then added back
			}		
		}
			
		// remove items from this action set that are in other action bars
		for (int i=0; i<adjunctContributions.size(); i++) {
			ContributionItem item = (ContributionItem)adjunctContributions.get(i);
			CoolItemToolBarManager parent = (CoolItemToolBarManager)item.getParent();
			if (parent != null) {
				parent.remove(item);
			}
		}
		adjunctContributions = new ArrayList();
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
			IContributionItem item = cBarMgr.find(actionSetId);
			if (item != null) {
				// the coolitem for this action set already exists, can
				// occur when another action set contributes to the cool
				// item for this action set and that action set is still 
				// active
				coolItemToolBarMgr = ((CoolBarContributionItem)item).getToolBarManager();
			} else {
				coolItemToolBarMgr = new CoolItemToolBarManager(cBarMgr.getStyle());
				// Just create the CoolBarContributionItem, PluginActionSetBuilder will add the item to
				// the CoolBarManager.
				new CoolBarContributionItem(cBarMgr, coolItemToolBarMgr, actionSetId);
				coolItemToolBarMgr.setVisible(getActive());
			}
		}
		return coolItemToolBarMgr;
	}
	/* package */ String getActionSetId() {
		return actionSetId;
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
