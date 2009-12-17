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

package org.eclipse.ui.menus;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;

/**
 * Instances of this interface represent a position in the contribution
 * hierarchy into which {@link AbstractContributionFactory} instances may insert
 * elements. Instances of this interface are provided by the platform and this
 * interface should <b>NOT</b> be implemented by clients.
 * 
 * 
 * @since 3.3
 */
public interface IContributionRoot {
	/**
	 * Adds a given contribution item with provided visibility expression and
	 * kill-switch filtering as a direct child of this container. This should be
	 * called for all top-level elements created in
	 * {@link AbstractContributionFactory#createContributionItems(org.eclipse.ui.services.IServiceLocator, IContributionRoot)}
	 * 
	 * @param item
	 *            the item to add
	 * @param visibleWhen
	 *            the visibility expression. May be <code>null</code>.
	 */
	public void addContributionItem(IContributionItem item,
			Expression visibleWhen);

	/**
	 * Registers visibilty for arbitrary {@link IContributionItem} instances
	 * that are <b>NOT</b> direct children of this container. Ie: children of a
	 * {@link IContributionManager} that has been previously registered with a
	 * call to {{@link #addContributionItem(IContributionItem, Expression)}.
	 * 
	 * @param item
	 *            the item for which to register a visibility clause
	 * @param visibleWhen
	 *            the visibility expression. May be <code>null</code> in which
	 *            case this method is a no-op.
	 */
	public void registerVisibilityForChild(IContributionItem item,
			Expression visibleWhen);
}
