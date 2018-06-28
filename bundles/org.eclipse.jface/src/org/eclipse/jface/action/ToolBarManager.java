/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 457211
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 457214
 *******************************************************************************/
package org.eclipse.jface.action;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.accessibility.AccessibleListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A tool bar manager is a contribution manager which realizes itself and its
 * items in a tool bar control.
 * <p>
 * This class may be instantiated; it may also be subclassed if a more
 * sophisticated layout is required.
 * </p>
 */
public class ToolBarManager extends ContributionManager implements IToolBarManager {

	/**
	 * The tool bar items style; <code>SWT.NONE</code> by default.
	 */
	private int itemStyle = SWT.NONE;

	/**
	 * The tool bar control; <code>null</code> before creation and after
	 * disposal.
	 */
	private ToolBar toolBar;

	/**
	 * The menu manager to the context menu associated with the toolbar.
	 *
	 * @since 3.0
	 */
	private MenuManager contextMenuManager;

	/**
	 * Creates a new tool bar manager with the default SWT button style. Use the
	 * {@link #createControl(Composite)} method to create the tool bar control.
	 */
	public ToolBarManager() {
		//Do nothing if there are no parameters
	}

	/**
	 * Creates a tool bar manager with the given SWT button style. Use the
	 * <code>createControl</code> method to create the tool bar control.
	 *
	 * @param style
	 *            the tool bar item style
	 * @see org.eclipse.swt.widgets.ToolBar for valid style bits
	 */
	public ToolBarManager(int style) {
		itemStyle = style;
	}

	/**
	 * Creates a tool bar manager for an existing tool bar control. This manager
	 * becomes responsible for the control, and will dispose of it when the
	 * manager is disposed. <strong>NOTE</strong> When creating a ToolBarManager
	 * from an existing {@link ToolBar} you will not get the accessible listener
	 * provided by JFace.
	 *
	 * @see #ToolBarManager()
	 * @see #ToolBarManager(int)
	 *
	 * @param toolbar
	 *            the tool bar control
	 */
	public ToolBarManager(ToolBar toolbar) {
		this();
		this.toolBar = toolbar;
		if (toolBarExist()) {
			this.itemStyle = toolBar.getStyle();
		}
	}

	/**
	 * Sets SWT button style for new tool bar controls created
	 * in the {@code createControl(Composite)} method. It does not
	 * affect already existing tool bar control.
	 *
	 * @param style
	 *            the tool bar item style
	 * @since 3.10
	 */
	public void setStyle(int style) {
		itemStyle = style;
	}

	/**
	 * Creates and returns this manager's tool bar control. Does not create
	 * a new control if one already exists and is not disposed.
	 * Also create an {@link AccessibleListener} for the {@link ToolBar}.
	 *
	 * @param parent
	 *            the parent control
	 * @return the tool bar control
	 */
	public ToolBar createControl(Composite parent) {
		if (!toolBarExist() && parent != null) {
			toolBar = new ToolBar(parent, itemStyle);
			toolBar.setMenu(getContextMenuControl());
			update(true);

			toolBar.getAccessible().addAccessibleListener(getAccessibleListener());
		}

		return toolBar;
	}

