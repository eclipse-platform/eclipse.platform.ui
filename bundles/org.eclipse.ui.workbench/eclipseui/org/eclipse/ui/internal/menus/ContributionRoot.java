/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.internal.expressions.AlwaysEnabledExpression;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;

/**
 * Default implementation.
 *
 * @since 3.3
 */
final class ContributionRoot implements IContributionRoot {

	private List<IContributionItem> topLevelItems = new ArrayList<>();
	private Map<IContributionItem, Expression> itemsToExpressions = new HashMap<>();
	Set<?> restriction;
	private ContributionManager mgr;
	private AbstractContributionFactory factory;

	public ContributionRoot(IMenuService menuService, Set<?> restriction, ContributionManager mgr,
			AbstractContributionFactory factory) {
		this.restriction = restriction;
		this.mgr = mgr;
		this.factory = factory;
	}

	@Override
	public void addContributionItem(IContributionItem item, Expression visibleWhen) {
		if (item == null)
			throw new IllegalArgumentException();
		topLevelItems.add(item);
		if (visibleWhen == null)
			visibleWhen = AlwaysEnabledExpression.INSTANCE;

		// menuService.registerVisibleWhen(item, visibleWhen, restriction,
		// createIdentifierId(item));
		itemsToExpressions.put(item, visibleWhen);
	}

	/**
	 * Create the activity identifier for this contribution item.
	 *
	 * @param item the item
	 * @return the identifier
	 */
	String createIdentifierId(IContributionItem item) {
		String namespace = factory.getNamespace();

																							// identifier ID. If
																							// this factory doesn't have
																							// a namespace
																							// it will be null.
		return namespace != null ? namespace + '/' + item.getId() : null;
	}

	public List<IContributionItem> getItems() {
		return topLevelItems;
	}

	public Map<IContributionItem, Expression> getVisibleWhen() {
		return itemsToExpressions;
	}

	/**
	 * Unregister all visible when expressions from the menu service.
	 */
	public void release() {
		for (IContributionItem item : itemsToExpressions.keySet()) {
			// menuService.unregisterVisibleWhen(item, restriction);
			item.dispose();
		}
		itemsToExpressions.clear();
		topLevelItems.clear();
	}

	@Override
	public void registerVisibilityForChild(IContributionItem item, Expression visibleWhen) {
		if (item == null)
			throw new IllegalArgumentException();
		if (visibleWhen == null)
			visibleWhen = AlwaysEnabledExpression.INSTANCE;
		// menuService.registerVisibleWhen(item, visibleWhen, restriction,
		// createIdentifierId(item));
		itemsToExpressions.put(item, visibleWhen);
	}

	/**
	 * @return Returns the mgr.
	 */
	public ContributionManager getManager() {
		return mgr;
	}
}