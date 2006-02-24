/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.SubActionBars2;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.services.IServiceLocator;

/**
 * This class represents the action bars for an action set.
 */
public class ActionSetActionBars extends SubActionBars2 {
	
	private IActionBarConfigurer2 actionBarConfigurer = null;

	private String actionSetId;

	private ArrayList adjunctContributions = new ArrayList();

	private IToolBarManager coolItemToolBarMgr = null;

	private IToolBarContributionItem toolBarContributionItem = null;

	/**
     * Constructs a new action bars object
     */
    public ActionSetActionBars(IActionBars2 parent, IServiceLocator serviceLocator, IActionBarConfigurer2 actionBarConfigurer, String actionSetId) {
    	super(parent, serviceLocator);
		this.actionSetId = actionSetId;		
        this.actionBarConfigurer = actionBarConfigurer;
    }

	/**
	 * Adds to the list all the actions that are part of this action set but
	 * belong to different cool bar items.
	 * 
	 * @param item
	 *            the item defined in this actionset but in a different tool Bar
	 *            contribution item
	 */
	/* package */void addAdjunctContribution(IContributionItem item) {
		adjunctContributions.add(item);
	}

	/*
	 * (non-Javadoc) Inherited from SubActionBars.
	 */
	protected SubMenuManager createSubMenuManager(IMenuManager parent) {
		return new ActionSetMenuManager(parent, actionSetId);
	}

	/*
	 * (non-Javadoc) Inherited from SubActionBars.
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
		if (coolItemToolBarMgr == null) {
			return;
		}
		IContributionItem[] items = coolItemToolBarMgr.getItems();
		// remove the action set's items from its action bar, don't use
		// removeAll since other items from other actions sets may be in
		// the action bar's cool item
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			if (item instanceof PluginActionCoolBarContributionItem) {
				PluginActionCoolBarContributionItem actionSetItem = (PluginActionCoolBarContributionItem) item;
				if (actionSetItem.getActionSetId().equals(actionSetId)) {
					coolItemToolBarMgr.remove(item);
					item.dispose();
				}
			} else {
				// leave separators and group markers intact, doing
				// so allows ordering to be maintained when action sets
				// are removed then added back
			}
		}

		// remove items from this action set that are in other action bars
		for (int i = 0; i < adjunctContributions.size(); i++) {
			ContributionItem item = (ContributionItem) adjunctContributions
					.get(i);
			IContributionManager parent = item.getParent();
			if (parent != null) {
				parent.remove(item);
				item.dispose();
			}
		}
		toolBarContributionItem = null;
		coolItemToolBarMgr = null;
		adjunctContributions = new ArrayList();
	}

	/**
	 * Returns the contribution item that the given contribution item should be
	 * inserted after.
	 * 
	 * @param startId
	 *            the location to start looking alphabetically.
	 * @param itemId
	 *            the target item id.
	 * @param mgr
	 *            the contribution manager.
	 * @return the contribution item that the given items should be returned
	 *         after.
	 * 
	 * @since 3.0
	 */
	private IContributionItem findAlphabeticalOrder(String startId,
			String itemId, IContributionManager mgr) {
		IContributionItem[] items = mgr.getItems();
		int insertIndex = 0;

		// look for starting point
		while (insertIndex < items.length) {
			IContributionItem item = items[insertIndex];
			if (item.getId() != null && item.getId().equals(startId)) {
				break;
			}
			++insertIndex;
		}

		// Find the index that this item should be inserted in
		for (int i = insertIndex + 1; i < items.length; i++) {
			IContributionItem item = items[i];
			String testId = item.getId();

			if (item.isGroupMarker()) {
				break;
			}

			if (itemId != null && testId != null) {
				if (itemId.compareTo(testId) < 1) {
					break;
				}
			}
			insertIndex = i;
		}
		// Should be inserted at the end
		if (insertIndex >= items.length) {
			return null;
		}
		return items[insertIndex];
	}

	/* package */String getActionSetId() {
		return actionSetId;
	}

	/**
	 * Returns a tool bar manager for this Item.
	 * 
	 * @return the tool bar manager
	 */
	public IToolBarManager getToolBarManager() {
		ICoolBarManager coolBarManager = getCastedParent().getCoolBarManager();
		if (coolBarManager == null) {
			return null;
		}
        return actionBarConfigurer.createToolBarManager();
	}

