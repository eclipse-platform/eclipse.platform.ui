package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;

/**
 * A CoolBarContributionItem is an item which realizes itself and its items
 * in as a CoolItem in a CoolBar control.  CoolItems map to ToolBars within a
 * CoolBar.
 */
public class CoolBarContributionItem extends ContributionItem {
	/**
	 * The visibility of the item,
	 */
	private boolean visible = true;

	/**
	 * The toolbar contribution manager.
	 */
	private CoolItemToolBarManager toolBarManager;

	/**
	 * Need to remember order information about the item since
	 * item layout order is dynamic for coolbars.  We know the
	 * order the of all of the CoolBarContribution items, but when 
	 * an item is dynamically added and removed we need to remember 
	 * its order relationship to the items around it.
	 */
	private boolean orderBefore = false;
	private boolean orderAfter = false;
	/**
	 */
	public CoolBarContributionItem() {
	}
	/**
	 * Creates a CoolBarContributionItem for the given CoolBarManager.
	 */
	public CoolBarContributionItem(CoolBarManager parent, String id) {
		this(parent, new CoolItemToolBarManager(parent.getStyle()), id);
	}
	/**
	 * Creates a CoolBarContributionItem for the given CoolBarManager and CoolItemToolBarManager.
	 */
	public CoolBarContributionItem(CoolBarManager parent, CoolItemToolBarManager tBarMgr, String id) {
		super(id);
		this.toolBarManager = tBarMgr;
		tBarMgr.setParentMgr(parent);
		tBarMgr.setCoolBarItem(this);
		parent.setToolBarLayout(this);
	}
	/**
	 * Creates the SWT control for the CoolBarContributionItem.
	 */
	protected ToolBar createControl() {
		ToolBar tBar = null;
		CoolBar parentControl = getParentManager().getControl();
		if (parentControl != null) {
			tBar = toolBarManager.createControl(parentControl);
		}
		return tBar;
	}
	/**
	 */
	public void dispose() {
		if (toolBarManager != null) {
			toolBarManager.removeAll();
		}
	}		
	/**
	 */
	public boolean equals(Object object) {
		if (object instanceof CoolBarContributionItem) {
			CoolBarContributionItem item = (CoolBarContributionItem) object;
			return getId().equals(item.getId());
		}
		return false;
	}
	/**
	 * Fills the given composite control with controls representing this 
	 * contribution item.  Used by <code>StatusLineManager</code>.
	 *
	 * @param parent the parent control
	 */
	public void fill(Composite parent) {
		// invalid
	}
	/**
	 * Fills the given menu with controls representing this contribution item.
	 * Used by <code>MenuManager</code>.
	 *
	 * @param parent the parent menu
	 * @param index the index where the controls are inserted,
	 *   or <code>-1</code> to insert at the end
	 */
	public void fill(Menu parent, int index) {
		// invalid
	}
	/**
	 * Fills the given tool bar with controls representing this contribution item.
	 * Used by <code>ToolBarManager</code>.
	 *
	 * @param parent the parent tool bar
	 * @param index the index where the controls are inserted,
	 *   or <code>-1</code> to insert at the end
	 */
	public void fill(ToolBar parent, int index) {
		// invalid
	}
	/**
	 */
	public ToolBar getControl() {
		ToolBar tBar = toolBarManager.getControl();
		if (tBar == null) {
			tBar = createControl();
		}
		return tBar;
	}
	/**
	 */
	public IContributionItem[] getItems() {
		return toolBarManager.getItems();
	}
	/**
	 * Returns the parent manager.
	 *
	 * @return the parent manager
	 */
	public CoolBarManager getParentManager() {
		return getToolBarManager().getParentManager();
	}
	/**
	 * Returns the toolbar manager for this contribution item
	 */
	public CoolItemToolBarManager getToolBarManager() {
		return toolBarManager;
	}
	/**
	 */
	public boolean hasDisplayableItems() {
		IContributionItem[] items = toolBarManager.getItems();
		for (int i=0; i<items.length; i++) {
			IContributionItem item = items[i];
			if (item.isSeparator() || item.isGroupMarker() || (!item.isVisible())) continue;
			return true;
		}
		return false;
	}
	/**
	 * Returns whether this contribution item isEmpty.  If no items
	 * exist or only marker/separator items exist, this method will
	 * answer true.
	 */
	public boolean isEmpty() {
		IContributionItem[] items = toolBarManager.getItems();
		for (int i=0; i<items.length; i++) {
			IContributionItem item = items[i];
			if (item.isSeparator() || item.isGroupMarker()) continue;
			return false;
		}
		return true;
	}
	/**
	 */
	public int hashCode() {
		return getId().hashCode();
	}
	/**
	 * Returns whether this contribution item is dynamic. A dynamic contribution
	 * item contributes items conditionally, dependent on some internal state.
	 *
	 * @return <code>true</code> if this item is dynamic, and
	 *  <code>false</code> for normal items
	 */
	public boolean isDynamic() {
		return true;
	}
	/**
	 * Returns whether this contribution item is a group marker.
	 * This information is used when adding items to a group.
	 *
	 * @return <code>true</code> if this item is a group marker, and
	 *  <code>false</code> for normal items
	 *
	 * @see IContributionManager#appendToGroup
	 * @see IContributionManager#prependToGroup
	 */
	public boolean isGroupMarker() {
		return true;
	}
	/**
	 * Returns whether this contribution item is ordered after the 
	 * the item before it.
	 */
	protected boolean isOrderAfter() {
		return orderAfter;
	}
	/**
	 * Returns whether this contribution item is ordered before a the
	 * item after it.
	 */
	protected boolean isOrderBefore() {
		return orderBefore;
	}
	/**
	 * Returns whether this contribution item is a separator.
	 * This information is used to enable hiding of unnecessary separators.
	 *
	 * @return <code>true</code> if this item is a separator, and
	 *  <code>false</code> for normal items
	 * @see Separator
	 */
	public boolean isSeparator() {
		return false;
	}
	/**
	 * Returns whether the contribution list is visible.
	 * If the visibility is <code>true</code> then each item within the manager 
	 * appears within the parent manager.  Otherwise, the items are not visible.
	 *
	 * @return <code>true</code> if the manager is visible
	 */
	public boolean isVisible() {
		if (getParentManager() == null)
			return true;
		return visible;
	}
	/**
	 * Sets whether this contribution item is ordered after the
	 * item before it.
	 */
	protected void setOrderAfter(boolean orderAfter) {
		this.orderAfter = orderAfter;
	}
	/**
	 * Sets whether this contribution item is ordered before the
	 * item after it.
	 */
	protected void setOrderBefore(boolean orderBefore) {
		this.orderBefore = orderBefore;
	}
	/**
	 * Sets the visibility of the manager.  If the visibility is <code>true</code>
	 * then each item within the manager appears within the parent manager.
	 * Otherwise, the items are not visible.
	 *
	 * @param visible the new visibility
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		if (getParentManager() != null) 
			getParentManager().markDirty();
	}
	/**
	 * Sets the visibility of the manager. If the visibility is <code>true</code>
	 * then each item within the manager appears within the parent manager.
	 * Otherwise, the items are not visible if force visibility is
	 * <code>true</code>, or grayed out if force visibility is <code>false</code>
	 * <p>
	 * This is a workaround for the layout flashing when editors contribute
	 * large amounts of items.</p>
	 *
	 * @param visible the new visibility
	 * @param forceVisibility whether to change the visibility or just the
	 * 		enablement state. This parameter is ignored if visible is 
	 * 		<code>true</code>.
	 */
	public void setVisible(boolean visible, boolean forceVisibility) {
		if (visible) {
			if (!isVisible()) setVisible(true);
		} else {
			if (forceVisibility) {
				if (isVisible()) setVisible(false);
			} else {
				if (!isVisible()) setVisible(true);
			}
		}
	}
	/**
	 * Updates any SWT controls cached by this contribution item with any
	 * changes which have been made to this contribution item since the last update.
	 * Called by contribution manager update methods.
	 */
	public void update(boolean force) {
		toolBarManager.update(force);
	}
}