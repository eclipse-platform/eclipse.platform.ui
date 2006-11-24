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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.commands.ICommandService;

/**
 * <p>
 * Provides services related to contributing menu elements to the workbench.
 * </p>
 * <p>
 * This class is only intended for internal use within the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
/**
 * @since 3.3
 *
 */
public final class WorkbenchMenuService implements IMenuService {

	/**
	 * The central authority for determining which menus are visible within this
	 * window.
	 */
	private final MenuAuthority menuAuthority;

	/**
	 * The menu manager underlying this menu service; never <code>null</code>.
	 */
	private final SMenuManager menuManager;

	/**
	 * The class providing persistence for this service.
	 */
	private final MenuPersistence menuPersistence;

	/**
	 * Constructs a new instance of <code>MenuService</code> using a menu
	 * manager.
	 * 
	 * @param menuManager
	 *            The menu manager to use; must not be <code>null</code>.
	 * @param commandService
	 *            The command service to use; must not be <code>null</code>.
	 */
	public WorkbenchMenuService(final SMenuManager menuManager,
			final ICommandService commandService) {
		this.menuAuthority = new MenuAuthority(null);
		this.menuManager = menuManager;
		this.menuPersistence = new MenuPersistence(this, commandService);
	}

	public final void addSourceProvider(final ISourceProvider provider) {
		menuAuthority.addSourceProvider(provider);
	}

	public final IMenuContribution contributeMenu(final MenuElement menuElement) {
		return contributeMenu(menuElement, null);
	}

	public final IMenuContribution contributeMenu(
			final MenuElement menuElement, final Expression expression) {
		final IMenuContribution contribution = new MenuContribution(
				menuElement, expression, this);
		menuAuthority.contributeMenu(contribution);
		return contribution;
	}

	public final void dispose() {
		menuPersistence.dispose();
		menuAuthority.dispose();
	}

	public final SActionSet getActionSet(final String actionSetId) {
		return menuManager.getActionSet(actionSetId);
	}

	public final SActionSet[] getDefinedActionSets() {
		return menuManager.getDefinedActionSets();
	}

	public final SGroup[] getDefinedGroups() {
		return menuManager.getDefinedGroups();
	}

	public final SItem[] getDefinedItems() {
		return menuManager.getDefinedItems();
	}

	public final SMenu[] getDefinedMenus() {
		return menuManager.getDefinedMenus();
	}

	public final SWidget[] getDefinedWidgets() {
		return menuManager.getDefinedWidgets();
	}

	public final SGroup getGroup(final String groupId) {
		return menuManager.getGroup(groupId);
	}

	public final SItem getItem(final String itemId) {
		return menuManager.getItem(itemId);
	}

	public final SMenuLayout getLayout() {
		return menuManager.getLayout();
	}

	public final SMenu getMenu(final String menuId) {
		return menuManager.getMenu(menuId);
	}

	public final SWidget getWidget(final String widgetId) {
		return menuManager.getWidget(widgetId);
	}

	public final void readRegistry() {
		menuPersistence.read();
	}

	public final void removeContribution(final IMenuContribution contribution) {
		if (contribution.getMenuService() == this) {
			menuAuthority.removeContribution(contribution);
		}
	}

	public final void removeContributions(final Collection contributions) {
		final Iterator contributionItr = contributions.iterator();
		while (contributionItr.hasNext()) {
			final IMenuContribution contribution = (IMenuContribution) contributionItr
					.next();
			removeContribution(contribution);
		}
	}

	public final void removeSourceProvider(final ISourceProvider provider) {
		menuAuthority.removeSourceProvider(provider);
	}
	
	//
	// 3.3 common menu service information
	//
	private Map uriToManager = new HashMap();

	/**
	 * Construct an 'id' string from the given URI.
	 * @param uri The URI to construct teh id from
	 * @return The id
	 */
	private String getIdFromURI(MenuLocationURI uri) {
		return uri.getScheme() + ":" + uri.getPath(); //$NON-NLS-1$;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#getManagerForURI(org.eclipse.ui.internal.menus.MenuLocationURI)
	 */
	public MenuAddition getManagerForURI(MenuLocationURI uri) {
		if (uri == null)
			return null;

		MenuAddition mgr = (MenuAddition) uriToManager.get(getIdFromURI(uri));
		return mgr;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#populateMenu(org.eclipse.jface.action.ContributionManager, org.eclipse.ui.internal.menus.MenuLocationURI)
	 */
	public void populateMenu(ContributionManager mgr, MenuLocationURI uri) {
		MenuAddition additionCache = getManagerForURI(uri);
		if (additionCache != null)
			additionCache.populateMenuManager(mgr);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#registerAdditionCache(java.lang.String, org.eclipse.ui.internal.menus.MenuAddition)
	 */
	public void registerAdditionCache(MenuLocationURI uri, MenuAddition addition) {
		uriToManager.put(getIdFromURI(uri), addition);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#addContribution(org.eclipse.ui.internal.menus.MenuActivation)
	 */
	public void addContribution(MenuActivation menuItem) {
		menuAuthority.addContribution(menuItem);
	}
	
	public IEvaluationContext getCurrentState() {
		return menuAuthority.getCurrentState();
	}
}
