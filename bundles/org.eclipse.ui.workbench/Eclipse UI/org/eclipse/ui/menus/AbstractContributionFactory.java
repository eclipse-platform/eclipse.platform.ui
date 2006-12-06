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

package org.eclipse.ui.menus;

import java.util.List;

/**
 * This class allows the programmatic contribution to a manager.
 * 
 * @since 3.3
 */
public abstract class AbstractContributionFactory {
	private String location = null;

	/**
	 * @param service
	 * @param location
	 *            the addition location in Menu API URI format.
	 */
	public AbstractContributionFactory(String location) {
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	/**
	 * This should be renamed to contributeItems. Cause the menu cache to
	 * generate IContributionItems for the additions that it contains.
	 * 
	 * @param menuService
	 *            the service for callbacks
	 * @param additions
	 *            A List supplied by the framework. It should be filled in with
	 *            new instances of IContributionItems.
	 */
	public abstract void createContributionItems(IMenuService menuService,
			List additions);

	/**
	 * This method tells the cache that the menu service is finished with the
	 * IContributionItems that it created. If it caches them internally, it is
	 * time to remove them.
	 * 
	 * @param menuService
	 *            the service for callbacks
	 * @param items
	 *            a list of IContributionItems created by this cache.
	 */
	public abstract void releaseContributionItems(IMenuService menuService,
			List items);
}
