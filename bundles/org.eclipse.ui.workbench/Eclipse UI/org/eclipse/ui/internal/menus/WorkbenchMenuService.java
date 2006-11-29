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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.util.Util;

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
	 * Construct an 'id' string from the given URI. The resulting
	 * 'id' is the part of the URI not containing the query:
	 * <p>
	 * i.e. [menu | popup | toolbar]:id
	 * </p>
	 * 
	 * @param uri The URI to construct the id from
	 * @return The id
	 */
	private String getIdFromURI(MenuLocationURI uri) {
		return uri.getScheme() + ":" + uri.getPath(); //$NON-NLS-1$;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#getManagerForURI(org.eclipse.ui.internal.menus.MenuLocationURI)
	 */
	public List getAdditionsForURI(MenuLocationURI uri) {
		if (uri == null)
			return null;

		List caches = (List) uriToManager.get(getIdFromURI(uri));
		
		// we always return a list
		if (caches == null) {
			caches = new ArrayList();
			uriToManager.put(getIdFromURI(uri), caches);
		}
		
		return caches;
	}

	private boolean processAdditions(ContributionManager mgr, MenuAdditionCacheEntry cache) {
		int insertionIndex = getInsertionIndex(mgr, cache.getUri());
		if (insertionIndex == -1)
			return false;  // can't process (yet)
		
		// Get the additions
		List ciList = new ArrayList();
		cache.getContributionItems(ciList);
		
		// If we have any then add them at the correct location
		if (ciList.size() > 0) {
			for (Iterator ciIter = ciList.iterator(); ciIter
					.hasNext();) {
				IContributionItem ici = (IContributionItem) ciIter.next();

				// Register for 'visibleWhen' handling
				Expression visibleWhen = cache.getVisibleWhenForItem(ici);
				if (visibleWhen != null) {
					menuAuthority.addContribution(new MenuActivation(ici, visibleWhen, this));
				}
				
				mgr.insert(insertionIndex++, ici);
			}
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.IMenuService#populateMenu(org.eclipse.jface.action.ContributionManager, org.eclipse.ui.internal.menus.MenuLocationURI)
	 */
	public void populateMenu(ContributionManager mgr, MenuLocationURI uri) {
		List additionCaches = getAdditionsForURI(uri);

		List retryList = new ArrayList();
		for (Iterator iterator = additionCaches.iterator(); iterator.hasNext();) {
			MenuAdditionCacheEntry cache = (MenuAdditionCacheEntry) iterator.next();
			if (!processAdditions(mgr, cache)) {
				retryList.add(cache);
			}			
		}
		
		// OK, iteratively loop through entries whose URI's could not
		// be resolved until we either run out of entries or the list
		// doesn't change size (indicating that the remaining entries
		// can never be resolved).
		boolean done = retryList.size() == 0;
		while (!done) {
			// Clone the retry list and clear it
			List curRetry = new ArrayList(retryList);
			int  retryCount = retryList.size();
			retryList.clear();
			
			// Walk the current list seeing if any entries can now be resolved
			for (Iterator iterator = curRetry.iterator(); iterator.hasNext();) {
				MenuAdditionCacheEntry cache = (MenuAdditionCacheEntry) iterator.next();
				if (!processAdditions(mgr, cache))
					retryList.add(cache);
			}
			
			// We're done if the retryList is now empty (everything done) or
			// if the list hasn't changed at all (no hope)
			done = (retryList.size() == 0) || (retryList.size() == retryCount);
		}
		
		// Now, recurse through any sub-menus
		IContributionItem[] curItems = mgr.getItems();
		for (int i = 0; i < curItems.length; i++) {
			if (curItems[i] instanceof ContributionManager) {
				IContributionItem menuItem = (IContributionItem)curItems[i];
				MenuLocationURI subURI = new MenuLocationURI("menu:" + menuItem.getId()); //$NON-NLS-1$
				populateMenu((ContributionManager) curItems[i], subURI);
			}
		}
	}

	/**
	 * @param mgr
	 * @param uri
	 * @return
	 */
	private int getInsertionIndex(ContributionManager mgr, MenuLocationURI uri) {
		String query = uri.getQuery();
		
		int additionsIndex = -1;

		// No Query means 'after=additions' (if ther) or
		// the end of the menu
		if (query.length() == 0) {
			additionsIndex = mgr.indexOf("additions"); //$NON-NLS-1$
			if (additionsIndex == -1)
				additionsIndex = mgr.getItems().length;
			else
				++additionsIndex;
		}
		else {
			// Should be in the form "[before|after]=id"
			String[] queryParts = Util.split(query, '=');
			if (queryParts[1].length() > 0) {
				additionsIndex = mgr.indexOf(queryParts[1]);
				if (additionsIndex != -1 && queryParts[0].equals("after")) //$NON-NLS-1$
					additionsIndex++;
			}
		}
		
		return additionsIndex;
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
