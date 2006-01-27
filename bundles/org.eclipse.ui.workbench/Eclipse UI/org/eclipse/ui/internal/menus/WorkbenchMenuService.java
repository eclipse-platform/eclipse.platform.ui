/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.menus.MenuElement;
import org.eclipse.jface.menus.SActionSet;
import org.eclipse.jface.menus.SGroup;
import org.eclipse.jface.menus.SItem;
import org.eclipse.jface.menus.SMenu;
import org.eclipse.jface.menus.SMenuLayout;
import org.eclipse.jface.menus.SMenuManager;
import org.eclipse.jface.menus.SWidget;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.menus.IMenuContribution;
import org.eclipse.ui.menus.IMenuService;

/**
 * <p>
 * Provides services related to contributing menu elements to the workbench.
 * </p>
 * <p>
 * This class is only intended for internal use within the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
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
}
