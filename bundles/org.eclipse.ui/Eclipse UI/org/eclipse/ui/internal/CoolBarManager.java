package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;

/**
 * WORK IN PROGRESS FOR COOLBAR SUPPORT
 */
public class CoolBarManager extends ContributionManager implements IToolBarManager {
	/** 
	 * The tool bar items style; <code>SWT.NONE</code> by default.
	 */
	private int itemStyle = SWT.NONE;

	/** 
	 * The cool bat control; <code>null</code> before creation
	 * and after disposal.
	 */
	private CoolBar coolBar = null;
	
	private CoolBarLayout coolBarLayout = new CoolBarLayout();
	
	/**
	 */
	public CoolBarManager() {
	}
	/**
	 */
	public CoolBarManager(int style) {
		itemStyle = style;
	}
	/**
	 * Adds an action as a contribution item to this manager.
	 * Equivalent to <code>add(new ActionContributionItem(action))</code>.
	 * 
	 * @param action the action
	 */
	public void add(IAction action) {
		// what to do?
	}
	/**
	 * Adds a contribution item to this manager.
	 * 
	 * @exception IllegalArgumentException if the type of item is
	 * not valid
	 */
	public void add(IContributionItem item) {
		validateItem(item);
		super.add(item);
	}
	/**
	 * Adds a contribution item to the start or end of the group 
	 * with the given name.
	 *
	 * @param itemId the id of the CoolBarContributionItem
	 * @param item the contribution item
	 * @param append <code>true</code> to add to the end of the group, 
	 *   and <code>false</code> to add the beginning of the group
	 * @exception IllegalArgumentException if there is no group with
	 *   the given name
	 */
	private void addToGroup(String itemId, IContributionItem item, boolean append) {
		IContributionItem[] items = getItems();
		for (int i = 0; i<items.length; i++) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem)items[i];
			String id = cbItem.getId();
			if (id != null && id.equalsIgnoreCase(itemId)) {
				item.setParent(cbItem);
				if (append) {
					cbItem.add(item);
				} else {
					IContributionItem firstItem = cbItem.getItems()[0];
					cbItem.insertBefore(firstItem.getId(), item);
				}
				return;
			}
		}
		throw new IllegalArgumentException("Group not found: " + itemId);//$NON-NLS-1$
	}
	/**
	 */
	private boolean coolBarExist() {
		return coolBar != null && !coolBar.isDisposed();
	}
	/**
	 */
	public CoolBar createControl(Composite parent) {
		if (!coolBarExist() && parent != null) {
			coolBar = new CoolBar(parent, itemStyle);
			coolBar.addListener(SWT.Resize, new Listener() {
				public void handleEvent(Event event) {
					coolBar.getParent().layout();
				}
			});
			IContributionItem[] items = getItems();
			for (int i = 0; i<items.length; i++) {
				CoolBarContributionItem cbItem = (CoolBarContributionItem)items[i];
				if (cbItem.getControl() == null) {
					cbItem.createControl(coolBar);
				} 
			}
			update(false);
		}
		return coolBar;
	}
	/**
	 */
	public void dispose() {
		if (coolBarExist())
			coolBar.dispose();
		coolBar = null;
	}
	public CoolBar getControl() {
		return coolBar;
	}
	/**
	 * Returns the overrides for the items of this manager.
	 * 
	 * @return the overrides for the items of this manager
	 * @since 2.0 
	 */
	public IContributionManagerOverrides getOverrides() {
		/// ????
		return null;
	}
	/**
	 */
	public int getStyle() {
		return itemStyle;
	}
	/**
	 */
	private ArrayList getContributionIds() {
		IContributionItem[] items = getItems();
		ArrayList ids = new ArrayList(items.length);
		for (int i = 0; i<items.length; i++) {
			IContributionItem item = items[i];
			ids.add(item.getId());
		}
		return ids;
	}
	/**
	 */
	public CoolBarLayout getLayout() {
		if (!coolBarExist()) return null;
// System.out.println("get layout");
		CoolItem[] coolItems = coolBar.getItems();
		ArrayList itemsInOrder = new ArrayList(coolItems.length);
		for (int i=0; i<coolItems.length; i++) {
			CoolBarContributionItem item = (CoolBarContributionItem)coolItems[i].getData();
			itemsInOrder.add(item);
// System.out.println("item " + item.getId());
		}
		coolBarLayout.items = itemsInOrder;
		coolBarLayout.itemWrapIndices = coolBar.getWrapIndices();
		coolBarLayout.itemSizes = coolBar.getItemSizes();
// System.out.println(coolBarLayout.toString());
		return coolBarLayout;
	}
	/**
	 */
	private ArrayList getCoolItemIds() {
		CoolItem[] coolItems = coolBar.getItems();
		ArrayList ids = new ArrayList(coolItems.length);
		for (int i=0; i<coolItems.length; i++) {
			CoolBarContributionItem group = (CoolBarContributionItem)coolItems[i].getData();
			ids.add(group.getId());
		}
		return ids;
	}
	/**
	 * Inserts a contribution item for the given action after the item 
	 * with the given id.
	 * Equivalent to
	 * <code>insertAfter(id,new ActionContributionItem(action))</code>.
	 *
	 * @param id the contribution item id
	 * @param action the action to insert
	 */
	public void insertAfter(String id, IAction action) {
		// what to do?
	}
	/**
	 * Inserts a contribution item after the item with the given id.
	 *
	 * @param id the CoolBarContributionItem 
	 * @param item the contribution item to insert
	 * @exception IllegalArgumentException if there is no item with
	 *   the given id
	 * @exception IllegalArgumentException if the type of item is
	 * 	not valid
	 */
	public void insertAfter(String id, IContributionItem item) {
		validateItem(item);
		super.insertAfter(id, item);
	}
	/**
	 * Inserts a contribution item for the given action before the item 
	 * with the given id.
	 * Equivalent to
	 * <code>insertBefore(id,new ActionContributionItem(action))</code>.
	 *
	 * @param id the contribution item id
	 * @param action the action to insert
	 */
	public void insertBefore(String id, IAction action) {
		// what to do?
	}
	/**
	 * Inserts a contribution item before the item with the given id.
	 *
	 * @param id the CoolBarContributionItem 
	 * @param item the contribution item to insert
	 * @exception IllegalArgumentException if there is no item with
	 *   the given id
	 * @exception IllegalArgumentException if the type of item is
	 * 	not valid
	 */
	public void insertBefore(String id, IContributionItem item) {
		validateItem(item);
		super.insertBefore(id, item);
	}
	/**
	 */
	public void lockLayout(boolean value) {
		coolBarLayout.locked = value;
		coolBar.setLocked(value);
	}
	/**
	 */
	public void resetLayout() {
		CoolItem[] coolItems = coolBar.getItems();
		for (int i=0; i<coolItems.length; i++) {
			CoolItem coolItem = coolItems[i];
			coolItem.setData(null);
			coolItem.setControl(null);
			coolItem.dispose();
		}
		update(true);
		lockLayout(false);
	}
	/**
	 * Removes the given contribution item from the contribution items
	 * known to this manager.
	 *
	 * @param item the contribution item
	 * @return the <code>item</code> parameter if the item was removed,
	 *   and <code>null</code> if it was not found
	 * @exception IllegalArgumentException if the type of item is
	 * 	not valid
	 */
	public IContributionItem remove(IContributionItem item) {
		validateItem(item);
		return super.remove(item);
	}
	/**
	 */
	protected void relayout() {
		coolBar.getParent().layout();
	}
	/**
	 */
	public void setLayout(CoolBarLayout layout) {
		if (layout == null) {
			coolBarLayout = new CoolBarLayout();
			coolBar.setLocked(coolBarLayout.locked);
			return;
		}
		// some of the items may not exist on the coolbar if we save
		// the layout of editor action bars
// System.out.println("set layout");
		ArrayList currentCoolItemIds = getCoolItemIds();
		Vector itemOrder = new Vector();
		Vector itemSizes = new Vector();
		int i = 0;
		for (Iterator e = layout.items.iterator(); e.hasNext();) {
			CoolBarContributionItem item = (CoolBarContributionItem)e.next();
			int index = currentCoolItemIds.indexOf(item.getId());
			if (index == -1) {
				// not found
			} else {
				// figure out its index location and its associated size
// System.out.println("item " + item.getId() + " index " + index);
				itemOrder.addElement(new Integer(index));
				itemSizes.addElement(layout.itemSizes[i]);
			} 
			i++;
		}
		Object[] objectArray = itemOrder.toArray();
		int[] itemOrderArray = new int[objectArray.length];
		for (int j=0; j<objectArray.length; j++) {
			itemOrderArray[j]=((Integer)objectArray[j]).intValue();
		}
		objectArray = itemSizes.toArray();
		Point[] itemSizesArray = new Point[objectArray.length];
		for (int j=0; j<objectArray.length; j++) {
			itemSizesArray[j]=(Point)objectArray[j];
		}
		// not working with editor action items, need to revisit this...
//		coolBar.setItemLayout(itemOrderArray, layout.itemWrapIndices, itemSizesArray);
		coolBar.setLocked(layout.locked);
		coolBarLayout = layout;
	}
	/**
	 */
	public void update(boolean force) {
		if (isDirty() || force) {
			if (coolBarExist()) {
				boolean changed = false;
				coolBar.setRedraw(false);
				// remove CoolBarItemContributions that are empty
				IContributionItem[] items = getItems();
				ArrayList toRemove = new ArrayList(items.length);
				for (int i = 0; i<items.length; i++) {
					CoolBarContributionItem cbItem = (CoolBarContributionItem)items[i];
					if (cbItem.getItems().length == 0) {
						toRemove.add(cbItem);
					} 
				}
				changed = changed || (toRemove.size() > 0);
				for (Iterator e = toRemove.iterator(); e.hasNext();) {
					CoolBarContributionItem cbItem = (CoolBarContributionItem)e.next();
					remove(cbItem);
				}
				// remove obsolete CoolItems that do not have an associated CoolBarContributionItem
				ArrayList contributionIds = getContributionIds();
				CoolItem[] coolItems = coolBar.getItems();
				for (int i = 0; i < coolItems.length; i++) {
					CoolItem coolItem = coolItems[i];
					ToolBar tBar = (ToolBar) coolItem.getControl();
					CoolBarContributionItem cbItem = (CoolBarContributionItem)coolItem.getData();
					if ((cbItem == null) || (!contributionIds.contains(cbItem.getId()))) {
						changed = true;
						coolItem.setControl(null);
						tBar.dispose();
						coolItem.dispose();
					} 
				}
				
				// create a CoolItem for each group of items that does not exist 
				contributionIds = getCoolItemIds();
				items = getItems();
				for (int i = 0; i<items.length; i++) {
					CoolBarContributionItem cbItem = (CoolBarContributionItem)items[i];
					if (!contributionIds.contains(cbItem.getId())) {
						if (cbItem.isVisible()) {
							ToolBar toolBar = cbItem.getControl();
							if ((toolBar != null) && (!toolBar.isDisposed())) {
								changed = true;
								toolBar.setVisible(true);
								CoolItem coolItem = new CoolItem(coolBar, SWT.NULL);
								coolItem.setControl(toolBar);
								coolItem.setData(cbItem);
								cbItem.update(true);
								int minWidth = toolBar.getItems()[0].getWidth();
								Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//								System.out.println(cbItem.getId() + " toolbar size " + size);
								coolItem.setSize(coolItem.computeSize(size.x, size.y));
								coolItem.setMinimumWidth(minWidth);
//								System.out.println("cool item size " + coolItem.getSize());
							}
						}
					} 
				}
				// remove non-visible CoolBarContributionItems
				coolItems = coolBar.getItems();
				for (int i = 0; i < coolItems.length; i++) {
					CoolItem item = coolItems[i];
					ToolBar tBar = (ToolBar) item.getControl();
					CoolBarContributionItem cbItem = (CoolBarContributionItem)item.getData();
					if (!cbItem.isVisible()) {
						// do not dispose of the ToolBar, just the CoolItem
						changed = true;
						item.setControl(null);
						tBar.setVisible(false);
						item.dispose();
					} 
				}
				setDirty(false);
				coolBar.setRedraw(true);
				if (changed) {
					relayout();
				} 
			}

		}
	}
	/**
	 */
	private void validateItem(IContributionItem item) {
		if (!(item instanceof CoolBarContributionItem)) {
			throw new IllegalArgumentException("Invalid item type " + item.getClass());//$NON-NLS-1$
		}
	}
}