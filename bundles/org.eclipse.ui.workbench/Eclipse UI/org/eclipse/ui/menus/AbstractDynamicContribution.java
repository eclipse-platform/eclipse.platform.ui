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
 * Base class for declarative dynamic menu item contributions. For programmatic
 * dynamic items please use CompoundContributionItem.
 * <p>
 * The items in the returned List must be <code>IContributionItems</code>s.
 * {@link #createContributionItems(List)} will be called when the menu is about
 * to show.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @see org.eclipse.ui.actions.CompoundContributionItem
 * @since 3.3
 */
public abstract class AbstractDynamicContribution {
	/**
	 * Fill in the given list with the set of <code>IContributionItem</code>s
	 * that will replace the dynamic item in the menu.
	 * 
	 * @param items
	 *            A list of <code>IContributionItem</code>s.
	 */
	public abstract void createContributionItems(List items);
}
