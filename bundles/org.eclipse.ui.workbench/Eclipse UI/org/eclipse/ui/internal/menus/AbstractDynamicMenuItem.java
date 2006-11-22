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
 * Base class for dynamic menu item contributions.
 * <p>
 * The items in the returned List must be
 * <code>IContributionItems</code>s.
 * </p>
 * @since 3.3
 *
 */
public abstract class AbstractDynamicMenuItem {
	/**
	 * Fill in the given list with the set of
	 * <code>IContributionItem</code>s that will
	 * replace the dynamic item in the menu.
	 * 
	 * @param items A list of <code>IContributionItem</code>s. 
	 */
	public abstract void fillItems(List items);
}
