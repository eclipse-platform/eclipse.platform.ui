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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.ISourceProvider;

/**
 * <p>
 * Provides services related to contributing menu elements to a workbench
 * window. Visibility and showing are tracked at the workbench window level.
 * </p>
 * <p>
 * This class is only intended for internal use within the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public final class WindowMenuService implements IMenuService {

	/**
	 * The central authority for determining which menus are visible within this
	 * window.
	 */
	private final MenuAuthority menuAuthority;

	/**
	 * The parent menu service for this window. This parent must track menu
	 * definitions and the regsitry. Must not be <code>null</code>
	 */
	private final IMenuService parent;

	/**
	 * Constructs a new instance of <code>MenuService</code> using a menu
	 * manager.
	 * 
	 * @param parent
	 *            The parent menu service for this window. This parent must
	 *            track menu definitions and the regsitry. Must not be
	 *            <code>null</code>
	 * @param window
	 *            The workbench window to use; must not be <code>null</code>.
	 */
	public WindowMenuService(final IMenuService parent, final Window window) {
		if (parent == null) {
			throw new NullPointerException(
					"The parent service must not be null"); //$NON-NLS-1$
		}
		if (window == null) {
			throw new NullPointerException("The window must not be null"); //$NON-NLS-1$
		}

		this.menuAuthority = new MenuAuthority(window);
		this.parent = parent;
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
		menuAuthority.dispose();
	}

	public final SActionSet getActionSet(final String actionSetId) {
		return parent.getActionSet(actionSetId);
	}

	public final SActionSet[] getDefinedActionSets() {
		return parent.getDefinedActionSets();
	}

	public final SGroup[] getDefinedGroups() {
		return parent.getDefinedGroups();
	}

	public final SItem[] getDefinedItems() {
		return parent.getDefinedItems();
	}

	public final SMenu[] getDefinedMenus() {
		return parent.getDefinedMenus();
	}

	public final SWidget[] getDefinedWidgets() {
		return parent.getDefinedWidgets();
	}

	public final SGroup getGroup(final String groupId) {
		return parent.getGroup(groupId);
	}

	public final SItem getItem(final String itemId) {
		return parent.getItem(itemId);
	}

	public final SMenuLayout getLayout() {
		return parent.getLayout();
	}

	public final SMenu getMenu(final String menuId) {
		return parent.getMenu(menuId);
	}

	public final SWidget getWidget(final String widgetId) {
		return parent.getWidget(widgetId);
	}

	public final void readRegistry() {
		parent.readRegistry();
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#getManagerForURI(java.net.URI)
	 */
	public List getAdditionsForURI(MenuLocationURI uri) {
		return parent.getAdditionsForURI(uri);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#populateMenu(org.eclipse.jface.action.MenuManager, org.eclipse.ui.internal.menus.MenuLocationURI)
	 */
	public void populateMenu(ContributionManager mgr, MenuLocationURI uri) {
		parent.populateMenu(mgr, uri);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#registerAdditionCache(java.lang.String, org.eclipse.ui.internal.menus.MenuAddition)
	 */
	public void registerAdditionCache(MenuLocationURI  uri, MenuAddition addition) {
		parent.registerAdditionCache(uri, addition);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return parent.getCurrentState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#addCacheForURI(org.eclipse.ui.internal.menus.MenuLocationURI, org.eclipse.ui.internal.menus.MenuCacheEntry)
	 */
	public void addCacheForURI(MenuCacheEntry cache) {
		parent.addCacheForURI(cache);
	}
}
