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

import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.expressions.WorkbenchWindowExpression;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;

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
public final class WindowMenuService extends InternalMenuService {

	/**
	 * The parent menu service for this window. This parent must track menu
	 * definitions and the regsitry. Must not be <code>null</code>
	 */
	private final WorkbenchMenuService parent;
	private IServiceLocator serviceLocator;
	private Expression restrictionExpression;

	/**
	 * Constructs a new instance of <code>MenuService</code> using a menu
	 * manager.
	 * 
	 * @param parent
	 *            The parent menu service for this window. This parent must
	 *            track menu definitions and the regsitry. Must not be
	 *            <code>null</code>
	 */
	public WindowMenuService(final IServiceLocator serviceLocator) {
		IMenuService menuService = (IMenuService) serviceLocator
				.getService(IMenuService.class);
		if (menuService == null
				|| !(menuService instanceof WorkbenchMenuService)) {
			throw new NullPointerException(
					"The parent service must not be null"); //$NON-NLS-1$
		}
		IWorkbenchWindow window = (IWorkbenchWindow) serviceLocator
				.getService(IWorkbenchWindow.class);
		if (window == null)
			throw new NullPointerException("Window cannot be null"); //$NON-NLS-1$

		restrictionExpression = new WorkbenchWindowExpression(window);

		this.parent = (WorkbenchMenuService) menuService;
		this.serviceLocator = serviceLocator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#populateMenu(org.eclipse.jface.action.MenuManager,
	 *      org.eclipse.ui.internal.menus.MenuLocationURI)
	 */
	public void populateContributionManager(ContributionManager mgr, String uri) {
		parent.populateContributionManager(serviceLocator,
				restrictionExpression, mgr, uri, true);
	}

	public void populateContributionManager(ContributionManager mgr,
			String uri, boolean recurse) {
		parent.populateContributionManager(serviceLocator,
				restrictionExpression, mgr, uri, recurse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return parent.getCurrentState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#addCacheForURI(org.eclipse.ui.internal.menus.MenuLocationURI,
	 *      org.eclipse.ui.internal.menus.MenuCacheEntry)
	 */
	public void addContributionFactory(AbstractContributionFactory cache) {
		parent.addContributionFactory(cache);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.IMenuService#releaseMenu(org.eclipse.jface.action.ContributionManager)
	 */
	public void releaseContributions(ContributionManager mgr) {
		parent.releaseContributions(mgr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.menus.IMenuService#removeContributionFactory(org.eclipse.ui.menus.AbstractContributionFactory)
	 */
	public void removeContributionFactory(AbstractContributionFactory factory) {
		parent.removeContributionFactory(factory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void addSourceProvider(ISourceProvider provider) {
		throw new RuntimeException("addSourceProvider"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(ISourceProvider provider) {
		throw new RuntimeException("removeSourceProvider"); //$NON-NLS-1$
	}

	public List getAdditionsForURI(MenuLocationURI uri) {
		return parent.getAdditionsForURI(uri);
	}

	public void registerVisibleWhen(final IContributionItem item,
			final Expression visibleWhen, final Expression restriction,
			String identifierID) {
		parent
				.registerVisibleWhen(item, visibleWhen, restriction,
						identifierID);
	}

	public void unregisterVisibleWhen(IContributionItem item) {
		parent.unregisterVisibleWhen(item);
	}
}
