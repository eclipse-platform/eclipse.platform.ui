/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.ISources;
import org.eclipse.ui.services.IServiceWithSources;

/**
 * <p>
 * Provides services related to the menu architecture within the workbench. This
 * service can be used to access the set of menu, tool bar and status line
 * contributions. It can also be used to contribute additional items to the
 * menu, tool bar and status line.
 * </p>
 * <p>
 * This interface should not be implemented or extended by clients.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class is meant to exist in the <code>org.eclipse.ui.menus</code>
 * package.
 * </p>
 * 
 * @since 3.2
 */
public interface IMenuService extends IServiceWithSources {

	/**
	 * <p>
	 * Reads the menu information from the registry and the preferences. This
	 * will overwrite any of the existing information in the menu service. This
	 * method is intended to be called during start-up. When this method
	 * completes, this menu service will reflect the current state of the
	 * registry and preference store.
	 * </p>
	 * <p>
	 * This will also attach listeners that will monitor changes to the registry
	 * and preference store and update appropriately.
	 * </p>
	 */
	public void readRegistry();

	//
	// additions for 3.3 support
	//

	/**
	 * Transient - get the contribution manager for this URI.
	 * 
	 * @param uri
	 *            The uri
	 * @return a contribution manager
	 * 
	 * @since 3.3
	 */
	public List getAdditionsForURI(MenuLocationURI uri);

	/**
	 * Contribute and initialize the cache. This should only be called once per
	 * cache.
	 * 
	 * @param uri
	 * @param cache
	 * @since 3.3
	 */
	public void addMenuCache(MenuCacheEntry cache);

	/**
	 * populate a <code>ContributionManager</code> with the set of
	 * <code>IContributionElement</code>'s representing the additions.
	 * 
	 * @param mgr
	 *            The MenuManager to populate
	 * @param uri
	 *            The URI to use to locate the menu additions
	 */
	public void populateMenu(ContributionManager mgr, MenuLocationURI uri);

	/**
	 * Before calling dispose() on a ContributionManager populated by the menu
	 * service, you must unregister the contribution manager. This takes care of
	 * unregistering any IContributionItems that have their visibleWhen clause
	 * managed by this menu service.
	 * 
	 * @param mgr
	 *            The manager that was populated by a call to populateMenu
	 */
	public void releaseMenu(ContributionManager mgr);

	/**
	 * Get the current state as seen by the menu service.
	 * 
	 * @return an IEvaluationContext containing state variables.
	 * 
	 * @see ISources
	 */
	public IEvaluationContext getCurrentState();

	/**
	 * This item will have its visibleWhen clause managed by this menu service.
	 * The item lifecycle must be managed by this service as well.
	 * 
	 * @param item
	 *            the item to manage. Must not be <code>null</code>. The item
	 *            must return the <code>setVisible(boolean)</code> value from
	 *            its <code>isVisible()</code> method.
	 * @param visibleWhen
	 *            The visibleWhen expression. Must not be <code>null</code>.
	 */
	public void registerVisibleWhen(IContributionItem item,
			Expression visibleWhen);

	/**
	 * Remove this item from having its visibleWhen clause managed by this menu
	 * service. This method does nothing if the item is not managed by this menu
	 * service.
	 * 
	 * @param item
	 *            the item to remove. Must not be <code>null</code>.
	 */
	public void unregisterVisibleWhen(IContributionItem item);
}
