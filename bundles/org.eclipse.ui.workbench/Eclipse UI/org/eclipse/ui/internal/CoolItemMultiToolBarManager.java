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


import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.part.CoolItemGroupMarker;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * CoolItemMultiToolBarManager class
 */
public class CoolItemMultiToolBarManager extends CoolItemToolBarManager {
	ArrayList coolBarItems = new ArrayList();
	boolean active = false;
	String id;
	boolean coolItemsCreated = false;

	public void add(IAction action) {
		if (!coolItemsCreated) {
			super.add(action);
			return;
		}
		CoolBarContributionItem cbItem = ((CoolBarContributionItem) coolBarItems.get(coolBarItems.size() - 1));
		cbItem.getToolBarManager().add(action);
		if (cbItem.hasDisplayableItems()) setVisible(true);
	}
	public void add(IContributionItem item) {
		if (!coolItemsCreated) {
			super.add(item);
			return;
		}
		CoolBarContributionItem cbItem = ((CoolBarContributionItem) coolBarItems.get(coolBarItems.size() - 1));
		cbItem.getToolBarManager().add(item);
		if (cbItem.hasDisplayableItems()) setVisible(true);
	}
	public void appendToGroup(String groupName, IAction action) {
		if (!coolItemsCreated) {
			super.appendToGroup(groupName, action);
			return;
		}
		CoolBarContributionItem cbItem = findGroup(groupName);
		if (cbItem != null) {
			cbItem.getToolBarManager().add(action);
			if (cbItem.hasDisplayableItems()) setVisible(true);
		} else {
			cbItem = findGroupFor(groupName);
			if (cbItem != null) {
				cbItem.getToolBarManager().appendToGroup(groupName, action);
				if (cbItem.hasDisplayableItems()) setVisible(true);
			}
		}
			
	}
	public void appendToGroup(String groupName, IContributionItem item) {
		if (!coolItemsCreated) {
			super.appendToGroup(groupName, item);
			return;
		}
		CoolBarContributionItem cbItem = findGroup(groupName);
		if (cbItem != null) {
			cbItem.getToolBarManager().add(item);
			if (cbItem.hasDisplayableItems()) setVisible(true);
		} else {
			cbItem = findGroupFor(groupName);
			if (cbItem != null) {
				cbItem.getToolBarManager().appendToGroup(groupName, item);
				if (cbItem.hasDisplayableItems()) setVisible(true);
			}
		}
	}
	/**
	* NOT VALID for multiple CoolItem case
	*/
	public ToolBar createControl(Composite parent) {
		if (!coolItemsCreated) {
			return super.createControl(parent);
		}
		return null;
	}

