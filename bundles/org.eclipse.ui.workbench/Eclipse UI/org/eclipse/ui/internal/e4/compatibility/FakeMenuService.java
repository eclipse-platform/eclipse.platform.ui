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

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.menus.AbstractContributionFactory;

import org.eclipse.ui.menus.IMenuService;

/**
 * @since 3.5
 *
 */
public class FakeMenuService implements IMenuService {

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
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
	}

}