	/**
	 * Get the accessible listener for the tool bar.
	 *
	 * @return AccessibleListener
	 *
	 * @since 3.1
	 */
	private AccessibleListener getAccessibleListener() {
		return new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				if (e.childID != ACC.CHILDID_SELF) {
					ToolItem item = toolBar.getItem(e.childID);
					if (item != null) {
						String toolTip = item.getToolTipText();
						if (toolTip != null) {
							e.result = toolTip;
						}
					}
				}
			}
		};

	}

	/**
	 * Disposes of this tool bar manager and frees all allocated SWT resources.
	 * Notifies all contribution items of the dispose. Note that this method
	 * does not clean up references between this tool bar manager and its
	 * associated contribution items. Use <code>removeAll</code> for that
	 * purpose.
	 */
	public void dispose() {

		if (toolBarExist()) {
			toolBar.dispose();
		}
		toolBar = null;

		IContributionItem[] items = getItems();
		for (IContributionItem item : items) {
			item.dispose();
		}

		if (getContextMenuManager() != null) {
			getContextMenuManager().dispose();
			setContextMenuManager(null);
		}
	}

	/**
	 * Returns the tool bar control for this manager.
	 *
	 * @return the tool bar control, or <code>null</code> if none (before
	 *         creating or after disposal)
	 */
	public ToolBar getControl() {
		return toolBar;
	}

	/**
	 * Re-lays out the tool bar.
	 * <p>
	 * The default implementation of this framework method re-lays out the
	 * parent when the number of items are different and the new count != 0
	 *
	 * @param layoutBar
	 *            the tool bar control
	 * @param oldCount
	 *            the old number of items
	 * @param newCount
	 *            the new number of items
	 */
	protected void relayout(ToolBar layoutBar, int oldCount, int newCount) {
		if (oldCount != newCount && newCount != 0) {
			Point beforePack = layoutBar.getSize();
			layoutBar.pack(true);
			Point afterPack = layoutBar.getSize();

			// If the TB didn't change size then we're done
			if (beforePack.equals(afterPack)) {
				return;
			}

			// OK, we need to re-layout the TB
			layoutBar.getParent().layout();

			// Now, if we're in a CoolBar then change the CoolItem size as well
			if (layoutBar.getParent() instanceof CoolBar) {
				CoolBar cb = (CoolBar) layoutBar.getParent();
				CoolItem[] items = cb.getItems();
				for (CoolItem item : items) {
					if (item.getControl() == layoutBar) {
						Point curSize = item.getSize();
						item.setSize(curSize.x + (afterPack.x - beforePack.x),
								curSize.y + (afterPack.y - beforePack.y));
						return;
					}
				}
			}
		}
	}

	/**
	 * Returns whether the tool bar control is created and not disposed.
	 *
	 * @return <code>true</code> if the control is created and not disposed,
	 *         <code>false</code> otherwise
	 */
	private boolean toolBarExist() {
		return toolBar != null && !toolBar.isDisposed();
	}

	@Override
	public void update(boolean force) {

		if (!isDirty() && !force) {
			return;
		}

		if (!toolBarExist()) {
			return;
		}

		int oldCount = toolBar.getItemCount();

		// clean contains all active items without double separators
		IContributionItem[] items = getItems();
		ArrayList<IContributionItem> clean = new ArrayList<>(items.length);
		IContributionItem separator = null;
		for (IContributionItem ci : items) {
			if (!isChildVisible(ci)) {
				continue;
			}
			if (ci.isSeparator()) {
				// delay creation until necessary (handles both adjacent
				// separators, and separator at end)
				separator = ci;
			} else {
				if (separator != null) {
					if (clean.size() > 0) {
						clean.add(separator);
					}
					separator = null;
				}
				clean.add(ci);
			}
		}

		// determine obsolete items (removed or non active)
		ToolItem[] mi = toolBar.getItems();
		ArrayList<ToolItem> toRemove = new ArrayList<>(mi.length);
		for (ToolItem item : mi) {
			// there may be null items in a toolbar
			if (item == null) {
				continue;
			}

			Object data = item.getData();
			if (data == null || !clean.contains(data)
					|| (data instanceof IContributionItem && ((IContributionItem) data).isDynamic())) {
				toRemove.add(item);
			}
		}

		try {
			toolBar.setRedraw(false);

			// remove obsolete items
			for (int i = toRemove.size(); --i >= 0;) {
				ToolItem item = toRemove.get(i);
				if (!item.isDisposed()) {
					Control ctrl = item.getControl();
					if (ctrl != null) {
						item.setControl(null);
						ctrl.dispose();
					}
					item.dispose();
				}
			}

			// add new items
			IContributionItem dest;
			mi = toolBar.getItems();
			int srcIx = 0;
			int destIx = 0;
			for (IContributionItem src : clean) {

				// get corresponding item in SWT widget
				if (srcIx < mi.length) {
					dest = (IContributionItem) mi[srcIx].getData();
				} else {
					dest = null;
				}

				if (dest != null && src.equals(dest)) {
					srcIx++;
					destIx++;
					continue;
				}

				if (dest != null && dest.isSeparator() && src.isSeparator()) {
					mi[srcIx].setData(src);
					srcIx++;
					destIx++;
					continue;
				}

				int start = toolBar.getItemCount();
				src.fill(toolBar, destIx);
				int newItems = toolBar.getItemCount() - start;
				for (int i = 0; i < newItems; i++) {
					ToolItem item = toolBar.getItem(destIx++);
					item.setData(src);
				}
			}

			// remove any old tool items not accounted for
			for (int i = mi.length; --i >= srcIx;) {
				ToolItem item = mi[i];
				if (!item.isDisposed()) {
					Control ctrl = item.getControl();
					if (ctrl != null) {
						item.setControl(null);
						ctrl.dispose();
					}
					item.dispose();
				}
			}

			setDirty(false);

			// turn redraw back on if we turned it off above
		} finally {
			toolBar.setRedraw(true);
		}

		int newCount = toolBar.getItemCount();

		// If we're forcing a change then ensure that we re-layout
		// everything
		if (force) {
			oldCount = newCount + 1;
		}

		relayout(toolBar, oldCount, newCount);

	}

	/**
	 * Returns the control of the Menu Manager. If the menu manager does not
	 * have a control then one is created.
	 *
	 * @return menu widget associated with manager
	 */
	private Menu getContextMenuControl() {
		if ((contextMenuManager != null) && (toolBar != null)) {
			Menu menuWidget = contextMenuManager.getMenu();
			if ((menuWidget == null) || (menuWidget.isDisposed())) {
				menuWidget = contextMenuManager.createContextMenu(toolBar);
			}
			return menuWidget;
		}
		return null;
	}

	/**
	 * Returns the context menu manager for this tool bar manager.
	 *
	 * @return the context menu manager, or <code>null</code> if none
	 * @since 3.0
	 */
	public MenuManager getContextMenuManager() {
		return contextMenuManager;
	}

	/**
	 * Sets the context menu manager for this tool bar manager to the given menu
	 * manager. If the tool bar control exists, it also adds the menu control to
	 * the tool bar.
	 *
	 * @param contextMenuManager
	 *            the context menu manager, or <code>null</code> if none
	 * @since 3.0
	 */
	public void setContextMenuManager(MenuManager contextMenuManager) {
		this.contextMenuManager = contextMenuManager;
		if (toolBar != null) {
			toolBar.setMenu(getContextMenuControl());
		}
	}

	/**
	 * Computes real item visibility considering possibly overridden state from
	 * manager
	 */
	private boolean isChildVisible(IContributionItem item) {
		IContributionManagerOverrides overrides = getOverrides();
		if(overrides == null) {
			return item.isVisible();
		}
		Boolean v = overrides.getVisible(item);
		return v != null ? v.booleanValue() : item.isVisible();
	}
}
