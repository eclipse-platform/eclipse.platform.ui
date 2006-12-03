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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.3
 * 
 */
public class MenuDataCacheEntry extends MenuCacheEntry {

	private List items = new ArrayList();

	private Map contributionsToData = new HashMap();

	/**
	 * @param service
	 *            the menu service. Must not be <code>null</code>.
	 * @param location
	 *            the addition location. Must not be <code>null</code>.
	 */
	public MenuDataCacheEntry(IMenuService service, String location) {
		super(service);
		setUri(new MenuLocationURI(location));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.MenuCacheEntry#generateSubCaches()
	 */
	public void generateSubCaches() {
		Iterator i = items.iterator();
		while (i.hasNext()) {
			Object obj = i.next();
			if (obj instanceof MenuData) {
				MenuData menu = (MenuData) obj;
				MenuDataCacheEntry cache = new MenuDataCacheEntry(menuService,
						"menu:" + menu.getId()); //$NON-NLS-1$
				cache.addAll(menu.getItems());
				menuService.addCacheForURI(cache);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.MenuCacheEntry#getContributionItems(java.util.List)
	 */
	public void getContributionItems(List additions) {
		additions.clear();

		Iterator i = items.iterator();
		while (i.hasNext()) {
			Object obj = i.next();
			if (obj instanceof ItemData) {
				ItemData item = (ItemData) obj;
				String id = item.getId();
				if (id == null) {
					id = item.getCommandId();
				}
				IContributionItem ici = new CommandDataContributionItem(id,
						item);
				contributionsToData.put(ici, item);
				additions.add(ici);
			} else if (obj instanceof MenuData) {
				MenuData menu = (MenuData) obj;
				MenuManager manager = new MenuManager(menu.getLabel(), menu
						.getId());
				contributionsToData.put(manager, menu);
				additions.add(manager);
			} else if (obj instanceof WidgetData) {
				WidgetData widget = (WidgetData) obj;
				IContributionItem ici = new WidgetDataContributionItem(widget
						.getId(), widget);
				contributionsToData.put(ici, widget);
				additions.add(ici);
			} else {
				WorkbenchPlugin
						.log("Unknown object contributed to menu cache: " //$NON-NLS-1$
								+ obj);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.MenuCacheEntry#getVisibleWhenForItem(org.eclipse.jface.action.IContributionItem)
	 */
	public Expression getVisibleWhenForItem(IContributionItem item) {
		ServiceData data = (ServiceData) contributionsToData.get(item);
		if (data != null) {
			return data.getVisibleWhen();
		}
		return null;
	}

	/**
	 * Add an item to this cache.
	 * 
	 * @param item
	 *            the service data item
	 */
	public void add(ServiceData item) {
		items.add(item);
	}

	/**
	 * Add all of the ServiceData items to this cache. Used when creating
	 * submenu caches.
	 * 
	 * @param l
	 *            the list
	 */
	public void addAll(List l) {
		items.addAll(l);
	}
}
