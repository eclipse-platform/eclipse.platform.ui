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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IMenuService;

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
public final class WorkbenchMenuService implements IMenuService {

	/**
	 * The central authority for determining which menus are visible within this
	 * window.
	 */
	private final MenuAuthority menuAuthority;

	/**
	 * The class providing persistence for this service.
	 */
	private final MenuPersistence menuPersistence;

	/**
	 * Constructs a new instance of <code>MenuService</code> using a menu
	 * manager.
	 */
	public WorkbenchMenuService() {
		this.menuAuthority = new MenuAuthority();
		this.menuPersistence = new MenuPersistence(this);
	}

	public final void addSourceProvider(final ISourceProvider provider) {
		menuAuthority.addSourceProvider(provider);
	}

	public final void dispose() {
		menuPersistence.dispose();
		menuAuthority.dispose();
	}

	public final void readRegistry() {
		menuPersistence.read();
	}

	public final void removeSourceProvider(final ISourceProvider provider) {
		menuAuthority.removeSourceProvider(provider);
	}

	//
	// 3.3 common menu service information
	//
	private Map uriToManager = new HashMap();

	private Map contributionManagerTracker = new HashMap();

	private IMenuListener menuTrackerListener;

	private Map cacheTracker = new HashMap();

	/**
	 * Construct an 'id' string from the given URI. The resulting 'id' is the
	 * part of the URI not containing the query:
	 * <p>
	 * i.e. [menu | popup | toolbar]:id
	 * </p>
	 * 
	 * @param uri
	 *            The URI to construct the id from
	 * @return The id
	 */
	private String getIdFromURI(MenuLocationURI uri) {
		return uri.getScheme() + ":" + uri.getPath(); //$NON-NLS-1$;
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#addCacheForURI(org.eclipse.ui.internal.menus.MenuCacheEntry)
	 */
	public void addContributionFactory(AbstractContributionFactory factory) {
		if (factory == null || factory.getLocation() == null)
			return;

		MenuLocationURI uri = new MenuLocationURI(factory.getLocation());
		String cacheId = getIdFromURI(uri);
		List caches = (List) uriToManager.get(cacheId);

		// we always return a list
		if (caches == null) {
			caches = new ArrayList();
			uriToManager.put(cacheId, caches);
		}
		caches.add(factory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.menus.IMenuService#removeContributionFactory(org.eclipse.ui.menus.AbstractContributionFactory)
	 */
	public void removeContributionFactory(AbstractContributionFactory factory) {
		if (factory == null || factory.getLocation() == null)
			return;

		MenuLocationURI uri = new MenuLocationURI(factory.getLocation());
		String cacheId = getIdFromURI(uri);
		List caches = (List) uriToManager.get(cacheId);
		if (caches != null) {
			caches.remove(factory);
		}
	}

	private boolean processAdditions(ContributionManager mgr,
			AbstractContributionFactory cache) {
		int insertionIndex = getInsertionIndex(mgr, cache.getLocation());
		if (insertionIndex == -1)
			return false; // can't process (yet)

		// Get the additions
		List ciList = new ArrayList();
		cache.createContributionItems(this, ciList);

		// If we have any then add them at the correct location
		if (ciList.size() > 0) {
			track(mgr, cache, ciList);
			for (Iterator ciIter = ciList.iterator(); ciIter.hasNext();) {
				IContributionItem ici = (IContributionItem) ciIter.next();

				mgr.insert(insertionIndex++, ici);
			}
		}

		return true;
	}

	/**
	 * @param mgr
	 * @param cache
	 * @param ciList
	 */
	private void track(ContributionManager mgr,
			AbstractContributionFactory cache, List ciList) {
		List contributions = (List) contributionManagerTracker.get(mgr);
		if (contributions == null) {
			contributions = new ArrayList();
			contributionManagerTracker.put(mgr, contributions);
			if (mgr instanceof IMenuManager) {
				IMenuManager m = (IMenuManager) mgr;
				if (m.getRemoveAllWhenShown()) {
					m.addMenuListener(getMenuTrackerListener());
				}
			}
		}
		contributions.add(ciList);

		cacheTracker.put(ciList, cache);
	}

	/**
	 * @return
	 */
	private IMenuListener getMenuTrackerListener() {
		if (menuTrackerListener == null) {
			menuTrackerListener = new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					sweepContributions(manager);
				}
			};
		}
		return menuTrackerListener;
	}

	/**
	 * @param manager
	 */
	protected void sweepContributions(IMenuManager manager) {
		List contributions = (List) contributionManagerTracker.get(manager);
		if (contributions == null) {
			return;
		}
		Iterator i = contributions.iterator();
		while (i.hasNext()) {
			final List items = (List) i.next();
			boolean removed = false;
			Iterator j = items.iterator();
			while (j.hasNext()) {
				IContributionItem item = (IContributionItem) j.next();
				if (item instanceof ContributionItem
						&& ((ContributionItem) item).getParent() == null) {
					removed = true;
					releaseItem(item);
				}
			}
			if (removed) {
				releaseCache(items);
				i.remove();
			}
		}
	}

	/**
	 * @param items
	 */
	private void releaseCache(final List items) {
		final AbstractContributionFactory cache = (AbstractContributionFactory) cacheTracker
				.remove(items);
		if (cache != null) {
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
				}

				public void run() throws Exception {
					cache.releaseContributionItems(WorkbenchMenuService.this,
							items);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#populateMenu(org.eclipse.jface.action.ContributionManager,
	 *      org.eclipse.ui.internal.menus.MenuLocationURI)
	 */
	public void populateContributionManager(ContributionManager mgr, String uri) {
		MenuLocationURI contributionLocation = new MenuLocationURI(uri);
		List additionCaches = getAdditionsForURI(contributionLocation);

		List retryList = new ArrayList();
		for (Iterator iterator = additionCaches.iterator(); iterator.hasNext();) {
			AbstractContributionFactory cache = (AbstractContributionFactory) iterator
					.next();
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
			int retryCount = retryList.size();
			retryList.clear();

			// Walk the current list seeing if any entries can now be resolved
			for (Iterator iterator = curRetry.iterator(); iterator.hasNext();) {
				AbstractContributionFactory cache = (AbstractContributionFactory) iterator
						.next();
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
				String id = curItems[i].getId();
				if (id != null && id.length() > 0) {
					populateContributionManager(
							(ContributionManager) curItems[i],
							contributionLocation.getScheme() + ":" + id); //$NON-NLS-1$
				}
			} else if (curItems[i] instanceof IToolBarContributionItem) {
				IToolBarContributionItem tbci = (IToolBarContributionItem) curItems[i];
				if (tbci.getId() != null && tbci.getId().length() > 0) {
					populateContributionManager((ContributionManager) tbci
							.getToolBarManager(), contributionLocation
							.getScheme()
							+ ":" + tbci.getId()); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * @param mgr
	 * @param uri
	 * @return
	 */
	private int getInsertionIndex(ContributionManager mgr, String location) {
		MenuLocationURI uri = new MenuLocationURI(location);
		String query = uri.getQuery();

		int additionsIndex = -1;

		// No Query means 'after=additions' (if ther) or
		// the end of the menu
		if (query.length() == 0 || query.equals("after=additions")) { //$NON-NLS-1$
			additionsIndex = mgr.indexOf("additions"); //$NON-NLS-1$
			if (additionsIndex == -1)
				additionsIndex = mgr.getItems().length;
			else
				++additionsIndex;
		} else {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return menuAuthority.getCurrentState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#registerVisibleWhen(org.eclipse.jface.action.IContributionItem,
	 *      org.eclipse.core.expressions.Expression)
	 */
	public void registerVisibleWhen(IContributionItem item,
			Expression visibleWhen) {
		menuAuthority.addContribution(item, visibleWhen);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#unregisterVisibleWhen(org.eclipse.jface.action.IContributionItem)
	 */
	public void unregisterVisibleWhen(IContributionItem item) {
		menuAuthority.removeContribition(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#releaseMenu(org.eclipse.jface.action.ContributionManager)
	 */
	public void releaseContributions(ContributionManager mgr) {
		List contributions = (List) contributionManagerTracker.remove(mgr);
		if (contributions == null) {
			return;
		}

		if (mgr instanceof IMenuManager) {
			IMenuManager m = (IMenuManager) mgr;
			if (m.getRemoveAllWhenShown()) {
				m.removeMenuListener(getMenuTrackerListener());
			}
		}

		Iterator i = contributions.iterator();
		while (i.hasNext()) {
			final List items = (List) i.next();
			Iterator j = items.iterator();
			while (j.hasNext()) {
				IContributionItem item = (IContributionItem) j.next();
				releaseItem(item);
			}
			releaseCache(items);
		}
		contributions.clear();
	}

	/**
	 * @param item
	 */
	private void releaseItem(IContributionItem item) {
		unregisterVisibleWhen(item);
		if (item instanceof ContributionManager) {
			releaseContributions((ContributionManager) item);
		} else if (item instanceof IToolBarContributionItem) {
			IToolBarContributionItem tbci = (IToolBarContributionItem) item;
			releaseContributions((ContributionManager) tbci
					.getToolBarManager());
		}
	}
}