	/**
	* Disposes of this tool bar manager and frees all allocated SWT resources.
	* Note that this method does not clean up references between this tool bar 
	* manager and its associated contribution items.
	* Use <code>removeAll</code> for that purpose.
	*/
	public void dispose() {
		if (!coolItemsCreated) {
			super.dispose();
			return;
		}
		for (Iterator e = coolBarItems.iterator(); e.hasNext();) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
			cbItem.getToolBarManager().dispose();
		}
		super.dispose();
	}

	public IContributionItem find(String id) {
		if (!coolItemsCreated) {
			return super.find(id);
		}
		for (Iterator e = coolBarItems.iterator(); e.hasNext();) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
			IContributionItem item = cbItem.getToolBarManager().find(id);
			if (item != null) return item;
		}
		return null;
	}
	
	protected CoolBarContributionItem findGroup(String id) {
		for (Iterator e = coolBarItems.iterator(); e.hasNext();) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
			if (cbItem.getId().equals(id))
				return cbItem;
		}
		return null;
	}
	protected CoolBarContributionItem findGroupFor(String id) {
		for (Iterator e = coolBarItems.iterator(); e.hasNext();) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
			IContributionItem item = cbItem.getToolBarManager().find(id);
			if (item != null)
				return cbItem;
		}
		return null;
	}
	/**
	 * NOT VALID for multiple CoolItem case
	 */
	public ToolBar getControl() {
		if (!coolItemsCreated) {
			return super.getControl();
		}
		return null;
	}

	public void prependToGroup(String groupName, IAction action) {
		if (!coolItemsCreated) {
			super.prependToGroup(groupName, action);
			return;
		}
		CoolBarContributionItem cbItem = findGroup(groupName);
		if (cbItem != null) {
			cbItem.getToolBarManager().prependToGroup(groupName, action);
			if (cbItem.hasDisplayableItems()) setVisible(true);
		} else {
			cbItem = findGroupFor(groupName);
			if (cbItem != null) {
				cbItem.getToolBarManager().prependToGroup(groupName, action);
				if (cbItem.hasDisplayableItems()) setVisible(true);
			}
		}
	}
	public void prependToGroup(String groupName, IContributionItem item) {
		if (!coolItemsCreated) {
			super.prependToGroup(groupName, item);
			return;
		}
		CoolBarContributionItem cbItem = findGroup(groupName);
		if (cbItem != null) {
			cbItem.getToolBarManager().prependToGroup(groupName, item);
			if (cbItem.hasDisplayableItems()) setVisible(true);
		} else {
			cbItem = findGroupFor(groupName);
			if (cbItem != null) {
				cbItem.getToolBarManager().prependToGroup(groupName, item);
				if (cbItem.hasDisplayableItems()) setVisible(true);
			}
		}
	}


	public void update(boolean force) {
		if (!coolItemsCreated) {
			super.update(force);
			return;
		}
		for (Iterator e = coolBarItems.iterator(); e.hasNext();) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
			cbItem.getToolBarManager().update(force);
		}
		getParentManager().update(force);
	}

	public CoolItemMultiToolBarManager(CoolBarManager parentManager, String id, boolean active) {
		super(parentManager.getStyle());
		this.parentManager = parentManager;
		this.active = active;
		this.id = id;
	}
	public void createCoolBarContributionItems() {
		if (!isCoolItemGrouped()) {
			// Don't create groups.
			this.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
			CoolBarContributionItem coolBarItem = new CoolBarContributionItem(parentManager, this, id);
			parentManager.add(coolBarItem);
			this.setVisible(active);
			return;
		}
		ArrayList groupedItems = getGroups();
		if (groupedItems.size() > 0) {
			coolItemsCreated = true;
		}
		for (Iterator groupIter = groupedItems.iterator(); groupIter.hasNext();) {
			ArrayList items = (ArrayList) groupIter.next();
			if (!items.isEmpty()) {
				String groupId = ((IContributionItem) items.get(0)).getId();
				CoolItemToolBarManager tBarMgr = createGroup(groupId);
				for (Iterator itemIter = items.iterator(); itemIter.hasNext();) {
					tBarMgr.add((IContributionItem) itemIter.next());
				}
			}
		}
	}
	protected CoolItemToolBarManager createGroup(String groupId) {
		CoolItemToolBarManager tBarMgr = new CoolItemToolBarManager(parentManager.getStyle());
		tBarMgr.setOverrides(getOverrides());
		CoolBarContributionItem coolBarItem = new CoolBarContributionItem(parentManager, tBarMgr, groupId);
		parentManager.add(coolBarItem);
		coolBarItems.add(coolBarItem);
		coolBarItem.setVisible(active);
		return tBarMgr;
	}
	/**
	 * NOT VALID for multiple CoolItem case
	 */
	protected CoolBarContributionItem getCoolBarItem() {
		if (!coolItemsCreated) {
			return super.getCoolBarItem();
		}
		return null;
	}
	public IContributionItem[] getItems() {
		if (!coolItemsCreated) {
			return super.getItems();
		}
		ArrayList allItems = new ArrayList();
		for (Iterator e = coolBarItems.iterator(); e.hasNext();) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
			IContributionItem[] items = cbItem.getToolBarManager().getItems();
			for (int i=0; i<items.length; i++) {
				allItems.add(items[i]);
			}
		}
		IContributionItem[] items = new IContributionItem[allItems.size()];
		allItems.toArray(items);
		return items;
	}
	protected boolean hasDynamicItems() {
		if (!coolItemsCreated) {
			return super.hasDynamicItems();
		}
		return false;
	}	
	protected ArrayList getGroups() {
		ArrayList groups = new ArrayList();
		IContributionItem[] items = getItems();
		if (items.length == 0)
			return groups;
		ArrayList group = new ArrayList();
		IContributionItem firstItem = items[0];
		int start = 0;
		if (isCoolItemMarker(firstItem)) {
			group.add(firstItem);
			start = 1;
		} else {
			group.add(new CoolItemGroupMarker(id));
		}

		for (int i = start; i < items.length; i++) {
			IContributionItem item = items[i];
			if (isCoolItemMarker(item)) {
				groups.add(group);
				group = new ArrayList();
				group.add(item);
			} else {
				group.add(item);
			}
		}
		groups.add(group);
		return groups;
	}

	public void insertAfter(String ID, IAction action) {
		if (!coolItemsCreated) {
			super.insertAfter(ID, action);
			return;
		}
		CoolBarContributionItem cbItem = findGroupFor(ID);
		if (cbItem != null) {
			cbItem.getToolBarManager().insertAfter(ID, action);
			if (cbItem.hasDisplayableItems()) setVisible(true);
		}
	}
	public void insertAfter(String ID, IContributionItem item) {
		if (!coolItemsCreated) {
			super.insertAfter(ID, item);
			return;
		}
		CoolBarContributionItem cbItem = findGroupFor(ID);
		if (cbItem != null) {
			cbItem.getToolBarManager().insertAfter(ID, item);
			if (cbItem.hasDisplayableItems()) setVisible(true);
		}
	}
	public void insertBefore(String ID, IAction action) {
		if (!coolItemsCreated) {
			super.insertBefore(ID, action);
			return;
		}
		CoolBarContributionItem cbItem = findGroupFor(ID);
		if (cbItem != null) {
			cbItem.getToolBarManager().insertBefore(ID, action);
			if (cbItem.hasDisplayableItems()) setVisible(true);
		}
	}
	public void insertBefore(String ID, IContributionItem item) {
		if (!coolItemsCreated) {
			super.insertBefore(ID, item);
			return;
		}
		CoolBarContributionItem cbItem = findGroupFor(ID);
		if (cbItem != null) {
			cbItem.getToolBarManager().insertBefore(ID, item);
			if (cbItem.hasDisplayableItems()) setVisible(true);
		}
	}
	public boolean isDirty() {
		if (!coolItemsCreated) {
			return super.isDirty();
		}
		for (Iterator e = coolBarItems.iterator(); e.hasNext();) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
			if (cbItem.getToolBarManager().isDirty())
				return true;
		}
		return super.isDirty();
	}
	protected boolean isCoolItemMarker(IContributionItem item) {
		return item instanceof CoolItemGroupMarker;
	}
	protected boolean isCoolItemGrouped() {
		IContributionItem[] items = getItems();
		for (int i=0; i<items.length; i++) {
			IContributionItem item = items[i];
			if (isCoolItemMarker(item)) return true;
		}
		return false;
	}


