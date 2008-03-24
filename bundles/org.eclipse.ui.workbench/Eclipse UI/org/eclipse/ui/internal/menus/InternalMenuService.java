/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Internal baseclass for Workbench and Window menu service implementations.
 * Methods in this class might some day make sense to live in IMenuService.
 * 
 * @since 3.3
 * 
 */
public abstract class InternalMenuService implements IMenuService {

	/**
	 * Ties an expression to {@link IContributionItem#setVisible(boolean)}.
	 * 
	 * @param item
	 *            the item
	 * @param visibleWhen
	 *            the expression
	 * @param restriction
	 *            the restriction expression
	 * @param identifierId
	 * 			  the activity identifier id
	 */
	public abstract void registerVisibleWhen(final IContributionItem item,
			final Expression visibleWhen, final Expression restriction, String identifierID);

	/**
	 * Removes any expressions bound to
	 * {@link IContributionItem#setVisible(boolean)} of the given item
	 * 
	 * @param item
	 *            the item to unbind
	 */
	public abstract void unregisterVisibleWhen(IContributionItem item);

	/**
	 * Return a list of {@link MenuAdditionCacheEntry} objects that are
	 * contributed at the given uri.
	 * 
	 * @param uri
	 *            the uri to search on
	 * @return the list of items
	 */
	public abstract List getAdditionsForURI(MenuLocationURI uri);

	public abstract void populateContributionManager(
			IServiceLocator serviceLocatorToUse, Expression restriction,
			ContributionManager mgr, String uri, boolean recurse);

	public abstract void populateContributionManager(ContributionManager mgr,
			String uri, boolean recurse);
}
