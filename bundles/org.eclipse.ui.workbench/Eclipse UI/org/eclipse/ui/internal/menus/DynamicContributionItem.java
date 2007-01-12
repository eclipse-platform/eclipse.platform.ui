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
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.AbstractDynamicContribution;

/**
 * Wrapper for a CompoundContribution item that uses the new
 * menus <code>AbstractDynamicContribution</code> to create
 * the replacement contribution items.
 * 
 * @since 3.3
 *
 */
public class DynamicContributionItem extends CompoundContributionItem {
	private AbstractDynamicContribution loadedDynamicContribution;
	
	/**
	 * Constructor
	 * 
	 * @param id The items' id
	 * @param factory The factory used to construct the replacement ICI's
	 * (NOTE: This must be non-null...)
	 */
	public DynamicContributionItem(String id, AbstractDynamicContribution factory) {
		super(id);
		loadedDynamicContribution = factory;
	}
	
	protected IContributionItem[] getContributionItems() {
		List dynamicItems = new ArrayList();
		loadedDynamicContribution.createContributionItems(dynamicItems);
		return (IContributionItem[]) dynamicItems
				.toArray(new IContributionItem[dynamicItems.size()]);
	}
}
