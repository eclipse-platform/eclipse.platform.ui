package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

/**
 * WORK IN PROGRESS FOR COOLBAR SUPPORT
 */
public class CoolBarManager extends ContributionManager implements IToolBarManager {
	/** 
	 * The cool bar style; <code>SWT.NONE</code> by default.
	 */
	private int style = SWT.NONE;

	/** 
	 * The cool bar control; <code>null</code> before creation
	 * and after disposal.
	 */
	private CoolBar coolBar = null;

	/** 
	 * MenuManager for chevron menu when CoolItems not fully displayed.
	 */
	private MenuManager chevronMenuManager;
	
	/** 
	 * MenuManager for coolbar popup menu
	 */
	private MenuManager coolBarMenuManager = new MenuManager();

	/**
	 */
	public CoolBarManager() {
	}
	/**
	 */
	public CoolBarManager(int style) {
		this.style = style;
	}
	/**
	 * Adds an action as a contribution item to this manager.
	 * Equivalent to <code>add(new ActionContributionItem(action))</code>.
	 * 
	 * Not valid for CoolBarManager.  Only CoolBarContributionItems may be added
	 * to this manager.
	 * 
	 * @param action the action
	 */
	public void add(IAction action) {
		Assert.isTrue(false);
	}
	/**
	 * Adds a CoolBarContributionItem to this manager.
	 * 
	 * @exception AssertionFailedException if the type of item is
	 * not valid
	 */
	public void add(IContributionItem item) {
		Assert.isTrue(item instanceof CoolBarContributionItem);
		super.add(item);
	}
	/**
	 * Adds a contribution item to the coolbar's menu.
	 */
	public void addToMenu(ActionContributionItem item) {
		coolBarMenuManager.add(item.getAction());
	}
	/**
	 * Adds a contribution item to the start or end of the group 
	 * with the given id.
	 * 
	 * Not valid for CoolBarManager.  Only CoolBarContributionItems are items
	 * of this manager.
	 */
	private void addToGroup(String itemId, IContributionItem item, boolean append) {
		Assert.isTrue(false);
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
			// Create the CoolBar and its popup menu.
			coolBar = new CoolBar(parent, style);
			coolBar.setLocked(false);
			coolBar.addListener(SWT.Resize, new Listener() {
				public void handleEvent(Event event) {
					coolBar.getParent().layout();
				}
			});
			coolBar.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					popupCoolBarMenu(e);
				}
			});
		}
		return coolBar;
	}
	/**
	 * Create the coolbar item for the given contribution item.
	 */
	private CoolItem createCoolItem(CoolBarContributionItem cbItem) {
		CoolItem coolItem;
		int index;
		if (cbItem.isOrderBefore()) {
			index = getInsertBeforeIndex(cbItem);
		} else {
			index = getInsertAfterIndex(cbItem);
		}
		coolItem = new CoolItem(coolBar, SWT.DROP_DOWN, index);
		return coolItem;
	}
	/**
	 */
	public void dispose() {
		if (coolBarExist()) {
			coolBar.dispose();
			coolBar = null;
		}
		if (chevronMenuManager != null) {
			chevronMenuManager.dispose();
			chevronMenuManager = null;
		}
		if (coolBarMenuManager != null) {
			coolBarMenuManager.dispose();
			coolBarMenuManager = null;
		}
	}
	/**
	 */
	CoolItem findCoolItem(CoolBarContributionItem item) {
		CoolItem[] items = coolBar.getItems();
		for (int i = 0; i < items.length; i++) {
			CoolItem coolItem = items[i];
			if (coolItem.getData().equals(item)) return coolItem;
		}
		return null;
	}
	/**
	 */
	CoolBarContributionItem findSubId(String id) {
		IContributionItem[] items = getItems();
		for (int i = 0; i < items.length; i++) {
			CoolBarContributionItem item = (CoolBarContributionItem)items[i];
			IContributionItem subItem = item.getToolBarManager().find(id);
			if (subItem != null) return item;
		}
		return null;
	}
	/**
	 */
	private ArrayList getContributionIds() {
		IContributionItem[] items = getItems();
		ArrayList ids = new ArrayList(items.length);
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			ids.add(item.getId());
		}
		return ids;
	}
	/**
	 */
	private ArrayList getCoolItemIds() {
		CoolItem[] coolItems = coolBar.getItems();
		ArrayList ids = new ArrayList(coolItems.length);
		for (int i = 0; i < coolItems.length; i++) {
			CoolBarContributionItem group = (CoolBarContributionItem) coolItems[i].getData();
			ids.add(group.getId());
		}
		return ids;
	}
	/**
	 * Return the SWT control for this manager.
	 */
	public CoolBar getControl() {
		return coolBar;
	}
	private int getInsertAfterIndex(CoolBarContributionItem coolBarItem) {
		IContributionItem[] items = getItems();
		int index = -1;
		CoolBarContributionItem afterItem = null;
		// find out which item should be after this item
		for (int i=0; i<items.length; i++) {
			if (items[i].equals(coolBarItem)) {
				if (i > 0) {
					afterItem = (CoolBarContributionItem)items[i-1];
				} else {
					afterItem = null;
				}
				break;
			}
		}
		// get the coolbar location of the after item
		if (afterItem != null) {
			CoolItem afterCoolItem = findCoolItem(afterItem);
			if (afterCoolItem != null) {
				index = coolBar.indexOf(afterCoolItem);
				index++;
			}
		}
		if (index == -1) index = 0;
		return index;
	}
	private int getInsertBeforeIndex(CoolBarContributionItem coolBarItem) {
		IContributionItem[] items = getItems();
		int index = -1;
		CoolBarContributionItem beforeItem = null;
		// find out which item should be before this item
		for (int i=0; i<items.length; i++) {
			if (items[i].equals(coolBarItem)) {
				if (i < items.length - 1) {
					beforeItem = (CoolBarContributionItem)items[i+1];
				} else {
					beforeItem = null;
				}
				break;
			}
		}
		// get the coolbar location of the before item
		if (beforeItem != null) {
			CoolItem beforeCoolItem = findCoolItem(beforeItem);
			if (beforeCoolItem != null) {
				index = coolBar.indexOf(beforeCoolItem);
			}
		}
		if (index == -1) index = coolBar.getItems().length;
		return index;
	}
	/**
	 */
	public CoolBarLayout getLayout() {
		if (!coolBarExist())
			return null;

		CoolBarLayout layout = new CoolBarLayout();
		CoolItem[] coolItems = coolBar.getItems();
		ArrayList newItems = new ArrayList(coolItems.length);
		for (int i = 0; i < coolItems.length; i++) {
			CoolBarContributionItem item = (CoolBarContributionItem) coolItems[i].getData();
			newItems.add(item.getId());
		}
		layout.items = newItems;
		layout.itemSizes = coolBar.getItemSizes();
		layout.itemWrapIndices = coolBar.getWrapIndices();

		//System.out.println("get layout " + layout.toString());

		return layout;
	}
	/**
	 */
	public int getStyle() {
		return style;
	}
	/**
	 * Create and display the chevron menu.
	 */
	private void handleChevron(SelectionEvent event) {
		CoolItem item = (CoolItem) event.widget;
		Control control = item.getControl();
		if ((control instanceof ToolBar) == false) {
			return;
		}
		Rectangle itemBounds = item.getBounds();
		Point chevronPosition = coolBar.toDisplay(new Point(event.x, event.y));
		ToolBar toolBar = (ToolBar) control;
		ToolItem[] tools = toolBar.getItems();
		int toolCount = tools.length;
		int visibleItemCount = 0;
		while (visibleItemCount < toolCount) {
			Rectangle toolBounds = tools[visibleItemCount].getBounds();
			Point point = toolBar.toDisplay(new Point(toolBounds.x, toolBounds.y));
			toolBounds.x = point.x;
			toolBounds.y = point.y;
			// stop if the tool is at least partially hidden by the drop down chevron
			if (chevronPosition.x >= toolBounds.x && chevronPosition.x - toolBounds.x <= toolBounds.width) {
				break;
			}
			visibleItemCount++;
		}

		// Create a pop-up menu with items for each of the hidden buttons.
		if (chevronMenuManager != null) {
			chevronMenuManager.dispose();
		}
		chevronMenuManager = new MenuManager();
		for (int i = visibleItemCount; i < toolCount; i++) {
			IContributionItem data = (IContributionItem) tools[i].getData();
			if (data instanceof ActionContributionItem) {
				ActionContributionItem contribution = new ActionContributionItem(((ActionContributionItem) data).getAction());
				chevronMenuManager.add(contribution);
			} else if (data instanceof SubContributionItem) {
				IContributionItem innerData = ((SubContributionItem)data).getInnerItem();
				if (innerData instanceof ActionContributionItem) {
					ActionContributionItem contribution = new ActionContributionItem(((ActionContributionItem) innerData).getAction());
					chevronMenuManager.add(contribution);
				}
			} else if (data.isSeparator()) {
				chevronMenuManager.add(new Separator());
			}
		}
		Menu popup = chevronMenuManager.createContextMenu(coolBar);
		popup.setLocation(chevronPosition.x, chevronPosition.y);
		popup.setVisible(true);
	}
	/**
	 * Inserts a contribution item for the given action after the item 
	 * with the given id.
	 * Equivalent to
	 * <code>insertAfter(id,new ActionContributionItem(action))</code>.
	 *
	 * Not valid for CoolBarManager.  Only CoolBarContributionItems may be added
	 * to this manager.
	 *
	 * @param id the contribution item id
	 * @param action the action to insert
	 */
	public void insertAfter(String id, IAction action) {
		Assert.isTrue(false);
	}
	/**
	 * Inserts a contribution item after the item with the given id.
	 *
	 * @param id the CoolBarContributionItem 
	 * @param item the CoolBarContributionItem to insert
	 * @exception IllegalArgumentException if there is no item with
	 *   the given id
	 * @exception IllegalArgumentException if the type of item is
	 * 	not valid
	 */
	public void insertAfter(String id, IContributionItem item) {
		Assert.isTrue(item instanceof CoolBarContributionItem);
		super.insertAfter(id, item);
		((CoolBarContributionItem)item).setOrderAfter(true);
	}
	/**
	 * Inserts a contribution item for the given action before the item 
	 * with the given id.
	 * Equivalent to
	 * <code>insertBefore(id,new ActionContributionItem(action))</code>.
	 *
	 * Not valid for CoolBarManager.  Only CoolBarContributionItems may be added
	 * to this manager.
	 *
	 * @param id the contribution item id
	 * @param action the action to insert
	 */
	public void insertBefore(String id, IAction action) {
		Assert.isTrue(false);
	}
	/**
	 * Inserts a contribution item before the item with the given id.
	 *
	 * @param id the CoolBarContributionItem 
	 * @param item the CoolBarContributionItem to insert
	 * @exception IllegalArgumentException if there is no item with
	 *   the given id
	 * @exception IllegalArgumentException if the type of item is
	 * 	not valid
	 */
	public void insertBefore(String id, IContributionItem item) {
		Assert.isTrue(item instanceof CoolBarContributionItem);
		super.insertBefore(id, item);
		((CoolBarContributionItem)item).setOrderBefore(true);
	}
	/**
	 */
	public void lockLayout(boolean value) {
		coolBar.setLocked(value);
	}
	/**
	 */
	/* package */ void popupCoolBarMenu(MouseEvent e) {
		if (e.button != 3)
			return;
		Point pt = new Point(e.x, e.y);
		pt = ((Control) e.widget).toDisplay(pt);
		Menu coolBarMenu = coolBarMenuManager.createContextMenu(coolBar);
		coolBarMenu.setLocation(pt.x, pt.y);
		coolBarMenu.setVisible(true);
	}
	/**
	 */
	public void resetLayout() {
		CoolItem[] coolItems = coolBar.getItems();
		for (int i = 0; i < coolItems.length; i++) {
			CoolItem coolItem = coolItems[i];
			coolItem.setData(null);
			coolItem.setControl(null);
			coolItem.dispose();
		}
		coolBar.setLocked(false);
		coolBar.setWrapIndices(new int[] {
		});
		update(true);
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
		Assert.isTrue(item instanceof CoolBarContributionItem);
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
			CoolItem[] coolItems = coolBar.getItems();
			for (int i = 0; i < coolItems.length; i++) {
				CoolItem coolItem = coolItems[i];
				ToolBar toolBar = (ToolBar) coolItem.getControl();
				int minWidth = toolBar.getItems()[0].getWidth();
				Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point coolSize = coolItem.computeSize(size.x, size.y);
				coolItem.setSize(coolSize);
				coolItem.setPreferredSize(coolSize);
				coolItem.setMinimumSize(minWidth, coolItem.getMinimumSize().y);
			}
			coolBar.setWrapIndices(new int[] {
			});
			return;
		}
		// Some of the layout items may not exist on the coolbar, for example, if we save
		// the layout of editor action bars.  Similarly, items may exist on the coolbar that
		// are not part of the saved layout.
		CoolItem[] coolItems = coolBar.getItems();
		ArrayList currentCoolItemIds = getCoolItemIds();
		ArrayList newItems = new ArrayList();
		int maxItemCount = Math.max(coolItems.length, layout.items.size());
		// the maximum possible number of items that need to be visited
		int[] itemOrder = new int[maxItemCount];
		Point[] itemSizes = new Point[maxItemCount];
		int[] currentItemOrder = coolBar.getItemOrder();
		// necessary if cool item order has already changed from its original creation order
		for (int i = 0; i < itemOrder.length; i++) {
			itemOrder[i] = -1;
		}
		for (int i = 0; i < coolItems.length; i++) {
			CoolBarContributionItem item = (CoolBarContributionItem) coolItems[i].getData();
			int index = layout.items.indexOf(item.getId());
			if (index != -1) {
				// index = new visual position, i = current visual position 
				// itemOrder[index] must be original visual/creation position
				itemOrder[index] = currentItemOrder[i];
				itemSizes[index] = layout.itemSizes[index];
			} else {
				newItems.add(coolItems[i]);
			}
		}
		Iterator iterator = newItems.iterator();
		while (iterator.hasNext()) {
			CoolItem coolItem = (CoolItem) iterator.next();
			CoolBarContributionItem item = (CoolBarContributionItem) coolItem.getData();
			int index = currentCoolItemIds.indexOf(item.getId());
			for (int i = 0; i < itemOrder.length; i++) {
				if (itemOrder[i] == -1) {
					itemOrder[i] = index;
					itemSizes[i] = coolItem.getSize();
					break;
				} else if (i == index) {
					// index of new item is already used. move items up one,
					boolean increment = false;
					for (int j = 0; j <itemOrder.length; j++) {
						if (itemOrder[j] == index) {
							increment = true;
							break;
						}
					}
					System.arraycopy(itemOrder, i, itemOrder, i + 1, itemOrder.length - i - 1);
					if (increment) {
						for (int j=i+1; j<itemOrder.length; j++) {
							if (itemOrder[j] != -1) {
								itemOrder[j]++;
							}
						}
					}
					System.arraycopy(itemSizes, i, itemSizes, i + 1, itemSizes.length - i - 1);
					itemOrder[i] = index;
					itemSizes[i] = coolItem.getSize();
					break;
				}
			}
		}
		// remove gaps/unused slots that exist if the old CoolBar had more items 
		// than the new CoolBar
		int unusedCount = 0;
		for (int i = 0; i < itemOrder.length - 1; i++) {
			if (itemOrder[i] == -1) {
				unusedCount++;
				// remove unused slot
				System.arraycopy(itemOrder, i + 1, itemOrder, i, itemOrder.length - i - 1);
				System.arraycopy(itemSizes, i + 1, itemSizes, i, itemSizes.length - i - 1);
			}
		}
		if (itemOrder[itemOrder.length - 1] == -1) {
			unusedCount++;
		}
		if (unusedCount > 0) {
			int[] newItemOrder = new int[itemOrder.length - unusedCount];
			Point[] newItemSizes = new Point[itemSizes.length - unusedCount];

			System.arraycopy(itemOrder, 0, newItemOrder, 0, newItemOrder.length);
			System.arraycopy(itemSizes, 0, newItemSizes, 0, newItemSizes.length);
			itemOrder = newItemOrder;
			itemSizes = newItemSizes;
		}
		/*
		 * TODO: Make sure that a new item that used to be the last item
		 * and is now no longer the last item doesn't take more space than
		 * its preferred size.
		 * Otherwise it would move the following items to the right
		 */

