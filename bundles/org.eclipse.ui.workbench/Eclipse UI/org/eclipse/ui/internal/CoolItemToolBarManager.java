package org.eclipse.ui.internal;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.jface.action.*;
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
		// Helper method used when creating the workbench toolbars.
		CoolItemGroupSeparator group = new CoolItemGroupSeparator(groupId, contributingId);
		add(group);
	}
	/* package */ void addBaseGroup(String groupId) {
		// Add a new base group after the last item in the last base group.
		CoolItemGroupSeparator group = new CoolItemGroupSeparator(groupId, coolBarItem.getId());
		int index = findEndOfBaseGroups();
		if (index == -1) {
			insert(0, group);
		} else {
			insert(index + 1, group);
		}	
	}
	protected void addAdjunctGroup(String groupId, String contributingId) {
		// Add a new adjunct group after the base groups for the toolbar.  Adjunct groups
		// are added in action set id order.
		CoolItemGroupSeparator group = new CoolItemGroupSeparator(groupId, contributingId);
		int endIndex = findEndOfBaseGroups();
		if (endIndex == - 1) {
			// no base groups exist, just add the new group at the end of the toolbar
			add(group);
		} else {
			// look at the items after the base groups, when we find a group with a
			// contributing id > the contributing id of the group we are adding, we
			// have found our insertion point
			++endIndex;
			IContributionItem[] items = getItems();
			while (endIndex < items.length) {
				IContributionItem item = items[endIndex];
				if (item instanceof ICoolItemGroup) {
					ICoolItemGroup itemGroup = (ICoolItemGroup)item;
					int compare = itemGroup.getContributingId().compareTo(contributingId); 
					if (compare > 1) break;
				}
				++endIndex;
			}
			insert(endIndex, group);	
		}
	}
	/* package */ void addAdjunctGroupBefore(String groupId, String contributingId, String beforeGroupId) {
		// Add a new adjunct group before the given base groupId.  Adjunct before groups are added in action 
		// set id order.
		
		// Validate the beforeGroupId.
		int beforeIndex = -1;
		if (beforeGroupId != null) {
			beforeIndex = indexOf(beforeGroupId);
			if (beforeIndex != -1) {
				IContributionItem item = find(beforeGroupId);
				if (item instanceof ICoolItemGroup) {
					ICoolItemGroup itemGroup = (ICoolItemGroup)item;
					if (itemGroup.getContributingId().equals(coolBarItem.getId())) {
						// beforeGroupId is valid
					} else {
						beforeIndex = -1; // beforeGroupId is not a base group
					}
				} else {
					beforeIndex = -1; // beforeGroupId is not a group
				}
			}
		}

		if (beforeIndex == -1) {
			// the before base group does not exist, was not specified, or is invalid
			addAdjunctGroup(groupId, contributingId);	
			return;
		}
		
		CoolItemGroupSeparator group = new CoolItemGroupSeparator(groupId, contributingId, beforeGroupId);
		if (beforeIndex != 0) {
			--beforeIndex;
			IContributionItem[] items = getItems();
			while (beforeIndex >= 0) {
				IContributionItem item = items[beforeIndex];
				// Look for other groups that were added before the beforeGroup.
				// When a non-before group is encountered or when we encounter
				// a group with an id <= the id of the group we are contributing,
				// we have found our insert point.
				if (item instanceof CoolItemGroupSeparator) {
					CoolItemGroupSeparator sep = (CoolItemGroupSeparator)item;
					if (beforeGroupId.equals(sep.getBeforeGroupId())) {
						int compare = sep.getContributingId().compareTo(contributingId); 
						if (compare <= 0) {
							beforeIndex = findEndOfGroup(sep.getId());
							++beforeIndex;
							break;
						}
					} else {
						beforeIndex = findEndOfGroup(sep.getId());
						++beforeIndex;
						break;
					}
				}
				--beforeIndex;
			}
		}
		beforeIndex = Math.max(0, beforeIndex);
		insert(beforeIndex, group);
	}
	/* package */ void addGroupMarker(String groupId, String contributingId) {
		// Add a group to the coolitem.  Add the group marker at the end of the toolbar.
		// Helper method used when creating the workbench toolbars.
		CoolItemGroup group = new CoolItemGroup(groupId, contributingId);
		add(group);
	}
	/* package */ void addToGroup(String groupId, String contributingId, IContributionItem actionContribution) {
		// Add the item to the existing group.  If the item is a base contribution, add it at the beginning
		// of the group.  Otherwise, add the item after the base contributions in action set id order.
		
		int startIndex = findStartOfGroup(groupId);
		if (startIndex == -1) {
			WorkbenchPlugin.log("Unable to find start of group " + groupId); //$NON-NLS-1$
			return;
		}
		
		// Find insertion point within the group.  Start index will be the group separator/marker.
		// Items are added to the group at the beginning of the group in action set id order.
		String coolBarId = coolBarItem.getId();
		++startIndex;
		if (contributingId.equals(coolBarId)) {
			// base items are added at the beginning of the group
			insert(startIndex, actionContribution);
		} else {
			// adjunct items are added after the base items in the group
			IContributionItem[] items = getItems();
			while (startIndex < items.length) {
				IContributionItem item = items[startIndex];
				if (item instanceof ICoolItemGroup) break; // end of group reached
				if (item instanceof PluginActionCoolBarContributionItem) {
					PluginActionCoolBarContributionItem pluginItem = (PluginActionCoolBarContributionItem)item;
					String actionSetId = pluginItem.getActionSetId();
					if (actionSetId.equals(coolBarId)) {
						// base contribution, continue iterating
					} else {
						// compare the actionSetId of the found item to the id of the
						// item we are contributing
						int compare = actionSetId.compareTo(contributingId); 
						if (compare == 0) 
							// start of contributingId's contributions
							break; 
						if (compare > 1) 
							// start of another action sets's contributions that have
							// actionSetId > the actionSetId of the item we are contributing 
 							break; 
					}
				}
				++startIndex;
			}
			insert(startIndex, actionContribution);
		}
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
	protected int findEndOfBaseGroups() {
		IContributionItem[] items = getItems();
		int i = 0;
		String id = coolBarItem.getId();
		ICoolItemGroup lastGroup = null;
		while (i < items.length) {
			IContributionItem item = items[i];
			if (item instanceof ICoolItemGroup) {
				ICoolItemGroup itemGroup = (ICoolItemGroup)item;
				if (itemGroup.getContributingId().equals(id)) {
					lastGroup = itemGroup;
				} else if (itemGroup instanceof CoolItemGroupSeparator) {
					CoolItemGroupSeparator itemSep = (CoolItemGroupSeparator)itemGroup;
					if (itemSep.getBeforeGroupId() != null) {
						// continue
					} else {
						break;
					}
				} 
			}
			++i;
		}
		if (lastGroup == null) return -1;
		return findEndOfGroup(lastGroup.getId());
	}
	protected int findStartOfGroup(String groupId) {
		IContributionItem[] items = getItems();
		int index = 0;
		while (index < items.length) {
			IContributionItem item = items[index];
			if (item instanceof ICoolItemGroup) {
				if (groupId.equals(item.getId())) {
					break;
				}
			}
			++index;
		}
		if (index >= items.length) index = -1;
		return index;
	}
	protected int findEndOfGroup(String groupId) {
		// Return the index of the last item in the given group.  Return 
		// -1 if no group exists.
		IContributionItem[] items = getItems();
		// Find the group item.
		int i = 0;
		while (i < items.length) {
			IContributionItem item = items[i];
			if (item instanceof ICoolItemGroup) {
				if (groupId.equals(item.getId())) {
					// the found item will be the marker for the group
					break;
				}
			}
			++i;
		}
		if (i >= items.length) {
			return -1;
		}
		i = i + 1;
		while (i < items.length) {
			ContributionItem item = (ContributionItem)items[i];
			if (item instanceof ICoolItemGroup) {
				// when we find another cool item group, we are
				// at the end of the group
				--i;
				break;
			}
			++i;
		}
		if (i >= items.length) return items.length - 1;
		return i;	
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
