/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.Set;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IMenuService;

/**
 * @since 3.5
 *
 */
public class WorkbenchMenuService implements IMenuService {

	private IEclipseContext e4Context;
	// private ServiceLocator serviceLocator;
	private ExpressionContext legacyContext;
	private MenuPersistence persistence;

	/**
	 * @param serviceLocator
	 * @param e4Context
	 */
	public WorkbenchMenuService(ServiceLocator serviceLocator, IEclipseContext e4Context) {
		// this.serviceLocator = serviceLocator;
		this.e4Context = e4Context;

		persistence = new MenuPersistence(e4Context.get(MApplication.class), e4Context);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void addSourceProvider(ISourceProvider provider) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(ISourceProvider provider) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		persistence.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.menus.IMenuService#addContributionFactory(org.eclipse.ui.menus.AbstractContributionFactory)
	 */
	public void addContributionFactory(AbstractContributionFactory factory) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.menus.IMenuService#removeContributionFactory(org.eclipse.ui.menus.AbstractContributionFactory)
	 */
	public void removeContributionFactory(AbstractContributionFactory factory) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.menus.IMenuService#populateContributionManager(org.eclipse.jface.action.ContributionManager, java.lang.String)
	 */
	public void populateContributionManager(ContributionManager mgr, String location) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.menus.IMenuService#releaseContributions(org.eclipse.jface.action.ContributionManager)
	 */
	public void releaseContributions(ContributionManager mgr) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.menus.IMenuService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		if (legacyContext == null) {
			legacyContext = new ExpressionContext(e4Context);
		}
		return legacyContext;
	}

	/**
	 * read in the menu contributions and turn them into model menu
	 * contributions
	 */
	public void readRegistry() {
		persistence.read();
	}

	public void updateManagers() {
		E4Util.unsupported("WorkbenchMenuService.updateManagers - time to update ... something"); //$NON-NLS-1$
	}

	/**
	 * @param item
	 * @param visibleWhen
	 * @param restriction
	 * @param createIdentifierId
	 */
	public void registerVisibleWhen(IContributionItem item, Expression visibleWhen,
			Set restriction, String createIdentifierId) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param item
	 * @param restriction
	 */
	public void unregisterVisibleWhen(IContributionItem item, Set restriction) {
		// TODO Auto-generated method stub

	}

}
