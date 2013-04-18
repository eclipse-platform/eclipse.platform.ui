/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IMenuService;

/**
 * @since 3.105
 * 
 */
public class SlaveMenuService implements IMenuService {
	private IMenuService parentService;

	/**
	 * @param provider
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void addSourceProvider(ISourceProvider provider) {
		parentService.addSourceProvider(provider);
	}

	/**
	 * @param provider
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(ISourceProvider provider) {
		parentService.removeSourceProvider(provider);
	}

	/**
	 * @param factory
	 * @see org.eclipse.ui.menus.IMenuService#addContributionFactory(org.eclipse.ui.menus.AbstractContributionFactory)
	 */
	public void addContributionFactory(AbstractContributionFactory factory) {
		parentService.addContributionFactory(factory);
	}

	/**
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		// nothing to do here yet.
	}

	/**
	 * @param factory
	 * @see org.eclipse.ui.menus.IMenuService#removeContributionFactory(org.eclipse.ui.menus.AbstractContributionFactory)
	 */
	public void removeContributionFactory(AbstractContributionFactory factory) {
		parentService.removeContributionFactory(factory);
	}

	/**
	 * @param mgr
	 * @param location
	 * @see org.eclipse.ui.menus.IMenuService#populateContributionManager(org.eclipse.jface.action.ContributionManager,
	 *      java.lang.String)
	 */
	public void populateContributionManager(ContributionManager mgr, String location) {
		populateContributionManager(model, mgr, location);
	}

	public void populateContributionManager(MApplicationElement model, ContributionManager mgr,
			String location) {
		if (parentService instanceof SlaveMenuService) {
			((SlaveMenuService) parentService).populateContributionManager(model, mgr, location);
		} else if (parentService instanceof WorkbenchMenuService) {
			((WorkbenchMenuService) parentService)
					.populateContributionManager(model, mgr, location);
		}
	}
	/**
	 * @param mgr
	 * @see org.eclipse.ui.menus.IMenuService#releaseContributions(org.eclipse.jface.action.ContributionManager)
	 */
	public void releaseContributions(ContributionManager mgr) {
		parentService.releaseContributions(mgr);
	}

	/**
	 * @return
	 * @see org.eclipse.ui.menus.IMenuService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return parentService.getCurrentState();
	}

	public SlaveMenuService(IMenuService parent, MApplicationElement model) {
		parentService = parent;
		this.model = model;
	}

	private MApplicationElement model;

	public MApplicationElement getModel() {
		return model;
	}
}
