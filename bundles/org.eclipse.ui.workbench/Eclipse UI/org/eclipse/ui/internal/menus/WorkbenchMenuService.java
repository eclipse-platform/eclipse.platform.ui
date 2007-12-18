/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IIdentifierListener;
import org.eclipse.ui.activities.IdentifierEvent;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.expressions.AlwaysEnabledExpression;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.services.IRestrictionService;
import org.eclipse.ui.internal.services.RestrictionListener;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.services.IEvaluationReference;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;

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
public final class WorkbenchMenuService extends InternalMenuService {

	/**
	 * A combined property and activity listener that updates the visibility of
	 * contribution items in the new menu system.
	 * 
	 * @since 3.3
	 */
	private final class ContributionItemUpdater implements
			IPropertyChangeListener, IIdentifierListener {

		private final IContributionItem item;
		private IIdentifier identifier;
		private boolean lastExpressionResult = true;

		private ContributionItemUpdater(IContributionItem item,
				IIdentifier identifier) {
			this.item = item;
			if (identifier != null) {
				this.identifier = identifier;
				this.identifier.addIdentifierListener(this);
				updateVisibility(); // force initial visibility to fall in line
				// with activity enablement
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty() == PROP_VISIBLE) {
				if (event.getNewValue() != null) {
					this.lastExpressionResult = ((Boolean) event.getNewValue())
							.booleanValue();
				} else {
					this.lastExpressionResult = false;
				}
				updateVisibility();
			}
		}

		private void updateVisibility() {
			boolean visible = identifier != null ? (identifier.isEnabled() && lastExpressionResult)
					: lastExpressionResult;
			item.setVisible(visible);

			IContributionManager parent = null;
			if (item instanceof ContributionItem) {
				parent = ((ContributionItem) item).getParent();

			} else if (item instanceof MenuManager) {
				parent = ((MenuManager) item).getParent();
			}
			if (parent != null) {
				parent.markDirty();
				managersAwaitingUpdates.add(parent);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.activities.IIdentifierListener#identifierChanged(org.eclipse.ui.activities.IdentifierEvent)
		 */
		public void identifierChanged(IdentifierEvent identifierEvent) {
			updateVisibility();
		}

		/**
		 * Dispose of this updater
		 */
		public void dispose() {
			identifier.removeIdentifierListener(this);
		}
	}

	/**
	 * 
	 */
	private static final String PROP_VISIBLE = "visible"; //$NON-NLS-1$

	/**
	 * The class providing persistence for this service.
	 */
	private final MenuPersistence menuPersistence;

	/**
	 * The central authority for determining which menus are visible within this
	 * window.
	 */
	private IEvaluationService evaluationService;

	private IPropertyChangeListener serviceListener;

	/**
	 * The service locator into which this service will be inserted.
	 */
	private IServiceLocator serviceLocator;

	private IActivityManagerListener activityManagerListener;

	private IRestrictionService restrictionService;

	/**
	 * Constructs a new instance of <code>MenuService</code> using a menu
	 * manager.
	 */
	public WorkbenchMenuService(IServiceLocator serviceLocator) {
		this.menuPersistence = new MenuPersistence(this);
		this.serviceLocator = serviceLocator;
		evaluationService = (IEvaluationService) serviceLocator
				.getService(IEvaluationService.class);
		restrictionService = (IRestrictionService) serviceLocator
				.getService(IRestrictionService.class);
		evaluationService.addServiceListener(getServiceListener());
		((IWorkbench) serviceLocator.getService(IWorkbench.class))
				.getActivitySupport().getActivityManager()
				.addActivityManagerListener(getActivityManagerListener());
	}

	/**
	 * @return
	 */
	private IActivityManagerListener getActivityManagerListener() {
		if (activityManagerListener == null) {
			activityManagerListener = new IActivityManagerListener() {

				public void activityManagerChanged(
						ActivityManagerEvent activityManagerEvent) {
					if (activityManagerEvent.haveEnabledActivityIdsChanged()) {
						updateManagers(); // called after all identifiers have
						// been update - now update the
						// managers
					}

				}
			};
		}
		return activityManagerListener;
	}

	/**
	 * @return
	 */
	private IPropertyChangeListener getServiceListener() {
		if (serviceListener == null) {
			serviceListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (event.getProperty().equals(
							IEvaluationService.PROP_NOTIFYING)) {
						if (!((Boolean) event.getNewValue()).booleanValue()) {
							// if it's false, the evaluation service has
							// finished
							// with its latest round of updates
							updateManagers();
						}
					}
				}
			};
		}
		return serviceListener;
	}

