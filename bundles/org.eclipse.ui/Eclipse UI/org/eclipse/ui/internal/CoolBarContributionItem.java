package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.*;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.RetargetAction;

/**
 * A CoolBarContributionItem is an item which realizes itself and its items
 * in as a CoolItem in a CoolBar control.  CoolItems map to ToolBars within a
 * CoolBar.
 */
public class CoolBarContributionItem extends ContributionItem implements IContributionItem {
	/**
	 * The visibility of the item,
	 */
	private boolean visible = true;

	/**
	 * The parent contribution manager.
	 */
	private CoolBarManager parentManager;
	private ToolBarManager toolBarManager;

	private String id;

	/**
	 */
	public CoolBarContributionItem() {
	}
	/**
	 * Creates a CoolBarContributionItem for the given CoolBarManager.
	 */
	public CoolBarContributionItem(CoolBarManager parent, String id) {
		// have the item take on the style of its parent
		toolBarManager = new ToolBarManager(parent.getStyle());
		this.parentManager = parent;
		this.id = id;
		CoolBar parentControl = parent.getControl();
		if (parentControl != null)
			createControl(parentControl);
		parent.add(this);
	}
	public ToolBar createControl(Composite parent) {
		ToolBar tBar = toolBarManager.createControl(parent);
		// add support for popup menu
		tBar.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				getParentManager().popupCoolBarMenu(e);
			}
		});
		return tBar;
	}
	public boolean equals(Object object) {
		if (object instanceof CoolBarContributionItem) {
			CoolBarContributionItem item = (CoolBarContributionItem) object;
			return id.equals(item.id);
		}
		return false;
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
		if (parentManager != null) 
			parentManager.markDirty();
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
	public ToolBar getControl() {
		return toolBarManager.getControl();
	}
	/**
	 * Returns the identifier of this contribution item.
	 * The id is used for retrieving an item from its manager.
	 *
	 * @return the contribution item identifier, or <code>null</code>
	 *   if none
	 */
	public String getId() {
		return id;
	}
	public IContributionItem[] getItems() {
		return toolBarManager.getItems();
	}
	/**
	 * Returns the parent manager.
	 *
	 * @return the parent manager
	 */
	public CoolBarManager getParentManager() {
		return parentManager;
	}
	public ToolBarManager getToolBarManager() {
		return toolBarManager;
	}
	public int hashCode() {
		return id.hashCode();
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
	 * Returns whether this contribution item is a separator.
	 * This information is used to enable hiding of unnecessary separators.
	 *
	 * @return <code>true</code> if this item is a separator, and
	 *  <code>false</code> for normal items
	 * @see Separator
	 */
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
		if (parentManager == null)
			return true;
		return visible;
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