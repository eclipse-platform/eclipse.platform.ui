/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.internal.IMenuServiceWorkaround;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IMenuService;

/**
 * @since 3.105
 */
public class SlaveMenuService implements IMenuService, IMenuServiceWorkaround {
	private IMenuService parentService;

	/**
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	@Override
	public void addSourceProvider(ISourceProvider provider) {
		parentService.addSourceProvider(provider);
	}

	/**
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	@Override
	public void removeSourceProvider(ISourceProvider provider) {
		parentService.removeSourceProvider(provider);
	}

	/**
	 * @see org.eclipse.ui.menus.IMenuService#addContributionFactory(org.eclipse.ui.menus.AbstractContributionFactory)
	 */
	@Override
	public void addContributionFactory(AbstractContributionFactory factory) {
		parentService.addContributionFactory(factory);
	}

	/**
	 *
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		// nothing to do here yet.
	}

	/**
	 * @see org.eclipse.ui.menus.IMenuService#removeContributionFactory(org.eclipse.ui.menus.AbstractContributionFactory)
	 */
	@Override
	public void removeContributionFactory(AbstractContributionFactory factory) {
		parentService.removeContributionFactory(factory);
	}

	/**
	 * @see org.eclipse.ui.menus.IMenuService#populateContributionManager(org.eclipse.jface.action.ContributionManager,
	 *      java.lang.String)
	 */
	@Override
	public void populateContributionManager(ContributionManager mgr, String location) {
		populateContributionManager(model, mgr, location);
	}

	public void populateContributionManager(MApplicationElement model, ContributionManager mgr, String location) {
		if (parentService instanceof SlaveMenuService) {
			((SlaveMenuService) parentService).populateContributionManager(model, mgr, location);
		} else if (parentService instanceof WorkbenchMenuService) {
			((WorkbenchMenuService) parentService).populateContributionManager(model, mgr, location);
		}
	}

	/**
	 * @see org.eclipse.ui.menus.IMenuService#releaseContributions(org.eclipse.jface.action.ContributionManager)
	 */
	@Override
	public void releaseContributions(ContributionManager mgr) {
		parentService.releaseContributions(mgr);
	}

	/**
	 * Disposes contributions created by service for given part. See bug 537046.
	 */
	@Override
	public void clearContributions(PartSite site, MPart part) {
		if (parentService instanceof IMenuServiceWorkaround) {
			IMenuServiceWorkaround service = (IMenuServiceWorkaround) parentService;
			service.clearContributions(site, part);
		}
	}

	/**
	 * @return IEvaluationContext
	 * @see org.eclipse.ui.menus.IMenuService#getCurrentState()
	 */
	@Override
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