public IContributionItem remove(IContributionItem item) {
	if (!coolItemsCreated) {
		return super.remove(item);
	}
	CoolBarContributionItem cbItem = findGroupFor(item.getId());
	if (cbItem != null) {
		IContributionItem removed = cbItem.getToolBarManager().remove(item);
		if (!cbItem.hasDisplayableItems()) cbItem.setVisible(false); 
		return removed;
	}
	return null;
}

	public IContributionItem remove(String ID) {
		if (!coolItemsCreated) {
			return super.remove(ID);
		}
		CoolBarContributionItem cbItem = findGroupFor(ID);
		if (cbItem != null) {
			IContributionItem removed = cbItem.getToolBarManager().remove(ID);
			if (!cbItem.hasDisplayableItems()) cbItem.setVisible(false); 
			return removed;
		}
		return null;
	}
	public void removeAll() {
		if (!coolItemsCreated) {
			super.removeAll();
			return;
		}
		for (Iterator e = coolBarItems.iterator(); e.hasNext();) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
			cbItem.getToolBarManager().removeAll();
		}
		coolBarItems.clear();
		setDirty(true);
	}

	/**
	 * NOT VALID for multiple CoolItem case
	 */
	protected void setCoolBarItem(CoolBarContributionItem coolBarItem) {
		if (!coolItemsCreated) {
			super.setCoolBarItem(coolBarItem);
		}
	}
	protected void setVisible(boolean set) {
		super.setVisible(set);
		if (!coolItemsCreated) {
			return;
		}
		for (Iterator e = coolBarItems.iterator(); e.hasNext();) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
			cbItem.setVisible(set);
		}
	}
	protected void setVisible(boolean set, boolean forceVisibility) {
		super.setVisible(set, forceVisibility);
		if (!coolItemsCreated) {
			return;
		}
		for (Iterator e = coolBarItems.iterator(); e.hasNext();) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
			cbItem.setVisible(set, forceVisibility);
		}
	}
}
