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
import org.eclipse.ui.internal.expressions.AlwaysEnabledExpression;
import org.eclipse.ui.menus.IContributionRoot;

/**
 * Default implementation.
 * 
 * @since 3.3
 */
final class ContributionRoot implements
		IContributionRoot {

	private List topLevelItems = new ArrayList();
	private List itemsToExpressions = new ArrayList();
	private InternalMenuService menuService;
	private Expression restriction;
	private String namespace;

	public ContributionRoot(InternalMenuService menuService, Expression restriction, String namespace) {
		this.menuService = menuService;
		this.restriction = restriction;
		this.namespace = namespace;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.menus.IContributionRoot#addContributionItem(org.eclipse.jface.action.IContributionItem, org.eclipse.core.expressions.Expression, org.eclipse.core.expressions.Expression)
	 */
	public void addContributionItem(IContributionItem item,
			Expression visibleWhen) {
		if (item == null)
			throw new IllegalArgumentException();
		topLevelItems.add(item);
		if (visibleWhen == null) 
			visibleWhen = AlwaysEnabledExpression.INSTANCE;
		
		menuService.registerVisibleWhen(item, visibleWhen, restriction,
				createIdentifierId(item));
		itemsToExpressions.add(item);
	}

	/**
     * Create the activity identifier for this contribution item.
     *
	 * @param item the item
	 * @return the identifier
	 */
	private String createIdentifierId(IContributionItem item) {
		String identifierID = namespace != null ? namespace + '/'
				+ item.getId() : null; // create the activity identifier ID. If
										// this factory doesn't have a namespace
										// it will be null.
		return identifierID;
	}

	public Collection getItems() {
		return topLevelItems;
	}

	/**
	 * Unregister all visible when expressions from the menu service.
	 */
	public void release() {
		for (Iterator itemIter = itemsToExpressions.iterator(); itemIter.hasNext();) {
			IContributionItem item = (IContributionItem) itemIter.next();
			menuService.unregisterVisibleWhen(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.menus.IContributionRoot#registerVisibilityForChild(org.eclipse.jface.action.IContributionItem,
	 *      org.eclipse.core.expressions.Expression,
	 *      org.eclipse.core.expressions.Expression)
	 */
	public void registerVisibilityForChild(IContributionItem item,
			Expression visibleWhen) {
		if (item == null)
			throw new IllegalArgumentException();
		if (visibleWhen == null) 
			visibleWhen = AlwaysEnabledExpression.INSTANCE;
		menuService.registerVisibleWhen(item, visibleWhen, restriction,
				createIdentifierId(item));
		itemsToExpressions.add(item);
	}
}
