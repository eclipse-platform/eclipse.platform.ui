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

import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.IContributionItem;

/**
 * @since 3.3
 * 
 */
public abstract class MenuCacheEntry {
	private MenuLocationURI uri = null;

	protected IMenuService menuService;

	/**
	 * @param service
	 */
	public MenuCacheEntry(IMenuService service) {
		menuService = service;
	}

	/**
	 * The location URI for this Cache.
	 * 
	 * @return Returns the uri.
	 */
	public MenuLocationURI getUri() {
		return uri;
	}

	void setUri(MenuLocationURI u) {
		uri = u;
	}

	/**
	 * The menu cache can return the visibleWhen clause for any
	 * IContributionItems that it generates. It can return the same Expression
	 * for many items.
	 * 
	 * @param item
	 *            to find. Must not be <code>null</code>.
	 * @return the visibleWhen clause, or <code>null</code> if there is none
	 *         for this item.
	 */
	public abstract Expression getVisibleWhenForItem(IContributionItem item);

	/**
	 * This should be renamed to contributeItems. Cause the menu cache to
	 * generate IContributionItems for the additions that it contains.
	 * 
	 * @param additions
	 *            A List supplied by the framework. It should be filled in with
	 *            IContributionItems.
	 */
	public abstract void getContributionItems(List additions);

	/**
	 * A framework lifecycle method. When a menu cache is added to the
	 * IMenuService, it can generate additional menu caches for any submenus
	 * that it contains.
	 */
	public abstract void generateSubCaches();
}
