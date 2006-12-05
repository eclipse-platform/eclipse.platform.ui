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

	public void setUri(MenuLocationURI u) {
		uri = u;
	}

	/**
	 * This should be renamed to contributeItems. Cause the menu cache to
	 * generate IContributionItems for the additions that it contains.
	 * 
	 * @param additions
	 *            A List supplied by the framework. It should be filled in with
	 *            new instances of IContributionItems.
	 */
	public abstract void createContributionItems(List additions);

	/**
	 * This method tells the cache that the menu service is finished with the
	 * IContributionItems that it created. If it caches them internally, it is
	 * time to remove them.
	 * 
	 * @param items
	 *            a list of IContributionItems created by this cache.
	 */
	public abstract void releaseContributionItems(List items);
}
