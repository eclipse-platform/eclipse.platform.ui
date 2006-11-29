/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @since 3.3
 * 
 */
public class MenuAddition extends AdditionBase {

	// Cache sub-additions
	private List additions = new ArrayList();

	private ImageDescriptor imageDesc = null;
	private Image icon = null;

	private Listener toolItemListener = null;
	private IMenuService menuService = null;

	public MenuAddition(IConfigurationElement element, IMenuService service) {
		super(element);
		menuService = service;
	}

	public void readAdditions(IConfigurationElement addition, int insertionIndex) {
		IConfigurationElement[] items = addition.getChildren();
		for (int i = 0; i < items.length; i++) {
			String itemType = items[i].getName();

			if (IWorkbenchRegistryConstants.TAG_ITEM.equals(itemType)) {
				additions.add(insertionIndex++, new ItemAddition(items[i]));
			} else if (IWorkbenchRegistryConstants.TAG_WIDGET.equals(itemType)) {
				additions.add(insertionIndex++, new WidgetAddition(items[i]));
			} else if (IWorkbenchRegistryConstants.TAG_MENU.equals(itemType)) {
				MenuAddition newCache = new MenuAddition(items[i], menuService);
				newCache.readAdditions(items[i], 0);

				MenuLocationURI uri = new MenuLocationURI(
						"menu:" + newCache.getId()); //$NON-NLS-1$
				menuService.registerAdditionCache(uri, newCache);

				additions.add(insertionIndex++, newCache);
			} else if (IWorkbenchRegistryConstants.TAG_SEPARATOR
					.equals(itemType)) {
				additions
						.add(insertionIndex++, new SeparatorAddition(items[i]));
			}
		}
	}

	public String getMnemonic() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_MNEMONIC);
	}

	public String getLabel() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
	}

	public String getTooltip() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_TOOLTIP);
	}

	public Image getIcon() {
		if (imageDesc == null) {
			String extendingPluginId = element.getDeclaringExtension()
					.getContributor().getName();

			imageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(
					extendingPluginId, getIconPath());
		}

		// Stall loading the icon until first access
		if (icon == null && imageDesc != null) {
			icon = imageDesc.createImage(true, null);
		}
		return icon;
	}

	private String getIconPath() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
	}

	public String toString() {
		return getClass().getName()
				+ "(" + getLabel() + ":" + getTooltip() + ") " + getIconPath(); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}

	private IContributionItem getMenuContributionItem() {
		return new MenuManager(getLabel(), getId());
	}

	/**
	 * Returns the listener for SWT tool item widget events.
	 * 
	 * @return a listener for tool item events
	 */
	private Listener getToolItemListener() {
		if (toolItemListener == null) {
			toolItemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.Selection:
						Widget ew = event.widget;
						if (ew != null) {
							handleShowMenu(event, ((ToolItem) ew)
									.getSelection());
						}
						break;
					}
				}
			};
		}
		return toolItemListener;
	}

	/**
	 * @param event
	 * @param selection
	 */
	protected void handleShowMenu(Event event, boolean selection) {
		// Create a new Menu Manager and populate it from the additions
		MenuManager mgr = new MenuManager();
		MenuLocationURI uri = new MenuLocationURI("menu:" + getId()); //$NON-NLS-1$
		menuService.populateMenu(mgr, uri);

		// Create a menu and fill it
		Widget item = event.widget;
		ToolItem ti = (ToolItem) item;
		Menu m = new Menu(ti.getParent().getShell());
		IContributionItem[] items = mgr.getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].fill(m, i);
		}
		// mgr.fill(m, 0);

		// Show the menu if the correct location
		if (m != null) {
			// position the menu below the drop down item
			Rectangle b = ti.getBounds();
			Point p = ti.getParent().toDisplay(new Point(b.x, b.y + b.height));
			m.setLocation(p.x, p.y); // waiting for SWT 0.42
			m.setVisible(true);
		}
	}

	/**
	 * @param event
	 */
	protected void handleWidgetDispose(Event event) {
		// TODO Auto-generated method stub
		int i = 0;
		i = i + 12;
	}

	private IContributionItem getToolBarContributionItem() {
		return new ContributionItem(getId()) {

			public void fill(ToolBar parent, int index) {
				ToolItem newItem = new ToolItem(parent, SWT.DROP_DOWN, index);

				if (getIconPath() != null)
					newItem.setImage(getIcon());
				else if (getLabel() != null)
					newItem.setText(getLabel());

				if (getTooltip() != null)
					newItem.setToolTipText(getTooltip());
				else
					newItem.setToolTipText(getLabel());

				newItem.addListener(SWT.Selection, getToolItemListener());
				newItem.addListener(SWT.Dispose, getToolItemListener());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.ContributionItem#update()
			 */
			public void update() {
				update(null);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.ContributionItem#update(java.lang.String)
			 */
			public void update(String id) {
				if (getParent() != null) {
					getParent().update(true);
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.AdditionBase#getContributionItem()
	 */
	public IContributionItem getContributionItem(boolean forMenu) {
		if (forMenu)
			return getMenuContributionItem();

		return getToolBarContributionItem();
	}

	public void populateMenuManager(ContributionManager mgr) {
		for (Iterator additionIter = additions.iterator(); additionIter
				.hasNext();) {
			AdditionBase addition = (AdditionBase) additionIter.next();

			// Is this a dynamic item?
			if (addition instanceof ItemAddition
					&& ((ItemAddition) addition).isDynamic()) {
				ItemAddition dynamicItem = (ItemAddition) addition;

				// Get the list of contribution items and
				// add then into the menu manager.
				List items = new ArrayList();
				dynamicItem.getFiller().fillItems(items);
				for (Iterator itemIter = items.iterator(); itemIter.hasNext();) {
					IContributionItem item = (IContributionItem) itemIter
							.next();
					mgr.add(item);
				}
			} else {
				// normal item, just get the contribution
				// Should we just change getContributionItem to return
				// a list and move the logic into ItemAddition??
				boolean forMenu = mgr instanceof MenuManager;
				IContributionItem ci = addition.getContributionItem(forMenu);

				// Populate the sub-items of menus
				if (addition instanceof MenuAddition) {
					if (ci instanceof MenuManager) {
						((MenuAddition) addition)
								.populateMenuManager((MenuManager) ci);
					}
				}

				// Add the item to the manager
				mgr.add(ci);
			}
		}
	}

	/**
	 * @param additionId
	 *            The id of the addition to find
	 * @return the index of the given addition
	 */
	public int indexOf(String additionId) {
		int index = 0;
		for (Iterator iterator = additions.iterator(); iterator.hasNext();) {
			AdditionBase addition = (AdditionBase) iterator.next();
			if (additionId.equals(addition.getId()))
				return index;
			index++;
		}
		return -1;
	}
}
