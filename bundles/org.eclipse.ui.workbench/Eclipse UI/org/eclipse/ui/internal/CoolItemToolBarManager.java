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
package org.eclipse.ui.internal;


import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

/**
 * CoolItemToolBarManager class
 */
public class CoolItemToolBarManager extends ToolBarManager {
	CoolBarManager parentManager;
	CoolBarContributionItem coolBarItem;
	
	public CoolItemToolBarManager(int style) {
		super(style);
	}
	/* package */ void addBaseGroup(String groupId, boolean separator) {
		// Add a new base group after the last item in the last base group.
		IContributionItem group;
		if (separator) {
			group = new CoolItemGroupSeparator(groupId, coolBarItem.getId());
		} else {
			group = new CoolItemGroup(groupId, coolBarItem.getId());
		}
		int index = findGroupInsertionPoint(coolBarItem.getId());
		insert(index, group);
	}
	/* package */ void addAdjunctItemToGroup(String groupId, String contributingId, IContributionItem actionContribution) {
		int index = findItemInsertionPointInGroup(contributingId, groupId);
		insert(index, actionContribution);
	}
	/* package */ void addBaseItemToGroup(String groupId, IContributionItem actionContribution) {
		int index = findItemInsertionPointInGroup(coolBarItem.getId(), groupId);
		insert(index, actionContribution);
	}
	/* package */ void addAdjunctGroup(String groupId, String contributingId) {
		CoolItemGroupSeparator group = new CoolItemGroupSeparator(groupId, contributingId);
		int index = findGroupInsertionPoint(contributingId);
		insert(index, group);
	}
	public ToolBar createControl(Composite parent) {
		ToolBar tBar = super.createControl(parent);
		tBar.setMenu(parentManager.getCoolBarMenu());
		return tBar;
	}
	public void dispose() {
		// the toolbar menu is shared by all coolitems, so clear the
		// reference to the menu so that it does not get disposed of
		ToolBar tBar = getControl();
		// null check necessary for CoolItemMultiToolBarManager which
		// does not have a toolbar, but calls super.dispose() 
		if (tBar != null) tBar.setMenu(null);
		super.dispose();
	}
	protected CoolBarContributionItem getCoolBarItem() {
		return coolBarItem;
	}
	protected int findEndOfGroup(String groupId) {
		// Return the index of the position after the 
		// last item in the given group. 

		// Find the group item.
		int i = indexOf(groupId);
		if (i == -1) return i;
		
		// Look for the beginning of the next group.
		i = i + 1;
		IContributionItem[] items = getItems();
		while (i < items.length) {
			ContributionItem item = (ContributionItem)items[i];
			if (item instanceof ICoolItemGroup) {
				break;
			}
			++i;
		}
		return i;	
	}
	protected int findGroupInsertionPoint(String contributingId) {
		IContributionItem[] items = getItems();
		int i = 0;
		IContributionItem lastGroup = null;
		while (i < items.length) {
			IContributionItem item = items[i];
			if (item instanceof ICoolItemGroup) {
				ICoolItemGroup itemGroup = (ICoolItemGroup)item;
				int compare = contributingId.compareTo(itemGroup.getContributingId());
				if (compare < 0) break;
				if (compare == 0) lastGroup = item;
			}
			++i;
		}
		if (lastGroup != null) i= findEndOfGroup(lastGroup.getId());
		return i;
	}
	protected int findItemInsertionPointInGroup(String contributingId, String groupId) {
		int startIndex = indexOf(groupId);
		if (startIndex == -1) {
			return -1;
		}
		++startIndex;
		IContributionItem[] items = getItems();
		while (startIndex < items.length) {
			IContributionItem item = items[startIndex];
			if (item instanceof ICoolItemGroup) break; // start of new group
			String id;
			if (item instanceof PluginActionCoolBarContributionItem) {
				id = ((PluginActionCoolBarContributionItem)item).getActionSetId();
			} else {
				id = coolBarItem.getId();
			}
			int compare = contributingId.compareTo(id);
			if (compare == 0) break;
			if (compare < 0) break;
			++startIndex;
		}
		return startIndex;
	}
	protected CoolBarManager getParentManager() {
		return parentManager;
	}
	protected boolean isVisible() {
		if (coolBarItem == null) {
			return false;
		}
		return coolBarItem.isVisible();
	}
	protected void itemAdded(IContributionItem item) {
		super.itemAdded(item);
		update(true);
		parentManager.updateSizeFor(coolBarItem);
	}
	protected void itemRemoved(IContributionItem item) {
		super.itemRemoved(item);
		update(true);
		parentManager.updateSizeFor(coolBarItem);
	}
	protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
		// coolbar manager will handle relayout issues
	} 
	protected void setCoolBarItem(CoolBarContributionItem coolBarItem) {
		this.coolBarItem = coolBarItem;
	}
	protected void setParentMgr(CoolBarManager parentManager) {
		this.parentManager = parentManager;
	}
	
	protected void setVisible(boolean set) {
		if (coolBarItem != null) {
			coolBarItem.setVisible(set);
		}
	}
	protected void setVisible(boolean set, boolean forceVisibility) {
		if (coolBarItem != null) {
			coolBarItem.setVisible(set, forceVisibility);
		}
	}
}      
