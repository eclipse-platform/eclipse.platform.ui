/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.action;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.accessibility.AccessibleListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
public class ToolBarManager extends ContributionManager implements
		IToolBarManager {

	/**
	 * The tool bar items style; <code>SWT.NONE</code> by default.
	 */
	private int itemStyle = SWT.NONE;

	/**
	 * The tool bat control; <code>null</code> before creation and after
	 * disposal.
	 */
	private ToolBar toolBar = null;

	/**
	 * The menu manager to the context menu associated with the toolbar.
	 * 
	 * @since 3.0
	 */
	private MenuManager contextMenuManager = null;

	/**
	 * Creates a new tool bar manager with the default SWT button style. Use the
	 * <code>createControl</code> method to create the tool bar control.
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
	 * manager is disposed.
	 * 
	 * @param toolbar
	 *            the tool bar control
	 */
	public ToolBarManager(ToolBar toolbar) {
		this();
		this.toolBar = toolbar;
	}

	/**
	 * Creates and returns this manager's tool bar control. Does not create a
	 * new control if one already exists.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the tool bar control
	 */
	public ToolBar createControl(Composite parent) {
		if (!toolBarExist() && parent != null) {
			toolBar = new ToolBar(parent, itemStyle);
			toolBar.setMenu(getContextMenuControl());
			update(false);
		}

		toolBar.getAccessible().addAccessibleListener(getAccessibleListener());
		return toolBar;
	}

	/**
	 * Get the accessible listener for the tool bar.
	 * 
	 * @return AccessibleListener
	 */
	private AccessibleListener getAccessibleListener() {
		return new AccessibleAdapter() {
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
		for (int i = 0; i < items.length; i++) {
			items[i].dispose();
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
	 * parent when the number of items crosses the zero threshold. Subclasses
	 * should override this method to implement their own re-layout strategy
	 * 
	 * @param layoutBar
	 *            the tool bar control
	 * @param oldCount
	 *            the old number of items
	 * @param newCount
	 *            the new number of items
	 */
	protected void relayout(ToolBar layoutBar, int oldCount, int newCount) {
		if ((oldCount == 0) != (newCount == 0))
			layoutBar.getParent().layout();
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

	/*
	 * (non-Javadoc) Method declared on IContributionManager.
	 */
	public void update(boolean force) {

		//	long startTime= 0;
		//	if (DEBUG) {
		//		dumpStatistics();
		//		startTime= (new Date()).getTime();
		//	}

		if (isDirty() || force) {

			if (toolBarExist()) {

				int oldCount = toolBar.getItemCount();

				// clean contains all active items without double separators
				IContributionItem[] items = getItems();
				ArrayList clean = new ArrayList(items.length);
				IContributionItem separator = null;
				//			long cleanStartTime= 0;
				//			if (DEBUG) {
				//				cleanStartTime= (new Date()).getTime();
				//			}
				for (int i = 0; i < items.length; ++i) {
					IContributionItem ci = items[i];
					if (!ci.isVisible())
						continue;
					if (ci.isSeparator()) {
						// delay creation until necessary
						// (handles both adjacent separators, and separator at
						// end)
						separator = ci;
					} else {
						if (separator != null) {
							if (clean.size() > 0) // no separator if first item
								clean.add(separator);
							separator = null;
						}
						clean.add(ci);
					}
				}
				//			if (DEBUG) {
				//				System.out.println(" Time needed to build clean vector: " +
				// ((new Date()).getTime() - cleanStartTime));
				//			}

				// determine obsolete items (removed or non active)
				ToolItem[] mi = toolBar.getItems();
				ArrayList toRemove = new ArrayList(mi.length);
				for (int i = 0; i < mi.length; i++) {
					Object data = mi[i].getData();
					if (data == null
							|| !clean.contains(data)
							|| (data instanceof IContributionItem && ((IContributionItem) data)
									.isDynamic())) {
						toRemove.add(mi[i]);
					}
				}

				// Turn redraw off if the number of items to be added
				// is above a certain threshold, to minimize flicker,
				// otherwise the toolbar can be seen to redraw after each item.
				// Do this before any modifications are made.
				// We assume each contribution item will contribute at least one
				// toolbar item.
				boolean useRedraw = (clean.size() - (mi.length - toRemove
						.size())) >= 3;
				if (useRedraw) {
					toolBar.setRedraw(false);
				}

				// remove obsolete items
				for (int i = toRemove.size(); --i >= 0;) {
					ToolItem item = (ToolItem) toRemove.get(i);
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
				IContributionItem src, dest;
				mi = toolBar.getItems();
				int srcIx = 0;
				int destIx = 0;
				for (Iterator e = clean.iterator(); e.hasNext();) {
					src = (IContributionItem) e.next();

					// get corresponding item in SWT widget
					if (srcIx < mi.length)
						dest = (IContributionItem) mi[srcIx].getData();
					else
						dest = null;

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
				if (useRedraw) {
					toolBar.setRedraw(true);
				}

				int newCount = toolBar.getItemCount();
				relayout(toolBar, oldCount, newCount);
			}

		}

		//	if (DEBUG) {
		//		System.out.println(" Time needed for update: " + ((new
		// Date()).getTime() - startTime));
		//		System.out.println();
		//	}
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

}