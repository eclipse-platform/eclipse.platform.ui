package org.eclipse.ui.internal;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

/**
 * CoolItemToolBarManager class
 */
public class CoolItemToolBarManager extends ToolBarManager {
	CoolBarManager parentManager;
	CoolBarContributionItem coolBarItem;
	
	public CoolItemToolBarManager(int style) {
		super(style);
	}
	/* package */ void addGroup(String groupId, String contributingId) {
		// Add a new group to the coolitem.  Add the group at the end of the toolbar.
		addGroupBefore(groupId, contributingId, null);
	}
	/* package */ void addGroupBefore(String groupId, String contributingId, String beforeGroupId) {
		// Add a new group to the coolitem.  Add the group before the group with the given id
		// or at the end of the toolbar if no before group id is specified.
		if (beforeGroupId != null) {
			IContributionItem item = find(beforeGroupId);
			if (item == null) beforeGroupId = null;
		}
		// Groups are delineated by an ActionSetSeparator.  Within each group,
		// ActionSetGroupMarkers are used to delineate the items contributed by
		// a particular action set.  Groups can have items contributed from 
		// multiple action sets.  Groups are visually separated by separators.
		String subGroupId = getSubGroupId(groupId, contributingId);
		CoolItemGroupSeparator group = new CoolItemGroupSeparator(groupId, contributingId);
		CoolItemSubGroupMarker subGroup = new CoolItemSubGroupMarker(subGroupId, contributingId);
		if (beforeGroupId == null) {
			add(group);
			add(subGroup);
		} else {
			insertBefore(beforeGroupId, group);
			insertBefore(beforeGroupId, subGroup);
		}
	}
	/* package */ void addSubGroup(String groupId, String contributingId) {
		// Add a sub group to the group identified by groupId.  Add the subGroup
		// at the end of the group.  Subgroups have an id of their groupId + the
		// id of the action set that is contributing the subGroup.
		String subGroupId = getSubGroupId(groupId, contributingId);
		CoolItemSubGroupMarker subGroup = new CoolItemSubGroupMarker(subGroupId, contributingId);
		IContributionItem refItem = findEndOfGroup(groupId);
		if (refItem == null) {
			add(subGroup);
		} else {
			insertBefore(refItem.getId(), subGroup);
		}
	}
	/* package */ void addToGroup(String groupId, String contributingId, IContributionItem actionContribution) {
		// Add the item to an existing subgroup within the given group.  Subgroups have
		// an id of their groupId + the id of the action set that is contributing the
		// item
		String subGroupId = getSubGroupId(groupId, contributingId);
		CoolItemSubGroupMarker subGroup = (CoolItemSubGroupMarker)find(subGroupId);	
		if (subGroup == null) {
			// create the subgroup marker if it does not exist
			addSubGroup(groupId, contributingId);
		} 
		// insert the item, add it to the beginning of the subgroup.
		insertAfter(subGroupId, actionContribution);
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
	protected IContributionItem findEndOfGroup(String groupId) {
		// Get items.
		IContributionItem[] items = getItems();
		// Find the group item.
		int insertIndex = 0;
		while (insertIndex < items.length) {
			if (groupId.equals(items[insertIndex].getId())) {
				// the found item will be the ActionSetSeparator for
				// the group
				++insertIndex;
				break;
			}
			++insertIndex;
		}
		while (insertIndex < items.length) {
			ContributionItem item = (ContributionItem)items[insertIndex];
			if (item instanceof CoolItemGroupSeparator) {
				// when we find another ActionSetSeparator we are
				// at the end of the group
				break;
			}
			++insertIndex;
		}
		if (insertIndex >= items.length) return null;
		else return items[insertIndex];	
	}
	/* package */ String getSubGroupId(String groupId, String toolBarId) {
		return groupId + "-" + toolBarId;
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
		if (coolBarItem.isEmpty()) parentManager.saveToolBarLayout(coolBarItem);
		super.itemRemoved(item);
		update(true);
		parentManager.updateSizeFor(coolBarItem);
	}
	protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
		if (oldCount == newCount) return;
		CoolBar coolBar = (CoolBar)toolBar.getParent();
		CoolItem[] coolItems = coolBar.getItems();
		CoolItem coolItem = null;
		for (int i = 0; i < coolItems.length; i++) {	
			CoolItem item = coolItems[i];
			if (item.getControl() == toolBar) {
				coolItem = item;
				break;
			}						
		}
		// recompute preferred size so chevron will work correctly when
		// items are added/removed from the toolbar, don't set the size of
		// the coolItem since that would affect the position of other
		// coolItems on the toolbar
		if (coolItem != null) {
			Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point coolSize = coolItem.computeSize (size.x, size.y);
			coolItem.setPreferredSize(coolSize);
		}
		coolBar.layout();
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