//				System.out.print("item create positions ");
//				CoolItem[] itms = coolBar.getItems();
//				for (int i=0; i<itms.length; i++) {
//					CoolItem cItem = itms[i];
//					System.out.print(coolBar.indexOf(cItem) + " ");
//				}
//				System.out.println();
//				System.out.print("item order ");
//				for (int i=0; i<itemOrder.length; i++) {
//					System.out.print(itemOrder[i] + " ");
//				}
//				System.out.println();
//				System.out.print("item sizes ");
//				for (int i=0; i<itemSizes.length; i++) {
//					System.out.print(itemSizes[i] + " ");
//				}
//				System.out.println();

		coolBar.setRedraw(false);
		coolBar.setItemLayout(itemOrder, new int[0], itemSizes);

		// restore the wrap indices after the new item order is restored, wrap on the same items that 
		// were specified in the layout
		String[] wrapItems = new String[layout.itemWrapIndices.length];
		for (int i = 0; i < layout.itemWrapIndices.length; i++) {
			wrapItems[i] = (String) layout.items.get(layout.itemWrapIndices[i]);
		}
		int[] wrapIndices = new int[wrapItems.length];
		currentCoolItemIds = getCoolItemIds();
		int j = 0;
		int numItems = itemSizes.length;
		for (int i = 0; i < wrapItems.length; i++) {
			int index = currentCoolItemIds.indexOf(wrapItems[i]);
			if (index != -1) {
				wrapIndices[j] = index;
				j++;
			} 
		}
		int[] itemWraps = new int[j];
		System.arraycopy(wrapIndices, 0, itemWraps, 0, j);

		//		System.out.print("item wraps ");
		//		for (int i=0; i<itemWraps.length; i++) {
		//			System.out.print(itemWraps[i] + " ");
		//		}
		//		System.out.println();

		coolBar.setWrapIndices(itemWraps);
		coolBar.setRedraw(true);

		//		System.out.println("layout set");
		//		System.out.println(getLayout().toString());
	}
	/**
	 */
	public void update(boolean force) {
		if (isDirty() || force) {
			if (coolBarExist()) {
				boolean changed = false;
				coolBar.setRedraw(false);
				
				// workaround for 14330
				boolean relock = false;
				if (coolBar.getLocked() && (coolBar.getItems().length > 0)) {
					coolBar.setLocked(false);
					relock = true;
				}
				
				// remove CoolBarItemContributions that are empty
				IContributionItem[] items = getItems();
				ArrayList toRemove = new ArrayList(items.length);
				for (int i = 0; i < items.length; i++) {
					CoolBarContributionItem cbItem = (CoolBarContributionItem) items[i];
					if (cbItem.getItems().length == 0) {
						toRemove.add(cbItem);
					}
				}
				changed = changed || (toRemove.size() > 0);
				for (Iterator e = toRemove.iterator(); e.hasNext();) {
					CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
					remove(cbItem);
				}
				// remove obsolete CoolItems that do not have an associated CoolBarContributionItem
				ArrayList contributionIds = getContributionIds();
				CoolItem[] coolItems = coolBar.getItems();
				for (int i = 0; i < coolItems.length; i++) {
					CoolItem coolItem = coolItems[i];
					ToolBar tBar = (ToolBar) coolItem.getControl();
					CoolBarContributionItem cbItem = (CoolBarContributionItem) coolItem.getData();
					if ((cbItem == null) || (!contributionIds.contains(cbItem.getId()))) {
						changed = true;
						coolItem.setData(null);
						coolItem.setControl(null);
						tBar.dispose();
						coolItem.dispose();
					}
				}

				// create a CoolItem for each group of items that does not have a CoolItem 
				ArrayList coolItemIds = getCoolItemIds();
				items = getItems();
				for (int i = 0; i < items.length; i++) {
					CoolBarContributionItem cbItem = (CoolBarContributionItem) items[i];
					if (!coolItemIds.contains(cbItem.getId())) {
						if (cbItem.isVisible()) {
							ToolBar toolBar = cbItem.getControl();
							if ((toolBar != null) && (!toolBar.isDisposed())) {
								changed = true;
								toolBar.setVisible(true);
								CoolItem coolItem = createCoolItem(cbItem);
								coolItem.setControl(toolBar);
								coolItem.setData(cbItem);
								cbItem.update(true);
								int minWidth = toolBar.getItems()[0].getWidth();
								Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
								Point coolSize = coolItem.computeSize(size.x, size.y);
								coolItem.setSize(coolSize);
								coolItem.setPreferredSize(coolSize);
								coolItem.setMinimumSize(minWidth, coolItem.getMinimumSize().y);
								coolItem.addSelectionListener(new SelectionAdapter() {
									public void widgetSelected(SelectionEvent event) {
										if (event.detail == SWT.ARROW) {
											handleChevron(event);
										}
									}
								});
							}
						}
					} 				}
				// remove non-visible CoolBarContributionItems
				coolItems = coolBar.getItems();
				for (int i = 0; i < coolItems.length; i++) {
					CoolItem item = coolItems[i];
					ToolBar tBar = (ToolBar) item.getControl();
					CoolBarContributionItem cbItem = (CoolBarContributionItem) item.getData();
					if (!cbItem.isVisible()) {
						// do not dispose of the ToolBar, just the CoolItem
						changed = true;
						item.setControl(null);
						tBar.setVisible(false);
						item.dispose();
					}
				}

				setDirty(false);

				// workaround for 14330
				if(relock) {
					coolBar.setLocked(true);
				}

				coolBar.setRedraw(true);
				if (changed) {
					relayout();
				}
			}

		}
	}
}