	private void updateManagers() {
		Object[] managers = managersAwaitingUpdates.toArray();
		managersAwaitingUpdates.clear();
		for (int i = 0; i < managers.length; i++) {
			IContributionManager mgr = (IContributionManager) managers[i];
			mgr.update(true);
			if (mgr instanceof ToolBarManager) {
				if (!updateCoolBar((ToolBarManager) mgr)) {
					updateTrim((ToolBarManager) mgr);
				}
			} else if (mgr instanceof MenuManager) {
				IContributionManager parent = ((MenuManager) mgr).getParent();
				if (parent != null) {
					parent.update(true);
				}
			}
		}
	}

	private void updateTrim(ToolBarManager mgr) {
		Control control = mgr.getControl();
		if (control == null || control.isDisposed()) {
			return;
		}
		LayoutUtil.resize(control);
	}

	private boolean updateCoolBar(ToolBarManager mgr) {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			WorkbenchWindow window = (WorkbenchWindow) windows[i];
			ICoolBarManager cb = window.getCoolBarManager2();
			if (cb != null) {
				IContributionItem[] items = cb.getItems();
				for (int j = 0; j < items.length; j++) {
					if (items[j] instanceof ToolBarContributionItem) {
						IToolBarManager tbm = ((ToolBarContributionItem) items[j])
								.getToolBarManager();
						if (mgr == tbm) {
							cb.update(true);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public final void addSourceProvider(final ISourceProvider provider) {
		// no-op
	}

	public final void dispose() {
		menuPersistence.dispose();
		Iterator i = evaluationsByItem.values().iterator();
		while (i.hasNext()) {
			IEvaluationReference ref = (IEvaluationReference) i.next();
			evaluationService.removeEvaluationListener(ref);
		}
		evaluationsByItem.clear();
		managersAwaitingUpdates.clear();
		if (serviceListener != null) {
			evaluationService.removeServiceListener(serviceListener);
			serviceListener = null;
		}
	}

	public final void readRegistry() {
		menuPersistence.read();
	}

	public final void removeSourceProvider(final ISourceProvider provider) {
		// no-op
	}

	//
	// 3.3 common menu service information
	//
	private Map uriToManager = new HashMap();

	private Map contributionManagerTracker = new HashMap();

	private IMenuListener menuTrackerListener;

	private Map evaluationsByItem = new HashMap();

	private Map activityListenersByItem = new HashMap();

	private Set managersAwaitingUpdates = new HashSet();

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

	private boolean processAdditions(final IServiceLocator serviceLocatorToUse,
			Expression restriction, final ContributionManager mgr,
			final AbstractContributionFactory cache, final Set itemsAdded) {
		final int idx = getInsertionIndex(mgr, cache.getLocation());
		if (idx == -1)
			return false; // can't process (yet)

		// Get the additions
		final ContributionRoot ciList = new ContributionRoot(this, restriction,
				cache.getNamespace());

		ISafeRunnable run = new ISafeRunnable() {

			public void handleException(Throwable exception) {
				// TODO Auto-generated method stub

			}

			public void run() throws Exception {
				int insertionIndex = idx;
				cache.createContributionItems(serviceLocatorToUse, ciList);

				// If we have any then add them at the correct location
				if (ciList.getItems().size() > 0) {
					track(mgr, cache, ciList);
					for (Iterator ciIter = ciList.getItems().iterator(); ciIter
							.hasNext();) {
						IContributionItem ici = (IContributionItem) ciIter
								.next();
						if (ici.getId() != null) {
							itemsAdded.add(ici.getId());
						}
						final int oldSize = mgr.getSize();
						mgr.insert(insertionIndex, ici);
						if (mgr.getSize() > oldSize)
							insertionIndex++;
					}
				}
			}
		};
		SafeRunner.run(run);

		return true;
	}

	/**
	 * @param mgr
	 * @param cache
	 * @param ciList
	 */
	private void track(ContributionManager mgr,
			AbstractContributionFactory cache, ContributionRoot ciList) {
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
			final ContributionRoot items = (ContributionRoot) i.next();
			boolean removed = false;
			Iterator j = items.getItems().iterator();
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
	private void releaseCache(final ContributionRoot items) {
		items.release();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#populateMenu(org.eclipse.jface.action.ContributionManager,
	 *      org.eclipse.ui.internal.menus.MenuLocationURI)
	 */
	public void populateContributionManager(ContributionManager mgr, String uri) {
		populateContributionManager(serviceLocator, null, mgr, uri, true);
	}

	public void populateContributionManager(
			IServiceLocator serviceLocatorToUse, Expression restriction,
			ContributionManager mgr, String uri, boolean recurse) {
		MenuLocationURI contributionLocation = new MenuLocationURI(uri);
		List additionCaches = getAdditionsForURI(contributionLocation);

		List retryList = new ArrayList();
		Set itemsAdded = new HashSet();
		for (Iterator iterator = additionCaches.iterator(); iterator.hasNext();) {
			AbstractContributionFactory cache = (AbstractContributionFactory) iterator
					.next();
			if (!processAdditions(serviceLocatorToUse, restriction, mgr, cache,
					itemsAdded)) {
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
				if (!processAdditions(serviceLocatorToUse, restriction, mgr,
						cache, itemsAdded))
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
				if (id != null && id.length() > 0
						&& (recurse || itemsAdded.contains(id))) {
					populateContributionManager(serviceLocatorToUse,
							restriction, (ContributionManager) curItems[i],
							contributionLocation.getScheme() + ":" + id, true); //$NON-NLS-1$
				}
			} else if (curItems[i] instanceof IToolBarContributionItem) {
				IToolBarContributionItem tbci = (IToolBarContributionItem) curItems[i];
				if (tbci.getId() != null && tbci.getId().length() > 0
						&& (recurse || itemsAdded.contains(tbci.getId()))) {
					populateContributionManager(serviceLocatorToUse,
							restriction, (ContributionManager) tbci
									.getToolBarManager(), contributionLocation
									.getScheme()
									+ ":" + tbci.getId(), true); //$NON-NLS-1$
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
		return evaluationService.getCurrentState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#registerVisibleWhen(org.eclipse.jface.action.IContributionItem,
	 *      org.eclipse.core.expressions.Expression)
	 */
	public void registerVisibleWhen(final IContributionItem item,
			final Expression visibleWhen, final Expression restriction,
			String identifierID) {
		if (item == null) {
			throw new IllegalArgumentException("item cannot be null"); //$NON-NLS-1$
		}
		if (visibleWhen == null) {
			throw new IllegalArgumentException(
					"visibleWhen expression cannot be null"); //$NON-NLS-1$
		}
		if (evaluationsByItem.get(item) != null) {
			final String id = item.getId();
			WorkbenchPlugin.log("item is already registered: " //$NON-NLS-1$
					+ (id == null ? "no id" : id)); //$NON-NLS-1$
			return;
		}
		IIdentifier identifier = null;
		if (identifierID != null) {
			identifier = PlatformUI.getWorkbench().getActivitySupport()
					.getActivityManager().getIdentifier(identifierID);
		}
		ContributionItemUpdater listener = new ContributionItemUpdater(item,
				identifier);

		if (visibleWhen != AlwaysEnabledExpression.INSTANCE) {
			IEvaluationReference ref = evaluationService.addEvaluationListener(
					visibleWhen, listener, PROP_VISIBLE);
			if (restriction != null) {
				restrictionService.addEvaluationListener(restriction,
						new RestrictionListener(ref), RestrictionListener.PROP);
			}
			evaluationsByItem.put(item, ref);
		}
		activityListenersByItem.put(item, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#unregisterVisibleWhen(org.eclipse.jface.action.IContributionItem)
	 */
	public void unregisterVisibleWhen(IContributionItem item) {
		ContributionItemUpdater identifierListener = (ContributionItemUpdater) activityListenersByItem
				.get(item);
		if (identifierListener != null) {
			identifierListener.dispose();
		}

		IEvaluationReference ref = (IEvaluationReference) evaluationsByItem
				.remove(item);
		if (ref == null) {
			return;
		}

		evaluationService.removeEvaluationListener(ref);
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
			final ContributionRoot items = (ContributionRoot) i.next();
			Iterator j = items.getItems().iterator();
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
			releaseContributions((ContributionManager) tbci.getToolBarManager());
		}
	}
}