	/**
	 * Returns the correct tool bar for the given action id. If this action is
	 * an adjunct type the it returns the toolbar manager from the cool bar
	 * manager.
	 * 
	 * @param id
	 *            the id of the action
	 * @return the tool bar manager
	 */
	public IToolBarManager getToolBarManager(String actionId) {
		// Check if a tool bar manager for an adjunct type is being requested
		String toolBarId = actionSetId;
		boolean isAdjunctType = false;
		if (!actionId.equals(actionSetId)) {
			// Adjunct type
			toolBarId = actionId;
			isAdjunctType = true;
		}

		// Rereive the cool bar manager
		ICoolBarManager coolBarManager = getCastedParent().getCoolBarManager();
		if (coolBarManager == null) {
			return null;
		}

		// Check to see that there isn't already a tool bar created
		// and the tool bar being requested is not for an adjunct action
		if ((coolItemToolBarMgr != null) && (!isAdjunctType)) {
			return coolItemToolBarMgr;
		}

		// Search for toolBarId in the cool Bar manager
		IContributionItem cbItem = coolBarManager.find(toolBarId);
		// If there hasn't been a tool bar contribution item created for this
		// tool bar
		// id then create one. Otherwise retrieve the tool bar contribution
		// item
		if (cbItem instanceof IToolBarContributionItem) {

			IToolBarContributionItem tbcbItem = (IToolBarContributionItem) cbItem;
			coolItemToolBarMgr = tbcbItem.getToolBarManager();
			// If this not an adjuct type then we can cashe the tool bar
			// contribution type
			if (!isAdjunctType) {
				toolBarContributionItem = tbcbItem;
			}
		} else {
			
			coolItemToolBarMgr = actionBarConfigurer.createToolBarManager();
           
			// If this is not an adjunct type then create a tool bar
			// contribution item
			// we don't create one for an adjunct type because another action
			// set action bars contains one
            
            IContributionItem toolBarContributionItem = actionBarConfigurer
					.createToolBarContributionItem(coolItemToolBarMgr,
							toolBarId);

			toolBarContributionItem.setParent(coolItemToolBarMgr);
			toolBarContributionItem.setVisible(getActive());
			coolItemToolBarMgr.markDirty();

			// Now add the tool bar contribution Item to the cool bar manager
			IContributionItem refItem = findAlphabeticalOrder(
					IWorkbenchActionConstants.MB_ADDITIONS, toolBarId,
					coolBarManager);
			if (refItem != null) {
				coolBarManager.insertAfter(refItem.getId(),
						toolBarContributionItem);
			} else {
				coolBarManager.add(toolBarContributionItem);
			}
		}
		return coolItemToolBarMgr;
	}

	// for dynamic UI
	/* package */void removeAdjunctContribution(ContributionItem item) {
		adjunctContributions.remove(item);
	}

	/**
	 * Activate / Deactivate the contributions.
	 */
	protected void setActive(boolean set) {
		super.setActive(set);

		ICoolBarManager coolBarManager = getCastedParent().getCoolBarManager();
		if (coolBarManager == null) {
			return;
		}

		// 1. Need to set visibility for all non-adjunct actions
		if (coolItemToolBarMgr != null) {
			IContributionItem[] items = coolItemToolBarMgr.getItems();
			for (int i = 0; i < items.length; i++) {
				IContributionItem item = items[i];
				if (item instanceof PluginActionCoolBarContributionItem) {
					PluginActionCoolBarContributionItem actionSetItem = (PluginActionCoolBarContributionItem) item;
					// Only if the action set id for this contribution item is
					// the same
					// as this object
					if (actionSetItem.getActionSetId().equals(actionSetId)) {
						item.setVisible(set);
						coolItemToolBarMgr.markDirty();
						if (!coolBarManager.isDirty()) {
							coolBarManager.markDirty();
						}
					}
				}
			}
			// Update the manager
			coolItemToolBarMgr.update(false);
			if (toolBarContributionItem != null) {
				toolBarContributionItem.update(ICoolBarManager.SIZE);
			}
		}

		// 2. Need to set visibility for all adjunct actions
		if (adjunctContributions.size() > 0) {
			for (Iterator i = adjunctContributions.iterator(); i.hasNext();) {
				IContributionItem item = (IContributionItem) i.next();
				if (item instanceof ContributionItem) {
					item.setVisible(set);
					IContributionManager manager = ((ContributionItem) item)
							.getParent();
					manager.markDirty();
					manager.update(false);
					if (!coolBarManager.isDirty()) {
						coolBarManager.markDirty();
					}
					item.update(ICoolBarManager.SIZE);
				}

			}

		}
	}

}
