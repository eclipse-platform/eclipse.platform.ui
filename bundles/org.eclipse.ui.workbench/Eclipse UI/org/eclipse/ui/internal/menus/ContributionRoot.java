/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.menus.IContributionRoot;

/**
 * Default implementation.
 * 
 * @since 3.3
 */
final class ContributionRoot implements
		IContributionRoot {

	private List topLevelItems = new ArrayList();
	private List itemsWithExpressions = new ArrayList();
	private InternalMenuService menuService;

	public ContributionRoot(InternalMenuService menuService) {
		this.menuService = menuService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.menus.IContributionRoot#addContributionItem(org.eclipse.jface.action.IContributionItem, org.eclipse.core.expressions.Expression, org.eclipse.core.expressions.Expression)
	 */
	public void addContributionItem(IContributionItem item,
			Expression visibleWhen, Expression killswitch) {
		if (item == null)
			throw new IllegalArgumentException();
		topLevelItems.add(item);
		if (visibleWhen == null) 
			return;
		
		menuService.registerVisibleWhen(item, visibleWhen);
		itemsWithExpressions.add(item);
	}

	public Collection getItems() {
		return topLevelItems;
	}

	/**
	 * Unregister all visible when expressions from the menu service.
	 */
	public void release() {
		for (Iterator itemIter = itemsWithExpressions.iterator(); itemIter.hasNext();) {
			IContributionItem item = (IContributionItem) itemIter.next();
			menuService.unregisterVisibleWhen(item);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.menus.IContributionRoot#registerVisibilityForChild(org.eclipse.jface.action.IContributionItem, org.eclipse.core.expressions.Expression, org.eclipse.core.expressions.Expression)
	 */
	public void registerVisibilityForChild(IContributionItem item,
			Expression visibleWhen, Expression killswitch) {
		if (item == null)
			throw new IllegalArgumentException();
		if (visibleWhen == null)
			return;
		menuService.registerVisibleWhen(item, visibleWhen);
		itemsWithExpressions.add(item);
	}
}
