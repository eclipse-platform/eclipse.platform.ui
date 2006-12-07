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
package org.eclipse.ui.menus;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.services.IServiceWithSources;

/**
 * <p>
 * Provides services related to the menu architecture within the workbench. It
 * can be used to contribute additional items to the menu, tool bar and status
 * line.
 * </p>
 * <p>
 * This interface should not be implemented or extended by clients.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.3
 */
public interface IMenuService extends IServiceWithSources {

	/**
	 * Contribute and initialize the contribution factory. This should only be
	 * called once per factory. After the call, the factory should be treated as
	 * an unmodifiable object.
	 * <p>
	 * <b>Note:</b> In 3.3M4, this will make the factory available for any
	 * following calls to
	 * {@link #populateContributionManager(ContributionManager, String)}, but
	 * it will not add those contributions to already populated managers.
	 * </p>
	 * 
	 * @param factory
	 *            the contribution factory. Must not be <code>null</code>
	 */
	public void addContributionFactory(AbstractContributionFactory factory);

	/**
	 * Remove the contributed factory from the menu service. If the factory is
	 * not contained by this service, this call does nothing.
	 * <p>
	 * <b>Note:</b>In 3.3M4, this factory will no longer be called during
	 * {@link #populateContributionManager(ContributionManager, String)} calls,
	 * but outstanding contributions will not be removed from populated
	 * contribution managers.
	 * </p>
	 * 
	 * @param factory
	 *            the contribution factory to remove. Must not be
	 *            <code>null</code>.
	 */
	public void removeContributionFactory(AbstractContributionFactory factory);

	/**
	 * Populate a <code>ContributionManager</code> at the specified starting
	 * location with a set of <code>IContributionItems</code>s. It applies
	 * <code>AbstractContributionFactory</code>s that are stored against the
	 * provided location.
	 * 
	 * @param mgr
	 *            The ContributionManager to populate
	 * @param location
	 *            The starting location to begin populating this contribution
	 *            manager. The format is the Menu API URI format.
	 * @see #releaseContributions(ContributionManager)
	 */
	public void populateContributionManager(ContributionManager mgr,
			String location);

	/**
	 * Before calling dispose() on a ContributionManager populated by the menu
	 * service, you must inform the menu service to release its contributions.
	 * This takes care of unregistering any IContributionItems that have their
	 * visibleWhen clause managed by this menu service.
	 * 
	 * @param mgr
	 *            The manager that was populated by a call to
	 *            {@link #populateContributionManager(ContributionManager, String)}
	 */
	public void releaseContributions(ContributionManager mgr);

	/**
	 * Get the current state of eclipse as seen by the menu service.
	 * 
	 * @return an IEvaluationContext containing state variables.
	 * 
	 * @see org.eclipse.ui.ISources
	 */
	public IEvaluationContext getCurrentState();

	/**
	 * This method allows any <code>IContributionItem</code> to have its
	 * visibility managed by the menu service by providing a core expression.
	 * The core expression will be updated as the sources change. The item
	 * lifecycle must be managed by this service as well.
	 * <p>
	 * {@link IContributionItem#isVisible()} must return the value set by
	 * {@link IContributionItem#setVisible(boolean)}.
	 * </p>
	 * <p>
	 * When the visible expression changes, the service will call
	 * {@link IContributionItem#setVisible(boolean)} with the appropriate value
	 * and then {@link IContributionItem#update()}.
	 * </p>
	 * 
	 * @param item
	 *            An IContributionItem. Must not be <code>null</code>.
	 * @param visibleWhen
	 *            The visibleWhen expression. Must not be <code>null</code>.
	 * @see org.eclipse.ui.ISources
	 * @see #unregisterVisibleWhen(IContributionItem)
	 */
	public void registerVisibleWhen(IContributionItem item,
			Expression visibleWhen);

	/**
	 * If an IContributionItem was registered with the menu service, when it is
	 * no longer needed by any ContributionManager it must be unregistered.
	 * 
	 * @param item
	 *            the item to remove. Must not be <code>null</code>.
	 */
	public void unregisterVisibleWhen(IContributionItem item);
}
