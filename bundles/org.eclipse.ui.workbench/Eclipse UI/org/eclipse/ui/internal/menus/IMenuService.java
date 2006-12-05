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

import java.util.Collection;
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
	 * Contributes the given menu element within the context of this service. If
	 * this service was retrieved from the workbench, then this contribution
	 * will be visible globally. If the service was retrieved from a nested
	 * component, then the contribution will only be visible within that
	 * component.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the contributions submitted through a
	 * particular service will be cleaned up when that service is destroyed. So,
	 * for example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would remove all of its contributions when the site is destroyed.
	 * </p>
	 * 
	 * @param menuElement
	 *            The menu element to contribute; must not be <code>null</code>.
	 * @return A token which can be used to later cancel the contribution. Only
	 *         someone with access to this token can cancel the contribution.
	 *         The contribution will automatically be cancelled if the context
	 *         from which this service was retrieved is destroyed.
	 */
	public IMenuContribution contributeMenu(MenuElement menuElement);

	/**
	 * <p>
	 * Contributes the given menu element within the context of this service.
	 * The menu element becomes visible when <code>expression</code> evaluates
	 * to <code>true</code>.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the contribution submitted through a
	 * particular service will be cleaned up when that services is destroyed.
	 * So, for example, a service retrieved from a
	 * <code>IWorkbenchPartSite</code> would remove all of its contributions
	 * when the site is destroyed.
	 * </p>
	 * 
	 * @param menuElement
	 *            The menu element to contribution; must not be
	 *            <code>null</code>.
	 * @param expression
	 *            This expression must evaluate to <code>true</code> before
	 *            this handler will really become visible. The expression may be
	 *            <code>null</code> if the menu element should always be
	 *            visible.
	 * @return A token which can be used to later cancel the contribution. Only
	 *         someone with access to this token can cancel the contribution.
	 *         The contribution will automatically be cancelled if the context
	 *         from which this service was retrieved is destroyed.
	 */
	public IMenuContribution contributeMenu(MenuElement menuElement,
			Expression expression);

	/**
	 * Retrieves the action set with the given identifier. If no such action set
	 * exists, then an undefined action set is created and returned. An action
	 * set is a logical grouping of menu elements, that can be made visible or
	 * hidden as a group.
	 * 
	 * @param actionSetId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return An action set with the given identifier, either defined or
	 *         undefined.
	 */
	public SActionSet getActionSet(String actionSetId);

	/**
	 * Returns those action sets that are defined.
	 * 
	 * @return The defined action sets; this value may be empty, but it is never
	 *         <code>null</code>.
	 */
	public SActionSet[] getDefinedActionSets();

	/**
	 * Returns those groups that are defined.
	 * 
	 * @return The defined groups; this value may be empty, but it is never
	 *         <code>null</code>.
	 */
	public SGroup[] getDefinedGroups();

	/**
	 * Returns those items that are defined.
	 * 
	 * @return The defined items; this value may be empty, but it is never
	 *         <code>null</code>.
	 */
	public SItem[] getDefinedItems();

	/**
	 * Returns those menus that are defined.
	 * 
	 * @return The defined menus; this value may be empty, but it is never
	 *         <code>null</code>.
	 */
	public SMenu[] getDefinedMenus();

	/**
	 * Returns those widgets that are defined.
	 * 
	 * @return The defined widgets; this value may be empty, but it is never
	 *         <code>null</code>.
	 */
	public SWidget[] getDefinedWidgets();

	/**
	 * Retrieves the group with the given identifier. If no such group exists,
	 * then an undefined group is created and returned. A group is a logical
	 * grouping of items and widgets within a menu.
	 * 
	 * @param groupId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A group with the given identifier, either defined or undefined.
	 */
	public SGroup getGroup(String groupId);

	/**
	 * Retrieves the item with the given identifier. If no such item exists,
	 * then an undefined item is created and returned. An item is a single entry
	 * in a menu, tool bar or status line.
	 * 
	 * @param itemId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return An item with the given identifier, either defined or undefined.
	 */
	public SItem getItem(String itemId);

	/**
	 * <p>
	 * Retrieves the layout for the menu elements held by this menu manager.
	 * This layout does not consider visibility or whether the elements are
	 * currently shown. It is simply the layout if everything was visible and
	 * showing. It also does not consider dynamic menu elements, which will be
	 * asked to make changes to the layout before the menu element is shown.
	 * </p>
	 * <p>
	 * The result of this computation is cached between subsequent calls. So, if
	 * no changes are made to the menu elements, the layout can be retrieved in
	 * constant time. Otherwise, it will take <code>O(n)</code> time to
	 * compute, where <code>n</code> is the number of menu elements held by
	 * this manager.
	 * </p>
	 * 
	 * @return The menu layout; never <code>null</code>.
	 */
	public SMenuLayout getLayout();

	/**
	 * Retrieves the menu with the given identifier. If no such menu exists,
	 * then an undefined group is created and returned. A menu is either a
	 * top-level menu, a context menu, a cool bar or a tool bar.
	 * 
	 * @param menuId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A menu with the given identifier, either defined or undefined.
	 */
	public SMenu getMenu(String menuId);

	/**
	 * Retrieves the widget with the given identifier. If no such widget exists,
	 * then an undefined widget is created and returned. A widget is a custom
	 * contribution into a menu. This allows the plug-in to draw the widgets as
	 * desired.
	 * 
	 * @param widgetId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return A widget with the given identifier, either defined or undefined.
	 */
	public SWidget getWidget(String widgetId);

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

	/**
	 * Removes the given contribution within the context of this service. If the
	 * contribution was contributed with a different service, then it must be
	 * removed from that service instead. It is only possible to retract a
	 * contribution with this method. That is, you must have the same
	 * <code>IMenuContribution</code> used to contribute.
	 * 
	 * @param contribution
	 *            The token that was returned from a call to
	 *            <code>contributeMenu</code>; must not be <code>null</code>.
	 */
	public void removeContribution(IMenuContribution contribution);

	/**
	 * Removes the given contribution within the context of this service. If the
	 * contribution was contributed with a different service, then it must be
	 * removed from that service instead. It is only possible to retract a
	 * contribution with this method. That is, you must have the same
	 * <code>IMenuContribution</code> used to contribute.
	 * 
	 * @param contributions
	 *            The tokens that were returned from a call to
	 *            <code>contributeMenu</code>. This collection must only
	 *            contain instances of <code>IMenuContribution</code>. The
	 *            collection must not be <code>null</code>.
	 */
	public void removeContributions(Collection contributions);

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
	public void addCacheForURI(MenuCacheEntry cache);

	/**
	 * Register a new menu addition cache with the service. This entry
	 * represents the 'root' location of some id'd menu. Submenus are explicitly
	 * registered here so that additions can be made directly into sub-menus.
	 * 
	 * @param id
	 *            The id representing the 'key' for the map
	 * @param addition
	 *            The cache element representing the 'value' for the map.
	 */
	public void registerAdditionCache(MenuLocationURI uri, MenuAddition addition);

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
